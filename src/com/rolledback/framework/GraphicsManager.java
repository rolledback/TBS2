package com.rolledback.framework;

import java.awt.Image;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

public class GraphicsManager {
   
   public Image[] unitImages;
   public LinkedHashMap<String, Image> tileTextures;
   
   public GraphicsManager() {
      tileTextures = new LinkedHashMap<String, Image>();
      initTileImages();
      
      unitImages = new Image[32];
      initUnitImages();
   }
   
   public String toString() {
      return tileTextures.size() + " " + unitImages.length;
   }
   
   public void initTileImages() {
      Logger.consolePrint("loading tile textures", "manager");
      try {
         tileTextures.put("grass.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("grass.png")));
         tileTextures.put("forest.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("forest.png")));
         tileTextures.put("mountain.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("mountain.png")));
         
         tileTextures.put("cityRed.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("cityRed.png")));
         tileTextures.put("cityBlue.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("cityBlue.png")));
         tileTextures.put("cityGrey.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("cityGrey.png")));
         
         tileTextures.put("factoryRed.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("factoryRed.png")));
         tileTextures.put("factoryBlue.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("factoryBlue.png")));
         tileTextures.put("factoryGrey.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("factoryGrey.png")));
         
         tileTextures.put("river_horizontal.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_horizontal.png")));
         tileTextures.put("river_vertical.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_vertical.png")));
         
         tileTextures.put("riverEnd_up.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_up.png")));
         tileTextures.put("riverEnd_right.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_right.png")));
         tileTextures.put("riverEnd_down.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_down.png")));
         tileTextures.put("riverEnd_left.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverEnd_left.png")));
         
         tileTextures.put("riverCorner_one.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_one.png")));
         tileTextures.put("riverCorner_two.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_two.png")));
         tileTextures.put("riverCorner_three.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_three.png")));
         tileTextures.put("riverCorner_four.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("riverCorner_four.png")));
         
         tileTextures.put("bridge_horizontal.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge_horizontal.png")));
         tileTextures.put("bridge_vertical.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge_vertical.png")));
      }
      catch(IOException e) {
         Logger.consolePrint("ERROR loading image. " + e.toString(), "manager");
         e.printStackTrace();
      }
   }
   
   public void initUnitImages() {
      try {
         Logger.consolePrint("loading unit images", "manager");
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
         Logger.consolePrint("ERROR loading image. " + e.toString(), "manager");
         e.printStackTrace();
      }
   }
   
}
