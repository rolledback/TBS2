package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

/**
 * City tile class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class City extends CapturableTile {
   
   private int resourceValue = 100;
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in. Passed back to the Tile constructor.
    * @param x x position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param y y position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param o owner of the city. Passed back to the CapturableTile constructor.
    * @param t image texture for the tile.
    */
   public City(World w, int x, int y, Team o, Image t) {
      super(w, x, y, new TerrainEffect(0, 20, 0), 'c', o);
      type = TILE_TYPE.CITY;
      texture = t;
   }
   
   /**
    * Adds the resourceValue of the city to it's owner resource stockpile.
    */
   public void produceResources() {
      owner.setResources(owner.getResources() + resourceValue);
   }
   
   /**
    * Transfers the city from one team to another. If the owner is null (city is currently not
    * owned), then simply adds it to the new owner's city list. Also changes the texture of the city
    * to match the owner's team color.
    */
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
