package com.rolledback.framework;

import javax.swing.JFrame;

public class Launcher {
   
   static Game newGame;
   
   public static void main(String args[]) {
      //init(104, 56); //1920x1080 @ 16x16 tiles
      //init(52, 28); // 1920x1080 @ 32x32 tiles
      init(26, 15); //1920x1080 @ 64x64 tiles
      //init(13, 7); //1920x1080 @ 128x128 tiles
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
      int guiHeight = 0; //128;
      
      int tileSize = 128;
      while((gameWidth * tileSize > screenWidth || gameHeight * tileSize > screenHeight - guiHeight) && tileSize >= 16) {
         tileSize /= 2;
      }
      
      if(tileSize < 16) {
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
      newGame.switchTeams();
   }
   
}
