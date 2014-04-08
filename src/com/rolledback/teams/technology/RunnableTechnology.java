package com.rolledback.teams.technology;

import com.rolledback.teams.Team;

public class RunnableTechnology extends Technology {
   
   private TechnologyEffect techEffect;
   
   public RunnableTechnology(Team t, TechnologyEffect effect) {
      super();
      effectedTeam = t;
      techEffect = effect;
      effect.run();
   }
   
   public TechnologyEffect getTechEffect() {
      return techEffect;
   }
   
   public void setTechEffect(TechnologyEffect techEffect) {
      this.techEffect = techEffect;
   }
   
}

abstract class TechnologyEffect {
   
   Object effectObjectOne;
   Object effectObjectTwo;
   
   public TechnologyEffect(Object o, Object t) {
      effectObjectOne = o;
      effectObjectTwo = t;
   }
   
   public abstract void run();
}
