package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class Infantry extends Unit {
   
   public Infantry(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.INFANTRY;
      minAttack = 5;
      maxAttack = 8;
      defense = 2;
      moveRange = 2;
      type = UNIT_TYPE.INFANTRY;
   }
   
}