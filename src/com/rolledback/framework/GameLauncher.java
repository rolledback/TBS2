package com.rolledback.framework;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.rolledback.mapping.Cartographer;
import com.rolledback.teams.Team;
import com.rolledback.teams.ai.ComputerTeamE;

public class GameLauncher extends JDialog implements ItemListener, ActionListener {
   
   private static final long serialVersionUID = 1L;
   
   private static int winFractionHeight = 10;
   private static int winFractionWidth = 4;
   
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
   
   private final String CUSTOM = "Custom Map";
   private final String RANDOM = "Random Map";
   
   private JPanel mapNameCardHolder;
   
   private JPanel customMapNamePanel;
   private BoxLayout customMapNameLayout;
   private JLabel customMapNameLabel;
   private JComboBox<String> customMapName;
   
   private JPanel randomMapNamePanel;
   private BoxLayout randomMapNameLayout;
   private JLabel randomMapNameLabel;
   private JComboBox<String> randomMapName;
   
   public JPanel buttons;
   private JButton okButton;
   
   private String[] teamTypes;
   private String[] customMaps;
   private String[] randomMaps;
   
   private boolean choiceMade;
   private int[] dimensions;
   private int x;
   private int y;
   private int tileSize;
   private String fileToLoad;
   private Team teamOne;
   private Team teamTwo;
   
   public static void main(String args[]) {
      GameLauncher launcher = new GameLauncher();
      launcher.dispose();
      
      if(!launcher.isChoiceMade())
         System.exit(1);
      
      int x = launcher.getCustomX();
      int y = launcher.getCustomY();
      int tileSize = launcher.getTileSize();
      int[] dimensions = launcher.getDimensions();
      Team teamOne = launcher.getTeamOne();
      Team teamTwo = launcher.getTeamTwo();
      String fileToLoad = launcher.getFileToLoad();
      
      if(x == -1 && dimensions.length == 2) {
         initGame(teamOne, teamTwo, dimensions[0], dimensions[1], fileToLoad);
      }
      else if(x != -1 && dimensions.length == 0) {
         initGame(teamOne, teamTwo, x, y, fileToLoad);
      }
      else if(tileSize == -1 || dimensions.length == 0)
         System.exit(-1);
   }
   
   public GameLauncher() {
      this.setTitle("TBS2 Game Launcher");
      
      loadMapNames();
      setRandomNames();
      choiceMade = false;
      
      mainPanel = new JPanel();
      mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
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
      
      fieldsToDefault();
      
      // Get the size of the screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      
      // Determine the new location of the window
      int w = this.getSize().width;
      int h = this.getSize().height;
      int x = (dim.width - w) / 2;
      int y = (dim.height - h) / 2;
      
      // Move the window
      setLocation(x, y);
      
      
      this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      this.setModal(true);
      this.setVisible(true);
   }
   
   public void fieldsToDefault() {
      teamOneName.setText("Team One");
      teamTwoName.setText("Team Two");
      teamOneResources.setText("100");
      teamTwoResources.setText("100");
   }
   
   public void loadMapNames() {
      customMaps = new String[0];
      File directory = new File("maps");
      String[] files = directory.list();
      if(files == null)
         files = new File(System.getProperty("user.dir") + "maps\\").list();
      if(files == null)
         return;
      ArrayList<String> customMapNames = new ArrayList<String>();
      for(int f = 0; f < files.length; f++) {
         if(files[f].endsWith(".map")) {
            int[] size = (System.getProperty("os.name").equals("Linux")) ? Cartographer.getDimensions(directory + "/" + files[f]) : Cartographer.getDimensions(directory + "\\" + files[f]);
            if(size.length != 0)
               customMapNames.add(files[f].substring(0, files[f].lastIndexOf(".")) + ": (" + size[0] + "x" + size[1] + ")");
         }
      }
      customMaps = customMapNames.toArray(customMaps);
   }
   
