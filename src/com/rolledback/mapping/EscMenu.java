package com.rolledback.mapping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class EscMenu extends JDialog implements ActionListener {
   
   private static final long serialVersionUID = 1L;
   private JButton openButton, saveButton;
   private JFileChooser fc;
   private String fileName;
   private boolean openFile;
   private boolean saveFile;
   
   public EscMenu() {
      setName("Menu");
      JPanel topPanel = new JPanel(new BorderLayout());
      
      // Create a file chooser
      fc = new JFileChooser();
      
      // Create the open button
      openButton = new JButton("Open a File...");
      openButton.addActionListener(this);
      
      // Create the save button
      saveButton = new JButton("Save a File...");
      saveButton.addActionListener(this);
      
      // Add the buttons to the top panel
      topPanel.add(openButton, BorderLayout.NORTH);
      topPanel.add(saveButton, BorderLayout.SOUTH);
      
      // Add the top panel to the frame
      add(topPanel, BorderLayout.PAGE_START);
      
      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      pack();
      
      // Get the size of the screen
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      
      // Determine the new location of the window
      int w = this.getSize().width;
      int h = this.getSize().height;
      int x = (dim.width - w) / 2;
      int y = (dim.height - h) / 2;
      
      // Move the window
      setLocation(x, y);
      setModal(true);
   }
   
   public void actionPerformed(ActionEvent e) {
      // Handle open button action.
      if(e.getSource() == openButton) {
         int returnVal = fc.showOpenDialog(EscMenu.this);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            openFile = true;
            saveFile = false;
            fileName = file.getPath();
            this.dispose();
         }
      }
      else if(e.getSource() == saveButton) {
         int returnVal = fc.showSaveDialog(EscMenu.this);
         if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            openFile = false;
            saveFile = true;
            fileName = file.getPath();
            this.dispose();
         }
      }
   }
   
   public String getFileName() {
      return fileName;
   }
   
   public void setFileName(String fileName) {
      this.fileName = fileName;
   }
   
   public boolean isOpenFile() {
      return openFile;
   }
   
   public void setOpenFile(boolean openFile) {
      this.openFile = openFile;
   }
   
   public boolean isSaveFile() {
      return saveFile;
   }
   
   public void setSaveFile(boolean saveFile) {
      this.saveFile = saveFile;
   }
}
