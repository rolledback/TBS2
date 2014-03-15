package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class Infantry extends Unit {
   
   public Infantry(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.INFANTRY;
      minAttack = 55;
      maxAttack = 60;
      infAttackBonus = 15;
      vehAttackBonus = -30;
      defense = 10;
      moveRange = 3;
      type = UNIT_TYPE.INFANTRY;
   }
   
}