package com.rolledback.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import com.rolledback.units.Unit;

public class ComputerTeam extends Team {
   
   Game game;
   Team opponent;
   
   public ComputerTeam(String name, int size, int r, Game g) {
      super(name, size, r);
      game = g;
   }
   
   public void executeTurn() {
      Coordinate target = chooseTarget();
      //System.out.println("Target coordinates: " + target.toString());
      int targetX = target.getX();
      int targetY = target.getY();
      for(int x = 0; x < units.size(); x++) {
         Unit currUnit = units.get(x);
         int[][] moveSpots = game.world.calcMoveSpots(currUnit);
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
                  if(col == targetX)
                     currentDistance = Math.abs(row - targetY);
                  else if(row == targetY)
                     currentDistance = Math.abs(col - targetX);
                  else 
                     currentDistance = (Math.abs(col - targetX) / Math.abs(row - targetY));
                  //System.out.println("Distance of " + col + "," + row + " to target is " + currentDistance);
                  if(currentDistance < lowestDistance) {
                     moveHere = new Coordinate(col, row);
                     lowestDistance = currentDistance;
                  }
               }                  
            }
         }
         if(moveHere != null) {
            //System.out.println("Move a unit to: " + moveHere.toString());
            game.gameLoop(currUnit.getX(), currUnit.getY());
            game.gameLoop(moveHere.getX(), moveHere.getY());
         }
      }
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
         possibleTargets.clear();
         numInRange.clear();
         return findFutureTarget();   
      }
      else if(possibleTargets.size() > 1) {
         possibleTargets.clear();
         numInRange.clear();
         return chooseBestTarget();
      }
      return new Coordinate(possibleTargets.get(0).getX(), possibleTargets.get(0).getY());
   }
   
   public Coordinate findFutureTarget() {
      //System.out.println("By distance.");
      HashMap<Unit, Double> distanceHash = new HashMap<Unit, Double>();
      ListIterator<Unit> opponentI = opponent.getUnits().listIterator();
      while(opponentI.hasNext()) {  
         Unit currOpponent = opponentI.next();
         double distanceAvg = 0.0;
         ListIterator<Unit> unitI = units.listIterator();
         while(unitI.hasNext()) {
            Unit currFriendly = unitI.next();
            if(currFriendly.getX() == currOpponent.getX())
               distanceAvg += Math.abs(currFriendly.getY() - currOpponent.getY());
            else if(currFriendly.getY() == currOpponent.getY())
               distanceAvg += Math.abs(currFriendly.getX() - currOpponent.getX());
            else if(currFriendly.getY() == currOpponent.getY() && currFriendly.getX() == currOpponent.getX())
               distanceAvg += 0;
            else 
               distanceAvg += (Math.abs(currFriendly.getX() - currOpponent.getX()) / Math.abs(currFriendly.getY() - currOpponent.getY()));
         }
         distanceAvg /= opponent.getUnits().size();
         distanceHash.put(currOpponent, distanceAvg);
      }

      Map.Entry<Unit, Double> minEntry = null;
      for (Map.Entry<Unit, Double> entry : distanceHash.entrySet()) {
          if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0) {
             minEntry = entry;
          }
      }
      return new Coordinate(minEntry.getKey().getX(), minEntry.getKey().getY(), minEntry.getValue());
   }
   
   public Coordinate chooseBestTarget() {
      //System.out.println("By potential damage");
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
