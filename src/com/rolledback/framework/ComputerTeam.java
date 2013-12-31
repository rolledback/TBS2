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
   Coordinate target;
   Unit targetUnit;
   
   public ComputerTeam(String name, int size, int r, Game g) {
      super(name, size, r);
      game = g;
   }
   
   public void executeTurn() {
      // find unit you want army to attack
      target = chooseTarget();
      targetUnit = game.world.getTiles()[target.getY()][target.getX()].getOccupiedBy();
      int targetX = targetUnit.getX();
      int targetY = targetUnit.getY();
      
      // go through all units and determine where they will move
      for(int x = 0; x < units.size(); x++) {
         Unit currUnit = units.get(x);
         // generate valid move spots for unit
         int[][] moveSpots = game.world.calcMoveSpots(currUnit);
         Coordinate moveHere = null;
         double lowestDistance = Integer.MAX_VALUE;
         double currentDistance = 0;
         
         // if unit can attack target, do it
         if(moveSpots[targetY][targetX] == 2) {
            game.gameLoop(currUnit.getX(), currUnit.getY());
            game.gameLoop(targetX, targetY);
         }
         // else iterate through moveSpots find, find valid spot closest to target
         else {
            for(int row = 0; row < game.gameHeight; row++) {
               for(int col = 0; col < game.gameWidth; col++) {
                  if(moveSpots[row][col] == 1) {
                     // calculate distance from spot to target using a breadth first search
                     currentDistance = bfsToBestSpot(game.world.getTiles().clone(), col, row, targetX, targetY, currUnit);
                     if(currentDistance < lowestDistance) {
                        moveHere = new Coordinate(col, row);
                        lowestDistance = currentDistance;
                     }
                  }
               }
            }
            // if you found a spot to move to move there
            if(moveHere != null) {
               game.gameLoop(currUnit.getX(), currUnit.getY());
               game.gameLoop(moveHere.getX(), moveHere.getY());
            }
         }
         // if you reached here without moving the unit never moved
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
      
      LinkedList<Tile> queue = new LinkedList<Tile>();
      HashSet<Tile> set = new HashSet<Tile>();
      queue.offer(world[targetY][targetX]);
      queue.offer(null);
      queue.offer(world[targetY][targetX]);
      int distance = 0;
      while(queue.size() > 1) {
         Tile t = queue.poll();
         if(t == null) {
            distance++;
            queue.offer(null);
         }
         else if(t.equals(world[row][col]))
            return distance;
         else {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < 4; i++)
               try {
                  if(moveSpots[t.getY() + yDirs[i]][t.getX() + xDirs[i]] == 1 && !set.contains(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]])) {
                     set.add(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                     queue.offer(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                  }
               }
               catch(Exception e) {
                  // out of bounds
               }
         }
      }
      return distance;
   }
   
   public Coordinate chooseTarget() {
      // create HashMap, keySet is all enemy hits
      HashMap<Unit, Integer> numInRange = new HashMap<Unit, Integer>();
      ListIterator<Unit> opponentI = opponent.getUnits().listIterator();
      while(opponentI.hasNext())
         numInRange.put(opponentI.next(), 0);
      
      // go through all friendly units and determine what enemy units they can
      // attack
      ListIterator<Unit> teamI = units.listIterator();
      while(teamI.hasNext()) {
         Unit currUnit = teamI.next();
         int[][] currMS = game.world.calcMoveSpots(currUnit);
         for(int row = 0; row < game.gameHeight; row++) {
            for(int col = 0; col < game.gameWidth; col++) {
               if(currMS[row][col] == 2) {
                  // increment HashMap value of any enemy unit you can attack
                  Unit temp = game.world.getTiles()[row][col].getOccupiedBy();
                  numInRange.put(temp, numInRange.get(temp) + 1);
               }
            }
         }
      }
      
      // construct list of enemies with the most possible attackers
      int currentMax = 0;
      ArrayList<Unit> possibleTargets = new ArrayList<Unit>();
      Map.Entry<Unit, Integer> maxEntry = null;
      for(Map.Entry<Unit, Integer> entry: numInRange.entrySet()) {
         if(maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0) {
            maxEntry = entry;
            if(maxEntry.getValue() > currentMax) {
               possibleTargets.clear();
               currentMax = maxEntry.getValue();
               possibleTargets.add(maxEntry.getKey());
            }
            else if(maxEntry.getValue() == currentMax)
               possibleTargets.add(maxEntry.getKey());
         }
      }
      
      // if no one can be attacked, find closest enemy to the swarm
      if(currentMax <= 0) {
         possibleTargets.clear();
         numInRange.clear();
         return findFutureTarget();
      }
      // if multiple units have the most number of possible attackers, determine
      // target on who will receive the most damage
      else if(possibleTargets.size() > 1) {
         possibleTargets.clear();
         numInRange.clear();
         return chooseBestTarget();
      }
      // found one target, return coordinate to him
      return new Coordinate(possibleTargets.get(0).getX(), possibleTargets.get(0).getY());
   }
   
   public Coordinate findFutureTarget() {
      // create HashMap, key set is all enemy units
      HashMap<Unit, Double> distanceHash = new HashMap<Unit, Double>();
      
      // go through each enemy unit
      ListIterator<Unit> targetI = opponent.getUnits().listIterator();
      while(targetI.hasNext()) {
         Unit currOpponent = targetI.next();
         double distanceAvg = 0.0;
         // go through all friendly units and calculate approximate distance to enemy unit
         ListIterator<Unit> unitI = units.listIterator();
         while(unitI.hasNext()) {
            Unit currFriendly = unitI.next();
            distanceAvg += Math.abs(currFriendly.getX() - currOpponent.getX()) + Math.abs(currFriendly.getY() - currOpponent.getY());
         }
         // set enemy unit's distanceHash value as that average
         distanceHash.put(currOpponent, distanceAvg / (double)units.size());
      }
      
      // find enemy unit with lowest average distance to army
      Map.Entry<Unit, Double> minEntry = null;
      for(Map.Entry<Unit, Double> entry: distanceHash.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) <= 0)
            minEntry = entry;
      
      return new Coordinate(minEntry.getKey().getX(), minEntry.getKey().getY(), minEntry.getValue());
   }
   
   public Coordinate chooseBestTarget() {
      // create HashMap of all enemy units
      HashMap<Unit, Double> numInRange = new HashMap<Unit, Double>();
      ListIterator<Unit> opponentI = opponent.getUnits().listIterator();
      while(opponentI.hasNext())
         numInRange.put(opponentI.next(), 0.0);
      
      // go through all friendly units and calculate damage they can inflict on all enemies in range
      ListIterator<Unit> teamI = units.listIterator();
      while(teamI.hasNext()) {
         Unit currUnit = teamI.next();
         int[][] currMS = game.world.calcMoveSpots(currUnit);
         for(int row = 0; row < game.gameHeight; row++) {
            for(int col = 0; col < game.gameWidth; col++) {
               if(currMS[row][col] == 2)
                  numInRange.put(game.world.getTiles()[row][col].getOccupiedBy(), numInRange.get(game.world.getTiles()[row][col].getOccupiedBy())
                        + currUnit.getMaxAttack());
            }
         }
      }
      // find enemy units with the highest potential damage
      double currentMax = 0;
      ArrayList<Unit> possibleTargets = new ArrayList<Unit>();
      Map.Entry<Unit, Double> maxEntry = null;
      for(Map.Entry<Unit, Double> entry: numInRange.entrySet()) {
         if(maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0 && entry.getValue() > 0) {
            maxEntry = entry;
            if(maxEntry.getValue() > currentMax) {
               possibleTargets.clear();
               currentMax = maxEntry.getValue();
               possibleTargets.add(maxEntry.getKey());
            }
            else if(maxEntry.getValue() == currentMax)
               possibleTargets.add(maxEntry.getKey());
         }
      }
      // if more than one with greatest, take one with the lowest defense
      if(possibleTargets.size() > 1) {
         possibleTargets.clear();
         maxEntry = null;
         currentMax = 9999.9999;
         for(Map.Entry<Unit, Double> entry: numInRange.entrySet()) {
            if(maxEntry == null || ((Integer)(entry.getKey().getDefense())).compareTo((Integer)(maxEntry.getKey().getDefense())) <= 0
                  && entry.getValue() > 0)
               maxEntry = entry;
         }
      }
      return new Coordinate(maxEntry.getKey().getX(), maxEntry.getKey().getY(), maxEntry.getValue());
   }
}
