package com.rolledback.framework;

import java.awt.Image;
import java.io.IOException;
import java.util.LinkedHashMap;

import javax.imageio.ImageIO;

import com.rolledback.units.Unit.UNIT_TYPE;

/**
 * The GraphicsManager loads and stores all grapical related objects for the game. This includes
 * tile textures and unit images.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class GraphicsManager {
   
   private static Image[] unitImages;
   private static LinkedHashMap<String, Image> tileTextures;
   
   static {
      tileTextures = new LinkedHashMap<String, Image>();
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
         
         tileTextures.put("river_intersection.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_intersection.png")));
         
         tileTextures.put("river_tSection_one.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_tSection_one.png")));
         tileTextures.put("river_tSection_two.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_tSection_two.png")));
         tileTextures.put("river_tSection_three.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_tSection_three.png")));
         tileTextures.put("river_tSection_four.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("river_tSection_four.png")));
         
         tileTextures.put("bridge_horizontal.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge_horizontal.png")));
         tileTextures.put("bridge_vertical.png", ImageIO.read(GraphicsManager.class.getClassLoader().getResource("bridge_vertical.png")));
      }
      catch(IOException e) {
         Logger.consolePrint("ERROR loading image. " + e.toString(), "manager");
         e.printStackTrace();
      }
   }
   
   static {
      unitImages = new Image[16];
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
   
   /**
    * Given a specific unit type and the number representing what color of unit is desired, returns
    * an array containing both the left and right (in that order) facing images for that unit.
    * 
    * @param type the unit type for what image is wanted
    * @param team represents what color of unit is wanted, 1 for red, 2 for blue
    * @return array of images corresponding to type and team
    */
   public static Image[] typetoImage(UNIT_TYPE type, int team) {
      Image[] ret = new Image[2];
      if(type == UNIT_TYPE.INFANTRY) {
         if(team == 1) {
            ret[0] = unitImages[0];
            ret[1] = unitImages[4];
         }
         else {
            ret[0] = unitImages[8];
            ret[1] = unitImages[12];
         }
      }
      if(type == UNIT_TYPE.RPG) {
         if(team == 1) {
            ret[0] = unitImages[1];
            ret[1] = unitImages[5];
         }
         else {
            ret[0] = unitImages[9];
            ret[1] = unitImages[13];
         }
      }
      if(type == UNIT_TYPE.TANK) {
         if(team == 1) {
            ret[0] = unitImages[2];
            ret[1] = unitImages[6];
         }
         else {
            ret[0] = unitImages[10];
            ret[1] = unitImages[14];
         }
      }
      if(type == UNIT_TYPE.TANK_DEST) {
         if(team == 1) {
            ret[0] = unitImages[3];
            ret[1] = unitImages[7];
         }
         else {
            ret[0] = unitImages[11];
            ret[1] = unitImages[15];
         }
      }
      return ret;
   }
   
   /**
    * Retrieves the array of unit textures.
    * 
    * @return the unitImages array.
    */
   public static Image[] getUnitImages() {
      return unitImages;
   }
   
   /**
    * Returns the hash map of tile textures.
    * 
    * @return the tileTextures hash map.
    */
   public static LinkedHashMap<String, Image> getTileTextures() {
      return tileTextures;
   }
   
}
