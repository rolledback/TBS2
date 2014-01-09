package com.rolledback.terrain;

import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

public class City extends Tile {
   
   private Team owner;
   private int resourceValue = 100;
   
   public City(World w, int x, int y, Team o) {
      super(w, x, y, new TerrainEffect(0, 0, 0), 'c');
      owner = o;
      type = TILE_TYPE.CITY;
   }
   
   public Team getOwner() {
      return owner;
   }
   
   public void setOwner(Team owner) {
      this.owner = owner;
   }
   
   public void produceResources() {
      owner.setResources(owner.getResources() + resourceValue);
   }
   
   public void capture(Unit unit) {
      if(owner != null)
         owner.getCities().remove(this);
      owner = unit.getOwner();
      unit.getOwner().getCities().add(this);
   }
   
}
