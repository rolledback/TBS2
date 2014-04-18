package com.rolledback.teams.ai;

import java.util.ArrayList;
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
   
   final int animationDelay = 100;
   private HashMap<CapturableTile, HashSet<Unit>> captureSpots;
   private int operationSizeLimit;
   private Coordinate avgEnemyPos;
   
   public ComputerTeamD(String name, int size, int r, Game g, int n) {
      super(name, size, r, g, n);
   }
   
   public void findCaptureSpots() {
      Logger.consolePrint("Finding all capturable tiles.", "ai");
      captureSpots = new HashMap<CapturableTile, HashSet<Unit>>();
      operationSizeLimit = 3;
      Tile[][] world = game.getWorld().getTiles();
      for(int r = 0; r < world.length; r++)
         for(int c = 0; c < world[r].length; c++)
            if(world[r][c] instanceof CapturableTile)
               captureSpots.put((CapturableTile)world[r][c], new HashSet<Unit>(4));
   }
   
   public void executeTurn() {
      calcAvgEnemyPos();
      boolean capPriority = false;
      if(captureSpots == null)
         findCaptureSpots();
      else {
         if(cities.size() + factories.size() <= opponent.getCities().size() + opponent.getFactories().size())
            capPriority = true;
         else if(cities.size() + factories.size() + opponent.getCities().size() + opponent.getFactories().size() < captureSpots.size())
            capPriority = true;
         if(capPriority) {
            int availOperations = 0;
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
      sortUnits();
      for(int i = 0; i < units.size(); i++) {
         if(opponent.getUnits().size() == 0)
            return;
         Unit u = units.get(i);
         Logger.consolePrint("Deciding move spot for: " + u, "ai");
         Coordinate moveSpot = moveUnit(u, capPriority);
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
      game.getLogicLock().unlock();
      
   }
   
   public Coordinate moveUnit(Unit u, boolean capPriority) {
      u.calcMoveSpots(false);
      if(u.getCaptureSet().size() != 0)
         return captureMove(u);
      else if(u.getAttackSet().size() != 0)
         return attackMove(u);
      else if(u.getMoveSet().size() != 0)
         return simpleMove(u, capPriority);
      return null;
   }
   
   public Coordinate simpleMove(Unit u, boolean capPriority) {
      Logger.consolePrint("Conduting a simple move.", "ai");
      capPriority = (capPriority && (u.getClassification() == UNIT_CLASS.INFANTRY));
      long start = System.currentTimeMillis();
      Object[] closestEnemyTuple = aStarToClosestObject(game.getWorld().getTiles(), u, opponent, capPriority);
      long end = System.currentTimeMillis();
      System.out.println((end - start));
      int closestEnemyDistance = (int)closestEnemyTuple[0];
      if(closestEnemyDistance == Integer.MAX_VALUE) {
         if(capPriority)
            return simpleMove(u, false);
         return null;
      }
      CoordinateNode bestMoveSpotNode = (CoordinateNode)closestEnemyTuple[2];
      Coordinate bestMoveSpot = new Coordinate(bestMoveSpotNode.getX(), bestMoveSpotNode.getY());
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
         return minEntry.getKey();
      }
      return null;
   }
   
   public void calcAvgEnemyPos() {
      Logger.consolePrint("Calculating average enemy position.", "ai");
      for(Unit u: units) {
         u.calcMoveSpots(false);
      }
      
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
   
   public void sortFactories() {
      Logger.consolePrint("Sorting factores.", "ai");
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
      return ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
   }
   
   public Object[] djikstraToClosestObject(Tile[][] world, Unit unit, Team targetOwner, boolean capPriority) {
      Logger.consolePrint("Finding the next closest object.", "ai");
      Object[] resultsTuple = new Object[3];
      // initialize all nodes in the graph to have distance infinity
      ArrayList<CoordinateNode> nodes = new ArrayList<CoordinateNode>(game.getGameHeight() * game.getGameWidth());
      for(int r = 0; r < game.getGameHeight(); r++)
         for(int c = 0; c < game.getGameWidth(); c++)
            nodes.add(new CoordinateNode(unit.getMoveSet().contains(new Coordinate(c, r)), null, c, r, game.getWorld().getTiles()[r][c], Integer.MAX_VALUE));
      // set up the priority queue and set
      Comparator<CoordinateNode> comparator = new TileCostComparator();
      PriorityQueue<CoordinateNode> queue = new PriorityQueue<CoordinateNode>(25, comparator);
      HashSet<CoordinateNode> set = new HashSet<CoordinateNode>();
      
      // set starting node to have distance 0 and push it on the queue and add it to the set
      nodes.get(unit.getY() * game.getGameWidth() + unit.getX()).setfScore(0);
      nodes.get(unit.getY() * game.getGameWidth() + unit.getX()).setReachable(true);
      queue.offer(nodes.get(unit.getY() * game.getGameWidth() + unit.getX()));
      set.add(queue.peek());
      world[unit.getY()][unit.getX()].setOccupied(false);
      
      while(queue.size() > 0) {
         CoordinateNode n = queue.poll();
         Tile t = world[n.getY()][n.getX()];
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
                  if(!set.contains(temp) && unit.canTraverse(tempTile)) {
                     temp.setPrev(n);
                     temp.setfScore(n.getfScore() + tempTile.getEffect().getMoveCost());
                     queue.offer(temp);
                     set.add(temp);
                  }
                  
                  // if distance through this node is less than it's current distance update it and
                  // its previous node
                  if(n.getfScore() + tempTile.getEffect().getMoveCost() < temp.getfScore()) {
                     temp.setPrev(n);
                     temp.setfScore(n.getfScore() + tempTile.getEffect().getMoveCost());
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
   
   public Object[] aStarToClosestObject(Tile[][] world, Unit unit, Team targetOwner, boolean capPriority) {
      Logger.consolePrint("Finding the next closest object.", "ai");
      Object[] resultsTuple = new Object[3];
      // find what you think the closest object is
      Coordinate estimatedGoal = estimatedClosestObject(unit, capPriority);
      // initialize all nodes in the graph to have distance infinity
      CoordinateNode[] nodes = new CoordinateNode[game.getGameHeight() * game.getGameWidth()];
      // set up the priority queue and set
      Comparator<CoordinateNode> comparator = new TileCostComparator();
      PriorityQueue<CoordinateNode> queue = new PriorityQueue<CoordinateNode>(25, comparator);
      HashSet<CoordinateNode> set = new HashSet<CoordinateNode>();
      
      // set starting node to have distance 0 and push it on the queue and add it to the set
      nodes[unit.getY() * game.getGameWidth() + unit.getX()] = new CoordinateNode(true, null, unit.getX(), unit.getY(), unit.getCurrentTile(), Integer.MAX_VALUE, Integer.MAX_VALUE);
      nodes[unit.getY() * game.getGameWidth() + unit.getX()].setgScore(0);
      nodes[unit.getY() * game.getGameWidth() + unit.getX()].setfScore(heuristic(unit.getX(), unit.getY(), estimatedGoal.getX(), estimatedGoal.getY()));
      queue.offer(nodes[unit.getY() * game.getGameWidth() + unit.getX()]);
      world[unit.getY()][unit.getX()].setOccupied(false);
      
      while(queue.size() > 0) {
         CoordinateNode current = queue.poll();
         // System.out.println("Current node: " + n);
         Tile t = current.getTile();
         if((unit.canCapture(t) && (captureSpots.get(t).size() <= operationSizeLimit - 1 || captureSpots.get(t).contains(unit))) || (!capPriority && unit.canAttack(t))) {
            if(capPriority)
               captureSpots.get(t).add(unit);
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
            set.add(current);
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < xDirs.length; i++) {
               try {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
                  CoordinateNode neighbor;
                  if(nodes[(r * game.getGameWidth()) + c] == null)
                     neighbor = new CoordinateNode(unit.getMoveSet().contains(new Coordinate(c, r)), current, c, r, game.getWorld().getTiles()[r][c], Integer.MAX_VALUE, Integer.MAX_VALUE);
                  else
                     neighbor = nodes[r * game.getGameWidth() + c];
                  boolean visited = set.contains(neighbor);
                  if(visited) {
                     if(visited == false)
                        System.exit(-1);
                     continue;
                  }
                  
                  Tile neighborTile = neighbor.getTile();
                  int tentative_gScore = current.getgScore() + current.getTile().getEffect().getMoveCost();
                  if((!queue.contains(neighbor) || tentative_gScore < neighbor.getgScore()) && unit.canTraverse(neighborTile)) {
                     neighbor.setPrev(current);
                     neighbor.setgScore(tentative_gScore);
                     neighbor.setfScore(tentative_gScore + heuristic(c, r, estimatedGoal.getX(), estimatedGoal.getY()));
                     
                     if(!queue.contains(neighbor))
                        queue.offer(neighbor);
                  }
                  // System.out.println("Distance of neighbor " + i + ": " + temp.getDistance());
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
   
   public Coordinate estimatedClosestObject(Unit unit, boolean capPriority) {
      Coordinate closest = null;
      int distance = Integer.MAX_VALUE;
      for(int r = 0; r < game.getGameHeight(); r++)
         for(int c = 0; c < game.getGameWidth(); c++) {
            Tile t = game.getWorld().getTiles()[r][c];
            if(unit.canCapture(t) || unit.canAttack(t))
               if(distanceFormula(unit.getX(), unit.getY(), c, r) < distance) {
                  distance = distanceFormula(unit.getX(), unit.getY(), c, r);
                  closest = new Coordinate(c, r);
               }
         }
      return closest;
   }
   
   public int heuristic(int x1, int y1, int x2, int y2) {
      return 3 * Math.abs(x1 - x2) + Math.abs(y1 - y2);
   }
   
   public Object[] bfsToClosestObject(Tile[][] world, Unit unit, Team targetOwner, boolean capPriority) {
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
         if((unit.canCapture(t) && (captureSpots.get(t).size() <= operationSizeLimit - 1 || captureSpots.get(t).contains(unit))) || (!capPriority && unit.canAttack(t))) {
            if(capPriority) {
               captureSpots.get(t).add(unit);
            }
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

class CoordinateNode {
   
   private boolean reachable;
   private CoordinateNode prev;
   private int x;
   private int y;
   private Tile tile;
   private int fScore;
   private int gScore;
   
   public CoordinateNode(boolean r, CoordinateNode p, int x, int y, Tile t, int f) {
      reachable = r;
      prev = p;
      this.x = x;
      this.y = y;
      tile = t;
      fScore = f;
   }
   
   public CoordinateNode(boolean r, CoordinateNode p, int x, int y, Tile t, int d, int g) {
      reachable = r;
      prev = p;
      this.x = x;
      this.y = y;
      tile = t;
      fScore = d;
      gScore = g;
   }
   
   public boolean equals(Object compare) {
      if(compare == null)
         return false;
      if(compare.getClass() != this.getClass())
         return false;
      return ((CoordinateNode)compare).getX() == this.getX() && ((CoordinateNode)compare).getY() == this.getY();
   }
   
   public int hashCode() {
      return (this.getX() * 31) + this.getY();
   }
   
   public boolean isReachable() {
      return reachable;
   }
   
   public void setReachable(boolean reachable) {
      this.reachable = reachable;
   }
   
   public CoordinateNode getPrev() {
      return prev;
   }
   
   public void setPrev(CoordinateNode prev) {
      this.prev = prev;
   }
   
   public int getX() {
      return x;
   }
   
   public void setX(int x) {
      this.x = x;
   }
   
   public int getY() {
      return y;
   }
   
   public void setY(int y) {
      this.y = y;
   }
   
   public Tile getTile() {
      return tile;
   }
   
   public int getfScore() {
      return fScore;
   }
   
   public void setfScore(int fScore) {
      this.fScore = fScore;
   }
   
   public int getgScore() {
      return gScore;
   }
   
   public void setgScore(int gScore) {
      this.gScore = gScore;
   }
   
   public String toString() {
      return "" + fScore;
   }
}

class TileCostComparator implements Comparator<CoordinateNode> {
   
   @Override
   public int compare(CoordinateNode arg0, CoordinateNode arg1) {
      return arg0.getfScore() - arg1.getfScore();
   }
   
}