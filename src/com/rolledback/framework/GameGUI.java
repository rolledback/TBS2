package com.rolledback.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;

public class GameGUI extends JPanel {
   
   private static final long serialVersionUID = 1L;
   
   private JPanel guiPanel;
   private BoxLayout guiLayout;
   
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
   
   private JPanel teamInfoPanel;
   private GridLayout teamLayout;
   
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
   
   private JPanel buttonPanel;
   private BoxLayout buttonLayout;
   private JButton endTurnButton;
   
   public static void main(String args[]) {
      JFrame test = new JFrame();
      GameGUI t = new GameGUI();
      test.add(t);
      test.pack();
      test.setVisible(true);
      t.fixComponents();
   }
   
   public GameGUI() {
      guiPanel = new JPanel();
      guiLayout = new BoxLayout(guiPanel, BoxLayout.X_AXIS);
      guiPanel.setLayout(guiLayout);
      
      setupCanvas();
      setupUnit();
      setupTerrain();
      setupTeamPanel();
      setupButtonPanel();
      
      guiPanel.add(canvasPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(unitPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(terrainPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(teamInfoPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(buttonPanel);
      guiPanel.setBackground(new Color(190, 190, 190));
      add(guiPanel);
      
      setBackground(new Color(190, 190, 190));
      LineBorder borderOne = new LineBorder(Color.BLACK, 1);
      EmptyBorder borderTwo = new EmptyBorder(10, 10, 10, 10);
      setBorder(new CompoundBorder(borderOne, borderTwo));
   }
   
   @Override
   public void paintComponent(Graphics g) {
      setBackground(new Color(190, 190, 190));
      super.paintComponent(g);
   }
   
   public void fixComponents() {
      canvasPanel.setPreferredSize(new Dimension(canvasPanel.getWidth(), canvasPanel.getHeight()));
      unitPanel.setPreferredSize(new Dimension(unitPanel.getWidth(), unitPanel.getHeight()));
      terrainPanel.setPreferredSize(new Dimension(terrainPanel.getWidth(), terrainPanel.getHeight()));
      teamOnePanel.setPreferredSize(new Dimension(teamOnePanel.getWidth(), teamOnePanel.getHeight()));
      teamTwoPanel.setPreferredSize(new Dimension(teamTwoPanel.getWidth(), teamTwoPanel.getHeight()));
      buttonPanel.setPreferredSize(new Dimension(buttonPanel.getWidth(), buttonPanel.getHeight()));
      guiPanel.setPreferredSize(new Dimension(guiPanel.getWidth(), guiPanel.getHeight()));
   }
   
   public void setupCanvas() {
      canvasPanel = new UnitPanel();
      canvasPanel.setPreferredSize(new Dimension(164, 164));
      canvasPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Current Tile & Unit"));
      canvasPanel.setBackground(new Color(190, 190, 190));
   }
   
   public void setupUnit() {
      unitPanel = new JPanel();
      unitLayout = new GridLayout(6, 0, 0, 0);
      unitPanel.setLayout(unitLayout);
      
      unitName = new JLabel();
      unitName.setText("Type: TANK DESTROYER");
      
      unitHealth = new JLabel();
      unitHealth.setText("Health: 100");
      
      unitInfAttack = new JLabel();
      unitInfAttack.setText("Inf Atk: 100-100");
      
      unitVehAttack = new JLabel();
      unitVehAttack.setText("Veh Atk: 100-100");
      
      unitDefense = new JLabel();
      unitDefense.setText("Def: 100");
      
      unitMove = new JLabel();
      unitMove.setText("Move Range: 100");
      
      unitPanel.add(unitName);
      unitPanel.add(unitHealth);
      unitPanel.add(unitInfAttack);
      unitPanel.add(unitVehAttack);
      unitPanel.add(unitDefense);
      unitPanel.add(unitMove);
      
      unitPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Unit Info"));
      unitPanel.setBackground(new Color(190, 190, 190));
   }
   
   public void setupTerrain() {
      terrainPanel = new JPanel();
      terrainLayout = new GridLayout(6, 0, 0, 0);
      terrainPanel.setLayout(terrainLayout);
      
      terrainName = new JLabel();
      terrainName.setText("Type: MOUNTAIN TEMP");
      
      attackBonus = new JLabel();
      attackBonus.setText("Atk Bonus: 100");
      
      defenseBonus = new JLabel();
      defenseBonus.setText("Def Bonus: 100");
      
      moveBonus = new JLabel();
      moveBonus.setText("Move Bonus: 100");
      
      terrainPanel.add(terrainName);
      terrainPanel.add(attackBonus);
      terrainPanel.add(defenseBonus);
      terrainPanel.add(moveBonus);
      
      terrainPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Terrain Info"));
      terrainPanel.setBackground(new Color(190, 190, 190));
   }
   
   public void setupTeamPanel() {
      teamInfoPanel = new JPanel();
      
      setupTeamOne();
      setupTeamTwo();
      
      // define south layout
      teamLayout = new GridLayout(2, 1, 2, 2);
      teamInfoPanel.setLayout(teamLayout);
      
      // combine southern components
      teamInfoPanel.add(teamOnePanel);
      teamInfoPanel.add(teamTwoPanel);
      teamInfoPanel.setBackground(new Color(255, 255, 255, 0));
   }
   
   public void setupTeamOne() {
      teamOnePanel = new JPanel();
      teamOneLayout = new GridLayout(4, 0, 2, 2);
      teamOnePanel.setLayout(teamOneLayout);
      
      nameOne = new JLabel();
      nameOne.setText("Name: TEAM ONE CPU VER D");
      
      resOne = new JLabel();
      resOne.setText("Res: 1000000");
      
      numCitiesOne = new JLabel();
      numCitiesOne.setText("Num Cities: 100");
      
      numFactOne = new JLabel();
      numFactOne.setText("Num Factories: 100");
      
      teamOnePanel.add(nameOne);
      teamOnePanel.add(resOne);
      teamOnePanel.add(numCitiesOne);
      teamOnePanel.add(numFactOne);
      
      teamOnePanel.setBorder(new TitledBorder(new LineBorder(Color.RED, 1), "Team One Info"));
      teamOnePanel.setBackground(new Color(190, 190, 190));
   }
   
   public void setupTeamTwo() {
      teamTwoPanel = new JPanel();
      teamTwoLayout = new GridLayout(4, 0, 2, 2);
      teamTwoPanel.setLayout(teamTwoLayout);
      
      nameTwo = new JLabel();
      nameTwo.setText("Name: TEAM TWO CPU VER D");
      
      resTwo = new JLabel();
      resTwo.setText("Res: 1000000");
      
      numCitiesTwo = new JLabel();
      numCitiesTwo.setText("Num Cities: 100");
      
      numFactTwo = new JLabel();
      numFactTwo.setText("Num Factories: 100");
      
      teamTwoPanel.add(nameTwo);
      teamTwoPanel.add(resTwo);
      teamTwoPanel.add(numCitiesTwo);
      teamTwoPanel.add(numFactTwo);
      
      teamTwoPanel.setBorder(new TitledBorder(new LineBorder(Color.BLUE, 1), "Team Two Info"));
      teamTwoPanel.setBackground(new Color(190, 190, 190));
   }
   
   public void setupButtonPanel() {
      buttonPanel = new JPanel();
      buttonLayout = new BoxLayout(buttonPanel, BoxLayout.Y_AXIS);
      buttonPanel.setLayout(buttonLayout);
      
      endTurnButton = new JButton("End Turn");
      buttonPanel.add(endTurnButton);
      buttonPanel.setBackground(new Color(190, 190, 190));
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
      g.drawImage(terrainImage, (int)((this.getWidth() - 128) / 2.0), (int)((this.getHeight() - 128) / 2.0), 128, 128, this);
      if(unitImage != null) {
         g.drawImage(unitImage, (int)((this.getWidth() - 128) / 2.0), (int)((this.getHeight() - 128) / 2.0), 128, 128, this);
         
         int buffer = (int)((double)128 * .10);
         int xCorner = (int)(((this.getWidth() - 128) / 2.0) + buffer);
         int yCorner = (int)(((this.getHeight() - 128) / 2.0) + buffer);
         
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