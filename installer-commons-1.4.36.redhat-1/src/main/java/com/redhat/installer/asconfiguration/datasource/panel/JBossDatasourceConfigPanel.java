package com.redhat.installer.asconfiguration.datasource.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.GenericInformationJPanel;
import com.izforge.izpack.util.GUIHelper;
import com.redhat.installer.asconfiguration.datasource.validator.DataSourcePropertyValidator;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;
import com.redhat.installer.gui.dynamic.DynamicComponentsPanel;
import com.redhat.installer.gui.dynamic.TwoJTextFieldJPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Class: JBossDatasourceConfigPanel
 * Description: Panel for data source configuration
 * 
 */
public class JBossDatasourceConfigPanel extends IzPanel
{

    private static final long serialVersionUID = 6753215353591061624L;

    private DatasourcePanel datasourcePanel;

    private String panelid = "";
    private String panelName = "";

    public JBossDatasourceConfigPanel(InstallerFrame parent, InstallData idata)
    {
        this(parent, idata, new IzPanelLayout());
    }

    public JBossDatasourceConfigPanel(InstallerFrame parent, InstallData idata,
            LayoutManager2 layout)
    {
        super(parent, idata, layout);
        // Boarder Setup
        // izpack does something somewhere that makes this border actually be a compound visible
        // border. somewhere in the look and feel no doubt.
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());

        JPanel mainPanel = GUIHelper.createMainPanel(!parent.hasBackground);
        if(idata.getVariable("panelID") != null) {
            panelid = idata.getVariable("panelID") + ".";
            panelName = (idata.getVariable("panelID")).replace("Datasource", "");
        }
        JLabel title;
        if(idata.getVariable("isBxMS").equals("true")){
            title = new JLabel(idata.langpack.getString(panelName + "JBossDatasourceConfigPanel.headline"),
                    SwingConstants.CENTER);
            title.setOpaque(!parent.hasBackground);
            Font font = title.getFont();
            font = font.deriveFont(Font.BOLD, font.getSize()*2.0f);
            title.setFont(font);
        }
        else {
            title = LabelFactory.createTitleLabel(
                    idata.langpack.getString(panelName + "JBossDatasourceConfigPanel.headline"),
                    !parent.hasBackground);
        }
        mainPanel.add(title, GUIHelper.getTitleConstraints());

        try
        {
            datasourcePanel = new DatasourcePanel(idata, !parent.hasBackground);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        mainPanel.add(datasourcePanel, GUIHelper.getContentPanelConstraints());

        JScrollPane scroller = GUIHelper.createPanelScroller(getBorder(), mainPanel,
                !parent.hasBackground);
        add(scroller, BorderLayout.CENTER);
    }

    @Override
    public void panelActivate()
    {
        datasourcePanel.setDriverName(idata.getVariable("jdbc.driver.name"));
        datasourcePanel.resetFields();
        datasourcePanel.setUsernamePasswordFields();
        datasourcePanel.revalidate();
        datasourcePanel.repaint();
        revalidate();
        repaint();
        super.panelActivate();
    }

