package com.rolledback.mapping;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.Launcher;
import com.rolledback.teams.Team;
import com.rolledback.terrain.Bridge;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Forest;
import com.rolledback.terrain.Mountain;
import com.rolledback.terrain.Plain;
import com.rolledback.terrain.River;
import com.rolledback.terrain.Tile;

public class MapEditor extends JPanel implements MouseListener {
   
   private static final long serialVersionUID = 1L;
   public static JFrame window;
   
   public static void main(String args[]) {
      int tileSize = 128;

      int[] dimensions = Launcher.autoCalcDimensions(tileSize);
      init(dimensions[0], dimensions[1]);
   }
   
   public static void init(int x, int y) {
      window = new JFrame("Map Editor");
      
      int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
      int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
      
      screenHeight -= (int)((double)screenHeight / 10);
      screenWidth -= (int)((double)screenWidth / 10);
      
      while(screenWidth % 64 != 0 || screenWidth % 32 != 0 || screenWidth % 128 != 0 || screenWidth % 16 != 0)
         screenWidth--;
      while(screenHeight % 64 != 0 || screenHeight % 32 != 0 || screenHeight % 128 != 0 || screenHeight % 16 != 0)
         screenHeight--;
      
      int gameWidth = x;
      int gameHeight = y;
      int guiHeight = 0;
      
      int tileSize = 128;
      while((gameWidth * tileSize > screenWidth || gameHeight * tileSize > screenHeight - guiHeight) && tileSize >= 1) {
         tileSize /= 2;
      }
      if(tileSize < 1) {
         System.out.println("Bad dimensions.");
         System.out.println("Final width attempted: " + (gameWidth * 16) + " w/screen width: " + screenWidth);
         if(gameWidth * 16 > screenWidth)
            System.out.println("Make game less wide.");
         System.out.println("Final height attempted: " + (gameHeight * 16) + " w/screen height: " + screenHeight);
         if(gameHeight * 16 > screenHeight)
            System.out.println("Make game less tall.");
         System.exit(-1);
      }

      MapEditor editor = new MapEditor(x, y, tileSize);
      editor.setDoubleBuffered(true);
      editor.setSize(screenWidth, screenHeight);
      window.getContentPane().add(editor);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.setResizable(false);
      window.setVisible(true);
      window.setSize(screenWidth + window.getInsets().right + window.getInsets().left, screenHeight + window.getInsets().top
            + window.getInsets().bottom);
      window.setVisible(true);
      window.setLocation(75, 75);
   }
   
   public int width;
   public int height;
   public int tileSize;
   public Tile[][] tiles;
   public Image currentTexture;
   public GraphicsManager manager;
   public Rectangle[][] grid;
   public TextureOptionPane texturePicker;
   public Team dummyTeam;
   
   public MapEditor(int x, int y, int t) {
      dummyTeam = new Team("dummy", 0, 0);
      width = x;
      height = y;
      tileSize = t;
      manager = new GraphicsManager();
      manager.initTileImages();
      currentTexture = manager.tileTextures.get("grass.png");
      setVisible(true);
      
      setDoubleBuffered(true);
      
      String[] availTextures = new String[0];
      availTextures = manager.tileTextures.keySet().toArray(availTextures);
           
      texturePicker = new TextureOptionPane(manager, availTextures);
      texturePicker.setVisible(true);
      
      grid = new Rectangle[height][width];
      tiles = new Tile[height][width];
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            tiles[row][col] = new Plain(null, col, row, currentTexture);
            grid[row][col] = new Rectangle(col * tileSize, row * tileSize, tileSize, tileSize);
         }
      }
      addMouseListener(this);
   }
   
   public void paintComponent(Graphics g) {
      System.out.println("repaint");
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            Tile currTile = tiles[row][col];
            g.drawImage(currTile.getTexture(), tileSize * col, tileSize * row, tileSize, tileSize, this);
            g.drawRect(tileSize * col, tileSize * row, tileSize, tileSize);
         }
      }
   }
   
   @Override
   public void mouseClicked(MouseEvent arg0) {
      if(SwingUtilities.isLeftMouseButton(arg0)) {
         int eventX = arg0.getX();
         int eventY = arg0.getY();
         for(int row = 0; row < height; row++)
            for(int col = 0; col < width; col++) {
               if(grid[row][col].contains(eventX, eventY)) {
                  currentTexture = manager.tileTextures.get(texturePicker.currTexture);
                  System.out.println(texturePicker.currTexture.toLowerCase());
                  if(texturePicker.currTexture.toLowerCase().contains("city"))
                     tiles[row][col] = new City(null, col, row, dummyTeam, currentTexture);
                  else if(texturePicker.currTexture.toLowerCase().contains("factory"))
                     tiles[row][col] = new Factory(null, col, row, dummyTeam, currentTexture);
                  else if(texturePicker.currTexture.toLowerCase().contains("river"))
                     tiles[row][col] = new River(null, col, row, currentTexture);
                  else if(texturePicker.currTexture.toLowerCase().contains("mountain"))
                     tiles[row][col] = new Mountain(null, col, row, currentTexture);
                  else if(texturePicker.currTexture.toLowerCase().contains("grass"))
                     tiles[row][col] = new Plain(null, col, row, currentTexture);
                  else if(texturePicker.currTexture.toLowerCase().contains("bridge"))
                     tiles[row][col] = new Bridge(null, col, row, currentTexture);
                  else if(texturePicker.currTexture.toLowerCase().contains("forest"))
                     tiles[row][col] = new Forest(null, col, row, currentTexture);
                  repaint();
               }
               System.out.println(tiles[row][col]);
            }
      }
      else {
         System.out.println("Saving map");
         Cartographer.createMapFile(tiles, manager);
      }
      return;
   }
   
   @Override
   public void mouseEntered(MouseEvent e) {
      // TODO Auto-generated method stub
      
   }
   
   @Override
   public void mouseExited(MouseEvent e) {
      // TODO Auto-generated method stub
      
   }
   
   @Override
   public void mousePressed(MouseEvent e) {
      // TODO Auto-generated method stub
      
   }
   
   @Override
   public void mouseReleased(MouseEvent e) {
      // TODO Auto-generated method stub
      
   }
   
}
