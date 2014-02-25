package com.rolledback.teams;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.units.Unit;

public abstract class ComputerTeam extends Team {
   
   Game game;
   protected Team opponent;
   Coordinate target;
   Unit targetUnit;
   
   public ComputerTeam(String name, int size, int r, Game g) {
      super(name, size, r);
      game = g;
   }
   
   public abstract void executeTurn();

   public Team getOpponent() {
      return opponent;
   }

   public void setOpponent(Team opponent) {
      this.opponent = opponent;
   }
   
   public void delay(int n) {
      long startDelay = System.currentTimeMillis();
      long endDelay = 0;
      while(endDelay - startDelay < n)
         endDelay = System.currentTimeMillis();
   }
}
