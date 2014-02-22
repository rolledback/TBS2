package com.rolledback.terrain;

import java.awt.Image;

import com.rolledback.framework.World;

public class Plain extends Tile {
   
   public Plain(World w, int x, int y, Image t) {
      super(w, x, y, new TerrainEffect(0, 0, 1), 'p');
      type = TILE_TYPE.PLAIN;
      texture = t;
   }
   
}
