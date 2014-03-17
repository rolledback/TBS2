package com.rolledback.framework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;

public class GameGUI extends JFrame {
   
   private static final long serialVersionUID = 1L;
   
   private JPanel guiPanel;
   private BorderLayout guiLayout;
   
   private JPanel northInfoPanel;
   private BoxLayout northLayout;
   
   private UnitPanel canvasPanel;
   private Image uImage;
   private Image tImage;
   
   private JPanel unitPanel;
   private GridLayout unitLayout;
   
   private JLabel unitName;
   private JLabel unitHealth;
   private JLabel unitInfAttack;
   private JLabel unitVehAttack;
   private JLabel unitDefense;
   private JLabel unitMove;
   private JPanel terrainPanel;
   
   private GridLayout terrainLayout;
   private JLabel terrainName;
   private JLabel attackBonus;
   private JLabel defenseBonus;
   private JLabel moveBonus;
   
   private JPanel southInfoPanel;
   private BoxLayout southLayout;
   
   private JPanel teamOnePanel;
   private GridLayout teamOneLayout;
   private JLabel nameOne;
   private JLabel resOne;
   private JLabel numCitiesOne;
   private JLabel numFactOne;
   
   private JPanel teamTwoPanel;
   private GridLayout teamTwoLayout;
   private JLabel nameTwo;
   private JLabel resTwo;
   private JLabel numCitiesTwo;
   private JLabel numFactTwo;
   
   public GameGUI() {
      guiPanel = new JPanel();
      this.setupNorth();
      this.setupSouth();
      
      guiLayout = new BorderLayout();
      guiLayout.setVgap(1);
      guiPanel.setLayout(guiLayout);
      
      guiPanel.add(northInfoPanel, BorderLayout.PAGE_START);
      guiPanel.add(southInfoPanel, BorderLayout.PAGE_END);
      guiPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      this.setPreferredSize(new Dimension(460, 320));
      this.add(guiPanel);
      this.setVisible(true);
   }
   
   public void setupNorth() {
      northInfoPanel = new JPanel();
      
      setupCanvas();
      setupUnit();
      setupTerrain();
      
      // define north layout
      northLayout = new BoxLayout(northInfoPanel, BoxLayout.LINE_AXIS);
      northInfoPanel.setLayout(northLayout);
      
      // combine northern components
      northInfoPanel.add(canvasPanel);
      northInfoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      northInfoPanel.add(unitPanel);
      northInfoPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      northInfoPanel.add(terrainPanel);
   }
   
   public void setupCanvas() {
      canvasPanel = new UnitPanel();
      canvasPanel.setPreferredSize(new Dimension(164, 164));
      canvasPanel.setBorder(new TitledBorder("Current Unit"));
   }
   
   public void setupUnit() {
      unitPanel = new JPanel();
      unitLayout = new GridLayout(6, 0, 0, 0);
      unitPanel.setLayout(unitLayout);
      
      unitName = new JLabel();
      unitName.setText("Type: ");
      
      unitHealth = new JLabel();
      unitHealth.setText("Health: ");
      
      unitInfAttack = new JLabel();
      unitInfAttack.setText("Inf Atk: ");
      
      unitVehAttack = new JLabel();
      unitVehAttack.setText("Veh Atk: ");
      
      unitDefense = new JLabel();
      unitDefense.setText("Def: ");
      
      unitMove = new JLabel();
      unitMove.setText("Move Range: ");
      
      unitPanel.add(unitName);
      unitPanel.add(unitHealth);
      unitPanel.add(unitInfAttack);
      unitPanel.add(unitVehAttack);
      unitPanel.add(unitDefense);
      unitPanel.add(unitMove);
      
      unitPanel.setBorder(new TitledBorder("Unit Info"));
   }
   
   public void setupTerrain() {
      terrainPanel = new JPanel();
      terrainLayout = new GridLayout(6, 0, 0, 0);
      terrainPanel.setLayout(terrainLayout);
      
      terrainName = new JLabel();
      terrainName.setText("Type: ");
      
      attackBonus = new JLabel();
      attackBonus.setText("Atk Bonus: ");
      
      defenseBonus = new JLabel();
      defenseBonus.setText("Def Bonus: ");
      
      moveBonus = new JLabel();
      moveBonus.setText("Move Bonus: ");
      
      terrainPanel.add(terrainName);
      terrainPanel.add(attackBonus);
      terrainPanel.add(defenseBonus);
      terrainPanel.add(moveBonus);
      
      terrainPanel.setBorder(new TitledBorder("Terrain Info"));
   }
   
   public void setupSouth() {
      southInfoPanel = new JPanel();
      
      setupTeamOne();
      setupTeamTwo();
      
      // define south layout
      southLayout = new BoxLayout(southInfoPanel, BoxLayout.LINE_AXIS);
      southInfoPanel.setLayout(southLayout);
      
      // combine southern components
      southInfoPanel.add(teamOnePanel);
      southInfoPanel.add(Box.createRigidArea(new Dimension(15, 25)));
      southInfoPanel.add(teamTwoPanel);
   }
   
   public void setupTeamOne() {
      teamOnePanel = new JPanel();
      teamOneLayout = new GridLayout(4, 0, 2, 2);
      teamOnePanel.setLayout(teamOneLayout);
      
      nameOne = new JLabel();
      nameOne.setText("Name: ");
      
      resOne = new JLabel();
      resOne.setText("Res: ");
      
      numCitiesOne = new JLabel();
      numCitiesOne.setText("Num Cities: ");
      
      numFactOne = new JLabel();
      numFactOne.setText("Num Factories: ");
      
      teamOnePanel.add(nameOne);
      teamOnePanel.add(resOne);
      teamOnePanel.add(numCitiesOne);
      teamOnePanel.add(numFactOne);
      
      teamOnePanel.setBorder(new TitledBorder(new LineBorder(Color.RED, 1), "Team One Info"));
      
   }
   
