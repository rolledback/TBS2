package com.rolledback.teams.ai;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

public abstract class ComputerTeam extends Team {
   
   Game game;
   Coordinate target;
   Unit targetUnit;
   
   public ComputerTeam(String name, int size, int r, Game g, int n) {
      super(name, size, r, n);
      game = g;
   }
   
   public abstract void executeTurn();
   
   public void delay(int n) {
      long startDelay = System.currentTimeMillis();
      long endDelay = 0;
      while(endDelay - startDelay < n)
         endDelay = System.currentTimeMillis();
   }

   public void setGame(Game game) {
      this.game = game;      
   }
}
