package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

/**
 * Bridge tile class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Bridge extends Tile {
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in. Passed back to the Tile constructor.
    * @param x x position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param y y position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param t image texture for the tile.
    */
   public Bridge(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(0, -5, 0), 'b');
      type = TILE_TYPE.BRIDGE;
      vehiclePassable = true;
      infantryPassable = true;
      texture = t;
   }
   
}
