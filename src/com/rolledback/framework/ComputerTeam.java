package com.rolledback.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;

public class ComputerTeam extends Team {
   
   Game game;
   Team opponent;
   boolean deubg = true;
   public ComputerTeam(String name, int size, int r, Game g) {
      super(name, size, r);
      game = g;
   }
   
   public void executeTurn() {
      Coordinate target = chooseTarget();
      if(deubg) System.out.println("Target coordinates: " + target.toString());
      int targetX = target.getX();
      int targetY = target.getY();
      for(int x = 0; x < units.size(); x++) {
         Unit currUnit = units.get(x);
         int[][] moveSpots = game.world.calcMoveSpots(currUnit);
         if(deubg) System.out.println(currUnit.getX() + "," + currUnit.getY());
         Coordinate moveHere = null;
         double lowestDistance = Integer.MAX_VALUE;
         double currentDistance = 0;
         for(int row = 0; row < game.gameHeight; row++) {
            for(int col = 0; col < game.gameWidth; col++) {
               if(moveSpots[row][col] == 2) {
                  game.gameLoop(currUnit.getX(), currUnit.getY());
                  game.gameLoop(targetX, targetY);
                  break;
               }
               if(moveSpots[row][col] == 1) {
                  System.out.println("BFS of " + col + "," + row + " to target is: " + bfsToBestSpot(game.world.getTiles().clone(), col, row, targetX, targetY, currUnit));
                  if(col == targetX)
                     currentDistance = Math.abs(row - targetY);
                  else if(row == targetY)
                     currentDistance = Math.abs(col - targetX);
                  else
                     currentDistance = (double)Math.abs(col - targetX) + (double)Math.abs(row - targetY);
                  if(deubg) System.out.println("Distance of " + col + "," + row + " to target is " + currentDistance);
                  if(currentDistance < lowestDistance) {
                     moveHere = new Coordinate(col, row);
                     lowestDistance = currentDistance;
                  }
               }                  
            }
         }
         if(moveHere != null) {            
            if(deubg) System.out.println("Move a unit to: " + moveHere.toString());
            game.gameLoop(currUnit.getX(), currUnit.getY());
            game.gameLoop(moveHere.getX(), moveHere.getY());
         }
      }
   }
   public int bfsToBestSpot(Tile[][] world, int col, int row, int targetX, int targetY, Unit unit) {
      int[][] moveSpots = new int[game.gameHeight][game.gameWidth];
      for(int y = 0; y < game.gameHeight; y++) {
         for(int x = 0; x < game.gameWidth; x++) {
            if(world[y][x].isOccupied())
               moveSpots[y][x] = 0;
            else if(!world[y][x].isVehiclePassable() && unit.getClassification() == UNIT_CLASS.VEHICLE)
               moveSpots[y][x] = 0;
            else if(!world[y][x].isInfantryPassable() && unit.getClassification() == UNIT_CLASS.INFANTRY)
               moveSpots[y][x] = 0;
            else
               moveSpots[y][x] = 1;
         }
      }
      
      for(int y = 0; y < game.gameHeight; y++) {
         for(int x = 0; x < game.gameWidth; x++) {
            if((y == targetY && x == targetX) || (y == row && x == col))
               System.out.print("X ");
            else
               System.out.print(moveSpots[y][x] + " ");            
         }
         System.out.println();
      }
      
      LinkedList<Tile> queue = new LinkedList<Tile>();
      HashSet<Tile> set = new HashSet<Tile>();
      queue.offer(world[row][col]);
      queue.offer(null);
      set.add(world[row][col]);
      int distance = 1;
      while(queue.size() > 1) {
         Tile t = queue.poll();
         if(t == null) {
            distance++;
            queue.offer(null);            
         }
         else if(t.getX() == targetX && t.getY() == targetY)
            return distance;
         else {
            int[] yDirs = {0, 0, 1, -1};
            int[] xDirs = {1, -1, 0, 0};
            for(int i = 0; i < 4; i++)
               try {
                  if(moveSpots[t.getX() + xDirs[i]][t.getY() + yDirs[i]] == 1 && !set.contains(world[t.getX() + xDirs[i]][t.getY() + yDirs[i]])) {
                     set.add(world[t.getX() + xDirs[i]][t.getY() + yDirs[i]]);
                     queue.offer(world[t.getX() + xDirs[i]][t.getY() + yDirs[i]]);
                  }
               }
               catch(Exception e) {}
         }
      }
      return distance;
   }
   
   
   public Coordinate chooseTarget() {
      HashMap<Unit, Integer> numInRange = new HashMap<Unit, Integer>();      
      ListIterator<Unit> opponentI = opponent.getUnits().listIterator();
      while(opponentI.hasNext())
         numInRange.put(opponentI.next(), 0);
      
      ListIterator<Unit> teamI = units.listIterator();
      while(teamI.hasNext()) {
         Unit currUnit = teamI.next();
         int[][] currMS = game.world.calcMoveSpots(currUnit);
         for(int row = 0; row < game.gameHeight; row++) {
            for(int col = 0; col < game.gameWidth; col++) {
               if(currMS[row][col] == 2)
                  numInRange.put(game.world.getTiles()[row][col].getOccupiedBy(), numInRange.get(game.world.getTiles()[row][col].getOccupiedBy()) + 1);
            }
         }
      }      

      int currentMax = 0;
      ArrayList<Unit> possibleTargets = new ArrayList<Unit>();
      Map.Entry<Unit, Integer> maxEntry = null;
      for (Map.Entry<Unit, Integer> entry : numInRange.entrySet()) {
          if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0) {
             maxEntry = entry;
             if(maxEntry.getValue() > currentMax) {
                possibleTargets.clear();
                currentMax = maxEntry.getValue();
                possibleTargets.add(maxEntry.getKey());
             }
             else
                possibleTargets.add(maxEntry.getKey());
          }
      }

