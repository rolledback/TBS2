package com.rolledback.teams.ai;

import java.util.Comparator;

import com.rolledback.terrain.Tile;

public class CoordinateNode {
   
   private boolean reachable;
   private CoordinateNode prev;
   private int x;
   private int y;
   private Tile tile;
   private int fScore;
   private int gScore;
   
   public CoordinateNode(boolean r, CoordinateNode p, int x, int y, Tile t, int f) {
      reachable = r;
      prev = p;
      this.x = x;
      this.y = y;
      tile = t;
      fScore = f;
   }
   
   public CoordinateNode(boolean r, CoordinateNode p, int x, int y, Tile t, int d, int g) {
      reachable = r;
      prev = p;
      this.x = x;
      this.y = y;
      tile = t;
      fScore = d;
      gScore = g;
   }
   
   public boolean equals(Object compare) {
      if(compare == null)
         return false;
      if(compare.getClass() != this.getClass())
         return false;
      CoordinateNode t = (CoordinateNode)compare;
      return t.x == x && t.y == y;
   }
   
   public int hashCode() {
      return (this.getX() * 31) + this.getY();
   }
   
   public boolean isReachable() {
      return reachable;
   }
   
   public void setReachable(boolean reachable) {
      this.reachable = reachable;
   }
   
   public CoordinateNode getPrev() {
      return prev;
   }
   
   public void setPrev(CoordinateNode prev) {
      this.prev = prev;
   }
   
   public int getX() {
      return x;
   }
   
   public void setX(int x) {
      this.x = x;
   }
   
   public int getY() {
      return y;
   }
   
   public void setY(int y) {
      this.y = y;
   }
   
   public Tile getTile() {
      return tile;
   }
   
   public int getfScore() {
      return fScore;
   }
   
   public void setfScore(int fScore) {
      this.fScore = fScore;
   }
   
   public int getgScore() {
      return gScore;
   }
   
   public void setgScore(int gScore) {
      this.gScore = gScore;
   }
   
   public String toString() {
      return x + " " + y + " " + "(" + fScore + ")";
   }
}

class CoordinateNodeComparator implements Comparator<CoordinateNode> {
   
   @Override
   public int compare(CoordinateNode arg0, CoordinateNode arg1) {
      return arg0.getfScore() - arg1.getfScore();
   }
   
}