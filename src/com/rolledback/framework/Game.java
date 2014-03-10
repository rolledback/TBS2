package com.rolledback.framework;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.rolledback.teams.ComputerTeam;
import com.rolledback.teams.ComputerTeamD;
import com.rolledback.teams.Team;
import com.rolledback.teams.Technology;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.DIRECTION;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Game extends JPanel implements MouseListener, ActionListener {
   
   public enum GAME_STATE {
      NORMAL, DISPLAY_MOVE, UPDATE, SWITCH_TEAMS, END_GAME
   }
   
   ArrayList<Coordinate> history = new ArrayList<Coordinate>();
   
   private static final long serialVersionUID = 1L;
   public int gameWidth, gameHeight, teamSize, tileSize, offsetHorizontal, offsetVertical, guiHeight, selectedX, selectedY;
   public Team teamOne, teamTwo;
   
   Team currentTeam;
   public Team winner;
   private World world;
   
   boolean unitSelected, ready;
   Tile selectedTile;
   Unit selectedUnit;
   Unit targetUnit;
   Rectangle[][] grid;
   GAME_STATE state;
   GraphicsManager manager;
   
   int UNIT_DENSITY = 5;
   int drawDetectorOne = 0;
   int drawDetectorTwo = 0;
   
   public ReentrantLock logicLock;
   
   public Game(int x, int y, int ts, int oH, int oV, int gH, GraphicsManager m, String fileToLoad) {
      gameWidth = x;
      gameHeight = y;
      manager = m;
      
      logicLock = new ReentrantLock();
      // teamSize = (gameWidth / 5) * (gameHeight / UNIT_DENSITY);
      teamOne = new Team("team one", 50, 125);
      teamTwo = new ComputerTeamD("team two", 50, 125, this);
      currentTeam = teamOne;
      
      teamOne.setOpponent(teamTwo);
      teamTwo.setOpponent(teamOne);
      
      world = new World(gameWidth, gameHeight, teamOne, teamTwo, manager, ts, fileToLoad);
      tileSize = ts;
      offsetHorizontal = oH;
      offsetVertical = oV;
      addMouseListener(this);
      state = GAME_STATE.UPDATE;
      
      grid = new Rectangle[y][x];
      for(int row = 0; row < gameHeight; row++)
         for(int col = 0; col < gameWidth; col++)
            grid[row][col] = new Rectangle((col * tileSize) + offsetHorizontal, (row * tileSize) + offsetVertical, tileSize, tileSize);
      this.setBackground(Color.black);
      
      guiHeight = gH;
      
      selectedX = 0;
      selectedY = 0;
   }
   
   public void run() {
      while(state != GAME_STATE.END_GAME) {
         if(state != GAME_STATE.END_GAME && currentTeam instanceof ComputerTeam) {
            ((ComputerTeam)currentTeam).executeTurn();
            state = GAME_STATE.SWITCH_TEAMS;
         }
         if(state == GAME_STATE.UPDATE || state == GAME_STATE.DISPLAY_MOVE)
            repaint();
         if(state == GAME_STATE.SWITCH_TEAMS) {
            if(!teamOne.isFirstTurn() && teamOne.getUnits().size() == 0) {
               winner = teamTwo;
               state = GAME_STATE.END_GAME;
            }
            else if(!teamTwo.isFirstTurn() && teamTwo.getUnits().size() == 0) {
               winner = teamOne;
               state = GAME_STATE.END_GAME;
            }
            else {
               if(currentTeam.isFirstTurn())
                  currentTeam.setFirstTurn(false);
               switchTeams();
            }
         }
      }
   }
   
   public void paintComponent(Graphics g) {
      logicLock.lock();
      drawTiles(g);
      drawUnits(g);
      drawHealthBars(g);
      if(state == GAME_STATE.DISPLAY_MOVE) {
         drawMoveSpots(g);
      }
      g.setColor(Color.black);
      g.drawOval(selectedX * tileSize + offsetHorizontal, selectedY * tileSize + offsetVertical, tileSize, tileSize);
      if(unitSelected && !selectedUnit.getOwner().equals(currentTeam)) {
         selectedUnit = null;
         unitSelected = false;
      }
      // drawGui(g);
      logicLock.unlock();
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
         tileColor = Color.LIGHT_GRAY;
      else if(world.getTiles()[selectedY][selectedX].getType() == TILE_TYPE.RIVER)
         tileColor = new Color(41, 32, 132);
      else if(world.getTiles()[selectedY][selectedX].getType() == TILE_TYPE.BRIDGE)
         tileColor = new Color(128, 128, 0);
      else if(world.getTiles()[selectedY][selectedX].getType() == TILE_TYPE.CITY) {
         if(((City)world.getTiles()[selectedY][selectedX]).getOwner() == null)
            tileColor = Color.MAGENTA;
         else if(((City)world.getTiles()[selectedY][selectedX]).getOwner().equals(teamOne))
            tileColor = Color.orange;
         else
            tileColor = Color.cyan;
      }
      else {
         if(((Factory)world.getTiles()[selectedY][selectedX]).getOwner().equals(teamOne))
            tileColor = Color.red;
         else
            tileColor = Color.blue;
      }
      g.setColor(tileColor);
      g.fillRect(32, this.getHeight() - guiHeight + 32, 128, 128);
      
      g.setColor(Color.black);
      Font font = new Font("Arial", Font.BOLD, 12);
      g.setFont(font);
      g.drawString("Current Tile: " + world.getTiles()[selectedY][selectedX].getType(), 102, this.getHeight() - guiHeight + 45);
      g.drawString("Attack Bonus: " + world.getTiles()[selectedY][selectedX].getEffect().getAttackBonus(), 102, this.getHeight() - guiHeight + 62);
      g.drawString("Defense Bonus: " + world.getTiles()[selectedY][selectedX].getEffect().getDefenseBonus(), 102, this.getHeight() - guiHeight + 79);
      g.drawString("Move Bonus: " + world.getTiles()[selectedY][selectedX].getEffect().getMoveBonus(), 102, this.getHeight() - guiHeight + 96);
      g.drawRect(32, this.getHeight() - guiHeight + 32, 128, 128);
      for(int x = 0; x < 5; x++)
         g.drawRect(24 + x, this.getHeight() - guiHeight + 24 + x, 300 - (2 * x), 80 - (2 * x));
   }
   
   public void switchTeams() {
      Logger.consolePrint("switching teams", "game");
      unitSelected = false;
      selectedUnit = null;
      Iterator<Unit> i = currentTeam.getUnits().iterator();
      while(i.hasNext()) {
         Unit temp = i.next();
         temp.setMoved(false);
         temp.setAttacked(false);
      }
      
      if(teamOne.getUnits().size() == 1) {
         drawDetectorOne++;
         if(drawDetectorOne == 30) {
            teamOne.getUnits().clear();
            Logger.consolePrint("default win detected", "game");
         }
      }
      else
         drawDetectorOne = 0;
      
      if(teamTwo.getUnits().size() == 1) {
         drawDetectorTwo++;
         if(drawDetectorTwo == 30) {
            teamTwo.getUnits().clear();
            Logger.consolePrint("default win detected", "game");
         }
      }
      else
         drawDetectorTwo = 0;
      
      for(City c: currentTeam.getCities())
         c.produceResources();
      for(Factory f: currentTeam.getFactories())
         f.produceResources();
      
      if(currentTeam.equals(teamOne))
         currentTeam = teamTwo;
      else
         currentTeam = teamOne;
      
      state = GAME_STATE.UPDATE;
   }
   
   public void drawTiles(Graphics g) {
      for(int x = 0; x < gameWidth; x++)
         for(int y = 0; y < gameHeight; y++) {
            Tile currTile = world.getTiles()[y][x];
            g.drawImage(currTile.getTexture(), tileSize * x, tileSize * y, tileSize, tileSize, this);
         }
   }
   
   public void drawHeightMap(Graphics g) {
      for(int x = 0; x < gameWidth; x++) {
         for(int y = 0; y < gameHeight; y++) {
            Color tileColor;
            if(world.getTiles()[y][x].getType() == TILE_TYPE.RIVER || world.getTiles()[y][x].getType() == TILE_TYPE.BRIDGE)
               tileColor = Color.blue;
            else if(world.getHeightMap()[y][x] == 0)
               tileColor = Color.green;
            else if(world.getHeightMap()[y][x] == 1)
               tileColor = Color.yellow;
            else if(world.getHeightMap()[y][x] == 2)
               tileColor = Color.orange;
            else
               tileColor = Color.red;
            g.setColor(tileColor);
            g.fillRect((x * tileSize) + offsetHorizontal, (y * tileSize) + offsetVertical, tileSize, tileSize);
//            g.setColor(Color.black);
//            g.drawRect((x * tileSize) + offsetHorizontal, (y * tileSize) + offsetVertical, tileSize, tileSize);
         }
      }
   }
   
   public void drawUnits(Graphics g) {
      g.setColor(Color.black);
      Font font = new Font("Serif", Font.PLAIN, 22);
      g.setFont(font);
      for(Unit temp: teamOne.getUnits()) {
         UNIT_TYPE u = temp.getType();
         if(u == UNIT_TYPE.INFANTRY)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[0], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[4], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(u == UNIT_TYPE.TANK)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[2], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[6], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(u == UNIT_TYPE.TANK_DEST)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[3], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[7], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(u == UNIT_TYPE.RPG)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[1], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[5], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(temp.hasAttacked() || temp.hasMoved() && teamOne.equals(currentTeam)) {
            g.setColor(new Color(192, 192, 192, 135));
            g.fillRect(tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize);
         }
      }
      
      for(Unit temp: teamTwo.getUnits()) {
         UNIT_TYPE u = temp.getType();
         if(u == UNIT_TYPE.INFANTRY)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[8], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[12], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(u == UNIT_TYPE.TANK)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[10], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[14], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(u == UNIT_TYPE.TANK_DEST)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[11], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[15], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(u == UNIT_TYPE.RPG)
            if(temp.getDir() == DIRECTION.LEFT)
               g.drawImage(manager.unitImages[9], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
            else
               g.drawImage(manager.unitImages[13], tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize, this);
         if(temp.hasAttacked() || temp.hasMoved() && teamTwo.equals(currentTeam)) {
            g.setColor(new Color(192, 192, 192, 135));
            g.fillRect(tileSize * temp.getX(), tileSize * temp.getY(), tileSize, tileSize);
         }
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
   
   public void gameLogic(int xTile, int yTile) {
      logicLock.lock();
      int x = xTile; // click data
      int y = yTile; // click data
      selectedX = xTile;
      selectedY = yTile;
      selectedTile = selectTile(x, y);
      
      history.add(new Coordinate(x, y));
      
      if(unitSelected) {
         if(!selectedUnit.hasMoved() && !selectedUnit.hasAttacked() && selectedUnit.getAttackSet().contains(new Coordinate(x, y))) {
            targetUnit = world.getTiles()[y][x].getOccupiedBy();
            Logger.consolePrint("selected unit attacking: " + targetUnit, "game");
            attackMove(x, y);
            selectedUnit.attack(targetUnit, false);
            if(!targetUnit.isAlive()) {
               world.destroyUnit(targetUnit);
               Logger.consolePrint("target destroyed", "game");
               state = GAME_STATE.UPDATE;
            }
            else {
               targetUnit.attack(selectedUnit, true);
               if(!selectedUnit.isAlive()) {
                  world.destroyUnit(selectedUnit);
                  Logger.consolePrint("unit destroyed", "game");
                  state = GAME_STATE.UPDATE;
               }
            }
            selectedUnit.setMoved(true);
            selectedUnit.setAttacked(true);
         }
         if(!selectedUnit.hasMoved() && selectedUnit.getCaptureSet().contains(new Coordinate(x, y))) {
            Logger.consolePrint("capturing city at: (" + selectedTile.getX() + ", " + selectedTile.getY() + ")", "game");
            selectedUnit.move(selectedTile);
            selectedUnit.setMoved(true);
            if(((CapturableTile)selectedTile).getOwner() == null || !((CapturableTile)selectedTile).getOwner().equals(currentTeam)) {
               ((CapturableTile)selectedTile).capture(selectedUnit);
            }
         }
         if(!selectedUnit.hasMoved() && selectedUnit.getMoveSet().contains(new Coordinate(x, y))) {
            Logger.consolePrint("moving selected unit to: (" + selectedTile.getX() + ", " + selectedTile.getY() + ")", "game");
            selectedUnit.move(selectedTile);
            selectedUnit.setMoved(true);
         }
         unitSelected = false;
         state = GAME_STATE.UPDATE;
      }
      
      else if(selectedTile.isOccupied()) {
         selectedUnit = selectedTile.getOccupiedBy();
         unitSelected = true;
         if(selectedUnit.getOwner().equals(currentTeam)) {
            selectedUnit.calcMoveSpots();
            if(!selectedUnit.hasMoved())
               state = GAME_STATE.DISPLAY_MOVE;
         }
      }
      else if(selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory)selectedTile).getOwner().equals(currentTeam)) {
         unitSelected = false;
         Logger.consolePrint("factory selected", "game");
         FactoryOptionPane factoryPane = new FactoryOptionPane(currentTeam);
         if(factoryPane.isUnitChoiceMade()) {
            ((Factory)selectedTile).produceUnit(factoryPane.getReturnedUnit());
            Logger.consolePrint("producing " + factoryPane.getReturnedUnit(), "game");
         }
         else if(factoryPane.isTechChoiceMade()) {
            Technology.researchTech(currentTeam, factoryPane.getReturnedTech());
         }
         state = GAME_STATE.UPDATE;
      }
      if(!unitSelected) {
         selectedUnit = null;
         state = GAME_STATE.UPDATE;
      }
      logicLock.unlock();
   }
   
   public void drawMoveSpots(Graphics g) {
      Color moveColor = new Color(0, 128, 128, 135);
      Color attackColor = new Color(255, 0, 0, 135);
      Color captureColor = new Color(255, 225, 0, 135);
      g.setColor(moveColor);
      
      for(Coordinate c: selectedUnit.getMoveSet())
         g.fillRect((c.getX() * tileSize) + offsetHorizontal, (c.getY() * tileSize) + offsetVertical, tileSize, tileSize);
      
      g.setColor(attackColor);
      for(Coordinate c: selectedUnit.getAttackSet())
         g.fillRect((c.getX() * tileSize) + offsetHorizontal, (c.getY() * tileSize) + offsetVertical, tileSize, tileSize);
      
      g.setColor(captureColor);
      for(Coordinate c: selectedUnit.getCaptureSet())
         g.fillRect((c.getX() * tileSize) + offsetHorizontal, (c.getY() * tileSize) + offsetVertical, tileSize, tileSize);
      
   }
   
   public void attackMove(int x, int y) {
      if(Math.abs(selectedUnit.getX() - targetUnit.getX()) == 1 && targetUnit.getY() == selectedUnit.getY())
         return;
      if(Math.abs(selectedUnit.getY() - targetUnit.getY()) == 1 && targetUnit.getX() == selectedUnit.getX())
         return;
      else if(x - 1 >= 0 && selectedUnit.getMoveSet().contains(new Coordinate(x - 1, y))) {
         selectedUnit.move(world.getTiles()[y][x - 1]);
      }
      else if(x + 1 < gameWidth && selectedUnit.getMoveSet().contains(new Coordinate(x + 1, y))) {
         selectedUnit.move(world.getTiles()[y][x + 1]);
      }
      else if(y - 1 >= 0 && selectedUnit.getMoveSet().contains(new Coordinate(x, y - 1))) {
         selectedUnit.move(world.getTiles()[y - 1][x]);
      }
      else if(y + 1 < gameHeight && selectedUnit.getMoveSet().contains(new Coordinate(x, y + 1))) {
         selectedUnit.move(world.getTiles()[y + 1][x]);
      }
   }
   
   public void highLightTile(int x, int y, Color c) {
      Graphics g = this.getGraphics();
      g.setColor(c);
      g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
   }
   
   public Tile selectTile(int x, int y) {
      Tile selectedTile = world.getTiles()[y][x];
      return selectedTile;
   }
   
   public void gameDebugMini() {
      System.out.println("Unit selected: " + unitSelected);
      System.out.println(selectedUnit);
      System.out.println(selectedTile);
      System.out.println("Team one: " + teamOne.toString());
      System.out.println("Team two: " + teamTwo.toString());
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
      System.out.println("Team one resources: " + teamOne.getResources());
      System.out.println("Team one army size: " + teamOne.getUnits().size());
      System.out.println("----------------------------------");
      System.out.println("Team two: " + teamTwo.toString());
      System.out.println("Team two units: " + teamTwo.getUnits().toString());
      System.out.println("Team two resources: " + teamTwo.getResources());
      System.out.println("Team two army size: " + teamTwo.getUnits().size());
   }
   
   public static void delay(int n) {
      long startDelay = System.currentTimeMillis();
      long endDelay = 0;
      while(endDelay - startDelay < n)
         endDelay = System.currentTimeMillis();
   }
   
   @Override
   public void mouseClicked(MouseEvent arg0) {
      if(SwingUtilities.isLeftMouseButton(arg0)) {
         int eventX = arg0.getX();
         int eventY = arg0.getY();
         for(int row = 0; row < gameHeight; row++)
            for(int col = 0; col < gameWidth; col++)
               if(grid[row][col].contains(eventX, eventY)) {
                  gameLogic(col, row);
                  return;
               }
      }
      else
         state = GAME_STATE.SWITCH_TEAMS;
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
