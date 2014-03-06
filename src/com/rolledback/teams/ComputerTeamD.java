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
   final int animationDelay = 0;

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
      HashMap<Coordinate, Integer> moveDistances = new HashMap<Coordinate, Integer>();
      int closestEnemyDistance = Integer.MAX_VALUE;
      Unit closestEnemy = null;
      for(Unit t: opponent.getUnits()) {
         int dToEnemy = distance(game.getWorld().getTiles(), u.getX(), u.getY(), t.getX(), t.getY(), u);
         if(dToEnemy < closestEnemyDistance && !(u.getType() == UNIT_TYPE.INFANTRY && t.getClassification() == UNIT_CLASS.VEHICLE)) {
            closestEnemyDistance = dToEnemy;
            closestEnemy = t;
         }
      }
      if(closestEnemy != null) {
         for(Coordinate c: u.getMoveSet()) {
            int d = distance(game.getWorld().getTiles(), c.getX(), c.getY(), closestEnemy.getX(), closestEnemy.getY(), u);
            if(d != Integer.MAX_VALUE)
               moveDistances.put(c, d);
         }
         moveDistances.put(new Coordinate(u.getX(), u.getY()),
               distance(game.getWorld().getTiles(), u.getX(), u.getY(), closestEnemy.getX(), closestEnemy.getY(), u));
      }
      
      if(u.getType() == UNIT_TYPE.INFANTRY) {
         int closestCaptureableDistance = Integer.MAX_VALUE;
         Coordinate closestCapturable = null;
         for(int row = 0; row < game.gameHeight; row++)
            for(int col = 0; col < game.gameWidth; col++)
               if(u.canCapture(game.getWorld().getTiles()[row][col])) {
                  int dToCapture = distance(game.getWorld().getTiles(), u.getX(), u.getY(), col, row, u);
                  if(dToCapture < closestCaptureableDistance) {
                     closestCapturable = new Coordinate(col, row);
                     closestCaptureableDistance = dToCapture;
                  }
               }
         
         if(closestCaptureableDistance < closestEnemyDistance) {
            moveDistances.clear();
            moveDistances.put(new Coordinate(u.getX(), u.getY()),
                  distance(game.getWorld().getTiles(), u.getX(), u.getY(), closestCapturable.getX(), closestCapturable.getY(), u));
            for(Coordinate c: u.getMoveSet()) {
               int d = distance(game.getWorld().getTiles(), c.getX(), c.getY(), closestCapturable.getX(), closestCapturable.getY(), u);
               if(d != Integer.MAX_VALUE)
                  moveDistances.put(c, d);
            }
         }
      }
      
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: moveDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null)
         return minEntry.getKey();
      return null;
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
               return u1.getCaptureSet().size() - u2.getCaptureSet().size();
            }
            if(u1.getAttackSet().size() != u2.getAttackSet().size()) {
               return u1.getAttackSet().size() - u2.getAttackSet().size();
            }
            if(distanceForumula(u1.getX(), u1.getY(), trueAvgX, trueAvgY) != distanceForumula(u2.getX(), u2.getY(), trueAvgX, trueAvgY)) {
            	return distanceForumula(u1.getX(), u1.getY(), trueAvgX, trueAvgY) - distanceForumula(u2.getX(), u2.getY(), trueAvgX, trueAvgY);
            }
            else
            	return u1.getMoveSet().size() - u2.getMoveSet().size();
         }
      });
   }
   
   public int distanceForumula(int x1, int y1, int x2, int y2) {
   	return ((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2));
   }
   
   public int distance(Tile[][] world, int col, int row, int targetX, int targetY, Unit unit) {
      if(!unit.canTraverse(world[targetY][targetX]))
         return Integer.MAX_VALUE;
      LinkedList<Tile> queue = new LinkedList<Tile>();
      HashSet<Tile> set = new HashSet<Tile>();
      queue.offer(world[targetY][targetX]);
      queue.offer(null);
      queue.offer(world[targetY][targetX]);
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
      return Integer.MAX_VALUE;
   }
   
}
