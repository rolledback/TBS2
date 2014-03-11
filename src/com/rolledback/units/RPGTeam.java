package com.rolledback.units;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class RPGTeam extends Unit {
   
   public RPGTeam(int x, int y, Tile t, Team o) {
      super(x, y, t, o);
      classification = UNIT_CLASS.INFANTRY;
      minAttack = 10;
      maxAttack = 15;
      infAttackBonus = -5;
      vehAttackBonus = 5;
      defense = 4;
      moveRange = 2;
      type = UNIT_TYPE.RPG;
   }
   
}
