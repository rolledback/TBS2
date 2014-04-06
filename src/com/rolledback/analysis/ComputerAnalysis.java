package com.rolledback.analysis;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.rolledback.framework.Coordinate;

/**
 * Used to analyze dump files of click histories. Uses a gradient to display the frequency of the
 * clicks.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class ComputerAnalysis {
   
   static LinkedHashMap<Coordinate, Double> coordHash;
   
   /**
    * Reads in the dump file and passes the coordinate hash map to the Analysis Display object.
    * 
    * @param args
    */
   public static void main(String args[]) {
      File coordinateDump = new File("dump.txt");
      coordHash = new LinkedHashMap<Coordinate, Double>();
      int maxValue = 0;
      int width = 176;
      int height = 96;
      int size = 8;
      
      for(int x = 0; x < width; x++)
         for(int y = 0; y < height; y++)
            coordHash.put(new Coordinate(x, y), 0.0);
      
      try {
         Scanner dumpReader = new Scanner(coordinateDump);
         while(dumpReader.hasNextLine()) {
            String line = dumpReader.nextLine();
            Coordinate temp = processLine(line);
            if(temp != null) {
               coordHash.put(temp, coordHash.get(temp) + 1);
               if(coordHash.get(temp) > maxValue)
                  maxValue = (int)Math.round(coordHash.get(temp));
            }
         }
         dumpReader.close();
      }
      catch(FileNotFoundException e) {
         e.toString();
      }
      
      JFrame frame = new JFrame("Display");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setVisible(true);
      frame.setSize((width * size) + frame.getInsets().right + frame.getInsets().left, (height * size) + frame.getInsets().top + frame.getInsets().bottom);
      AnalysisDisplay display = new AnalysisDisplay(coordHash, maxValue);
      display.setSize(width * size, height * size);
      frame.add(display);
      display.setVisible(true);
   }
   
   /**
    * Parses the line of a dump.txt file into a coordinate.
    * 
    * @param line a line from dump.txt of the format <x coord> <y coord>
    * @return the coordinate version of the line
    */
   public static Coordinate processLine(String line) {
      String[] keys = line.split(" ");
      try {
         return new Coordinate(Integer.parseInt(keys[0]), Integer.parseInt(keys[1]));
      }
      catch(Exception e) {
         e.toString();
         return null;
      }
   }
}

/**
 * Displays the data parsed by the ComputerAnalysis class. Uses a standard heat map to visually
 * represent the frequency of clicks on a given tile. Blue being small amount of clicks, and red
 * being the most.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
class AnalysisDisplay extends JPanel {
   
   LinkedHashMap<Coordinate, Double> values;
   int maxValue;
   int size = 8;
   
   private static final long serialVersionUID = 1L;
   Color[] gradient = {
         new Color(0, 0, 255),
         new Color(0, 40, 255),
         new Color(0, 81, 255),
         new Color(0, 122, 255),
         new Color(0, 163, 255),
         new Color(0, 204, 255),
         new Color(0, 244, 255),
         new Color(0, 255, 254),
         new Color(0, 255, 183),
         new Color(0, 255, 142),
         new Color(0, 255, 101),
         new Color(0, 255, 61),
         new Color(0, 255, 20),
         new Color(20, 255, 0),
         new Color(61, 255, 0),
         new Color(101, 255, 0),
         new Color(142, 255, 0),
         new Color(183, 255, 0),
         new Color(224, 255, 0),
         new Color(255, 244, 0),
         new Color(255, 203, 0),
         new Color(255, 163, 0),
         new Color(255, 122, 0),
         new Color(255, 81, 0),
         new Color(255, 40, 0),
         new Color(255, 0, 0), };
   
   /**
    * Constructor.
    * 
    * @param v Map containing values for all possible grid coordinates.
    * @param mV The highest value in the map. Pre computed during parsing to save time.
    */
   public AnalysisDisplay(LinkedHashMap<Coordinate, Double> v, int mV) {
      values = v;
      maxValue = mV;
      System.out.println(maxValue);
   }
   
   public void paintComponent(Graphics g) {
      for(Map.Entry<Coordinate, Double> entry: values.entrySet()) {
         int value = (int)(entry.getValue() / maxValue * 25);
         g.setColor(gradient[value]);
         g.fillRect(entry.getKey().getX() * size, entry.getKey().getY() * size, size, size);
      }
   }
   
}
