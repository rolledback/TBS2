package com.rolledback.teams;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rolledback.framework.Coordinate;
import com.rolledback.framework.Game;
import com.rolledback.framework.GameGUI;
import com.rolledback.framework.Launcher;
import com.rolledback.framework.Logger;

public class Simulator {
   static private int winFractionHeight = 10;
   static private int winFractionWidth = 4;
   static private boolean watchSim = true;
   static private boolean printDump = false;
   
   public static void main(String args[]) {
      Logger.consolePrint("Getting map files.", "simulator");
      File directory = new File("maps");
      // if you are editing in eclipse, looks at files in the map folder in the
      // workspace
      String[] files = directory.list();
      // if you are running from a jar, looks at files in the map folder in same
      // dir as the jar
      if(files == null)
         files = new File(System.getProperty("user.dir") + "maps\\").list();
      
      ArrayList<Object> choices = new ArrayList<Object>();
      for(int f = 0; f < files.length; f++) {
         if(files[f].endsWith(".map")) {
            int[] size = (System.getProperty("os.name").equals("Linux")) ? getDimensions(directory + "/" + files[f]) : getDimensions(directory + "\\" + files[f]);
            if(size.length != 0)
               choices.add(files[f].substring(0, files[f].lastIndexOf(".")) + ": (" + size[0] + "x" + size[1] + ")");
         }
      }
      
      choices.add("128x128 pixel Tiles Random");
      choices.add("64x64 pixel Tiles Random");
      choices.add("32x32 pixel Tiles Random");
      choices.add("16x16 pixel Tiles Random");
      choices.add("8x8 pixel Tiles Random");
      choices.add("4x4 pixel Tiles Random");
      choices.add("2x2 pixel Tiles Random");
      choices.add("Random Tiles Size");
      choices.add("Custom Grid Size");
      
      Object[] possibilities = choices.toArray();
      int tileSize = -1;
      int x = -1;
      int y = -1;
      int[] dimensions = new int[0];
      String fileToLoad = "";
      if(tileSize == -1) {
         Object s = JOptionPane.showInputDialog(new JPanel(), "Choose map:", "simulator", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
         if(s != null) {
            String choice = (String)s;
            Logger.consolePrint("Combobox choice: " + choice, "simulator");
            if(choice.equals("Random Size")) {
               Logger.consolePrint("Choosing a random tile size.", "simulator");
               int[] sizes = { 8, 16, 32, 64, 128 };
               tileSize = sizes[new Random().nextInt(sizes.length)];
               dimensions = Launcher.autoCalcDimensions(tileSize);
            }
            else if(choice.equals("Custom Grid Size")) {
               JTextField xField = new JTextField(5);
               JTextField yField = new JTextField(5);
               int result = JOptionPane.DEFAULT_OPTION;
               try {
                  JPanel myPanel = new JPanel();
                  myPanel.add(new JLabel("x:"));
                  myPanel.add(xField);
                  myPanel.add(Box.createHorizontalStrut(15));
                  myPanel.add(new JLabel("y:"));
                  myPanel.add(yField);
                  result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter X and Y Values", JOptionPane.OK_CANCEL_OPTION);
                  if(result == JOptionPane.OK_OPTION) {
                     x = Integer.parseInt(xField.getText());
                     y = Integer.parseInt(yField.getText());
                     Logger.consolePrint("User input of: " + x + "x" + y + ".", "simulator");
                  }
               }
               catch(Exception e) {
                  JOptionPane.showMessageDialog(new JFrame(), "Invalid answer. Now closing.", "Error", JOptionPane.ERROR_MESSAGE);
                  System.exit(0);
               }
            }
            else if(choice.contains(":")) {
               Logger.consolePrint("File chosen.", "simulator");
               if(System.getProperty("os.name").equals("Linux"))
                  fileToLoad = directory + "/" + choice.substring(0, choice.lastIndexOf(":")) + ".map";
               else
                  fileToLoad = directory + "\\" + choice.substring(0, choice.lastIndexOf(":")) + ".map";
               dimensions = getDimensions(fileToLoad);
            }
            else {
               tileSize = Integer.parseInt(choice.split("x")[0]);
               dimensions = Launcher.autoCalcDimensions(tileSize);
            }
         }
      }
      if(x == -1 && dimensions.length == 2) {
         init(dimensions[0], dimensions[1], fileToLoad);
      }
      else if(x != -1 && dimensions.length == 0) {
         init(x, y, fileToLoad);
      }
      else if(tileSize == -1 || dimensions.length == 0)
         System.exit(-1);
   }
   
   public static int[] getDimensions(String name) {
      BufferedInputStream mapReader;
      byte[] map = new byte[6];
      try {
         mapReader = new BufferedInputStream(new FileInputStream(name));
         mapReader.read(map);
         mapReader.close();
      }
      catch(IOException e) {
         return new int[0];
      }
      if(map[0] != 0x6d)
         return new int[0];
      int width = map[2] ^ (map[3] << 8);
      int height = map[4] ^ (map[5] << 8);
      int[] ret = new int[2];
      ret[0] = width;
      ret[1] = height;
      return ret;
   }
   
   public static int[] autoCalcDimensions(int size) {
      int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
      int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
      
      screenHeight -= (int)((double)screenHeight / winFractionHeight);
      screenWidth -= (int)((double)screenWidth / winFractionWidth);
      
      // create a dummy frame and GUI to determine the real GUI's eventual size on current display
      JFrame window = new JFrame("TBS 2");
      window.setResizable(false);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.getContentPane().setLayout(new BorderLayout());
      
      GameGUI infoBox = new GameGUI();
      window.add(infoBox, BorderLayout.SOUTH);
      window.pack();
      
      int guiHeight = infoBox.getHeight();
      Logger.consolePrint("Dummy gui dimensions: " + infoBox.getSize(), "simulator");
      window.dispose();
      screenHeight -= guiHeight;
      
      while(screenWidth % 64 != 0 || screenWidth % 32 != 0 || screenWidth % 128 != 0 || screenWidth % 16 != 0)
         screenWidth--;
      while(screenHeight % 64 != 0 || screenHeight % 32 != 0 || screenHeight % 128 != 0 || screenHeight % 16 != 0)
         screenHeight--;
      
      int[] d = { screenWidth / size, screenHeight / size };
      return d;
   }
   
   public static void init(int x, int y, String fileName) {
      int[] winners = new int[2];
      for(int i = 0; i < 100000; i++) {
         Logger.consolePrint("Init'ing with (" + x + ", " + y + ").", "simulator");
         
         // get the size of the screen
         int gamePanelHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
         int gamePanelWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
         Logger.consolePrint("Screen resolution: " + gamePanelWidth + "x" + gamePanelHeight, "simulator");
         
         // reduce the dimensions
         gamePanelHeight -= (int)((double)gamePanelHeight / winFractionHeight);
         gamePanelWidth -= (int)((double)gamePanelWidth / winFractionWidth);
         Logger.consolePrint("Initial reduction resulting in screen size of: " + gamePanelWidth + "x" + gamePanelHeight, "simulator");
         
         // create the game window
         Logger.consolePrint("Constructing game window.", "simulator");
         JFrame window = new JFrame("TBS 2 " + Arrays.toString(winners));
         window.setResizable(false);
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         window.getContentPane().setLayout(new BorderLayout());
         
         // create and add the GUI to the game window
         GameGUI infoBox = new GameGUI();
         window.add(infoBox, BorderLayout.SOUTH);
         window.pack();
         
         // remove how tall the GUI is from the gamePanel's height
         int guiHeight = infoBox.getHeight();
         gamePanelHeight -= guiHeight;
         infoBox.fixComponents();
         Logger.consolePrint("Intial gui dimensions: " + infoBox.getSize(), "simulator");
         
         // further reduce the dimensions until divisible by 128, 64, 32, and 16
         while(gamePanelWidth % 64 != 0 || gamePanelWidth % 32 != 0 || gamePanelWidth % 128 != 0 || gamePanelWidth % 16 != 0)
            gamePanelWidth--;
         while(gamePanelHeight % 64 != 0 || gamePanelHeight % 32 != 0 || gamePanelHeight % 128 != 0 || gamePanelHeight % 16 != 0)
            gamePanelHeight--;
         Logger.consolePrint("Final reduction resulting in panel size of: " + gamePanelWidth + "x" + gamePanelHeight, "simulator");
         
         // find out what tile size will first fit the given width and height (x, y)
         int tileSize = 128;
         int gameWidth = x;
         int gameHeight = y;
         
         while((gameWidth * tileSize > gamePanelWidth || gameHeight * tileSize > gamePanelHeight) && tileSize >= 1)
            tileSize /= 2;
         Logger.consolePrint("Tile size: " + tileSize, "simulator");
         
         // calculate the offset
         int offsetHorizontal = gamePanelWidth - (gameWidth * tileSize);
         int offsetVertical = gamePanelHeight - (gameHeight * tileSize);
         
         // initialize the game and add it to the window
         Game gamePanel = new Game(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2, fileName, infoBox);
         gamePanel.setSize(gamePanelWidth, gamePanelHeight);
         gamePanel.createBackground();
         window.add(gamePanel, BorderLayout.CENTER);
         infoBox.updateInfo(null, gamePanel.getWorld().getTiles()[0][0], gamePanel.teamOne, gamePanel.teamTwo);
         
         // set the game size and finish up creating the window
         gamePanel.setPreferredSize(new Dimension(gamePanelWidth, gamePanelHeight));
         gamePanel.setSize(gamePanel.getPreferredSize());
         Logger.consolePrint("Resizing using rules for OS: " + System.getProperty("os.name"), "simulator");
         if(System.getProperty("os.name").equals("Linux"))
            window.setSize(gamePanelWidth, gamePanelHeight + guiHeight);
         else {
            Logger.consolePrint("Inset left = " + window.getInsets().left, "simulator");
            Logger.consolePrint("Inset right = " + window.getInsets().right, "simulator");
            Logger.consolePrint("Inset top = " + window.getInsets().top, "simulator");
            Logger.consolePrint("Inset bottom = " + window.getInsets().bottom, "simulator");
            window.setSize(gamePanelWidth + window.getInsets().right + window.getInsets().left, gamePanelHeight + window.getInsets().top + window.getInsets().bottom + infoBox.getHeight());
         }
         if(watchSim)
            window.setVisible(true);
         Logger.consolePrint("Final window dimensions: " + window.getSize(), "simulator");
         Logger.consolePrint("Final game panel dimensions: " + gamePanel.getSize(), "simulator");
         Logger.consolePrint("Final gui dimensions: " + infoBox.getSize(), "simulator");
         
         // setup complete, run the game
         Logger.consolePrint("Running game.", "simulator");
         gamePanel.run();
         if(gamePanel.winner.equals(gamePanel.teamOne)) 
            winners[0]++;
         else
            winners[1]++;
         if(!window.isVisible()) {
            Logger.consolePrint(Arrays.toString(winners), "simulator");
         }
         
         if(printDump)
            try {
               PrintStream out = new PrintStream(new FileOutputStream("dump.txt", true));
               for(Coordinate c: gamePanel.history)
                  out.println(c.getX() + " " + c.getY());
               out.close();
            }
            catch(FileNotFoundException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
         window.dispose();
      }
   }
}
