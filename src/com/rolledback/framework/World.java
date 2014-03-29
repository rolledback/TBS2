package com.rolledback.framework;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import com.rolledback.mapping.Cartographer;
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
   
   public World(int w, int h, Team a, Team b, int t, String fileToLoad) {
      width = w;
      height = h;
      tiles = new Tile[h][w];
      heightMap = new int[h][w];
      teamOne = a;
      teamTwo = b;
      if(fileToLoad.equals("")) {
         boolean accept = false;
         while(!accept)
            accept = buildMap();
      }
      else
         tiles = (Tile[][])Cartographer.readMapFile(fileToLoad, tiles, this)[1];
   }
   
   public World() {
      teamOne = new Team("", 0, 0, 1);
      teamTwo = new Team("", 0, 0, 2);
   }
   
   public World(int w, int h, boolean rivers) {
      if(rivers) {
         width = w;
         height = h;
      }
      tiles = new Tile[h][w];
      heightMap = new int[h][w];
      teamOne = new Team("", 0, 0, 1);
      teamTwo = new Team("", 0, 0, 2);
      buildMap();
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
   
   public boolean buildMap() {
      long start = System.currentTimeMillis();
      Logger.consolePrint("building map", "map");
      Logger.consolePrint("dimensions: " + width + ", " + height, "map");
      initialTerrain();
      createHeightMap();
      generateRivers();
      placeFactories(teamOne, 0, width / 5);
      placeFactories(teamTwo, width - (width / 5), width);
      placeCities((int)(Math.sqrt(height * height + width * width) / 5), (int)(width / 5), (int)(width - (width / 5)));
      long end = System.currentTimeMillis();
      Logger.consolePrint("map building complete (" + (end - start) + " milliseconds)", "map");
      return validMap();
   }
   
   public boolean validMap() {
      HashSet<Coordinate> notVisited = new HashSet<Coordinate>();
      for(int r = 0; r < height; r++)
         for(int c = 0; c < width; c++)
            notVisited.add(new Coordinate(c, r));
      
      int firstX = 0;
      int firstY = 0;
      while(tiles[firstY][firstX].getType() == TILE_TYPE.RIVER) {
         Random rand = new Random();
         firstX = rand.nextInt(width);
         firstY = rand.nextInt(height);
      }
      
      LinkedList<Tile> queue = new LinkedList<Tile>();
      HashSet<Tile> set = new HashSet<Tile>();
      queue.offer(tiles[0][0]);
      while(queue.size() > 0) {
         Tile t = queue.poll();
         int[] yDirs = { 0, 0, 1, -1 };
         int[] xDirs = { 1, -1, 0, 0 };
         for(int i = 0; i < 4; i++)
            try {
               if(!set.contains(tiles[t.getY() + yDirs[i]][t.getX() + xDirs[i]])) {
                  if(!tiles[t.getY() + yDirs[i]][t.getX() + xDirs[i]].isOccupied())
                     if(tiles[t.getY() + yDirs[i]][t.getX() + xDirs[i]].isVehiclePassable()) {
                        // the previous if statement should be both veh and inf, but AI updates are needed before this can happen
                        set.add(tiles[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                        notVisited.remove(new Coordinate(t.getX() + xDirs[i], t.getY() + yDirs[i]));
                        queue.offer(tiles[t.getY() + yDirs[i]][t.getX() + xDirs[i]]);
                     }
               }
            }
            catch(Exception e) {
               // out of bounds
            }         
      }
      
      for(Coordinate c: notVisited)
         if(tiles[c.getY()][c.getX()].getType() != TILE_TYPE.RIVER && tiles[c.getY()][c.getX()].getType() != TILE_TYPE.MOUNTAIN)
            return false;
      
      return true;
   }
   
   public void initialTerrain() {
      Logger.consolePrint("creating initial terrain", "map");
      double numPlains = 0;
      double numForests = 0;
      double numMountains = 0;
      
      for(int row = 0; row < tiles.length; row++)
         for(int col = 0; col < tiles[row].length; col++) {
            double type = Math.random();
            if(type <= .75) {
               numPlains++;
               tiles[row][col] = new Plain(this, col, row);
            }
            else if(type > .75 && type <= .95) {
               numForests++;
               tiles[row][col] = new Forest(this, col, row);
            }
            else {
               numMountains++;
               tiles[row][col] = new Mountain(this, col, row);
            }
         }
      numPlains /= (double)(width * height);
      numForests /= (double)(width * height);
      numMountains /= (double)(width * height);
      Logger.consolePrint("creation completed", "map");
      Logger.consolePrint("plains: " + numPlains * 100 + "% forest: " + numForests * 100 + "% mountains: " + numMountains * 100 + "%", "map");
   }
   
   public void createHeightMap() {
      Logger.consolePrint("creating height map", "map");
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
      Logger.consolePrint("height map complete", "map");
      Logger.consolePrint("average height: " + heightAvg / (width * height), "map");
   }
   
   public void generateRivers() {
      Logger.consolePrint("creating rivers", "map");
      int maxLength = (int)(Math.sqrt(height * height + width * width) / 2);
      int minLength = maxLength - (maxLength / 2);
      int numRivers = maxLength / 5;
      int numGenerated = 0;
      int numAttempted = 0;
      int riverFraction = 3;
      int minRivers = (numRivers < riverFraction) ? 1 : (int)(numRivers / riverFraction);
      Logger.consolePrint("max river length = " + maxLength, "map");
      Logger.consolePrint("min river length = " + minLength, "map");
      Logger.consolePrint("num rivers wanted = " + numRivers, "map");
      Logger.consolePrint("min rivers needed = " + minRivers, "map");
      for(int x = 0; x < numRivers; x++) {
         Logger.consolePrint("generation attempt " + numAttempted + "...", "map");
         int genAttempts = 0;
         ArrayList<Coordinate> river = new ArrayList<Coordinate>();
         while(river.size() < 2 && genAttempts < 1024) {
            river = generateRiver(maxLength);
            genAttempts++;
            if(river.size() < 2 || river.size() < minLength)
               river.clear();
         }
         numAttempted++;
         if(genAttempts < 1024) {
            for(Coordinate c: river)
               tiles[c.getY()][c.getX()] = new River(this, c.getX(), c.getY(), null);
            setRiverTileDirections(river);
            int b = placeBridges(river);
            Logger.consolePrint("success (length " + river.size() + ", " + b + " bridges)", "map");
            numGenerated++;
         }
         else
            Logger.consolePrint("failure", "map");
         if(x == numRivers - 1 && numGenerated < minRivers) {
            minLength = Math.max(minLength - (int)((double)minLength * .1), 4);
            Logger.consolePrint("redefining min length to " + minLength, "map");
            x = 0;
         }
      }
      double rate = ((double)(numAttempted - numGenerated) / (double)numAttempted * 100.0);
      Logger.consolePrint(numGenerated + " rivers generated (" + rate + "% failure)", "map");
   }
   
   public ArrayList<Coordinate> generateRiver(int MLE) {
      ArrayList<Coordinate> riverPath = new ArrayList<Coordinate>();
      Random rand = new Random();
      // int hBound = 5;
      // int wBound = 5;
      int row = rand.nextInt(height); // rand.nextInt(height - (height / hBound) - (height /
                                      // hBound)) + (height / hBound);
      int col = rand.nextInt(width); // rand.nextInt(width - (width / wBound) - (width / wBound)) +
                                     // (width / wBound);
      int attempts = 0;
      while(heightMap[row][col] != 2 || tiles[row][col].getType() == TILE_TYPE.RIVER || numRiverNextTo(col, row, riverPath) != 0) {
         row = rand.nextInt(height);// rand.nextInt(height - (height / hBound) - (height / hBound))
                                    // + (height / hBound);
         col = rand.nextInt(width);// rand.nextInt(width - (width / wBound) - (width / wBound)) +
                                   // (width / wBound);
         attempts++;
         if(attempts > 64)
            break;
      }
      if(attempts > 64) {
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
      
      while(next == null && attempts < 16) {
         double dir = Math.random();
         try {
            if(dir < .30 && heightMap[row + 1][col] <= height && numRiverNextTo(col, row + 1, pathSoFar) <= 1 && !pathSoFar.contains(new Coordinate(col, row + 1))
                  && tiles[row + 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col, row + 1);
            else if(dir >= .95 && heightMap[row - 1][col] <= height && numRiverNextTo(col, row - 1, pathSoFar) <= 1 && !pathSoFar.contains(new Coordinate(col, row - 1))
                  && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col, row - 1);
            else if(dir >= .60 && dir < .90 && heightMap[row][col + 1] <= height && numRiverNextTo(col + 1, row, pathSoFar) <= 1 && !pathSoFar.contains(new Coordinate(col + 1, row))
                  && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
               next = new Coordinate(col + 1, row);
            else if(dir >= .30 && dir < .60 && heightMap[row][col - 1] <= height && numRiverNextTo(col - 1, row, pathSoFar) <= 1 && !pathSoFar.contains(new Coordinate(col - 1, row))
                  && tiles[row - 1][col].getType() != TILE_TYPE.RIVER)
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
         if(currTile.getY() > 0 && currTile.getY() < height - 1 && currTile.getX() > 0 && currTile.getX() < width - 1) {
            if(spotImage.equals(GraphicsManager.getTileTextures().get("river_horizontal.png"))) {
               Tile nextTile = tiles[path.get(spot + 1).getY()][path.get(spot + 1).getX()];
               Tile prevTile = tiles[path.get(spot - 1).getY()][path.get(spot - 1).getX()];
               if(nextTile.getType() != TILE_TYPE.BRIDGE && prevTile.getType() != TILE_TYPE.BRIDGE) {
                  tiles[path.get(spot).getY()][path.get(spot).getX()] = new Bridge(this, path.get(spot).getX(), path.get(spot).getY(), GraphicsManager.getTileTextures().get("bridge_vertical.png"));
                  numBridges++;
               }
            }
            else if(spotImage.equals(GraphicsManager.getTileTextures().get("river_vertical.png"))) {
               Tile nextTile = tiles[path.get(spot + 1).getY()][path.get(spot + 1).getX()];
               Tile prevTile = tiles[path.get(spot - 1).getY()][path.get(spot - 1).getX()];
               if(nextTile.getType() != TILE_TYPE.BRIDGE && prevTile.getType() != TILE_TYPE.BRIDGE) {
                  tiles[path.get(spot).getY()][path.get(spot).getX()] = new Bridge(this, path.get(spot).getX(), path.get(spot).getY(), GraphicsManager.getTileTextures().get("bridge_horizontal.png"));
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
            tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_down.png"));
         else
            tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_up.png"));
         ;
      }
      else {
         if(currX < nextX)
            tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_right.png"));
         else
            tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_left.png"));
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
               tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("river_vertical.png"));
            else
               tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("bridge_horizontal.png"));
         else if(prevY == currY && currY == nextY)
            if(tiles[currY][currX].getType() == TILE_TYPE.RIVER)
               tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("river_horizontal.png"));
            else
               tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("bridge_vertical.png"));
         else if(prevX < nextX) {
            if(prevY < nextY) {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_two.png"));
               if(prevY == currY)
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_three.png"));
            }
            else {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_four.png"));
               else
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_one.png"));
            }
            
         }
         else if(prevX > nextX) {
            if(prevY > nextY) {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_three.png"));
               if(prevY == currY)
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_two.png"));
            }
            else {
               if(prevX == currX)
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_one.png"));
               else
                  tiles[currY][currX].setTexture(GraphicsManager.getTileTextures().get("riverCorner_four.png"));
            }
         }
      }
      
      if(nextX == currX) {
         if(nextY < currY)
            tiles[nextY][nextX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_down.png"));
         else
            tiles[nextY][nextX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_up.png"));
      }
      else {
         if(nextX < currX)
            tiles[nextY][nextX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_right.png"));
         else
            tiles[nextY][nextX].setTexture(GraphicsManager.getTileTextures().get("riverEnd_left.png"));
      }
   }
   
   public void placeCities(int numCities, int min, int max) {
      Logger.consolePrint("placing " + numCities + " cities", "map");
      for(int x = 0; x < numCities; x++) {
         Random rand = new Random();
         int col = rand.nextInt(max - min) + min;
         int row = rand.nextInt(height - 1) + 1;
         int nc = 0;
         while(nc < 2) {
            while(tiles[row][col].getType() != TILE_TYPE.PLAIN) {
               col = rand.nextInt(max - min) + min;
               row = rand.nextInt(height - 1) + 1;
            }
            tiles[row][col] = new City(this, col, row, null, GraphicsManager.getTileTextures().get("cityGrey.png"));
            nc++;
         }
      }
      
   }
   
   public void placeFactories(Team team, int min, int max) {
      Logger.consolePrint("placing factories for " + team.getName(), "map");
      for(int col = min; col < max; col += 2) {
         int row = (int)(Math.random() * height);
         while(tiles[row][col].getType() == TILE_TYPE.RIVER || tiles[row][col].getType() == TILE_TYPE.MOUNTAIN || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
            row = (int)(Math.random() * height);
         }
         if(team.equals(teamOne))
            tiles[row][col] = new Factory(this, col, row, team, GraphicsManager.getTileTextures().get("factoryRed.png"));
         else
            tiles[row][col] = new Factory(this, col, row, team, GraphicsManager.getTileTextures().get("factoryBlue.png"));
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
      Logger.consolePrint("Building army for: " + team.getName(), "map");
      int col = minCol;
      for(int x = 0; x < team.getTeamSize(); x++) {
         int row = (int)(Math.random() * height);
         while(tiles[row][col].isOccupied() || tiles[row][col].getType() == TILE_TYPE.MOUNTAIN || tiles[row][col].getType() == TILE_TYPE.RIVER || tiles[row][col].getType() == TILE_TYPE.FACTORY
               || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
            row = (int)(Math.random() * height);
         }
         if(x == team.getTeamSize() - 1) {
            if(team.equals(teamOne))
               team.createUnit(tiles[row][col], UNIT_TYPE.INFANTRY, GraphicsManager.getTileTextures().get("infantryRed_left"), GraphicsManager.getTileTextures().get("infantryRed_right"));
            else if(team.equals(teamOne))
               team.createUnit(tiles[row][col], UNIT_TYPE.INFANTRY, GraphicsManager.getTileTextures().get("infantryBlue_left"), GraphicsManager.getTileTextures().get("infantryBlue_right"));
         }
         else {
            UNIT_TYPE rand = randUnitType();
            Image[] textures = GraphicsManager.typetoImage(rand, team.getTeamNumber());
            team.createUnit(tiles[row][col], rand, textures[0], textures[1]);
         }
         if(team.equals(teamTwo))
            team.getUnits().get(x).setDir(DIRECTION.LEFT);
         if((x + 1) % (double)(team.getTeamSize() / (width / 5)) == 0)
            col++;
         if(col >= width)
            col--;
      }
      Logger.consolePrint("Done building army for: " + team.getName(), "map");
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
   
   public int getHeight() {
      return height;
   }
   
   public void setHeight(int height) {
      this.height = height;
   }
   
   public int getWidth() {
      return width;
   }
   
   public void setWidth(int width) {
      this.width = width;
   }
   
}
