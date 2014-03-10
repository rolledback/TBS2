package com.rolledback.framework;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Launcher {
   
   static Game newGame;
   
   public static void main(String args[]) {
      Object[] possibilities = { "128x128", "64x64", "32x32", "16x16", "8x8", "4x4", "2x2", "Random" };
      int tileSize = -1;
      if(tileSize == -1) {
         Object s = JOptionPane.showInputDialog(new JPanel(), "Choose tile size:\n(default 64x64)", "Tile Size", JOptionPane.PLAIN_MESSAGE, null,
               possibilities, possibilities[0]);
         if(s != null) {
            String size = (String)s;
            if(size.equals("Random")) {
               int[] sizes = { 8, 16, 32, 64, 128 };
               tileSize = sizes[new Random().nextInt(sizes.length)];
            }
            else
               tileSize = Integer.parseInt(size.split("x")[0]);
         }
      }
      if(tileSize == -1)
         tileSize = 64;
      int[] dimensions = autoCalcDimensions(tileSize);
      Logger.consolePrint(Arrays.toString(dimensions), "launcher");
      init(dimensions[0], dimensions[1], tileSize);
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
   
   public static void init(int x, int y, int size) { 
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
      int winner[] = { 0, 0 };
      for(int i = 0; i < 10000; i++) {
         JFrame frame = new JFrame("TBS2");
         frame.setTitle("TBS2 " + i + " " + Arrays.toString(winner));
         Logger.consolePrint("Game " + i, "launcher");
         long start = System.currentTimeMillis();
         int offsetHorizontal = screenWidth - (gameWidth * tileSize);
         int offsetVertical = screenHeight - guiHeight - (gameHeight * tileSize);
         newGame = new Game(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2, guiHeight, manager);
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
         newGame.run();
         long end = System.currentTimeMillis();
         if(newGame.winner.equals(newGame.getWorld().getTeamOne()))
            winner[0]++;
         else
            winner[1]++;
         // frame.setVisible(false);
         Logger.consolePrint(Arrays.toString(winner) + " " + (end - start), "launcher");
         System.out.println(i);
         try {
            PrintStream out = new PrintStream(new FileOutputStream("dump.txt", true));
            for(Coordinate c: newGame.history)
               out.println(c.getX() + " " + c.getY());
            out.close();
         }
         catch(FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         newGame = null;
      }
      System.exit(-1);
   }
   
}
