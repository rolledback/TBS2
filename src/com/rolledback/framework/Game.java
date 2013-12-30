package com.rolledback.framework;

import java.util.Scanner;

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
   
   // smaller number = more units per column at start
   int UNIT_DENSITY = 5;
   
   public static void main(String args[]) {
      int[] winnerRecord = new int[3];
      for(int z = 0; z < 1; z++) {
         Game newGame = new Game();
         //newGame.world.printUnits();
         //System.out.println();
         //newGame.gameDebugFull();
         newGame.testCode();
         
         if(newGame.teamOne.getUnits().size() == 0 && newGame.teamTwo.getUnits().size() != 0)
            winnerRecord[1]++;
         else if(newGame.teamOne.getUnits().size() != 0 && newGame.teamTwo.getUnits().size() == 0)
            winnerRecord[0]++;
         else
            winnerRecord[2]++;
      }
      
      System.out.println("Team one wins: " + winnerRecord[0]);
      System.out.println("Team two wins: " + winnerRecord[1]);
      System.out.println("Ties?: " + winnerRecord[2]);
   }
   
   public Game() {
      gameWidth = 10;
      gameHeight = 10;
      teamSize = (gameWidth / 5) * (gameHeight / UNIT_DENSITY);
      teamOne = new ComputerTeam("CPU1", teamSize, 500, this);
      teamTwo = new ComputerTeam("CPU2", teamSize, 500, this);
      ((ComputerTeam)teamOne).opponent = teamTwo;
      ((ComputerTeam)teamTwo).opponent = teamOne;
      currentTeam = teamOne;
      world = new World(gameWidth, gameHeight, teamOne, teamTwo);
   }
   
   public void gameLoop(int xTile, int yTile) {
      //System.out.println("Start of function");
      int x = xTile; // click data
      int y = yTile; // click data
      
      selectedTile = selectTile(x, y);
      
      if(unitSelected) {
         if(!selectedUnit.hasAttacked() && moveSpots[y][x] == 2) {
            targetUnit = world.getTiles()[y][x].getOccupiedBy();
            attackMove(x, y);
            int attackNum = selectedUnit.attack();
            //System.out.println("Attacking with: " + attackNum);
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
            //System.out.println("Moving unit.");
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
            //System.out.println("Selected unit.\n" + selectedUnit.toString() + "\n");
            // display move spots if haven't moved yet
            // display attack spots if haven't attacked yet
         }
      }
      else if(selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory)selectedTile).getOwner().equals(currentTeam)) {
         unitSelected = false;
         //System.out.println("Factory selected:\n" + ((Factory)selectedTile).getProductionList().toString() + "\n");
         // choose unit to produce
      }
      if(!unitSelected)
         selectedUnit = null;
   }
   
   public void attackMove(int x, int y) {
      //System.out.println("Attacking unit.");
      //System.out.println(targetUnit.toString());
      if(Math.abs(selectedUnit.getX() - targetUnit.getX()) == 1 && targetUnit.getY() == selectedUnit.getY())
         return;
      if(Math.abs(selectedUnit.getY() - targetUnit.getY()) == 1 && targetUnit.getX() == selectedUnit.getX())
         return;
      //System.out.println("Have to move to attack.");
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
      //System.out.println("Tile selected:\n" + selectedTile.toString() + "\n");
      return selectedTile;
   }
   
   public void testCode() {
      //System.out.println("\n\nTESTING BEGINS HERE");
      for(int y = 0; y < 100; y++) {
         if(teamOne.getUnits().size() == 0 || teamTwo.getUnits().size() == 0)            
            return;
         currentTeam = teamOne;
         ((ComputerTeam)teamOne).executeTurn();
         for(int x = 0; x < teamOne.getUnits().size(); x++) {
            teamOne.getUnits().get(x).setMoved(false);
            teamOne.getUnits().get(x).setAttacked(false);
         }
         world.printUnits();
         System.out.println();
         System.out.println();
         currentTeam = teamTwo;
         if(teamOne.getUnits().size() == 0 || teamTwo.getUnits().size() == 0)            
            return;
         ((ComputerTeam)teamTwo).executeTurn();
         for(int x = 0; x < teamTwo.getUnits().size(); x++) {
            teamTwo.getUnits().get(x).setMoved(false);
            teamTwo.getUnits().get(x).setAttacked(false);
         }
         world.printUnits();
         System.out.println();
         System.out.println();
      }
       world.printUnits();
       System.out.println();
       world.printMap();
       System.out.println("\n\n\n\n");
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
