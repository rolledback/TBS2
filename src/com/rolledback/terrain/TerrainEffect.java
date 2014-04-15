package com.rolledback.terrain;

/**
 * Represents the bonuses that tile con provide to a unit. These include attack, defense, and move
 * bonuses.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class TerrainEffect {
   
   private int attackBonus;
   private int defenseBonus;
   private int moveCost;
   
   /**
    * Constructor.
    * 
    * @param a attack bonus.
    * @param d defense bonus.
    * @param m move cost.
    */
   public TerrainEffect(int a, int d, int m) {
      attackBonus = a;
      defenseBonus = d;
      moveCost = m;
   }
   
   public String toString() {
      return "Attack Bonus: " + attackBonus + " Defense Bonus: " + defenseBonus + " Move Bonus: " + moveCost;
   }
   
   /**
    * Similar to string, but puts each string output for each bonus in a separate array index.
    * 
    * @return an array of string outputs for each bonus. In the order attack, defense, then move.
    */
   public String[] toStringArray() {
      String[] retArray = new String[3];
      retArray[0] = "Attack Bonus: " + attackBonus;
      retArray[1] = "Defense Bonus: " + defenseBonus;
      retArray[2] = "Move Bonus: " + moveCost;
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
   
   public int getMoveCost() {
      return moveCost;
   }
   
   public void setMoveCost(int moveCost) {
      this.moveCost = moveCost;
   }
   
}
