package com.rolledback.units;

import java.util.HashSet;
import java.util.Random;

import com.rolledback.framework.Coordinate;
import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

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
      if(adHocDefense  <= 0)
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
