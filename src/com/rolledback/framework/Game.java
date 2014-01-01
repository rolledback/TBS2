package com.rolledback.framework;

import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;

public class Game {
   
   int gameWidth, gameHeight, teamSize;
   Team teamOne, teamTwo, currentTeam;
   World world;
   
   boolean unitSelected;
   int[][] moveSpots;
   Tile selectedTile;
   Unit selectedUnit;
   Unit targetUnit;
   
   int UNIT_DENSITY = 5;
   
   public static void main(String args[]) {
      int numGames = 500;
      int[] winnerRecord = new int[3];
      double turnAvg = 0.0;
      long timeAvg = 0;
      long start = System.currentTimeMillis() / 1000;
      for(int z = 0; z < numGames; z++) {
         System.out.println(z);
         Game newGame = new Game();
         turnAvg += (double)newGame.testCode();
         if(newGame.teamOne.getUnits().size() == 0 && newGame.teamTwo.getUnits().size() != 0)
            winnerRecord[1]++;
         else if(newGame.teamOne.getUnits().size() != 0 && newGame.teamTwo.getUnits().size() == 0)
            winnerRecord[0]++;
         else
            winnerRecord[2]++;
      }
      timeAvg = (System.currentTimeMillis() / 1000) - start;
      
      System.out.println("Team one wins: " + winnerRecord[0]);
      System.out.println("Team two wins: " + winnerRecord[1]);
      System.out.println("Ties?: " + winnerRecord[2]);
      System.out.println("Avg turns: " + turnAvg / numGames);
      System.out.println("Avg time: " + (double)timeAvg / (double)numGames);
   }
   
   public Game() {
      gameWidth = 25;
      gameHeight = 15;
      teamSize = (gameWidth / 5) * (gameHeight / UNIT_DENSITY);
      teamOne = new ComputerTeamB("CPU1", teamSize, 500, this);
      teamTwo = new ComputerTeamB("CPU2", teamSize, 500, this);
      ((ComputerTeam)teamOne).opponent = teamTwo;
      ((ComputerTeam)teamTwo).opponent = teamOne;
      currentTeam = teamOne;
      world = new World(gameWidth, gameHeight, teamOne, teamTwo);
   }
   
   public void gameLoop(int xTile, int yTile) {
      int x = xTile; // click data
      int y = yTile; // click data
      
      selectedTile = selectTile(x, y);
      
      if(unitSelected) {
         if(!selectedUnit.hasAttacked() && moveSpots[y][x] == 2) {
            targetUnit = world.getTiles()[y][x].getOccupiedBy();
            attackMove(x, y);
            int attackNum = selectedUnit.attack();
            targetUnit.takeDamage(attackNum);
            if(!targetUnit.isAlive())
               world.destroyUnit(targetUnit);
            else {
               attackNum = targetUnit.attack() / 2;
               selectedUnit.takeDamage(attackNum);
               if(!selectedUnit.isAlive())
                  world.destroyUnit(selectedUnit);
            }
            selectedUnit.setMoved(true);
            selectedUnit.setAttacked(true);
         }
         if(!selectedUnit.hasMoved() && moveSpots[y][x] == 1) {
            selectedUnit.move(selectedTile);
            selectedUnit.setMoved(true);
         }
         unitSelected = false;
      }
      
      if(selectedTile.isOccupied() && selectedTile.getOccupiedBy().getOwner().equals(currentTeam)) {
         selectedUnit = selectedTile.getOccupiedBy();
         unitSelected = true;
         if(selectedUnit.getOwner().equals(currentTeam)) {
            moveSpots = world.calcMoveSpots(selectedUnit);
            // display move spots if haven't moved yet
            // display attack spots if haven't attacked yet
         }
      }
      else if(selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory)selectedTile).getOwner().equals(currentTeam)) {
         unitSelected = false;
         // choose unit to produce
      }
      if(!unitSelected)
         selectedUnit = null;
   }
   
   public void attackMove(int x, int y) {
      if(Math.abs(selectedUnit.getX() - targetUnit.getX()) == 1 && targetUnit.getY() == selectedUnit.getY())
         return;
      if(Math.abs(selectedUnit.getY() - targetUnit.getY()) == 1 && targetUnit.getX() == selectedUnit.getX())
         return;
      else if(x - 1 >= 0 && moveSpots[y][x - 1] == 1) {
         selectedUnit.move(world.getTiles()[y][x - 1]);
      }
      else if(x + 1 < gameWidth && moveSpots[y][x + 1] == 1) {
         selectedUnit.move(world.getTiles()[y][x + 1]);
      }
      else if(y - 1 >= 0 && moveSpots[y - 1][x] == 1) {
         selectedUnit.move(world.getTiles()[y - 1][x]);
      }
      else if(y + 1 < gameHeight && moveSpots[y + 1][x] == 1) {
         selectedUnit.move(world.getTiles()[y + 1][x]);
      }
      
   }
   
   public Tile selectTile(int x, int y) {
      Tile selectedTile = world.getTiles()[y][x];
      return selectedTile;
   }
   
   public int testCode() {
      for(int y = 0; y < 500000; y++) {
         // System.out.println(y);
         // System.out.println();
         // world.printUnits();
         // System.out.println();
         if(teamOne.getUnits().size() == 0 || teamTwo.getUnits().size() == 0)
            return y;
         if(currentTeam.equals(teamOne))
            ((ComputerTeam)currentTeam).executeTurn();
         if(currentTeam.equals(teamTwo))
            ((ComputerTeam)currentTeam).executeTurn();
         for(int x = 0; x < currentTeam.getUnits().size(); x++) {
            currentTeam.getUnits().get(x).setMoved(false);
            currentTeam.getUnits().get(x).setAttacked(false);
         }
         if(currentTeam.equals(teamOne))
            currentTeam = teamTwo;
         else
            currentTeam = teamOne;
      }
      world.printMap();
      System.out.println();
      world.printUnits();
      System.out.println();
      return 9999;
      
   }
   
   public void gameDebugFull() {
      System.out.println("----------------------------------");
      System.out.println("Game Debug: Full");
      System.out.println("----------------------------------");
      System.out.println("Board width: " + gameWidth);
      System.out.println("Board height: " + gameHeight);
      System.out.println("Starting team sizes: " + teamSize);
      System.out.println("Percentage of board occupied by units: " + (double)(100 * (teamSize * 2) / (gameWidth * gameHeight)));
      System.out.println();
      world.printMap();
      System.out.println();
      world.printUnits();
      System.out.println();
      System.out.println("----------------------------------");
      System.out.println("Team one: " + teamOne.toString());
      System.out.println("Team one units: " + teamOne.getUnits().toString());
      System.out.println("----------------------------------");
      System.out.println("Team two: " + teamTwo.toString());
      System.out.println("Team two units: " + teamTwo.getUnits().toString());
   }
}
