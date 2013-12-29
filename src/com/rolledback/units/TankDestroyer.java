package com.rolledback.units;

import com.rolledback.framework.Team;
import com.rolledback.terrain.Tile;

public class TankDestroyer extends Unit {
   
   public TankDestroyer(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minAttack = 15;
      maxAttack = 20;
      defense = 5;
      moveRange = 3;
      type = UNIT_TYPE.TANK_DEST;
   }
   
}