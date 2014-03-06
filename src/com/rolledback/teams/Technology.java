package com.rolledback.teams;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rolledback.terrain.City;
import com.rolledback.terrain.Factory;
import com.rolledback.terrain.Tile.TILE_TYPE;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

public class Technology {

	// APCR Shells
	// GPS Navigation
	// Artillery Barrage
	// Conscription
	// Stimulus Package
	// Fortifications
	// Militia

	private UNIT_TYPE unitType;
	private TILE_TYPE tileType;
	private int attackValue;
	private int defenseValue;
	private int moveValue;
	private TechnologyEffect techEffect;
	private Team effectedTeam;

	public enum TECH_NAME {
		APCR, GPS, ART, CON, STIM, FORT, MILI;

		public String toString() {
			if (this.equals(ART))
				return "Artillery Barrage";
			if (this.equals(MILI))
				return "Militia";
			if (this.equals(CON))
				return "Conscription";
			else
				return null;
		}

		public static TECH_NAME stringToName(String s) {
			if (s.equals("Artillery Barrage"))
				return ART;
			if (s.equals("Militia"))
				return MILI;
			if (s.equals("Conscription"))
				return CON;
			else
				return null;
		}
	}

	public static void researchTech(Team researcher, TECH_NAME name) {
		if (name == TECH_NAME.MILI) {
			researcher.getResearchedTechs().add(new Technology(researcher, new TechnologyEffect(researcher, null) {
				public void run() {
					for (City c : ((Team) effectObjectOne).getCities())
						if (!c.isOccupied())
							((Team) effectObjectOne).createUnit(c, UNIT_TYPE.INFANTRY);
				}
			}));
			researcher.getTechTree().remove(TECH_NAME.MILI);
		} else if (name == TECH_NAME.ART) {
			researcher.getResearchedTechs().add(new Technology(researcher, new TechnologyEffect(researcher, null) {
				public void run() {
					for (Unit u : (((Team) effectObjectOne).getOpponent()).getUnits())
						u.takeDamage(20);
				}
			}));
			researcher.getTechTree().remove(TECH_NAME.ART);
		} else if (name == TECH_NAME.CON) {
			Technology conscription = new Technology(researcher, UNIT_TYPE.INFANTRY, .50);
			researcher.getTechTree().remove(TECH_NAME.CON);
		}

	}

	// unit mod tech
	public Technology(Team t, UNIT_TYPE unit, int a, int d, int m) {
		effectedTeam = t;
		unitType = unit;
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
		for (Map.Entry<UNIT_TYPE, Integer> entry : effectedTeam.getProductionList().entrySet())
			if (entry.getKey() == type || type == UNIT_TYPE.ALL)
				entry.setValue(entry.getValue() - (int) (entry.getValue() * discount));
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

	public UNIT_TYPE getUnitType() {
		return unitType;
	}

	public void setUnitType(UNIT_TYPE unitType) {
		this.unitType = unitType;
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
