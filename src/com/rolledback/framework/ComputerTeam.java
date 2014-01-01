package com.rolledback.framework;

import com.rolledback.units.Unit;

public class ComputerTeam extends Team {
   
   Game game;
   Team opponent;
   Coordinate target;
   Unit targetUnit;
   
   public ComputerTeam(String name, int size, int r, Game g) {
      super(name, size, r);
      game = g;
   }
   
   public void executeTurn() {
      for(int x = 0; x < units.size(); x++) {
         Unit currUnit = units.get(x);
         int[][] moveSpots = game.world.calcMoveSpots(currUnit);
         for(int row = 0; row < game.gameHeight; row++)
            for(int col = 0; col < game.gameWidth; col++)
               if(moveSpots[row][col] == 1) {
                  game.gameLoop(currUnit.getX(), currUnit.getY());
                  game.gameLoop(col, row);
               }
      }
   }
}
