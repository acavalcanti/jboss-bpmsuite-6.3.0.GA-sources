package com.redhat.installer.asconfiguration.jdbc.panel;

import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.GenericInformationJPanel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by thauser on 2/13/14.
 */
public class JBossExistingJDBCDriverSetupPanel extends JBossJDBCDriverSetupPanelFSW {

    public JBossExistingJDBCDriverSetupPanel(InstallerFrame parent, InstallData idata) {
        super(parent, idata, new IzPanelLayout());
    }

    public JBossExistingJDBCDriverSetupPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout){
        super(parent, idata, layout);

        mainPanel.remove(jdbcPanel);

        try {
            jdbcPanel = new ExistingJDBCPanel(this, idata, !parent.hasBackground);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class ExistingJDBCPanel extends JDBCPanelFSW {
        protected GenericInformationJPanel existingDriverName;

        public ExistingJDBCPanel(IzPanel parent, InstallData idata, boolean isOpaque) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
            super(parent, idata, isOpaque);

            remove(driverJarSelectionPanel);

            existingDriverName = new GenericInformationJPanel(JComboBox.class, idata.langpack.getString("JBossExistingJDBCDriverSetupPanel"),"");
            GridBagLayout layout = (GridBagLayout) getLayout();
            GridBagConstraints gbc_existingDriverName = layout.getConstraints(driverVendorName);

            remove(driverVendorName);

            add(existingDriverName, gbc_existingDriverName);

        }

        /**
         * Method used to parse the idata and get the detected JDBC driver names / vendors
         * To do this, classnames are matched to the vendor, and the user-defined name is added so that the
         * user can see the relation to their driver
         * @return
         */

        private String[] getExistingDriverNames(){
            return null;
        }
    }
}
