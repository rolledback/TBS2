package com.rolledback.terrain;

public class TerrainEffect {

   private int attackBonus;
   private int defenseBonus;
   private int moveBonus;
   
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
   
   public int getAttackBonus() {
      return attackBonus;
   }

   public void setAttackBonus(int attackBonus) {
      this.attackBonus = attackBonus;
   }

   public int getDefenseBonus() {
      return defenseBonus;
   }

   public void setDefenseBonus(int defenseBonus) {
      this.defenseBonus = defenseBonus;
   }

   public int getMoveBonus() {
      return moveBonus;
   }

   public void setMoveBonus(int moveBonus) {
      this.moveBonus = moveBonus;
   }
   
}
