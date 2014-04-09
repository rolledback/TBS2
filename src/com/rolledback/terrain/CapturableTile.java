package com.rolledback.terrain;

import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

/**
 * An abstract extension of the Tile class that abstractly defines the methods needed for a tile to
 * be capturable and implements the basic ones.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public abstract class CapturableTile extends Tile {
   
   protected Team owner;
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in. Passed back to the Tile constructor.
    * @param x x position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param y y position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param e terrain effect associated with the tile. Passed back to the Tile constructor.
    * @param m character representation of the tile. Passed back to the Tile constructor.
    * @param o owner of the tile.
    */
   public CapturableTile(World w, int x, int y, TerrainEffect e, char m, Team o) {
      super(w, x, y, e, m);
      this.owner = o;
   }
   
   /**
    * Implements the logic to be performed whenever the tile is capture.
    * 
    * @param unit
    */
   public abstract void capture(Unit unit);
   
   public Team getOwner() {
      return owner;
   }
   
   public void setOwner(Team owner) {
      this.owner = owner;
   }
   
}
