package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class Tank extends Unit {
   
   public Tank(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minAttack = 65;
      maxAttack = 70;
      infAttackBonus = 15;
      vehAttackBonus = 0;
      defense = 20;
      moveRange = 5;
      type = UNIT_TYPE.TANK;
   }
   
}
