package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class TankDestroyer extends Unit {
   
   public TankDestroyer(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minAttack = 70;
      maxAttack = 75;
      infAttackBonus = -35;
      vehAttackBonus = 15;
      defense = 15;
      moveRange = 4;
      type = UNIT_TYPE.TANK_DEST;
   }
   
}