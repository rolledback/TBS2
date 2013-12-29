package com.rolledback.framework;

import com.rolledback.terrain.*;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;

public class Game {
   
   static int gameWidth, gameHeight, teamSize;
   static Team teamOne, teamTwo, currentTeam;
   static World world;
   static boolean unitSelected;
   
   public static void main(String args[]) {
      initGame();
      gameDebugFull();
      testCode();
   }
   
   public static void initGame() {
      gameWidth = 30;
      gameHeight = 20;
      teamSize = gameWidth / 5 * 3;
      teamOne = new Team("One", teamSize, 500);
      teamTwo = new Team("Two", teamSize, 500);
      currentTeam = teamOne;
      world = new World(gameWidth, gameHeight, teamOne, teamTwo);
   }
   
   public static void selectTile(int x, int y) {
      Tile selectedTile = world.getTiles()[y][x];
      Unit selectedUnit = null;
      System.out.println("Tile selected:\n" + selectedTile.toString() + "\n");
      if (selectedTile.isOccupied()) {
         selectedUnit = selectedTile.getOccupiedBy();
         if (selectedUnit.getOwner().equals(currentTeam)) {
            world.calcMoveSpots(selectedUnit);
            unitSelected = true;
            System.out.println("Selected unit.\n" + selectedUnit.toString() + "\n");
         }
      } else if (selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory) selectedTile).getOwner().equals(currentTeam)) {
         System.out.println("Factory selected:\n" + ((Factory) selectedTile).getProductionList().toString() + "\n");
      }
   }
   
   public static void testCode() {
      System.out.println("\n\nTESTING BEGINS HERE");
   }
   
   public static void gameDebugFull() {
      System.out.println("----------------------------------");
      System.out.println("Game Debug: Full");
      System.out.println("----------------------------------");
      System.out.println(world.toString());
      System.out.println(world.mapKey());
      world.printMap();
      System.out.println("----------------------------------");
      System.out.println(teamOne.toString());
      System.out.println(teamOne.getUnits().toString());
      System.out.println("----------------------------------");
      System.out.println(teamTwo.toString());
      System.out.println(teamTwo.getUnits().toString());
   }
}
