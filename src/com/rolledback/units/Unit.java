package com.rolledback.units;

import java.util.Random;

import com.rolledback.framework.Team;
import com.rolledback.terrain.Tile;

public class Unit {
   
   public enum UNIT_CLASS {
      VEHICLE, INFANTRY, NONE
   }
   
   public enum UNIT_TYPE {
      TANK, TANK_DEST, INFANTRY
   }
   
   public enum DIRECTION {
      LEFT, RIGHT
   }
   
   protected int minAttack, maxAttack, defense, attackRange, moveRange;
   protected int x, y;
   int health, maxHealth;
   private boolean alive;
   private boolean moved;
   private boolean attacked;
   protected Tile currentTile;
   protected UNIT_CLASS classification;
   protected UNIT_TYPE type;
   
   DIRECTION dir;
   private Team owner;
   
   public Unit(int x, int y, Tile t, Team o) {
      minAttack = 0;
      maxAttack = 0;
      defense = 0;
      attackRange = 0;
      moveRange = 0;
      this.x = x;
      this.y = y;
      dir = DIRECTION.RIGHT;
      health = 100;
      maxHealth = 100;
      alive = true;
      moved = false;
      currentTile = t;
      classification = UNIT_CLASS.NONE;
      owner = o;
   }
   
   public void move(Tile tile) {
      currentTile.setOccupied(false);
      currentTile.setOccupiedBy(null);
      this.x = tile.getX();
      this.y = tile.getY();
      currentTile = tile;
      tile.setOccupied(true);
      tile.setOccupiedBy(this);
   }
   
   public int attack() {
      Random random = new Random();
      int adHocMaxAttack = maxAttack + currentTile.getEffect().attackBonus;
      int adHocMinAttack = minAttack + currentTile.getEffect().attackBonus;
      return random.nextInt(adHocMaxAttack - adHocMinAttack) + adHocMinAttack;
   }
   
   public void takeDamage(int amount) {
      Random random = new Random();
      int adHocDefense = defense + currentTile.getEffect().defenseBonus;
      int percMinus = random.nextInt(adHocDefense - (adHocDefense / 2)) + (adHocDefense);
      System.out.println("Actual damage taken: " + percMinus);
      health -= (int)(amount - Math.ceil(amount * (percMinus / 100)));
      alive = health > 0;
   }
   
   public String toString() {
      return "Class: " + classification + " Type: " + type + " Health: " + health + " x: " + x + " y: " + y + " Team: " + owner.getName() + " Moved: "
            + moved + " Attacked: " + attacked;
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
   
}
