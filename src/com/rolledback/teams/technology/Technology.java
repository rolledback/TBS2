package com.rolledback.teams.technology;

import java.awt.Image;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.teams.Team;
import com.rolledback.terrain.City;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_CLASS;
import com.rolledback.units.Unit.UNIT_TYPE;

public abstract class Technology {
   
   // APCR Shells
   // GPS Navigation
   // Artillery Barrage
   // Conscription
   // Fortifications
   // Militia
   
   protected UNIT_CLASS unitClass;
   protected TILE_TYPE tileType;
   protected int attackValue;
   protected int defenseValue;
   protected int moveValue;
   protected Team effectedTeam;
   
   public enum TECH_NAME {
      APCR("APCR Shells"),
      GPS("GPS Navigaion"),
      ART("Artillery Shells"),
      CON("Conscription"),
      FORT("Fortifications"),
      MILI("Militia"),
      FIELD("Field Repairs");
      
      private final String name;
      
      TECH_NAME(String n) {
         name = n;
      }
      
      public String toString() {
         return name;
      }
      
      public static TECH_NAME stringToName(String s) {
         switch(s) {
            case "Artillery Barrage":
               return ART;
            case "Militia":
               return MILI;
            case "Conscription":
               return CON;
            case "APCR Shells":
               return APCR;
            case "GPS Navigation":
               return GPS;
            case "Fortifications":
               return FORT;
            case "Field Repairs":
               return FIELD;
            default:
               return null;
         }
      }
   }
   
   public static void researchTech(Team researcher, TECH_NAME name) {
      if(name == TECH_NAME.MILI) {
         researcher.getResearchedTechs().add(new RunnableTechnology(researcher, new TechnologyEffect(researcher, null) {
            public void run() {
               for(City c: ((Team)effectObjectOne).getCities())
                  if(!c.isOccupied()) {
                     Image[] textures = GraphicsManager.typetoImage(UNIT_TYPE.INFANTRY, ((Team)effectObjectOne).getTeamNumber());
                     ((Team)effectObjectOne).createUnit(c, UNIT_TYPE.INFANTRY, textures[0], textures[1]);
                  }
            }
         }));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.ART) {
         researcher.getResearchedTechs().add(new RunnableTechnology(researcher, new TechnologyEffect(researcher, null) {
            public void run() {
               for(Unit u: (((Team)effectObjectOne).getOpponent()).getUnits())
                  u.takeDamage((int)(u.getHealth() * .25));
            }
         }));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
         
      }
      else if(name == TECH_NAME.CON) {
         researcher.getResearchedTechs().add(new FactoryTechnology(researcher, UNIT_TYPE.INFANTRY, .50));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.APCR) {
         researcher.getResearchedTechs().add(new UnitTechnology(researcher, UNIT_CLASS.VEHICLE, 5, 0, 0));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.GPS) {
         researcher.getResearchedTechs().add(new UnitTechnology(researcher, UNIT_CLASS.ALL, 0, 0, 1));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.FORT) {
         researcher.getResearchedTechs().add(new TileTechnology(researcher, TILE_TYPE.CITY, 5, 10, -1));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      else if(name == TECH_NAME.FIELD) {
         researcher.getResearchedTechs().add(new RunnableTechnology(researcher, new TechnologyEffect(researcher, null) {
            public void run() {
               for(Unit u: ((Team)effectObjectOne).getUnits())
                  if(u.getClassification() == UNIT_CLASS.VEHICLE)
                     u.setHealth(u.getHealth() + u.getMaxHealth() / 2);
            }
         }));
         researcher.setResources(researcher.getResources() - researcher.getTechTree().get(name));
         researcher.getTechTree().remove(name);
      }
      
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
