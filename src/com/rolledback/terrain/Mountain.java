package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

public class Mountain extends Tile {
   
   public Mountain(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(10, 30, -2), 'm');
      type = TILE_TYPE.MOUNTAIN;
      vehiclePassable = false;
      texture = t;
   }
}
