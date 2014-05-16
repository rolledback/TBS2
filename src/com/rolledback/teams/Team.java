package com.rolledback.teams;

import java.awt.Image;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.rolledback.teams.technology.Technology;
import com.rolledback.teams.technology.Technology.TECH_NAME;
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
   private int colorNumber;
   private LinkedHashMap<UNIT_TYPE, Integer> productionHistory;
   private int researchCount;
   private int killCount;
   private int deathCount;
   private int resourcesGathered;
   
   public Team(String n, int size, int r, int c) {
      units = new ArrayList<Unit>();
      cities = new ArrayList<City>();
      factories = new ArrayList<Factory>();
      researchedTechs = new ArrayList<Technology>();
      researchCount = 0;
      killCount = 0;
      deathCount = 0;
      resourcesGathered = 0;
      teamSize = size;
      name = n;
      resources = r;
      firstTurn = true;
      colorNumber = c;
      initProductionList();
      initTechTree();
   }
   
   public void initProductionList() {
      productionList = new LinkedHashMap<UNIT_TYPE, Integer>();
      productionList.put(UNIT_TYPE.INFANTRY, 100);
      productionList.put(UNIT_TYPE.RPG, 300);
      productionList.put(UNIT_TYPE.TANK, 500);
      productionList.put(UNIT_TYPE.TANK_DEST, 600);
      
      productionHistory = new LinkedHashMap<UNIT_TYPE, Integer>();
      productionHistory.put(UNIT_TYPE.INFANTRY, 0);
      productionHistory.put(UNIT_TYPE.RPG, 0);
      productionHistory.put(UNIT_TYPE.TANK, 0);
      productionHistory.put(UNIT_TYPE.TANK_DEST, 0);
   }
   
   public void initTechTree() {
      techTree = new LinkedHashMap<TECH_NAME, Integer>();
      techTree.put(TECH_NAME.CON, 1000);
      techTree.put(TECH_NAME.GPS, 1000);
      techTree.put(TECH_NAME.FORT, 800);
      techTree.put(TECH_NAME.MILI, 700);
      techTree.put(TECH_NAME.FIELD, 600);
      techTree.put(TECH_NAME.APCR, 600);
      techTree.put(TECH_NAME.ART, 600);
   }
   
   /**
    * Parses the production list into an array of strings for the factory option pane.
    * 
    * @return array of strings which contains the production list in format suitable for a drop down
    *         menu.
    */
   public String[] dialogBoxProductionList() {
      String[] list = new String[productionList.size()];
      int numEntry = 0;
      for(Map.Entry<UNIT_TYPE, Integer> entry: productionList.entrySet()) {
         list[numEntry] = entry.getKey().toString() + ", cost: " + entry.getValue().toString();
         numEntry++;
      }
      return list;
   }
   
   /**
    * Parses the tech tree into an array of strings for the factory option pane.
    * 
    * @return array of strings which contains the tech tree in format suitable for a drop down menu.
    */
   public String[] dialogBoxTechTree() {
      String[] list = new String[techTree.size()];
      int numEntry = 0;
      for(Map.Entry<TECH_NAME, Integer> entry: techTree.entrySet()) {
         list[numEntry] = entry.getKey().toString() + ", cost: " + entry.getValue().toString();
         numEntry++;
      }
      return list;
   }
   
   public void createUnit(Tile t, UNIT_TYPE uType, Image i) {
      Unit newUnit;
      if(uType == UNIT_TYPE.TANK)
         newUnit = new Tank(t.getX(), t.getY(), t, this, i);
      else if(uType == UNIT_TYPE.TANK_DEST)
         newUnit = new TankDestroyer(t.getX(), t.getY(), t, this, i);
      else if(uType == UNIT_TYPE.INFANTRY)
         newUnit = new Infantry(t.getX(), t.getY(), t, this, i);
      else if(uType == UNIT_TYPE.RPG)
         newUnit = new RPGTeam(t.getX(), t.getY(), t, this, i);
      else
         return;
      productionHistory.put(uType, productionHistory.get(uType) + 1);
      units.add(newUnit);
      t.addUnit(newUnit);
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
      if(resources > this.resources)
         resourcesGathered += resources - this.resources;
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
   
   public int getColorNumber() {
      return colorNumber;
   }
   
   public void setColorNumber(int colorNumber) {
      this.colorNumber = colorNumber;
   }
   
   public void incrementResearchCount() {
      researchCount++;
   }
   
   public int getResearchCount() {
      return researchCount;
   }
   
   public void setResearchCount(int researchCount) {
      this.researchCount = researchCount;
   }
   
   public void incrementKillCount() {
      killCount++;
   }
   
   public int getKillCount() {
      return killCount;
   }
   
   public void setKillCount(int killCount) {
      this.killCount = killCount;
   }
   
   public void incrementDeathCount() {
      deathCount++;
   }
   
   public int getDeathCount() {
      return deathCount;
   }
   
   public void setDeathCount(int deathCount) {
      this.deathCount = deathCount;
   }
   
   public LinkedHashMap<UNIT_TYPE, Integer> getProductionHistory() {
      return productionHistory;
   }
   
   public void setProductionHistory(LinkedHashMap<UNIT_TYPE, Integer> productionHistory) {
      this.productionHistory = productionHistory;
   }
   
   public int getResourcesGathered() {
      return resourcesGathered;
   }
   
   public void setResourcesGathered(int resourcesGathered) {
      this.resourcesGathered = resourcesGathered;
   }
}
