package com.rolledback.framework;

import com.rolledback.terrain.*;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.*;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Game {
   
   static int gameWidth, gameHeight, teamSize;
   static Team teamOne, teamTwo, currentTeam;
   static World world;
   
   static boolean unitSelected;
   static int[][] moveSpots;
   static Tile selectedTile;
   static Unit selectedUnit;
   static Unit targetUnit;
   
   // smaller number = more units per column at start, also slower game setup
   static int UNIT_DENSITY = 5;
   
   public static void main(String args[]) {
      initGame();
      gameDebugFull();
      testCode();
   }
   
   public static void initGame() {
      gameWidth = 25;
      gameHeight = 15;
      teamSize = (gameWidth / 5) * (gameHeight / UNIT_DENSITY);
      teamOne = new Team("One", teamSize, 500);
      teamTwo = new Team("Two", teamSize, 500);
      currentTeam = teamOne;
      world = new World(gameWidth, gameHeight, teamOne, teamTwo);
   }
   
   public static void gameLoop(int xTile, int yTile) {
      int x = xTile; // click data
      int y = yTile; // click data
      
      selectedTile = selectTile(x, y);
      
      if(unitSelected) {
         if(!selectedUnit.hasAttacked() && moveSpots[y][x] == 2) {
            targetUnit = world.getTiles()[y][x].getOccupiedBy();
            attackMove(x, y);
            int attackNum = selectedUnit.attack();
            System.out.println("Attacking with: " + attackNum);
            targetUnit.takeDamage(attackNum);
            if(!targetUnit.isAlive())
               world.destroyUnit(targetUnit);
            selectedUnit.setMoved(true);
            selectedUnit.setAttacked(true);
         }
         if(!selectedUnit.hasMoved() && moveSpots[y][x] == 1) {
            System.out.println("Moving unit.");
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
            System.out.println("Selected unit.\n" + selectedUnit.toString() + "\n");
         }
      }
      else if(selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory)selectedTile).getOwner().equals(currentTeam)) {
         unitSelected = false;
         System.out.println("Factory selected:\n" + ((Factory)selectedTile).getProductionList().toString() + "\n");
      }
      if(!unitSelected)
         selectedUnit = null;
   }
   
   public static void attackMove(int x, int y) {
      System.out.println("Attacking unit.");
      System.out.println(targetUnit.toString());
      if(Math.abs(selectedUnit.getX() - targetUnit.getX()) == 1 && targetUnit.getY() == selectedUnit.getY())
         return;
      if(Math.abs(selectedUnit.getY() - targetUnit.getY()) == 1 && targetUnit.getX() == selectedUnit.getX())
         return;
      System.out.println("Have to move to attack.");
      if(moveSpots[y - 1][x] == 1) {
         selectedUnit.move(world.getTiles()[y - 1][x]);
      }
      else if(moveSpots[y + 1][x] == 1) {
         selectedUnit.move(selectedTile);
         selectedUnit.move(world.getTiles()[y + 1][x]);
      }
      else if(moveSpots[y][x - 1] == 1) {
         selectedUnit.move(selectedTile);
         selectedUnit.move(world.getTiles()[y][x - 1]);
      }
      else if(moveSpots[y][x + 1] == 1) {
         selectedUnit.move(selectedTile);
         selectedUnit.move(world.getTiles()[y][x + 1]);
      }
   }
   
   public static Tile selectTile(int x, int y) {
      Tile selectedTile = world.getTiles()[y][x];
      System.out.println("Tile selected:\n" + selectedTile.toString() + "\n");
      return selectedTile;
   }
   
   public static void testCode() {
      System.out.println("\n\nTESTING BEGINS HERE");
      
      teamOne.getUnits().clear();
      teamTwo.getUnits().clear();
      for(int row = 0; row < gameHeight; row++) {
         for(int col = 0; col < gameWidth; col++) {
            world.getTiles()[row][col] = new Plain(world, col, row);
         }
      }
      teamOne.createUnit(world.getTiles()[10][7], UNIT_TYPE.TANK);
      teamOne.createUnit(world.getTiles()[4][5], UNIT_TYPE.TANK);
      teamTwo.createUnit(world.getTiles()[9][9], UNIT_TYPE.TANK);
      world.printUnits();
      System.out.println("---TESTING MOVING THEN ATTACKING AND DESTROYING---");
      gameLoop(7, 10);
      gameLoop(8, 9);
      world.printUnits();
      System.out.println(teamOne.getUnits().toString());
      System.out.println(teamTwo.getUnits().toString());
      gameLoop(9, 9);
      world.printUnits();
      
      while(teamTwo.getUnits().size() > 0) {
         teamOne.getUnits().get(0).setAttacked(false);
         gameLoop(8, 9);
         gameLoop(9, 9);      
      }
      
      world.printUnits();
            
      if(selectedUnit != null) 
         System.out.println(selectedUnit.toString());
      else
         System.out.println("NO UNIT SELECTED");
      System.out.println(teamOne.getUnits().toString());
      System.out.println(teamTwo.getUnits().toString());
      
      System.out.println("DONE");
   }
   
   public static void gameDebugFull() {
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
