package com.rolledback.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.rolledback.teams.Team;
import com.rolledback.teams.technology.Technology.TECH_NAME;
import com.rolledback.units.Unit.UNIT_TYPE;

/**
 * A FactoryOptionPane is created every time a human user clicks on a factory. It allows the user to
 * create new units are research new technologies. As an extension of the JDialog class, a Game
 * cannot continue to execute until the FactoryOptionPane has been closed.
 * 
 * @author Matthew Rayermann (rolledback, www.github.com/rolledback, www.cs.utexas.edu/~mrayer)
 * @version 1.0
 */
public class FactoryOptionPane extends JDialog {
   
   private static final long serialVersionUID = 1L;
   private JTabbedPane tabbedPane;
   
   private GridLayout unitLayout;
   private JPanel unitPanel;
   private JButton unitOkButton;
   private JButton unitCancelButton;
   private JLabel unitCurrRes;
   
   private GridLayout techLayout;
   private JPanel techPanel;
   private JButton techOkButton;
   private JButton techCancelButton;
   private JLabel techCurrRes;
   private JComboBox<String> techList;
   
   private UNIT_TYPE returnedUnit;
   private TECH_NAME returnedTech;
   private boolean unitChoiceMade;
   private boolean techChoiceMade;
   private JComboBox<String> unitList;
   
   private int numResources;
   
   /**
    * Constructor. The pane will is set to visible in the constructor.
    * 
    * @param caller the team that the options for the pane should come from.
    */
   public FactoryOptionPane(Team caller) {
      setTitle("Factory");
      setSize(225, 150);
      unitChoiceMade = false;
      techChoiceMade = false;
      numResources = caller.getResources();
      
      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BorderLayout());
      getContentPane().add(topPanel);
      
      createUnitPage(caller.getProductionList());
      createTechPage(caller.getTechTree());
      
      tabbedPane = new JTabbedPane();
      tabbedPane.addTab("Units", unitPanel);
      tabbedPane.addTab("Technology", techPanel);
      topPanel.add(tabbedPane);
      
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setResizable(false);
      setModal(true);
      
      // Get the size of the screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      
      // Determine the new location of the window
      int w = this.getSize().width;
      int h = this.getSize().height;
      int x = (dim.width - w) / 2;
      int y = (dim.height - h) / 2;
      
