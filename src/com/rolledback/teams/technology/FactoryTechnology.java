package com.rolledback.teams.technology;

import java.util.Map;

import com.rolledback.teams.Team;
import com.rolledback.units.Unit.UNIT_TYPE;

public class FactoryTechnology extends Technology {
   public FactoryTechnology(Team t, UNIT_TYPE type, double discount) {
      super();
      effectedTeam = t;
      for(Map.Entry<UNIT_TYPE, Integer> entry: effectedTeam.getProductionList().entrySet())
         if(entry.getKey() == type || type == UNIT_TYPE.ALL)
            entry.setValue(entry.getValue() - (int)(entry.getValue() * discount));
   }
}