    public void makeXMLData(IXMLElement panelRoot)
    {
        new JBossDatasourceConfigPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    public boolean isValidated()
    {
        if (!datasourcePanel.validated())
        {
            emitError("Error", datasourcePanel.getError());
            return false;
        }
        else
        {
            return true;
        }
    }

    public String getSummaryBody()
    {
        return datasourcePanel.getSummaryBody();
    }

    class DatasourcePanel extends JPanel implements ActionListener
    {

        private static final long serialVersionUID = -8911720119777317385L;

        private GenericInformationJPanel securityType;

        private GenericInformationJPanel securityDomain;

        private GenericInformationJPanel datasourceName;

        private GenericInformationJPanel username;

        private GenericInformationJPanel password;

        private GenericInformationJPanel jndiName;

        private GenericInformationJPanel minPoolSize;

        private GenericInformationJPanel maxPoolSize;

        private GenericInformationJPanel datasourceType;

        private GenericInformationJPanel connectionUrl;

        private GenericInformationJPanel xaDatabaseName;

        private GenericInformationJPanel xaRecoveryUser;

        private GenericInformationJPanel xaRecoveryPwd;

        private DynamicComponentsPanel[] xaExtraPropsList = new DynamicComponentsPanel[JBossJDBCConstants.driverIndex.size()];

        private JButton connectionTestButton;
        
        // lists for drawing the components efficiently
        // perhaps can make only one list and populate it everytime the panel is loaded. will have
        // to
        // see if it's worth it
        private List<Component> oracleComps;

        private List<Component> sybaseComps;

        private List<Component> microsoftComps;

        private List<Component> mysqlComps;

        private List<Component> postgresqlComps;

        private List<Component> ibmComps;

        private List<Component> currentDriver; // this list will hold a ref to one of the above
                                               // lists,
                                               // based upon the driver name
        
        

        // dynamic panel for additional XA properties

        private boolean isXa;

        private String driverName;

        private String error;

        private boolean isSecurityDomain;

        private InstallData idata;

        private boolean isOpaque;

        private boolean setDefaults;
        
        private int defaultCount = 0;


        
        private DataSourcePropertyValidator validator = new DataSourcePropertyValidator();

        /**
         * Create the panel.
         * 
         * @throws InvocationTargetException
         * @throws NoSuchMethodException
         * @throws IllegalAccessException
         * @throws InstantiationException
         * @throws IllegalArgumentException
         * @throws SecurityException
         */
        public DatasourcePanel(InstallData idata, boolean isOpaque) throws SecurityException,
                IllegalArgumentException, InstantiationException, IllegalAccessException,
                NoSuchMethodException, InvocationTargetException
        {
            this.idata = idata;
            isSecurityDomain = true; // default is to show the Security Domain option first

            GridBagLayout gridBagLayout = new GridBagLayout();
            gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            gridBagLayout.columnWeights = new double[] { 1.0};
            gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    0, 0, 0, 0};
            setLayout(gridBagLayout);
            setOpaque(isOpaque);

            // just in case the description grows in size
            JTextArea infoLabel = LabelFactory.createMultilineLabel(
                    idata.langpack.getString("JBossDatasourceConfigPanel.info"), isOpaque);
            GridBagConstraints gbcInfo = new GridBagConstraints();
            gbcInfo.anchor = GridBagConstraints.NORTHWEST;
            gbcInfo.fill = GridBagConstraints.BOTH;
            gbcInfo.gridx = 0;
            gbcInfo.gridy = 0;
            add(infoLabel, gbcInfo);

            datasourceName = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.name"), "myNew"+ panelName +"Datasource");
            GridBagConstraints gbcName = new GridBagConstraints();
            datasourceName.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.name.tooltip"));
            datasourceName.setColumns(40);
            gbcName.anchor = GridBagConstraints.WEST;
            gbcName.fill = GridBagConstraints.HORIZONTAL;
            gbcName.gridx = 0;
            gbcName.gridy = 1;
            add(datasourceName, gbcName);

            jndiName = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.jndi.name"),
                    "java:jboss/example/TestDS");
            GridBagConstraints gbcJndi = new GridBagConstraints();
            jndiName.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.jndi.name.tooltip"));
            jndiName.setColumns(40);
            gbcJndi.anchor = GridBagConstraints.WEST;
            gbcJndi.fill = GridBagConstraints.HORIZONTAL;
            gbcJndi.gridx = 0;
            gbcJndi.gridy = 2;
            add(jndiName, gbcJndi);

            minPoolSize = new GenericInformationJPanel(JTextFieldNum("0"),
                    idata.langpack.getString("JBossDatasourceConfigPanel.pool.min"));
            GridBagConstraints gbcMin = new GridBagConstraints();
            minPoolSize.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.pool.min.tooltip"));
            minPoolSize.setColumns(40);
            gbcMin.anchor = GridBagConstraints.WEST;
            gbcMin.fill = GridBagConstraints.HORIZONTAL;
            gbcMin.gridx = 0;
            gbcMin.gridy = 3;
            add(minPoolSize, gbcMin);

            maxPoolSize = new GenericInformationJPanel(JTextFieldNum("20"),
                    idata.langpack.getString("JBossDatasourceConfigPanel.pool.max"));
            maxPoolSize.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.pool.max.tooltip"));
            maxPoolSize.setColumns(40);
            GridBagConstraints gbcMax = new GridBagConstraints();
            gbcMax.fill = GridBagConstraints.HORIZONTAL;
            gbcMax.gridx = 0;
            gbcMax.gridy = 4;
            add(maxPoolSize, gbcMax);

            securityType = new GenericInformationJPanel(JComboBox.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.securitytype"),
                    new String[] {
                            idata.langpack.getString("JBossDatasourceConfigPanel.security.domain"),
                            idata.langpack.getString("JBossDatasourceConfigPanel.username")
                                    + " + "
                                    + idata.langpack
                                            .getString("JBossDatasourceConfigPanel.password")});
            securityType.addActionListener(this);
            GridBagConstraints gbcSecType = new GridBagConstraints();
            securityType.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.securitytype.tooltip"));
            gbcSecType.fill = GridBagConstraints.HORIZONTAL;
            gbcSecType.gridx = 0;
            gbcSecType.gridy = 5;
            add(securityType, gbcSecType);

            securityDomain = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.security.domain"),
                    "mySecurityDomain");
            GridBagConstraints gbcSecDom = new GridBagConstraints();
            securityDomain.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.security.domain.tooltip"));
            securityDomain.setColumns(40);
            gbcSecDom.fill = GridBagConstraints.HORIZONTAL;
            gbcSecDom.gridx = 0;
            gbcSecDom.gridy = 6;
            add(securityDomain, gbcSecDom);