      // Move the window
      setLocation(x, y);
      pack();
      setVisible(true);
   }
   
   /**
    * Creates the card for unit production. Contains a dropdown of units that can be produced, two
    * buttons, and a label of how many resources the caller team currently has. Hitting the ok
    * button will do a check to make sure the caller team has enough resources for the unit.
    * 
    * @param productionList list of units that the caller team can produce mapped to Integer values
    *           of how much they will cost in resources.
    */
   public void createUnitPage(LinkedHashMap<UNIT_TYPE, Integer> productionList) {
      unitLayout = new GridLayout(0, 1, 5, 5);
      unitPanel = new JPanel(unitLayout);
      final String[] unitNames = new String[productionList.size()];
      int counter = 0;
      for(Map.Entry<UNIT_TYPE, Integer> entry: productionList.entrySet()) {
         unitNames[counter] = entry.getKey().toString() + ", " + entry.getValue().toString();
         counter++;
      }
      unitList = new JComboBox<String>(unitNames);
      unitList.setSelectedIndex(0);
      returnedUnit = UNIT_TYPE.stringToType(unitNames[0].split(",")[0]);
      unitList.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            returnedUnit = UNIT_TYPE.stringToType(unitNames[unitList.getSelectedIndex()].split(",")[0]);
         }
      });
      
      unitOkButton = new JButton("Ok");
      unitOkButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            techChoiceMade = false;
            unitChoiceMade = true;
            if(Integer.parseInt(unitNames[unitList.getSelectedIndex()].substring(unitNames[unitList.getSelectedIndex()].lastIndexOf(" ") + 1)) > numResources)
               JOptionPane.showMessageDialog(new JFrame(), "Not enough resources.", "Error", JOptionPane.ERROR_MESSAGE);
            else {
               setVisible(false);
               dispose();
            }
         }
      });
      
      unitCancelButton = new JButton("Cancel");
      unitCancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
            dispose();
            techChoiceMade = false;
            unitChoiceMade = false;
         }
      });
      
      unitCurrRes = new JLabel("Resources: " + numResources);
      unitCurrRes.setHorizontalAlignment(SwingConstants.CENTER);
      
      unitPanel.add(unitCurrRes);
      unitPanel.add(unitList);
      unitPanel.add(unitOkButton);
      unitPanel.add(unitCancelButton);
   }
   
   /**
    * Creates the card for technology research. Contains a dropdown of technologies that can be
    * researched, two buttons, and a label of how many resources the caller team currently has.
    * Hitting the ok button will do a check to make sure the caller team has enough resources for
    * the technology.
    * 
    * @param techTree list of technologies that the caller team can reduce mapped to Integer values
    *           of how much the technology cost in resources.
    */
   public void createTechPage(LinkedHashMap<TECH_NAME, Integer> techTree) {
      techLayout = new GridLayout(0, 1, 5, 5);
      techPanel = new JPanel(techLayout);
      final String[] techNames;
      techNames = new String[techTree.size()];
      int counter = 0;
      for(Map.Entry<TECH_NAME, Integer> entry: techTree.entrySet()) {
         techNames[counter] = entry.getKey().toString() + ", " + entry.getValue().toString();
         counter++;
      }
      
      techList = new JComboBox<String>(techNames);
      if(techNames.length > 0)
         returnedTech = TECH_NAME.stringToName(techNames[0].split(",")[0]);
      techList.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            returnedTech = TECH_NAME.stringToName(techNames[techList.getSelectedIndex()].split(",")[0]);
         }
      });
      
      techOkButton = new JButton("Ok");
      techOkButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            unitChoiceMade = false;
            techChoiceMade = true;
            if(techNames.length == 0)
               JOptionPane.showMessageDialog(new JFrame(), "No technologies available.", "Error", JOptionPane.ERROR_MESSAGE);
            else if(Integer.parseInt(techNames[techList.getSelectedIndex()].substring(techNames[techList.getSelectedIndex()].lastIndexOf(" ") + 1)) > numResources)
               JOptionPane.showMessageDialog(new JFrame(), "Not enough resources.", "Error", JOptionPane.ERROR_MESSAGE);
            else {
               setVisible(false);
               dispose();
            }
         }
      });
      
      techCancelButton = new JButton("Cancel");
      techCancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            setVisible(false);
            dispose();
            techChoiceMade = false;
            unitChoiceMade = false;
         }
      });
      
      techCurrRes = new JLabel("Resources: " + numResources);
      techCurrRes.setHorizontalAlignment(SwingConstants.CENTER);
      
      techPanel.add(techCurrRes);
      techPanel.add(techList);
      techPanel.add(techOkButton);
      techPanel.add(techCancelButton);
   }
   
   public UNIT_TYPE getReturnedUnit() {
      return returnedUnit;
   }
   
   public void setReturnedUnit(UNIT_TYPE returnedUnit) {
      this.returnedUnit = returnedUnit;
   }
   
   public boolean isUnitChoiceMade() {
      return unitChoiceMade;
   }
   
   public void setUnitChoiceMade(boolean unitChoiceMade) {
      this.unitChoiceMade = unitChoiceMade;
   }
   
   public TECH_NAME getReturnedTech() {
      return returnedTech;
   }
   
   public void setReturnedTech(TECH_NAME returnedTech) {
      this.returnedTech = returnedTech;
   }
   
   public boolean isTechChoiceMade() {
      return techChoiceMade;
   }
   
   public void setTechChoiceMade(boolean techChoiceMade) {
      this.techChoiceMade = techChoiceMade;
   }
}
