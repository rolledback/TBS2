package com.rolledback.framework;

import java.awt.Image;
import java.util.ArrayList;
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
import com.rolledback.units.Unit.DIRECTION;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class World {
   private Team teamOne, teamTwo;
   private Tile tiles[][];
   private int heightMap[][];
   private int width, height;
   private GraphicsManager manager;
   
   public World(int w, int h, Team a, Team b) {
      width = w;
      height = h;
      tiles = new Tile[h][w];
      heightMap = new int[h][w];
      teamOne = a;
      teamTwo = b;
      manager = new GraphicsManager();
      buildMap();
      // buildArmy(teamOne, 0, w / 5);
      // buildArmy(teamTwo, w - (w / 5), w);
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
   
   public void calcMoveSpots(Unit unit) {
      int adHocRange = unit.getMoveRange() + unit.getCurrentTile().getEffect().moveBonus;
      if(adHocRange <= 0)
         adHocRange = 1;
      unit.getCurrentTile().setOccupied(false);
      unit.getAttackSet().clear();
      unit.getMoveSet().clear();
      unit.getCaptureSet().clear();
      calcMoveSpotsHelper(unit, unit.getX(), unit.getY(), adHocRange + 1, false);
      unit.getCurrentTile().setOccupied(true);
      unit.getMoveSet().remove(new Coordinate(unit.getX(), unit.getY()));
   }
   
   public void calcMoveSpotsHelper(Unit unit, int x, int y, int range, boolean movedThrough) {
      Coordinate thisCoord = new Coordinate(x, y);
      if(x < 0 || x >= width || y < 0 || y >= height)
         return;
      else if(range <= 0) {
         if(tiles[y][x].isOccupied() && !unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner()) && !movedThrough)
            unit.getAttackSet().add(thisCoord);
         return;
      }
      else if(tiles[y][x].isOccupied()) {
         if(!unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner()) && !movedThrough)
            unit.getAttackSet().add(thisCoord);
      }
      else if(unit.getType() == UNIT_TYPE.INFANTRY && tiles[y][x].getType() == TILE_TYPE.CITY
            && (((City)tiles[y][x]).getOwner() == null || !((City)tiles[y][x]).getOwner().equals(unit.getOwner()))) {
         unit.getCaptureSet().add(thisCoord);
         
      }
      else if(tiles[y][x].getType() == TILE_TYPE.CITY)
         unit.getMoveSet().add(thisCoord);
      else if(canTraverse(unit, tiles[y][x])) {
         unit.getMoveSet().add(thisCoord);
      }
      range--;
      if(unit.getCaptureSet().contains(thisCoord) || unit.getMoveSet().contains(thisCoord) || tiles[y][x].isOccupied()
            && unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner())) {
         boolean mT = tiles[y][x].isOccupied() && unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner());
         calcMoveSpotsHelper(unit, x + 1, y, range, mT);
         calcMoveSpotsHelper(unit, x - 1, y, range, mT);
         calcMoveSpotsHelper(unit, x, y + 1, range, mT);
         calcMoveSpotsHelper(unit, x, y - 1, range, mT);
      }
      else
         return;
   }
   
   public boolean canTraverse(Unit unit, Tile tile) {
      if(tile.getType() == TILE_TYPE.RIVER)
         return false;
      if(tile.getType() == TILE_TYPE.MOUNTAIN && unit.getClassification() != UNIT_CLASS.INFANTRY)
         return false;
      return true;
   }
   
   public void buildMap() {
      long start = System.currentTimeMillis();
      System.out.println("> building map");
      System.out.println("> dimensions: " + width + ", " + height);
      initialTerrain();
      createHeightMap();
      generateRivers();
      placeFactories(teamOne, 0, width / 5);
      placeFactories(teamTwo, width - (width / 5), width);
      placeCities((int)Math.sqrt(width + height), (int)(width / 5), (int)(width - (width / 5)));
      long end = System.currentTimeMillis();
      System.out.println("> map building complete (" + (end - start) + " milliseconds)");
   }
   
   public void initialTerrain() {
      System.out.println("> creating initial terrain");
      double numPlains = 0;
      double numForests = 0;
      double numMountains = 0;
      
      for(int row = 0; row < tiles.length; row++)
         for(int col = 0; col < tiles[row].length; col++) {
            double type = Math.random();
            if(type <= .75) {
               numPlains++;
               tiles[row][col] = new Plain(this, col, row, manager.tileTextures[0]);
            }
            else if(type > .75 && type <= .94) {
               numForests++;
               tiles[row][col] = new Forest(this, col, row, manager.tileTextures[1]);
            }
            else {
               numMountains++;
               tiles[row][col] = new Mountain(this, col, row, manager.tileTextures[2]);
            }
         }
      numPlains /= (double)(width * height);
      numForests /= (double)(width * height);
      numMountains /= (double)(width * height);
      System.out.println("> creation completed");
      System.out.println("> plains: " + numPlains * 100 + "% forest: " + numForests * 100 + "% mountains: " + numMountains * 100 + "%");
   }
   
   public void createHeightMap() {
      System.out.println("> creating height map");
      double heightAvg = 0;
      for(int row = 0; row < tiles.length; row++)
         for(int col = 0; col < tiles[row].length; col++) {
            if(tiles[row][col].getType() == TILE_TYPE.MOUNTAIN)
               heightAvg += heightMap[row][col] = 3;
            else if(mountainNextTo(col, row)) {
               heightAvg += heightMap[row][col] = 2;
               if(row < height - 1 && heightMap[row + 1][col] != 2 && heightMap[row + 1][col] != 3)
                  heightAvg += heightMap[row + 1][col] = 1;
               
               if(row > 0 && heightMap[row - 1][col] != 2 && heightMap[row - 1][col] != 3)
                  heightAvg += heightMap[row - 1][col] = 1;
               
               if(col < width - 1 && heightMap[row][col + 1] != 2 && heightMap[row][col + 1] != 3)
                  heightAvg += heightMap[row][col + 1] = 1;
               
               if(col > 0 && heightMap[row][col - 1] != 2 && heightMap[row][col - 1] != 3)
                  heightAvg += heightMap[row][col - 1] = 1;
            }
         }
      System.out.println("> height map complete");
      System.out.println("> average height: " + heightAvg / (width * height));
   }
   
   public void generateRivers() {
      System.out.println("> creating rivers");
      int maxLength = (int)(Math.sqrt(height * height + width * width) / 2);
      int minLength = maxLength - (maxLength / 2);
      int numRivers = maxLength / 5;
      int numGenerated = 0;
      int numAttempted = 0;
      int riverFraction = 3;
      int minRivers = (numRivers < riverFraction) ? 1 : numRivers / riverFraction;
      System.out.println("> max river length = " + maxLength);
      System.out.println("> min river length = " + minLength);
      System.out.println("> num rivers wanted = " + numRivers);
      System.out.println("> min rivers needed = " + minRivers);
      for(int x = 0; x < numRivers; x++) {
         System.out.print("> generation attempt " + numAttempted + "...");
         int genAttempts = 0;
         ArrayList<Coordinate> river = new ArrayList<Coordinate>();
         while(river.size() < 2 && genAttempts < 250) {
            river = generateRiver(maxLength);
            genAttempts++;
            if(river.size() < 2 || river.size() < minLength)
               river.clear();
         }
         numAttempted++;
         if(genAttempts < 25) {
            for(Coordinate c: river)
               tiles[c.getY()][c.getX()] = new River(this, c.getX(), c.getY(), null);
            setRiverTileDirections(river);
            int b = placeBridges(river);
            System.out.println("success (length " + river.size() + ", " + b + " bridges)");
            numGenerated++;
         }
         else
            System.out.println("generation failure");
         if(x == numRivers - 1 && numGenerated < minRivers)
            x = 0;
      }
      double rate = ((double)(numAttempted - numGenerated) / (double)numAttempted * 100.0);
      System.out.println("> " + numGenerated + " rivers generated (" + rate + "% failure)");
   }
   
   public ArrayList<Coordinate> generateRiver(int MLE) {
      ArrayList<Coordinate> riverPath = new ArrayList<Coordinate>();
      Random rand = new Random();
      int bound = 5;
      int row = rand.nextInt(height - (height / bound) - (height / bound)) + (height / bound);
      int col = rand.nextInt(width - (width / bound) - (width / bound)) + (width / bound);
      int attempts = 0;
      while(heightMap[row][col] != 2 || tiles[row][col].getType() == TILE_TYPE.RIVER || numRiverNextTo(col, row, riverPath) != 0) {
         row = rand.nextInt(height - (height / bound) - (height / bound)) + (height / bound);
         col = rand.nextInt(width - (width / bound) - (width / bound)) + (width / bound);
         attempts++;
         if(attempts > 25)
            break;
      }
      if(attempts > 25) {
         riverPath.clear();
         return riverPath;
      }
      riverPath.add(new Coordinate(col, row));
      int maxLength = rand.nextInt(MLE - (MLE / 2)) + (MLE / 2);
      int length = 0;
      while(length < maxLength) {
         Coordinate next = nextRiverSpot(col, row, riverPath);
         if(next == null)
            break;
         riverPath.add(next);
         length++;
         col = next.getX();
         row = next.getY();
      }
      return riverPath;
   }
   
   public Coordinate nextRiverSpot(int col, int row, ArrayList<Coordinate> pathSoFar) {
      Coordinate next = null;
      int height = heightMap[row][col];
      int attempts = 0;
      
      while(next == null && attempts < 100) {
         double dir = Math.random();
         try {
            if(dir < .35 && heightMap[row + 1][col] <= height && numRiverNextTo(col, row + 1, pathSoFar) <= 1
                  && !pathSoFar.contains(new Coordinate(col, row + 1)) && tiles[row + 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col, row + 1);
            else if(dir >= .65 && heightMap[row - 1][col] <= height && numRiverNextTo(col, row - 1, pathSoFar) <= 1
                  && !pathSoFar.contains(new Coordinate(col, row - 1)) && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col, row - 1);
            else if(dir >= .5 && dir < .65 && heightMap[row][col + 1] <= height && numRiverNextTo(col + 1, row, pathSoFar) <= 1
                  && !pathSoFar.contains(new Coordinate(col + 1, row)) && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col + 1, row);
            else if(dir >= .35 && dir < .5 && heightMap[row][col - 1] <= height && numRiverNextTo(col - 1, row, pathSoFar) <= 1
                  && !pathSoFar.contains(new Coordinate(col - 1, row)) && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col - 1, row);
            if(next == null)
               attempts++;
         }
         catch(Exception e) {
         }
      }
      return next;
   }
   
   public int numRiverNextTo(int col, int row, ArrayList<Coordinate> pathSoFar) {
      int numRivers = 0;
      if(row + 1 < height && (pathSoFar.contains(new Coordinate(col, row + 1)) || tiles[row + 1][col].getType() == TILE_TYPE.RIVER))
         numRivers++;
      if(row - 1 >= 0 && (pathSoFar.contains(new Coordinate(col, row - 1)) || tiles[row - 1][col].getType() == TILE_TYPE.RIVER))
         numRivers++;
      if(col + 1 < width && (pathSoFar.contains(new Coordinate(col + 1, row)) || tiles[row][col + 1].getType() == TILE_TYPE.RIVER))
         numRivers++;
      if(col - 1 >= 0 && (pathSoFar.contains(new Coordinate(col - 1, row)) || tiles[row][col - 1].getType() == TILE_TYPE.RIVER))
         numRivers++;
      return numRivers;
   }
   
   public int placeBridges(ArrayList<Coordinate> path) {
      Random rand = new Random();
      int spot = rand.nextInt(path.size());
      int attempts = 0;
      int numBridges = 0;
      int limit = (path.size() <= 4) ? 1 : path.size() / 5;
      while(numBridges < limit && attempts < 50) {
         Tile currTile = tiles[path.get(spot).getY()][path.get(spot).getX()];
         Image spotImage = currTile.getTexture();
         if(currTile.getY() > 0 && currTile.getY() < height - 1) {
            if(spotImage.equals(manager.tileTextures[18])) {
               Tile nextTile = tiles[path.get(spot + 1).getY()][path.get(spot + 1).getX()];
               Tile prevTile = tiles[path.get(spot - 1).getY()][path.get(spot - 1).getX()];
               if(nextTile.getType() != TILE_TYPE.BRIDGE && prevTile.getType() != TILE_TYPE.BRIDGE) {
                  tiles[path.get(spot).getY()][path.get(spot).getX()] = new Bridge(this, path.get(spot).getX(), path.get(spot).getY(),
                        manager.tileTextures[29]);
                  numBridges++;
               }
            }
            else if(spotImage.equals(manager.tileTextures[19])) {
               Tile nextTile = tiles[path.get(spot + 1).getY()][path.get(spot + 1).getX()];
               Tile prevTile = tiles[path.get(spot - 1).getY()][path.get(spot - 1).getX()];
               if(nextTile.getType() != TILE_TYPE.BRIDGE && prevTile.getType() != TILE_TYPE.BRIDGE) {
                  tiles[path.get(spot).getY()][path.get(spot).getX()] = new Bridge(this, path.get(spot).getX(), path.get(spot).getY(),
                        manager.tileTextures[28]);
                  numBridges++;
               }
            }
            else
               attempts++;
         }
         else
            attempts++;
         spot = rand.nextInt(path.size());
      }
      return numBridges;
   }
   
   public void setRiverTileDirections(ArrayList<Coordinate> riverPath) {
      int currX = riverPath.get(0).getX();
      int currY = riverPath.get(0).getY();
      
      int nextX = riverPath.get(1).getX();
      int nextY = riverPath.get(1).getY();
      
      int prevX = 0;
      int prevY = 0;
      
      if(currX == nextX) {
         if(currY < nextY)
            tiles[currY][currX].setTexture(manager.tileTextures[22]);
         else
            tiles[currY][currX].setTexture(manager.tileTextures[20]);
         ;
      }
      else {
         if(currX < nextX)
            tiles[currY][currX].setTexture(manager.tileTextures[21]);
         else
            tiles[currY][currX].setTexture(manager.tileTextures[23]);
      }
      
      for(int x = 1; x < riverPath.size() - 1; x++) {
         prevX = currX;
         prevY = currY;
         currX = riverPath.get(x).getX();
         currY = riverPath.get(x).getY();
         nextX = riverPath.get(x + 1).getX();
         nextY = riverPath.get(x + 1).getY();
         
         if(prevX == currX && currX == nextX)
            if(tiles[currY][currX].getType() == TILE_TYPE.RIVER)
               tiles[currY][currX].setTexture(manager.tileTextures[19]);
            else
               tiles[currY][currX].setTexture(manager.tileTextures[28]);
         else if(prevY == currY && currY == nextY)
            if(tiles[currY][currX].getType() == TILE_TYPE.RIVER)
               tiles[currY][currX].setTexture(manager.tileTextures[18]);
            else
               tiles[currY][currX].setTexture(manager.tileTextures[29]);
         else if(prevX < nextX) {
            if(prevY < nextY) {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(manager.tileTextures[25]);
               if(prevY == currY)
                  tiles[currY][currX].setTexture(manager.tileTextures[26]);
            }
            else {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(manager.tileTextures[27]);
               else
                  tiles[currY][currX].setTexture(manager.tileTextures[24]);
            }
            
         }
         else if(prevX > nextX) {
            if(prevY > nextY) {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(manager.tileTextures[26]);
               if(prevY == currY)
                  tiles[currY][currX].setTexture(manager.tileTextures[25]);
            }
            else {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(manager.tileTextures[24]);
               else
                  tiles[currY][currX].setTexture(manager.tileTextures[27]);
            }
         }
      }
      
      if(nextX == currX) {
         if(nextY < currY)
            tiles[nextY][nextX].setTexture(manager.tileTextures[22]);
         else
            tiles[nextY][nextX].setTexture(manager.tileTextures[20]);
      }
      else {
         if(nextX < currX)
            tiles[nextY][nextX].setTexture(manager.tileTextures[21]);
         else
            tiles[nextY][nextX].setTexture(manager.tileTextures[23]);
      }
   }
   
   public void placeCities(int numCities, int min, int max) {
      System.out.println("> placing cities");
      for(int x = 0; x < numCities; x++) {
         Random rand = new Random();
         int col = rand.nextInt(max - min) + min;
         int row = rand.nextInt(height - 1) + 1;
         while(tiles[row][col].getType() != TILE_TYPE.PLAIN) {
            col = rand.nextInt(max - min) + min;
            row = rand.nextInt(height - 1) + 1;
         }
         tiles[row][col] = new City(this, col, row, null, manager.tileTextures[15]);
      }
      
   }
   
   public void placeFactories(Team team, int min, int max) {
      System.out.println("> placing factories for " + team.getName());
      for(int col = min; col < max; col += 2) {
         int row = (int)(Math.random() * height);
         while(tiles[row][col].getType() == TILE_TYPE.RIVER || tiles[row][col].getType() == TILE_TYPE.MOUNTAIN
               || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
            row = (int)(Math.random() * height);
         }
         if(team.equals(teamOne))
            tiles[row][col] = new Factory(this, col, row, team, manager.tileTextures[12]);
         else
            tiles[row][col] = new Factory(this, col, row, team, manager.tileTextures[13]);
         team.getFactories().add((Factory)tiles[row][col]);
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
                  tiles[row - 1][col] = new Plain(this, col, row - 1, manager.tileTextures[0]);
               else if(!(row + 1 >= height))
                  tiles[row + 1][col] = new Plain(this, col, row + 1, manager.tileTextures[0]);
               else if(!(col + 1 >= width))
                  tiles[row][col + 1] = new Plain(this, col + 1, row, manager.tileTextures[0]);
               else
                  tiles[row][col - 1] = new Plain(this, col - 1, row, manager.tileTextures[0]);
         }
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
         if(team.equals(teamTwo))
            team.getUnits().get(x).setDir(DIRECTION.LEFT);
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
      // return UNIT_TYPE.INFANTRY;
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
   
   public GraphicsManager getManager() {
      return manager;
   }
   
   public void setManager(GraphicsManager manager) {
      this.manager = manager;
   }
   
   public Team getTeamTwo() {
      return teamTwo;
   }
   
   public void setTeamTwo(Team teamTwo) {
      this.teamTwo = teamTwo;
   }
   
   public Team getTeamOne() {
      return teamOne;
   }
   
   public void setTeamOne(Team teamOne) {
      this.teamOne = teamOne;
   }
   
}
