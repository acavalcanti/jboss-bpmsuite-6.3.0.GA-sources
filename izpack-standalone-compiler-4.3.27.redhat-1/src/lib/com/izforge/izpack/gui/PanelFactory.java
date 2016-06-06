package com.izforge.izpack.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by thauser on 5/7/15.
 */
public class PanelFactory {

    public static JPanel createContentPanel(boolean isOpaque){
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(isOpaque);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.weightx = 10;
        gbc.weighty = 10;
        gbc.gridx = 100;
        gbc.gridy = 100;
        gbc.gridwidth = 100;
        gbc.gridheight = 100;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(Box.createGlue(), gbc);
        contentPanel.setBorder(BorderFactory.createEmptyBorder());
        return contentPanel;
    }

    public static JPanel createMainPanel(boolean isOpaque){
        JPanel panel = new JPanel();
        panel.setOpaque(isOpaque);
        GridBagLayout gbl = new GridBagLayout();
        panel.setLayout(gbl);
        return panel;
    }
}
