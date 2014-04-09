package com.rolledback.units;

import java.awt.Image;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

/**
 * Tank Destroyer class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 * 
 */
public class TankDestroyer extends Unit {
   
   /**
    * Constructor.
    * 
    * @param x starting x position of the unit.
    * @param y starting y position of the unit.
    * @param t tile that the unit is to be placed on.
    * @param o team that the unit belongs to.
    * @param lI left facing image.
    * @param rI right facing image.
    */
   public TankDestroyer(int x, int y, Tile t, Team o, Image i) {
      super(x, y, t, o);
      classification = UNIT_CLASS.VEHICLE;
      defense = 15;
      moveRange = 4;
      type = UNIT_TYPE.TANK_DEST;
      texture = i;
   }
   
}