package com.redhat.installer.asconfiguration.jdbc.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.GenericInformationJPanel;
import com.izforge.izpack.util.GUIHelper;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

public class JBossJDBCDriverSetupPanelFSW extends JBossJDBCDriverSetupPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JBossJDBCDriverSetupPanelFSW(InstallerFrame parent, InstallData idata) {
		this(parent, idata, new IzPanelLayout());
	}

	public JBossJDBCDriverSetupPanelFSW(InstallerFrame parent, InstallData idata, LayoutManager2 layout) {
		super(parent, idata, layout);

		mainPanel.remove(jdbcPanel);
		
		try {
			jdbcPanel = new JDBCPanelFSW(this, idata, !parent.hasBackground);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mainPanel.add(jdbcPanel, GUIHelper.getContentPanelConstraints());
		JScrollPane scroller = GUIHelper.createPanelScroller(getBorder(), mainPanel, !parent.hasBackground);
		add(scroller, BorderLayout.CENTER);
	}

    @Override
    public void makeXMLData(IXMLElement panelRoot) {
        super.makeXMLData(panelRoot);
        new JBossJDBCDriverSetupPanelFSWAutomationHelper().makeXMLData(idata, panelRoot);
    }


    class JDBCPanelFSW extends JDBCPanel {
		protected GenericInformationJPanel dbUrl;
		protected GenericInformationJPanel dbUser;
		protected GenericInformationJPanel dbPassword;
		protected GenericInformationJPanel dbPasswordConfirm;
		protected JButton jdbcTest;

		public JDBCPanelFSW(IzPanel parent, InstallData idata, boolean isOpaque) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
				IllegalAccessException, InvocationTargetException {
			super(parent, idata, isOpaque);

			// remove the components we don't need.
			remove(xaClassname);
			remove(directoryStructure);
			remove(moduleName);
			remove(jdbcName);

			// create and add the new ones
			dbUrl = new GenericInformationJPanel(JTextField.class, idata.langpack.getString("db.url.text"), JBossJDBCConstants.connUrlMap.get(JBossJDBCConstants.ibmJdbcName));
			// dbUrl.getInfoComponent().setToolTipText(idata.langpack.getString());
			GridBagConstraints gbc_dbUrl = new GridBagConstraints();
			dbUrl.setColumns(40);
			gbc_dbUrl.fill = GridBagConstraints.BOTH;
			gbc_dbUrl.gridx = 0;
			gbc_dbUrl.gridy = 4;
			add(dbUrl, gbc_dbUrl);

			dbUser = new GenericInformationJPanel(JTextField.class, idata.langpack.getString("db.username.text"), "");
			// dbUrl.getInfoComponent().setToolTipText(idata.langpack.getString());
			GridBagConstraints gbc_dbUsername = new GridBagConstraints();
            dbUser.setColumns(40);
			gbc_dbUsername.fill = GridBagConstraints.BOTH;
			gbc_dbUsername.gridx = 0;
			gbc_dbUsername.gridy = 5;
			add(dbUser, gbc_dbUsername);

			dbPassword = new GenericInformationJPanel(JPasswordField.class, idata.langpack.getString("db.password.text"), "");
            dbPassword.setColumns(40);
			// dbUrl.getInfoComponent().setToolTipText(idata.langpack.getString());
			GridBagConstraints gbc_dbPassword = new GridBagConstraints();
			gbc_dbPassword.fill = GridBagConstraints.BOTH;
			gbc_dbPassword.gridx = 0;
			gbc_dbPassword.gridy = 6;
			add(dbPassword, gbc_dbPassword);

			dbPasswordConfirm = new GenericInformationJPanel(JPasswordField.class, idata.langpack.getString("db.password.re.text"), "");
            dbPasswordConfirm.setColumns(40);
			// dbUrl.getInfoComponent().setToolTipText(idata.langpack.getString());
			GridBagConstraints gbc_dbPasswordConfirm = new GridBagConstraints();
			gbc_dbPasswordConfirm.fill = GridBagConstraints.BOTH;
			gbc_dbPasswordConfirm.gridx = 0;
			gbc_dbPasswordConfirm.gridy = 7;
			add(dbPasswordConfirm, gbc_dbPasswordConfirm);
			
			jdbcTest = new JButton(idata.langpack.getString("db.test.connection"));
			jdbcTest.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					testJDBCConnection();					
				}
			});
			GridBagConstraints gbc_jdbcTest = new GridBagConstraints();
			gbc_jdbcTest.fill = GridBagConstraints.NONE;
			gbc_jdbcTest.anchor = GridBagConstraints.EAST;
			gbc_jdbcTest.gridx = 0;
			gbc_jdbcTest.gridy = 8;
			add(jdbcTest, gbc_jdbcTest);
			
			
		}

		@Override
		protected void updateInformation(String s) {
			super.updateInformation(s);
			if (s.equals(JBossJDBCConstants.ibmVendorName)) {
				dbUrl.setInfo(JBossJDBCConstants.connUrlMap.get(JBossJDBCConstants.ibmJdbcName));
			} else if (s.equals(JBossJDBCConstants.mysqlVendorName)) {
				dbUrl.setInfo(JBossJDBCConstants.connUrlMap.get(JBossJDBCConstants.mysqlJdbcName));
			} else if (s.equals(JBossJDBCConstants.postgresqlVendorName)) {
				dbUrl.setInfo(JBossJDBCConstants.connUrlMap.get(JBossJDBCConstants.postgresqlJdbcName));
			} else if (s.equals(JBossJDBCConstants.microsoftVendorName)) {
				dbUrl.setInfo(JBossJDBCConstants.connUrlMap.get(JBossJDBCConstants.microsoftJdbcName));
			} else if (s.equals(JBossJDBCConstants.oracleVendorName)) {
				dbUrl.setInfo(JBossJDBCConstants.connUrlMap.get(JBossJDBCConstants.oracleJdbcName));
			} else {
				dbUrl.setInfo(idata.langpack.getString("JBossJDBCDriverSetupPanel.nodefault"));
			}
		}

		@Override
		public int validated() {
			if (dbUser.getInfo().trim().isEmpty()){
				setError(idata.langpack.getString("username.nameerror"));
				return 2;
			}
			
			if (!dbPassword.getInfo().equals(dbPasswordConfirm.getInfo()) || dbPassword.getInfo().isEmpty()) {
				setError(idata.langpack.getString("username.no.match.password"));
				return 2;
			} 
			
			int superCall = super.validated();
			if (superCall != 0) {
				return superCall;
			}
			// set after all validation
			idata.setVariable("db.user", dbUser.getInfo().trim());
			idata.setVariable("db.password", dbPassword.getInfo());
			idata.setVariable("db.url", dbUrl.getInfo());
			return 0;
		}
		
		@Override
		public String getSummary(){
			StringBuilder summary = new StringBuilder();
			String superSummary = super.getSummary();
			if (superSummary == null){
				return superSummary;
			}
			summary.append(superSummary);
			summary.append(idata.langpack.getString("db.url")+": " + idata.getVariable("db.url") + "<br/>");
			summary.append(idata.langpack.getString("db.user")+": " + idata.getVariable("db.user") + "<br/>");
			summary.append(idata.langpack.getString("db.dialect")+": " + idata.getVariable("db.dialect")+"<br/>");
			return summary.toString();
		}

		public void testJDBCConnection() {
			if (super.verifyJars()) { // jars contain the driver, etc
				// do our own loading of the driver here
				if (!super.verifyDriver()){
					// set the driverclass to something usable
					// driver class not found
					emitError(idata.langpack.getString("installer.error"), String.format(idata.langpack.getString("JBossJDBCDriverSetupPanelFSW.path.nodriver"), JBossJDBCConstants.classnameMap.get(jdbcName.getInfo())));
					return;
				}
				
				if (!dbPassword.getInfo().equals(dbPasswordConfirm.getInfo())) {
					emitError(idata.langpack.getString("installer.error"), idata.langpack.getString("username.no.match.password"));
					return;
				}
				
				Driver jdbcDriver = null;
				
				try {
					jdbcDriver = (Driver) driverClass.newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}

				Object conn = JDBCConnectionUtils.getDatabaseConnection(jdbcDriver, dbUser.getInfo(), dbPassword.getInfo(), dbUrl.getInfo());

				if (conn != null) {
                    if (conn.getClass().equals(String.class)) {
                        emitError("Connection Failed", (String) conn);
                        return;
                    }
					try {
                        ((Connection) conn).close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					emitNotification(idata.langpack.getString("db.test.success"));
				} else {
					emitError("Connection Failed", idata.langpack.getString("db.test.failure"));
				}
			} else {
				emitError(idata.langpack.getString("installer.error"), getError());
			}
		}
	}
	
	

}
