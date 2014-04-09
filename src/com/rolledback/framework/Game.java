package com.rolledback.framework;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.rolledback.teams.Team;
import com.rolledback.teams.ai.ComputerTeam;
import com.rolledback.teams.ai.ComputerTeamD;
import com.rolledback.teams.technology.Technology;
import com.rolledback.terrain.CapturableTile;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.DIRECTION;

/**
 * The Game class contains all logic for running and displaying the game. An extension of JPanel,
 * Game objects are meant to put inside of a JFrame. Each game is primarily made up of two team
 * objects (one or more of which may be instances of ComputerTeam) and a World object (which itself
 * contains important information regarding the tiles that make up the Game. Winning a game is
 * defined as eliminating all enemy units. Both the paintComponent and gameLogic functions are
 * critical sections, requiring the acquisition of the logicLock variable to execute. The main loop
 * for the game can be found in the run function, which executes various functions depending on the
 * value of the state variable.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Game extends JPanel implements MouseListener, KeyListener {
   
   public enum GAME_STATE {
      NORMAL,
      DISPLAY_MOVE,
      UPDATE,
      SWITCH_TEAMS,
      END_GAME
   }
   
   private ArrayList<Coordinate> clickHistory = new ArrayList<Coordinate>();
   private static final long serialVersionUID = 1L;
   private int gameWidth;
   private int gameHeight;
   private int teamSize;
   private int tileSize;
   private int offsetHorizontal;
   private int offsetVertical;
   private int selectedX;
   private int selectedY;
   private Team teamOne;
   private Team teamTwo;
   private Team currentTeam;
   private Team winner;
   private World world;
   private boolean unitSelected;
   private Tile selectedTile;
   private Unit selectedUnit;
   private Unit targetUnit;
   private Rectangle[][] grid;
   private GAME_STATE state;
   private Image[][] background;
   private GameGUI infoBox;
   private int numTurns;
   private ReentrantLock logicLock;
   
   /**
    * Constructor.
    * 
    * @param x number of tiles in the width (x) direction.
    * @param y number of tiles in the height (y) direction.
    * @param ts size of the tiles, in pixels.
    * @param oH horizontal offset, used when game doesn't fill entire screen horizontally.
    * @param oV vertical offset, used when game doesn't fill entire screen vertically.
    * @param fileToLoad if the user chose to load a map, this will be the file's name, empty string
    *           if not loading a file.
    * @param iB pointer to the GameGUI object used by the window in Launcher.java.
    */
   public Game(int x, int y, int ts, int oH, int oV, String fileToLoad, GameGUI iB) {
      gameWidth = x;
      gameHeight = y;
      
      logicLock = new ReentrantLock();
      // teamSize = (gameWidth / 5) * (gameHeight / UNIT_DENSITY);
      
      teamOne = new Team("team one", 50, 100, 1);
      teamTwo = new ComputerTeamD("team two", 50, 100, this, 2);
      
      currentTeam = teamOne;
      
      teamOne.setOpponent(teamTwo);
      teamTwo.setOpponent(teamOne);
      
      world = new World(gameWidth, gameHeight, teamOne, teamTwo, fileToLoad);
      tileSize = ts;
      offsetHorizontal = oH;
      offsetVertical = oV;
      state = GAME_STATE.UPDATE;
      
      grid = new Rectangle[y][x];
      for(int row = 0; row < gameHeight; row++)
         for(int col = 0; col < gameWidth; col++)
            grid[row][col] = new Rectangle((col * tileSize) + offsetHorizontal, (row * tileSize) + offsetVertical, tileSize, tileSize);
      this.setBackground(Color.black);
      
      selectedX = 0;
      selectedY = 0;
      
      infoBox = iB;
      
      setNumTurns(0);
      winner = null;
      
      setDoubleBuffered(true);
      setIgnoreRepaint(true);
      
      addMouseListener(this);
      addKeyListener(this);
   }
   
   /**
    * Main game loop. Runs as long as the GAME_STATE variable is not set to END_GAME Possible states
    * are END_GAME (game over), UPDATE (repaint needs to be called), SWITCH_TEAMS (time for teams to
    * be switched). Also sees if there is a winner.
    */
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
               setNumTurns(getNumTurns() + 1);
               switchTeams();
            }
         }
      }
   }
   
   /**
    * Redraws all game elements.
    * 
    * @param g
    */
   public void paintComponent(Graphics g) {
      logicLock.lock();
      drawBackground(g);
      drawTiles(g);
      drawUnits(g);
      drawHealthBars(g);
      if(state == GAME_STATE.DISPLAY_MOVE) {
         drawMoveSpots(g);
      }
      g.setColor(Color.black);
      g.drawOval(selectedX * tileSize + offsetHorizontal, selectedY * tileSize + offsetVertical, tileSize - 2, tileSize - 2);
      if(unitSelected && !selectedUnit.getOwner().equals(currentTeam)) {
         selectedUnit = null;
         unitSelected = false;
      }
      // drawGui(g);
      logicLock.unlock();
   }
   
   /**
    * Draws the background underneath the actual game tiles/units. All games, even if they fill up
    * the entire screen actually have a background.
    * 
    * @param g Graphics object, passed in by paintComponent
    */
   public void drawBackground(Graphics g) {
      int horizOffset = (offsetHorizontal % tileSize == 0) ? 0 : tileSize / 2;
      int vertiOffset = (offsetVertical % tileSize == 0) ? 0 : tileSize / 2;
      for(int r = 0; r < background.length; r++)
         for(int c = 0; c < background[0].length; c++) {
            g.setColor(new Color(185, 185, 250, 175));
            g.setColor(new Color(120, 100, 165, 175));
            g.drawImage(background[r][c], (tileSize * c) - horizOffset, (tileSize * r) - vertiOffset, tileSize, tileSize, this);
            g.fillRect((c * tileSize) - horizOffset, (r * tileSize) - vertiOffset, tileSize, tileSize);
         }
   }
   
   /**
    * Creates the background and stores it in the background matrix.
    */
   public void createBackground() {
      int w = (this.getWidth() + tileSize) / tileSize;
      int h = (this.getHeight() + tileSize) / tileSize;
      background = new Image[h][w];
      for(int r = 0; r < h; r++)
         for(int c = 0; c < w; c++) {
            double i = Math.random();
            if(i < .75)
               background[r][c] = GraphicsManager.getTileTextures().get("grass.png");
            else if(i < .95)
               background[r][c] = GraphicsManager.getTileTextures().get("forest.png");
            else
               background[r][c] = GraphicsManager.getTileTextures().get("mountain.png");
         }
   }
   
   /**
    * Switches teams, teamOne -> teamTwo or teamTwo -> teamOne.
    */
   public void switchTeams() {
      Logger.consolePrint("switching teams", "game");
      infoBox.updateInfo(teamOne, teamTwo);
      unitSelected = false;
      selectedUnit = null;
      Iterator<Unit> i = currentTeam.getUnits().iterator();
      while(i.hasNext()) {
         Unit temp = i.next();
         temp.setMoved(false);
         temp.setAttacked(false);
      }
      
      for(City c: currentTeam.getCities())
         c.produceResources();
      for(Factory f: currentTeam.getFactories())
         f.produceResources();
      
      if(currentTeam.equals(teamOne))
         currentTeam = teamTwo;
      else
         currentTeam = teamOne;
      
      state = GAME_STATE.UPDATE;
      setNumTurns(getNumTurns() + 1);
   }
   
   /**
    * Draws the game tiles.
    * 
    * @param g Graphics object, passed in by paintComponent
    */
   public void drawTiles(Graphics g) {
      for(int x = 0; x < gameWidth; x++)
         for(int y = 0; y < gameHeight; y++) {
            Tile currTile = world.getTiles()[y][x];
            g.drawImage(currTile.getTexture(), tileSize * x + offsetHorizontal, tileSize * y + offsetVertical, tileSize, tileSize, this);
         }
   }
   
   /**
    * Draws the height map. Red is higher than yellow, which is higher than green. Blue indicates a
    * river tile.
    * 
    * @param g Graphics object, passed in by paintComponent
    */
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
         }
      }
   }
   
   /**
    * Draws all units on the screen. If the units are facing left it grabs their left image, and
    * vice versa for right. Team one's units are drawn first, and then team two's.
    * 
    * @param g Graphics object, passed in by paintComponent
    */
   public void drawUnits(Graphics g) {
      g.setColor(Color.black);
      Font font = new Font("Serif", Font.PLAIN, 22);
      g.setFont(font);
      for(Unit temp: teamOne.getUnits()) {
         int xCorner = tileSize * temp.getX() + offsetHorizontal;
         int yCorner = tileSize * temp.getY() + offsetVertical;
         BufferedImage unitImage = (BufferedImage)temp.getTexture();
         if(temp.getDir() == DIRECTION.RIGHT)
            g.drawImage(unitImage, xCorner, yCorner, tileSize, tileSize, this);
         else {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-unitImage.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            unitImage = op.filter(unitImage, null);
            g.drawImage(unitImage, xCorner, yCorner, tileSize, tileSize, this);
         }
         if(temp.hasMoved() && teamOne.equals(currentTeam)) {
            g.setColor(new Color(192, 192, 192, 135));
            g.fillRect(xCorner, yCorner, tileSize, tileSize);
         }
         if(temp.hasAttacked() && teamOne.equals(currentTeam)) {
            g.setColor(new Color(212, 212, 212, 135));
            g.fillRect(xCorner, yCorner, tileSize, tileSize);
         }
      }
      
      for(Unit temp: teamTwo.getUnits()) {
         int xCorner = tileSize * temp.getX() + offsetHorizontal;
         int yCorner = tileSize * temp.getY() + offsetVertical;
         BufferedImage unitImage = (BufferedImage)temp.getTexture();
         if(temp.getDir() == DIRECTION.RIGHT)
            g.drawImage(unitImage, xCorner, yCorner, tileSize, tileSize, this);
         else {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-unitImage.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            unitImage = op.filter(unitImage, null);
            g.drawImage(unitImage, xCorner, yCorner, tileSize, tileSize, this);
         }
         if(temp.hasMoved() && teamOne.equals(currentTeam)) {
            g.setColor(new Color(192, 192, 192, 135));
            g.fillRect(xCorner, yCorner, tileSize, tileSize);
         }
         if(temp.hasAttacked() && teamOne.equals(currentTeam)) {
            g.setColor(new Color(212, 212, 212, 135));
            g.fillRect(xCorner, yCorner, tileSize, tileSize);
         }
      }
   }
   
   /**
    * Draws health bars for each unit. Health bars take up 80% (rounded) of tileSize in width, and
    * are 20% of the tileSize in height.
    * 
    * @param g Graphics object, passed in by paintComponent
    */
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
               g.fillRect(xCorner, yCorner, (int)((double)(tileSize - (2 * buffer)) * (double)((double)world.getTiles()[y][x].getOccupiedBy().getHealth() / (double)world.getTiles()[y][x]
                     .getOccupiedBy().getMaxHealth())), buffer);
            }
         }
      }
   }
   
   /**
    * Performs all the logic associated with the "clicking" of a tile. Critical section of the code.
    * Only allowed to execute if the EventQueue is not repainting the canvas. Handles selecting of
    * units, moving of units, production of factories, capturing of capturable tiles.
    * 
    * @param xTile the x index of the tile in the tiles matrix
    * @param yTile the y index of the tile in the tiles matrix
    */
   public void gameLogic(int xTile, int yTile) {
      logicLock.lock();
      int x = xTile; // click data
      int y = yTile; // click data
      selectedX = xTile;
      selectedY = yTile;
      selectedTile = selectTile(x, y);
      clickHistory.add(new Coordinate(x, y));
      
      if(unitSelected) {
         if(!selectedUnit.hasAttacked() && selectedUnit.getAttackSet().contains(new Coordinate(x, y))) {
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
            selectedUnit.calcMoveSpots(selectedUnit.hasMoved() && !selectedUnit.hasAttacked());
            if(!selectedUnit.hasMoved() || (selectedUnit.hasMoved() && !selectedUnit.hasAttacked()))
               state = GAME_STATE.DISPLAY_MOVE;
         }
      }
      else if(selectedTile.getType() == TILE_TYPE.FACTORY && ((Factory)selectedTile).getOwner() != null && ((Factory)selectedTile).getOwner().equals(currentTeam)) {
         unitSelected = false;
         Logger.consolePrint("factory selected", "game");
         FactoryOptionPane factoryPane = new FactoryOptionPane((Factory)selectedTile);
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
   
   /**
    * Draws the move, attack and capture spots of a selected unit. Move are blue, attack are red,
    * and capture are yellow.
    * 
    * @param g Graphics object, passed in by paintComponent
    */
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
   
   /**
    * Called when a unit is told to attack a unit at coordinates x, y (in relation to the tiles
    * matrix). Logic for choosing which spot to move the unit to go first below the target, then
    * above, then to the left, then to the right. If a player wants to choooe which spot to attack
    * from (if there are more than one) then they can move their unit then attack.
    * 
    * @param x x coordinate of the target in the tiles matrix
    * @param y y coordinate of the target in the tiles matrix
    */
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
   
   /**
    * Selects the tile at matrix coordinates x, y.
    * 
    * @param x x coordinate of the target in the tiles matrix
    * @param y y coordinate of the target in the tiles matrix
    * @return the tile located at x, y
    */
   public Tile selectTile(int x, int y) {
      Tile selectedTile = world.getTiles()[y][x];
      return selectedTile;
   }
   
   /**
    * Produces a small output to the console. DOES NOT use the Logger class. This will always output
    * no matter what.
    */
   public void gameDebugMini() {
      System.out.println("Unit selected: " + unitSelected);
      System.out.println(selectedUnit);
      System.out.println(selectedTile);
      System.out.println("Team one: " + teamOne.toString());
      System.out.println("Team two: " + teamTwo.toString());
   }
   
   /**
    * Produces a verbose debug output for the game. DOES NOT use the Logger class. This will always
    * output no matter what.
    */
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
   
   @Override
   public void mouseClicked(MouseEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   @Override
   public void mouseEntered(MouseEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   @Override
   public void mouseExited(MouseEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   /**
    * Handles clicking for the game. For left clicks, calculates the x and y value of the tile
    * clicked on, updates the GameGUI associated with the game, calls gameLogic, and once again
    * updates the GameGUI. On the event of a right click, the game state will be changed to
    * SWITCH_TEAMS which will cause the current team to be changed upon the next iteration of the
    * run loop.
    */
   @Override
   public void mousePressed(MouseEvent arg0) {
      if(SwingUtilities.isLeftMouseButton(arg0)) {
         int row = (arg0.getY() - offsetVertical) / tileSize;
         int col = (arg0.getX() - offsetHorizontal) / tileSize;
         if(row >= gameHeight || col >= gameWidth || row < 0 || col < 0)
            return;
         infoBox.updateInfo(world.getTiles()[row][col].getOccupiedBy(), world.getTiles()[row][col], teamOne, teamTwo);
         gameLogic(col, row);
         infoBox.updateInfo(world.getTiles()[row][col].getOccupiedBy(), world.getTiles()[row][col], teamOne, teamTwo);
         return;
      }
      else
         state = GAME_STATE.SWITCH_TEAMS;
   }
   
   @Override
   public void mouseReleased(MouseEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   @Override
   public void keyPressed(KeyEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   @Override
   public void keyReleased(KeyEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   @Override
   public void keyTyped(KeyEvent arg0) {
      // TODO Auto-generated method stub
   }
   
   public World getWorld() {
      return world;
   }
   
   public void setWorld(World world) {
      this.world = world;
   }
   
   public Team getTeamOne() {
      return teamOne;
   }
   
   public void setTeamOne(Team teamOne) {
      this.teamOne = teamOne;
   }
   
   public Team getTeamTwo() {
      return teamTwo;
   }
   
   public void setTeamTwo(Team teamTwo) {
      this.teamTwo = teamTwo;
   }
   
   public Team getWinner() {
      return winner;
   }
   
   public void setWinner(Team winner) {
      this.winner = winner;
   }
   
   public ReentrantLock getLogicLock() {
      return logicLock;
   }
   
   public void setLogicLock(ReentrantLock logicLock) {
      this.logicLock = logicLock;
   }
   
   public ArrayList<Coordinate> getClickHistory() {
      return clickHistory;
   }
   
   public void setClickHistory(ArrayList<Coordinate> clickHistory) {
      this.clickHistory = clickHistory;
   }
   
   public int getGameWidth() {
      return gameWidth;
   }
   
   public void setGameWidth(int gameWidth) {
      this.gameWidth = gameWidth;
   }
   
   public int getGameHeight() {
      return gameHeight;
   }
   
   public void setGameHeight(int gameHeight) {
      this.gameHeight = gameHeight;
   }
   
   public int getNumTurns() {
      return numTurns;
   }
   
   public void setNumTurns(int numTurns) {
      this.numTurns = numTurns;
   }
   
}
