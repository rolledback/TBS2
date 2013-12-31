package com.rolledback.terrain;

import com.rolledback.framework.World;
import com.rolledback.units.*;

public class Tile {
   
   public enum TILE_TYPE {
      PLAIN, FOREST, MOUNTAIN, FACTORY
   }
   
   protected boolean infantryPassable, vehiclePassable;
   boolean occupied;
   int x, y;
   protected TILE_TYPE type;
   protected TerrainEffect effect;
   private char mapChar;
   protected World world;
   Unit occupiedBy;
   
   public Tile(World w, int x, int y, TerrainEffect e, char m) {
      this.x = x;
      this.y = y;
      effect = e;
      infantryPassable = true;
      vehiclePassable = true;
      occupied = false;
      mapChar = m;
      world = w;
      occupiedBy = null;
   }
   
   public void setOccupiedBy(Unit u) {
      occupiedBy = u;
   }
   
   public Unit getOccupiedBy() {
      return occupiedBy;
   }
   
   public String toString() {
      return "Type: " + type + " x: " + x + " y: " + y + " " + getEffect().toString() + " Occupied: " + occupied;
   }
   
   public TerrainEffect getEffect() {
      return effect;
   }
   
   public void setEffect(TerrainEffect effect) {
      this.effect = effect;
   }
   
   public int getX() {
      return x;
   }
   
   public int getY() {
      return y;
   }
   
   public boolean isOccupied() {
      return occupied;
   }
   
   public void setOccupied(boolean occupied) {
      this.occupied = occupied;
   }
   
   public boolean isInfantryPassable() {
      return infantryPassable;
   }
   
   public void setInfantryPassable(boolean infantryPassable) {
      this.infantryPassable = infantryPassable;
   }
   
   public char getMapChar() {
      return mapChar;
   }
   
   public void setMapChar(char mapChar) {
      this.mapChar = mapChar;
   }
   
   public boolean isVehiclePassable() {
      return vehiclePassable;
   }
   
   public void setVehiclePassable(boolean vehiclePassable) {
      this.vehiclePassable = vehiclePassable;
   }
   
   public TILE_TYPE getType() {
      return type;
   }
   
}
