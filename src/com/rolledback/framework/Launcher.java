package com.rolledback.framework;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Launcher {
   
   static Game newGame;
   
   public static void main(String args[]) {
      Logger.consolePrint("Getting map files.", "launcher");
      File directory = new File("maps");
      // if you are editing in eclipse, looks at files in the map folder in the workspace
      String[] files = directory.list();
      // if you are running from a jar, looks at files in the map folder in same dir as the jar
      if(files == null)
         files = new File(System.getProperty("user.dir") + "maps\\").list();
      
      ArrayList<Object> choices = new ArrayList<Object>();
      for(int f = 0; f < files.length; f++) {
         if(files[f].endsWith(".map")) {
            int size = getTileSize(directory + "\\" + files[f]);
            if(size != -1)
               choices.add(files[f].substring(0, files[f].lastIndexOf(".")) + ": (" + size + "x" + size + ")");
         }
      }
      
      choices.add("128x128 Random");
      choices.add("64x64 Random");
      choices.add("32x32 Random");
      choices.add("16x16 Random");
      choices.add("8x8 Random");
      choices.add("4x4 Random");
      choices.add("2x2 Random");
      choices.add("Random Size");
      
      Object[] possibilities = choices.toArray();
      int tileSize = -1;
      String fileToLoad = "";
      if(tileSize == -1) {
         Object s = JOptionPane.showInputDialog(new JPanel(), "Choose map:", "Tile Size", JOptionPane.PLAIN_MESSAGE, null, possibilities,
               possibilities[0]);
         if(s != null) {
            String size = (String)s;
            Logger.consolePrint(size, "launcher");
            if(size.equals("Random Size")) {
               int[] sizes = { 8, 16, 32, 64, 128 };
               tileSize = sizes[new Random().nextInt(sizes.length)];
            }
            else if(size.contains(":")) {
               fileToLoad = directory + "\\" + size.substring(0, size.lastIndexOf(":")) + ".map";
               tileSize = getTileSize(fileToLoad);
            }
            else
               tileSize = Integer.parseInt(size.split("x")[0]);
         }
      }
      if(tileSize == -1)
         System.exit(-1);
      int[] dimensions = autoCalcDimensions(tileSize);
      init(dimensions[0], dimensions[1], tileSize, fileToLoad);
         
   }
   
   public static int getTileSize(String name) {
      BufferedInputStream mapReader;
      byte[] map = new byte[5];
      try {
         mapReader = new BufferedInputStream(new FileInputStream(name));
         mapReader.read(map);
         mapReader.close();
      }
      catch(IOException e) {
         return -1;
      }
      if(map[0] != 0x6d)
         return -1;
      return Math.abs(map[1]);
   }
   
   public static int[] autoCalcDimensions(int size) {
      int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
      int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
      
      screenHeight -= (int)((double)screenHeight / 10);
      screenWidth -= (int)((double)screenWidth / 10);
      
      while(screenWidth % 64 != 0 || screenWidth % 32 != 0 || screenWidth % 128 != 0 || screenWidth % 16 != 0)
         screenWidth--;
      while(screenHeight % 64 != 0 || screenHeight % 32 != 0 || screenHeight % 128 != 0 || screenHeight % 16 != 0)
         screenHeight--;
      
      int[] d = { screenWidth / size, screenHeight / size };
      
      return d;
   }
   
   public static void init(int x, int y, int size, String fileName) {
      int tileSize = size;
      
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
      
      int gameWidth = x;
      int gameHeight = y;
      int guiHeight = 0;
      
      GraphicsManager manager = new GraphicsManager();
      JFrame frame;
      int winner[] = { 0, 0 };
      for(int i = 0; i < 1; i++) {
         Logger.consolePrint("Constructing frame.", "launcher");
         frame = new JFrame("TBS2");
         Logger.consolePrint("Game " + i, "launcher");
         long start = System.currentTimeMillis();
         int offsetHorizontal = screenWidth - (gameWidth * tileSize);
         int offsetVertical = screenHeight - guiHeight - (gameHeight * tileSize);
         Logger.consolePrint("Constructing game.", "launcher");
         newGame = new Game(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2, guiHeight, manager, fileName);
         newGame.setDoubleBuffered(true);
         newGame.setIgnoreRepaint(true);
         newGame.setSize(screenWidth, screenHeight);
         frame.getContentPane().add(newGame);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         
         frame.setVisible(true);
         frame.setSize(screenWidth + frame.getInsets().right + frame.getInsets().left, screenHeight + frame.getInsets().top
               + frame.getInsets().bottom);
         newGame.setVisible(true);
         Logger.consolePrint("Running game.", "launcher");
         newGame.run();
         long end = System.currentTimeMillis();
         if(newGame.winner.equals(newGame.getWorld().getTeamOne()))
            winner[0]++;
         else
            winner[1]++;
         Logger.consolePrint(Arrays.toString(winner) + " " + (end - start), "launcher");
//         try {
//            PrintStream out = new PrintStream(new FileOutputStream("dump.txt", true));
//            for(Coordinate c: newGame.history)
//               out.println(c.getX() + " " + c.getY());
//            out.close();
//         }
//         catch(FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//         }
//         Logger.consolePrint("Click history saved.", "launcher");
         newGame = null;
         frame.dispose();
         frame = null;
         Logger.consolePrint("Next game.", "launcher");
      }
      System.exit(-1);
   }
   
}
