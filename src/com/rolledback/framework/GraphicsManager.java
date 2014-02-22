package com.rolledback.framework;

import java.awt.Image;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class GraphicsManager {
   
   public Image[] tileTextures;
   public Image[] unitImages;
   
   public GraphicsManager() {
      tileTextures = new Image[30];
      initTileImages();
      
      unitImages = new Image[32];
      initUnitImages();
   }
   
   public String toString() {
      return tileTextures.length + " " + unitImages.length; 
   }
   
   public void initTileImages() {
      try {
         tileTextures[0] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("grass.png"));
         tileTextures[1] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("forest.png"));
         tileTextures[2] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("mountain.png"));     
         tileTextures[3] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge.png"));         
         tileTextures[4] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river.png"));      
         tileTextures[5] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner.png"));
         tileTextures[6] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd.png"));
         tileTextures[8] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("cityRed.png"));
         tileTextures[9] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("cityBlue.png"));     
         tileTextures[15] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("cityGrey.png"));
         tileTextures[12] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("factoryRed.png"));         
         tileTextures[13] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("factoryBlue.png"));      
         tileTextures[17] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("factoryGrey.png"));
         tileTextures[18] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_horizontal.png"));
         tileTextures[19] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_vertical.png"));
         
         tileTextures[20] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_up.png"));      
         tileTextures[21] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_right.png"));
         tileTextures[22] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_down.png"));
         tileTextures[23] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_left.png"));
         
         tileTextures[24] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_one.png"));      
         tileTextures[25] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_two.png"));
         tileTextures[26] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_three.png"));
         tileTextures[27] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_four.png"));
         
         tileTextures[28] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge_horizontal.png"));
         tileTextures[29] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge_vertical.png"));
      }
      catch(IOException e) {
         System.out.println("Error loading image. " + e.toString());
         e.printStackTrace();
      }     
   }
   
   public void initUnitImages() {
      try {
         unitImages[0] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("infantryRed_left.png"));
         unitImages[1] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("rpgRed_left.png"));
         unitImages[2] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankRed_left.png"));
         unitImages[3] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankDestroyerRed_left.png"));
         
         unitImages[4] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("infantryRed_right.png"));
         unitImages[5] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("rpgRed_right.png"));
         unitImages[6] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankRed_right.png"));
         unitImages[7] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankDestroyerRed_right.png"));
         
         unitImages[8] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("infantryBlue_left.png"));
         unitImages[9] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("rpgBlue_left.png"));
         unitImages[10] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankBlue_left.png"));
         unitImages[11] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankDestroyerBlue_left.png"));
         
         unitImages[12] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("infantryBlue_right.png"));
         unitImages[13] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("rpgBlue_right.png"));
         unitImages[14] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankBlue_right.png"));
         unitImages[15] = ImageIO.read(GraphicsManager.class.getClassLoader().getResource("tankDestroyerBlue_right.png"));
      }
      catch(IOException e) {
         System.out.println("Error loading image. " + e.toString());
         e.printStackTrace();
      }      
   }   
   
}
