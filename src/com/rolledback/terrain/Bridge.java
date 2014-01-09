package com.rolledback.terrain;

import com.rolledback.framework.World;
import com.rolledback.terrain.Tile.TILE_TYPE;

public class Bridge extends Tile {
   
   public Bridge(World w, int x, int y) {
      super(w, x, y, new TerrainEffect(0, 0, 0), 'b');
      type = TILE_TYPE.BRIDGE;
      vehiclePassable = true;
      infantryPassable = true;
   }
   
}
