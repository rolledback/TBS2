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

public class ComputerAnalysis {
   
   static LinkedHashMap<Coordinate, Double> coordHash;
   
   public static void main(String args[]) {
      File coordinateDump = new File("dump.txt");
      coordHash = new LinkedHashMap<Coordinate, Double>();
      int maxValue = 0;
      int width = 208;
      int height = 112;
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
      frame.setSize((width * size) + frame.getInsets().right + frame.getInsets().left, (height * size) + frame.getInsets().top
            + frame.getInsets().bottom);
      AnalysisDisplay display = new AnalysisDisplay(coordHash, maxValue);
      display.setSize(width * size, height * size);
      frame.add(display);
      display.setVisible(true);
   }
   
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
