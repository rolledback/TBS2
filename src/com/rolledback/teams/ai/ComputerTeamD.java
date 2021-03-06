package com.rolledback.teams.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.framework.Logger;
import com.rolledback.teams.Team;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamD extends ComputerTeam {
   
   final int animationDelay = 0;
   private HashMap<CapturableTile, HashSet<Unit>> captureSpots;
   private Coordinate avgEnemyPos;
   private CoordinateNode[] nodes = new CoordinateNode[game.getGameHeight() * game.getGameWidth()];
   private boolean capPriority;
   private int operationSizeLimit = 3;
   private int availOperations = 0;
   
   private double tauntChance = .65;
   private String[] generalTaunts = { "Come onnnnnnnnn!", "Wololololol", "Long time, no siege.", "My granny could scrap better than that.", "Dont point that thing at me." };
   private String[] triggeredTaunts = { "All hail, King of the losers!", "Nice city, I'll take it.", "You played two hours to die like this?", "Enemy sighted!", "Raiding party!" };
   
   public ComputerTeamD(String name, int size, int r, Game g, int n) {
      super(name, size, r, g, n);
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
   
   /**
    * Function called when it is this teams turn.
    */
   public void executeTurn() {
      Logger.consolePrint(getName() + " starting turn.", "ai");
      
      Random random = new Random();
      int rndIndex = random.nextInt(generalTaunts.length);
      if(Math.random() > tauntChance)
         game.getGUI().sendMessage(this, generalTaunts[rndIndex]);
      
      calcAvgEnemyPos();
      if(captureSpots == null)
         findCaptureSpots();
      else
         updateOperations();
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
            game.gameLogic(moveSpot.getX(), moveSpot.getY());
            game.repaint();
            delay(animationDelay);
            if(!units.contains(u))
               i--;
         }
      }
      game.getLogicLock().lock();
      Logger.consolePrint("Producing units.", "ai");
      sortFactories();
      Iterator<Factory> factoryIterator = factories.iterator();
      while(factoryIterator.hasNext()) {
         Factory currentFactory = factoryIterator.next();
         Random rand = new Random();
         int unitToProduce = rand.nextInt(productionList.size());
         int attempts = 0;
         do {
            unitToProduce = rand.nextInt(productionList.size());
            attempts++;
         }
         while(!currentFactory.produceUnit((UNIT_TYPE)productionList.keySet().toArray()[unitToProduce]) && attempts < 32);
      }
      
      if(units.size() / 2 > opponent.getUnits().size()) {
         if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[0]);
         else if(Math.random() > tauntChance)
            game.getGUI().sendMessage(this, triggeredTaunts[2]);
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
      Object[] closestEnemyTuple = aStarToClosestObject(game.getWorld().getTiles(), u, opponent);
      int closestEnemyDistance = (int)closestEnemyTuple[0];
      if(closestEnemyDistance == Integer.MAX_VALUE)
         return null;
      CoordinateNode bestMoveSpotNode = (CoordinateNode)closestEnemyTuple[2];
      Coordinate bestMoveSpot = new Coordinate(bestMoveSpotNode.getX(), bestMoveSpotNode.getY());
      capPriority = savedPriority;
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
   
   public int distanceFormula(int x1, int y1, int x2, int y2) {
      return (int)Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
   }
   
   public Object[] dijkstraToClosestObject(Tile[][] world, Unit unit, Team targetOwner) {
      Logger.consolePrint("Finding the next closest object.", "ai");
      Object[] resultsTuple = new Object[3];
      // initialize all nodes in the graph to have distance infinity
      ArrayList<CoordinateNode> nodes = new ArrayList<CoordinateNode>(game.getGameHeight() * game.getGameWidth());
      for(int r = 0; r < game.getGameHeight(); r++)
         for(int c = 0; c < game.getGameWidth(); c++)
            nodes.add(new CoordinateNode(unit.getMoveSet().contains(new Coordinate(c, r)), null, c, r, game.getWorld().getTiles()[r][c], Integer.MAX_VALUE));
      // set up the priority queue and set
      Comparator<CoordinateNode> comparator = new CoordinateNodeComparator();
      PriorityQueue<CoordinateNode> queue = new PriorityQueue<CoordinateNode>(25, comparator);
      HashSet<CoordinateNode> set = new HashSet<CoordinateNode>();
      
      // set starting node to have distance 0 and push it on the queue and add it to the set
      nodes.get(unit.getY() * game.getGameWidth() + unit.getX()).setfScore(0);
      nodes.get(unit.getY() * game.getGameWidth() + unit.getX()).setReachable(true);
      queue.offer(nodes.get(unit.getY() * game.getGameWidth() + unit.getX()));
      world[unit.getY()][unit.getX()].setOccupied(false);
      
      while(queue.size() > 0) {
         CoordinateNode n = queue.poll();
         Tile t = world[n.getY()][n.getX()];
         
         if(set.contains(n))
            continue;
         set.add(n);
         
         if((unit.canCapture(t) && (captureSpots.get(t).size() <= operationSizeLimit - 1 || captureSpots.get(t).contains(unit))) || (!capPriority && unit.canAttack(t))) {
            if(capPriority)
               captureSpots.get(t).add(unit);
            world[unit.getY()][unit.getX()].setOccupied(true);
            CoordinateNode moveNode = n;
            while(moveNode != null && !moveNode.isReachable())
               moveNode = moveNode.getPrev();
            resultsTuple[0] = moveNode.getfScore();
            resultsTuple[1] = new Coordinate(t.getX(), t.getY());
            resultsTuple[2] = moveNode;
            
            return resultsTuple;
         }
         else {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < xDirs.length; i++) {
               try {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
                  Tile tempTile = world[r][c];
                  // get the node associated with the tile
                  CoordinateNode temp = nodes.get(r * game.getGameWidth() + c);
                  // if it has not been visited, set prev, set distance, push on queue, add to set
                  if(unit.canTraverse(tempTile)) {
                     // if distance is less than current distance update it and its prev node
                     if(n.getfScore() + tempTile.getEffect().getMoveCost() < temp.getfScore()) {
                        temp.setPrev(n);
                        temp.setfScore(n.getfScore() + tempTile.getEffect().getMoveCost());
                        queue.offer(temp);
                     }
                  }
               }
               catch(Exception e) {
                  // out of bounds
               }
            }
         }
      }
      world[unit.getY()][unit.getX()].setOccupied(true);
      resultsTuple[0] = Integer.MAX_VALUE;
      resultsTuple[1] = null;
      resultsTuple[2] = null;
      return resultsTuple;
   }
   
   public Object[] aStarToClosestObject(Tile[][] world, Unit unit, Team targetOwner) {
      Logger.consolePrint("Finding the next closest object.", "ai");
      Object[] resultsTuple = new Object[3];
      // find what you think the closest object is
      Coordinate estimatedGoal = estimatedClosestObject(unit);
      // initialize all nodes in the graph to have distance infinity
      Arrays.fill(nodes, null);
      // set up the priority queue and set
      Comparator<CoordinateNode> comparator = new CoordinateNodeComparator();
      PriorityQueue<CoordinateNode> openSetQueue = new PriorityQueue<CoordinateNode>(25, comparator);
      HashSet<CoordinateNode> openSet = new HashSet<CoordinateNode>();
      HashSet<CoordinateNode> closedSet = new HashSet<CoordinateNode>();
      
      // set starting node to have distance 0 and push it on the queue and add it to the set
      nodes[unit.getY() * game.getGameWidth() + unit.getX()] = new CoordinateNode(true, null, unit.getX(), unit.getY(), unit.getCurrentTile(), Integer.MAX_VALUE, Integer.MAX_VALUE);
      nodes[unit.getY() * game.getGameWidth() + unit.getX()].setgScore(0);
      nodes[unit.getY() * game.getGameWidth() + unit.getX()].setfScore(heuristic(unit.getX(), unit.getY(), estimatedGoal.getX(), estimatedGoal.getY()));
      openSetQueue.offer(nodes[unit.getY() * game.getGameWidth() + unit.getX()]);
      openSet.add(openSetQueue.peek());
      world[unit.getY()][unit.getX()].setOccupied(false);
      
      while(openSetQueue.size() > 0) {
         CoordinateNode current = openSetQueue.poll();
         Tile t = current.getTile();
         if((unit.canCapture(t) && (captureSpots.get(t).size() < operationSizeLimit || captureSpots.get(t).contains(unit))) || (!capPriority && unit.canAttack(t))) {
            if(capPriority) {
               if(captureSpots.get(t).add(unit) && captureSpots.get(t).size() >= operationSizeLimit)
                  availOperations--;
               if(availOperations == 0) {
                  availOperations = captureSpots.size() - factories.size() - cities.size();
                  operationSizeLimit++;
               }
            }
            world[unit.getY()][unit.getX()].setOccupied(true);
            CoordinateNode moveNode = current;
            
            while(moveNode != null && !moveNode.isReachable())
               moveNode = moveNode.getPrev();
            resultsTuple[0] = moveNode.getfScore();
            resultsTuple[1] = new Coordinate(t.getX(), t.getY());
            resultsTuple[2] = moveNode;
            return resultsTuple;
         }
         else {
            openSet.remove(current);
            closedSet.add(current);
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < xDirs.length; i++) {
               try {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
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
               catch(Exception e) {
                  // out of bounds
               }
            }
         }
      }
      world[unit.getY()][unit.getX()].setOccupied(true);
      resultsTuple[0] = Integer.MAX_VALUE;
      resultsTuple[1] = null;
      resultsTuple[2] = null;
      return resultsTuple;
   }
   
   public Coordinate estimatedClosestObject(Unit unit) {
      Coordinate closest = null;
      int distance = Integer.MAX_VALUE;
      
      for(Unit e: opponent.getUnits()) {
         int d = heuristic(unit.getX(), unit.getY(), e.getX(), e.getY());
         if(d < distance) {
            distance = d;
            closest = new Coordinate(e.getX(), e.getY());
         }
      }
      
      if(unit.getClassification() == UNIT_CLASS.INFANTRY) {
         for(Map.Entry<CapturableTile, HashSet<Unit>> entry: captureSpots.entrySet()) {
            if(!unit.canCapture(entry.getKey()))
               continue;
            if(entry.getValue().contains(unit)) {
               closest = new Coordinate(entry.getKey().getX(), entry.getKey().getY());
               break;
            }
            int d = heuristic(unit.getX(), unit.getY(), entry.getKey().getX(), entry.getKey().getY());
            
            if(d < distance) {
               distance = d;
               closest = new Coordinate(entry.getKey().getX(), entry.getKey().getY());
            }
         }
      }
      
      return closest;
   }
   
   public int heuristic(int x1, int y1, int x2, int y2) {
      int dx = x1 - x2;
      int dy = y1 - y2;
      dx = ((dx >> 31) ^ dx) - (dx >> 31);
      dy = ((dy >> 31) ^ dy) - (dy >> 31);
      return Math.abs(x1 - x2) + Math.abs(y1 - y2);
   }
   
   public Object[] bfsToClosestObject(Tile[][] world, Unit unit, Team targetOwner) {
      Object[] resultsTuple = new Object[3];
      
      LinkedList<CoordinateNode> queue = new LinkedList<CoordinateNode>();
      HashSet<CoordinateNode> set = new HashSet<CoordinateNode>();
      
      queue.offer(new CoordinateNode(true, null, unit.getX(), unit.getY(), null, 0));
      set.add(queue.peek());
      queue.offer(null);
      world[unit.getY()][unit.getX()].setOccupied(false);
      
      int distance = 0;
      while(queue.size() > 1) {
         CoordinateNode n = queue.poll();
         if(n == null) {
            distance++;
            queue.offer(null);
            continue;
         }
         Tile t = world[n.getY()][n.getX()];
         if((unit.canCapture(t) && (captureSpots.get(t).size() < operationSizeLimit || captureSpots.get(t).contains(unit))) || (!capPriority && unit.canAttack(t))) {
            if(capPriority)
               captureSpots.get(t).add(unit);
            world[unit.getY()][unit.getX()].setOccupied(true);
            CoordinateNode moveNode = n;
            while(moveNode != null && !moveNode.isReachable()) {
               moveNode = moveNode.getPrev();
            }
            resultsTuple[0] = distance;
            resultsTuple[1] = new Coordinate(t.getX(), t.getY());
            resultsTuple[2] = moveNode;
            
            return resultsTuple;
         }
         else {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < xDirs.length; i++)
               try {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
                  CoordinateNode temp = new CoordinateNode(unit.getMoveSet().contains(new Coordinate(c, r)), n, c, r, null, 0);
                  Tile tempTile = world[r][c];
                  if(!set.contains(temp) && unit.canTraverse(tempTile)) {
                     queue.offer(temp);
                     set.add(temp);
                  }
               }
               catch(Exception e) {
                  // out of bounds
               }
         }
      }
      world[unit.getY()][unit.getX()].setOccupied(true);
      resultsTuple[0] = Integer.MAX_VALUE;
      resultsTuple[1] = null;
      resultsTuple[2] = null;
      return resultsTuple;
   }
}