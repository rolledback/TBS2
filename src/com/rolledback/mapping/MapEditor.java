package com.rolledback.mapping;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.Launcher;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.terrain.Bridge;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Forest;
import com.rolledback.terrain.Mountain;
import com.rolledback.terrain.Plain;
import com.rolledback.terrain.River;
import com.rolledback.terrain.Tile;

public class MapEditor extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
   
   private static final long serialVersionUID = 1L;
   private static JFrame window;
   private int width;
   private int height;
   private int tileSize;
   private Tile[][] tiles;
   private Image currentTexture;
   private GraphicsManager manager;
   private Rectangle[][] grid;
   private TextureOptionPane texturePicker;
   private Team dummyTeam;
   private EscMenu menu;
   private boolean gridVisible;
   private int offsetHorizontal;
   private int offsetVertical;
   
   public static void main(String args[]) {
      Object[] possibilities = { "128x128", "64x64", "32x32", "16x16", "8x8", "4x4", "2x2", "Random", "Custom" };
      int tileSize = -1;
      int x = -1;
      int y = -1;
      if(tileSize == -1) {
         Object s = JOptionPane.showInputDialog(new JPanel(), "Choose tile size:", "Tile Size", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
         if(s != null) {
            String size = (String)s;
            if(size.equals("Random")) {
               int[] sizes = { 8, 16, 32, 64, 128 };
               tileSize = sizes[new Random().nextInt(sizes.length)];
            }
            else if(size.equals("Custom")) {
               JTextField xField = new JTextField(5);
               JTextField yField = new JTextField(5);
               
               JPanel myPanel = new JPanel();
               myPanel.add(new JLabel("x:"));
               myPanel.add(xField);
               myPanel.add(Box.createHorizontalStrut(15));
               myPanel.add(new JLabel("y:"));
               myPanel.add(yField);
               
               int result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter X and Y Values", JOptionPane.OK_CANCEL_OPTION);
               if(result == JOptionPane.OK_OPTION) {
                  x = Integer.parseInt(xField.getText());
                  y = Integer.parseInt(yField.getText());
               }
            }
            else
               tileSize = Integer.parseInt(size.split("x")[0]);
         }
      }
      if(x == -1) {
         int[] dimensions = Launcher.autoCalcDimensions(tileSize);
         init(dimensions[0], dimensions[1]);
      }
      else if(x != -1) {
         init(x, y);
      }
      else if(tileSize == -1)
         System.exit(-1);
   }
   
   public static void init(int x, int y) {
      // get the size of the screen
      int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
      int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
      
      // reduce the dimensions by 10%
      screenHeight -= (int)((double)screenHeight / 10);
      screenWidth -= (int)((double)screenWidth / 10);
      
      // further reduce them until divisible by 128, 64, 32, and 16
      while(screenWidth % 64 != 0 || screenWidth % 32 != 0 || screenWidth % 128 != 0 || screenWidth % 16 != 0)
         screenWidth--;
      while(screenHeight % 64 != 0 || screenHeight % 32 != 0 || screenHeight % 128 != 0 || screenHeight % 16 != 0)
         screenHeight--;
      
      int tileSize = 128;
      int gameWidth = x;
      int gameHeight = y;
      int guiHeight = 0;
      
      while((gameWidth * tileSize > screenWidth || gameHeight * tileSize > screenHeight - guiHeight) && tileSize >= 1) {
         tileSize /= 2;
      }
      
      int offsetHorizontal = screenWidth - (gameWidth * tileSize);
      int offsetVertical = screenHeight - guiHeight - (gameHeight * tileSize);
      
      window = new JFrame("Map Editor");
      MapEditor editor = new MapEditor(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2);
      editor.setDoubleBuffered(true);
      editor.setSize(screenWidth, screenHeight);
      window.getContentPane().add(editor);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.setResizable(false);
      window.setVisible(true);
      window.setSize(screenWidth + window.getInsets().right + window.getInsets().left, screenHeight + window.getInsets().top + window.getInsets().bottom);
      window.setVisible(true);
      window.setLocation(75, 75);
   }
   
   public MapEditor(int x, int y, int t, int oH, int oV) {
      setFocusable(true);
      menu = new EscMenu();
      dummyTeam = new Team("dummy", 0, 0);
      width = x;
      height = y;
      tileSize = t;
      offsetHorizontal = oH;
      offsetVertical = oV;
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
            grid[row][col] = new Rectangle(col * tileSize + offsetHorizontal, row * tileSize + offsetVertical, tileSize, tileSize);
         }
      }
      addMouseListener(this);
      addMouseMotionListener(this);
      addKeyListener(this);
   }
   
   public void paintComponent(Graphics g) {
      for(int row = 0; row < height; row++) {
         for(int col = 0; col < width; col++) {
            Tile currTile = tiles[row][col];
            g.drawImage(currTile.getTexture(), tileSize * col + offsetHorizontal, tileSize * row + offsetVertical, tileSize, tileSize, this);
            if(gridVisible)
               g.drawRect(tileSize * col + offsetHorizontal, tileSize * row + offsetVertical, tileSize, tileSize);
         }
      }
   }
   
   @Override
   public void mouseClicked(MouseEvent arg0) {
   }
   
   @Override
   public void mouseEntered(MouseEvent e) {
   }
   
   @Override
   public void mouseExited(MouseEvent e) {
   }
   
   @Override
   public void mousePressed(MouseEvent arg0) {
      if(SwingUtilities.isLeftMouseButton(arg0)) {
         int eventX = arg0.getX();
         int eventY = arg0.getY();
         for(int row = 0; row < height; row++)
            for(int col = 0; col < width; col++) {
               if(grid[row][col].contains(eventX, eventY)) {
                  currentTexture = manager.tileTextures.get(texturePicker.getCurrTexture());
                  if(texturePicker.getCurrTexture().toLowerCase().contains("city"))
                     tiles[row][col] = new City(null, col, row, dummyTeam, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("factory"))
                     tiles[row][col] = new Factory(null, col, row, dummyTeam, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("river"))
                     tiles[row][col] = new River(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("mountain"))
                     tiles[row][col] = new Mountain(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("grass"))
                     tiles[row][col] = new Plain(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("bridge"))
                     tiles[row][col] = new Bridge(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("forest"))
                     tiles[row][col] = new Forest(null, col, row, currentTexture);
                  repaint();
               }
            }
      }
      else {
         if(!texturePicker.isVisible()) {
            texturePicker.setVisible(true);
            texturePicker.setLocation(arg0.getX(), arg0.getY());
         }
      }
      return;
   }
   
   @Override
   public void mouseReleased(MouseEvent e) {
   }
   
   @Override
   public void keyPressed(KeyEvent e) {
      if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
         if(!menu.isVisible()) {
            menu.setVisible(true);
         }
         else
            menu.setVisible(false);
         if(menu.isOpenFile()) {
            Object[] results = Cartographer.readMapFile(menu.getFileName(), tiles, new World(), manager);
            boolean success = (boolean)results[0];
            if(success) {
               int[] dimensions = Launcher.getDimensions(menu.getFileName());
               tiles = (Tile[][])results[1];
               System.out.println(Arrays.toString(dimensions));
               width = dimensions[0];
               height = dimensions[1];
               tileSize = Launcher.getTileSize(menu.getFileName());
               offsetHorizontal = this.getWidth() - (width * tileSize);
               offsetVertical = this.getHeight() - 0 - (height * tileSize);
               
               grid = new Rectangle[height][width];
               for(int row = 0; row < height; row++)
                  for(int col = 0; col < width; col++)
                     grid[row][col] = new Rectangle(col * tileSize + offsetHorizontal, row * tileSize + offsetVertical, tileSize, tileSize);
               
               repaint();
            }
            else
               JOptionPane.showMessageDialog(new JFrame(), "Error opening map file.", "Error", JOptionPane.ERROR_MESSAGE);
         }
         else if(menu.isSaveFile()) {
            boolean success = Cartographer.createMapFile(menu.getFileName(), tiles, tileSize, manager);
            if(success)
               repaint();
            else
               JOptionPane.showMessageDialog(new JFrame(), "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
         }
      }
      if(e.getKeyChar() == 'r') {
         World temp = new World(manager, width, height, true);
         tiles = temp.getTiles();
         repaint();
      }
      if(e.getKeyChar() == 't') {
         World temp = new World(manager, width, height, false);
         tiles = temp.getTiles();
         repaint();
      }
      if(e.getKeyChar() == 'g') {
         gridVisible = !gridVisible;
         repaint();
      }
      if(e.getKeyChar() == 'f') {
         currentTexture = manager.tileTextures.get(texturePicker.getCurrTexture());
         for(int row = 0; row < height; row++)
            for(int col = 0; col < width; col++)
               if(texturePicker.getCurrTexture().toLowerCase().contains("city"))
                  tiles[row][col] = new City(null, col, row, dummyTeam, currentTexture);
               else if(texturePicker.getCurrTexture().toLowerCase().contains("factory"))
                  tiles[row][col] = new Factory(null, col, row, dummyTeam, currentTexture);
               else if(texturePicker.getCurrTexture().toLowerCase().contains("river"))
                  tiles[row][col] = new River(null, col, row, currentTexture);
               else if(texturePicker.getCurrTexture().toLowerCase().contains("mountain"))
                  tiles[row][col] = new Mountain(null, col, row, currentTexture);
               else if(texturePicker.getCurrTexture().toLowerCase().contains("grass"))
                  tiles[row][col] = new Plain(null, col, row, currentTexture);
               else if(texturePicker.getCurrTexture().toLowerCase().contains("bridge"))
                  tiles[row][col] = new Bridge(null, col, row, currentTexture);
               else if(texturePicker.getCurrTexture().toLowerCase().contains("forest"))
                  tiles[row][col] = new Forest(null, col, row, currentTexture);
         repaint();
      }
      
   }
   
   @Override
   public void keyReleased(KeyEvent e) {
   }
   
   @Override
   public void keyTyped(KeyEvent e) {
   }
   
   @Override
   public void mouseDragged(MouseEvent arg0) {
      if(SwingUtilities.isLeftMouseButton(arg0)) {
         int eventX = arg0.getX();
         int eventY = arg0.getY();
         for(int row = 0; row < height; row++)
            for(int col = 0; col < width; col++) {
               if(grid[row][col].contains(eventX, eventY)) {
                  currentTexture = manager.tileTextures.get(texturePicker.getCurrTexture());
                  if(texturePicker.getCurrTexture().toLowerCase().contains("city"))
                     tiles[row][col] = new City(null, col, row, dummyTeam, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("factory"))
                     tiles[row][col] = new Factory(null, col, row, dummyTeam, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("river"))
                     tiles[row][col] = new River(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("mountain"))
                     tiles[row][col] = new Mountain(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("grass"))
                     tiles[row][col] = new Plain(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("bridge"))
                     tiles[row][col] = new Bridge(null, col, row, currentTexture);
                  else if(texturePicker.getCurrTexture().toLowerCase().contains("forest"))
                     tiles[row][col] = new Forest(null, col, row, currentTexture);
                  repaint();
               }
            }
      }
      else {
         if(!texturePicker.isVisible()) {
            texturePicker.setVisible(true);
            texturePicker.setLocation(arg0.getX(), arg0.getY());
         }
      }
      return;
   }
   
   @Override
   public void mouseMoved(MouseEvent arg0) {
   }
   
}
