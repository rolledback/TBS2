package com.rolledback.analysis;

import java.util.ArrayList;

import com.rolledback.units.DamageTable;
import com.rolledback.units.Infantry;
import com.rolledback.units.RPGTeam;
import com.rolledback.units.Tank;
import com.rolledback.units.TankDestroyer;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

public class UnitAttackAnalysis {
   
   public static void main(String[] args) {
      calcAttackAverage();
      System.out.println();
      calcAttackProfit();
   }
   
   static int tableTextSize = 0;
   static {
      for(UNIT_TYPE t: UNIT_TYPE.values()) {
         if(t.toString().length() > tableTextSize) {
            tableTextSize = t.toString().length();
         }
      }
   }
   
   static ArrayList<Unit> unitList = new ArrayList<Unit>();
   static {
      unitList.add(new Infantry(0, 0, null, null, null, null));
      unitList.add(new RPGTeam(0, 0, null, null, null, null));
      unitList.add(new Tank(0, 0, null, null, null, null));
      unitList.add(new TankDestroyer(0, 0, null, null, null, null));
   }
   
   public static void printSpaces(int n) {
      for(int i = 0; i < n; i++)
         System.out.print(" ");
   }
   
   public static void calcAttackAverage() {
      System.out.println("Unit Attack Average Table:");
      printSpaces(tableTextSize + 2);
      for(Unit u: unitList)
         System.out.print(u.getType().toString() + " ");
      System.out.print("Average");
      System.out.println();
      for(Unit attacker: unitList) {
         double average = 0;
         System.out.print(attacker.getType().toString() + ":");
         printSpaces(tableTextSize + 1 - attacker.getType().toString().length());
         for(Unit defender: unitList) {
            double value = 0;
            int[] bounds = DamageTable.getBounds(attacker.getType(), defender.getType());
            value = bounds[0] + bounds[1];
            value /= 2;
            value -= value * ((double)defender.getDefense() / 100.0);
            average += value;
            value = Math.round(value);
            System.out.print(value + " | ");
         }
         average /= unitList.size();
         printSpaces(new String("Average").length() - Double.toString(average).length());
         System.out.print(average);
         System.out.println();
      }
   }
   
   public static void calcAttackProfit() {
      System.out.println("Unit Attack Profit Table:");
      printSpaces(tableTextSize + 2);
      for(Unit u: unitList)
         System.out.print(u.getType().toString() + " ");
      System.out.print("Average");
      System.out.println();
      for(Unit attacker: unitList) {
         double average = 0;
         System.out.print(attacker.getType().toString() + ":");
         printSpaces(tableTextSize + 1 - attacker.getType().toString().length());
         for(Unit defender: unitList) {
            double attackValue = 0;
            int[] bounds = DamageTable.getBounds(attacker.getType(), defender.getType());
            attackValue = bounds[0] + bounds[1];
            attackValue /= 2;
            attackValue -= attackValue * ((double)defender.getDefense() / 100.0);
            
            double retaliationValue = 0;
            bounds = DamageTable.getBounds(attacker.getType(), defender.getType());
            retaliationValue = bounds[0] + bounds[1];
            retaliationValue /= 2;
            retaliationValue -= retaliationValue * ((double)attacker.getDefense() / 100.0);
            retaliationValue *= (100.0 - attackValue) / 100.0;
            
            double profit = attackValue - retaliationValue;
            average += profit;
            profit = Math.round(profit);
            System.out.print(profit + " | ");
         }
         average /= unitList.size();
         printSpaces(new String("Average").length() - Double.toString(average).length() + 1);
         System.out.print(average);
         System.out.println();
      }
   }
   
}
