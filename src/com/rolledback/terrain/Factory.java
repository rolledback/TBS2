package com.rolledback.terrain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Factory extends Tile {
   
   private Team owner;
   private HashMap<UNIT_TYPE, Integer> productionList;
   private int resourceValue = 0;
   
   public Factory(World w, int x, int y, Team team) {
      super(w, x, y, new TerrainEffect(0, 10, 0), 'F');
      owner = team;
      productionList = new HashMap<UNIT_TYPE, Integer>();
      type = TILE_TYPE.FACTORY;
      initProductionList();
   }
   
   public Team getOwner() {
      return owner;
   }
   
   public void initProductionList() {
      productionList.put(UNIT_TYPE.INFANTRY, 100);
      productionList.put(UNIT_TYPE.TANK, 250);
      productionList.put(UNIT_TYPE.TANK_DEST, 250);
      productionList.put(UNIT_TYPE.RPG, 125);
   }
   
   public HashMap<UNIT_TYPE, Integer> getProductionList() {
      return this.productionList;
   }
   
   public String[] dialogBoxList() {
      String[] list = new String[productionList.size()];
      int numEntry = 0;
      for(Map.Entry<UNIT_TYPE, Integer> entry: productionList.entrySet()) {
         String unitString = "";
         if(entry.getKey() == UNIT_TYPE.INFANTRY)
            unitString = "Infantry";
         else if(entry.getKey() == UNIT_TYPE.TANK)
            unitString = "Tank";
         else if(entry.getKey() == UNIT_TYPE.TANK_DEST)
            unitString = "Tank Destroyer";
         else if(entry.getKey() == UNIT_TYPE.RPG)
            unitString = "RPG Team";
         list[numEntry] = unitString + ", cost: " + entry.getValue().toString();
         numEntry++;
      }
      Arrays.sort(list);
      return list;      
   }
   
   public boolean produceUnit(UNIT_TYPE type) {
      if(owner.getResources() < productionList.get(type))
         return false;
      if(world.getTiles()[y][x].isOccupied())
         return false;
      owner.setResources(owner.getResources() - productionList.get(type));
      owner.createUnit(world.getTiles()[y][x], type);
      owner.getUnits().get(owner.getUnits().size() - 1).setAttacked(true);
      owner.getUnits().get(owner.getUnits().size() - 1).setMoved(true);
      return true;
   }
   
   public void produceResources() {
      owner.setResources(owner.getResources() + resourceValue);
   }
   
}
