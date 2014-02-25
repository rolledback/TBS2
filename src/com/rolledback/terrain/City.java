package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

public class City extends Tile {
   
   private Team owner;
   private int resourceValue = 50;
   
   public City(World w, int x, int y, Team o, Image t) {
      super(w, x, y, new TerrainEffect(0, 20, 0), 'c');
      owner = o;
      type = TILE_TYPE.CITY;
      texture = t;
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
      if(owner.equals(getWorld().getTeamOne()))
         texture = getWorld().getManager().tileTextures[8];
      else
         texture = getWorld().getManager().tileTextures[9];
   }
   
}
