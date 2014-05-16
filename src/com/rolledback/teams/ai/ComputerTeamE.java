package com.rolledback.teams.ai;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.framework.Logger;
import com.rolledback.teams.Team;
import com.rolledback.teams.technology.Technology;
import com.rolledback.teams.technology.Technology.TECH_NAME;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamE extends ComputerTeam {
   
   final int animationDelay = 0;
   private HashMap<CapturableTile, HashSet<Unit>> captureSpots;
   private Coordinate avgEnemyPos;
   private ArrayList<aStarCallable> calls;
   private int spotEvalLimit = 3;
   
   private boolean capPriority;
   private int operationSizeLimit = 1;
   private int availOperations = 0;
   
   private double tauntChance = .65;
   private String[] generalTaunts = { "Come onnnnnnnnn!", "Wololololol", "Long time, no siege.", "My granny could scrap better than that.", "Dont point that thing at me." };
   private String[] triggeredTaunts = { "All hail, King of the losers!", "Nice city, I'll take it.", "You played two hours to die like this?", "Enemy sighted!", "Raiding party!" };
   
   public ComputerTeamE(String name, int size, int r, Game g, int n) {
      super(name, size, r, g, n);
   }
   
   /**
    * Function called when it is this teams turn.
    */
   public void executeTurn() {
      Logger.consolePrint(getName() + " starting turn.", "ai");
      
      Random random = new Random();
      int rndIndex = random.nextInt(generalTaunts.length);
      if(Math.random() > tauntChance)
         game.getGUI().sendMessage(this, generalTaunts[rndIndex]);
      
      moveUnits();
      produceUnits();
      
      if(units.size() / 2 > opponent.getUnits().size()) {
         if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[0]);
         else if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[2]);
      }
   }
   
   public void moveUnits() {
      Logger.consolePrint("Moving units.", "ai");
      calcAvgEnemyPos();
      
      if(captureSpots == null)
         findCaptureSpots();
      else
         updateOperations();
      
      if(calls == null) {
         calls = new ArrayList<aStarCallable>();
         for(int x = 0; x < spotEvalLimit; x++)
            calls.add(new aStarCallable());
      }
      
      sortUnits();
      for(int i = 0; i < units.size(); i++) {
         if(opponent.getUnits().size() == 0)
            return;
         Unit u = units.get(i);
         Logger.consolePrint("Deciding move spot for: " + u, "ai");
         Coordinate moveSpot = moveUnit(u);
         Logger.consolePrint("Move spot is: " + moveSpot, "ai");
         if(moveSpot != null && !(moveSpot.getX() == u.getX() && moveSpot.getY() == u.getY())) {
            game.gameLogic(u.getX(), u.getY());
            game.repaint();
            delay(animationDelay);
            Logger.consolePrint("Move spot: " + moveSpot, "ai");
            game.gameLogic(moveSpot.getX(), moveSpot.getY());
            game.repaint();
            delay(animationDelay);
            if(!units.contains(u))
               i--;
         }
      }
   }
   
   /**
    * Find all possible capturable tiles, regardless of owner, and then store them in the
    * captureSpots hash map.
    */
   public void findCaptureSpots() {
      Logger.consolePrint("Finding all capturable tiles.", "ai");
      captureSpots = new HashMap<CapturableTile, HashSet<Unit>>();
      Tile[][] world = game.getWorld().getTiles();
      for(int r = 0; r < world.length; r++)
         for(int c = 0; c < world[r].length; c++)
            if(world[r][c] instanceof CapturableTile)
               captureSpots.put((CapturableTile)world[r][c], new HashSet<Unit>(operationSizeLimit));
   }
   
   /**
    * Calculate the average x and y position of all enemy units. Then save these values in
    * avgEnemyPos.
    */
   public void calcAvgEnemyPos() {
      Logger.consolePrint("Calculating average enemy position.", "ai");
      int sumX = 0;
      int sumY = 0;
      for(Unit u: opponent.getUnits()) {
         sumX += u.getX();
         sumY += u.getY();
      }
      
      int avgX;
      int avgY;
      if(opponent.getUnits().size() > 0) {
         avgX = sumX / opponent.getUnits().size();
         avgY = sumY / opponent.getUnits().size();
      }
      else {
         avgX = 0;
         avgY = 0;
      }
      avgEnemyPos = new Coordinate(avgX, avgY);
      Logger.consolePrint("New avg position: " + avgEnemyPos, "ai");
   }
   
   /**
    * Determine if infantry units need to prioritize capturing. If there any unowned capturable
    * tiles, or if the other team owns more capturable tiles, than capPriority is set to true. If
    * capPriority is set to true then the function will iterate through captureSpots. If the tile
    * has no owner or is owned by the other team, then availOperations is increased. If the hash set
    * associated with each capturable tile size is equal to operationSizeLimit, then availOperations
    * is decreased. If after all keys have been iterated over there are no avail operations, then
    * the operationSizeLimit is increased. This system ensures that infantry are dispersed to
    * different cities in a semi equally fashion.
    */
   public void updateOperations() {
      if(cities.size() + factories.size() <= opponent.getCities().size() + opponent.getFactories().size())
         capPriority = true;
      else if(cities.size() + factories.size() + opponent.getCities().size() + opponent.getFactories().size() < captureSpots.size())
         capPriority = true;
      else
         capPriority = false;
      Logger.consolePrint("Capture priority has been set to: " + capPriority, "ai");
      if(capPriority) {
         availOperations = 0;
         for(Map.Entry<CapturableTile, HashSet<Unit>> temp: captureSpots.entrySet()) {
            if(temp.getKey().getOwner() == null || !temp.getKey().getOwner().equals(this))
               availOperations++;
            if(temp.getValue().size() == operationSizeLimit)
               availOperations--;
         }
         if(availOperations == 0) {
            operationSizeLimit++;
            Logger.consolePrint("Increasing operation size limit to: " + operationSizeLimit, "ai");
         }
         Logger.consolePrint("Avail operations: " + availOperations, "ai");
         Logger.consolePrint("Operation size limit: " + operationSizeLimit, "ai");
      }
   }
   
   public void produceUnits() {
      Logger.consolePrint("Producing units.", "ai");
      game.getLogicLock().lock();
      
      boolean enemyVehicles = false;
      int numEnemyTanks = 0;
      int numEnemyTDs = 0;
      for(Unit e: opponent.getUnits())
         if(e.getClassification() == UNIT_CLASS.VEHICLE) {
            enemyVehicles = true;
            if(e.getType() == UNIT_TYPE.TANK)
               numEnemyTanks++;
            else
               numEnemyTDs++;
         }
      
      int numTDs = 0;
      int numTanks = 0;
      for(Unit u: units) {
         if(u.getType() == UNIT_TYPE.TANK_DEST)
            numTDs++;
         if(u.getType() == UNIT_TYPE.TANK)
            numTanks++;
      }
      
      sortFactories();
      Iterator<Factory> factoryIterator = factories.iterator();
      while(factoryIterator.hasNext()) {
         Factory currentFactory = factoryIterator.next();
         if(currentFactory.isOccupied())
            continue;
         if(this.getResources() < productionList.get(UNIT_TYPE.INFANTRY))
            break;
         
         if(capPriority) {
            if(numTanks < (opponent.getUnits().size() * .05))
               if(currentFactory.produceUnit(UNIT_TYPE.TANK))
                  continue;
            if(this.getTechTree().keySet().contains(TECH_NAME.GPS) && units.size() > 1)
               Technology.researchTech(this, TECH_NAME.GPS);
            if(enemyVehicles)
               if(currentFactory.produceUnit(UNIT_TYPE.RPG))
                  continue;
            currentFactory.produceUnit(UNIT_TYPE.INFANTRY);
         }
         else {
            if(this.getTechTree().keySet().contains(TECH_NAME.APCR) && units.size() > 1)
               Technology.researchTech(this, TECH_NAME.APCR);
            if(enemyVehicles) {
               if(numTDs < (numEnemyTanks + numEnemyTDs * .25))
                  if(currentFactory.produceUnit(UNIT_TYPE.TANK_DEST))
                     continue;
               if(currentFactory.produceUnit(UNIT_TYPE.TANK))
                  continue;
               if(currentFactory.produceUnit(UNIT_TYPE.RPG))
                  continue;
               if(currentFactory.produceUnit(UNIT_TYPE.INFANTRY))
                  continue;
            }
            else {
               if(numTanks < (opponent.getUnits().size() * .1))
                  if(currentFactory.produceUnit(UNIT_TYPE.TANK))
                     continue;
               currentFactory.produceUnit(UNIT_TYPE.INFANTRY);
            }
         }
      }
      game.getLogicLock().unlock();
   }
   
   /**
    * Determines what type of move the given unit should executer. First sees if it can capture or
    * attack, and if it can do neither the unit will simply decide a new spot to move to, if any.
    * 
    * @param u unit to possibly move.
    * @return the coordinate to move the unit to, or null if the unit should not be moved.
    */
   public Coordinate moveUnit(Unit u) {
      u.calcMoveSpots(false);
      if(u.getCaptureSet().size() != 0) {
         if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[1]);
         else if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[4]);
         return captureMove(u);
      }
      else if(u.getAttackSet().size() != 0)
         return attackMove(u);
      else if(u.getMoveSet().size() != 0) {
         if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[3]);
         return simpleMove(u);
      }
      return null;
   }
   
   /**
    * Determines where the unit should move next, if anywhere.
    * 
    * @param u unit to possibly move.
    * @return the coordinate to move the unit to, or null if the unit should not be moved.
    */
   public Coordinate simpleMove(Unit u) {
      Logger.consolePrint("Conduting a simple move.", "ai");
      boolean savedPriority = capPriority;
      capPriority = (capPriority && (u.getClassification() == UNIT_CLASS.INFANTRY));
      Coordinate bestMoveSpot = null;
      try {
         bestMoveSpot = findClosestObject(game.getWorld().getTiles(), u, opponent);
      }
      catch(Exception e) {
         e.printStackTrace();
      }
      if(bestMoveSpot == null)
         return null;
      capPriority = savedPriority;
      Logger.consolePrint("Best move spot: " + bestMoveSpot, "ai");
      return bestMoveSpot;
   }
   
   public Coordinate attackMove(Unit u) {
      Logger.consolePrint("Conducting an attack move.", "ai");
      HashMap<Coordinate, Integer> attackDistances = new HashMap<Coordinate, Integer>();
      for(Coordinate c: u.getAttackSet()) {
         attackDistances.put(c, Integer.MAX_VALUE);
         int d = distanceFormula(u.getX(), u.getY(), c.getX(), c.getY());
         if(d < attackDistances.get(c))
            attackDistances.put(c, d);
      }
      
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: attackDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null)
         return minEntry.getKey();
      return null;
   }
   
   public Coordinate captureMove(Unit u) {
      Logger.consolePrint("Conducting a capture move.", "ai");
      HashMap<Coordinate, Integer> captureDistances = new HashMap<Coordinate, Integer>();
      for(Coordinate c: u.getCaptureSet()) {
         captureDistances.put(c, Integer.MAX_VALUE);
         int d = distanceFormula(u.getX(), u.getY(), c.getX(), c.getY());
         if(d < captureDistances.get(c))
            captureDistances.put(c, d);
      }
      
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: captureDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null) {
         CapturableTile temp = (CapturableTile)game.getWorld().getTiles()[minEntry.getKey().getY()][minEntry.getKey().getX()];
         captureSpots.put(temp, new HashSet<Unit>(4));
         availOperations--;
         if(availOperations == 0) {
            availOperations = captureSpots.size() - factories.size() - cities.size() - 1;
            operationSizeLimit++;
         }
         if(factories.size() + cities.size() == captureSpots.size()) {
            availOperations = 0;
            operationSizeLimit = 0;
            capPriority = false;
         }
         return minEntry.getKey();
      }
      return null;
   }
   
   public void sortFactories() {
      Logger.consolePrint("Sorting factories.", "ai");
      final int avgX = avgEnemyPos.getX();
      final int avgY = avgEnemyPos.getY();
      
      Collections.sort(factories, new Comparator<Factory>() {
         public int compare(Factory f1, Factory f2) {
            return distanceFormula(f1.getX(), f1.getY(), avgX, avgY) - distanceFormula(f2.getX(), f2.getY(), avgX, avgY);
         }
      });
   }
   
   public void sortUnits() {
      Logger.consolePrint("Sorting units.", "ai");
      final int avgX = avgEnemyPos.getX();
      final int avgY = avgEnemyPos.getY();
      for(Unit u: units) {
         u.calcMoveSpots(false);
      }
      Collections.sort(units, new Comparator<Unit>() {
         public int compare(Unit u1, Unit u2) {
            if(u1.getCaptureSet().size() != u2.getCaptureSet().size()) {
               return u2.getCaptureSet().size() - u1.getCaptureSet().size();
            }
            if(u1.getAttackSet().size() != u2.getAttackSet().size()) {
               return u2.getAttackSet().size() - u1.getAttackSet().size();
            }
            if(distanceFormula(u1.getX(), u1.getY(), avgX, avgY) != distanceFormula(u2.getX(), u2.getY(), avgX, avgY)) {
               return distanceFormula(u1.getX(), u1.getY(), avgX, avgY) - distanceFormula(u2.getX(), u2.getY(), avgX, avgY);
            }
            else
               return u2.getMoveSet().size() - u1.getMoveSet().size();
         }
      });
   }
   
   public Coordinate findClosestObject(Tile[][] world, Unit unit, Team targetOwner) throws Exception {
      Logger.consolePrint("Finding the next closest object.", "ai");
      ExecutorService threadPool = Executors.newFixedThreadPool(spotEvalLimit);
      PriorityQueue<CoordinateNode> goals = objectDistances(unit);
      Map.Entry<CoordinateNode[], Integer> minTarget = null;
      Set<Future<Map.Entry<CoordinateNode[], Integer>>> set = new HashSet<Future<Map.Entry<CoordinateNode[], Integer>>>();
      
      for(int x = 0; x < spotEvalLimit && goals.size() > 0; x++) {
         Logger.consolePrint("Finding path for goal " + x + " on thread [" + calls.get(x).getID() + "]", "ai");
         calls.get(x).setArgs(goals.poll(), world, unit, targetOwner);
         Future<Map.Entry<CoordinateNode[], Integer>> future = threadPool.submit(calls.get(x));
         set.add(future);
      }
      
      for(Future<Map.Entry<CoordinateNode[], Integer>> future: set) {
         if(minTarget == null || minTarget.getValue() > future.get().getValue())
            minTarget = future.get();
         future.cancel(true);
         future = null;
      }
      set = null;
      threadPool.shutdown();
      threadPool = null;
      if(minTarget != null && minTarget.getValue() != Integer.MAX_VALUE) {
         Tile targetTile = world[minTarget.getKey()[1].getY()][minTarget.getKey()[1].getX()];
         if(capPriority && targetTile instanceof CapturableTile) {
            CapturableTile temp = (CapturableTile)targetTile;
            if(captureSpots.get(temp).add(unit) && captureSpots.get(temp).size() >= operationSizeLimit)
               availOperations--;
            if(availOperations == 0) {
               availOperations = captureSpots.size() - factories.size() - cities.size();
               operationSizeLimit++;
               Logger.consolePrint("Simple move target resulting in 0 avail ops. Increasing op limit.", "ai");
            }
         }
         return new Coordinate(minTarget.getKey()[0].getX(), minTarget.getKey()[0].getY());
      }
      return null;
   }
   
   public PriorityQueue<CoordinateNode> objectDistances(Unit unit) {
      Comparator<CoordinateNode> comparator = new CoordinateNodeComparator();
      PriorityQueue<CoordinateNode> returnQueue = new PriorityQueue<CoordinateNode>(opponent.getUnits().size() + captureSpots.size(), comparator);
      for(Unit enemy: opponent.getUnits()) {
         int fScore = heuristic(unit.getX(), unit.getY(), enemy.getX(), enemy.getY());
         returnQueue.offer(new CoordinateNode(true, null, enemy.getX(), enemy.getY(), null, fScore));
      }
      
      if(unit.getClassification() == UNIT_CLASS.INFANTRY) {
         if(capPriority)
            returnQueue.clear();
         for(Map.Entry<CapturableTile, HashSet<Unit>> entry: captureSpots.entrySet()) {
            if(!unit.canCapture(entry.getKey()))
               continue;
            if(entry.getValue().contains(unit)) {
               returnQueue.clear();
               int fScore = heuristic(unit.getX(), unit.getY(), entry.getKey().getX(), entry.getKey().getY());
               returnQueue.offer(new CoordinateNode(true, null, entry.getKey().getX(), entry.getKey().getY(), null, fScore));
               break;
            }
            if(entry.getValue().size() < operationSizeLimit) {
               int fScore = heuristic(unit.getX(), unit.getY(), entry.getKey().getX(), entry.getKey().getY());
               returnQueue.offer(new CoordinateNode(true, null, entry.getKey().getX(), entry.getKey().getY(), null, fScore));
            }
         }
      }
      return returnQueue;
   }
   
   public int heuristic(int x1, int y1, int x2, int y2) {
      int dx = x1 - x2;
      int dy = y1 - y2;
      dx = ((dx >> 31) ^ dx) - (dx >> 31);
      dy = ((dy >> 31) ^ dy) - (dy >> 31);
      return Math.abs(x1 - x2) + Math.abs(y1 - y2);
   }
   
   public Coordinate nodeConverter(CoordinateNode n) {
      return new Coordinate(n.getX(), n.getY());
   }
   
   public int distanceFormula(int x1, int y1, int x2, int y2) {
      return (int)Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
   }
   
   public class aStarCallable implements Callable<Map.Entry<CoordinateNode[], Integer>> {
      private CoordinateNode target;
      private CoordinateNode[] nodes = new CoordinateNode[game.getGameHeight() * game.getGameWidth()];
      private Tile[][] world;
      private Unit currUnit;
      private Team targetOwner;
      private int id;
      
      public aStarCallable() {
         target = null;
         id = 100 + (int)(Math.random() * ((999 - 100) + 100));
      }
      
      public void setArgs(CoordinateNode t, Tile[][] w, Unit u, Team o) {
         target = t;
         world = w;
         currUnit = u;
         targetOwner = o;
      }
      
      public int getID() {
         return id;
      }
      
      public Map.Entry<CoordinateNode[], Integer> call() {
         Logger.consolePrint("[" + id + "] Path finding thread running for target: " + target, "ai");
         return aStarToClosestObject(world, currUnit, targetOwner);
      }
      
      public Map.Entry<CoordinateNode[], Integer> aStarToClosestObject(Tile[][] world, Unit unit, Team targetOwner) {
         // find what you think the closest object is
         Coordinate estimatedGoal = nodeConverter(target);
         // initialize all nodes in the graph to have distance infinity
         Arrays.fill(nodes, null);
         // set up the priority queue and set
         Comparator<CoordinateNode> comparator = new CoordinateNodeComparator();
         PriorityQueue<CoordinateNode> openSetQueue = new PriorityQueue<CoordinateNode>(100, comparator);
         HashSet<CoordinateNode> openSet = new HashSet<CoordinateNode>();
         HashSet<CoordinateNode> closedSet = new HashSet<CoordinateNode>();
         
         // set starting node to have distance 0 and push it on the queue and add it to the set
         nodes[unit.getY() * game.getGameWidth() + unit.getX()] = new CoordinateNode(true, null, unit.getX(), unit.getY(), unit.getCurrentTile(), Integer.MAX_VALUE, Integer.MAX_VALUE);
         nodes[unit.getY() * game.getGameWidth() + unit.getX()].setgScore(0);
         nodes[unit.getY() * game.getGameWidth() + unit.getX()].setfScore(heuristic(unit.getX(), unit.getY(), estimatedGoal.getX(), estimatedGoal.getY()));
         openSetQueue.offer(nodes[unit.getY() * game.getGameWidth() + unit.getX()]);
         openSet.add(openSetQueue.peek());
         
         while(openSetQueue.size() > 0) {
            CoordinateNode current = openSetQueue.poll();
            Tile t = current.getTile();
            if((capPriority && current.equals(target)) || (!capPriority && (unit.canAttack(t) || unit.canCapture(t)))) {
               CoordinateNode moveNode = current;
               while(moveNode != null && !moveNode.isReachable())
                  moveNode = moveNode.getPrev();
               Logger.consolePrint("[" + id + "] Returning, final cost of " + current.getfScore() + ", move spot: " + moveNode, "ai");
               return new AbstractMap.SimpleEntry<CoordinateNode[], Integer>(new CoordinateNode[] { moveNode, current }, current.getfScore());
            }
            else {
               openSet.remove(current);
               closedSet.add(current);
               int[] yDirs = { 0, 0, 1, -1 };
               int[] xDirs = { 1, -1, 0, 0 };
               for(int i = 0; i < xDirs.length; i++) {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
                  if(r < 0 || r >= world.length || c < 0 || c >= world[0].length)
                     continue;
                  CoordinateNode neighbor;
                  if(nodes[(r * game.getGameWidth()) + c] == null) {
                     neighbor = new CoordinateNode(unit.getMoveSet().contains(new Coordinate(c, r)), current, c, r, game.getWorld().getTiles()[r][c], Integer.MAX_VALUE, Integer.MAX_VALUE);
                     nodes[(r * game.getGameWidth()) + c] = neighbor;
                  }
                  else
                     neighbor = nodes[r * game.getGameWidth() + c];
                  if(closedSet.contains(neighbor))
                     continue;
                  
                  Tile neighborTile = neighbor.getTile();
                  int tentative_gScore = current.getgScore() + neighbor.getTile().getEffect().getMoveCost();
                  if((!openSet.contains(neighbor) || tentative_gScore < neighbor.getgScore()) && unit.canTraverse(neighborTile)) {
                     neighbor.setPrev(current);
                     neighbor.setgScore(tentative_gScore);
                     neighbor.setfScore((int)(tentative_gScore + (heuristic(c, r, estimatedGoal.getX(), estimatedGoal.getY()))));
                     if(!openSet.contains(neighbor)) {
                        openSetQueue.remove(neighbor);
                        openSetQueue.offer(neighbor);
                        openSet.add(neighbor);
                     }
                  }
               }
            }
         }
         Logger.consolePrint("[" + id + "] Returning, target not found.", "ai");
         return new AbstractMap.SimpleEntry<CoordinateNode[], Integer>(null, Integer.MAX_VALUE);
      }
   }
   
}
