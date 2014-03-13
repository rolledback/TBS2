package com.rolledback.terrain;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;

public class Forest extends Tile {
   
   public Forest(World w, int x, int y) {
      super(w, x, y, new TerrainEffect(0, 10, -1), 'f');
      type = TILE_TYPE.FOREST;
      texture = GraphicsManager.getTileTextures().get("forest.png");
   }
   
}
