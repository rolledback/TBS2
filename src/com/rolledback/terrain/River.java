package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

/**
 * River tile class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class River extends Tile {
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in. Passed back to the Tile constructor.
    * @param x x position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param y y position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    */
   public River(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(0, 0, 0), 'r');
      type = TILE_TYPE.RIVER;
      vehiclePassable = false;
      infantryPassable = false;
      texture = t;
   }
   
}
