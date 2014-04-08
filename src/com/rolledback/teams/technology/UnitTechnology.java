package com.rolledback.teams.technology;

import com.rolledback.teams.Team;
import com.rolledback.units.Unit.UNIT_CLASS;

public class UnitTechnology extends Technology {
   public UnitTechnology(Team t, UNIT_CLASS unit, int a, int d, int m) {
      super();
      effectedTeam = t;
      unitClass = unit;
      attackValue = a;
      defenseValue = d;
      moveValue = m;
   }
}
