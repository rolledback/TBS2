package com.rolledback.framework;

import java.util.ArrayList;
import com.rolledback.terrain.*;
import com.rolledback.units.*;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Team {
	
	private ArrayList<Unit> units;
	String name;
	int teamSize;
	private int resources;
	
	public Team(String name, int size, int r) {
		units = new ArrayList<Unit>();
		teamSize = size;
		this.name = name;
		resources = r;
	}
	
	public void createUnit(Tile t, UNIT_TYPE uType) {
		if (uType == UNIT_TYPE.TANK)
			units.add(new Tank(t.getX(), t.getY(), t, this));
		if (uType == UNIT_TYPE.TANK_DEST)
			units.add(new TankDestroyer(t.getX(), t.getY(), t, this));
		if (uType == UNIT_TYPE.INFANTRY)
			units.add(new Infantry(t.getX(), t.getY(), t, this));
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
		return "Team: " + name + "\nResources: " + resources + "\nNum units: " + units.size();
	}
	
	public int getResources() {
		return resources;
	}
	
	public void setResources(int resources) {
		this.resources = resources;
	}
	
}
