package com.rolledback.terrain;

import com.rolledback.framework.World;

public class Mountain extends Tile {
	
	public Mountain(World w, int x, int y) {
		super(w, x, y, new TerrainEffect(0, 20, -2), 'm');
		type = TILE_TYPE.MOUNTAIN;
		vehiclePassable = false;
	}
}