   public void setupTeamTwo() {
      teamTwoPanel = new JPanel();
      teamTwoLayout = new GridLayout(4, 0, 2, 2);
      teamTwoPanel.setLayout(teamTwoLayout);
      
      nameTwo = new JLabel();
      nameTwo.setText("Name: ");
      
      resTwo = new JLabel();
      resTwo.setText("Res: ");
      
      numCitiesTwo = new JLabel();
      numCitiesTwo.setText("Num Cities: ");
      
      numFactTwo = new JLabel();
      numFactTwo.setText("Num Factories: ");
      
      teamTwoPanel.add(nameTwo);
      teamTwoPanel.add(resTwo);
      teamTwoPanel.add(numCitiesTwo);
      teamTwoPanel.add(numFactTwo);
      
      teamTwoPanel.setBorder(new TitledBorder(new LineBorder(Color.BLUE, 1), "Team Two Info"));
   }
   
   public void updateInfo(Team one, Team two) {
      nameOne.setText("Name: " + one.getName());
      resOne.setText("Num Resources: " + one.getResources());
      numCitiesOne.setText("Num Citiies: " + one.getCities().size());
      numFactOne.setText("Num Factories: " + one.getFactories().size());
      
      nameTwo.setText("Name: " + two.getName());
      resTwo.setText("Num Resources: " + two.getResources());
      numCitiesTwo.setText("Num Citiies: " + two.getCities().size());
      numFactTwo.setText("Num Factories: " + two.getFactories().size());
   }
   
   public void updateInfo(Unit unit, Tile tile, Team one, Team two) {
      if(unit != null) {
         unitName.setText("Type: " + unit.getType().toString());
         unitHealth.setText("Health: " + unit.getHealth() + "/" + unit.getMaxHealth());
         unitInfAttack.setText("Inf Atk: " + unit.getMinInfantryAttack() + "-" + unit.getMaxInfantryAttack());
         unitVehAttack.setText("Veh Atk: " + unit.getMinVehicleAttack() + "-" + unit.getMaxVehicleAttack());
         unitDefense.setText("Def: " + Integer.toString(unit.getDefense()));
         unitMove.setText("Move Range: " + Integer.toString(unit.getMoveRange()));
         uImage = unit.getRightTexture();
      }
      else {
         unitName.setText("Type: ");
         unitHealth.setText("Health: ");
         unitInfAttack.setText("Inf Atk: ");
         unitVehAttack.setText("Veh Atk: ");
         unitDefense.setText("Def: ");
         unitMove.setText("Move Range: ");
         uImage = null;
      }
      
      if(tile != null) {
         terrainName.setText("Type: " + tile.getType().toString());
         attackBonus.setText("Atk Bonus: " + Integer.toString(tile.getEffect().getAttackBonus()));
         defenseBonus.setText("Def Bonus: " + Integer.toString(tile.getEffect().getDefenseBonus()));
         moveBonus.setText("Move Bonus: " + Integer.toString(tile.getEffect().getMoveBonus()));
         tImage = tile.getTexture();
      }
      else {
         terrainName.setText("Type: ");
         attackBonus.setText("Atk Bonus: ");
         defenseBonus.setText("Def Bonus: ");
         moveBonus.setText("Move Bonus: ");
         tImage = GraphicsManager.getTileTextures().get("grass.png");
      }
      
      nameOne.setText("Name: " + one.getName());
      resOne.setText("Num Resources: " + one.getResources());
      numCitiesOne.setText("Num Citiies: " + one.getCities().size());
      numFactOne.setText("Num Factories: " + one.getFactories().size());
      
      nameTwo.setText("Name: " + two.getName());
      resTwo.setText("Num Resources: " + two.getResources());
      numCitiesTwo.setText("Num Citiies: " + two.getCities().size());
      numFactTwo.setText("Num Factories: " + two.getFactories().size());
      
      if(unit != null)
         canvasPanel.setUnitImage(uImage, unit.getHealth(), unit.getMaxHealth());
      
      else
         canvasPanel.setUnitImage(null, 0, 0);
      
      canvasPanel.setTerrainImage(tImage);
      canvasPanel.repaint();
   }
}

class UnitPanel extends JPanel {
   
   private static final long serialVersionUID = 1L;
   private Image unitImage = null;
   private int unitMax;
   private int unitMin;
   private Image terrainImage = GraphicsManager.getTileTextures().get("grass.png");
   
   public void paintComponent(Graphics g) {
      g.drawImage(terrainImage, 20, 20, 128, 128, this);
      if(unitImage != null) {
         g.drawImage(unitImage, 20, 20, 128, 128, this);
         
         int buffer = (int)((double)128 * .10);
         int xCorner = 20 + buffer;
         int yCorner = 20 + buffer;
         
         g.setColor(Color.red);
         g.fillRect(xCorner, yCorner, 128 - (2 * buffer), buffer);
         g.setColor(Color.green);
         g.fillRect(xCorner, yCorner, (int)((double)(128 - (2 * buffer)) * (double)((double)unitMin / (double)unitMax)), buffer);
      }
   }
   
   public Image getUnitImage() {
      return unitImage;
   }
   
   public void setUnitImage(Image unitImage, int min, int max) {
      this.unitMax = max;
      this.unitMin = min;
      this.unitImage = unitImage;
   }
   
   public Image getTerrainImage() {
      return terrainImage;
   }
   
   public void setTerrainImage(Image terrainImage) {
      this.terrainImage = terrainImage;
   }
   
}