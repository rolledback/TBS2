package com.rolledback.terrain;

public class TerrainEffect {
	
	public int attackBonus;
	public int defenseBonus;
	public int moveBonus;
	
	public TerrainEffect(int a, int d, int m) {
		attackBonus = a;
		defenseBonus = d;
		moveBonus = m;
	}
	
	public String toString() {
		return "Attack Bonus: " + attackBonus + "\nDefense Bonus: "
		      + defenseBonus + "\nMove Bonus: " + moveBonus;
	}
	
}
