package com.rolledback.units;

import java.awt.Image;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

public class Infantry extends Unit {
   
   public Infantry(int x, int y, Tile t, Team o, Image lI, Image rI) {
      super(x, y, t, o);
      classification = UNIT_CLASS.INFANTRY;
      minInfantryAttack = 70;
      maxInfantryAttack = 75;
      minVehicleAttack = 25;
      maxVehicleAttack = 30;
      defense = 10;
      moveRange = 3;
      type = UNIT_TYPE.INFANTRY;
      leftTexture = lI;
      rightTexture = rI;
   }
   
}