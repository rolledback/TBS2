package com.rolledback.units;

import java.awt.Image;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;

/**
 * Infantry class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 * 
 */
public class Infantry extends Unit {
   
   /**
    * Constructor. 
    * @param x starting x position of the unit.
    * @param y starting y position of the unit.
    * @param t tile that the unit is to be placed on.
    * @param o team that the unit belongs to.
    * @param lI left facing image.
    * @param rI right facing image.
    */
   public Infantry(int x, int y, Tile t, Team o, Image lI, Image rI) {
      super(x, y, t, o);
      classification = UNIT_CLASS.INFANTRY;
      defense = 10;
      moveRange = 3;
      type = UNIT_TYPE.INFANTRY;
      leftTexture = lI;
      rightTexture = rI;
   }
   
}