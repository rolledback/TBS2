package com.rolledback.units;

import java.util.HashSet;
import java.util.Random;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Logger;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;

public class Unit {
   
   public enum UNIT_CLASS {
      VEHICLE, INFANTRY, NONE
   }
   
   public enum UNIT_TYPE {
      TANK, TANK_DEST, INFANTRY, RPG
   }
   
   public enum DIRECTION {
      LEFT, RIGHT
   }
   
   protected HashSet<Coordinate> moveSet;
   protected HashSet<Coordinate> attackSet;
   protected HashSet<Coordinate> captureSet;
   protected int minAttack;
   protected int maxAttack;
   protected int infAttackBonus;
   protected int vehAttackBonus;
   protected int defense;
   protected int attackRange;
   protected int moveRange;
   protected int x, y;
   private int health;
   int maxHealth;
   private boolean alive;
   private boolean moved;
   private boolean attacked;
   protected Tile currentTile;
   protected UNIT_CLASS classification;
   protected UNIT_TYPE type;
   
   private DIRECTION dir;
   private Team owner;
   
   public Unit(int x, int y, Tile t, Team o) {
      minAttack = 0;
      maxAttack = 0;
      infAttackBonus = 0;
      vehAttackBonus = 0;
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
      classification = UNIT_CLASS.NONE;
      owner = o;
      moveSet = new HashSet<Coordinate>();
      attackSet = new HashSet<Coordinate>();
      captureSet = new HashSet<Coordinate>();
   }
   
   public void calcMoveSpots() {
      World world = this.currentTile.getWorld();
      int adHocRange = moveRange + currentTile.getEffect().moveBonus;
      if(adHocRange <= 0)
         adHocRange = 1;
      currentTile.setOccupied(false);
      attackSet.clear();
      moveSet.clear();
      captureSet.clear();
      calcMoveSpotsHelper(world, x, y, adHocRange + 1, false);
      currentTile.setOccupied(true);
      moveSet.remove(new Coordinate(x, y));
   }
   
   public void calcMoveSpotsHelper(World world, int x, int y, int range, boolean movedThrough) {     
      Coordinate thisCoord = new Coordinate(x, y);
      Tile tiles[][] = world.getTiles();
      int height = world.getHeight();
      int width = world.getWidth();
      if(x < 0 || x >= width || y < 0 || y >= height)
         return;
      else if(range <= 0) {
         if(tiles[y][x].isOccupied() && !owner.equals(tiles[y][x].getOccupiedBy().getOwner()) && !movedThrough)
            attackSet.add(thisCoord);
         return;
      }
      else if(tiles[y][x].isOccupied()) {
         if(!owner.equals(tiles[y][x].getOccupiedBy().getOwner()) && !movedThrough)
            attackSet.add(thisCoord);
      }
      else if(canCapture(tiles[y][x]))
         captureSet.add(thisCoord);
      else if(tiles[y][x].getType() == TILE_TYPE.CITY)
         moveSet.add(thisCoord);
      else if(canTraverse(tiles[y][x])) {
         moveSet.add(thisCoord);
      }
      range--;
      boolean mT = tiles[y][x].isOccupied() && owner.equals(tiles[y][x].getOccupiedBy().getOwner());
      if(captureSet.contains(thisCoord) || moveSet.contains(thisCoord) || mT) {
         calcMoveSpotsHelper(world, x + 1, y, range, mT);
         calcMoveSpotsHelper(world, x - 1, y, range, mT);
         calcMoveSpotsHelper(world, x, y + 1, range, mT);
         calcMoveSpotsHelper(world, x, y - 1, range, mT);
      }
      else
         return;
   }
   
   public boolean canTraverse(Tile tile) {
      if(tile.getType() == TILE_TYPE.RIVER)
         return false;
      if(tile.getType() == TILE_TYPE.MOUNTAIN && classification != UNIT_CLASS.INFANTRY)
         return false;
      return true;
   }
   
   public boolean canCapture(Tile tile) {
      if(!(tile instanceof CapturableTile))
         return false;
      if(type != UNIT_TYPE.INFANTRY)
         return false;
      return ((CapturableTile)tile).getOwner() == null || !owner.equals(((CapturableTile)tile).getOwner());
   }
   
   public void move(Tile tile) {
      if(x < tile.getX())
         setDir(DIRECTION.RIGHT);
      if(x > tile.getX())
         setDir(DIRECTION.LEFT);
      currentTile.setOccupied(false);
      currentTile.setOccupiedBy(null);
      this.x = tile.getX();
      this.y = tile.getY();
      currentTile = tile;
      tile.setOccupied(true);
      tile.setOccupiedBy(this);
   }
   
   public void attack(Unit target, boolean isRetaliation) {
      Random random = new Random();
      int adHocMaxAttack = maxAttack + currentTile.getEffect().attackBonus;
      int adHocMinAttack = minAttack + currentTile.getEffect().attackBonus;
      int attackNum = random.nextInt(adHocMaxAttack - adHocMinAttack) + adHocMinAttack;
      if(target.getClass().equals(UNIT_CLASS.INFANTRY))
         attackNum += infAttackBonus;
      else
         attackNum += vehAttackBonus;
      if(isRetaliation)
         attackNum /= 2;
      target.takeDamage(attackNum);
   }
   
   public void takeDamage(int amount) {
      Random random = new Random();
      int adHocDefense = defense + currentTile.getEffect().defenseBonus;
      if(adHocDefense <= 0)
         adHocDefense = 1;
      int percMinus = random.nextInt(adHocDefense - (adHocDefense / 2)) + (adHocDefense);
      health -= (int)(amount - Math.ceil(amount * (percMinus / 100)));
      alive = health > 0;
   }
   
   public String toString() {
      return "Class: " + classification + " Type: " + type + " Health: " + health + " x: " + x + " y: " + y + " Team: " + owner.getName()
            + " Moved: " + moved + " Attacked: " + attacked + " Defense: " + defense;
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
   
   public int getMaxAttack() {
      return maxAttack;
   }
   
   public void setMaxAttack(int maxAttack) {
      this.maxAttack = maxAttack;
   }
   
   public int getMinAttack() {
      return minAttack;
   }
   
   public void setMinAttack(int minAttack) {
      this.minAttack = minAttack;
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
      this.health = health;
   }
   
   public DIRECTION getDir() {
      return dir;
   }
   
   public void setDir(DIRECTION dir) {
      this.dir = dir;
   }
}
