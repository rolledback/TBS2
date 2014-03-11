package com.rolledback.mapping;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.Logger;
import com.rolledback.framework.World;
import com.rolledback.terrain.Bridge;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Forest;
import com.rolledback.terrain.Mountain;
import com.rolledback.terrain.Plain;
import com.rolledback.terrain.River;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;

public class Cartographer {
   
   public static Object[] readMapFile(String fileName, Tile[][] tiles, World w, GraphicsManager manager) {
      BufferedInputStream mapReader;
      try {
         Logger.consolePrint("Opening: " + fileName, "cartographer");
         mapReader = new BufferedInputStream(new FileInputStream(fileName));
         byte[] map = new byte[(int)new File(fileName).length()];
         mapReader.read(map);
         mapReader.close();
         Logger.consolePrint("Checking for magic number.", "cartographer");
         if(map[0] != 0x6d) {
            Logger.consolePrint("Number not found. Invalid file.", "cartographer");
            Object[] ret = new Object[1];
            ret[0] = false;
            return ret;
         }
         Logger.consolePrint("File checks out. Length of: " + map.length, "cartographer");
         int width = map[2] ^ (map[3] << 8);
         int height = map[4] ^ (map[5] << 8);
         Logger.consolePrint("File gives dim of: (" + width + ", " + height + ")", "cartographer");
         tiles = new Tile[height][width];
         
         Logger.consolePrint("Reading tile info...", "cartographer");
         for(int row = 0; row < height; row++)
            for(int col = 0; col < width; col++)
               tiles[row][col] = byteToTile(w, manager, (byte)map[6 + (row * width) + col], col, row);
      }
      catch(Exception e) {
         Logger.consolePrint("Error opening file: " + e.toString(), "cartographer");
         Object[] ret = new Object[1];
         ret[0] = false;
         return ret;
      }
      Logger.consolePrint("Map has been loaded.", "cartographer");
      Object[] ret = new Object[2];
      ret[0] = true;
      ret[1] = tiles;
      return ret;
   }
   
   public static boolean createMapFile(String fileName, Tile[][] tiles, int tileSize, GraphicsManager manager) {
      Logger.consolePrint("Creating file: " + fileName, "cartographer");
      int height = tiles.length;
      int width = tiles[0].length;
      
      byte[] map = new byte[6 + (tiles.length * tiles[0].length)];
      
      map[0] = 0x6d;
      
      map[1] = (byte)tileSize;
      
      map[2] = (byte)(width & 0xff);
      map[3] = (byte)((width >> 8) & 0xff);
      
      map[4] = (byte)(height & 0xff);
      map[5] = (byte)((height >> 8) & 0xff);
      
      for(int row = 0; row < height; row++)
         for(int col = 0; col < width; col++)
            map[6 + (row * width) + col] = tileToByte(manager, tiles[row][col]);
      
      Logger.consolePrint("Initial data converted to byte format.", "cartographer");
      
      BufferedOutputStream mapWriter;
      try {
         
         Logger.consolePrint("Writing bytes to file.", "cartographer");
         mapWriter = new BufferedOutputStream(new FileOutputStream(fileName));
         mapWriter.write(map);
         mapWriter.close();
      }
      catch(Exception e) {
         Logger.consolePrint("Error creating file: " + e.toString(), "cartographer");
         return false;
      }
      
      Logger.consolePrint("Map file has been created.", "cartographer");
      return true;
   }
   
   public static byte tileToByte(GraphicsManager manager, Tile tile) {
      if(tile.getType() == TILE_TYPE.BRIDGE) {
         if(tile.getTexture().equals(manager.tileTextures.get("bridge_horizontal.png")))
            return (byte)4;
         else
            return (byte)20;
      }
      if(tile.getType() == TILE_TYPE.RIVER) {
         if(tile.getTexture().equals(manager.tileTextures.get("river_horizontal.png")))
            return (byte)3;
         if(tile.getTexture().equals(manager.tileTextures.get("river_vertical.png")))
            return (byte)19;
         if(tile.getTexture().equals(manager.tileTextures.get("riverEnd_up.png")))
            return (byte)35;
         if(tile.getTexture().equals(manager.tileTextures.get("riverEnd_right.png")))
            return (byte)51;
         if(tile.getTexture().equals(manager.tileTextures.get("riverEnd_down.png")))
            return (byte)67;
         if(tile.getTexture().equals(manager.tileTextures.get("riverEnd_left.png")))
            return (byte)83;
         if(tile.getTexture().equals(manager.tileTextures.get("riverCorner_one.png")))
            return (byte)99;
         if(tile.getTexture().equals(manager.tileTextures.get("riverCorner_two.png")))
            return (byte)115;
         if(tile.getTexture().equals(manager.tileTextures.get("riverCorner_three.png")))
            return (byte)131;
         if(tile.getTexture().equals(manager.tileTextures.get("riverCorner_four.png")))
            return (byte)147;
         if(tile.getTexture().equals(manager.tileTextures.get("river_intersection.png")))
            return (byte)163;
         if(tile.getTexture().equals(manager.tileTextures.get("river_tSection_one.png")))
            return (byte)179;
         if(tile.getTexture().equals(manager.tileTextures.get("river_tSection_two.png")))
            return (byte)195;
         if(tile.getTexture().equals(manager.tileTextures.get("river_tSection_three.png")))
            return (byte)211;
         else
            return (byte)227;
         
      }
      if(tile.getType() == TILE_TYPE.PLAIN) {
         return (byte)0;
      }
      if(tile.getType() == TILE_TYPE.FOREST) {
         return (byte)1;
      }
      if(tile.getType() == TILE_TYPE.MOUNTAIN) {
         return (byte)2;
      }
      if(tile.getType() == TILE_TYPE.CITY) {
         if(tile.getTexture().equals(manager.tileTextures.get("cityGrey.png")))
            return (byte)5;
         if(tile.getTexture().equals(manager.tileTextures.get("cityRed.png")))
            return (byte)21;
         else
            return (byte)37;
      }
      if(tile.getType() == TILE_TYPE.FACTORY) {
         if(tile.getTexture().equals(manager.tileTextures.get("factoryGrey.png")))
            return (byte)6;
         if(tile.getTexture().equals(manager.tileTextures.get("factoryRed.png")))
            return (byte)22;
         else
            return (byte)28;
      }
      return 0b1111111;
   }
   