            datasourceType = new GenericInformationJPanel(JComboBox.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.ds.type"), new String[] {
                            "Datasource", "Datasource XA"});
            datasourceType.addActionListener(this);
            datasourceType.setColumns(40);
            GridBagConstraints gbcDataType = new GridBagConstraints();
            datasourceType.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.ds.type.tooltip"));
            gbcDataType.fill = GridBagConstraints.HORIZONTAL;
            gbcDataType.gridx = 0;
            gbcDataType.gridy = 7;
            add(datasourceType, gbcDataType);

            connectionUrl = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.url"),
                    JBossJDBCConstants.ibmConnUrl);
            // to make GridBagLayout happy.
            // if we do not do this, if the text grows too large, it has a complete fit, making
            // all other components stretch out off the screen. sigh.
            connectionUrl.getInfoComponent().setPreferredSize(
                    connectionUrl.getInfoComponent().getPreferredSize());
            connectionUrl.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.url.tooltip"));
            connectionUrl.setColumns(40);
            GridBagConstraints gbcConnUrl = new GridBagConstraints();
            gbcConnUrl.fill = GridBagConstraints.HORIZONTAL;
            gbcConnUrl.gridx = 0;
            gbcConnUrl.gridy = 8;
            add(connectionUrl, gbcConnUrl);

            username = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.username")+":", "");
            username.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.username.tooltip"));
            username.setColumns(40);

            password = new GenericInformationJPanel(JPasswordField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.password")+":", "");
            password.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.password.tooltip"));
            password.setColumns(40);

            xaDatabaseName = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.xa.databasename"), "");
            xaDatabaseName.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.xa.databasename.tooltip"));
            GridBagLayout xaPropsLayout2 = new GridBagLayout();
            xaPropsLayout2.columnWidths = new int[] { 190};
            xaPropsLayout2.columnWeights = new double[] { 0.0, 1.0};
            xaPropsLayout2.rowWeights = new double[] { 0.0, Double.MIN_VALUE};
            xaRecoveryUser = new GenericInformationJPanel(JTextField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.xa.recoveryuser"), "");
            xaRecoveryUser.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.xa.recoveryuser.tooltip"));
            xaRecoveryUser.setColumns(40);
            xaRecoveryPwd = new GenericInformationJPanel(JPasswordField.class,
                    idata.langpack.getString("JBossDatasourceConfigPanel.xa.recoverypass"), "");
            xaRecoveryPwd.getInfoComponent().setToolTipText(idata.langpack.getString("JBossDatasourceConfigPanel.xa.recoverypass.tooltip"));
            xaRecoveryPwd.setColumns(40);

            connectionTestButton = new JButton(idata.langpack.getString("JBossDatasourceConfigPanel.test.connectionTestButtonText"));
            connectionTestButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    testDatasourceConnection();
                }
            });
            GridBagConstraints gbcConnectionTestButton = new GridBagConstraints();
            gbcConnectionTestButton.fill = GridBagConstraints.NONE;
            gbcConnectionTestButton.anchor = GridBagConstraints.EAST;
            gbcConnectionTestButton.gridx = 0;
            gbcConnectionTestButton.gridy = 9;
            add(connectionTestButton, gbcConnectionTestButton);

            fillUniqueLists();
            currentDriver = ibmComps; // default
        }

        /**
         * Attempts to establish a connection with the provided URL / driver.
         */
        private void testDatasourceConnection() {
            String driverClassName = idata.getVariable("db.driver");
            String errorWindowTitle = idata.langpack.getString("installer.error");
            String connectionFailedTitle = idata.langpack.getString("db.test.failure.title");
            String connectionFailedText = idata.langpack.getString("db.test.failure");
            String connectionSuccessText = idata.langpack.getString("db.test.success");
            String driverNullText = idata.langpack.getString("JBossDatasourceConfigPanel.test.driver.class.null");


            if (driverClassName != null){
                Object[] jarPaths = JDBCConnectionUtils.readIdataForJarPaths("jdbc.driver.jar").toArray();
                Class driverClass = JDBCConnectionUtils.findDriverClass(JBossJDBCConstants.classnameMap.get(driverClassName), JDBCConnectionUtils.convertToUrlArray(jarPaths));

                if (isSecurityDomain()){
                    emitError(errorWindowTitle, idata.langpack.getString("JBossDatasourceConfigPanel.test.no.usernamepassword"));
                    return;
                }

                Driver driverInstance = null;

                try {
                   driverInstance = (Driver) driverClass.newInstance();
                } catch (InstantiationException e){
                    e.printStackTrace();
                } catch (IllegalAccessException e){
                    e.printStackTrace();
                }

                Object jdbcConnection = JDBCConnectionUtils.getDatabaseConnection(driverInstance, username.getInfo(), password.getInfo(), connectionUrl.getInfo());

                if (jdbcConnection != null){
                    if (jdbcConnection.getClass().equals(String.class)){
                        emitError(connectionFailedTitle, (String) jdbcConnection);
                        return;
                    }
                    try {
                        ((Connection) jdbcConnection).close();
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                    emitNotification(connectionSuccessText);
                } else {
                    emitError(connectionFailedTitle, connectionFailedText);
                }
            } else {
                emitError(errorWindowTitle, driverNullText);
            }
        }

        /**
         * sets the initial values of the password / username fields. these will not be changed
         * after this call
         */
        private void setUsernamePasswordFields()
        {
            // if they are blank, set them to defaults. otherwise, leave them alone
            if (username.getInfo().isEmpty()) username.setInfo(idata.getVariable("adminUser"));
            if (password.getInfo().isEmpty()) password.setInfo(idata.getVariable("adminPassword"));

            if (xaRecoveryUser.getInfo().isEmpty())
                xaRecoveryUser.setInfo(idata.getVariable("adminUser"));

            if (xaRecoveryPwd.getInfo().isEmpty())
                xaRecoveryPwd.setInfo(idata.getVariable("adminPassword"));
        }

        /**
         * adds the required components to each vendor's list. these lists are switched at runtime
         * depending on the options selected
         */
        private void fillUniqueLists()
        {
        	
        	ibmComps = new ArrayList<Component>();
            //ibmComps.add(xaDatabaseName);
            ibmComps.add(xaRecoveryUser);
            ibmComps.add(xaRecoveryPwd);
            ibmComps.add(createExtraPropsPanel(JBossJDBCConstants.ibmJdbcName));
            
            oracleComps = new ArrayList<Component>();
            oracleComps.add(xaRecoveryUser);
            oracleComps.add(xaRecoveryPwd);
            oracleComps.add(createExtraPropsPanel(JBossJDBCConstants.oracleJdbcName));
            
            microsoftComps = new ArrayList<Component>();
            microsoftComps.add(xaRecoveryUser);
            microsoftComps.add(xaRecoveryPwd);            
            microsoftComps.add(createExtraPropsPanel(JBossJDBCConstants.microsoftJdbcName));
            
            sybaseComps = new ArrayList<Component>();
            sybaseComps.add(xaRecoveryUser);
            sybaseComps.add(xaRecoveryPwd);
            sybaseComps.add(createExtraPropsPanel(JBossJDBCConstants.sybaseJdbcName));
            
            mysqlComps = new ArrayList<Component>();
            mysqlComps.add(xaRecoveryUser);
            mysqlComps.add(xaRecoveryPwd);
            mysqlComps.add(createExtraPropsPanel(JBossJDBCConstants.mysqlJdbcName));
            
            postgresqlComps = new ArrayList<Component>();
            postgresqlComps.add(xaRecoveryUser);
            postgresqlComps.add(xaRecoveryPwd);
            postgresqlComps.add(createExtraPropsPanel(JBossJDBCConstants.postgresqlJdbcName));
        }

        private JPanel createExtraPropsPanel(String vendor)
        {
        	JPanel xaExtraPropsPanelLocal = new JPanel();
        	xaExtraPropsPanelLocal.setOpaque(false);
            GridBagLayout xaPropsLayout = new GridBagLayout();
            xaPropsLayout.columnWidths = new int[] { 190 };
            xaPropsLayout.columnWeights = new double[] { 0.0, 1.0 };
            xaPropsLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
            xaExtraPropsPanelLocal.setLayout(xaPropsLayout);

            JLabel xaExtraPropertiesLbl = new JLabel(
                    idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.label"));
            GridBagConstraints gbc_extraPropsLbl = new GridBagConstraints();
            gbc_extraPropsLbl.anchor = GridBagConstraints.NORTHWEST;
            gbc_extraPropsLbl.gridx = 0;
            gbc_extraPropsLbl.gridy = 0;
            gbc_extraPropsLbl.ipady = 38; // Use this padding when having headers for dynamic compnent
            xaExtraPropsPanelLocal.add(xaExtraPropertiesLbl, gbc_extraPropsLbl);

            JLabel xaExtraPropertiesHeaderLblLeft = new JLabel(
                    idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.col1.label"));
            xaExtraPropertiesHeaderLblLeft.setOpaque(false);
            JLabel xaExtraPropertiesHeaderLblRight = new JLabel(
                    idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.col2.label"));
            xaExtraPropertiesHeaderLblRight.setOpaque(false);

            JPanel xaExtraPropertiesHeader = new JPanel(new GridLayout(1, 1));
            xaExtraPropertiesHeader.add(xaExtraPropertiesHeaderLblLeft);
            xaExtraPropertiesHeader.add(xaExtraPropertiesHeaderLblRight);
            xaExtraPropertiesHeader.setOpaque(false);
            DynamicComponentsPanel xaExtraPropsLocal = new DynamicComponentsPanel(TwoJTextFieldJPanel.class, 30, 0,
                    xaExtraPropertiesHeader, idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.tooltip"));
                      
            xaExtraPropsLocal.setAddButtonText(idata.langpack
                    .getString("JBossDatasourceConfigPanel.xa.properties.addone"));
            xaExtraPropsLocal.setRemoveButtonText(idata.langpack
                    .getString("JBossDatasourceConfigPanel.xa.properties.removeone"));
            
            
            xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(vendor)] = (xaExtraPropsLocal);
            GridBagConstraints gbc_extraPropsPanel = new GridBagConstraints();
            gbc_extraPropsPanel.anchor = GridBagConstraints.EAST;
            gbc_extraPropsPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_extraPropsPanel.gridx = 1;
            gbc_extraPropsPanel.gridy = 0;

            xaExtraPropsPanelLocal.add(xaExtraPropsLocal, gbc_extraPropsPanel);
            
            return xaExtraPropsPanelLocal;
        }
        public String getDriverName()
        {
            return driverName;
        }

        public void setDriverName(String driverName)
        {
            if (this.driverName == null || !this.driverName.equals(driverName))
            {
                setDefaults = true;
            }
            this.driverName = driverName;
        }

        /**
         * Switches between username and password, and security domain
         * 
         * @param displayed whether the username/password is currently being displayed or not
         */
        private void switchUsernamePassword(boolean displayed)
        {
            GridBagLayout layout = (GridBagLayout) getLayout();
            isSecurityDomain = displayed;
            if (!displayed)
            {
                GridBagConstraints gbc = layout.getConstraints(securityDomain);
                remove(securityDomain);
                remove(datasourceType);
                remove(connectionTestButton);
                add(username, gbc);
                gbc.gridy++;
                add(password, gbc);
                if (isXa())
                {
                    for (Component comp : currentDriver)
                    {
                        remove(comp);
                    }
                    gbc.gridy++;
                    add(datasourceType, gbc);

                    for (Component comp : currentDriver)
                    {
                        gbc.gridy++;
                        add(comp, gbc);
                    }
                }
                else
                {
                    remove(connectionUrl);
                    gbc.gridy++;
                    add(datasourceType, gbc);
                    gbc.gridy++;
                    add(connectionUrl, gbc);
                }
                gbc.gridy++;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.EAST;
                add(connectionTestButton, gbc);
            }
            else
            {
                GridBagConstraints gbc = layout.getConstraints(username);
                remove(username);
                add(securityDomain, gbc);
                remove(password);
            }
            validate();
            repaint();
        }

        /**
         * Sets the fields to appropriate defaults, or sets to previously entered values
         * 
         */

        public void resetFields()
        {
            // only set the defaults if the user has changed the driver, and hence they should
            // expect to have their changes destroyed
            // maybe implement a warning here someday
            if (setDefaults)
            {
                datasourceType.setInfo(driverName, driverName + " XA");
                idata.setVariable(panelid + "jdbc.datasource.xa",driverName);
                idata.setVariable(panelid + "JBossDatasourceConfigPanel.securitytype.value", idata.langpack.getString("JBossDatasourceConfigPanel.security.domain"));
 
                //Ensure to clear extra properties (Database, RecoveryUser, RecoveryPassword)
                if (isXa) 
                {
	                for (Component comp : currentDriver)
	                {
	                    remove(comp);
	                }
                }
                if (driverName.equals(JBossJDBCConstants.ibmJdbcName))
                {
                    jndiName.setInfo("java:/DB2" + panelName + "DS");
                    xaDatabaseName.setInfo("ibmdb2db");
                    connectionUrl.setInfo(JBossJDBCConstants.ibmConnUrl);
                    currentDriver = ibmComps;
                }

                if (driverName.equals(JBossJDBCConstants.sybaseJdbcName))
                {
                    jndiName.setInfo("java:jboss/SybaseDB" + panelName);
                    xaDatabaseName.setInfo("mydatabase");
                    connectionUrl
                            .setInfo(JBossJDBCConstants.sybaseConnUrl);
                    currentDriver = sybaseComps;
                }

                if (driverName.equals(JBossJDBCConstants.mysqlJdbcName))
                {
                    jndiName.setInfo("java:jboss/MySql" + panelName + "DS");
                    xaDatabaseName.setInfo("mysqldb");
                    connectionUrl.setInfo(JBossJDBCConstants.mysqlConnUrl);
                    currentDriver = mysqlComps;
                }

                if (driverName.equals(JBossJDBCConstants.postgresqlJdbcName))
                {
                    jndiName.setInfo("java:jboss/Postgres" + panelName + "DS");
                    xaDatabaseName.setInfo("postgresdb");
                    connectionUrl.setInfo(JBossJDBCConstants.postgresqlConnUrl);
                    currentDriver = postgresqlComps;
                }

                if (driverName.equals(JBossJDBCConstants.microsoftJdbcName))
                {
                    jndiName.setInfo("java:/MSSQL" + panelName + "DS");
                    xaDatabaseName.setInfo("mssqldb");
                    connectionUrl
                            .setInfo(JBossJDBCConstants.microsoftConnUrl);
                    currentDriver = microsoftComps;
                }

                if (driverName.equals(JBossJDBCConstants.oracleJdbcName))
                {
                    jndiName.setInfo("java:/Oracle" + panelName + "DS");
                    connectionUrl.setInfo(JBossJDBCConstants.oracleConnUrl);
                    currentDriver = oracleComps;
                }
                // Reset Dynamic Components Panel, and Extra Props Panel
                if (xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)] != null) { 
                	xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)].clearDynamicComponents();
                }
                setDefaults = false;
                xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)].initializeDefaults(JBossJDBCConstants.defaultsMap.get(driverName));
                switchDatasourceProperties(false);
            }
        }

        /**
         * Switches the Components within this panel to the appropriate ones, if we switch between
         * XA and non-XA
         * 
         * @param displayed whether the datasource properties are being displayed or not
         */

        private void switchDatasourceProperties(boolean displayed)
        {
            GridBagLayout layout = (GridBagLayout) getLayout();
            isXa = displayed;
			if (displayed) {
                remove(connectionTestButton);
				if (driverName.equals(JBossJDBCConstants.oracleJdbcName)) {
					emitNotification(idata.langpack.getString("jdbc.driver.oracle.warning")
							+ "\n        GRANT SELECT ON     sys.dba_pending_transactions TO user;\n        GRANT SELECT ON sys.pending_trans$ TO user;\n        GRANT SELECT ON sys.dba_2pc_pending TO user;\n        GRANT EXECUTE ON sys.dbms_xa TO user;");
				}
				GridBagConstraints gbc = layout.getConstraints(connectionUrl);
				remove(connectionUrl);
				for (Component comp : currentDriver) {
					add(comp, gbc);
					gbc.gridy++;
				}
/*                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.EAST;
                add(connectionTestButton,gbc);*/
			}
            else
            {
                remove(connectionTestButton);
                GridBagConstraints gbc = layout.getConstraints(datasourceType);
                for (Component comp : currentDriver)
                {
                    remove(comp);
                }
                gbc.gridy++;
                add(connectionUrl, gbc);
                gbc.gridy++;
                gbc.fill = GridBagConstraints.NONE;
                gbc.anchor = GridBagConstraints.EAST;
                add(connectionTestButton, gbc);
            }
            validate();
            repaint();
            
        }

        public boolean isXa()
        {
            return isXa;
        }

        public void actionPerformed(ActionEvent e)
        {
            JComboBox test;
            if (e.getSource() instanceof JComboBox)
            {
                test = (JComboBox) e.getSource();

                if (test.equals((JComboBox) securityType.getInfoComponent()))
                {
                    if (((String) test.getSelectedItem()).contains("Security"))
                    {
                        if (!isSecurityDomain)
                        {
                        	idata.setVariable(panelid +
                        			"JBossDatasourceConfigPanel.securitytype.value"
                        			,(String) test.getSelectedItem());
                            switchUsernamePassword(true); // if we aren't already displaying the
                                                          // Security Domain option, switch to it
                        }
                    }
                    else
                    {
                        if (isSecurityDomain)
                        {
                        	idata.setVariable(panelid +
                        			"JBossDatasourceConfigPanel.securitytype.value"
                        			,(String) test.getSelectedItem());
                            switchUsernamePassword(false); // if we are displaying security domain
                                                           // information, switch to user / password
                        }
                    }
                }
                if (test.equals((JComboBox) datasourceType.getInfoComponent()))
                {
                    if (((String) test.getSelectedItem()).contains("XA"))
                    {
                        if (!isXa)
                        {
                        	idata.setVariable(panelid + "jdbc.datasource.xa", (String) test.getSelectedItem());
                            switchDatasourceProperties(true);
                        }
                    }
                    else
                    {
                        if (isXa)
                        {
                        	idata.setVariable(panelid + "jdbc.datasource.xa",(String) test.getSelectedItem());
                            switchDatasourceProperties(false);
                        }
                    }
                }
            }
        }

        private void setError(String s)
        {
            error = s;
        }

        public String getError()
        {
            return error;
        }

        public boolean isSecurityDomain()
        {
            return isSecurityDomain;
        }

        // helper to check integer is positive, for ports
        private boolean isPositiveInt(String test)
        {
            for (char c : test.toCharArray())
            {
                if (!Character.isDigit(c)) return false;
            }
            return true;
        }

        // helper to check port validity
        private boolean isValidPort(String port)
        {
            int test = 0;
            if (!isPositiveInt(port))
            {
                return false;
            }
            try
            {
                test = Integer.parseInt(port);
            }
            catch (NumberFormatException e)
            {
                return false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (test > 65535)
            {
                return false;
            }
            return true;
        }

        /**
         * Returns true if all fields are filled in with legitimate information
         * 
         * @return
         */
        public boolean validated()
        {
            if (datasourceName.getInfo().trim().isEmpty() ||
        		jndiName.getInfo().trim().isEmpty() 	  ||
        		minPoolSize.getInfo().trim().isEmpty()    ||
        		maxPoolSize.getInfo().trim().isEmpty())
            {
                setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorname"));
                return false;
            }

            if (!Pattern.matches("^\\S+$", datasourceName.getInfo())){
                setError(idata.langpack.getString("JBossDatasourceConfigPanel.nospaces"));
                return false;
            }
            
            if (!Pattern.matches("(java:/|java:jboss/)(.+)?[^ ].*", jndiName.getInfo()))
            {
                setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorjndi"));
                return false;
            } 
            
            if (!Pattern.matches("[0-9]+", minPoolSize.getInfo().trim()) ||
                !Pattern.matches("[0-9]+", maxPoolSize.getInfo().trim())) 
            {
            	setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorpool"));
                return false;
            }
            else if (Integer.parseInt(minPoolSize.getInfo().trim()) >  Integer.parseInt(maxPoolSize.getInfo().trim()))
            {
            	setError(idata.langpack.getString("JBossDatasourceConfigPanel.invalidpool"));
                return false;
            }

            if(panelid.equals("BusinessCentralDatasource.")) {
                idata.setVariable(panelid + "jdbc.datasource.name", "");
                idata.setVariable(panelid + "jdbc.datasource.jndiname", "");
            }

            if(idata.getVariable("BusinessCentralDatasource.jdbc.datasource.name") != null) {

                if ((idata.getVariable("BusinessCentralDatasource.jdbc.datasource.name")).equals(datasourceName.getInfo())
                        && panelid.equals("DashbuilderDatasource.")) {

                    setError(idata.langpack.getString("JBossDatasourceConfigPanel.duplicateName"));
                    return false;
                }
                if ((idata.getVariable("BusinessCentralDatasource.jdbc.datasource.jndiname")).equals(jndiName.getInfo())
                        && panelid.equals("DashbuilderDatasource.")) {

                    setError(idata.langpack.getString("JBossDatasourceConfigPanel.duplicateJNDI"));
                    return false;
                }
            }

            idata.setVariable(panelid + "jdbc.datasource.name", datasourceName.getInfo());
            idata.setVariable(panelid + "jdbc.datasource.jndiname", jndiName.getInfo());
            idata.setVariable(panelid + "jdbc.datasource.minpoolsize", minPoolSize.getInfo().trim());
            idata.setVariable(panelid + "jdbc.datasource.maxpoolsize", maxPoolSize.getInfo().trim());

            if (isSecurityDomain())
            {
                if (securityDomain.getInfo().trim().isEmpty())
                {
                	 setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorsecurity"));
                     return false;
                }
                else if (!Pattern.matches("^\\S+$", securityDomain.getInfo())){
                    setError(idata.langpack.getString("SecurityDomainPanel.nospaces"));
                    return false;
                }
                idata.setVariable(panelid + "jdbc.datasource.securitydomain", securityDomain.getInfo());
                idata.setVariable(panelid + "jdbc.datasource.issecuritydomain", "true");
            }
            else
            {
                if (!(username.getInfo().trim().isEmpty() || username.getInfo().trim().isEmpty())
                        && !(password.getInfo().isEmpty() || password.getInfo().trim().isEmpty()))
                {
                    idata.setVariable(panelid + "jdbc.datasource.username", username.getInfo().trim());
                    idata.setVariable(panelid + "jdbc.datasource.password", password.getInfo());
                    /**
                     * Must explicitly set this to null because this var is
                     * checked later on during the server commands in order to
                     * send the correct CLI command to server when using
                     * username/password instead of securitydomain.
                     */
/*                    if (idata.getVariable("jdbc.datasource.securitydomain") != null) {
                        idata.setVariable("jdbc.datasource.securitydomain",
                                null);
                    }*/
                    idata.setVariable(panelid + "jdbc.datasource.issecuritydomain","false");
                }
                else
                {
                    setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorsecurity"));
                    return false;
                }

            }
            
            idata.setVariable(panelid + "jdbc.datasource.datasourcetype", String.valueOf(isXa()));
            

            // if we don't have an XA datasource, we only set the connectionurl
            if (!isXa())
            {
            	if (connectionUrl.getInfo().trim().isEmpty()) 
            	{
                    setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorname"));
                    return false;
            	}
                idata.setVariable(panelid + "jdbc.datasource.connectionurl", connectionUrl.getInfo());
            }
            // if we do have an XA datasource, we set all of the variables that are contained within
            // the
            // currentDriver list, plus the extra properties
            // (extra properties is always a member of currentDriver
            else
            {
                if (currentDriver.contains(xaRecoveryUser)){
                	
                	if(xaRecoveryUser.getInfo().trim().isEmpty()){
                        setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorname"));
                        return false;
                	}
                	idata.setVariable(panelid + "jdbc.datasource.xa.recoveryuser", xaRecoveryUser.getInfo().trim());
                	
                }
                if (currentDriver.contains(xaRecoveryPwd)){
                	if(xaRecoveryPwd.getInfo().trim().isEmpty()){
                        setError(idata.langpack.getString("JBossDatasourceConfigPanel.errorname"));
                        return false;
                	}
                	idata.setVariable(panelid + "jdbc.datasource.xa.recoverypass", xaRecoveryPwd.getInfo());
                }

                int setCode = -1;
                // Check that properties are valid
                /**
                if (!xaExtraProps.noEmptyProperties()){
                    setError(idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.wrongFormat"));
                    return false;
                }
                else**/ 
                if(!xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)].validateDynamicComponents(validator)) {
                    setError(validator.getErrorMessageId());
                    //setError(idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.invalidChars"));
                    return false;
                }
                else if (!xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)].noKeyDuplication()) {
                    setError(idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.duplicateProperty"));
                    return false; 
                }
                
                
                if (xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)].getComponentCount() != 0)
                {
                    defaultCount = 0;
                    Map<String, String> extProps = xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)]
                            .serialize("jdbc.datasource.xa.extraprops");

                    for (Entry<String, String> entry : extProps.entrySet())
                    {
                        idata.setVariable(entry.getKey(), entry.getValue());
                    }
                }
            }

            return true;
        }

        public String getSummaryBody()
        {
            StringBuffer retval = new StringBuffer(256);
            if (idata.getVariable("jdbc.driver.install").equals("false") || idata.getVariable("datasource.install").equals("false"))
            {
                return null;
            }
            else
            {
            	String nameK 	   = idata.langpack.getString("JBossDatasourceConfigPanel.name");
            	String nameV       = idata.getVariable(panelid + "jdbc.datasource.name");
            	String jndiK       = idata.langpack.getString("JBossDatasourceConfigPanel.jndi.name");
            	String jndiV       = idata.getVariable(panelid + "jdbc.datasource.jndiname");
            	String minK        = idata.langpack.getString("JBossDatasourceConfigPanel.pool.min");
            	String minV        = idata.getVariable(panelid + "jdbc.datasource.minpoolsize");
            	String maxK        = idata.langpack.getString("JBossDatasourceConfigPanel.pool.max");
            	String maxV 	   = idata.getVariable(panelid + "jdbc.datasource.maxpoolsize");
                String secureTypeV = idata.getVariable(panelid+ "JBossDatasourceConfigPanel.securitytype.value");
                String secureTypeK = idata.langpack.getString("JBossDatasourceConfigPanel.securitytype");

            	String securePairK = (isSecurityDomain) ? (idata.langpack.getString("JBossDatasourceConfigPanel.security.domain") + ":")
            						   : (idata.langpack.getString("JBossDatasourceConfigPanel.username") + ":");
                String securePairV = (isSecurityDomain) ? idata
                        .getVariable(panelid + "jdbc.datasource.securitydomain") : idata
                        .getVariable(panelid + "jdbc.datasource.username");
                String dataTypeK   = idata.langpack.getString("JBossDatasourceConfigPanel.ds.type");
                String dataTypeV   = idata.getVariable(panelid +"jdbc.datasource.xa");

            	
                retval.append(nameK       + " " + nameV + "<br>"
                            + jndiK 	  + " " + jndiV + "<br>"
                            + minK 		  + " " + minV + "<br>"
                            + maxK 		  + " " + maxV + "<br>"
                            + secureTypeK + " " + secureTypeV + "<br>"
                            + securePairK + " " +securePairV + "<br>"				   
                            + dataTypeK   + " " + dataTypeV + "<br>"
                        	);

                if (isXa())
                {
                    /** Recovery User */
                    retval.append(idata.langpack
                            .getString("JBossDatasourceConfigPanel.xa.recoveryuser")
                            + " "
                            + idata.getVariable(panelid + "jdbc.datasource.xa.recoveryuser") + "<br>");
                    
                    /** XA Properteries */
                    retval.append(listExtraProps());
                }
                else
                {
                    retval.append(idata.langpack.getString("JBossDatasourceConfigPanel.xa.connectionurl")+ 
                    		" " +
                    		idata.getVariable(panelid + "jdbc.datasource.connectionurl"));
                }
            }
            return retval.toString();
        }

        /**
         * Returns an HTML formatted string containing the given xa Extra properties.
         * perhaps this should read idata instead of the actual fields, but unless we hit problems 
         * with the fields not being what is actually in idata, I think it's fine (Tom)
         * If we do change this, it will need to be changed for the jars as well, in JBossJDBCDriverSetupPanel
         * @return
         */
        private String listExtraProps()
        {
            StringBuffer sb = new StringBuffer();
            sb.append(idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.label") + "<br/>");
            int count = 1;
            for (Entry<String,String> entry : getExtraPropsMap(xaExtraPropsList[JBossJDBCConstants.driverIndex.indexOf(driverName)].serialize("")).entrySet()){
                sb.append(count + ". "+ entry.getKey() + " = " + entry.getValue() + "<br/>");
                count++;
            }
            return sb.toString();
        }

        /**
         * This method assumes that the map passed in is one resulting from link
         * DynamicComponentsPanel.serialize() call
         * 
         * @param allEntries
         * @return
         */
        private Map<String, String> getExtraPropsMap(Map<String, String> allEntries)
        {
            List<String> names = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            boolean skip = true;
            //String key = "";
            
            for (Entry<String, String> entry : allEntries.entrySet())
            {
            	if (skip) { skip = false; names.add(entry.getValue()); }
            	else      { skip = true; values.add(entry.getValue());}
            }
            Map<String, String> newEntries = new HashMap<String, String>();

            for (int i = 0; i < names.size(); i++)
            {
                newEntries.put(names.get(i), values.get(i));
            }
            return newEntries;
        }
    }
	private JTextField JTextFieldNum(String txt) {
        JTextField textField = new JTextField(txt) {
            public void processKeyEvent(KeyEvent ev) {
              char c = ev.getKeyChar();
              if (Character.isDigit(c) || ev.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                super.processKeyEvent(ev);
              }
            }
        };
      return textField;
    }
}
