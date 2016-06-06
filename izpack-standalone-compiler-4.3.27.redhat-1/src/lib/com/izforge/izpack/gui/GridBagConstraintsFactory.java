package com.izforge.izpack.gui;

import java.awt.*;

/**
 * Created by thauser on 5/7/15.
 */
public class GridBagConstraintsFactory {

    private static final int baseIndent = 20;
    private static final GridBagConstraints contentPanelConstraints;
    static {
        contentPanelConstraints = new GridBagConstraints();
        contentPanelConstraints.anchor = GridBagConstraints.SOUTH;
        contentPanelConstraints.weightx = 10;
        contentPanelConstraints.weighty = 10;
        contentPanelConstraints.gridx = 0;
        contentPanelConstraints.gridy = 1;
        contentPanelConstraints.gridwidth = 1;
        contentPanelConstraints.gridheight = 1;
        contentPanelConstraints.fill = GridBagConstraints.BOTH;
    }



    public static GridBagConstraints getContentPanelConstraints(){
        return contentPanelConstraints;
    }

    public static GridBagConstraints getTitleConstraints(int gridBagAnchor){
        GridBagConstraints titleConstraints = new GridBagConstraints();
        titleConstraints.insets= new Insets(10,20,10,20);
        titleConstraints.gridheight = 1;
        titleConstraints.gridwidth = 1;
        titleConstraints.weightx = 0;
        titleConstraints.weighty = 0;
        titleConstraints.fill = GridBagConstraints.NONE;
        titleConstraints.anchor = gridBagAnchor;
        titleConstraints.gridx = 0;
        titleConstraints.gridy = 0;
        return titleConstraints;
    }

    public static GridBagConstraints createNonFullLineElementConstraint(int row, int col, int alignment, int indent) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = alignment;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(2, baseIndent + indent, 2, 20);
        return gbc;
    }

    public static GridBagConstraints createFullLineElementConstraint(int row, int col, int indent) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(2, baseIndent + indent, 2, 40);
        return gbc;
    }


    public static GridBagConstraints createCheckAndRadioConstraint(int row, int col, int indent, double weightx){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = weightx;
        gbc.weighty = 0;
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(0, baseIndent + indent, 0, 20);
        return gbc;
    }

    public static GridBagConstraints createAlignedElementConstraint(int row, int col, int indent, int fill){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = fill;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(2, baseIndent+indent, 2, 20);
        return gbc;
    }

    public static GridBagConstraints createFixedSizeAlignedElementConstraint(int row, int col, int indent, double weightx){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weightx = weightx;
        gbc.weighty = 0;
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.insets = new Insets(2, baseIndent+indent, 2, 20);
        return gbc;
    }

    public static GridBagConstraints createDividerConstraints(int row, int indent){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 10000;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets = new Insets(2, baseIndent + indent, 2, 20);
        return gbc;
    }

}
