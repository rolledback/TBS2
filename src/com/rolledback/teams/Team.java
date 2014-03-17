package com.rolledback.teams;

import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.rolledback.teams.Technology.TECH_NAME;
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
   protected ArrayList<City> cities;
   protected ArrayList<Factory> factories;
   protected ArrayList<Technology> researchedTechs;
   protected LinkedHashMap<UNIT_TYPE, Integer> productionList;
   protected LinkedHashMap<TECH_NAME, Integer> techTree;
   protected Team opponent;
   
   private String name;
   private int teamSize;
   private int resources;
   private boolean firstTurn;
   private int teamNumber;
   
   public Team(String name, int size, int r, int n) {
      units = new ArrayList<Unit>();
      cities = new ArrayList<City>();
      factories = new ArrayList<Factory>();
      researchedTechs = new ArrayList<Technology>();
      teamSize = size;
      this.name = name;
      resources = r;
      firstTurn = true;
      setTeamNumber(n);
      initProductionList();
      initTechTree();
   }
   
   public void initProductionList() {
      productionList = new LinkedHashMap<UNIT_TYPE, Integer>();
      productionList.put(UNIT_TYPE.INFANTRY, 100);
      productionList.put(UNIT_TYPE.RPG, 300);
      productionList.put(UNIT_TYPE.TANK, 500);
      productionList.put(UNIT_TYPE.TANK_DEST, 600);
   }
   
   public void initTechTree() {
      techTree = new LinkedHashMap<TECH_NAME, Integer>();
      techTree.put(TECH_NAME.CON, 1000);
      techTree.put(TECH_NAME.ART, 800);
      techTree.put(TECH_NAME.FORT, 800);
      techTree.put(TECH_NAME.MILI, 700);
      techTree.put(TECH_NAME.FIELD, 600);
      techTree.put(TECH_NAME.APCR, 600);
      techTree.put(TECH_NAME.GPS, 300);
   }
   
   public void createUnit(Tile t, UNIT_TYPE uType, Image lI, Image rI) {
      if(uType == UNIT_TYPE.TANK)
         units.add(new Tank(t.getX(), t.getY(), t, this, lI, rI));
      else if(uType == UNIT_TYPE.TANK_DEST)
         units.add(new TankDestroyer(t.getX(), t.getY(), t, this, lI, rI));
      else if(uType == UNIT_TYPE.INFANTRY)
         units.add(new Infantry(t.getX(), t.getY(), t, this, lI, rI));
      else if(uType == UNIT_TYPE.RPG)
         units.add(new RPGTeam(t.getX(), t.getY(), t, this, lI, rI));
      t.setOccupied(true);
      t.setOccupiedBy(units.get(units.size() - 1));
   }
   
   public LinkedHashMap<TECH_NAME, Integer> getTechTree() {
      return techTree;
   }
   
   public void setTechTree(LinkedHashMap<TECH_NAME, Integer> techTree) {
      this.techTree = techTree;
   }
   
   public LinkedHashMap<UNIT_TYPE, Integer> getProductionList() {
      return this.productionList;
   }
   
   public ArrayList<Unit> getUnits() {
      return units;
   }
   
   public void removeUnit(Unit u) {
      units.remove(u);
   }
   
   public String toString() {
      return "Team: " + name + " Resources: " + resources + " Num units: " + units.size() + " Num factories: " + factories.size() + " Num cities: " + cities.size();
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
   
   public ArrayList<Technology> getResearchedTechs() {
      return researchedTechs;
   }
   
   public void setResearchedTechs(ArrayList<Technology> researchedTechs) {
      this.researchedTechs = researchedTechs;
   }
   
   public Team getOpponent() {
      return opponent;
   }
   
   public void setOpponent(Team opponent) {
      this.opponent = opponent;
   }

   public int getTeamNumber() {
      return teamNumber;
   }

   public void setTeamNumber(int teamNumber) {
      this.teamNumber = teamNumber;
   }
}
