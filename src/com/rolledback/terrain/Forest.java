package com.rolledback.terrain;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;

/**
 * Forest tile class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Forest extends Tile {
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in. Passed back to the Tile constructor.
    * @param x x position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param y y position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    */
   public Forest(World w, int x, int y) {
      super(w, x, y, new TerrainEffect(0, 10, -1), 'f');
      type = TILE_TYPE.FOREST;
      texture = GraphicsManager.getTileTextures().get("forest.png");
   }
   
}
