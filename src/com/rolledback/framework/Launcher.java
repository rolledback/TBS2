package com.rolledback.framework;

import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Launcher {
   
   static Game newGame;
   
   public static void main(String args[]) {
      Object[] possibilities = { "128x128", "64x64", "32x32", "16x16", "8x8", "Random" };
      int tileSize = 64;
      if(tileSize == -1) {
         Object s = JOptionPane.showInputDialog(new JPanel(), "Choose tile size:\n(default 64x64)", "Tile Size", JOptionPane.PLAIN_MESSAGE, null,
               possibilities, possibilities[0]);
         if(s != null) {
            String size = (String)s;
            if(size.equals("128x128"))
               tileSize = 128;
            else if(size.equals("64x64"))
               tileSize = 64;
            else if(size.equals("32x32"))
               tileSize = 32;
            else if(size.equals("16x16"))
               tileSize = 16;
            else if(size.equals("8x8"))
               tileSize = 8;
            else if(size.equals("Random")) {
               int[] sizes = { 8, 16, 32, 64, 128 };
               tileSize = sizes[new Random().nextInt(sizes.length)];
            }
         }
      }
      int[] dimensions = autoCalcDimensions(tileSize);
      init(dimensions[0], dimensions[1]);
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
   
   public static void init(int x, int y) {
      JFrame frame = new JFrame("TBS2");
      
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
      
      int offsetHorizontal = screenWidth - (gameWidth * tileSize);
      int offsetVertical = screenHeight - guiHeight - (gameHeight * tileSize);
      newGame = new Game(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2, guiHeight);
      newGame.setDoubleBuffered(true);
      newGame.setIgnoreRepaint(true);
      newGame.setSize(screenWidth, screenHeight);
      frame.getContentPane().add(newGame);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setResizable(false);
      frame.setVisible(true);
      frame.setSize(screenWidth + frame.getInsets().right + frame.getInsets().left, screenHeight + frame.getInsets().top + frame.getInsets().bottom);
      newGame.setVisible(true);
      newGame.switchTeams();
   }
   
}
