package com.rolledback.teams;

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
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamD extends ComputerTeam {
   
   public enum BFS_TYPE {
      CAPTURE, ENEMY;
   }
   
   final int animationDelay = 500;
   
   public ComputerTeamD(String name, int size, int r, Game g) {
      super(name, size, r, g);
   }
   
   public void executeTurn() {
      sortUnits();
      for(int i = 0; i < units.size(); i++) {
         if(opponent.getUnits().size() == 0)
            return;
         Unit u = units.get(i);
         Coordinate moveSpot = moveUnit(u);
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
      
      game.logicLock.lock();
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
      game.logicLock.unlock();
   }
   
   public Coordinate moveUnit(Unit u) {
      u.calcMoveSpots();
      if(u.getCaptureSet().size() != 0)
         return captureMove(u);
      else if(u.getAttackSet().size() != 0)
         return attackMove(u);
      else if(u.getMoveSet().size() != 0)
         return simpleMove(u);
      return null;
   }
   
   public Coordinate simpleMove(Unit u) {
      Object[] closestEnemyTuple = closestObject(game.getWorld().getTiles(), u, BFS_TYPE.ENEMY, opponent);
      int closestEnemyDistance = (int)closestEnemyTuple[0];
      Tile closesestEnemyTile = (Tile)closestEnemyTuple[2];
      
      Coordinate bestMoveSpot = null;
      int minMoveDistance = Integer.MAX_VALUE;
      if(closesestEnemyTile != null) {
         minMoveDistance = closestEnemyDistance;
         for(Coordinate c: u.getMoveSet()) {
            int d;
            if(closestEnemyDistance < game.getWidth() / 2)
               d = distance(game.getWorld().getTiles(), c.getX(), c.getY(), closesestEnemyTile.getX(), closesestEnemyTile.getY(), u);
            else
               d = distanceForumula(c.getX(), c.getY(), closesestEnemyTile.getX(), closesestEnemyTile.getY());
            if(d < minMoveDistance) {
               bestMoveSpot = c;
               minMoveDistance = d;
            }
         }
      }
      if(u.getType() == UNIT_TYPE.INFANTRY) {
         Object[] closestCaptureTuple = closestObject(game.getWorld().getTiles(), u, BFS_TYPE.CAPTURE, opponent);
         int closestCaptureDisance = (int)closestCaptureTuple[0];
         Tile closestCaptureTile = (Tile)closestCaptureTuple[2];
         
         if(closestCaptureTile != null && closestCaptureDisance < closestEnemyDistance) {
            for(Coordinate c: u.getMoveSet()) {
               int d;
               if(closestEnemyDistance < game.getWidth() / 2)
                  d = distance(game.getWorld().getTiles(), c.getX(), c.getY(), closestCaptureTile.getX(), closestCaptureTile.getY(), u);
               else
                  d = distanceForumula(c.getX(), c.getY(), closestCaptureTile.getX(), closestCaptureTile.getY());
               if(d < minMoveDistance) {
                  bestMoveSpot = c;
                  minMoveDistance = d;
               }
            }
         }
      }
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
         u.calcMoveSpots();
      }
      
      int avgX = 0;
      int avgY = 0;
      for(Unit u: opponent.getUnits()) {
         avgX += u.getX();
         avgY += u.getY();
      }
      
      final int trueAvgX;
      final int trueAvgY;
      if(opponent.getUnits().size() > 0) {
         trueAvgX = avgX / opponent.getUnits().size();
         trueAvgY = avgY / opponent.getUnits().size();
      }
      else {
         trueAvgX = 0;
         trueAvgY = 0;
      }
      
      Collections.sort(units, new Comparator<Unit>() {
         public int compare(Unit u1, Unit u2) {
            if(u1.getCaptureSet().size() != u2.getCaptureSet().size()) {
               return u2.getCaptureSet().size() - u1.getCaptureSet().size();
            }
            if(u1.getAttackSet().size() != u2.getAttackSet().size()) {
               return u2.getAttackSet().size() - u1.getAttackSet().size();
            }
            if(distanceForumula(u1.getX(), u1.getY(), trueAvgX, trueAvgY) != distanceForumula(u2.getX(), u2.getY(), trueAvgX, trueAvgY)) {
               return distanceForumula(u1.getX(), u1.getY(), trueAvgX, trueAvgY) - distanceForumula(u2.getX(), u2.getY(), trueAvgX, trueAvgY);
            }
            else
               return u2.getMoveSet().size() - u1.getMoveSet().size();
         }
      });
   }
   
   public int distanceForumula(int x1, int y1, int x2, int y2) {
      return ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
   }
   
   public Object[] closestObject(Tile[][] world, Unit unit, BFS_TYPE mission, Team targetOwner) {
      Object[] resultsTuple = new Object[3];
      LinkedList<Tile> queue = new LinkedList<Tile>();
      HashSet<Tile> set = new HashSet<Tile>();
      set.add(world[unit.getY()][unit.getX()]);
      queue.offer(world[unit.getY()][unit.getX()]);
      queue.offer(null);
      world[unit.getY()][unit.getX()].setOccupied(false);
      int distance = 0;
      
      while(queue.size() > 1) {
         Tile t = queue.poll();
         if(t == null) {
            distance++;
            queue.offer(null);
         }
         else if(mission == BFS_TYPE.CAPTURE && unit.canCapture(t)) {
            world[unit.getY()][unit.getX()].setOccupied(true);
            resultsTuple[0] = distance;
            resultsTuple[1] = new Coordinate(t.getX(), t.getY());
            resultsTuple[2] = t;
            return resultsTuple;
         }
         else if(mission == BFS_TYPE.ENEMY && t.isOccupied() && t.getOccupiedBy().getOwner().equals(targetOwner)) {
            world[unit.getY()][unit.getX()].setOccupied(true);
            resultsTuple[0] = distance;
            resultsTuple[1] = new Coordinate(t.getX(), t.getY());
            resultsTuple[2] = t;
            return resultsTuple;
         }
         else {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < xDirs.length; i++)
               try {
                  int r = t.getY() + yDirs[i];
                  int c = t.getX() + xDirs[i];
                  if(!set.contains(world[r][c])) {
                     // if you are looking for an enemy then you can offer anything on to the queue
                     // if you are looking for a city, you only want to offer passable tiles onto
                     // the queue
                     if(!world[r][c].isOccupied() || (world[r][c].isOccupied() && world[r][c].getOccupiedBy().getOwner().equals(this))
                           || mission == BFS_TYPE.ENEMY)
                        if(unit.canTraverse(world[r][c])) {
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
