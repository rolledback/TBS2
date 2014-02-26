package com.rolledback.framework;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static java.util.Arrays.asList;

public class Logger {
   
   // World.java = map
   // Game.java = game
   // Launcher.java = launcher
   // GraphicsManager.java = manager
   // Any of the CPU teams = ai
   
   private static boolean consolePrintingOn = true;
   private static List<String> validTags = asList("map");   
   
   public static String timeStamp() {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS");
      String formattedDate = sdf.format(date);
      return formattedDate;
   }
   
   public static void consolePrint(String message, String tag) {
      if(consolePrintingOn && validTags.contains(tag.toLowerCase())) {
         if(tag.length() > 3)
            tag = tag.substring(0, 3);
         if(tag.length() < 3) {
            int spacesNeeded = 3 - tag.length();
            for(int x = 0; x < spacesNeeded; x++)
               tag += " ";
         }            
         System.out.println("[" + tag.toUpperCase() + "] > " + timeStamp() + " " + message);
      }
   }
   
   public static boolean isConsolePrintingOn() {
      return consolePrintingOn;
   }
   
   public static void setConsolePrintingOn(boolean c) {
      consolePrintingOn = c;
   }
   
}