   public void setRandomNames() {
      randomMaps = new String[] { "128x128 Tiles", "64x64 Tiles", "32x32 Tiles", "16x16 Tiles", "8x8 Tiles", "4x4 Tiles", "2x2 Tiles", "Random Size", "Custom Grid Size" };
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
      mapType = new JComboBox<String>(new String[] { CUSTOM, RANDOM });
      mapType.addItemListener(this);
      mapTypePanel.add(mapTypeLabel);
      mapTypePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      mapTypePanel.add(mapType);
      mapOptions.add(mapTypePanel);
      mapOptions.add(Box.createRigidArea(new Dimension(15, 5)));
      
      customMapNamePanel = new JPanel();
      customMapNameLayout = new BoxLayout(customMapNamePanel, BoxLayout.X_AXIS);
      customMapNamePanel.setLayout(customMapNameLayout);
      customMapNameLabel = new JLabel("Map Name: ");
      customMapName = new JComboBox<String>(customMaps);
      customMapNamePanel.add(customMapNameLabel);
      customMapNamePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      customMapNamePanel.add(customMapName);
      
      randomMapNamePanel = new JPanel();
      randomMapNameLayout = new BoxLayout(randomMapNamePanel, BoxLayout.X_AXIS);
      randomMapNamePanel.setLayout(randomMapNameLayout);
      randomMapNameLabel = new JLabel("Tile Size: ");
      randomMapName = new JComboBox<String>(randomMaps);
      randomMapNamePanel.add(randomMapNameLabel);
      randomMapNamePanel.add(Box.createRigidArea(new Dimension(15, 5)));
      randomMapNamePanel.add(randomMapName);
      
      mapNameCardHolder = new JPanel(new CardLayout());
      mapNameCardHolder.add(customMapNamePanel, CUSTOM);
      mapNameCardHolder.add(randomMapNamePanel, RANDOM);
      
      mapOptions.add(mapNameCardHolder);
   }
   
   private void setupButtons() {
      buttons = new JPanel();
      okButton = new JButton("Launch Game");
      okButton.addActionListener(this);
      buttons.add(okButton);
   }
   
   @Override
   public void itemStateChanged(ItemEvent arg0) {
      CardLayout cl = (CardLayout)(mapNameCardHolder.getLayout());
      cl.show(mapNameCardHolder, (String)arg0.getItem());
   }
   
   @Override
   public void actionPerformed(ActionEvent arg0) {
      if(teamOneType.getSelectedItem().equals("Human"))
         teamOne = new Team(teamOneName.getText(), 50, Integer.parseInt(teamOneResources.getText()), 1);
      else
         teamOne = new ComputerTeamE(teamOneName.getText(), 50, Integer.parseInt(teamOneResources.getText()), null, 1);
      
      if(teamTwoType.getSelectedItem().equals("Human"))
         teamTwo = new Team(teamTwoName.getText(), 50, Integer.parseInt(teamTwoResources.getText()), 2);
      else
         teamTwo = new ComputerTeamE(teamTwoName.getText(), 50, Integer.parseInt(teamTwoResources.getText()), null, 2);
      
      tileSize = -1;
      x = -1;
      y = -1;
      dimensions = new int[0];
      fileToLoad = "";
      
      if(mapType.getSelectedItem().equals("Random Map")) {
         String choice = (String)randomMapName.getSelectedItem();
         if(choice.equals("Random Size")) {
            Logger.consolePrint("Choosing a random tile size.", "launcher");
            int[] sizes = { 8, 16, 32, 64, 128 };
            tileSize = sizes[new Random().nextInt(sizes.length)];
            dimensions = Launcher.autoCalcDimensions(tileSize);
         }
         else if(choice.equals("Custom Grid Size")) {
            JTextField xField = new JTextField(5);
            JTextField yField = new JTextField(5);
            int result = JOptionPane.DEFAULT_OPTION;
            try {
               JPanel myPanel = new JPanel();
               myPanel.add(new JLabel("x:"));
               myPanel.add(xField);
               myPanel.add(Box.createHorizontalStrut(15));
               myPanel.add(new JLabel("y:"));
               myPanel.add(yField);
               result = JOptionPane.showConfirmDialog(null, myPanel, "Please Enter X and Y Values", JOptionPane.OK_CANCEL_OPTION);
               if(result == JOptionPane.OK_OPTION) {
                  x = Integer.parseInt(xField.getText());
                  y = Integer.parseInt(yField.getText());
                  Logger.consolePrint("User input of: " + x + "x" + y + ".", "launcher");
               }
            }
            catch(Exception e) {
               JOptionPane.showMessageDialog(new JFrame(), "Invalid answer. Now closing.", "Error", JOptionPane.ERROR_MESSAGE);
               System.exit(0);
            }
         }
         else {
            tileSize = Integer.parseInt(choice.split("x")[0]);
            dimensions = Launcher.autoCalcDimensions(tileSize);
         }
      }
      else {
         Logger.consolePrint("File chosen.", "launcher");
         String choice = (String)customMapName.getSelectedItem();
         File directory = new File("maps");
         if(System.getProperty("os.name").equals("Linux"))
            fileToLoad = directory + "/" + choice.substring(0, choice.lastIndexOf(":")) + ".map";
         else
            fileToLoad = directory + "\\" + choice.substring(0, choice.lastIndexOf(":")) + ".map";
         dimensions = Cartographer.getDimensions(fileToLoad);
      }
      choiceMade = true;
      this.setVisible(false);
   }
   
