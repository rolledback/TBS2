package com.rolledback.units;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import com.rolledback.units.Unit.UNIT_TYPE;

public class DamageTable {
   
   public static void main(String args[]) {
      DamageTable.displayTable();
   }
   
   static private EnumMap<UNIT_TYPE, EnumMap<UNIT_TYPE, int[]>> damageTable;
   static {
      // declare main damage table
      damageTable = new EnumMap<UNIT_TYPE, EnumMap<UNIT_TYPE, int[]>>(UNIT_TYPE.class);
      
      // values for infantry vs x
      EnumMap<UNIT_TYPE, int[]> infValues = new EnumMap<UNIT_TYPE, int[]>(UNIT_TYPE.class);
      infValues.put(UNIT_TYPE.INFANTRY, new int[] { 80, 85 });
      infValues.put(UNIT_TYPE.RPG, new int[] { 80, 85 });
      infValues.put(UNIT_TYPE.TANK, new int[] { 0, 10 });
      infValues.put(UNIT_TYPE.TANK_DEST, new int[] { 5, 15 });
      damageTable.put(UNIT_TYPE.INFANTRY, infValues);
      
      // values for rpg vs x
      EnumMap<UNIT_TYPE, int[]> rpgValues = new EnumMap<UNIT_TYPE, int[]>(UNIT_TYPE.class);
      rpgValues.put(UNIT_TYPE.INFANTRY, new int[] { 45, 55 });
      rpgValues.put(UNIT_TYPE.RPG, new int[] { 45, 55 });
      rpgValues.put(UNIT_TYPE.TANK, new int[] { 60, 90 });
      rpgValues.put(UNIT_TYPE.TANK_DEST, new int[] { 70, 90 });
      damageTable.put(UNIT_TYPE.RPG, rpgValues);
      
      // values for tank vs x
      EnumMap<UNIT_TYPE, int[]> tankValues = new EnumMap<UNIT_TYPE, int[]>(UNIT_TYPE.class);
      tankValues.put(UNIT_TYPE.INFANTRY, new int[] { 75, 85 });
      tankValues.put(UNIT_TYPE.RPG, new int[] { 50, 70 });
      tankValues.put(UNIT_TYPE.TANK, new int[] {70, 80 });
      tankValues.put(UNIT_TYPE.TANK_DEST, new int[] { 35, 50 });
      damageTable.put(UNIT_TYPE.TANK, tankValues);
      
      // values for td vs x
      EnumMap<UNIT_TYPE, int[]> tdValues = new EnumMap<UNIT_TYPE, int[]>(UNIT_TYPE.class);
      tdValues.put(UNIT_TYPE.INFANTRY, new int[] { 10, 40 });
      tdValues.put(UNIT_TYPE.RPG, new int[] { 10, 40 });
      tdValues.put(UNIT_TYPE.TANK, new int[] { 85, 90 });
      tdValues.put(UNIT_TYPE.TANK_DEST, new int[] { 85, 90});
      damageTable.put(UNIT_TYPE.TANK_DEST, tdValues);
   }
   
   public static int[] getBounds(UNIT_TYPE attacker, UNIT_TYPE defender) {
      return damageTable.get(attacker).get(defender);
   }
   
   public static void setTableValue(UNIT_TYPE attacker, UNIT_TYPE defender, int[] bounds) {
      damageTable.get(attacker).put(defender, bounds);
   }
   
   public static void displayTable() {
      for(UNIT_TYPE a: damageTable.keySet()) {
         System.out.println(a.toString() + " vs:");
         for(Map.Entry<UNIT_TYPE, int[]> e: damageTable.get(a).entrySet()) {
            System.out.println("   " + e.getKey().toString() + "-->" + Arrays.toString(e.getValue()));
         }
      }
   }   
}
