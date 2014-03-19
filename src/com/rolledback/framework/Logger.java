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
   // General error messages = error
   // Cartographer.java = cartographer
   
   private static boolean consolePrintingOn = true;
   private static List<String> validTags = asList("launcher", "cartographer");
   private static int tagLength = 3;
   
   public static String timeStamp() {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS");
      String formattedDate = sdf.format(date);
      return formattedDate;
   }
   
   public static void consolePrint(String message, String tag) {
      if(consolePrintingOn && validTags.contains(tag.toLowerCase())) {
         if(tag.length() > tagLength)
            tag = tag.substring(0, tagLength);
         if(tag.length() < tagLength) {
            int spacesNeeded = tagLength - tag.length();
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
