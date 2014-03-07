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
      int height = 108;
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
         new Color(0, 69, 229),
         new Color(0, 161, 231),
         new Color(1, 234, 213),
         new Color(1, 237, 122),
         new Color(2, 240, 30),
         new Color(69, 234, 2),
         new Color(167, 246, 3),
         new Color(249, 242, 3),
         new Color(252, 137, 4),
         new Color(252, 83, 8),
         new Color(255, 0, 0) };
   
   public AnalysisDisplay(LinkedHashMap<Coordinate, Double> v, int mV) {
      values = v;
      maxValue = mV;
      System.out.println(maxValue);
   }
   
   public void paintComponent(Graphics g) {
      for(Map.Entry<Coordinate, Double> entry: values.entrySet()) {
         int value = (int)(entry.getValue() / maxValue * 10);
         g.setColor(gradient[value]);
         g.fillRect(entry.getKey().getX() * size, entry.getKey().getY() * size, size, size);
      }
   }
   
}
