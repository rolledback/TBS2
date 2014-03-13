package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

public class City extends CapturableTile {
   
   private int resourceValue = 100;
   
   public City(World w, int x, int y, Team o, Image t) {
      super(w, x, y, new TerrainEffect(0, 20, 0), 'c', o);
      type = TILE_TYPE.CITY;
      texture = t;
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
         texture = GraphicsManager.getTileTextures().get("cityRed.png");
      else
         texture = GraphicsManager.getTileTextures().get("cityBlue.png");
   }
   
}
