package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

public class Forest extends Tile {
   
   public Forest(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(0, 10, -1), 'f');
      type = TILE_TYPE.FOREST;
      texture = t;
   }
   
}
