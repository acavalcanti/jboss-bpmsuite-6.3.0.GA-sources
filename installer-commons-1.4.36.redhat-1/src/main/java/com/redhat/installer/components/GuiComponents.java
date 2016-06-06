package com.redhat.installer.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.GUIHelper;

public class GuiComponents {
    
    /** Creates 'Main' JPanel for your custom panel
     * 
     * This will provide you with the 'Main' JPanel for your custom panel.
     * Will have the normal GUI constraints, and have a title.
     * 
     * Note: In place of false you may want frame.parent.hasBackground,
     *       Unfortunately this is a protected variable.
     *       Can be changed in the future if need be.
     *       
     * @param headline The headline id defined in your localization files
     * @param frame    The panel that the skeleton is for
     * @param idata    Installation data
     * @return
     */
    public static JPanel createSkeleton(String headline, IzPanel frame, InstallData idata) {
        frame.setBorder(BorderFactory.createEmptyBorder());
        frame.setLayout(new BorderLayout());
        JPanel mainPanel = GUIHelper.createMainPanel(false);
        JLabel title = LabelFactory.createTitleLabel(idata.langpack.getString(headline),false);
        mainPanel.add(title, GUIHelper.getTitleConstraints());
        return mainPanel;
    }
}
