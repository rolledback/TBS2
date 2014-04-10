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
import com.rolledback.units.Unit.UNIT_TYPE;

/**
 * The world class contains all information and objects concerning the actual tile/terrain of the
 * Game class. The primary object of the World class is the Tiles matrix. This matrix can either be
 * randomly generated via the buildMap function, or is loaded from a .map file, as passed in by the
 * fileToLoad variable in the primary world constructor.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class World {
   private Team teamOne, teamTwo;
   private Tile tiles[][];
   private int heightMap[][];
   private int width, height;
   
   /**
    * Primary constructor. Called by the Game.java constructor
    * 
    * @param w the width of the world (tiles)
    * @param h the height of the world (tiles)
    * @param a the first team of the game
    * @param b the second team of the game
    * @param fileToLoad if the user chose to load a map, this will be the file's name, empty string
    *           if not loading a file.
    */
   public World(int w, int h, Team a, Team b, String fileToLoad) {
      width = w;
      height = h;
      tiles = new Tile[h][w];
      heightMap = new int[h][w];
      teamOne = a;
      teamTwo = b;
      if(fileToLoad.equals("")) {
         boolean accept = false;
         while(!accept) {
            accept = buildMap();
            if(!accept) {
               Logger.consolePrint("Map rejected.", "map");
               resetMap();
            }
         }
      }
      else
         tiles = (Tile[][])Cartographer.readMapFile(fileToLoad, tiles, this)[1];
   }
   
   /**
    * Dummy constructor. Used by the map editor. Only initializes the teams.
    */
   public World() {
      teamOne = new Team("", 0, 0, 1);
      teamTwo = new Team("", 0, 0, 2);
   }
   
   /**
    * Secondary dummy constructor. Also used by the map editor. Used to generate a random map.
    * 
    * @param w
    * @param h
    * @param rivers
    */
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
   
   /**
    * Prints out the map, with tiles represented by their mapChar variable.
    */
   public void printMap() {
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            System.out.print(tiles[row][col].getMapChar() + " ");
         }
         System.out.println();
      }
   }
   
   /**
    * Prints out all the units on the map, uppercase letters belong to teamOne and lowercase belong
    * to teamTwo.
    */
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
   
   /**
    * Prints out all the units on the map, uppercase letters belong to teamOne and lowercase belong
    * to teamTwo. Also places an X at the given x, y location.
    */
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
   
   /**
    * Primary function fall for random map generation. First creates an initial set of basic
    * terrain. Then a height map based on that terrain. After that rivers, factories, and cities are
    * placed in that order.
    * 
    * @return whether or not the map is considered valid, based on the result of a call to validMap
    */
   public boolean buildMap() {
      long start = System.currentTimeMillis();
      Logger.consolePrint("building map", "map");
      Logger.consolePrint("dimensions: " + width + ", " + height, "map");
      initialTerrain();
      createHeightMap();
      generateRivers();
      if(!placeFactories(teamOne, 0, width / 5))
         return false;
      if(!placeFactories(teamTwo, width - (width / 5), width))
         return false;
      if(!placeCities((int)(Math.sqrt(height * height + width * width) / 5), (int)(width / 5), (int)(width - (width / 5))))
         return false;
      long end = System.currentTimeMillis();
      Logger.consolePrint("map building complete (" + (end - start) + " milliseconds)", "map");
      return validMap();
   }
   
   /**
    * Resets all variables changed in the buildMap function. This includes resetting the tiles and
    * height map matrix, and clearing each team's factory list.
    */
   public void resetMap() {
      tiles = new Tile[height][width];
      heightMap = new int[height][width];
      teamOne.getFactories().clear();
      teamTwo.getFactories().clear();
   }
   
   /**
    * Performs a breadth first search from the first non-river or non-mountain tile possible. Checks
    * to make sure that all non-river & non-mountain tiles are then reachable from that tile. If all
    * are not, then there the map is considered invalid.
    * 
    * @return false if the map is not valid as defined by the method description.
    */
   public boolean validMap() {
      int factoriesFound = 0;
      HashSet<Coordinate> notVisited = new HashSet<Coordinate>();
      for(int r = 0; r < height; r++)
         for(int c = 0; c < width; c++)
            notVisited.add(new Coordinate(c, r));
      
      int firstX = 0;
      int firstY = 0;
      while(tiles[firstY][firstX].getType() == TILE_TYPE.RIVER || tiles[firstY][firstX].getType() == TILE_TYPE.MOUNTAIN) {
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
                        // the previous if statement should be both veh and inf, but AI updates are
                        // needed before this can happen
                        if(tiles[t.getY() + yDirs[i]][t.getX() + xDirs[i]].getType() == TILE_TYPE.FACTORY)
                           factoriesFound++;
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
         if(tiles[c.getY()][c.getX()].getType() != TILE_TYPE.RIVER && tiles[c.getY()][c.getX()].getType() != TILE_TYPE.MOUNTAIN) {
            if(factoriesFound != teamOne.getFactories().size() + teamTwo.getFactories().size())
               return false;
            else
               return true;
         }
      
      return true;
   }
   
   /**
    * Randomly generates the initial terrain. Initial terrain being only plains, forests and
    * mountains.
    */
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
   
   /**
    * Creates the height map for a given map. Mountains are given a height of 3, while all adjacent
    * tiles are given a height of 2, and then those adjacent to those a height of 1. The height map
    * is used in the river generation process.
    */
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
   
   /**
    * Top level function call for the generation of rivers. Controls max river length, minimum river
    * length, and the minimum number of rivers needed.
    */
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
   
   /**
    * Generates a singular river. Called by the generateRivers function.
    * 
    * @param MLE the max length estimate paramater. The river generated will have a max length in
    *           the range of MLE / 2 to MLE.
    * @return an ArrayList containing the coordinates that make up the river. The tiles at these
    *         coordinate locations are not yet rivers though.
    */
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
   
   /**
    * Returns a random spot adjacent to the given row and col. The direction for the next spot is
    * biased towards going to the left and downwards.
    * 
    * @param col x location of the current river spot.
    * @param row y location of the current river spot.
    * @param pathSoFar the path of the river up until this point.
    * @return the next spot in the path, if null is returned then none of the 4 adjacent spots were
    *         valid.
    */
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
   
   /**
    * Calculates the number of type river tiles next to the given spot.
    * 
    * @param col the x location of the spot being inspected
    * @param row the y location of the spot being inspected
    * @param pathSoFar the path so far of the current river
    * @return the number of type river tiles next to the spot
    */
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
   
   /**
    * Places bridges at random interior non-corner tiles on the river.
    * 
    * @param path all the coordinates of the river.
    * @return number of bridges placed on the river.
    */
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
   
   /**
    * Determines what texture each spot in the river should have based on the relative locations of
    * the next and previous river spots.
    * 
    * @param riverPath the path of the river.
    */
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
   
   /**
    * Randomly places cities on the map.
    * 
    * @param numCities number of cities to place.
    * @param min the lowest numbered column the cities can be placed on.
    * @param max the highest numbered column the cities can be placed on.
    */
   public boolean placeCities(int numCities, int min, int max) {
      Logger.consolePrint("placing " + numCities + " cities", "map");
      for(int x = 0; x < numCities; x++) {
         Random rand = new Random();
         int col = rand.nextInt(max - min) + min;
         int row = rand.nextInt(height - 1) + 1;
         int nc = 0;
         int attempts = width * height;
         while(nc < 2) {
            while(tiles[row][col].getType() == TILE_TYPE.RIVER || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
               col = rand.nextInt(max - min) + min;
               row = rand.nextInt(height - 1) + 1;
               attempts--;
               if(attempts == 0)
                  return false;
            }
            tiles[row][col] = new City(this, col, row, null, GraphicsManager.getTileTextures().get("cityGrey.png"));
            nc++;
         }
      }
      return true;
   }
   
   /**
    * Randomly places factories belonging to the given team on the map, two per column.
    * 
    * @param min the lowest numbered column the cities can be placed on.
    * @param max the highest numbered column the cities can be placed on.
    * @return whether or not the function was able to place all of the factories
    */
   public boolean placeFactories(Team team, int min, int max) {
      Logger.consolePrint("placing factories for " + team.getName(), "map");
      for(int col = min; col < max; col += 2) {
         int row = (int)(Math.random() * height);
         int attempts = width * height;
         while(tiles[row][col].getType() == TILE_TYPE.RIVER || tiles[row][col].getType() == TILE_TYPE.BRIDGE) {
            row = (int)(Math.random() * height);
            attempts--;
            if(attempts == 0)
               return false;
         }
         if(team.equals(teamOne))
            tiles[row][col] = new Factory(this, col, row, team, GraphicsManager.getTileTextures().get("factoryRed.png"));
         else
            tiles[row][col] = new Factory(this, col, row, team, GraphicsManager.getTileTextures().get("factoryBlue.png"));
         team.getFactories().add((Factory)tiles[row][col]);
      }
      return true;
   }
   
   /**
    * Counts the number of mountains next to the given spot.
    * 
    * @param col the x location of the current spot.
    * @param row the y location of the current spot.
    * @return the number of mountains next to the current spot.
    */
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
   
   /**
    * Randomly generates and places an army on the map for the given team.
    * 
    * @param team team to which the units will be long to.
    * @param minCol the lowest numbered column the units can be placed on.
    * @param maxCol the highest numbered column the units can be placed on.
    */
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
               team.createUnit(tiles[row][col], UNIT_TYPE.INFANTRY, GraphicsManager.getTileTextures().get("infantryRed"));
            else if(team.equals(teamOne))
               team.createUnit(tiles[row][col], UNIT_TYPE.INFANTRY, GraphicsManager.getTileTextures().get("infantryBlue"));
         }
         else {
            UNIT_TYPE rand = randUnitType();
            Image texture = GraphicsManager.typetoImage(rand, team.getTeamNumber());
            team.createUnit(tiles[row][col], rand, texture);
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
   
   /**
    * Destroys the given unit.
    * 
    * @param u the unit to be destroyed.
    */
   public void destroyUnit(Unit u) {
      Team owner = u.getOwner();
      owner.removeUnit(u);
      u.getCurrentTile().setOccupied(false);
      u.getCurrentTile().setOccupiedBy(null);
   }
   
   /**
    * Chooses a random unit type and returns it.
    * 
    * @return a unit type.
    */
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
