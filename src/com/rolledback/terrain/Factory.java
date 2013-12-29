package com.rolledback.terrain;

import java.util.HashMap;
import com.rolledback.framework.Team;
import com.rolledback.framework.World;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Factory extends Tile {
   
   private Team owner;
   private HashMap<UNIT_TYPE, Integer> productionList;
   
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
   }
   
   public HashMap<UNIT_TYPE, Integer> getProductionList() {
      return this.productionList;
   }
   
   public boolean produceUnit(UNIT_TYPE type) {
      if(owner.getResources() < productionList.get(type))
         return false;
      if(world.getTiles()[y][x].isOccupied())
         return false;
      owner.setResources(owner.getResources() - productionList.get(type));
      owner.createUnit(world.getTiles()[y][x], type);
      owner.getUnits().get(owner.getUnits().size()).setAttacked(true);
      owner.getUnits().get(owner.getUnits().size()).setMoved(true);
      return true;
   }
   
}
