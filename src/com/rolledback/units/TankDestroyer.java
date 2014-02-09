package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class TankDestroyer extends Unit {
   
   public TankDestroyer(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minAttack = 10;
      maxAttack = 20;
      infAttackBonus = -10;
      vehAttackBonus = 10;
      defense = 6;
      moveRange = 3;
      type = UNIT_TYPE.TANK_DEST;
   }
   
}