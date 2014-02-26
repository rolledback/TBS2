package com.rolledback.terrain;

import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.units.Unit;

public abstract class CapturableTile extends Tile {
   
   protected Team owner;
   
   public CapturableTile(World w, int x, int y, TerrainEffect e, char m, Team o) {
      super(w, x, y, e, m);
      this.owner = o;
   }
   
   public abstract void capture(Unit unit);

   public Team getOwner() {
      return owner;
   }

   public void setOwner(Team owner) {
      this.owner = owner;
   }

}
