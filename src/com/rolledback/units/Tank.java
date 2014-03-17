package com.rolledback.units;

import java.awt.Image;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class Tank extends Unit {
   
   public Tank(int x, int y, Tile t, Team o, Image lI, Image rI) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minInfantryAttack = 35;
      maxInfantryAttack = 40;
      minVehicleAttack = 85;
      maxVehicleAttack = 90;
      defense = 20;
      moveRange = 5;
      type = UNIT_TYPE.TANK;
      leftTexture = lI;
      rightTexture = rI;
   }
   
}
