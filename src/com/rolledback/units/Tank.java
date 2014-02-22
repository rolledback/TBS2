package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class Tank extends Unit {
   
   public Tank(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minAttack = 15;
      maxAttack = 20;
      infAttackBonus = -5;
      vehAttackBonus = 5;
      defense = 12;
      moveRange = 4;
      type = UNIT_TYPE.TANK;
   }
   
}
