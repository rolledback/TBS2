package com.rolledback.teams;

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
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamD extends ComputerTeam {
   
   final int animationDelay = 50;
   ArrayList<Coordinate> cityLocations;
   
   public ComputerTeamD(String name, int size, int r, Game g) {
      super(name, size, r, g);
      cityLocations = null;
   }

   public void executeTurn() {
      sortUnits(units);
      System.out.println("-------------------------------------" + units.size());
      for(int i = 0; i < units.size(); i++){
         Unit u = units.get(i);
         System.out.println(u);
         Coordinate moveSpot = moveUnit(u);
         if(moveSpot != null) {
            System.out.println(moveSpot);
            game.gameLoop(u.getX(), u.getY());
            delay(animationDelay);
            game.gameLoop(moveSpot.getX(), moveSpot.getY());
            delay(animationDelay); 
         }
         if(!units.contains(u))
            i--;       
      }
      
      Iterator<Factory> factoryIterator = factories.iterator();
      while(factoryIterator.hasNext()) {
         Factory currentFactory = factoryIterator.next();
         Random rand = new Random();
         int unitToProduce = rand.nextInt(currentFactory.getProductionList().size());
         int attempts = 0;
         while(currentFactory.produceUnit((UNIT_TYPE)currentFactory.getProductionList().keySet().toArray()[unitToProduce]) && attempts < 25) {
            unitToProduce = rand.nextInt(currentFactory.getProductionList().size());
            attempts++;
         }
      }
   }
   
   public Coordinate moveUnit(Unit u) {
      System.out.println(u.getCaptureSet());
      System.out.println(u.getAttackSet());
      System.out.println(u.getMoveSet());
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
      System.out.println("Simple move");
      HashMap<Coordinate, Integer> enemyDistances = new HashMap<Coordinate, Integer>();
     
      for(Unit t: opponent.getUnits()) {     
         for(Coordinate c: u.getMoveSet()) {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < 4; i++)
               try {
                  int d = distance(game.getWorld().getTiles(), c.getX(), c.getY(), t.getX() + xDirs[i], t.getY() + yDirs[i], u);
                  if(d != Integer.MAX_VALUE)
                     enemyDistances.put(c, d);
               }
               catch(Exception e) {}
         }         
      }
      if(u.getType() == UNIT_TYPE.INFANTRY) {
         if(cityLocations == null) {
            cityLocations = new ArrayList<Coordinate>();
            for(int row = 0; row < game.gameHeight; row++)
               for(int col = 0; col < game.gameWidth; col++)
                  if(u.canCapture(game.getWorld().getTiles()[row][col]))
                     cityLocations.add(new Coordinate(col, row));
         }
         
         for(Coordinate city: cityLocations) {     
            for(Coordinate c: u.getMoveSet()) {
               int[] yDirs = { 0, 0, 1, -1 };
               int[] xDirs = { 1, -1, 0, 0 };
               for(int i = 0; i < 4; i++)
                  try {
                     int d = distance(game.getWorld().getTiles(), c.getX(), c.getY(), city.getX() + xDirs[i], city.getY() + yDirs[i], u);
                     if(d != Integer.MAX_VALUE)
                        enemyDistances.put(c, d);
                  }
                  catch(Exception e) {}
            }         
         }
      }
      cityLocations = null;
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: enemyDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null)
         return minEntry.getKey();
      return null;      
   }
   
   public Coordinate attackMove(Unit u) {
      System.out.println("attack move");
      HashMap<Coordinate, Integer> attackDistances = new HashMap<Coordinate, Integer>();
      for(Coordinate c: u.getAttackSet()) {
         attackDistances.put(c, Integer.MAX_VALUE);
         int d = distance(game.getWorld().getTiles(), u.getX(), u.getY(), c.getX(), c.getY(), u);
         if(d < attackDistances.get(c))
            attackDistances.put(c, d);
      }
      System.out.println(attackDistances);
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: attackDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null)
         return minEntry.getKey();
      return null; 
   }
   
   public Coordinate captureMove(Unit u) {
      System.out.println("capture move");
      HashMap<Coordinate, Integer> captureDistances = new HashMap<Coordinate, Integer>();
      for(Coordinate c: u.getCaptureSet()) {
         captureDistances.put(c, Integer.MAX_VALUE);
         int d = distance(game.getWorld().getTiles(), u.getX(), u.getY(), c.getX(), c.getY(), u);
         if(d < captureDistances.get(c))
            captureDistances.put(c, d);
      }
      System.out.println(captureDistances);
      Map.Entry<Coordinate, Integer> minEntry = null;
      for(Map.Entry<Coordinate, Integer> entry: captureDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      if(minEntry != null)
         return minEntry.getKey();
      return null; 
   }
   
   public void sortUnits(ArrayList<Unit> unitList) {
      for(Unit u: unitList) {
         u.calcMoveSpots();
      }
      
      Collections.sort(unitList, new Comparator<Unit>() {
         @Override
         public int compare(Unit u1, Unit u2) {
            if(u1.getCaptureSet().size() != 0 && u2.getCaptureSet().size() == 0)
               return -1;
            if(u1.getCaptureSet().size() == 0 && u2.getCaptureSet().size() != 0)
               return 1;
            if(u1.getCaptureSet().size() == u2.getCaptureSet().size() && u1.getCaptureSet().size() != 0)
               return 0;
            
            if(u1.getAttackSet().size() != 0 && u2.getAttackSet().size() == 0)
               return -1;
            if(u1.getAttackSet().size() == 0 && u2.getAttackSet().size() != 0)
               return 1;
            if(u1.getAttackSet().size() == u2.getAttackSet().size() && u1.getAttackSet().size() != 0)
               return 0;
            
            if(u1.getMoveSet().size() > u2.getMoveSet().size())
               return -1;
            if(u1.getMoveSet().size() < u2.getMoveSet().size())
               return 1;
            return 0;
         }
      });
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
