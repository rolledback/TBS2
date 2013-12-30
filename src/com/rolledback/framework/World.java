package com.rolledback.framework;

import java.util.Arrays;
import java.util.Scanner;

import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Forest;
import com.rolledback.terrain.Mountain;
import com.rolledback.terrain.Plain;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class World {
   private Team teamOne, teamTwo;
   private Tile tiles[][];
   private int width, height;
   
   public World(int w, int h, Team a, Team b) {
      width = w;
      height = h;
      tiles = new Tile[h][w];
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
   
   public int[][] calcMoveSpots(Unit unit) {
      int[][] valid = new int[height][width];
      for(int x = 0; x < valid.length; x++)
         Arrays.fill(valid[x], -1);
      int adHocRange = unit.getMoveRange() + unit.getCurrentTile().getEffect().moveBonus;
      if(adHocRange <= 0)
         adHocRange = 1;
      unit.getCurrentTile().setOccupied(false);
      calcMoveSpotsHelper(unit, valid, unit.getX(), unit.getY(), adHocRange + 1);
      unit.getCurrentTile().setOccupied(true);
      return valid;
   }
   
   public void calcMoveSpotsHelper(Unit unit, int valid[][], int x, int y, int range) {
      if(x < 0 || x >= width || y < 0 || y >= height)
         return;
      else if(valid[y][x] == 0)
         return;
      else if(range <= 0) {
         if(tiles[y][x].isOccupied() && !unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner()))
            valid[y][x] = 2;
         return;
      }
      else if(tiles[y][x].isOccupied()) {
         if(!unit.getOwner().equals(tiles[y][x].getOccupiedBy().getOwner()))
            valid[y][x] = 2;
         else
            valid[y][x] = 0;
      }
      else if(!tiles[y][x].isVehiclePassable() && unit.getClassification() == UNIT_CLASS.VEHICLE)
         valid[y][x] = 0;
      else if(!tiles[y][x].isInfantryPassable() && unit.getClassification() == UNIT_CLASS.INFANTRY)
         valid[y][x] = 0;
      else
         valid[y][x] = 1;
      range--;
      if(valid[y][x] == 1) {
         calcMoveSpotsHelper(unit, valid, x + 1, y, range);
         calcMoveSpotsHelper(unit, valid, x - 1, y, range);
         calcMoveSpotsHelper(unit, valid, x, y + 1, range);
         calcMoveSpotsHelper(unit, valid, x, y - 1, range);
      }
      else
         return;
   }
   
   public void buildMap() {
      for(int row = 0; row < tiles.length; row++) {
         for(int col = 0; col < tiles[row].length; col++) {
            double type = Math.random();
            if(type <= 70)
               tiles[row][col] = new Plain(this, col, row);
            else if(type > .70 && type <= .95)
               tiles[row][col] = new Forest(this, col, row);
            else
               tiles[row][col] = new Mountain(this, col, row);
         }
      }
      placeFactories(teamOne, 0, width / 5);
      placeFactories(teamTwo, width - (width / 5), width);     
   }
   
   public void placeFactories(Team team, int min, int max) {
      for(int col = min; col < max; col++) {
         int row = (int)(Math.random() * height);
         Tile spot = tiles[row][col];
         while(spot.getType() == TILE_TYPE.MOUNTAIN) {
            row = (int)(Math.random() * height);
            spot = tiles[row][col];
         }
         tiles[row][col] = new Factory(this, col, row, team);
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
      for(int x = 0; x < team.teamSize; x++) {
         int row = (int)(Math.random() * height);
         while(tiles[row][col].isOccupied() || tiles[row][col].getType() == TILE_TYPE.MOUNTAIN) {
            row = (int)(Math.random() * height);
         }
         team.createUnit(tiles[row][col], randUnitType());
         if((x + 1) % (team.teamSize / (width / 5)) == 0)
            col++;
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
}
