package com.rolledback.terrain;

import java.awt.Image;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Factory extends CapturableTile {
   
   private LinkedHashMap<UNIT_TYPE, Integer> productionList;
   private int resourceValue = 10;
   
   public Factory(World w, int x, int y, Team team, Image t) {
      super(w, x, y, new TerrainEffect(0, 10, 0), 'F', team);
      productionList = new LinkedHashMap<UNIT_TYPE, Integer>();
      type = TILE_TYPE.FACTORY;
      texture = t;
      if(team != null)
         initProductionList();
   }
   
   public void initProductionList() {
      productionList = owner.getProductionList();
   }
   
   public LinkedHashMap<UNIT_TYPE, Integer> getProductionList() {
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
      if(getWorld().getTiles()[y][x].isOccupied())
         return false;
      owner.setResources(owner.getResources() - productionList.get(type));
      Image texture = GraphicsManager.typetoImage(type, owner.getTeamNumber());
      owner.createUnit(getWorld().getTiles()[y][x], type, texture);      
      owner.getUnits().get(owner.getUnits().size() - 1).setAttacked(true);
      owner.getUnits().get(owner.getUnits().size() - 1).setMoved(true);
      return true;
   }
   
   public void produceResources() {
      owner.setResources(owner.getResources() + resourceValue);
   }
   
   @Override
   public void capture(Unit unit) {
      if(owner != null)
         owner.getCities().remove(this);
      owner = unit.getOwner();
      unit.getOwner().getFactories().add(this);
      if(owner.equals(getWorld().getTeamOne()))
         texture = GraphicsManager.getTileTextures().get("factoryRed.png");
      else
         texture = GraphicsManager.getTileTextures().get("factoryBlue.png");
      initProductionList();
   }
   
}
