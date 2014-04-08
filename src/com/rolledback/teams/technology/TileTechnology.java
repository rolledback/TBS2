package com.rolledback.teams.technology;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile.TILE_TYPE;

public class TileTechnology extends Technology {
   public TileTechnology(Team t, TILE_TYPE tile, int a, int d, int m) {
      super();
      effectedTeam = t;
      tileType = tile;
      attackValue = a;
      defenseValue = d;
      moveValue = m;
   }
}
