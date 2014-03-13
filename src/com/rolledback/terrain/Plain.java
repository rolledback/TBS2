package com.rolledback.terrain;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;

public class Plain extends Tile {
   
   public Plain(World w, int x, int y) {
      super(w, x, y, new TerrainEffect(0, 0, 1), 'p');
      type = TILE_TYPE.PLAIN;
      texture = GraphicsManager.getTileTextures().get("grass.png");
   }
   
}
