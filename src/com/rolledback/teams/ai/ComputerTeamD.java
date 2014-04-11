package com.rolledback.teams.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.teams.Team;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamD extends ComputerTeam {
   
   final int animationDelay = 500;
   private ArrayList<CapturableTile> captureSpots;
   
   public ComputerTeamD(String name, int size, int r, Game g, int n) {
      super(name, size, r, g, n);
   }
   
   public void findCaptureSpots() {
      captureSpots = new ArrayList<CapturableTile>();
      Tile[][] world = game.getWorld().getTiles();
      for(int r = 0; r < world.length; r++)
         for(int c = 0; c < world[r].length; c++)
            if(world[r][c] instanceof CapturableTile)
               captureSpots.add((CapturableTile)world[r][c]);
   }
   
   public void executeTurn() {
      boolean capPriority = false;
      if(captureSpots == null)
         findCaptureSpots();
      else {
         if(cities.size() + factories.size() <= opponent.getCities().size() + opponent.getFactories().size())
            capPriority = true;
         else if(cities.size() + factories.size() + opponent.getCities().size() + opponent.getFactories().size() < captureSpots.size())
            capPriority = true;
      }
      sortUnits();
      for(int i = 0; i < units.size(); i++) {
         if(opponent.getUnits().size() == 0)
            return;
         Unit u = units.get(i);
         Coordinate moveSpot = moveUnit(u, capPriority);
         if(moveSpot != null) {
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
      capPriority = (capPriority && (u.getClassification() == UNIT_CLASS.INFANTRY));
      Object[] closestEnemyTuple = closestObject(game.getWorld().getTiles(), u, opponent, capPriority);
      
      int closestEnemyDistance = (int)closestEnemyTuple[0];
      if(closestEnemyDistance == Integer.MAX_VALUE)
         return null;
      CoordinateNode bestMoveSpotNode = (CoordinateNode)closestEnemyTuple[2];
      
      Coordinate bestMoveSpot = new Coordinate(bestMoveSpotNode.getX(), bestMoveSpotNode.getY());
      return bestMoveSpot;
   }
   
   public Coordinate attackMove(Unit u) {
      HashMap<Coordinate, Integer> attackDistances = new HashMap<Coordinate, Integer>();
      for(Coordinate c: u.getAttackSet()) {
         attackDistances.put(c, Integer.MAX_VALUE);
         int d = distance(game.getWorld().getTiles(), u.getX(), u.getY(), c.getX(), c.getY(), u);
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
      HashMap<Coordinate, Integer> captureDistances = new HashMap<Coordinate, Integer>();
      for(Coordinate c: u.getCaptureSet()) {
         captureDistances.put(c, Integer.MAX_VALUE);
         int d = distance(game.getWorld().getTiles(), u.getX(), u.getY(), c.getX(), c.getY(), u);
         if(d < captureDistances.get(c))
            captureDistances.put(c, d);
      }
      
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: captureDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null)
         return minEntry.getKey();
      return null;
   }
   
   public void sortUnits() {
      for(Unit u: units) {
         u.calcMoveSpots(false);
      }
      
      int sumX = 0;
      int sumY = 0;
      for(Unit u: opponent.getUnits()) {
         sumX += u.getX();
         sumY += u.getY();
      }
      
      final int avgX;
      final int avgY;
      if(opponent.getUnits().size() > 0) {
         avgX = sumX / opponent.getUnits().size();
         avgY = sumY / opponent.getUnits().size();
      }
      else {
         avgX = 0;
         avgY = 0;
      }
      
      Collections.sort(units, new Comparator<Unit>() {
         public int compare(Unit u1, Unit u2) {
            if(u1.getCaptureSet().size() != u2.getCaptureSet().size()) {
               return u2.getCaptureSet().size() - u1.getCaptureSet().size();
            }
            if(u1.getAttackSet().size() != u2.getAttackSet().size()) {
               return u2.getAttackSet().size() - u1.getAttackSet().size();
            }
            if(distanceForumula(u1.getX(), u1.getY(), avgX, avgY) != distanceForumula(u2.getX(), u2.getY(), avgX, avgY)) {
               return distanceForumula(u1.getX(), u1.getY(), avgX, avgY) - distanceForumula(u2.getX(), u2.getY(), avgX, avgY);
            }
            else
               return u2.getMoveSet().size() - u1.getMoveSet().size();
         }
      });
   }
   
   public int distanceForumula(int x1, int y1, int x2, int y2) {
      return ((x1 - x2) + (y1 - y2));
   }
   
   public Object[] closestObject(Tile[][] world, Unit unit, Team targetOwner, boolean capPriority) {
      Object[] resultsTuple = new Object[3];
      
      LinkedList<CoordinateNode> queue = new LinkedList<CoordinateNode>();
      HashSet<CoordinateNode> set = new HashSet<CoordinateNode>();
      
      queue.offer(new CoordinateNode(true, null, unit.getX(), unit.getY()));
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
         if(unit.canCapture(t) || (!capPriority && unit.canAttack(t))) {
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
                  CoordinateNode temp = new CoordinateNode(unit.getMoveSet().contains(new Coordinate(c, r)), n, c, r);
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
   
   // find shortest path btw spots for a given unit
   public int distance(Tile[][] world, int col, int row, int targetX, int targetY, Unit unit) {
      LinkedList<Tile> queue = new LinkedList<Tile>();
      HashSet<Tile> set = new HashSet<Tile>();
      queue.offer(world[targetY][targetX]);
      queue.offer(null);
      world[unit.getY()][unit.getX()].setOccupied(false);
      int distance = 0;
      
      while(queue.size() > 1) {
         Tile t = queue.poll();
         if(t == null) {
            distance++;
            queue.offer(null);
         }
         else if(t.equals(world[row][col])) {
            world[unit.getY()][unit.getX()].setOccupied(true);
            return distance;
         }
         else {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < 4; i++)
               try {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
                  if(!set.contains(world[r][c])) {
                     if(!world[r][c].isOccupied() || (world[r][c].isOccupied() && world[r][c].getOccupiedBy().getOwner().equals(this)))
                        if(unit.getClassification() == UNIT_CLASS.VEHICLE && world[r][c].isVehiclePassable()) {
                           set.add(world[r][c]);
                           queue.offer(world[r][c]);
                        }
                        else if(unit.getClassification() == UNIT_CLASS.INFANTRY && world[r][c].isInfantryPassable()) {
                           set.add(world[r][c]);
                           queue.offer(world[r][c]);
                        }
                  }
               }
               catch(Exception e) {
                  // out of bounds
               }
         }
      }
      world[unit.getY()][unit.getX()].setOccupied(true);
      return distance;
   }
}

class CoordinateNode {
   
   private boolean reachable;
   private CoordinateNode prev;
   private int x;
   private int y;
   
   public CoordinateNode(boolean r, CoordinateNode p, int x, int y) {
      setReachable(r);
      setPrev(p);
      this.x = x;
      this.y = y;
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
   
   public String toString() {
      return "x: " + x + " y: " + y;
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
   
}