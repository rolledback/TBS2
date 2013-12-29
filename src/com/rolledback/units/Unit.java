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
	boolean alive, moved;
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
	
	public void move(int x, int y, Tile tile) {
		currentTile.setOccupied(false);
		this.x = x;
		this.y = y;
		currentTile = tile;
		tile.setOccupied(true);
	}
	
	public int attack() {
		Random random = new Random();
		int adHocMaxAttack = maxAttack + currentTile.getEffect().attackBonus;
		int adHocMinAttack = maxAttack + currentTile.getEffect().attackBonus;
		return random.nextInt(adHocMaxAttack - adHocMinAttack) + adHocMinAttack;
	}
	
	public void takeDamage(int amount) {
		Random random = new Random();
		int adHocDefense = defense + currentTile.getEffect().defenseBonus;
		double percMinus = random.nextInt(adHocDefense - (adHocDefense / 2))
		      + (adHocDefense);
		health -= (int) (amount - Math.ceil(amount * (percMinus / 100)));
		alive = health > 0;
	}
	
	public String toString() {
		return "Type: " + classification + ", " + type + " x: " + x + " y: " + y;
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
	
}
