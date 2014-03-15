package com.rolledback.teams;

import java.util.Map;

import com.rolledback.terrain.City;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Technology {
   
   // APCR Shells
   // GPS Navigation
   // Artillery Barrage
   // Conscription
   // Fortifications
   // Militia
   
   private UNIT_CLASS unitClass;
   private TILE_TYPE tileType;
   private int attackValue;
   private int defenseValue;
   private int moveValue;
   private TechnologyEffect techEffect;
   private Team effectedTeam;
   
   public enum TECH_NAME {
      APCR, GPS, ART, CON, FORT, MILI, FIELD;
      
      public String toString() {
         if(this == ART)
            return "Artillery Barrage";
         if(this == MILI)
            return "Militia";
         if(this == CON)
            return "Conscription";
         if(this == APCR)
            return "APCR Shells";
         if(this == GPS)
            return "GPS Navigation";
         if(this == FORT)
            return "Fortifications";
         if(this == FIELD)
            return "Field Repairs";
         else
            return null;
      }
      
      public static TECH_NAME stringToName(String s) {
         if(s.equals("Artillery Barrage"))
            return ART;
         if(s.equals("Militia"))
            return MILI;
         if(s.equals("Conscription"))
            return CON;
         if(s.equals("APCR Shells"))
            return APCR;
         if(s.equals("GPS Navigation"))
            return GPS;
         if(s.equals("Fortifications"))
            return FORT;
         if(s.equals("Field Repairs"))
            return FIELD;
         else
            return null;
      }
   }
   
   public static void researchTech(Team researcher, TECH_NAME name) {
      if(name == TECH_NAME.MILI) {
         researcher.getResearchedTechs().add(new Technology(researcher, new TechnologyEffect(researcher, null) {
            public void run() {
               for(City c: ((Team)effectObjectOne).getCities())
                  if(!c.isOccupied())
                     ((Team)effectObjectOne).createUnit(c, UNIT_TYPE.INFANTRY);
            }
         }));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);         
      }
      else if(name == TECH_NAME.ART) {
         researcher.getResearchedTechs().add(new Technology(researcher, new TechnologyEffect(researcher, null) {
            public void run() {
               for(Unit u: (((Team)effectObjectOne).getOpponent()).getUnits())
                  u.takeDamage((int)(u.getHealth() * .25));
            }
         }));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);

      }
      else if(name == TECH_NAME.CON) {
         researcher.getResearchedTechs().add(new Technology(researcher, UNIT_TYPE.INFANTRY, .50));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.APCR) {
         researcher.getResearchedTechs().add(new Technology(researcher, UNIT_CLASS.VEHICLE, 5, 0, 0));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.GPS) {
         researcher.getResearchedTechs().add(new Technology(researcher, UNIT_CLASS.ALL, 0, 0, 1));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.FORT) {
         researcher.getResearchedTechs().add(new Technology(researcher, TILE_TYPE.CITY, 5, 10, -1));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.FIELD) {
         researcher.getResearchedTechs().add(new Technology(researcher, new TechnologyEffect(researcher, null) {
            public void run() {
               for(Unit u: ((Team)effectObjectOne).getUnits())
                  if(u.getClassification() == UNIT_CLASS.VEHICLE)
                     u.setHealth(u.getHealth() + u.getMaxAttack() / 4);
            }
         }));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name); 
      }
      
   }
   
   // unit mod tech
   public Technology(Team t, UNIT_CLASS unit, int a, int d, int m) {
      effectedTeam = t;
      unitClass = unit;
      attackValue = a;
      defenseValue = d;
      moveValue = m;
   }
   
   // tile mod tech
   public Technology(Team t, TILE_TYPE tile, int a, int d, int m) {
      effectedTeam = t;
      tileType = tile;
      attackValue = a;
      defenseValue = d;
      moveValue = m;
   }
   
   // instant effect
   public Technology(Team t, TechnologyEffect effect) {
      effectedTeam = t;
      techEffect = effect;
      effect.run();
   }
   
   // factory tech
   public Technology(Team t, UNIT_TYPE type, double discount) {
      effectedTeam = t;
      for(Map.Entry<UNIT_TYPE, Integer> entry: effectedTeam.getProductionList().entrySet())
         if(entry.getKey() == type || type == UNIT_TYPE.ALL)
            entry.setValue(entry.getValue() - (int)(entry.getValue() * discount));
   }
   
   public int getAttackValue() {
      return attackValue;
   }
   
   public void setAttackValue(int attackValue) {
      this.attackValue = attackValue;
   }
   
   public int getDefenseValue() {
      return defenseValue;
   }
   
   public void setDefenseValue(int defenseValue) {
      this.defenseValue = defenseValue;
   }
   
   public int getMoveValue() {
      return moveValue;
   }
   
   public void setMoveValue(int moveValue) {
      this.moveValue = moveValue;
   }
   
   public TechnologyEffect getTechEffect() {
      return techEffect;
   }
   
   public void setTechEffect(TechnologyEffect techEffect) {
      this.techEffect = techEffect;
   }
   
   public Team getEffectedTeam() {
      return effectedTeam;
   }
   
   public void setEffectedTeam(Team effectedTeam) {
      this.effectedTeam = effectedTeam;
   }
   
   public UNIT_CLASS getUnitClass() {
      return unitClass;
   }
   
   public void setUnitClass(UNIT_CLASS unitType) {
      this.unitClass = unitType;
   }
   
   public TILE_TYPE getTileType() {
      return tileType;
   }
   
   public void setTileType(TILE_TYPE tileType) {
      this.tileType = tileType;
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
