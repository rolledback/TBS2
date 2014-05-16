package com.rolledback.units;

import java.awt.Image;
import java.util.HashSet;
import java.util.Random;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.teams.technology.Technology;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;

/**
 * Super class for all units. Contains all functions nescesarry for a unit to interact with the
 * world/game.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Unit {
   
   public enum UNIT_CLASS {
      ALL,
      VEHICLE,
      INFANTRY
   }
   
   public enum UNIT_TYPE {
      ALL("All"),
      INFANTRY("Infantry"),
      RPG("RPG Team"),
      TANK("Tank"),
      TANK_DEST("Tank Destroyer");
      
      private String name;
      
      UNIT_TYPE(String n) {
         name = n;
      }
      
      public String toString() {
         return name;
      }
      
      public static UNIT_TYPE stringToType(String s) {
         switch(s) {
            case "Tank":
               return TANK;
            case "Tank Destroyer":
               return TANK_DEST;
            case "Infantry":
               return INFANTRY;
            case "RPG Team":
               return RPG;
            default:
               return null;
         }
      }
   }
   
   public enum DIRECTION {
      LEFT,
      RIGHT
   }
   
   private HashSet<Coordinate> moveSet;
   private HashSet<Coordinate> attackSet;
   private HashSet<Coordinate> captureSet;
   protected int defense;
   protected int attackRange;
   protected int moveRange;
   protected int x, y;
   private int health;
   private int maxHealth;
   private boolean alive;
   private boolean moved;
   private boolean attacked;
   protected Tile currentTile;
   protected UNIT_CLASS classification;
   protected UNIT_TYPE type;
   
   protected Image texture;
   
   private DIRECTION dir;
   private Team owner;
   
   /**
    * Constructor.
    * 
    * @param x starting x position of the unit.
    * @param y starting y position of the unit.
    * @param t tile that the unit is to be placed on.
    * @param o team that the unit belongs to.
    */
   public Unit(int x, int y, Tile t, Team o) {
      defense = 0;
      attackRange = 0;
      moveRange = 0;
      this.x = x;
      this.y = y;
      setDir(DIRECTION.RIGHT);
      health = 100;
      maxHealth = 100;
      alive = true;
      moved = false;
      currentTile = t;
      classification = null;
      owner = o;
      moveSet = new HashSet<Coordinate>();
      attackSet = new HashSet<Coordinate>();
      captureSet = new HashSet<Coordinate>();
   }
   
   /**
    * Sets up the necessary parameters and other actions that need to be done before calling a
    * separate private helper function, calcMoveSpots helper. Although this function is void, upon
    * it's completion the unit's moveSet, captureSet, and attackSet will contain coordinates that
    * the unit can move/capture/attack on.
    * 
    * @param atkOnly whether or not the helper function should only examine the tiles adjacent to
    *           the unit for enemy units. In simpler terms, true when a unit has moved but not yet
    *           attacked.
    */
   public void calcMoveSpots(boolean atkOnly) {
      World world = this.currentTile.getWorld();
      int techBonus = 0;
      for(Technology t: owner.getResearchedTechs()) {
         if(t.getUnitClass() == this.classification || t.getUnitClass() == UNIT_CLASS.ALL)
            techBonus += t.getMoveValue();
         if(t.getTileType() == currentTile.getType())
            techBonus += t.getMoveValue();
      }
      int adHocRange = (atkOnly) ? 1 : moveRange + techBonus + 1;
      if(adHocRange <= 0 && !atkOnly)
         adHocRange = 1;
      currentTile.setOccupied(false);
      attackSet.clear();
      moveSet.clear();
      captureSet.clear();
      calcMoveSpotsHelper(world, x, y, adHocRange, false, true, atkOnly);
      currentTile.setOccupied(true);
      moveSet.remove(new Coordinate(x, y));
   }
   
   /**
    * Called by calcMoveSpots. Performs a DFS around the unit finding all spots that the unit can
    * move to. Each move deducts one from range. Units are allowed to move through friendly units.
    * 
    * @param world the world that the unit will be traversing.
    * @param x the current x value of the DFS.
    * @param y the current y value of the DFS.
    * @param range the remaining move range for the unit.
    * @param movedThrough whether or not the unit just came through a friendly unit.
    */
   private void calcMoveSpotsHelper(World world, int x, int y, int range, boolean movedThrough, boolean firstMove, boolean atkOnly) {
      Coordinate thisCoord = new Coordinate(x, y);
      Tile tiles[][] = world.getTiles();
      int height = world.getHeight();
      int width = world.getWidth();
      if(x < 0 || x >= width || y < 0 || y >= height)
         return;
      if(range <= 0) {
         if(canAttack(tiles[y][x]) && !movedThrough)
            attackSet.add(thisCoord);
         return;
      }
      else if(tiles[y][x].isOccupied()) {
         if(canAttack(tiles[y][x]) && !movedThrough)
            attackSet.add(thisCoord);
      }
      else if(!canTraverse(tiles[y][x]))
         return;
      else if(canCapture(tiles[y][x]))
         captureSet.add(thisCoord);
      else if(canTraverse(tiles[y][x])) {
         moveSet.add(thisCoord);
      }
      range -= tiles[y][x].getEffect().getMoveCost();
      if(range <= 0 && firstMove && !atkOnly)
         range = 1;
      boolean mT = tiles[y][x].isOccupied() && owner.equals(tiles[y][x].getOccupiedBy().getOwner());
      if(captureSet.contains(thisCoord) || moveSet.contains(thisCoord) || mT) {
         calcMoveSpotsHelper(world, x + 1, y, range, mT, false, atkOnly);
         calcMoveSpotsHelper(world, x - 1, y, range, mT, false, atkOnly);
         calcMoveSpotsHelper(world, x, y + 1, range, mT, false, atkOnly);
         calcMoveSpotsHelper(world, x, y - 1, range, mT, false, atkOnly);
      }
      else
         return;
   }
   
   /**
    * Determines if the unit can traverse the given tile.
    * 
    * @param tile tile to be tested against.
    * @return whether or not the unit can traverse the tile.
    */
   public boolean canTraverse(Tile tile) {
      if(tile.getType() == TILE_TYPE.RIVER)
         return false;
      if(tile.getType() == TILE_TYPE.MOUNTAIN && classification != UNIT_CLASS.INFANTRY)
         return false;
      return true;
   }
   
   /**
    * Determines if the unit can capture the given tile. Will return true even if the tile is
    * occupied.
    * 
    * @param tile tile to be tested against.
    * @return whether or not the unit can capture the tile.
    */
   public boolean canCapture(Tile tile) {
      if(!(tile instanceof CapturableTile))
         return false;
      if(classification != UNIT_CLASS.INFANTRY)
         return false;
      return ((CapturableTile)tile).getOwner() == null || !owner.equals(((CapturableTile)tile).getOwner());
   }
   
   /**
    * Determines if the unit can attack the unit on the given tile. Checks to see if there is a unit
    * on the tile in the first place.
    * 
    * @param tile tile to be tested against.
    * @return whether or not the unit can attack at that tile.
    */
   public boolean canAttack(Tile tile) {
      if(!tile.isOccupied())
         return false;
      if(tile.getOccupiedBy().getOwner().equals(owner))
         return false;
      return true;
   }
   
   /**
    * Moves the unit from its current tile to the provided tile. First determines if the move
    * results in a change of image direction. Then sets the current tile to no longer be occupied.
    * And then finally changes the unit's coordinates, and sets the new tile as occupied and sets
    * its occupiedBy pointer to point to this unit.
    * 
    * @param tile tile that the unit is being moved to.
    */
   public void move(Tile tile) {
      if(x < tile.getX())
         setDir(DIRECTION.RIGHT);
      if(x > tile.getX())
         setDir(DIRECTION.LEFT);
      currentTile.removeUnit();
      this.x = tile.getX();
      this.y = tile.getY();
      currentTile = tile;
      tile.addUnit(this);
   }
   
   /**
    * Does all the calculations needed for this unit to attack a target.
    * 
    * @param target unit that this unit is attacking.
    * @param isRetaliation whether or not the attack is in retaliation to another attack.
    */
   public void attack(Unit target, boolean isRetaliation) {
      Random random = new Random();
      int techBonus = 0;
      for(Technology t: owner.getResearchedTechs()) {
         if(t.getUnitClass() == this.classification || t.getUnitClass() == UNIT_CLASS.ALL)
            techBonus += t.getAttackValue();
         if(t.getTileType() == currentTile.getType())
            techBonus += t.getAttackValue();
      }
      int[] minAndMax = DamageTable.getBounds(this.getType(), target.getType());
      int adHocMinAttack = minAndMax[0] + currentTile.getEffect().getAttackBonus() + techBonus;
      int adHocMaxAttack = minAndMax[1] + currentTile.getEffect().getAttackBonus() + techBonus;
      int attackNum = random.nextInt(adHocMaxAttack - adHocMinAttack) + adHocMinAttack;
      attackNum *= (double)health / (double)maxHealth;
      target.takeDamage(attackNum);
   }
   
   /**
    * Given an amount of damage to take, calculates the amount of actual damage to be taken by the
    * unit.
    * 
    * @param amount amount of damage done by whoever is attacking this unit.
    */
   public void takeDamage(int amount) {
      Random random = new Random();
      int techBonus = 0;
      for(Technology t: owner.getResearchedTechs()) {
         if(t.getUnitClass() == this.classification || t.getUnitClass() == UNIT_CLASS.ALL)
            techBonus += t.getDefenseValue();
         if(t.getTileType() == currentTile.getType())
            techBonus += t.getDefenseValue();
      }
      int adHocDefense = defense + currentTile.getEffect().getDefenseBonus() + techBonus;
      if(adHocDefense <= 0)
         adHocDefense = 1;
      int percMinus = random.nextInt(adHocDefense - (adHocDefense / 2)) + (adHocDefense / 2);
      health -= amount - (int)((double)amount * ((double)percMinus / 100.0));
      alive = health > 0;
   }
   
   public String toString() {
      return "Class: " + classification + " Type: " + type + " Health: " + health + " x: " + x + " y: " + y + " Team: " + owner.getName() + " Moved: " + moved + " Attacked: " + attacked
            + " Defense: " + defense;
   }
   
   public int getMoveRange() {
      return moveRange;
   }
   
   public void setMoveRange(int moveRange) {
      this.moveRange = moveRange;
   }
   
   public int getY() {
      return y;
   }
   
   public void setY(int y) {
      this.y = y;
   }
   
   public int getX() {
      return x;
   }
   
   public void setX(int x) {
      this.x = x;
   }
   
   public UNIT_CLASS getClassification() {
      return classification;
   }
   
   public void setClassification(UNIT_CLASS type) {
      this.classification = type;
   }
   
   public Tile getCurrentTile() {
      return currentTile;
   }
   
   public void setCurrentTile(Tile currentTile) {
      this.currentTile = currentTile;
   }
   
   public Team getOwner() {
      return owner;
   }
   
   public void setOwner(Team owner) {
      this.owner = owner;
   }
   
   public UNIT_TYPE getType() {
      return type;
   }
   
   public boolean hasMoved() {
      return moved;
   }
   
   public void setMoved(boolean moved) {
      this.moved = moved;
   }
   
   public boolean hasAttacked() {
      return attacked;
   }
   
   public void setAttacked(boolean attacked) {
      this.attacked = attacked;
   }
   
   public boolean isAlive() {
      return alive;
   }
   
   public void setAlive(boolean alive) {
      this.alive = alive;
   }
   
   public int getDefense() {
      return defense;
   }
   
   public void setDefense(int defense) {
      this.defense = defense;
   }
   
   public HashSet<Coordinate> getMoveSet() {
      return moveSet;
   }
   
   public void setMoveSet(HashSet<Coordinate> moveSet) {
      this.moveSet = moveSet;
   }
   
   public HashSet<Coordinate> getAttackSet() {
      return attackSet;
   }
   
   public void setAttackSet(HashSet<Coordinate> attackSet) {
      this.attackSet = attackSet;
   }
   
   public HashSet<Coordinate> getCaptureSet() {
      return captureSet;
   }
   
   public void getCaptureSet(HashSet<Coordinate> captureSet) {
      this.captureSet = captureSet;
   }
   
   public int getHealth() {
      return health;
   }
   
   public void setHealth(int health) {
      if(health > maxHealth)
         health = maxHealth;
      this.health = health;
   }
   
   public DIRECTION getDir() {
      return dir;
   }
   
   public void setDir(DIRECTION dir) {
      this.dir = dir;
   }
   
   public int getMaxHealth() {
      return maxHealth;
   }
   
   public void setMaxHealth(int maxHealth) {
      this.maxHealth = maxHealth;
   }
   
   public Image getTexture() {
      return texture;
   }
   
   public void setTexture(Image texture) {
      this.texture = texture;
   }
   
}
