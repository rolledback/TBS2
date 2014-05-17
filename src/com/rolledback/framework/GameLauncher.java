package com.rolledback.framework;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class GameLauncher extends JFrame {
   
   private static final long serialVersionUID = 1L;
   private JPanel mainPanel;
   private BoxLayout mainLayout;
   private JLabel title;
   
   private JPanel teamOneOptions;
   private BoxLayout teamOneLayout;
   
   private JPanel teamOneNamePanel;
   private BoxLayout teamOneNameLayout;
   private JLabel teamOneNameLabel;
   private JTextField teamOneName;
   
   private JPanel teamOneTypePanel;
   private BoxLayout teamOneTypeLayout;
   private JLabel teamOneTypeLabel;
   private JComboBox<String> teamOneType;
   
   private JPanel teamOneResourcePanel;
   private BoxLayout teamOneResourceLayout;
   private JLabel teamOneResourcesLabel;
   private JTextField teamOneResources;
   
   private JPanel teamTwoOptions;
   private BoxLayout teamTwoLayout;
   
   private JPanel teamTwoNamePanel;
   private BoxLayout teamTwoNameLayout;
   private JLabel teamTwoNameLabel;
   private JTextField teamTwoName;
   
   private JPanel teamTwoTypePanel;
   private BoxLayout teamTwoTypeLayout;
   private JLabel teamTwoTypeLabel;
   private JComboBox<String> teamTwoType;
   
   private JPanel teamTwoResourcePanel;
   private BoxLayout teamTwoResourceLayout;
   private JLabel teamTwoResourcesLabel;
   private JTextField teamTwoResources;
   
   private JPanel mapOptions;
   private BoxLayout mapLayout;
   
   private JPanel mapTypePanel;
   private BoxLayout mapTypeLayout;
   private JLabel mapTypeLabel;
   private JComboBox<String> mapType;
   
   private JPanel mapNamePanel;
   private BoxLayout mapNameLayout;
   private JLabel mapNameLabel;
   private JComboBox<String> mapName;
   
   private JPanel buttons;
   private JButton okButton;
   
   private String[] teamTypes;
   
   public static void main(String args[]) {
      GameLauncher launcher = new GameLauncher();
      launcher.toString();
   }
   
   public GameLauncher() {
      this.setTitle("TBS2 Game Launcher");
      
      mainPanel = new JPanel();
      mainPanel.setBorder(new EmptyBorder(10,10,10,10));
      mainLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
      mainPanel.setLayout(mainLayout);
      
      teamTypes = new String[] { "Human", "Computer Team" };
      
      setupTeamOneOp();
      setupTeamTwoOp();
      setupMapOp();
      setupButtons();
      
      title = new JLabel("TBS2");
      title.setFont(new Font(title.getFont().getName(), Font.ITALIC, title.getFont().getSize() * 2));
      title.setAlignmentX(CENTER_ALIGNMENT);
      
      mainPanel.add(title);
      mainPanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mainPanel.add(teamOneOptions);
      mainPanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mainPanel.add(teamTwoOptions);
      mainPanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mainPanel.add(mapOptions);
      mainPanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mainPanel.add(buttons);
      
      this.add(mainPanel);
      this.pack();
      this.setResizable(false);
      this.setVisible(true);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
   
   private void setupTeamOneOp() {
      teamOneOptions = new JPanel();
      teamOneLayout = new BoxLayout(teamOneOptions, BoxLayout.Y_AXIS);
      teamOneOptions.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Team One Options"));
      teamOneOptions.setLayout(teamOneLayout);
      
      teamOneNamePanel = new JPanel();
      teamOneNameLayout = new BoxLayout(teamOneNamePanel, BoxLayout.X_AXIS);
      teamOneNamePanel.setLayout(teamOneNameLayout);
      teamOneNameLabel = new JLabel("Team Name: ");
      teamOneName = new JTextField("THIS IS A PLACEHOLDER");
      teamOneNamePanel.add(teamOneNameLabel);
      teamOneNamePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      teamOneNamePanel.add(teamOneName);
      teamOneOptions.add(teamOneNamePanel);
      teamOneOptions.add(Box.createRigidArea(new Dimension(15, 5)));
      
      teamOneTypePanel = new JPanel();
      teamOneTypeLayout = new BoxLayout(teamOneTypePanel, BoxLayout.X_AXIS);
      teamOneTypePanel.setLayout(teamOneTypeLayout);
      teamOneTypeLabel = new JLabel("Team Type: ");
      teamOneType = new JComboBox<String>(teamTypes);
      teamOneTypePanel.add(teamOneTypeLabel);
      teamOneTypePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      teamOneTypePanel.add(teamOneType);
      teamOneOptions.add(teamOneTypePanel);
      teamOneOptions.add(Box.createRigidArea(new Dimension(15, 5)));
      
      teamOneResourcePanel = new JPanel();
      teamOneResourceLayout = new BoxLayout(teamOneResourcePanel, BoxLayout.X_AXIS);
      teamOneResourcePanel.setLayout(teamOneResourceLayout);
      teamOneResourcesLabel = new JLabel("Starting Resources: ");
      teamOneResources = new JTextField("99999999");
      teamOneResourcePanel.add(teamOneResourcesLabel);
      teamOneResourcePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      teamOneResourcePanel.add(teamOneResources);
      teamOneOptions.add(teamOneResourcePanel);
   }
   
   private void setupTeamTwoOp() {
      teamTwoOptions = new JPanel();
      teamTwoLayout = new BoxLayout(teamTwoOptions, BoxLayout.Y_AXIS);
      teamTwoOptions.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Team Two Options"));
      teamTwoOptions.setLayout(teamTwoLayout);
      
      teamTwoNamePanel = new JPanel();
      teamTwoNameLayout = new BoxLayout(teamTwoNamePanel, BoxLayout.X_AXIS);
      teamTwoNamePanel.setLayout(teamTwoNameLayout);
      teamTwoNameLabel = new JLabel("Team Name: ");
      teamTwoName = new JTextField("THIS IS A PLACEHOLDER");
      teamTwoNamePanel.add(teamTwoNameLabel);
      teamTwoNamePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      teamTwoNamePanel.add(teamTwoName);
      teamTwoOptions.add(teamTwoNamePanel);
      teamTwoOptions.add(Box.createRigidArea(new Dimension(15, 5)));
      
      teamTwoTypePanel = new JPanel();
      teamTwoTypeLayout = new BoxLayout(teamTwoTypePanel, BoxLayout.X_AXIS);
      teamTwoTypePanel.setLayout(teamTwoTypeLayout);
      teamTwoTypeLabel = new JLabel("Team Type: ");
      teamTwoType = new JComboBox<String>(teamTypes);
      teamTwoTypePanel.add(teamTwoTypeLabel);
      teamTwoTypePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      teamTwoTypePanel.add(teamTwoType);
      teamTwoOptions.add(teamTwoTypePanel);
      teamTwoOptions.add(Box.createRigidArea(new Dimension(15, 5)));
      
      teamTwoResourcePanel = new JPanel();
      teamTwoResourceLayout = new BoxLayout(teamTwoResourcePanel, BoxLayout.X_AXIS);
      teamTwoResourcePanel.setLayout(teamTwoResourceLayout);
      teamTwoResourcesLabel = new JLabel("Starting Resources: ");
      teamTwoResources = new JTextField("99999999");
      teamTwoResourcePanel.add(teamTwoResourcesLabel);
      teamTwoResourcePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      teamTwoResourcePanel.add(teamTwoResources);
      teamTwoOptions.add(teamTwoResourcePanel);
   }
   
   private void setupMapOp() {
      mapOptions = new JPanel();
      mapLayout = new BoxLayout(mapOptions, BoxLayout.Y_AXIS);
      mapOptions.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), "Map Options"));
      mapOptions.setLayout(mapLayout);
      
      mapTypePanel = new JPanel();
      mapTypeLayout = new BoxLayout(mapTypePanel, BoxLayout.X_AXIS);
      mapTypePanel.setLayout(mapTypeLayout);
      mapTypeLabel = new JLabel("Map Type: ");
      mapType = new JComboBox<String>(new String[] { "Custom Map" });
      mapTypePanel.add(mapTypeLabel);
      mapTypePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mapTypePanel.add(mapType);
      mapOptions.add(mapTypePanel);
      mapOptions.add(Box.createRigidArea(new Dimension(15, 5)));

      mapNamePanel = new JPanel();
      mapNameLayout = new BoxLayout(mapNamePanel, BoxLayout.X_AXIS);
      mapNamePanel.setLayout(mapNameLayout);
      mapNameLabel = new JLabel("Map Name: ");
      mapName = new JComboBox<String>(new String[] { "Placeholder Map Name" });
      mapNamePanel.add(mapNameLabel);
      mapNamePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mapNamePanel.add(mapName);
      mapOptions.add(mapNamePanel);
   }
   
   private void setupButtons() {
      buttons = new JPanel();
      okButton = new JButton("Launch Game");
      buttons.add(okButton);
   }
}  