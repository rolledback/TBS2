package com.rolledback.terrain;

import com.rolledback.framework.World;
import com.rolledback.terrain.Tile.TILE_TYPE;

public class River extends Tile {
   
   public River(World w, int x, int y) {
      super(w, x, y, new TerrainEffect(0, 0, 0), 'r');
      type = TILE_TYPE.RIVER;
      vehiclePassable = false;
      infantryPassable = false;
   }
   
}
