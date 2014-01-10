package com.rolledback.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Bridge;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Forest;
import com.rolledback.terrain.Mountain;
import com.rolledback.terrain.Plain;
import com.rolledback.terrain.River;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class World {
   private Team teamOne, teamTwo;
   private Tile tiles[][];
   private int heightMap[][];
   private int width, height;
   
   public World(int w, int h, Team a, Team b) {
      width = w;
      height = h;
      tiles = new Tile[h][w];
      heightMap = new int[h][w];
      teamOne = a;
      teamTwo = b;
      buildMap();
      buildArmy(teamOne, 0, w / 5);
      buildArmy(teamTwo, w - (w / 5), w);
   }
   
   public void printMap() {
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            System.out.print(tiles[row][col].getMapChar() + " ");
         }
         System.out.println();
      }
   }
   
   public void printUnits() {
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            char uChar = '_';
            if(tiles[row][col].isOccupied()) {
               UNIT_TYPE u = tiles[row][col].getOccupiedBy().getType();
               if(u == UNIT_TYPE.INFANTRY)
                  if(tiles[row][col].getOccupiedBy().getOwner().equals(teamOne))
                     uChar = 'I';
                  else
                     uChar = 'i';
               if(u == UNIT_TYPE.TANK)
                  if(tiles[row][col].getOccupiedBy().getOwner().equals(teamOne))
                     uChar = 'T';
                  else
                     uChar = 't';
               if(u == UNIT_TYPE.TANK_DEST)
                  if(tiles[row][col].getOccupiedBy().getOwner().equals(teamOne))
                     uChar = 'D';
                  else
                     uChar = 'd';
            }
            System.out.print(uChar + " ");
         }
         System.out.println();
      }
   }
   
   public void printUnits(int x, int y) {
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            char uChar = '_';
            if(row == y && col == x)
               uChar = 'X';
            else if(tiles[row][col].isOccupied()) {
               UNIT_TYPE u = tiles[row][col].getOccupiedBy().getType();
               if(u == UNIT_TYPE.INFANTRY)
                  if(tiles[row][col].getOccupiedBy().getOwner().equals(teamOne))
                     uChar = 'I';
                  else
                     uChar = 'i';
               if(u == UNIT_TYPE.TANK)
                  if(tiles[row][col].getOccupiedBy().getOwner().equals(teamOne))
                     uChar = 'T';
                  else
                     uChar = 't';
               if(u == UNIT_TYPE.TANK_DEST)
                  if(tiles[row][col].getOccupiedBy().getOwner().equals(teamOne))
                     uChar = 'D';
                  else
                     uChar = 'd';
            }
            System.out.print(uChar + " ");
         }
         System.out.println();
      }
   }
   
   public int[][] calcMoveSpots(Unit unit) {
      int[][] spots = new int[height][width];
      for(int x = 0; x < spots.length; x++)
         Arrays.fill(spots[x], -1);
      int adHocRange = unit.getMoveRange() + unit.getCurrentTile().getEffect().moveBonus;
      if(adHocRange <= 0)
         adHocRange = 1;
      ArrayList<Coordinate> set = null;
      set = new ArrayList<Coordinate>();
      unit.getCurrentTile().setOccupied(false);
      calcMoveSpotsHelper(unit, spots, unit.getX(), unit.getY(), adHocRange + 1, set);
      spots[unit.getY()][unit.getX()] = 0;
      set.remove(new Coordinate(unit.getX(), unit.getY()));
      unit.getCurrentTile().setOccupied(true);
      unit.setMoveSet(set);
      return spots;
   }
   
   public void calcMoveSpotsHelper(Unit unit, int valid[][], int x, int y, int range, ArrayList<Coordinate> moveSet) {
      if(x < 0 || x >= width || y < 0 || y >= height)
         return;
      else if(valid[y][x] == 0)
         return;
      else if(range <= 0) {
         if(tiles[y][x].isOccupied() && !unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner())) {
            valid[y][x] = 2;
            if(!moveSet.contains(new Coordinate(x, y))) {
               moveSet.add(new Coordinate(x, y));
            }
         }
         return;
      }
      else if(tiles[y][x].isOccupied()) {
         if(!unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner())) {
            valid[y][x] = 2;
            if(!moveSet.contains(new Coordinate(x, y))) {
               moveSet.add(new Coordinate(x, y));
            }
         }
         else
            valid[y][x] = 0;
      }
      else if(unit.getType() == UNIT_TYPE.INFANTRY && tiles[y][x].getType() == TILE_TYPE.CITY
            && (((City)tiles[y][x]).getOwner() == null || !((City)tiles[y][x]).getOwner().equals(unit.getOwner()))) {
         valid[y][x] = 3;
         if(!moveSet.contains(new Coordinate(x, y))) {
            moveSet.add(new Coordinate(x, y));
         }
      }
      else if(!tiles[y][x].isVehiclePassable() && unit.getClassification() == UNIT_CLASS.VEHICLE)
         valid[y][x] = 0;
      else if(!tiles[y][x].isInfantryPassable() && unit.getClassification() == UNIT_CLASS.INFANTRY)
         valid[y][x] = 0;
      else {
         valid[y][x] = 1;
         if(!moveSet.contains(new Coordinate(x, y))) {
            moveSet.add(new Coordinate(x, y));
         }
      }
      range--;
      if(valid[y][x] == 1) {
         calcMoveSpotsHelper(unit, valid, x + 1, y, range, moveSet);
         calcMoveSpotsHelper(unit, valid, x - 1, y, range, moveSet);
         calcMoveSpotsHelper(unit, valid, x, y + 1, range, moveSet);
         calcMoveSpotsHelper(unit, valid, x, y - 1, range, moveSet);
      }
      else
         return;
   }
   
   public void buildMap() {
      for(int row = 0; row < tiles.length; row++) {
         for(int col = 0; col < tiles[row].length; col++) {
            double type = Math.random();
            if(type <= .75)
               tiles[row][col] = new Plain(this, col, row);
            else if(type > .75 && type <= .96)
               tiles[row][col] = new Forest(this, col, row);
            else
               tiles[row][col] = new Mountain(this, col, row);
         }
      }
      createHeightMap();
      int maxLengthLimit = (int)(Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2))) / 2;
      for(int x = 0; x < (int)Math.sqrt(maxLengthLimit); x++) {
         ArrayList<Coordinate> riverPath = generateRiver(maxLengthLimit);
         if(riverPath.size() >= 5)
            for(int y = 0; y < riverPath.size() / 5; y++)
               placeBridge(riverPath);
      }
      
      placeFactories(teamOne, 0, width / 5);
      placeFactories(teamTwo, width - (width / 5), width);
      placeCities((int)Math.sqrt(width + height), (int)(width / 5), (int)(width - (width / 5)));
      lookForTraps();
   }
   
   public void placeCities(int numCities, int min, int max) {
      for(int x = 0; x < numCities; x++) {
         Random rand = new Random();
         int col = rand.nextInt(max - min) + min;
         int row = rand.nextInt(height - 1) + 1;
         while(tiles[row][col].getType() != TILE_TYPE.PLAIN) {
            col = rand.nextInt(max - min) + min;
            row = rand.nextInt(height - 1) + 1;
         }
         tiles[row][col] = new City(this, col, row, null);
      }
      
   }
   
   public void placeBridge(ArrayList<Coordinate> riverPath) {
      Random rand = new Random();
      int spot = rand.nextInt(riverPath.size() - 2) + 1;
      int prevX = riverPath.get(spot - 1).getX();
      int prevY = riverPath.get(spot - 1).getY();
      
      int curX = riverPath.get(spot).getX();
      int curY = riverPath.get(spot).getY();
      
      int nextX = riverPath.get(spot + 1).getX();
      int nextY = riverPath.get(spot + 1).getY();
      int attempts = 0;
      while(!(prevX == curX && nextX == curX) && !(prevY == curY && nextY == curY) && attempts < 100) {
         spot = rand.nextInt(riverPath.size() - 2) + 1;
         prevX = riverPath.get(spot - 1).getX();
         prevY = riverPath.get(spot - 1).getY();
         
         curX = riverPath.get(spot).getX();
         curY = riverPath.get(spot).getY();
         
         nextX = riverPath.get(spot + 1).getX();
         nextY = riverPath.get(spot + 1).getY();
         attempts++;
      }
      if(attempts < 100)
         tiles[riverPath.get(spot).getY()][riverPath.get(spot).getX()] = new Bridge(this, riverPath.get(spot).getX(), riverPath.get(spot).getY());
   }
   
   public void setRiverTileDirections(ArrayList<Coordinate> riverPath) {
      Iterator<Coordinate> riverBoat = riverPath.iterator();
      while(riverBoat.hasNext()) {
         // you should implement this at some point
      }
   }
   
   public ArrayList<Coordinate> generateRiver(int MLE) {
      ArrayList<Coordinate> riverPath = new ArrayList<Coordinate>();
      Random rand = new Random();
      int row = rand.nextInt(height);
      int col = rand.nextInt(width - (width / 4) - (width / 4)) + (width / 4);
      int attempts = 0;
      while(heightMap[row][col] != 2 || tiles[row][col].getType() == TILE_TYPE.RIVER || numRiverNextTo(col, row) != 0 || attempts > 200) {
         row = rand.nextInt(height);
         col = rand.nextInt(width - (width / 4) - (width / 4)) + (width / 4);
         attempts++;
      }
      riverPath.add(new Coordinate(col, row));
      tiles[row][col] = new River(this, col, row);
      int maxLength = rand.nextInt(MLE - (MLE / 2)) + (MLE / 2);
      int length = 0;
      while(length < maxLength) {
         Coordinate next = nextRiverSpot(col, row);
         if(next == null)
            break;
         riverPath.add(next);
         col = next.getX();
         row = next.getY();
         tiles[row][col] = new River(this, col, row);
         length++;
      }
      return riverPath;
   }
   
   public Coordinate nextRiverSpot(int col, int row) {
      Coordinate next = null;
      int height = heightMap[row][col];
      int attempts = 0;
      
      while(next == null && attempts < 100) {
         double dir = Math.random();
         try {
            if(dir < .25 && heightMap[row + 1][col] <= height && numRiverNextTo(col, row + 1) <= 1
                  && tiles[row + 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col, row + 1);
            if(dir >= .75 && heightMap[row - 1][col] <= height && numRiverNextTo(col, row - 1) <= 1
                  && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col, row - 1);
            if(dir >= .5 && dir < .75 && heightMap[row][col + 1] <= height && numRiverNextTo(col + 1, row) <= 1
                  && tiles[row][col + 1].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col + 1, row);
            if(dir >= .25 && dir < .5 && heightMap[row][col - 1] <= height && numRiverNextTo(col - 1, row) <= 1
                  && tiles[row][col - 1].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col - 1, row);
            if(next == null)
               attempts++;
         }
         catch(Exception e) {
         }
      }
      return next;
   }
   
   public int numRiverNextTo(int col, int row) {
      int numRivers = 0;
      if(row + 1 < height && tiles[row + 1][col].getType() == TILE_TYPE.RIVER)
         numRivers++;
      if(row - 1 >= 0 && tiles[row - 1][col].getType() == TILE_TYPE.RIVER)
         numRivers++;
      if(col + 1 < width && tiles[row][col + 1].getType() == TILE_TYPE.RIVER)
         numRivers++;
      if(col - 1 >= 0 && tiles[row][col - 1].getType() == TILE_TYPE.RIVER)
         numRivers++;
      return numRivers;
   }
   
   public void createHeightMap() {
      for(int row = 0; row < tiles.length; row++)
         for(int col = 0; col < tiles[row].length; col++)
            if(tiles[row][col].getType() == TILE_TYPE.MOUNTAIN)
               heightMap[row][col] = 3;
            else if(mountainNextTo(col, row))
               heightMap[row][col] = 2;
      for(int row = 0; row < tiles.length; row++) {
         for(int col = 0; col < tiles[row].length; col++) {
            if(heightMap[row][col] == 2) {
               if(row < height - 1 && heightMap[row + 1][col] != 2 && heightMap[row + 1][col] != 3)
                  heightMap[row + 1][col] = 1;
               
               if(row > 0 && heightMap[row - 1][col] != 2 && heightMap[row - 1][col] != 3)
                  heightMap[row - 1][col] = 1;
               
               if(col < width - 1 && heightMap[row][col + 1] != 2 && heightMap[row][col + 1] != 3)
                  heightMap[row][col + 1] = 1;
               
               if(col > 0 && heightMap[row][col - 1] != 2 && heightMap[row][col - 1] != 3)
                  heightMap[row][col - 1] = 1;
            }
         }
      }
   }
   
   public boolean mountainNextTo(int col, int row) {
      if(col < width - 1 && tiles[row][col + 1].getType() == TILE_TYPE.MOUNTAIN)
         return true;
      else if(col > 0 && tiles[row][col - 1].getType() == TILE_TYPE.MOUNTAIN)
         return true;
      else if(row < height - 1 && tiles[row + 1][col].getType() == TILE_TYPE.MOUNTAIN)
         return true;
      else if(row > 0 && tiles[row - 1][col].getType() == TILE_TYPE.MOUNTAIN)
         return true;
      return false;
   }
   
   public void lookForTraps() {
      boolean north, south, east, west;
      for(int row = 0; row < tiles.length; row++) {
         north = false;
         south = false;
         east = false;
         west = false;
         for(int col = 0; col < tiles[row].length; col++) {
            north = row - 1 < 0 || tiles[row - 1][col].getType() == TILE_TYPE.MOUNTAIN;
            south = row + 1 >= height || tiles[row + 1][col].getType() == TILE_TYPE.MOUNTAIN;
            east = col + 1 >= width || tiles[row][col + 1].getType() == TILE_TYPE.MOUNTAIN;
            west = col - 1 < 0 || tiles[row][col - 1].getType() == TILE_TYPE.MOUNTAIN;
            if(north && south && east && west)
               if(!(row - 1 < 0))
                  tiles[row - 1][col] = new Plain(this, col, row - 1);
               else if(!(row + 1 >= height))
                  tiles[row + 1][col] = new Plain(this, col, row + 1);
               else if(!(col + 1 >= width))
                  tiles[row][col + 1] = new Plain(this, col + 1, row);
               else
                  tiles[row][col - 1] = new Plain(this, col - 1, row);
         }
      }
   }
   
   public void placeFactories(Team team, int min, int max) {
      for(int col = min; col < max; col += 2) {
         int row = (int)(Math.random() * height);
         Tile spot = tiles[row][col];
         while(tiles[row][col].getType() == TILE_TYPE.RIVER || tiles[row][col].getType() == TILE_TYPE.MOUNTAIN
               || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
            row = (int)(Math.random() * height);
            spot = tiles[row][col];
         }
         tiles[row][col] = new Factory(this, col, row, team);
         team.getFactories().add((Factory)tiles[row][col]);
      }
   }
   
   public String mapKey() {
      String key = height + "-" + width;
      String terrainKeys[] = new String[height];
      for(int row = 0; row < tiles.length; row++) {
         terrainKeys[row] = "";
         for(int col = 0; col < tiles[row].length; col++) {
            char currentTile = tiles[row][col].getMapChar();
            switch(currentTile) {
            case ('p'):
               terrainKeys[row] += 0;
               break;
            case ('f'):
               terrainKeys[row] += 1;
               break;
            case ('m'):
               terrainKeys[row] += 2;
               break;
            case ('F'):
               terrainKeys[row] += 3;
               break;
            default:
               terrainKeys[row] += 0;
               break;
            }
         }
      }
      for(int row = 0; row < tiles.length; row++)
         key += '-' + terrainKeys[row];
      return key.toString();
   }
   
   public void buildArmy(Team team, int minCol, int maxCol) {
      int col = minCol;
      for(int x = 0; x < team.getTeamSize(); x++) {
         int row = (int)(Math.random() * height);
         while(tiles[row][col].isOccupied() || tiles[row][col].getType() == TILE_TYPE.MOUNTAIN || tiles[row][col].getType() == TILE_TYPE.RIVER
               || tiles[row][col].getType() == TILE_TYPE.FACTORY || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
            row = (int)(Math.random() * height);
         }
         team.createUnit(tiles[row][col], randUnitType());
         if((x + 1) % (double)(team.getTeamSize() / (width / 5)) == 0)
            col++;
         if(col >= width)
            col--;
      }
   }
   
   public void destroyUnit(Tile t) {
      Unit toRemove = t.getOccupiedBy();
      toRemove.getOwner().removeUnit(toRemove);
      t.setOccupied(false);
      t.setOccupiedBy(null);
   }
   
   public void destroyUnit(Unit u) {
      Team owner = u.getOwner();
      owner.removeUnit(u);
      u.getCurrentTile().setOccupied(false);
      u.getCurrentTile().setOccupiedBy(null);
   }
   
   public UNIT_TYPE randUnitType() {
      return UNIT_TYPE.values()[(int)(Math.random() * UNIT_TYPE.values().length)];
      
   }
   
   public Tile[][] getTiles() {
      return tiles;
   }
   
   public void setTiles(Tile tiles[][]) {
      this.tiles = tiles;
   }
   
   public int[][] getHeightMap() {
      return heightMap;
   }
   
   public void setHeightMap(int heightMap[][]) {
      this.heightMap = heightMap;
   }
}
