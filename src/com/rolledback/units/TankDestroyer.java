package com.rolledback.units;

import java.awt.Image;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class TankDestroyer extends Unit {
   
   public TankDestroyer(int x, int y, Tile t, Team o, Image lI, Image rI) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      minInfantryAttack = 35;
      maxInfantryAttack = 40;
      minVehicleAttack = 85;
      maxVehicleAttack = 90;
      defense = 15;
      moveRange = 4;
      type = UNIT_TYPE.TANK_DEST;
      leftTexture = lI;
      rightTexture = rI;
   }
   
}