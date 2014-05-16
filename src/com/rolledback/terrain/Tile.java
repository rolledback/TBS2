package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;
import com.rolledback.units.Unit;

/**
 * Super class for all tiles. Only contains a constructor and a variety of getters and setters. No
 * actual functions are performed by tiles besides these.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Tile {
   
   public enum TILE_TYPE {
      PLAIN,
      FOREST,
      MOUNTAIN,
      FACTORY,
      RIVER,
      BRIDGE,
      CITY
   }
   
   protected boolean infantryPassable, vehiclePassable;
   boolean occupied;
   protected int x, y;
   protected TILE_TYPE type;
   protected TerrainEffect effect;
   private char mapChar;
   protected World world;
   private Unit occupiedBy;
   protected Image texture;
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in.
    * @param x x position of the tile in the world's tile matrix.
    * @param y y position of the tile in the world's tile matrix.
    * @param e terrain effect associated with the tile.
    * @param m character representation of the tile.
    */
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
      texture = null;
   }
   
   public void removeUnit() {
      occupied = false;
      occupiedBy = null;
   }
   
   public void addUnit(Unit u) {
      occupied = true;
      occupiedBy = u;
   }
   
   public void setOccupiedBy(Unit u) {
      occupiedBy = u;
   }
   
   public Unit getOccupiedBy() {
      return occupiedBy;
   }
   
   public String toString() {
      return "Type: " + type + " x: " + x + " y: " + y + " " + getEffect().toString() + " Occupied: " + occupied + " by " + occupiedBy;
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
   
   public Image getTexture() {
      return texture;
   }
   
   public void setTexture(Image texture) {
      this.texture = texture;
   }
   
   public World getWorld() {
      return world;
   }
   
   public void setWorld(World world) {
      this.world = world;
   }
}
