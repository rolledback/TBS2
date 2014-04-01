package com.rolledback.framework;

/**
 * Basic implementation of a Coordinate class. Very similar to the Java Point class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Coordinate {
   
   private int x, y;
   private double bonus;
   
   /**
    * Basic constructor.
    * 
    * @param x the x value of the coordinate
    * @param y the y value of the coordinate
    */
   public Coordinate(int x, int y) {
      this.x = x;
      this.y = y;
      this.bonus = Integer.MAX_VALUE;
   }
   
   /**
    * Secondary constructor which also sets the bonus value. Used by two of the AI classes. No other
    * use for it.
    * 
    * @param x the x value of the coordinate
    * @param y the y value of the coordinate
    * @param b the value of to be stored in bonus
    */
   public Coordinate(int x, int y, double b) {
      this.x = x;
      this.y = y;
      this.bonus = b;
   }
   
   public int getX() {
      return this.x;
   }
   
   public int getY() {
      return this.y;
   }
   
   public double getBonus() {
      return this.bonus;
   }
   
   public String toString() {
      return "x: " + x + " y: " + y;
   }
   
   public boolean equals(Object compare) {
      if(compare == null)
         return false;
      if(compare.getClass() != this.getClass())
         return false;
      return ((Coordinate)compare).getX() == this.x && ((Coordinate)compare).getY() == this.y;
   }
   
   public int hashCode() {
      return (this.x * 31) + this.y;
   }
}