   public static Tile byteToTile(World w, GraphicsManager manager, byte value, int col, int row) {
      switch(value) {
         case (byte)0:
            return new Plain(w, col, row, manager.tileTextures.get("grass.png"));
            
         case (byte)1:
            return new Forest(w, col, row, manager.tileTextures.get("forest.png"));
            
         case (byte)2:
            return new Mountain(w, col, row, manager.tileTextures.get("mountain.png"));
            
         case (byte)3:
            return new River(w, col, row, manager.tileTextures.get("river_horizontal.png"));
            
         case (byte)19:
            return new River(w, col, row, manager.tileTextures.get("river_vertical.png"));
            
         case (byte)35:
            return new River(w, col, row, manager.tileTextures.get("riverEnd_up.png"));
            
         case (byte)51:
            return new River(w, col, row, manager.tileTextures.get("riverEnd_right.png"));
            
         case (byte)67:
            return new River(w, col, row, manager.tileTextures.get("riverEnd_down.png"));
            
         case (byte)83:
            return new River(w, col, row, manager.tileTextures.get("riverEnd_left.png"));
            
         case (byte)99:
            return new River(w, col, row, manager.tileTextures.get("riverCorner_one.png"));
            
         case (byte)115:
            return new River(w, col, row, manager.tileTextures.get("riverCorner_two.png"));
            
         case (byte)131:
            return new River(w, col, row, manager.tileTextures.get("riverCorner_three.png"));
            
         case (byte)147:
            return new River(w, col, row, manager.tileTextures.get("riverCorner_four.png"));
            
         case (byte)163:
            return new River(w, col, row, manager.tileTextures.get("river_intersection.png"));
            
         case (byte)179:
            return new River(w, col, row, manager.tileTextures.get("river_tSection_one.png"));
            
         case (byte)195:
            return new River(w, col, row, manager.tileTextures.get("river_tSection_two.png"));
            
         case (byte)211:
            return new River(w, col, row, manager.tileTextures.get("river_tSection_three.png"));
            
         case (byte)227:
            return new River(w, col, row, manager.tileTextures.get("river_tSection_four.png"));
            
         case (byte)4:
            return new Bridge(w, col, row, manager.tileTextures.get("bridge_horizontal.png"));
            
         case (byte)20:
            return new Bridge(w, col, row, manager.tileTextures.get("bridge_vertical.png"));
            
         case (byte)5:
            return new City(w, col, row, null, manager.tileTextures.get("cityGrey.png"));
            
         case (byte)21:
            w.getTeamOne().getCities().add(new City(w, col, row, w.getTeamOne(), manager.tileTextures.get("cityRed.png")));
            return w.getTeamOne().getCities().get(w.getTeamOne().getCities().size() - 1);
            
         case (byte)37:
            w.getTeamTwo().getCities().add(new City(w, col, row, w.getTeamTwo(), manager.tileTextures.get("cityBlue.png")));
            return w.getTeamTwo().getCities().get(w.getTeamTwo().getCities().size() - 1);
            
         case (byte)6:
            return new Factory(w, col, row, null, manager.tileTextures.get("factoryGrey.png"));
            
         case (byte)22:
            w.getTeamOne().getFactories().add(new Factory(w, col, row, w.getTeamOne(), manager.tileTextures.get("factoryRed.png")));
            return w.getTeamOne().getFactories().get(w.getTeamOne().getFactories().size() - 1);
            
         case (byte)28:
            w.getTeamTwo().getFactories().add(new Factory(w, col, row, w.getTeamTwo(), manager.tileTextures.get("factoryBlue.png")));
            return w.getTeamTwo().getFactories().get(w.getTeamTwo().getFactories().size() - 1);
      }
      return null;
   }
}
