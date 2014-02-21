package com.rolledback.teams;

import java.util.ArrayList;
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
import com.rolledback.units.Infantry;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class ComputerTeamC extends ComputerTeam {
   
   final int animationDelay = 100;
   ArrayList<Coordinate> cityLocations;
   public int[] unitsProduced = new int[4];
   
   public ComputerTeamC(String name, int size, int r, Game g) {
      super(name, size, r, g);
      cityLocations = null;
   }
   
   public void executeTurn() {
      // if no one to attack, just end your turn
      if(opponent.getUnits().size() <= 0)
         return;
      
      // iterate through all units
      for(int x = 0; x < units.size(); x++) {
         Unit currUnit = units.get(x);
         
         // make sure the unit hasn't moved yet
         if(currUnit.hasMoved())
            continue;
         
         Coordinate moveHere = null;
         game.getWorld().calcMoveSpots(currUnit);
         
         // if you can't move, go to the next unit
         if(currUnit.getMoveSet().size() <= 0)
            continue;
         
         // if unit is infantry do specialized infantry logic
         if(currUnit.getType() == UNIT_TYPE.INFANTRY) {
            moveHere = moveInfantry((Infantry)currUnit);
            if(moveHere == null)
               moveHere = moveUnit(currUnit);
         }
         // if any type of unit, do standard move logic
         else {
            moveHere = moveUnit(currUnit);
         }
         
         if(moveHere != null) {
            game.gameLoop(currUnit.getX(), currUnit.getY());
            delay(animationDelay);
            game.gameLoop(moveHere.getX(), moveHere.getY());
            delay(animationDelay);
         }
         
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
         unitsProduced[unitToProduce]++;
      }
      
   }
   
   public Coordinate moveUnit(Unit unit) {
      // find closest enemy unit
      HashMap<Unit, Integer> unitDistances = new HashMap<Unit, Integer>();
      Iterator<Unit> enemyUnitIterator = opponent.getUnits().iterator();
      while(enemyUnitIterator.hasNext()) {
         Unit currentEnemy = enemyUnitIterator.next();
         int distance = bfsToBestSpot(game.getWorld().getTiles(), unit.getX(), unit.getY(), currentEnemy.getX(), currentEnemy.getY(), unit);
         unitDistances.put(currentEnemy, distance);
      }
      
      // sort the list
      Map.Entry<Unit, Integer> minEntry = null;
      for(Map.Entry<Unit, Integer> entry: unitDistances.entrySet())
         if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
            minEntry = entry;
      
      if(minEntry == null)
         return null;
      Unit closestUnit = minEntry.getKey();
      
      // if you can attack the enemy, do so
      if(unit.getAttackSet().contains(new Coordinate(closestUnit.getY(), closestUnit.getX())))
         return new Coordinate(closestUnit.getX(), closestUnit.getY());
      
      // if not, find move spot closest to that unit
      HashMap<Coordinate, Integer> moveDistances = new HashMap<Coordinate, Integer>();
      Iterator<Coordinate> moveSetIterator = unit.getMoveSet().iterator();
      while(moveSetIterator.hasNext()) {
         Coordinate moveCoordinate = moveSetIterator.next();
         int distance = bfsToBestSpot(game.getWorld().getTiles(), closestUnit.getX(), closestUnit.getY(), moveCoordinate.getX(),
               moveCoordinate.getY(), unit);
         moveDistances.put(moveCoordinate, distance);
      }
      
      // sort the list
      Map.Entry<Coordinate, Integer> minEntryTwo = null;
      for(Map.Entry<Coordinate, Integer> entry: moveDistances.entrySet())
         if(minEntryTwo == null || entry.getValue().compareTo(minEntryTwo.getValue()) < 0)
            minEntryTwo = entry;
      Coordinate closest = minEntryTwo.getKey();
      return closest;
   }
   
   public Coordinate moveInfantry(Infantry unit) {      
      // if infantry is on a city, defend the city if needed
      if(game.getWorld().getTiles()[unit.getY()][unit.getX()].getType() == TILE_TYPE.CITY) {
         int[] yDirs = { 0, 0, 1, -1 };
         int[] xDirs = { 1, -1, 0, 0 };
         for(int i = 0; i < 4; i++)
            try {
               int r = unit.getY() + yDirs[i];
               int c = unit.getX() + xDirs[i];
               if(unit.getAttackSet().contains(new Coordinate(r, c)))
                  return new Coordinate(c, r);
            }
            catch(Exception e) {
               // out of bounds
            }
      }
      
      // find the locations of all cities (Should cache this)
      if(cityLocations == null) {
         cityLocations = new ArrayList<Coordinate>();
         for(int row = 0; row < game.gameHeight; row++)
            for(int col = 0; col < game.gameWidth; col++)
               if(game.getWorld().getTiles()[row][col].getType() == TILE_TYPE.CITY)
                  cityLocations.add(new Coordinate(col, row));
      }
      
      // find the closest city not owned by your team
      HashMap<Coordinate, Integer> cityDistances = new HashMap<Coordinate, Integer>();
      Iterator<Coordinate> cityIterator = cityLocations.iterator();
      while(cityIterator.hasNext()) {
         Coordinate cityLoc = cityIterator.next();
         Team cityOwner = ((City)game.getWorld().getTiles()[cityLoc.getY()][cityLoc.getX()]).getOwner();
         if(cityOwner == null || cityOwner.equals(opponent)) {
            int distance = bfsToBestSpot(game.getWorld().getTiles(), unit.getX(), unit.getY(), cityLoc.getX(), cityLoc.getY(), unit);
            cityDistances.put(cityLoc, distance);
         }
      }
      
      // sort the list
      if(cityDistances.size() > 0) {
         Map.Entry<Coordinate, Integer> minEntry = null;
         for(Map.Entry<Coordinate, Integer> entry: cityDistances.entrySet())
            if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
               minEntry = entry;
         Coordinate closest = minEntry.getKey();
         
         // if you are in capture/siege range, capture/siege the city
         if(unit.getAttackSet().contains(closest) || unit.getCaptureSet().contains(closest))
            return closest;
         
         // find move spot closest to the city
         HashMap<Coordinate, Integer> distancesToCity = new HashMap<Coordinate, Integer>();
         Iterator<Coordinate> moveSetIterator = unit.getMoveSet().iterator();
         while(moveSetIterator.hasNext()) {
            Coordinate currMoveSpot = moveSetIterator.next();
            if(unit.getMoveSet().contains(currMoveSpot)) {
               int distance = bfsToBestSpot(game.getWorld().getTiles().clone(), currMoveSpot.getX(), currMoveSpot.getY(), closest.getX(),
                     closest.getY(), unit);
               distancesToCity.put(currMoveSpot, distance);
            }
         }
         
         // sort the list
         minEntry = null;
         for(Map.Entry<Coordinate, Integer> entry: distancesToCity.entrySet())
            if(minEntry == null || entry.getValue().compareTo(minEntry.getValue()) < 0)
               minEntry = entry;
         if(minEntry != null)
            return minEntry.getKey();
      }
      return null;
   }
   
   // find shortest path btw spots for a given unit
   public int bfsToBestSpot(Tile[][] world, int col, int row, int targetX, int targetY, Unit unit) {
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
