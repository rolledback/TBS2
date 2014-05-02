package com.rolledback.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;

import com.rolledback.framework.Game.GAME_STATE;
import com.rolledback.teams.Team;
import com.rolledback.terrain.Tile;
import com.rolledback.units.Unit;

/**
 * The GameGUI object is used to display info about the current Game. The GameGUI object is an
 * extension of the JPanel class. This allows it to either be placed in the same window as the Game
 * (the current implementation), or in a separate window.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class GameGUI extends JPanel implements ActionListener {
   
   private static final long serialVersionUID = 1L;
   
   private Game game;
   
   private JPanel guiPanel;
   private BoxLayout guiLayout;
   
   private JPanel tauntPanel;
   private BoxLayout tauntLayout;
   public JTextArea tauntBox;
   public JTextField tauntField;
   
   private UnitPanel canvasPanel;
   private Image uImage;
   private Image tImage;
   
   private JPanel unitPanel;
   private GridLayout unitLayout;
   private JLabel unitName;
   private JLabel unitHealth;
   private JLabel unitDefense;
   private JLabel unitMove;
   
   private JPanel terrainPanel;
   private GridLayout terrainLayout;
   private JLabel terrainName;
   private JLabel attackBonus;
   private JLabel defenseBonus;
   private JLabel moveBonus;
   
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
   
   /**
    * Used to test the GUI. Will just create a place a GUI in it's own window with all starting
    * values present.
    * 
    * @param args
    */
   public static void main(String args[]) {
      JFrame test = new JFrame();
      GameGUI t = new GameGUI();
      test.add(t);
      test.pack();
      test.setVisible(true);
      t.fixComponents();
   }
   
   /**
    * Constructor. The GUI is NOT set to visible at the end of it's creation.
    */
   public GameGUI() {
      guiPanel = new JPanel();
      guiLayout = new BoxLayout(guiPanel, BoxLayout.X_AXIS);
      guiPanel.setLayout(guiLayout);
      
      setupTaunt();
      setupCanvas();
      setupUnit();
      setupTerrain();
      setupTeamOne();
      setupTeamTwo();
      setupButtonPanel();
      
      guiPanel.add(tauntPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(canvasPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(unitPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(terrainPanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(teamOnePanel);
      guiPanel.add(Box.createRigidArea(new Dimension(15, 0)));
      guiPanel.add(teamTwoPanel);
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
   
   /**
    * Sets the preferred size of each component of GUI to the size they have while holding their
    * placeholder/max length values.
    */
   public void fixComponents() {
      canvasPanel.setPreferredSize(new Dimension(canvasPanel.getWidth(), canvasPanel.getHeight()));
      unitPanel.setPreferredSize(new Dimension(unitPanel.getWidth(), unitPanel.getHeight()));
      terrainPanel.setPreferredSize(new Dimension(terrainPanel.getWidth(), terrainPanel.getHeight()));
      teamOnePanel.setPreferredSize(new Dimension(teamOnePanel.getWidth(), teamOnePanel.getHeight()));
      teamTwoPanel.setPreferredSize(new Dimension(teamTwoPanel.getWidth(), teamTwoPanel.getHeight()));
      buttonPanel.setPreferredSize(new Dimension(buttonPanel.getWidth(), buttonPanel.getHeight()));
      guiPanel.setPreferredSize(new Dimension(guiPanel.getWidth(), guiPanel.getHeight()));
   }
   
   /**
    * Sets up the component containing the taunt text box which allows teams to "taunt" each other.
    */
   public void setupTaunt() {
      tauntPanel = new JPanel();
      tauntLayout = new BoxLayout(tauntPanel, BoxLayout.Y_AXIS);
      tauntPanel.setLayout(tauntLayout);
      
      tauntBox = new JTextArea(5, 26);
      tauntBox.setEditable(false);
      DefaultCaret caret = (DefaultCaret)tauntBox.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      JScrollPane scrollPane = new JScrollPane(tauntBox);
      
      tauntField = new JTextField(26);
      tauntField.addActionListener(this);
      tauntField.setMaximumSize(tauntField.getPreferredSize());
      
      tauntPanel.add(scrollPane);
      tauntPanel.add(tauntField);
      tauntPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Chat Box"));
      tauntPanel.setBackground(new Color(190, 190, 190));
   }
   
   /**
    * Sets up the component containing the canvas which displays the texture of the current tile and
    * the image of the unit occupying that tile (if any).
    */
   public void setupCanvas() {
      canvasPanel = new UnitPanel();
      canvasPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Current Tile"));
      canvasPanel.setBackground(new Color(190, 190, 190));
   }
   
   /**
    * Sets up the panel containing the information about the current unit (if any).
    */
   public void setupUnit() {
      unitPanel = new JPanel();
      unitLayout = new GridLayout(6, 0, 0, 0);
      unitPanel.setLayout(unitLayout);
      
      unitName = new JLabel();
      unitName.setText("Type: TANK DESTROYER");
      
      unitHealth = new JLabel();
      unitHealth.setText("Health: 100");
      
      unitDefense = new JLabel();
      unitDefense.setText("Def: 100");
      
      unitMove = new JLabel();
      unitMove.setText("Move Range: 100");
      
      unitPanel.add(unitName);
      unitPanel.add(unitHealth);
      unitPanel.add(unitDefense);
      unitPanel.add(unitMove);
      
      unitPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Unit Info"));
      unitPanel.setBackground(new Color(190, 190, 190));
   }
   
   /**
    * Sets up the panel containing the information about the current tile.
    */
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
   
   /**
    * Sets up the panel containing the information about the Game's first team.
    */
   public void setupTeamOne() {
      teamOnePanel = new JPanel();
      teamOneLayout = new GridLayout(4, 0, 0, 0);
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
   
   /**
    * Sets up the panel containing the information about the Game's second team.
    */
   public void setupTeamTwo() {
      teamTwoPanel = new JPanel();
      teamTwoLayout = new GridLayout(4, 0, 0, 0);
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
   
   /**
    * Sets up the panel holding the end turn button. This button is currently NOT functional.
    */
   public void setupButtonPanel() {
      buttonPanel = new JPanel();
      buttonLayout = new BoxLayout(buttonPanel, BoxLayout.Y_AXIS);
      buttonPanel.setLayout(buttonLayout);
      
      endTurnButton = new JButton("End Turn");
      endTurnButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            if(game.getCurrentTeam().getClass().equals(Team.class))
               game.setState(GAME_STATE.SWITCH_TEAMS);
         }
      });
      buttonPanel.add(endTurnButton);
      buttonPanel.setBackground(new Color(190, 190, 190));
   }
   
   /**
    * Updates the info only for each team.
    * 
    * @param one the Game's first team.
    * @param two the Game's second team.
    */
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
   
   /**
    * Updates the info for each team, the current tile, and the current unit (if any).
    * 
    * @param unit the current unit, null if there isn't one.
    * @param tile the current tile.
    * @param one the Game's first team.
    * @param two the Game's second team.
    */
   public void updateInfo(Unit unit, Tile tile, Team one, Team two) {
      if(unit != null) {
         unitName.setText("Type: " + unit.getType().toString());
         unitHealth.setText("Health: " + unit.getHealth() + "/" + unit.getMaxHealth());
         unitDefense.setText("Def: " + Integer.toString(unit.getDefense()));
         unitMove.setText("Move Range: " + Integer.toString(unit.getMoveRange()));
         uImage = unit.getTexture();
      }
      else {
         unitName.setText("Type: ");
         unitHealth.setText("Health: ");
         unitDefense.setText("Def: ");
         unitMove.setText("Move Range: ");
         uImage = null;
      }
      
      if(tile != null) {
         terrainName.setText("Type: " + tile.getType().toString());
         attackBonus.setText("Atk Bonus: " + Integer.toString(tile.getEffect().getAttackBonus()));
         defenseBonus.setText("Def Bonus: " + Integer.toString(tile.getEffect().getDefenseBonus()));
         moveBonus.setText("Move Cost: " + Integer.toString(tile.getEffect().getMoveCost()));
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
   
   public void sendMessage(Team team, String msg) {
      tauntBox.append("[" + team.getName() + "] " + msg + "\n");
   }
   
   public void setGame(Game gamePanel) {
      game = gamePanel;
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
      sendMessage(game.getCurrentTeam(), tauntField.getText());
      tauntField.setText("");
   }
}

/**
 * @author Matthew
 * @version 1.0
 */
class UnitPanel extends JPanel {
   
   private static final long serialVersionUID = 1L;
   private Image unitImage = null;
   private int unitMax;
   private int unitMin;
   private Image terrainImage = GraphicsManager.getTileTextures().get("grass.png");
   private int canvasSize = 64;
   
   public UnitPanel() {
      this.setPreferredSize(new Dimension(canvasSize + (int)(canvasSize * .75), canvasSize));
   }
   
   @Override
   public void paintComponent(Graphics g) {
      g.drawImage(terrainImage, (int)((this.getWidth() - canvasSize) / 2.0), (int)((this.getHeight() - canvasSize) / 2.0), canvasSize, canvasSize, this);
      if(unitImage != null) {
         g.drawImage(unitImage, (int)((this.getWidth() - canvasSize) / 2.0), (int)((this.getHeight() - canvasSize) / 2.0), canvasSize, canvasSize, this);
         
         int buffer = (int)((double)canvasSize * .10);
         int xCorner = (int)(((this.getWidth() - canvasSize) / 2.0) + buffer);
         int yCorner = (int)(((this.getHeight() - canvasSize) / 2.0) + buffer);
         
         g.setColor(Color.red);
         g.fillRect(xCorner, yCorner, canvasSize - (2 * buffer), buffer);
         g.setColor(Color.green);
         g.fillRect(xCorner, yCorner, (int)((double)(canvasSize - (2 * buffer)) * (double)((double)unitMin / (double)unitMax)), buffer);
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