package com.rolledback.terrain;

import java.awt.Image;
import java.util.LinkedHashMap;

import com.rolledback.framework.GraphicsManager;
import com.rolledback.framework.World;
import com.rolledback.teams.Team;
import com.rolledback.teams.technology.Technology.TECH_NAME;
import com.rolledback.units.Unit;
import com.rolledback.units.Unit.UNIT_TYPE;

/**
 * Factory tile class.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class Factory extends CapturableTile {
   
   private LinkedHashMap<UNIT_TYPE, Integer> productionList;
   private LinkedHashMap<TECH_NAME, Integer> techTree;
   private int resourceValue = 10;
   
   /**
    * Constructor.
    * 
    * @param w world that the tile exists in. Passed back to the Tile constructor.
    * @param x x position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param y y position of the tile in the world's tile matrix. Passed back to the Tile
    *           constructor.
    * @param o owner of the facotry. Passed back to the CapturableTile constructor.
    * @param t image texture for the tile.
    */
   public Factory(World w, int x, int y, Team o, Image t) {
      super(w, x, y, new TerrainEffect(0, 10, 1), 'F', o);
      productionList = new LinkedHashMap<UNIT_TYPE, Integer>();
      type = TILE_TYPE.FACTORY;
      texture = t;
      if(o != null)
         initLists();
   }
   
   /**
    * Sets the factory's production list (what units it can produce) and tech tree (what stuff it
    * can research) to the production list and tech tree of its owner. The production list is a
    * mapping of unit types to integer values which represent the cost to produce that unit type.
    * The tech tree is also a mapping, but of tech names to integer types which represent the cost
    * to research that technology. These functions are currently not used anywhere in the code
    * except by some of the AI implementations. The FactoryOption pane draws production lists and
    * tech tress from the team that opened the pane. This might be changed in the future.
    */
   public void initLists() {
      productionList = owner.getProductionList();
      techTree = owner.getTechTree();
   }
   
   public LinkedHashMap<UNIT_TYPE, Integer> getProductionList() {
      return this.productionList;
   }
   
   public LinkedHashMap<TECH_NAME, Integer> getTechTree() {
      return this.techTree;
   }
   
   public boolean produceUnit(UNIT_TYPE type) {
      if(owner.getResources() < productionList.get(type))
         return false;
      if(this.isOccupied())
         return false;
      owner.setResources(owner.getResources() - productionList.get(type));
      Image texture = GraphicsManager.typetoImage(type, owner.getTeamNumber());
      owner.createUnit(this, type, texture);
      owner.getUnits().get(owner.getUnits().size() - 1).setAttacked(true);
      owner.getUnits().get(owner.getUnits().size() - 1).setMoved(true);
      return true;
   }
   
   public void produceResources() {
      owner.setResources(owner.getResources() + resourceValue);
   }
   
   @Override
   public void capture(Unit unit) {
      if(owner != null)
         owner.getFactories().remove(this);
      owner = unit.getOwner();
      unit.getOwner().getFactories().add(this);
      if(owner.equals(getWorld().getTeamOne()))
         texture = GraphicsManager.getTileTextures().get("factoryRed.png");
      else
         texture = GraphicsManager.getTileTextures().get("factoryBlue.png");
      initLists();
   }
   
}
