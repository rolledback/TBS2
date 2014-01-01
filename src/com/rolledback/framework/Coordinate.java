package com.rolledback.framework;

public class Coordinate {
   
   private int x, y;
   private double bonus;
   
   public Coordinate(int x, int y) {
      this.x = x;
      this.y = y;
      this.bonus = Integer.MAX_VALUE;
   }
   
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
}
