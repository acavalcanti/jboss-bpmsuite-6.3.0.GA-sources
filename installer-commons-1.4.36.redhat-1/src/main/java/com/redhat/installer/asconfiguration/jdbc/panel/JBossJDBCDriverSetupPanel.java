package com.redhat.installer.asconfiguration.jdbc.panel;


import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.GenericInformationJPanel;
import com.izforge.izpack.panels.PathSelectionPanel;
import com.izforge.izpack.util.GUIHelper;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;
import com.redhat.installer.gui.dynamic.DynamicComponentsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class JBossJDBCDriverSetupPanel  extends IzPanel {

	/* all strings that we care about */

	private static final long serialVersionUID = 1L;

	protected JPanel mainPanel;
	protected JDBCPanel jdbcPanel;
    protected DynamicComponentsPanel driverJarSelectionPanel;
    private LinkedHashMap<String, HashMap<String, ArrayList<String>>> driverNamesMap;
    private boolean mustClearDrivers = false;
    private boolean firstActivation = true;

	public JBossJDBCDriverSetupPanel(InstallerFrame parent, InstallData idata) {
		this(parent, idata, new IzPanelLayout());
	}

	public JBossJDBCDriverSetupPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout) {
		super(parent, idata, layout);
		// izpack has done something to make an "Empty" border actually appear
		// as you see in UserInputPanel.
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());

		mainPanel = GUIHelper.createMainPanel(!parent.hasBackground);
		
		JLabel title;
		if(idata.getVariable("isBxMS").equals("true")){
			title = new JLabel(idata.langpack.getString("JBossJDBCDriverSetupPanel.headline"),
					SwingConstants.CENTER);
			title.setOpaque(!parent.hasBackground);
			Font font = title.getFont();
			font = font.deriveFont(Font.BOLD, font.getSize()*2.0f);
			title.setFont(font);
		}
		else {
			title = LabelFactory.createTitleLabel(idata.langpack.getString("JBossJDBCDriverSetupPanel.headline"), !parent.hasBackground);
		}
		mainPanel.add(title, GUIHelper.getTitleConstraints());


		try {
			jdbcPanel = new JDBCPanel(this, idata, !parent.hasBackground);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mainPanel.add(jdbcPanel, GUIHelper.getContentPanelConstraints());

		JScrollPane scroller = GUIHelper.createPanelScroller(getBorder(), mainPanel, !parent.hasBackground);
		add(scroller, BorderLayout.CENTER);
	}

    @Override
	public boolean isValidated() {
		switch (jdbcPanel.validated()) {
		case 0:
			return true;
		case 1:
			return false;
		case 2:
			emitError(idata.langpack.getString("installer.error"), jdbcPanel.getError());
			return false;
		default:
			return false;
		} 
	}

	public String getSummaryBody() {
		return jdbcPanel.getSummary();
	}

	public void makeXMLData(IXMLElement panelRoot) {
		new JBossJDBCDriverSetupPanelAutomationHelper().makeXMLData(idata, panelRoot);
	}

	class JDBCPanel extends JPanel implements ActionListener {

		/**
	     * 
	     */
		private static final long serialVersionUID = 1170682341005766493L;

		private InstallData idata;

		private String error = "";

		protected GenericInformationJPanel xaClassname;

		protected GenericInformationJPanel directoryStructure;

		protected GenericInformationJPanel moduleName;

		protected GenericInformationJPanel jdbcName;

		protected GenericInformationJPanel driverVendorName;


		protected Class driverClass; // the class object we are able to load from the given jars

		public JDBCPanel(){
			super();
		}
		/**
		 * Create the panel.
		 * 
		 * @throws InvocationTargetException
		 * @throws IllegalAccessException
		 * @throws InstantiationException
		 * @throws IllegalArgumentException
		 * @throws NoSuchMethodException
		 * @throws SecurityException
		 */
		public JDBCPanel(IzPanel parent, InstallData idata, boolean isOpaque) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
				IllegalAccessException, InvocationTargetException {
			this.idata = idata;
			setOpaque(isOpaque);

			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 0 };
			gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			gridBagLayout.columnWeights = new double[] { 1.0 };
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
			setLayout(gridBagLayout);

			JTextArea infoLbl = LabelFactory.createMultilineLabel(idata.langpack.getString("JBossJDBCDriverSetupPanel.info"), isOpaque);
			GridBagConstraints gbc_infoLbl = new GridBagConstraints();
			gbc_infoLbl.anchor = GridBagConstraints.NORTHWEST;
			gbc_infoLbl.fill = GridBagConstraints.BOTH;
			gbc_infoLbl.gridx = 0;
			gbc_infoLbl.gridy = 0;
			add(infoLbl, gbc_infoLbl);

			/** 
			 * label with html link inside of it.
			 */
			
			JPanel linkLblPanel = new JPanel(new GridBagLayout());
			linkLblPanel.setOpaque(isOpaque);
			String linkLblTxt = idata.langpack.getString("JBossJDBCDriverSetupPanel.linklabel");
			String linkButtonTxt = idata.langpack.getString("JBossJDBCDriverSetupPanel.clickhere");
			JLabel linkLbl = LabelFactory.create(linkLblTxt);
			linkLbl.setOpaque(isOpaque);
			JButton linkButton = new JButton(linkButtonTxt);
			linkButton.setForeground(Color.BLUE);
			linkButton.setFocusPainted(false);
	        linkButton.setMargin(new Insets(0, 0, 0, 0));
	        linkButton.setContentAreaFilled(false);
	        linkButton.setBorderPainted(false);
	        linkButton.setOpaque(isOpaque);
			
			linkButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// here we call the pop-up to display the information
					displayCustomDSWarning();
				}
			});
			
			GridBagConstraints gbc_linkLblTxt = new GridBagConstraints();
			gbc_linkLblTxt.anchor = GridBagConstraints.WEST;
			// default fill = NONE
			gbc_linkLblTxt.gridx = 0;
			gbc_linkLblTxt.gridy = 0;
			
			
			GridBagConstraints gbc_linkButtonTxt = new GridBagConstraints();
			gbc_linkButtonTxt.anchor = GridBagConstraints.WEST;
			gbc_linkButtonTxt.gridx = 1;
			gbc_linkButtonTxt.gridy = 0;
			
			
			linkLblPanel.add(linkLbl, gbc_linkLblTxt);
			linkLblPanel.add(linkButton, gbc_linkButtonTxt);
			
			GridBagConstraints gbc_linkLblPanel = new GridBagConstraints();
			gbc_linkLblPanel.anchor = GridBagConstraints.WEST;
			gbc_linkLblPanel.fill = GridBagConstraints.NONE;
			gbc_linkLblPanel.gridx = 0;
			gbc_linkLblPanel.gridy = 1;
			add(linkLblPanel, gbc_linkLblPanel);		
			String[] driverOptions;
			String product = idata.getVariable("product.name");
			boolean isEap = product != null ? product.equals("eap") : false;
			
			if (isEap){
				driverOptions =	createArray(JBossJDBCConstants.ibmVendorName, JBossJDBCConstants.microsoftVendorName, JBossJDBCConstants.mysqlVendorName, JBossJDBCConstants.oracleVendorName,  JBossJDBCConstants.postgresqlVendorName, JBossJDBCConstants.sybaseVendorName);
			} else {
				driverOptions = createArray(JBossJDBCConstants.ibmVendorName, JBossJDBCConstants.microsoftVendorName, JBossJDBCConstants.mysqlVendorName, JBossJDBCConstants.oracleVendorName,  JBossJDBCConstants.postgresqlVendorName);
			}
			
				
			driverVendorName = new GenericInformationJPanel(JComboBox.class, idata.langpack.getString("JBossJDBCDriverSetupPanel.dropdown.label"), driverOptions);
			driverVendorName.addActionListener(this);
			driverVendorName.getInfoComponent().setToolTipText(idata.langpack.getString("JBossJDBCDriverSetupPanel.dropdown.tooltip"));
			GridBagConstraints gbc_driverVendorName = new GridBagConstraints();
			gbc_driverVendorName.fill = GridBagConstraints.BOTH;
			gbc_driverVendorName.gridx = 0;
			gbc_driverVendorName.gridy = 2;
			add(driverVendorName, gbc_driverVendorName);

			/**
			 * Consider putting this stuff in a new method
			 */
			JPanel jarContainer = new JPanel();
			jarContainer.setOpaque(isOpaque);
			GridBagLayout gbl_jarContainer = new GridBagLayout();
			gbl_jarContainer.columnWidths = new int[] { 190 };
			gbl_jarContainer.columnWeights = new double[] { 0.0, 1.0 };
			gbl_jarContainer.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			jarContainer.setLayout(gbl_jarContainer);

			/** Driver JAR locations label */
			JLabel jarsLbl = new JLabel(idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.label"));
			jarsLbl.setOpaque(isOpaque);
			GridBagConstraints gbc_jarsLbl = new GridBagConstraints();
			gbc_jarsLbl.anchor = GridBagConstraints.NORTHWEST;
			gbc_jarsLbl.gridx = 0;
			gbc_jarsLbl.gridy = 0;
			gbc_jarsLbl.ipady = 38;
			jarContainer.add(jarsLbl, gbc_jarsLbl);

			/** Might want to change default just to have a blank header, right now default has no header at all */
			JLabel jarsHeaderLblLeft = new JLabel(" ");
			jarsHeaderLblLeft.setOpaque(isOpaque);

			JLabel jarsHeaderLblRight = new JLabel(" ");
			jarsHeaderLblRight.setOpaque(isOpaque);

			JPanel jarsHeader = new JPanel(new GridLayout(1, 2));
			jarsHeader.add(jarsHeaderLblLeft);
			jarsHeader.add(jarsHeaderLblRight);
			jarsHeader.setOpaque(isOpaque);

			driverJarSelectionPanel = new DynamicComponentsPanel(PathSelectionPanel.class, 20, 1, jarsHeader,idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.tooltip"));
			driverJarSelectionPanel.setIdata(idata);
			driverJarSelectionPanel.setIzpanel(parent);
			//driverJarSelectionPanel.addInitial();
			driverJarSelectionPanel.setAddButtonText(idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.addone"));
			driverJarSelectionPanel.setRemoveButtonText(idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.removeone"));
			GridBagConstraints gbc_driverJarSelectionPanel = new GridBagConstraints();
			gbc_driverJarSelectionPanel.anchor = GridBagConstraints.EAST;
			gbc_driverJarSelectionPanel.fill = GridBagConstraints.HORIZONTAL;
			gbc_driverJarSelectionPanel.gridx = 1;
			gbc_driverJarSelectionPanel.gridy = 0;
			jarContainer.add(driverJarSelectionPanel, gbc_driverJarSelectionPanel);

			GridBagConstraints gbc_jarContainer = new GridBagConstraints();
			gbc_jarContainer.fill = GridBagConstraints.BOTH;
			gbc_jarContainer.gridx = 0;
			gbc_jarContainer.gridy = 3;
			add(jarContainer, gbc_jarContainer);

			/**
			 * End stuff to put in new method
			 */

			jdbcName = new GenericInformationJPanel(JTextField.class, idata.langpack.getString("JBossJDBCDriverSetupPanel.jdbcname.label"), JBossJDBCConstants.ibmJdbcName);
			((JTextField) jdbcName.getInfoComponent()).setEditable(false);
			jdbcName.getInfoComponent().setToolTipText(idata.langpack.getString("JBossJDBCDriverSetupPanel.jdbcname.tooltip"));
			GridBagConstraints gbc_jdbcName = new GridBagConstraints();
			gbc_jdbcName.fill = GridBagConstraints.BOTH;
			gbc_jdbcName.gridx = 0;
			gbc_jdbcName.gridy = 4;
			add(jdbcName, gbc_jdbcName);

			moduleName = new GenericInformationJPanel(JTextField.class, idata.langpack.getString("JBossJDBCDriverSetupPanel.modulename.label"), JBossJDBCConstants.ibmModuleName);
			((JTextField) moduleName.getInfoComponent()).setEditable(false);
			moduleName.getInfoComponent().setToolTipText(idata.langpack.getString("JBossJDBCDriverSetupPanel.modulename.tooltip"));
			GridBagConstraints gbc_moduleName = new GridBagConstraints();
			gbc_moduleName.fill = GridBagConstraints.BOTH;
			gbc_moduleName.gridx = 0;
			gbc_moduleName.gridy = 5;
			add(moduleName, gbc_moduleName);

			xaClassname = new GenericInformationJPanel(JTextField.class, idata.langpack.getString("JBossJDBCDriverSetupPanel.xaclassname.label"), JBossJDBCConstants.ibmXaDsName);
			((JTextField) xaClassname.getInfoComponent()).setEditable(false);
			xaClassname.getInfoComponent().setToolTipText(idata.langpack.getString("JBossJDBCDriverSetupPanel.xaclassname.tooltip"));
			GridBagConstraints gbc_xaClassname = new GridBagConstraints();
			gbc_xaClassname.fill = GridBagConstraints.BOTH;
			gbc_xaClassname.gridx = 0;
			gbc_xaClassname.gridy = 6;
			add(xaClassname, gbc_xaClassname);

			
			directoryStructure = new GenericInformationJPanel(JTextField.class, idata.langpack.getString("JBossJDBCDriverSetupPanel.dirstruct.label"), JBossJDBCConstants.ibmDirStruct);
			((JTextField) directoryStructure.getInfoComponent()).setEditable(false);
			directoryStructure.getInfoComponent().setToolTipText(idata.langpack.getString("JBossJDBCDriverSetupPanel.dirstruct.tooltip"));
			GridBagConstraints gbc_directoryStructure = new GridBagConstraints();
			gbc_directoryStructure.fill = GridBagConstraints.BOTH;
			gbc_directoryStructure.gridx = 0;
			gbc_directoryStructure.gridy = 7;
			add(directoryStructure, gbc_directoryStructure);

		}

		/**
		 * Annoyingly, java makes these shenanigans necessary
		 *
		 */
		private String[] createArray(String ... array){
			return array;
		}

		/**
		 * Updates all of the TextFields with proper information based on the driver selected
		 * 
		 * @param s
		 */
		protected void updateInformation(String s) {
            boolean useInstalledJDBC;
            ArrayList<String> driverPathsList = new ArrayList<String>();
            String existingJDBCWarning = idata.langpack.getString("JBossJDBCDriverSetupPanel.driver.existing.warning");
            String existingJDBCWarningText = idata.langpack.getString("JBossJDBCDriverSetupPanel.driver.existing.warning.text");

			if (s.equals(JBossJDBCConstants.ibmVendorName)) {
				jdbcName.setInfo(JBossJDBCConstants.ibmJdbcName);
				moduleName.setInfo(JBossJDBCConstants.ibmModuleName);
				xaClassname.setInfo(JBossJDBCConstants.ibmXaDsName);
				directoryStructure.setInfo(JBossJDBCConstants.ibmDirStruct);
			}
			else if (s.equals(JBossJDBCConstants.sybaseVendorName)) {
				jdbcName.setInfo(JBossJDBCConstants.sybaseJdbcName);
				moduleName.setInfo(JBossJDBCConstants.sybaseModuleName);
				xaClassname.setInfo(JBossJDBCConstants.sybaseXaDsName);
				directoryStructure.setInfo(JBossJDBCConstants.sybaseDirStruct);
			} else if (s.equals(JBossJDBCConstants.mysqlVendorName)) {
				jdbcName.setInfo(JBossJDBCConstants.mysqlJdbcName);
				moduleName.setInfo(JBossJDBCConstants.mysqlModuleName);
				xaClassname.setInfo(JBossJDBCConstants.mysqlXaDsName);
				directoryStructure.setInfo(JBossJDBCConstants.mysqlDirStruct);
			} else if (s.equals(JBossJDBCConstants.postgresqlVendorName)) {
				jdbcName.setInfo(JBossJDBCConstants.postgresqlJdbcName);
				moduleName.setInfo(JBossJDBCConstants.postgresqlModuleName);
				xaClassname.setInfo(JBossJDBCConstants.postgresqlXaDsName);
				directoryStructure.setInfo(JBossJDBCConstants.postgresqlDirStruct);
			} else if (s.equals(JBossJDBCConstants.microsoftVendorName)) {
				jdbcName.setInfo(JBossJDBCConstants.microsoftJdbcName);
				moduleName.setInfo(JBossJDBCConstants.microsoftModuleName);
				xaClassname.setInfo(JBossJDBCConstants.microsoftXaDsName);
				directoryStructure.setInfo(JBossJDBCConstants.microsoftDirStruct);
			} else if (s.equals(JBossJDBCConstants.oracleVendorName)) {
				jdbcName.setInfo(JBossJDBCConstants.oracleJdbcName);
				moduleName.setInfo(JBossJDBCConstants.oracleModuleName);
				xaClassname.setInfo(JBossJDBCConstants.oracleXaDsName);
				directoryStructure.setInfo(JBossJDBCConstants.oracleDirStruct);
			} else {
				jdbcName.setInfo(idata.langpack.getString("JBossJDBCDriverSetupPanel.nodefault"));
				moduleName.setInfo(idata.langpack.getString("JBossJDBCDriverSetupPanel.nodefault"));
				xaClassname.setInfo(idata.langpack.getString("JBossJDBCDriverSetupPanel.nodefault"));
				directoryStructure.setInfo(idata.langpack.getString("JBossJDBCDriverSetupPanel.nodefault"));
			}

            if (driverNamesMap.containsKey(jdbcName.getInfo())) {

                useInstalledJDBC = emitWarning(existingJDBCWarning, existingJDBCWarningText);

                if(useInstalledJDBC) {
                    HashMap<String, ArrayList<String>> driverLocationMap = driverNamesMap.get(jdbcName.getInfo());

                //Select driver you are going to use to talk to database
                    for(Entry<String, ArrayList<String>> pair: driverLocationMap.entrySet()){
                        driverPathsList.addAll(pair.getValue());
                    }
                    String jarLocation = driverPathsList.get(0);
                idata.setVariable("jdbc.driver.path", jarLocation);
                    idata.setVariable("jdbc.driver.use.preexising", "true");

                mustClearDrivers = true;
                driverJarSelectionPanel.clearDynamicComponents();
                    driverJarSelectionPanel.addInitial(driverPathsList, false);
                driverJarSelectionPanel.setEnabled(false);
                driverJarSelectionPanel.hideButtons();
                revalidate();
                repaint();

                }
                else {
                    driverJarSelectionPanel.clearDynamicComponents();
                    driverJarSelectionPanel.addInitial();
                    driverJarSelectionPanel.setEnabled(true);
                    driverJarSelectionPanel.showButtons();
                    mustClearDrivers = false;
                    revalidate();
                    repaint();
                }
            }
            else if (mustClearDrivers){

                idata.setVariable("jdbc.driver.path", "");
                driverJarSelectionPanel.clearDynamicComponents();
                driverJarSelectionPanel.addInitial();
                driverJarSelectionPanel.setEnabled(true);
                driverJarSelectionPanel.showButtons();
                mustClearDrivers = false;
                revalidate();
                repaint();

            }
		}

		public String getSummary() {
            String jdbcDriverInstall = idata.getVariable("jdbc.driver.install");
			if (jdbcDriverInstall == null || jdbcDriverInstall.equals("false")) {
				return null;
			} else {
				return idata.langpack.getString("JBossJDBCDriverSetupPanel.dropdown.label") + " " + idata.getVariable("jdbc.driver.name") + "<br>" + listJarPaths();
			}
		}

		/**
		 * returns an HTML formatted string containing the given paths of the jars, prepended by an appropriate number
		 * 
		 * @return
		 */
		private String listJarPaths() {
			StringBuffer sb = new StringBuffer();
			sb.append(idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.label") + "<br/>");
			int count = 1;
			for (Entry<String, String> entry : driverJarSelectionPanel.serialize("").entrySet()) {
				sb.append(count + ". " + entry.getValue() + "<br/>");
				count++;
			}
			return sb.toString();
		}

		/**
		 * Sets the error string internally.
		 * 
		 * @param s
		 */
		protected void setError(String s) {
			error = s;
		}

		/**
		 * Returns the saved error, if we have one. Can be empty if there was never an error.
		 * 
		 * @return
		 */
		public String getError() {
			return error;
		}
		/**
		 * Returns an int based upon the result of the validation<br/>
		 * 
		 * 0: validation is successful.<br/>
		 * 1: validation is successful, but a warning was displayed to the user.<br/>
		 * 2: validation is unsuccessful. display the error<br/>
		 * @return
		 */

		public int validated() {
            if (new HashSet<String>(getJarPaths()).size() < getJarPaths().size()) {
                setError(idata.langpack.getString("JBossJDBCDriverSetupPanel.duplicate.path"));
                return 2;
            }
			if (!verifyJars()){
				return 2;
			}
			
			if (!verifyDriver()){
				boolean response = emitWarning(
                        idata.langpack.getString("installer.warning"),
                        String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.nodriver"),
                        getDriverCheckName()));
				if (!response){
					return 1;
				}
			}
			// all checks valid
			Map<String, String> jars = driverJarSelectionPanel.serialize("jdbc.driver.jar");
			for (Entry<String, String> entry : jars.entrySet()) {
				idata.setVariable(entry.getKey(), entry.getValue()); // The path
																		// vars
																		// saved
																		// to
																		// izpack
																		// here.
			}

            // database information TODO: would like to get rid of jdbc.driver.name entirely if possible.
            String jdbcPreExisting = idata.getVariable("jdbc.driver.preexisting");
            String usePreExisting = idata.getVariable("jdbc.driver.use.preexising");
            if (jdbcPreExisting != null && jdbcPreExisting.equals("true") && usePreExisting.equals("false")) {
                String newDriverName = jdbcName.getInfo();
                int i = 1;
                while (driverNamesMap.get(jdbcName.getInfo()).containsKey(newDriverName)){
                    newDriverName = newDriverName.concat(String.valueOf(i));
                    i++;
                }
                idata.setVariable("jdbc.driver.name", newDriverName);
            }
            else if (jdbcPreExisting != null && jdbcPreExisting.equals("true") && usePreExisting.equals("true")){
                idata.setVariable("jdbc.driver.name",(String) driverNamesMap.get(jdbcName.getInfo()).keySet().toArray()[0]);
            }
            else {
			    idata.setVariable("jdbc.driver.name", jdbcName.getInfo());
            }

            idata.setVariable("jdbc.driver.vendor.name", (String)((JComboBox)driverVendorName.getInfoComponent()).getSelectedItem());
			idata.setVariable("jdbc.driver.module.name", moduleName.getInfo());
			idata.setVariable("jdbc.driver.xads.name", xaClassname.getInfo());
			idata.setVariable("jdbc.driver.dir.struct", directoryStructure.getInfo());
            idata.setVariable("db.driver",  jdbcName.getInfo());
			idata.setVariable("db.dialect", JBossJDBCConstants.sqlDialectMap.get(jdbcName.getInfo()));
			idata.setVariable("db.url", JBossJDBCConstants.connUrlMap.get(jdbcName.getInfo()));
			return 0;
		}

		/**
		 * Checks the given jars for validity.<br/>
		 * Return codes: <br/>
		 * 0: all is well<br/> 
		 * 1: no driver class detected (warn on this) <br/>
		 * 2: one of the files given doesn't exist, was not a jar, was an empty zip, or was unreadable (fail on this <br/>
		 *
         * @return
		 */
		protected boolean verifyJars() {
			for (String jar : getJarPaths()){
				switch(JDBCConnectionUtils.verifyJarPath(jar)){
				case 0: // jar is fine
					break;
				case 1: // given path doesn't exist (or is directory)
					setError(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.error"), jar));
					return false;
				case 2: // given path isn't a zip
					setError(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.notzip"), jar));
					return false;
				case 3: // given path is a zip, but an empty one
					setError(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.emptyzip"), jar));
					return false;
				case 4: // given remote location isn't reachable
					setError(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.remote.error"), jar));
					return false;
				case 5:
                    setError(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.exception.error"), jar));
					return false;
				}
			}
			return true;
		}
		/**
		 * Returns true if the driver class was found among the given jars
		 * @return
		 */
		protected boolean verifyDriver() {
            String driverClassName = JBossJDBCConstants.classnameMap.get(jdbcName.getInfo());
			driverClass = JDBCConnectionUtils.findDriverClass(driverClassName, JDBCConnectionUtils.convertToUrlArray(getJarPaths().toArray()));
			if (driverClass == null) {
                return false;
            } else {
                idata.setVariable("jdbc.driver.class.name", driverClassName);
                return true;
            }
		}
		/**
		 * Returns the user-entered jars in an ArrayLists
		 */
		protected List<String> getJarPaths(){
			ArrayList<String> jarList = new ArrayList<String>(1);
			Map<String, String> jars = driverJarSelectionPanel.serialize("");
			for (Entry<String, String> entry : jars.entrySet()){
				jarList.add(entry.getValue());
			}
			return jarList;
		}
		
		/**
		 * Method returns the class to look for in the given jars, based upon the selected driver vendor. Used in verifyJars() method.
		 * 
		 * @return one of the JBossJDBCConstants.*ClassCheck values, depending on the driver, or null if none exists (should be impossible)
		 */
		protected String getDriverCheckName() {
			String driverClassCheck = null;
			String driver = jdbcName.getInfo();
			if (driver.equals(JBossJDBCConstants.ibmJdbcName)) {
				driverClassCheck = JBossJDBCConstants.ibmDriverClassCheck;
			} else if (driver.equals(JBossJDBCConstants.microsoftJdbcName)) {
				driverClassCheck = JBossJDBCConstants.microsoftDriverClassCheck;
			} else if (driver.equals(JBossJDBCConstants.sybaseJdbcName)) {
				driverClassCheck = JBossJDBCConstants.sybaseDriverClassCheck;
			} else if (driver.equals(JBossJDBCConstants.oracleJdbcName)) {
				driverClassCheck = JBossJDBCConstants.oracleDriverClassCheck;
			} else if (driver.equals(JBossJDBCConstants.mysqlJdbcName)) {
				driverClassCheck = JBossJDBCConstants.mysqlDriverClassCheck;
			} else if (driver.equals(JBossJDBCConstants.postgresqlJdbcName)) {
				driverClassCheck = JBossJDBCConstants.postgresqlDriverClassCheck;
			}
			return driverClassCheck;
		}

		public void actionPerformed(ActionEvent arg0) {
			Object test1 = arg0.getSource();
			// safe to do this, since there is only one component being listened
			// for
			if (test1 == driverVendorName.getInfoComponent()) {
				JComboBox test = (JComboBox) arg0.getSource();
				String selected = (String) test.getSelectedItem();
				updateInformation(selected);
			}
		}

		private void displayCustomDSWarning(){
			// Links displayed here must have http:// in front of them or the pane will barf a big exception
			
			emitNotification(idata.langpack.getString("JBossJDBCDriverInfo.info"), true, 500);
		}

        /**
         * Sets the PathSelectionPanel's path to the input value
         */
        public void setInitialJarPath() {
            ArrayList<String> defaults = new ArrayList<String>();
            defaults.add(idata.getInstallPath());
            driverJarSelectionPanel.addInitial(defaults);
        }
    }


    @Override
    public void panelActivate() {

        // set the initial value for the first driver to the true installpath
        // we only do this once so that user input is not mangled
        if (firstActivation) {
            jdbcPanel.setInitialJarPath();
            firstActivation = false;
        }

        final String[] descriptors = new String[]{
                "standalone.xml", "standalone-ha.xml", "standalone-osgi.xml", "standalone-full-ha.xml",
                "standalone-full.xml", "host.xml", "domain.xml" };


        driverNamesMap = new LinkedHashMap<String, HashMap<String, ArrayList<String>>>();
        String preExistingDrivers = idata.getVariable("jdbc.driver.preexisting");

        //Uniquely add all the driver names found
        if (preExistingDrivers != null && preExistingDrivers.equals("true")) {
            for (String descriptor : descriptors) {

                int numJars = 0;
                String numFoundJars = idata.getVariable("jdbc.driver."+descriptor+".found.count");

                try{
                    numJars = Integer.parseInt(numFoundJars);
                } catch (Exception e) {/*Leave default at 0 */}

                for(int i = numJars; i > 0; i--) {

                    ArrayList<String> driverLocations = new ArrayList<String>();
                    String driverName = idata.getVariable("jdbc.preexisting.driver." + descriptor + "." + i + ".name");
                    String driverNameInternal = idata.getVariable("jdbc.preexisting.driver." + descriptor + "." + i + ".name.internal");
                    String driverLocation     = idata.getVariable("jdbc.preexisting.driver." + descriptor + "." + i + ".jar");

                    if(driverNameInternal != null && !driverNamesMap.containsKey(driverNameInternal))
                    {
                        driverLocations.add(driverLocation);
                        LinkedHashMap<String, ArrayList<String>> driverNameMap = new LinkedHashMap<String, ArrayList<String>>();
                        driverNameMap.put(driverName, driverLocations);
                        driverNamesMap.put(driverNameInternal,driverNameMap);
                    }
                    else
                    {
                        if (driverNamesMap.get(driverNameInternal).containsKey(driverName)) {
                            driverLocations = driverNamesMap.get(driverNameInternal).get(driverName);
                            if (!driverLocations.contains(driverLocation)) {
                                driverNamesMap.get(driverNameInternal).get(driverName).add(driverLocation);
                            }
                        }
                        else {
                        driverLocations.add(driverLocation);
                            driverNamesMap.get(driverNameInternal).put(driverName, driverLocations);
                        }
                    }
                }
            }
        }

        super.panelActivate();
    }
}
