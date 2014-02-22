package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

public class Bridge extends Tile {
   
   public Bridge(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(0, -5, 0), 'b');
      type = TILE_TYPE.BRIDGE;
      vehiclePassable = true;
      infantryPassable = true;
      texture = t;
   }
   
}
