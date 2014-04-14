package com.rolledback.framework;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static java.util.Arrays.asList;

/**
 * The logger class is used to debug the program. Log statements are filtered using "tags", which
 * are used in each call to conslePrint. Logging can be turned on and off by changing the
 * consolePrintingOnFlag. Individual tags can be filtered by modifying the validTags list.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Logger {
   
   // World.java = map
   // Game.java = game
   // Launcher.java = launcher
   // GraphicsManager.java = manager
   // Any of the CPU teams = ai
   // General error messages = error
   // Cartographer.java = cartographer
   // MapEditor.java = editor
   // Simulator.java = simulator
   // temporary comments = temp
   
   private static boolean consolePrintingOn = true;
   private static List<String> validTags = asList("simulator", "game", "map", "cartographer", "analysis");
   private static int tagLength = 3;
   
   /**
    * Creates a time stamp of the current time using the SimpleDateFormat class. Time stamp is in
    * the format hour:min:second:millisecond.
    * 
    * @return a string representation of the current time
    */
   public static String timeStamp() {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS");
      String formattedDate = sdf.format(date);
      return formattedDate;
   }
   
   /**
    * Prints out the given message in the format [tag] > time stamp message. Only messages that have
    * a tag found in the validTags list will be printed. Method will also verify that
    * consolePrintingOn is set to true.
    * 
    * @param message Message to be printed to the console.
    * @param tag Tag used to identify the message.
    */
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
   
   /**
    * Returns whether or not a call to consolePrint will produce any output on the console.
    * 
    * @return value of consolePrintingOn
    */
   public static boolean isConsolePrintingOn() {
      return consolePrintingOn;
   }
   
   /**
    * Sets the value of consolePrintingOn which determines if a call to consolePrint will produce
    * any output. If consolePrintingOn is false, then no output will be seen.
    * 
    * @param c value for consolePrintingOn, true will cause printing, false will disable it
    */
   public static void setConsolePrintingOn(boolean c) {
      consolePrintingOn = c;
   }
   
}
