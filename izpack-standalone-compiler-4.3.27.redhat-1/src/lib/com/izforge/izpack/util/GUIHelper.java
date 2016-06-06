package com.izforge.izpack.util;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class GUIHelper {
	// class to add special things like borders and titles to custom panels
    // TODO: maybe factor this out to installer proper
	public GUIHelper(){}

    public static Border createIzPackBorder()
    { // creates a border to mimic other izpack panels
        Border border;
        Border matte = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray);
        Border matte2 = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.white);
        border = BorderFactory.createCompoundBorder(matte2, matte);
        return border;
    }
	
	private static GridBagConstraints titleConstraints;
	private static GridBagConstraints contentPanelConstraints;
    private static GridBagConstraints infoLabelConstraints;

	
	/**
	 * Creates a panel that is useful for custom panels. All content for gathering user input should
	 * be placed within this panel
	 * @param isOpaque
	 * @return
	 */
	
	public static JPanel createMainPanel(boolean isOpaque){
	    JPanel panel = new JPanel();
	    panel.setOpaque(isOpaque);
	    GridBagLayout gbl = new GridBagLayout();
	    gbl.columnWeights = new double[]{1.0};
	    gbl.rowWeights = new double[] {0.0, 0.0, 1.0};
	    panel.setLayout(gbl);
	    return panel;
	}
	
	/**
	 * Returns a JScrollPane with the following characteristics:<br/>
	 * 1. ViewportBorder and VerticalScrollBar borders set to border.<br/>
	 * 2. Given panel set to the JScrollPane's view<br/>
	 * 3. VerticalScrollBar policy set to JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED<br/>
	 * 4. HorizontalScrollbar policy set to JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	 * @param border the bounds for the viewport and the scroll bars
	 * @param panel the view for the JScrollPane
	 * @param isOpaque indicates whether the JScrollPane should be opaque or not.
	 * @return
	 */
    public static JScrollPane createPanelScroller(Border border, JPanel panel, boolean isOpaque)
    {
        JScrollPane scroller = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setViewportBorder(border);
        scroller.getVerticalScrollBar().setBorder(border);
        scroller.getViewport().setOpaque(isOpaque);
        scroller.setOpaque(isOpaque);
        return scroller;
    }
    
    
    /**
     * Method that returns a GridBagConstraints object suitable for a standard IzPack title on custom panels
     * If the titleConstraints haven't been instantiated, the first call will instantiate them
     */
    public static GridBagConstraints getTitleConstraints(){
       
        if (titleConstraints == null){
            titleConstraints = new GridBagConstraints();
            titleConstraints.insets= new Insets(0,0,0,80);
            titleConstraints.fill = GridBagConstraints.HORIZONTAL;
            titleConstraints.anchor = GridBagConstraints.NORTHEAST;
            titleConstraints.gridx = 0;
            titleConstraints.gridy = 0;
        }
        
        return titleConstraints;        
    }
    
    
    public static GridBagConstraints getInfoConstraints()
    {
        if (infoLabelConstraints == null)
        {
            infoLabelConstraints = new GridBagConstraints();
            infoLabelConstraints.anchor = GridBagConstraints.NORTHWEST;
            infoLabelConstraints.insets = new Insets(0,80,0,80);
            infoLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
            infoLabelConstraints.gridx = 0;
            infoLabelConstraints.gridy = 1;
        }
        return infoLabelConstraints; 
    }
    
    /**
     * Method that returns a GridBagConstraints object suitable for a standard IzPack content panel on custom panels.
     * If the contentPanelConstraints haven't been instantiated, the first call will instantiate them
     * @return
     */
    public static GridBagConstraints getContentPanelConstraints(){
        
        if (contentPanelConstraints == null){
            contentPanelConstraints = new GridBagConstraints();
            contentPanelConstraints.insets = new Insets(0,80,0,80);
            contentPanelConstraints.fill = GridBagConstraints.BOTH;
            contentPanelConstraints.gridx = 0;
            contentPanelConstraints.gridy = 2;
        }
        return contentPanelConstraints;
    }

    
    /**
     * Returns a panel containing two buttons that are aligned to the right of the panel. Currently used by<br/> 
     * JBossJDBCDriverSetupPanel <br/>
     * JBossDatasourceConfigPanel <br/>
     * to display the add additional drivers / datasources buttons
     */
    
    public static JPanel getRightAlignedPanel(boolean isOpaque){
        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{0, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 0};
        gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gridBagLayout);
        panel.setOpaque(isOpaque);
        return panel;
    }


}
