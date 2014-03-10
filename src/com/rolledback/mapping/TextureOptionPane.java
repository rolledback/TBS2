package com.rolledback.mapping;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.rolledback.framework.GraphicsManager;

public class TextureOptionPane extends JDialog {
   
   private static final long serialVersionUID = 1L;
   public JComboBox<String> textureList;
   public Image currentImage;
   public String[] tileNames;
   public String currTexture;
   
   public TextureOptionPane(GraphicsManager manger, String[] tileNames) {
      setTitle("Texture Picker");
      currTexture = "grass.png";
      this.tileNames = tileNames;
      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BorderLayout());
      createPicker(topPanel, tileNames);
      getContentPane().add(topPanel);
      setSize(200, 75);
      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
   }
   
   public void createPicker(JPanel panel, String[] names) {
      final String[] textureNames = names;
      textureList = new JComboBox<String>(textureNames);
      textureList.setSelectedIndex(0);
      textureList.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            currTexture = tileNames[textureList.getSelectedIndex()];
         }
      });
      panel.add(textureList);
   }
   
}
