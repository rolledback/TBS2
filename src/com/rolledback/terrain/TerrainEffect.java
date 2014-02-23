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
      return "Attack Bonus: " + attackBonus + " Defense Bonus: " + defenseBonus + " Move Bonus: " + moveBonus;
   }
   
   public String[] toStringArray() {
      String[] retArray = new String[3];
      retArray[0] = "Attack Bonus: " + attackBonus;
      retArray[1] = "Defense Bonus: " + defenseBonus;
      retArray[2] = "Move Bonus: " + moveBonus;
      return retArray;
   }
   
}
