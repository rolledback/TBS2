package com.rolledback.mapping;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rolledback.framework.GraphicsManager;

public class TextureOptionPane extends JDialog {
   
   private static final long serialVersionUID = 1L;
   private JComboBox<String> textureList;
   private String[] tileNames;
   private String currTexture;
   private JLabel picLabel;
   private JPanel topPanel;
   private BorderLayout layout;
   
   public TextureOptionPane(String[] tileNames) {
      setTitle("Texture Picker");
      currTexture = "grass.png";
      this.tileNames = tileNames;
      topPanel = new JPanel();
      createPicker(tileNames);
      getContentPane().add(topPanel);
      pack();
      setResizable(false);
      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
   }
   
   public void createPicker(String[] names) {
      layout = new BorderLayout();
      layout.setVgap(1);
      topPanel.setLayout(layout);
      
      picLabel = new JLabel("", new ImageIcon(GraphicsManager.getTileTextures().get(currTexture)), JLabel.CENTER);
      picLabel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
      final String[] textureNames = names;
      textureList = new JComboBox<String>(textureNames);
      textureList.setSelectedIndex(0);
      textureList.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            currTexture = tileNames[textureList.getSelectedIndex()];
            picLabel.setIcon(new ImageIcon(GraphicsManager.getTileTextures().get(currTexture)));
         }
      });
      topPanel.add(textureList, BorderLayout.PAGE_START);
      topPanel.add(picLabel, BorderLayout.PAGE_END);
   }
   
   public String getCurrTexture() {
      return currTexture;
   }
   
   public void setCurrTexture(String currTexture) {
      this.currTexture = currTexture;
   }
   
}
