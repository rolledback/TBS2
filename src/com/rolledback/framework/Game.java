package com.rolledback.framework;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rolledback.teams.ComputerTeam;
import com.rolledback.teams.ComputerTeamB;
import com.rolledback.teams.Team;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Game extends JPanel implements MouseListener, ActionListener {
   
   public enum GAME_STATE {
      NORMAL, DISPLAY_MOVE
   }
   
   private static final long serialVersionUID = 1L;
   
   public int gameWidth, gameHeight, teamSize, tileSize, offsetHorizontal, offsetVertical, guiHeight, selectedX, selectedY;
   public Team teamOne, teamTwo;
   
   Team currentTeam;
   private World world;
   
   boolean unitSelected, ready;
   int[][] moveSpots;
   Tile selectedTile;
   Unit selectedUnit;
   Unit targetUnit;
   Rectangle[][] grid;
   GAME_STATE state;
   
   int UNIT_DENSITY = 5;
   
   public Game(int x, int y, int ts, int oH, int oV, int gH) {
      gameWidth = x;
      gameHeight = y;
      teamSize = (gameWidth / 5) * (gameHeight / UNIT_DENSITY);
      teamOne = new ComputerTeamB("Player", teamSize, 500, this);
      teamTwo = new ComputerTeamB("CPU2", teamSize, 500, this);
      currentTeam = teamTwo;
      
      if(teamOne.getClass().equals(ComputerTeamB.class))
         ((ComputerTeam)teamOne).setOpponent(teamTwo);
      if(teamTwo.getClass().equals(ComputerTeamB.class))
         ((ComputerTeam)teamTwo).setOpponent(teamOne);
      
      world = new World(gameWidth, gameHeight, teamOne, teamTwo);
      tileSize = ts;
      offsetHorizontal = oH;
      offsetVertical = oV;
      addMouseListener(this);
      state = GAME_STATE.NORMAL;
      
      grid = new Rectangle[y][x];
      for(int row = 0; row < gameHeight; row++)
         for(int col = 0; col < gameWidth; col++)
            grid[row][col] = new Rectangle((col * tileSize) + offsetHorizontal, (row * tileSize) + offsetVertical, tileSize, tileSize);
      this.setBackground(Color.black);
      
      guiHeight = gH;
      
      selectedX = 0;
      selectedY = 0;
      
      gameDebugFull();
   }
   
   public void paintComponent(Graphics g) {
      drawTiles(g);
      // drawHeightMap(g);
      drawUnits(g);
      drawHealthBars(g);
      if(state == GAME_STATE.DISPLAY_MOVE) {
         drawMoveSpots(g);
         state = GAME_STATE.NORMAL;
      }
      g.setColor(Color.black);
      g.drawOval(selectedX * tileSize + offsetHorizontal, selectedY * tileSize + offsetVertical, tileSize, tileSize);
      if(unitSelected && !selectedUnit.getOwner().equals(currentTeam)) {
         selectedUnit = null;
         unitSelected = false;
      }
      drawGui(g);
   }
   
   public void drawGui(Graphics g) {
      g.setColor(Color.GRAY);
      g.fillRect(0, this.getHeight() - guiHeight, this.getWidth(), guiHeight);
      
      g.setColor(Color.DARK_GRAY);
      g.fillRect(0, this.getHeight() - guiHeight, this.getWidth(), 16);
      g.fillRect(0, this.getHeight() - 16, this.getWidth(), 16);
      g.fillRect(0, this.getHeight() - guiHeight, 16, guiHeight);
      g.fillRect(this.getWidth() - 16, this.getHeight() - guiHeight, 16, guiHeight);
      
      currentTileGui(g);
   }
   
   public void currentTileGui(Graphics g) {
      Color tileColor;
      if(world.getTiles()[selectedY][selectedX].getType() == TILE_TYPE.FOREST)
         tileColor = new Color(0, 128, 0);
      else if(world.getTiles()[selectedY][selectedX].getType() == TILE_TYPE.PLAIN)
         tileColor = new Color(126, 208, 102);
      else if(world.getTiles()[selectedY][selectedX].getType() == TILE_TYPE.MOUNTAIN)
         tileColor = Color.lightGray;
      else
         tileColor = Color.red;
      g.setColor(tileColor);
      g.fillRect(32, this.getHeight() - guiHeight + 32, 64, 64);
      
      g.setColor(Color.black);
      Font font = new Font("Arial", Font.BOLD, 12);
      g.setFont(font);
      g.drawString("Current Tile: " + world.getTiles()[selectedY][selectedX].getType(), 102, this.getHeight() - guiHeight + 45);
      g.drawString("Attack Bonus: " + world.getTiles()[selectedY][selectedX].getEffect().attackBonus, 102, this.getHeight() - guiHeight + 62);
      g.drawString("Defense Bonus: " + world.getTiles()[selectedY][selectedX].getEffect().defenseBonus, 102, this.getHeight() - guiHeight + 79);
      g.drawString("Move Bonus: " + world.getTiles()[selectedY][selectedX].getEffect().moveBonus, 102, this.getHeight() - guiHeight + 96);
      g.drawRect(32, this.getHeight() - guiHeight + 32, 64, 64);
      for(int x = 0; x < 5; x++)
         g.drawRect(24 + x, this.getHeight() - guiHeight + 24 + x, 300 - (2 * x), 80 - (2 * x));
   }
   
   public void switchTeams() {
      System.out.println("Switch teams.");
      unitSelected = false;
      selectedUnit = null;
      Iterator<Unit> i = currentTeam.getUnits().iterator();
      while(i.hasNext()) {
         Unit temp = i.next();
         temp.setMoved(false);
         temp.setAttacked(false);
      }
      
      if(teamOne.getUnits().size() == 0 || teamTwo.getUnits().size() == 0)
         return;
      
      if(currentTeam.equals(teamOne))
         currentTeam = teamTwo;
      else
         currentTeam = teamOne;
      
      for(City c: currentTeam.getCities())
         c.produceResources();
      
      state = GAME_STATE.NORMAL;
      
      if(currentTeam.getClass().equals(ComputerTeamB.class)) {
         ((ComputerTeamB)currentTeam).executeTurn();
         switchTeams();
      }
      else
         update(this.getGraphics());
   }
   
   public void drawTiles(Graphics g) {
      for(int x = 0; x < gameWidth; x++) {
         for(int y = 0; y < gameHeight; y++) {
            Color tileColor;
            if(world.getTiles()[y][x].getType() == TILE_TYPE.FOREST)
               tileColor = new Color(0, 128, 0);
            else if(world.getTiles()[y][x].getType() == TILE_TYPE.PLAIN)
               tileColor = new Color(126, 208, 102);
            else if(world.getTiles()[y][x].getType() == TILE_TYPE.MOUNTAIN)
               tileColor = Color.lightGray;
            else if(world.getTiles()[y][x].getType() == TILE_TYPE.RIVER)
               tileColor = new Color(41, 32, 132);
            else if(world.getTiles()[y][x].getType() == TILE_TYPE.BRIDGE)
               tileColor = new Color(128, 64, 0);
            else if(world.getTiles()[y][x].getType() == TILE_TYPE.CITY) {
               if(((City)world.getTiles()[y][x]).getOwner() == null)
                  tileColor = Color.MAGENTA;
               else if(((City)world.getTiles()[y][x]).getOwner().equals(teamOne))
                  tileColor = Color.orange;
               else
                  tileColor = Color.cyan;
            }
            else {
               if(((Factory)world.getTiles()[y][x]).getOwner().equals(teamOne))
                  tileColor = Color.red;
               else
                  tileColor = Color.blue;
            }
            
            g.setColor(tileColor);
            g.fillRect((x * tileSize) + offsetHorizontal, (y * tileSize) + offsetVertical, tileSize, tileSize);
            g.setColor(Color.black);
            g.drawRect((x * tileSize) + offsetHorizontal, (y * tileSize) + offsetVertical, tileSize, tileSize);
         }
      }
   }
   
   public void drawHeightMap(Graphics g) {
      for(int x = 0; x < gameWidth; x++) {
         for(int y = 0; y < gameHeight; y++) {
            Color tileColor;
            if(world.getHeightMap()[y][x] == 0)
               tileColor = Color.green;
            else if(world.getHeightMap()[y][x] == 1)
               tileColor = Color.yellow;
            else if(world.getHeightMap()[y][x] == 2)
               tileColor = Color.orange;
            else
               tileColor = Color.red;
            g.setColor(tileColor);
            g.fillRect((x * tileSize) + offsetHorizontal, (y * tileSize) + offsetVertical, tileSize, tileSize);
            g.setColor(Color.black);
            g.drawRect((x * tileSize) + offsetHorizontal, (y * tileSize) + offsetVertical, tileSize, tileSize);
         }
      }
   }
   
   public void drawUnits(Graphics g) {
      Iterator<Unit> i = teamOne.getUnits().iterator();
      g.setColor(Color.black);
      Font font = new Font("Serif", Font.PLAIN, 22);
      g.setFont(font);
      while(i.hasNext()) {
         Unit temp = i.next();
         UNIT_TYPE u = temp.getType();
         String uString = "";
         if(u == UNIT_TYPE.INFANTRY)
            uString = "I";
         if(u == UNIT_TYPE.TANK)
            uString = "T";
         if(u == UNIT_TYPE.TANK_DEST)
            uString = "D";
         g.drawString(uString, (temp.getX() * tileSize) + offsetHorizontal + 10, (temp.getY() * tileSize) + offsetVertical + (tileSize / 2) + 10);
      }
      
      i = teamTwo.getUnits().iterator();
      while(i.hasNext()) {
         Unit temp = i.next();
         UNIT_TYPE u = temp.getType();
         String uString = "";
         if(u == UNIT_TYPE.INFANTRY)
            uString = "i";
         if(u == UNIT_TYPE.TANK)
            uString = "t";
         if(u == UNIT_TYPE.TANK_DEST)
            uString = "d";
         g.drawString(uString, (temp.getX() * tileSize) + offsetHorizontal + (tileSize / 2), (temp.getY() * tileSize) + offsetVertical
               + (tileSize / 2) + 10);
      }
   }
   
   public void drawHealthBars(Graphics g) {
      int buffer = (int)((double)tileSize * .10);
      for(int y = 0; y < gameHeight; y++) {
         for(int x = 0; x < gameWidth; x++) {
            if(world.getTiles()[y][x].isOccupied()) {
               int xCorner = (x * tileSize) + offsetHorizontal + buffer;
               int yCorner = (y * tileSize) + offsetVertical + buffer;
               g.setColor(Color.red);
               g.fillRect(xCorner, yCorner, tileSize - (2 * buffer), buffer);
               g.setColor(Color.green);
               g.fillRect(xCorner, yCorner, (int)((double)(tileSize - (2 * buffer)) * (double)((double)world.getTiles()[y][x].getOccupiedBy()
                     .getHealth() / 100.0)), buffer);
            }
         }
      }
   }
   
   public void gameLoop(int xTile, int yTile) {
      int x = xTile; // click data
      int y = yTile; // click data
      selectedX = xTile;
      selectedY = yTile;
      selectedTile = selectTile(x, y);
      
      if(unitSelected) {
         if(!selectedUnit.hasMoved() && !selectedUnit.hasAttacked() && moveSpots[y][x] == 2) {
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
         if(!selectedUnit.hasMoved() && moveSpots[y][x] == 3) {
            selectedUnit.move(selectedTile);
            selectedUnit.setMoved(true);
            if(((City)selectedTile).getOwner() == null || !((City)selectedTile).getOwner().equals(currentTeam)) {
               ((City)selectedTile).capture(selectedUnit);
            }
         }
         if(!selectedUnit.hasMoved() && moveSpots[y][x] == 1) {
            selectedUnit.move(selectedTile);
            selectedUnit.setMoved(true);
         }
         unitSelected = false;
      }
      
      if(selectedTile.isOccupied()) {
         selectedUnit = selectedTile.getOccupiedBy();
         unitSelected = true;
         if(selectedUnit.getOwner().equals(currentTeam)) {
            moveSpots = world.calcMoveSpots(selectedUnit);
            if(!selectedUnit.hasMoved())
               state = GAME_STATE.DISPLAY_MOVE;
         }
      }
      else if(selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory)selectedTile).getOwner().equals(currentTeam)) {
         unitSelected = false;
         Object[] possibilities = ((Factory)selectedTile).dialogBoxList();
         Object s = JOptionPane.showInputDialog(this, "Choose unit to produce:\n" + "Current resource points: " + currentTeam.getResources(),
               "Factory", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
         if(s != null) {
            String unitType = ((String)s).split(",")[0];
            if(unitType.equals("Tank"))
               ((Factory)selectedTile).produceUnit(UNIT_TYPE.TANK);
            else if(unitType.equals("Tank Destroyer"))
               ((Factory)selectedTile).produceUnit(UNIT_TYPE.TANK_DEST);
            else if(unitType.equals("Infantry"))
               ((Factory)selectedTile).produceUnit(UNIT_TYPE.INFANTRY);
         }
      }
      if(!unitSelected) {
         selectedUnit = null;
      }
      System.out.println("State: " + state);
      System.out.println("Selected unit: " + selectedUnit);
      update(this.getGraphics());
   }
   
   public void drawMoveSpots(Graphics g) {
      Color moveSet = new Color(0, 0, 255, 165);
      Color attackSet = new Color(255, 0, 0, 165);
      Color captureSet = new Color(255, 255, 0, 165);
      
      for(int row = 0; row < gameHeight; row++)
         for(int col = 0; col < gameWidth; col++) {
            if(state == GAME_STATE.DISPLAY_MOVE) {
               if(moveSpots[row][col] == 1) {
                  g.setColor(moveSet);
                  g.fillRect((col * tileSize) + offsetHorizontal, (row * tileSize) + offsetVertical, tileSize, tileSize);
               }
               else if(moveSpots[row][col] == 2) {
                  g.setColor(attackSet);
                  g.fillRect((col * tileSize) + offsetHorizontal, (row * tileSize) + offsetVertical, tileSize, tileSize);
               }
               else if(moveSpots[row][col] == 3) {
                  g.setColor(captureSet);
                  g.fillRect((col * tileSize) + offsetHorizontal, (row * tileSize) + offsetVertical, tileSize, tileSize);
               }
            }
         }
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
   
   public static void delay(int n) {
      long startDelay = System.currentTimeMillis();
      long endDelay = 0;
      while(endDelay - startDelay < n)
         endDelay = System.currentTimeMillis();
   }
   
   @Override
   public void mouseClicked(MouseEvent arg0) {
      System.out.println("Click.");
      int eventX = arg0.getX();
      int eventY = arg0.getY();
      for(int row = 0; row < gameHeight; row++)
         for(int col = 0; col < gameWidth; col++)
            if(grid[row][col].contains(eventX, eventY)) {
               gameLoop(col, row);
               return;
            }
      switchTeams();
   }
   
   @Override
   public void mouseEntered(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }
   
   @Override
   public void mouseExited(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }
   
   @Override
   public void mousePressed(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }
   
   @Override
   public void mouseReleased(MouseEvent arg0) {
      // TODO Auto-generated method stub
      
   }
   
   public World getWorld() {
      return world;
   }
   
   public void setWorld(World world) {
      this.world = world;
   }
   
   @Override
   public void actionPerformed(ActionEvent arg0) {
      // TODO Auto-generated method stub
      System.out.println(arg0.toString());
      
   }
   
}
