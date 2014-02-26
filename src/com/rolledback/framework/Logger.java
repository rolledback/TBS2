package com.rolledback.framework;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
   
   private static boolean consolePrintingOn = false;
   
   public static String timeStamp() {
      Date date = new Date();
      SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss:SS");
      String formattedDate = sdf.format(date);
      return formattedDate;
   }
   
   public static void consolePrint(String message) {
      if(consolePrintingOn)
         System.out.println("> " + timeStamp() + " " + message);
   }
   
   public static boolean isConsolePrintingOn() {
      return consolePrintingOn;
   }
   
   public static void setConsolePrintingOn(boolean c) {
      consolePrintingOn = c;
   }
   
}