      if(currentMax <= 0) {
         numInRange.clear();
         return findFutureTarget(possibleTargets);   
      }
      else if(possibleTargets.size() > 1) {
         possibleTargets.clear();
         numInRange.clear();
         return chooseBestTarget();
      }
      return new Coordinate(possibleTargets.get(0).getX(), possibleTargets.get(0).getY());
   }
   
   public Coordinate findFutureTarget(ArrayList<Unit> possibleTargets) {
      if(deubg) System.out.println("By distance.");
      HashMap<Unit, Double> distanceHash = new HashMap<Unit, Double>();
      ListIterator<Unit> targetI = possibleTargets.listIterator();
      while(targetI.hasNext()) {  
         Unit currOpponent = targetI.next();
         if(deubg) System.out.println("Swarm distnace from " + currOpponent.toString());
         double distanceAvg = 0.0;
         ListIterator<Unit> unitI = units.listIterator();
         while(unitI.hasNext()) {
            Unit currFriendly = unitI.next();
            double distance = 0.0;
            if(deubg) System.out.println("Friendly = (" + currFriendly.getX() + "," + currFriendly.getY() + ")");
            if(deubg) System.out.println("Enemy = (" + currOpponent.getX() + "," + currOpponent.getY() + ")");
            if(currFriendly.getX() == currOpponent.getX())
               distance = Math.abs(currFriendly.getY() - currOpponent.getY());
            else if(currFriendly.getY() == currOpponent.getY())
               distance = Math.abs(currFriendly.getX() - currOpponent.getX());
            else if(currFriendly.getY() == currOpponent.getY() && currFriendly.getX() == currOpponent.getX())
               distance = 0;
            else 
               distance = ((double)Math.abs(currFriendly.getX() - currOpponent.getX()) + (double)Math.abs(currFriendly.getY() - currOpponent.getY()));
            if(deubg) System.out.println("Distance: " + distance);
            distanceAvg += distance;
         }
         distanceAvg /= units.size();
         if(deubg) System.out.println("Swarm average: " + distanceAvg);
         if(deubg) System.out.println("------------");
         distanceHash.put(currOpponent, distanceAvg);
      }

      Map.Entry<Unit, Double> minEntry = null;
      for (Map.Entry<Unit, Double> entry : distanceHash.entrySet()) {
          if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
             minEntry = entry;
          }
      }
      if(deubg) System.out.println("Go for: " + minEntry.getKey().toString() + "(dist " + minEntry.getValue() + ")");
      return new Coordinate(minEntry.getKey().getX(), minEntry.getKey().getY(), minEntry.getValue());
   }
   
   public Coordinate chooseBestTarget() {
//      System.out.println("By potential damage");
      HashMap<Unit, Double> numInRange = new HashMap<Unit, Double>();      
      ListIterator<Unit> opponentI = opponent.getUnits().listIterator();
      while(opponentI.hasNext())
         numInRange.put(opponentI.next(), 0.0);
      
      ListIterator<Unit> teamI = units.listIterator();
      while(teamI.hasNext()) {
         Unit currUnit = teamI.next();
         int currAttackPotential = ((currUnit.getMaxAttack() - currUnit.getMinAttack()) / 2) + currUnit.getMinAttack();
         int[][] currMS = game.world.calcMoveSpots(currUnit);
         for(int row = 0; row < game.gameHeight; row++) {
            for(int col = 0; col < game.gameWidth; col++) {
               if(currMS[row][col] == 2)
                  numInRange.put(game.world.getTiles()[row][col].getOccupiedBy(), numInRange.get(game.world.getTiles()[row][col].getOccupiedBy()) + currAttackPotential);
            }
         }
      }      

      Double currentMax = 0.0;
      ArrayList<Unit> possibleTargets = new ArrayList<Unit>();
      Map.Entry<Unit, Double> maxEntry = null;
      for (Map.Entry<Unit, Double> entry : numInRange.entrySet()) {
          if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0) {
             maxEntry = entry;
             if(maxEntry.getValue() > currentMax) {
                possibleTargets.clear();
                currentMax = maxEntry.getValue();
                possibleTargets.add(maxEntry.getKey());
             }
          }
      }
      return new Coordinate(maxEntry.getKey().getX(), maxEntry.getKey().getY());
   }
}
