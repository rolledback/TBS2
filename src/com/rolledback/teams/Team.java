package com.rolledback.teams;

import java.util.ArrayList;

import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Infantry;
import com.rolledback.units.RPGTeam;
import com.rolledback.units.Tank;
import com.rolledback.units.TankDestroyer;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Team {
   
   protected ArrayList<Unit> units;
   private ArrayList<City> cities;
   protected ArrayList<Factory> factories;
   private String name;
   private int teamSize;
   private int resources;
   private boolean firstTurn;
   
   public Team(String name, int size, int r) {
      units = new ArrayList<Unit>();
      cities = new ArrayList<City>();
      factories = new ArrayList<Factory>();
      teamSize = size;
      this.name = name;
      resources = r;
      firstTurn = true;
   }
   
   public void createUnit(Tile t, UNIT_TYPE uType) {
      if(uType == UNIT_TYPE.TANK)
         units.add(new Tank(t.getX(), t.getY(), t, this));
      else if(uType == UNIT_TYPE.TANK_DEST)
         units.add(new TankDestroyer(t.getX(), t.getY(), t, this));
      else if(uType == UNIT_TYPE.INFANTRY)
         units.add(new Infantry(t.getX(), t.getY(), t, this));
      else if(uType == UNIT_TYPE.RPG)
         units.add(new RPGTeam(t.getX(), t.getY(), t, this));
      t.setOccupied(true);
      t.setOccupiedBy(units.get(units.size() - 1));
   }
   
   public ArrayList<Unit> getUnits() {
      return units;
   }
   
   public void removeUnit(Unit u) {
      units.remove(u);
   }
   
   public String toString() {
      return "Team: " + name + " Resources: " + resources + " Num units: " + units.size() + " Num factories: " + factories.size() + " Num cities: "
            + cities.size();
   }
   
   public int getResources() {
      return resources;
   }
   
   public void setResources(int resources) {
      this.resources = resources;
   }
   
   public String getName() {
      return name;
   }
   
   public void setName(String name) {
      this.name = name;
   }
   
   public int getTeamSize() {
      return teamSize;
   }
   
   public void setTeamSize(int teamSize) {
      this.teamSize = teamSize;
   }
   
   public ArrayList<City> getCities() {
      return cities;
   }
   
   public void setCities(ArrayList<City> cities) {
      this.cities = cities;
   }
   
   public ArrayList<Factory> getFactories() {
      return factories;
   }
   
   public void setFactories(ArrayList<Factory> factories) {
      this.factories = factories;
   }

   public boolean isFirstTurn() {
      return firstTurn;
   }

   public void setFirstTurn(boolean firstTurn) {
      this.firstTurn = firstTurn;
   }
   
}
