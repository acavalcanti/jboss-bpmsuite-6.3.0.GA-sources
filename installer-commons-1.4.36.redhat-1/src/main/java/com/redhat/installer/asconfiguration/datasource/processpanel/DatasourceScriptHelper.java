package com.redhat.installer.asconfiguration.datasource.processpanel;


import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommands;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.dmr.ModelNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatasourceScriptHelper {
    // ServerCommands to run the commands with
    private static ServerCommands serverCommands;
	private static AutomatedInstallData idata;
	private static AbstractUIProcessHandler mHandler;
	
	private static final String PROPERTIES = "properties";
	private static final String JNDI_NAME = "jndi-name";
    private static final String SCRIPT = "script";
    private static final String NAME = "ds-name";
    private static final String DOMAIN = "is-domain";
	private static final String HAS_DEPLOY = "has-deploy";
	//private static final String defaultProps [] = { "db.driver", "db.url", "db.user", "db.password", "db.jndi_name"};
	
	// general info
	private static String dsScript;
    private static File dsScriptFile;
	private static String propertiesFile;
	private static ArgumentParser parser;
	private static boolean hasPropsFile;
	private static boolean hasDeployCommand;

	// new additions to allow property usage.
	private static String dsName;
	private static String dsJndiName;
	private static String dsDriver;
	private static String dsConnUrl;
	private static String dsUser;
	private static String dsPassword;
	private static boolean hasDsScript;
	
	/**
	 * This class runs the datasource helpers, for switching the SOA datasources to use the installed JDBC driver.
	 * @param handler the handler instance. IzPack required parameter.
	 * @param args the list of args from the ProcessPanel.Spec.xml
	 * @throws InterruptedException
	 */
	public static boolean run(AbstractUIProcessHandler handler, String[]args) throws InterruptedException {
		idata = AutomatedInstallData.getInstance();
		mHandler = handler;
		parser = new ArgumentParser();
		parser.parse(args);
		
		initializeStaticVariables();
        initializeServerCommands();

		if (!readPropertiesFile()) {
			return false;
		}

		List<ModelNode> addDatasourceCommandResults = new ArrayList<ModelNode>();
		addDatasourceCommandResults.addAll(installDatasource());
		return isDatasourceAddSuccessful(addDatasourceCommandResults);
	}

	private static void initializeStaticVariables(){
		hasPropsFile = parser.hasProperty(PROPERTIES);
		hasDsScript = parser.hasProperty(SCRIPT);
		hasDeployCommand = parser.hasProperty(HAS_DEPLOY) && Boolean.parseBoolean(parser.getStringProperty(HAS_DEPLOY));

		if (hasPropsFile && hasDsScript){
			dsScript = parser.getStringProperty(SCRIPT);
			dsScriptFile = new File(dsScript);
			propertiesFile = parser.getStringProperty(PROPERTIES);
			completeProperties();
		} else {
			dsConnUrl = idata.getVariable("db.url");
			dsUser = idata.getVariable("db.user");
			dsPassword = idata.getVariable("db.password");
			dsDriver = idata.getVariable("jdbc.driver.name");
			dsName = parser.getStringProperty(NAME);
			dsJndiName = parser.getStringProperty(JNDI_NAME);
		}
	}

	private static void initializeServerCommands() throws InterruptedException {
		String username = idata.getVariable("postinstall.username");
		char[] password = idata.getVariable("postinstall.password").toCharArray();

		serverCommands = null;

		int port = ServerManager.getManagementPort();
		try {
			if (ServerManager.getMode().equals(DOMAIN)){
				boolean slave = Boolean.parseBoolean(ServerManager.getSlave());
				serverCommands = ServerCommands.createLocalDomainUsernameSession(username,  password, port, slave, "default","full","full-ha","ha");
			} else {
				serverCommands = ServerCommands.createLocalStandaloneUsernameSession(username, password, port);
			}
		} catch (CliInitializationException e){
			e.printStackTrace();
			ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("postinstall.processpanel.init.error"), true);
		}
		ServerCommandsHelper.createLogger(DatasourceScriptHelper.class.getName(), serverCommands);
		ServerCommandsHelper.connectContext(mHandler, serverCommands, 0, 5);
	}

	/**
	 * Denotes a successful properties file read
	 * @return properties file read successfully or not
	 */
	private static boolean readPropertiesFile() {
		if (hasPropsFile && hasDsScript) {
			try {
				serverCommands.readPropertiesFile(propertiesFile);
			} catch (IOException e) {
				// thrown if there was some kind of error reading the properties file. At this point, this probably points to
				// misconfiguration within the XML spec, so we error out here.
				ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("DatasourceScriptHelper.propertiesfile.error"), propertiesFile), true);
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	private static List<ModelNode> installDatasource() {
		List<ModelNode> returnVal = new ArrayList<ModelNode>();
		if (dsScript != null){
			serverCommands.setResolveParameterValues(true);
			returnVal.addAll(serverCommands.runCommandsInList(ServerCommandsHelper.loadCommandsIntoList(dsScript, hasDeployCommand)));
		} else {
			serverCommands.setResolveParameterValues(false);
			returnVal.addAll(serverCommands.installDatasourceUserPwdMinimal(dsName, dsJndiName, dsDriver, dsConnUrl, dsUser, dsPassword));
		}
        serverCommands.terminateSession();
		return returnVal;
	}

	private static boolean isDatasourceAddSuccessful(List<ModelNode> commandResults) {
		List<ModelNode> failures = ServerCommandsHelper.findFailures(commandResults);

		if (!failures.isEmpty()){
			printFailureMessage(failures);
			return false;
		} else {
			printSuccessMessage();
			return true;
		}
	}
	/**
	 * Completes the associated .properties filename
	 */
	private static void completeProperties() {
		if (!propertiesFile.endsWith("properties")) {
			String driverName = idata.getVariable("db.driver.");
			if (driverName != null) {
				if (driverName.equals(JBossJDBCConstants.ibmJdbcName)) {
					propertiesFile += "db2.properties";
				} else if (driverName.equals(JBossJDBCConstants.microsoftJdbcName)) {
					propertiesFile += "mssql.properties";
				} else if (driverName.equals(JBossJDBCConstants.sybaseJdbcName)) { // sybase
					propertiesFile += "h2.properties";
				} else {
					propertiesFile += driverName + ".properties";
				}
			} else {
				propertiesFile += "h2.properties";
			}
		}
	}

	private static void printFailureMessage(List<ModelNode> failures) {
		if (dsScript != null){
			ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("DatasourceScriptHelper.failure"),dsScriptFile.getAbsolutePath()), true);
		} else {
			ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("DatasourceScriptHelper.noscript.failure"),dsName), true);
		}
		for (ModelNode failure : failures){
			ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("DatasourceScriptHelper.command.failure"), ServerCommandsHelper.getCommand(failure)), true);
			ProcessPanelHelper.printToPanel(mHandler, failure.toString(), true);
		}
	}

	private static void printSuccessMessage() {
		if (dsScript != null) {
			ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("DatasourceScriptHelper.success"), dsScriptFile.getAbsolutePath()), false);
		} else {
			ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("DatasourceScriptHelper.noscript.success"), dsName), false);
		}
	}
}