   public static void initGame(Team one, Team two, int x, int y, String fileName) {
      Logger.consolePrint("Init'ing with (" + x + ", " + y + ").", "launcher");
      
      // get the size of the screen
      int gamePanelHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
      int gamePanelWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
      Logger.consolePrint("Screen resolution: " + gamePanelWidth + "x" + gamePanelHeight, "launcher");
      
      // reduce the dimensions
      gamePanelHeight -= (int)((double)gamePanelHeight / winFractionHeight);
      gamePanelWidth -= (int)((double)gamePanelWidth / winFractionWidth);
      Logger.consolePrint("Initial reduction resulting in screen size of: " + gamePanelWidth + "x" + gamePanelHeight, "launcher");
      
      // create the game window
      Logger.consolePrint("Constructing game window.", "launcher");
      JFrame window = new JFrame("TBS2");
      window.setResizable(false);
      window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      window.getContentPane().setLayout(new BorderLayout());
      
      // create and add the GUI to the game window
      GameGUI infoBox = new GameGUI();
      Logger.setConsole(infoBox);
      window.add(infoBox, BorderLayout.SOUTH);
      window.pack();
      
      // remove how tall the GUI is from the gamePanel's height
      int guiHeight = infoBox.getHeight();
      gamePanelHeight -= guiHeight;
      infoBox.fixComponents();
      Logger.consolePrint("Intial gui dimensions: " + infoBox.getSize(), "launcher");
      
      // further reduce the dimensions until divisible by 128, 64, 32, and 16
      while(gamePanelWidth % 64 != 0 || gamePanelWidth % 32 != 0 || gamePanelWidth % 128 != 0 || gamePanelWidth % 16 != 0)
         gamePanelWidth--;
      while(gamePanelHeight % 64 != 0 || gamePanelHeight % 32 != 0 || gamePanelHeight % 128 != 0 || gamePanelHeight % 16 != 0)
         gamePanelHeight--;
      Logger.consolePrint("Final reduction resulting in panel size of: " + gamePanelWidth + "x" + gamePanelHeight, "launcher");
      
      // find out what tile size will first fit the given width and height (x, y)
      int tileSize = 128;
      int gameWidth = x;
      int gameHeight = y;
      
      while((gameWidth * tileSize > gamePanelWidth || gameHeight * tileSize > gamePanelHeight) && tileSize >= 1)
         tileSize /= 2;
      Logger.consolePrint("Tile size: " + tileSize, "launcher");
      
      // calculate the offset
      int offsetHorizontal = gamePanelWidth - (gameWidth * tileSize);
      int offsetVertical = gamePanelHeight - (gameHeight * tileSize);
      
      // initialize the game and add it to the window
      Game gamePanel = new Game(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2, fileName, infoBox, one, two);
      infoBox.setGame(gamePanel);
      gamePanel.setSize(gamePanelWidth, gamePanelHeight);
      gamePanel.createBackground();
      window.add(gamePanel, BorderLayout.CENTER);
      infoBox.updateInfo(null, gamePanel.getWorld().getTiles()[0][0], gamePanel.getTeamOne(), gamePanel.getTeamTwo());
      
      // set the game size and finish up creating the window
      gamePanel.setPreferredSize(new Dimension(gamePanelWidth, gamePanelHeight));
      gamePanel.setSize(gamePanel.getPreferredSize());
      Logger.consolePrint("Resizing using rules for OS: " + System.getProperty("os.name"), "launcher");
      if(System.getProperty("os.name").equals("Linux"))
         window.setSize(gamePanelWidth, gamePanelHeight + guiHeight);
      else {
         Logger.consolePrint("Inset left = " + window.getInsets().left, "launcher");
         Logger.consolePrint("Inset right = " + window.getInsets().right, "launcher");
         Logger.consolePrint("Inset top = " + window.getInsets().top, "launcher");
         Logger.consolePrint("Inset bottom = " + window.getInsets().bottom, "launcher");
         window.setSize(gamePanelWidth + window.getInsets().right + window.getInsets().left, gamePanelHeight + window.getInsets().top + window.getInsets().bottom + infoBox.getHeight());
      }
      
      // Get the size of the screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      
      // Determine the new location of the window
      int w = window.getSize().width;
      int h = window.getSize().height;
      int xCorner = (dim.width - w) / 2;
      int yCorner = (dim.height - h) / 2;
      
      // Move the window
      window.setLocation(xCorner, yCorner);
      
      window.setVisible(true);
      Logger.consolePrint("Final window dimensions: " + window.getSize(), "launcher");
      Logger.consolePrint("Final game panel dimensions: " + gamePanel.getSize(), "launcher");
      Logger.consolePrint("Final gui dimensions: " + infoBox.getSize(), "launcher");
      
      // setup complete, run the game
      Logger.consolePrint("Running game.", "launcher");
      gamePanel.run();
      JOptionPane.showMessageDialog(new JFrame(), "Winner: " + gamePanel.getWinner().getName() + "\n" + gamePanel.endGameStats(), "WIN", JOptionPane.INFORMATION_MESSAGE);
      System.exit(1);
   }
   
   public int[] getDimensions() {
      return dimensions;
   }
   
   public void setDimensions(int[] dimensions) {
      this.dimensions = dimensions;
   }
   
   public String getFileToLoad() {
      return fileToLoad;
   }
   
   public void setFileToLoad(String fileToLoad) {
      this.fileToLoad = fileToLoad;
   }
   
   public Team getTeamOne() {
      return teamOne;
   }
   
   public void setTeamOne(Team teamOne) {
      this.teamOne = teamOne;
   }
   
   public Team getTeamTwo() {
      return teamTwo;
   }
   
   public void setTeamTwo(Team teamTwo) {
      this.teamTwo = teamTwo;
   }
   
   public boolean isChoiceMade() {
      return choiceMade;
   }
   
   public void setChoiceMade(boolean choiceMade) {
      this.choiceMade = choiceMade;
   }
   
   public int getCustomX() {
      return x;
   }
   
   public void setCustomX(int x) {
      this.x = x;
   }
   
   public int getCustomY() {
      return y;
   }
   
   public void setCustomY(int y) {
      this.y = y;
   }
   
   public int getTileSize() {
      return tileSize;
   }
   
   public void setTileSize(int tileSize) {
      this.tileSize = tileSize;
   }
}