package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

public class River extends Tile {
   
   public River(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(0, 0, 0), 'r');
      type = TILE_TYPE.RIVER;
      vehiclePassable = false;
      infantryPassable = false;
      texture = t;
   }
   
}
