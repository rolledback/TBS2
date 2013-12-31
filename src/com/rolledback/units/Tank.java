package com.rolledback.units;

import com.rolledback.framework.Team;
import com.rolledback.terrain.Tile;

public class Tank extends Unit {
   
   public Tank(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minAttack = 10;
      maxAttack = 15;
      setDefense(12);
      moveRange = 4;
      type = UNIT_TYPE.TANK;
   }
   
}
