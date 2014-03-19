package com.rolledback.framework;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Launcher {

	static private Game newGame;
	static private GameGUI infoBox;
	static private int winFractionHeight = 10;
	static private int winFractionWidth = 4;

	public static void main(String args[]) {
		Logger.consolePrint("Getting map files.", "launcher");
		File directory = new File("maps");
		// if you are editing in eclipse, looks at files in the map folder in the
		// workspace
		String[] files = directory.list();
		// if you are running from a jar, looks at files in the map folder in same
		// dir as the jar
		if (files == null)
			files = new File(System.getProperty("user.dir") + "maps\\").list();

		ArrayList<Object> choices = new ArrayList<Object>();
		for (int f = 0; f < files.length; f++) {
			if (files[f].endsWith(".map")) {
				int[] size = (System.getProperty("os.name").equals("Linux")) ? getDimensions(directory + "/" + files[f]) : getDimensions(directory + "\\" + files[f]);
				if (size.length != 0)
					choices.add(files[f].substring(0, files[f].lastIndexOf(".")) + ": (" + size[0] + "x" + size[1] + ")");
			}
		}

		choices.add("128x128 Tiles Random");
		choices.add("64x64 Tiles Random");
		choices.add("32x32 Tiles Random");
		choices.add("16x16 Tiles Random");
		choices.add("8x8 Tiles Random");
		choices.add("4x4 Tiles Random");
		choices.add("2x2 Tiles Random");
		choices.add("Random Tiles Size");
		choices.add("Custom Grid Size");

		Object[] possibilities = choices.toArray();
		int tileSize = -1;
		int x = -1;
		int y = -1;
		int[] dimensions = new int[0];
		String fileToLoad = "";
		if (tileSize == -1) {
			Object s = JOptionPane.showInputDialog(new JPanel(), "Choose map:", "Launcher", JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);
			if (s != null) {
				String size = (String) s;
				Logger.consolePrint(size, "launcher");
				if (size.equals("Random Size")) {
					int[] sizes = { 8, 16, 32, 64, 128 };
					tileSize = sizes[new Random().nextInt(sizes.length)];
					dimensions = Launcher.autoCalcDimensions(tileSize);
				} else if (size.equals("Custom Grid Size")) {
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
						if (result == JOptionPane.OK_OPTION) {
							x = Integer.parseInt(xField.getText());
							y = Integer.parseInt(yField.getText());
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(new JFrame(), "Invalid answer. Now closing.", "Error", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
				} else if (size.contains(":")) {
					if (System.getProperty("os.name").equals("Linux"))
						fileToLoad = directory + "/" + size.substring(0, size.lastIndexOf(":")) + ".map";
					else
						fileToLoad = directory + "\\" + size.substring(0, size.lastIndexOf(":")) + ".map";
					dimensions = getDimensions(fileToLoad);
				} else {
					tileSize = Integer.parseInt(size.split("x")[0]);
					dimensions = Launcher.autoCalcDimensions(tileSize);
				}
			}
		}
		if (x == -1 && dimensions.length == 2) {
			init(dimensions[0], dimensions[1], fileToLoad);
		} else if (x != -1 && dimensions.length == 0) {
			init(x, y, fileToLoad);
		} else if (tileSize == -1 || dimensions.length == 0)
			System.exit(-1);
	}

	public static int[] getDimensions(String name) {
		BufferedInputStream mapReader;
		byte[] map = new byte[6];
		try {
			mapReader = new BufferedInputStream(new FileInputStream(name));
			mapReader.read(map);
			mapReader.close();
		} catch (IOException e) {
			return new int[0];
		}
		if (map[0] != 0x6d)
			return new int[0];
		int width = map[2] ^ (map[3] << 8);
		int height = map[4] ^ (map[5] << 8);
		int[] ret = new int[2];
		ret[0] = width;
		ret[1] = height;
		return ret;
	}

	public static int[] autoCalcDimensions(int size) {
		int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
		int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;

		JFrame test = new JFrame();
		GameGUI t = new GameGUI();
		test.add(t);
		test.pack();
		test.setVisible(true);

		int guiHeight = t.getHeight();
		test.dispose();
		screenHeight -= guiHeight;
		screenHeight -= (int) ((double) screenHeight / winFractionHeight);
		screenWidth -= (int) ((double) screenWidth / winFractionWidth);

		while (screenWidth % 64 != 0 || screenWidth % 32 != 0 || screenWidth % 128 != 0 || screenWidth % 16 != 0)
			screenWidth--;
		while (screenHeight % 64 != 0 || screenHeight % 32 != 0 || screenHeight % 128 != 0 || screenHeight % 16 != 0)
			screenHeight--;

		int[] d = { screenWidth / size, screenHeight / size };

		return d;
	}

	public static void init(int x, int y, String fileName) {

		// get the size of the screen
		int screenHeight = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
		int screenWidth = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;

		// reduce the dimensions 
		screenHeight -= (int) ((double) screenHeight / winFractionHeight);
		screenWidth -= (int) ((double) screenWidth / winFractionWidth);

		JFrame test = new JFrame();
		GameGUI t = new GameGUI();
		test.add(t);
		test.pack();
		test.setVisible(true);
		int guiHeight = t.getHeight();
		test.dispose();
		screenHeight -= guiHeight;

		// further reduce them until divisible by 128, 64, 32, and 16
		while (screenWidth % 64 != 0 || screenWidth % 32 != 0 || screenWidth % 128 != 0 || screenWidth % 16 != 0)
			screenWidth--;
		while (screenHeight % 64 != 0 || screenHeight % 32 != 0 || screenHeight % 128 != 0 || screenHeight % 16 != 0)
			screenHeight--;

		int tileSize = 128;
		int gameWidth = x;
		int gameHeight = y;

		while ((gameWidth * tileSize > screenWidth || gameHeight * tileSize > screenHeight) && tileSize >= 1) {
			tileSize /= 2;
		}

		int offsetHorizontal = screenWidth - (gameWidth * tileSize);
		int offsetVertical = screenHeight - (gameHeight * tileSize);
		
		JFrame frame;
		int winner[] = { 0, 0 };
		for (int i = 0; i < 10000; i++) {
			Logger.consolePrint("Constructing frame.", "launcher");
			frame = new JFrame("TBS2 " + Arrays.toString(winner));
			frame.getContentPane().setLayout(new BorderLayout());
			Logger.consolePrint("Game " + i, "launcher");
			long start = System.currentTimeMillis();

			Logger.consolePrint("Constructing game.", "launcher");
			infoBox = new GameGUI();
			newGame = new Game(x, y, tileSize, offsetHorizontal / 2, offsetVertical / 2, fileName, infoBox);
			newGame.setDoubleBuffered(true);
			newGame.setIgnoreRepaint(true);
			newGame.setSize(screenWidth, screenHeight);
			newGame.createBackground();
			frame.add(newGame, BorderLayout.CENTER);
			frame.add(infoBox, BorderLayout.SOUTH);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);
			frame.setVisible(true);
			if (System.getProperty("os.name").equals("Linux"))
				frame.setSize(screenWidth, screenHeight + guiHeight);
			else
				frame.setSize(screenWidth + frame.getInsets().right + frame.getInsets().left, screenHeight + frame.getInsets().top + frame.getInsets().bottom + guiHeight);
			infoBox.fixComponents(guiHeight);
			infoBox.updateInfo(null, null, newGame.teamOne, newGame.teamTwo);
			Logger.consolePrint("Running game.", "launcher");
			newGame.run();
			long end = System.currentTimeMillis();
			if (newGame.winner.equals(newGame.getWorld().getTeamOne()))
				winner[0]++;
			else
				winner[1]++;
			Logger.consolePrint(Arrays.toString(winner) + " " + (end - start) + " " + newGame.numTurns, "launcher");
			// try {
			// PrintStream out = new PrintStream(new FileOutputStream("dump.txt",
			// true));
			// for(Coordinate c: newGame.history)
			// out.println(c.getX() + " " + c.getY());
			// out.close();
			// }
			// catch(FileNotFoundException e) {
			// 
			// e.printStackTrace();
			// }
			// Logger.consolePrint("Click history saved.", "launcher");
			newGame = null;
			frame = null;
			Logger.consolePrint("Next game.", "launcher");
		}
		System.exit(-1);
	}

	public static void delay(int n) {
		long startDelay = System.currentTimeMillis();
		long endDelay = 0;
		while (endDelay - startDelay < n)
			endDelay = System.currentTimeMillis();
	}

}
