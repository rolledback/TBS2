package com.rolledback.teams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamB extends ComputerTeam {
   
   final int animationDelay = 50;
   
   public ComputerTeamB(String name, int size, int r, Game g) {
      super(name, size, r, g);
   }
   
   public void executeTurn() {
      // go through all units and determine their target and where they should move
      for(int x = 0; x < units.size(); x++) {
         Unit currUnit = units.get(x);
         if(currUnit.hasMoved())
            continue;
         if(getOpponent().getUnits().size() <= 0)
            break;
         // find unit you want to attack
         target = chooseTarget(currUnit);
         targetUnit = game.getWorld().getTiles()[target.getY()][target.getX()].getOccupiedBy();
         int targetX = targetUnit.getX();
         int targetY = targetUnit.getY();
         
         // generate valid move spots for unit
         currUnit.calcMoveSpots();
         Coordinate moveHere = null;
         double lowestDistance = Integer.MAX_VALUE;
         double currentDistance = 0;
         
         // if the unit can capture a city, have it do that
         if(currUnit.getType() == UNIT_TYPE.INFANTRY) {
            Iterator<Coordinate> moveSetIterator = currUnit.getMoveSet().iterator();
            while(moveSetIterator.hasNext()) {
               Coordinate currentSpot = moveSetIterator.next();
               if(currUnit.getCaptureSet().contains(currentSpot)) {
                  game.gameLoop(currUnit.getX(), currUnit.getY());
                  delay(animationDelay);
                  game.gameLoop(currentSpot.getX(), currentSpot.getY());
                  delay(animationDelay);
                  break;
               }
            }
         }
         
         // if unit can attack target, do it
         if(!currUnit.hasMoved() && currUnit.getAttackSet().contains(new Coordinate(targetX, targetY))) {
            game.gameLoop(currUnit.getX(), currUnit.getY());
            delay(animationDelay);
            game.gameLoop(targetX, targetY);
            delay(animationDelay);
         }
         // else iterate through all valid spots, find valid spot closest to target
         else if(!currUnit.hasMoved()) {
            Iterator<Coordinate> moveSetIterator = currUnit.getMoveSet().iterator();
            while(moveSetIterator.hasNext()) {
               Coordinate currentSpot = moveSetIterator.next();
               if(currUnit.getMoveSet().contains(currentSpot)) {
                  // calculate distance from spot to target using a breadth first search
                  currentDistance = bfsToBestSpot(game.getWorld().getTiles().clone(), currentSpot.getX(), currentSpot.getY(), targetX, targetY,
                        currUnit);
                  if(currentDistance < lowestDistance) {
                     moveHere = new Coordinate(currentSpot.getX(), currentSpot.getY());
                     lowestDistance = currentDistance;
                  }
               }
            }
            // if you found a spot to move to go there
            if(moveHere != null) {
               game.gameLoop(currUnit.getX(), currUnit.getY());
               delay(animationDelay);
               game.gameLoop(moveHere.getX(), moveHere.getY());
               delay(animationDelay);
            }
         }
         // if you reached here without moving the unit never moved
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
   
   // conduct a breadth first search to find shortest path between (row, col) and (targetY, targetX)
   public int bfsToBestSpot(Tile[][] world, int col, int row, int targetX, int targetY, Unit unit) {
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
         else if(t.equals(world[row][col])) {
            return distance;
         }
         else {
            int[] yDirs = { 0, 0, 1, -1 };
            int[] xDirs = { 1, -1, 0, 0 };
            for(int i = 0; i < 4; i++)
               try {
                  if(!set.contains(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]])) {
                     if(!world[t.getY() + yDirs[i]][t.getX() + xDirs[i]].isOccupied())
                        if(unit.getClassification() == UNIT_CLASS.VEHICLE && world[t.getY() + yDirs[i]][t.getX() + xDirs[i]].isVehiclePassable()) {
                           set.add(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                           queue.offer(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                        }
                        else if(unit.getClassification() == UNIT_CLASS.INFANTRY
                              && world[t.getY() + yDirs[i]][t.getX() + xDirs[i]].isInfantryPassable()) {
                           set.add(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                           queue.offer(world[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                        }
                  }
               }
               catch(Exception e) {
                  // out of bounds
               }
         }
      }
      return distance;
   }
   
   public Coordinate chooseTarget(Unit attacker) {
      // create HashMap, keySet is all enemy hits
      HashMap<Unit, Integer> numInRange = new HashMap<Unit, Integer>();
      ListIterator<Unit> opponentI = getOpponent().getUnits().listIterator();
      while(opponentI.hasNext())
         numInRange.put(opponentI.next(), 0);
      
      // determine what enemy units attacker can attack
      Unit currUnit = attacker;
      currUnit.calcMoveSpots();
      Iterator<Coordinate> moveSetIterator = currUnit.getMoveSet().iterator();
      while(moveSetIterator.hasNext()) {
         Coordinate currentSpot = moveSetIterator.next();
         if(currUnit.getAttackSet().contains(currentSpot)) {
            // increment HashMap value of any enemy unit you can attack
            Unit temp = game.getWorld().getTiles()[currentSpot.getY()][currentSpot.getX()].getOccupiedBy();
            numInRange.put(temp, numInRange.get(temp) + 1);
         }
      }
      
      // find all units attacker can attack
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
      
      // if no one can be attacked, find closest enemy to the attacker
      if(currentMax <= 0) {
         possibleTargets.clear();
         numInRange.clear();
         return findFutureTarget(attacker);
      }
      // if multiple enemies in range, then attack whoever attacker can damage the most
      else if(possibleTargets.size() > 1) {
         possibleTargets.clear();
         numInRange.clear();
         return chooseBestTarget(attacker);
      }
      // found one target, return coordinate of the target
      return new Coordinate(possibleTargets.get(0).getX(), possibleTargets.get(0).getY());
   }
   
   public Coordinate findFutureTarget(Unit attacker) {
      // create HashMap, key set is all enemy units
      HashMap<Unit, Double> distanceHash = new HashMap<Unit, Double>();
      // go through each enemy unit
      ListIterator<Unit> targetI = getOpponent().getUnits().listIterator();
      while(targetI.hasNext()) {
         Unit currOpponent = targetI.next();
         // determine absolute distance to the enemy
         Unit currFriendly = attacker;
         distanceHash.put(currOpponent,
               (double)(Math.abs(currFriendly.getX() - currOpponent.getX()) + Math.abs(currFriendly.getY() - currOpponent.getY())));
      }
      // find enemy unit with lowest distance to the attacker
      Map.Entry<Unit, Double> minEntry = null;
      for(Map.Entry<Unit, Double> entry: distanceHash.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) <= 0)
            minEntry = entry;
      
      return new Coordinate(minEntry.getKey().getX(), minEntry.getKey().getY(), minEntry.getValue());
   }
   
   public Coordinate chooseBestTarget(Unit attacker) {
      // create HashMap of all enemy units
      HashMap<Unit, Double> numInRange = new HashMap<Unit, Double>();
      ListIterator<Unit> opponentI = getOpponent().getUnits().listIterator();
      while(opponentI.hasNext())
         numInRange.put(opponentI.next(), 0.0);
      
      // go through all enemy units in range and determine how much damage attacker can inflict
      Unit currUnit = attacker;
      currUnit.calcMoveSpots();
      Iterator<Coordinate> moveSetIterator = currUnit.getMoveSet().iterator();
      while(moveSetIterator.hasNext()) {
         Coordinate currentSpot = moveSetIterator.next();
         if(currUnit.getAttackSet().contains(currentSpot)) {
            // increment HashMap value of any enemy unit you can attack
            Unit temp = game.getWorld().getTiles()[currentSpot.getY()][currentSpot.getX()].getOccupiedBy();
            numInRange.put(temp, numInRange.get(temp) + currUnit.getMaxAttack() + currUnit.getCurrentTile().getEffect().attackBonus);
         }
      }
      
      // find enemy units with the highest potential damage from the attacker
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
            if(maxEntry == null
                  || ((Integer)(entry.getKey().getDefense() + entry.getKey().getCurrentTile().getEffect().defenseBonus)).compareTo((Integer)(maxEntry
                        .getKey().getDefense() + maxEntry.getKey().getCurrentTile().getEffect().defenseBonus)) <= 0 && entry.getValue() > 0)
               maxEntry = entry;
         }
      }
      return new Coordinate(maxEntry.getKey().getX(), maxEntry.getKey().getY(), maxEntry.getValue());
   }
}
