package com.redhat.installer.asconfiguration.datasource.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Shell;
import com.izforge.izpack.util.StringTool;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;

public class JBossDatasourceConfigPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {
	private boolean secDom;
	private static AutomatedInstallData idata;
	private String driverName;
	private String defaultUserName;
	private String defaultPassword;
	private String defaultMinPool;
	private String defaultMaxPool;
	private String defaultXaPropServerName;
	private String defaultXaPropDatabaseName;
	private String defaultXaPropPortNumber;
	private String defaultXaPropOracleUrl;
	private String defaultXaPropSybaseProtocol;
	private String defaultXaPropMicrosoftCursor;
	private String defaultRecoveryUser;
	private String defaultRecoveryPass;
	private String defaultSecurityDomain;
	private String defaultJndiName;
	private String defaultXaJndiName;
	private String defaultConnUrl;
	private String defaultDsName;
	private String panelid;
	private String panelName;
	private boolean dsIsXa;

	public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p) {
		return true;
	}

	public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter) {
		return true;
	}

	/* Do not use with readln
	private String read() throws Exception {
		byte[] byteArray = { (byte) System.in.read() };
		return new String(byteArray);
	}

	private String readln() throws Exception {
		String input = read();
		int available = System.in.available();
		if (available > 0) {
			byte[] byteArray = new byte[available];
			System.in.read(byteArray);
			input += new String(byteArray);
		}
		return input.trim();
	}*/

	public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata) {
		new JBossDatasourceConfigPanelAutomationHelper().makeXMLData(idata, panelRoot);
	}

	// just in case, no value atm
	// if add another question, we can replace the askSecDom with this and use
	// it in general
	private boolean booleanQuestion(String question, String defaultAns, String falseAns, String prevVarName) {
		question = StringTool.removeHTML(question);
		Shell console = Shell.getInstance();
		System.out.println(question);
		String prevAns = (idata.getVariable(prevVarName) != null) ? prevAns = idata.getVariable(prevVarName) : "0";

		while (true) {
			try {
				System.out.println(" 0 [" + (prevAns.equals("0") ? "x" : " ") + "] " + StringTool.removeHTML(defaultAns));
				System.out.println(" 1 [" + (!prevAns.equals("1") ? "x" : " ") + "] " + StringTool.removeHTML(falseAns));
				String input = console.getInput();
				if (!input.trim().isEmpty()) {
					return input.equals("0");
				} else {
					return prevAns.equals("0");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void enterValue(String prompt, String varName) {
		String prevAns = idata.getVariable(varName);
		Shell console = Shell.getInstance();
		if (prevAns == null) {
			prevAns = getDefault(varName);
		}

		String input = null;
		while (true) {    
			try {
				System.out.println(prompt + " [" + prevAns + "] ");
				input = console.getInput(true);
				if (input.isEmpty()) 
				{
					input = prevAns;
				}
				if (validate(varName, input)) {
					idata.setVariable(panelid + varName, input); // validate will
														// error correctly
														// for all variables
					break;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * provides validation based upon the variable name we're validating for
	 */
	private boolean validate(String varName, String value) {
		if (varName.equals("jdbc.datasource.name")) {
			if (!Pattern.matches("^\\S+$", value)){
				System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.nospaces"));
				return false;
			}
			if(panelid.equals("BusinessCentralDatasource.")) {
				idata.setVariable(panelid + "jdbc.datasource.name", "");
				idata.setVariable(panelid + "jdbc.datasource.jndiname", "");
			}
			if((idata.getVariable("BusinessCentralDatasource.jdbc.datasource.name")).equals(value) && panelid.equals("DashbuilderDatasource.")){
				System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.duplicateName"));
				return false;
			}

			return true;
		}
		if (varName.equals("jdbc.datasource.jndiname")) {
            if ((!Pattern.matches("(java:/|java:jboss/)(.+)?[^ ].*",value))) {
				System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.errorjndi"));
				return false;
			}
			if((idata.getVariable("BusinessCentralDatasource.jdbc.datasource.jndiname")).equals(value) && panelid.equals("DashbuilderDatasource.")){
				System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.duplicateJNDI"));
				return false;
			}

			return true;

		}
		if (varName.equals("jdbc.datasource.maxpoolsize")) {
			if (!isPositiveInt(value)) {
				System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.errornan"));
				return false;
			} else {
				int max = Integer.parseInt(value);
				int min = Integer.parseInt(idata.getVariable(panelid +"jdbc.datasource.minpoolsize"));
				if (max < min) {
					System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.mingtmax"));
					return false;
				} else {
					return true;
				}
			}
		}
		if (varName.equals("jdbc.datasource.minpoolsize")) {
			if (!isPositiveInt(value)) {
				System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.errornan"));
				return false;
			} else {
				return true;
			}
		}
		if (varName.equals("jdbc.datasource.securitydomain")) {
			if (!Pattern.matches("^\\S+$", value)){
				System.out.println(idata.langpack.getString("SecurityDomainPanel.nospaces"));
				return false;
			} else {
				return true;
			}
		}
		return true;
	}

	// avoid try catch expense
	private boolean isPositiveInt(String test) {
		for (char c : test.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	// is it worth it
	private String getDefault(String varName) {
		if (varName.equals("jdbc.datasource.name")) {
			return defaultDsName;
		}
		if (varName.equals("jdbc.datasource.username")) {
			return defaultUserName;
		}
		if (varName.equals("jdbc.datasource.jndiname")) {
			if (dsIsXa) {
				return defaultXaJndiName;
			} else {
				return defaultJndiName;
			}
		}
		if (varName.equals("jdbc.datasource.minpoolsize")){
			return defaultMinPool;
		}
		if (varName.equals("jdbc.datasource.maxpoolsize")){
			return defaultMaxPool;
		}
		if (varName.equals("jdbc.datasource.connectionurl")) {
			return defaultConnUrl;
		}
		if (varName.equals("jdbc.datasource.xa.servername")) {
			return defaultXaPropServerName;
		}
		if (varName.equals("jdbc.datasource.xa.databasename")) {
			return defaultXaPropDatabaseName;
		}
		if (varName.equals("jdbc.datasource.xa.portnumber")) {
			return defaultXaPropPortNumber;
		}
		if (varName.equals("jdbc.datasource.xa.oracleurl")) {
			return defaultXaPropOracleUrl;
		}
		if (varName.equals("jdbc.datasource.xa.sybaseprotocol")) {
			return defaultXaPropSybaseProtocol;
		}
		if (varName.equals("jdbc.datasource.xa.microsoftcursor")) {
			return defaultXaPropMicrosoftCursor;
		}
		if (varName.equals("jdbc.datasource.securitydomain")) {
			return defaultSecurityDomain;
		}
		
		if (varName.equals("jdbc.datasource.xa.recoveryuser")) {
		    return defaultRecoveryUser;
		}
		// for no default strings, we use the one in the JBossJDBCDriverSetupPanel section
		return idata.langpack.getString("JBossJDBCDriverSetupPanel.nodefault");
	}

	private void setDefaults() {
		// more defaults later in generalization work
		defaultUserName = idata.getVariable("adminUser");
		defaultPassword = idata.getVariable("adminPassword");
		defaultDsName = "myNew" + panelName + "Datasource";
		defaultRecoveryUser = defaultUserName;
		defaultRecoveryPass = idata.getVariable("adminPass");
		defaultSecurityDomain = "mySecurityDomain";
		defaultMinPool = "0";
		defaultMaxPool = "20";

		if (driverName.equals(JBossJDBCConstants.ibmJdbcName)) {
			defaultJndiName = "java:/DB2"+panelName+"DS";
			defaultXaJndiName = "java:/DB2XADS";
			defaultConnUrl = JBossJDBCConstants.ibmConnUrl;
			defaultXaPropDatabaseName = "ibmdb2db";
		}
		if (driverName.equals(JBossJDBCConstants.oracleJdbcName)) {
			defaultJndiName = "java:/Oracle"+panelName+"DS";
			defaultXaJndiName = "java:/XAOracleDS";
			defaultConnUrl = JBossJDBCConstants.oracleConnUrl;
			defaultXaPropOracleUrl = "jdbc:oracle:oci8:@tc";
		}
		if (driverName.equals(JBossJDBCConstants.mysqlJdbcName)) {
			defaultJndiName = "java:jboss/MySql"+panelName+"DS";
			defaultXaJndiName = "java:jboss/MySqlXADS";
			defaultConnUrl = JBossJDBCConstants.mysqlConnUrl;
			defaultXaPropServerName = "localhost";
			defaultXaPropDatabaseName = "mysqldb";
		}
		if (driverName.equals(JBossJDBCConstants.postgresqlJdbcName)) {
			defaultJndiName = "java:jboss/Postgres"+panelName+"DS";
			defaultXaJndiName = "java:jboss/PostgresXADS";
			defaultConnUrl = JBossJDBCConstants.postgresqlConnUrl;
			defaultXaPropServerName = "localhost";
			defaultXaPropDatabaseName = "postgresdb";
			defaultXaPropPortNumber = "5432";
		}
		if (driverName.equals(JBossJDBCConstants.sybaseJdbcName)) {
			defaultJndiName = "java:jboss/SybaseDB"+panelName;
			defaultXaJndiName = "java:jboss/SybaseXADB";
			defaultConnUrl = JBossJDBCConstants.sybaseConnUrl;
			defaultXaPropServerName = "myserver";
			defaultXaPropDatabaseName = "mydatabase";
			defaultXaPropPortNumber = "4100";
			defaultXaPropSybaseProtocol = "Tds";
		}
		if (driverName.equals(JBossJDBCConstants.microsoftJdbcName)) {
			defaultJndiName = "java:/MSSQL"+panelName+"DS";
			defaultXaJndiName = "java:/MSSQLXADS";
			defaultConnUrl = JBossJDBCConstants.microsoftConnUrl;
			defaultXaPropServerName = "localhost";
			defaultXaPropDatabaseName = "mssqldb";
			defaultXaPropMicrosoftCursor = "cursor";
        }
	}

	protected void enterPassword(String prompt, String varName) {
		String passwordPrompt = idata.langpack.getString(prompt);// "JBossDatasourceConfigPanel.password");
		String prevPwd = idata.getVariable(varName);// "jdbc.datasource.password");
		String noMatch = idata.langpack.getString("username.no.match.password");
		String set = "";
		Shell console = Shell.getInstance();
		
		if (prevPwd != null){
            for (int j = 0; j < prevPwd.length(); j++){
                set += "*";
            }
        } else {
            prevPwd = "";
        }
		String pwd = null;
		String pwdCheck = null;
		while (true) {
	    System.out.println(passwordPrompt + " [" + set + "]");
			try {
				pwd = new String(console.getPassword());
				if (pwd.equals("")) { pwd = prevPwd; }
				if (pwd.equals("")) { System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.pass.prompt")); continue; }
			} catch (Exception e) {
                e.printStackTrace();
            }
		
    		// Produce new masked password
            set = "";
            for (int j = 0; j < pwd.length(); j++){
                set += "*";
            }
				

			System.out.println(passwordPrompt + " [" + set + "]");
			pwdCheck = new String(console.getPassword());
			if (pwdCheck.equals("")) { pwdCheck = prevPwd; }
			if (pwd.equals(pwdCheck)) { break; } 
			else { System.out.println(noMatch); continue; }
		}
		
		idata.setVariable(panelid + varName, pwd);
	}

	
	private boolean validPort(String port) {
		int test = 0;
		if (!isPositiveInt(port)) {
			return false;
		}
		try {
			test = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (test > 65535) {
			return false;
		}
		return true;
	}

	private boolean askSecDom() {
		String question = idata.langpack.getString("JBossDatasourceConfigPanel.question");
		String prevAns = idata.getVariable("jdbc.datasource.usesecdom");
		Shell console = Shell.getInstance();
		if (prevAns == null) {
			prevAns = "0";
		}
		System.out.println(question);
		String input = null;
		while (true) {
			try {
				System.out.println(" 0 [" + (prevAns.equals("0") ? "x" : " ") + "] " + idata.langpack.getString("installer.yes"));
				System.out.println(" 1 [" + (prevAns.equals("1") ? "x" : " ") + "] " + idata.langpack.getString("installer.no"));
				input = console.getInput();
				if (!input.isEmpty()) {
					return input.equals("0");
				} else {
					return prevAns.equals("0");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean askDsType() {
		String question = idata.langpack.getString("JBossDatasourceConfigPanel.ds.type");
		String prevAns = idata.getVariable(panelid + "jdbc.datasource.datasourcetype");
		Shell console = Shell.getInstance();
		if (prevAns == null) {
			prevAns = "false";
		}
		System.out.println(question);
		String input = null;
		while (true) {
			try {
				System.out.println(" 0 [" + (prevAns.equals("false") ? "x" : " ") + "] " + idata.getVariable("jdbc.driver.name"));
				System.out.println(" 1 [" + (prevAns.equals("true") ? "x" : " ") + "] " + idata.getVariable("jdbc.driver.name") + " XA");
				input = console.getInput();
				if (input.equals("0")) {
					idata.setVariable(panelid + "jdbc.datasource.datasourcetype", "false");
					return false;
				} else if (input.equals("1")) {
					if (JBossJDBCConstants.oracleJdbcName.equals(idata.getVariable("jdbc.driver.name"))) {
						System.out
								.println(idata.langpack.getString("jdbc.driver.oracle.warning")
										+ "\n        GRANT SELECT ON     sys.dba_pending_transactions TO user;\n        GRANT SELECT ON sys.pending_trans$ TO user;\n        GRANT SELECT ON sys.dba_2pc_pending TO user;\n        GRANT EXECUTE ON sys.dbms_xa TO user;");
					}
					idata.setVariable(panelid + "jdbc.datasource.datasourcetype", "true");
					return true;
				} else if (input.equals("")) {
					idata.setVariable(panelid + "jdbc.datasource.datasourcetype", prevAns);
					if (prevAns.equals("true")) {
						return true;
					} else {
						return false;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void displayResults() {
		System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.console.summary"));
		System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.name") + " " + idata.getVariable(panelid + "jdbc.datasource.name"));
		System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.jndi.name") + " " + idata.getVariable(panelid + "jdbc.datasource.jndiname"));
		System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.pool.min") + " " + idata.getVariable(panelid + "jdbc.datasource.minpoolsize"));
		System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.pool.max") + " " + idata.getVariable(panelid + "jdbc.datasource.maxpoolsize"));
		if (secDom) {
			System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.security.domain") + " " + idata.getVariable(panelid + "jdbc.datasource.securitydomain"));
		} else {
			System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.username") + " " + idata.getVariable(panelid + "jdbc.datasource.username"));
			System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.password") + " <not shown>");
		}
		System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.ds.type") + " " + (idata.getVariable(panelid + "jdbc.datasource.datasourcetype").equals("true") ? "XA" : "Non-XA"));

		if (dsIsXa) {
            System.out.println(idata.langpack
                    .getString("JBossDatasourceConfigPanel.xa.recoveryuser")
                    + " "
                    + idata.getVariable(panelid + "jdbc.datasource.xa.recoveryuser"));
			// print the xa properties
			int count = 1;
			System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.label"));
			while (true){
				String name = idata.getVariable("jdbc.datasource.xa.extraprops-"+count+"-name");
				String value = idata.getVariable("jdbc.datasource.xa.extraprops-"+count+"-value");
				if (name != null && value != null){
					System.out.println(count+". "+ name + " = " + value);
					count++;	
				} // there are 0 properties if this hits 
				else if (count == 1){
					System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.none")); // TODO: add the langpack
					break;
				} else {
					// no more props
					break;
				}
			}
			
		} else {
			System.out.println(idata.langpack.getString("JBossDatasourceConfigPanel.url") + " " + idata.getVariable(panelid + "jdbc.datasource.connectionurl"));
		}
	}

	public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent) {
		this.idata = idata;
		panelName = idata.getVariable("panelID").replace("Datasource", "");
		panelid = idata.getVariable("panelID") + ".";
		String info = idata.langpack.getString("JBossDatasourceConfigPanel.info");
		System.out.println(info);
		driverName = idata.getVariable("jdbc.driver.name");
		dsIsXa = askDsType();
		setDefaults();
		enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.name"), "jdbc.datasource.name"); // ds
																									// name
		enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.jndi.name"), "jdbc.datasource.jndiname");
		enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.pool.min"), "jdbc.datasource.minpoolsize");
		enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.pool.max"), "jdbc.datasource.maxpoolsize");
		if (!askSecDom()) {
			secDom = false;
			enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.username"), "jdbc.datasource.username");
			enterPassword(idata.langpack.getString("JBossDatasourceConfigPanel.password"), "jdbc.datasource.password");
            idata.setVariable(panelid + "jdbc.datasource.issecuritydomain", "false");
		} else {
			secDom = true;
			enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.security.domain"), "jdbc.datasource.securitydomain");
            idata.setVariable(panelid + "jdbc.datasource.issecuritydomain", "true");
		}
		if (!dsIsXa) {
			enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.url"), "jdbc.datasource.connectionurl");
		} else {
	            enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.xa.recoveryuser"), "jdbc.datasource.xa.recoveryuser");
	            enterPassword(idata.langpack.getString("JBossDatasourceConfigPanel.xa.recoverypass"), "jdbc.datasource.xa.recoverypass");
	        
			enterPropertyValue();
		}

        if (!dsIsXa && !secDom){
            while(!datasourceConnectionSuccess()){
                if (askYesNo(idata.langpack.getString("JBossDatasourceConfigPanel.test.retestConnection"),true) == AbstractUIHandler.ANSWER_YES) {
                    enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.username"), "jdbc.datasource.username");
                    enterPassword(idata.langpack.getString("JBossDatasourceConfigPanel.password"), "jdbc.datasource.password");
                    enterValue(idata.langpack.getString("JBossDatasourceConfigPanel.url"), "jdbc.datasource.connectionurl");
                } else {
                    break;
                }
            }
        }

		displayResults();
		int i = askEndOfConsolePanel(idata);
		if (i == 1) {
			return true;
		} else if (i == 2) {
			return false;
		} else {
			return runConsole(idata, parent);
		}
	}

    /**
     * Tests the connection using the details given by the user. only possible to test while
     * the datasource is configured to use username+password and be non-XA
     * @return false if the connection fails, true if successful
     * TODO: this code is essentially duplicated in the JBossDatasourceConfigPanel.
     */
    private boolean datasourceConnectionSuccess() {
        int answer = askYesNo(idata.langpack.getString("JBossDatasourceConfigPanel.test.connectionTestButtonText")+"?",false);
        if (answer == AbstractUIHandler.ANSWER_YES){
            String connectionFailedTitle = idata.langpack.getString("db.test.failure.title");
            String connectionFailedText = idata.langpack.getString("db.test.failure");
            String connectionSuccessText = idata.langpack.getString("db.test.success");
            String driverNullText = idata.langpack.getString("JBossDatasourceConfigPanel.test.driver.class.null");
            String driverClassname = idata.getVariable("db.driver");
            if (driverClassname != null){
                Object[] jarPaths = JDBCConnectionUtils.readIdataForJarPaths("jdbc.driver.jar").toArray();
                Class driverClass = JDBCConnectionUtils.findDriverClass(JBossJDBCConstants.classnameMap.get(driverClassname), JDBCConnectionUtils.convertToUrlArray(jarPaths));

                Driver driverInstance = null;

                try {
                    driverInstance = (Driver) driverClass.newInstance();
                } catch (InstantiationException e){
                    e.printStackTrace();
                } catch (IllegalAccessException e){
                    e.printStackTrace();
                }

                Object databaseConnection = JDBCConnectionUtils.getDatabaseConnection(driverInstance, idata.getVariable("jdbc.datasource.username"), idata.getVariable("jdbc.datasource.password"), idata.getVariable("jdbc.datasource.connectionurl"));

                if (databaseConnection != null){
                    if (databaseConnection.getClass().equals(String.class)){
                        System.out.println(connectionFailedTitle+": "+ databaseConnection);
                        return false;
                    }
                    try {
                        ((Connection) databaseConnection).close();
                    } catch (SQLException e){
                        e.printStackTrace();
                    }
                    System.out.println(connectionSuccessText);
                    return true;
                } else {
                    System.out.println(connectionFailedText);
                    return false;
                }
            }
            System.out.println(driverNullText);
            return false;
        } else {
            return true;
        }
    }

    // TODO: Test this method.
    private void enterPropertyValue()
    {
        HashMap<String,String> xaPropertyMap = new HashMap<String, String>();
        String[][] defaultList = null;
        String namePrefix = "jdbc.datasource.xa.extraprops";
        String errorMsgDup = idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.duplicateProperty");
        String errorMsgBadCar = idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.invalidChars");
        String errorMsgWrongFormat = idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.wrongFormat");
        String propertyPrompt = idata.langpack.getString("JBossDatasourceConfigPanel.xa.properties.label.console");
		String vendor = idata.getVariable("jdbc.driver.name");
        String input;
        Shell console = Shell.getInstance();
        int propCount=1; // Skipping non-dynamic components: starting at i=1.
        int validatorCode = 0;
        
        
        if (driverName.equals(JBossJDBCConstants.oracleJdbcName))
        {
        	defaultList = JBossJDBCConstants.oracleDefaults;
        }
        else if (driverName.equals(JBossJDBCConstants.microsoftJdbcName))
        {
        	defaultList = JBossJDBCConstants.microsoftDefaults;
        }
        else if (driverName.equals(JBossJDBCConstants.ibmJdbcName))
        {
        	defaultList = JBossJDBCConstants.ibmDefaults;
        }
        else if (driverName.equals(JBossJDBCConstants.sybaseJdbcName)) 
        {
        	defaultList = JBossJDBCConstants.sybaseDefaults;
        }
        else if (driverName.equals(JBossJDBCConstants.mysqlJdbcName)) 
        {
        	defaultList = JBossJDBCConstants.mysqlDefaults;
        }
        else if (driverName.equals(JBossJDBCConstants.postgresqlJdbcName)) 
        {
        	defaultList = JBossJDBCConstants.postgresqlDefaults;
        }

        for (String [] entry : defaultList)
        {
        	String varName = entry[0];
    		String prevAns = idata.getVariable(namePrefix + "-" + propCount + "-value");
   		
    		if (prevAns == null) 
    		{
    			prevAns = entry[1];
    		}

    		input = null;
    		while (true) {    
    			try {
    				System.out.println(varName + " [" + prevAns + "] ");
    				input = console.getInput();
    				if (input.isEmpty()) 
    				{
    					input = prevAns;
    				}
    
					if (validate(varName, input)) 
					{
						xaPropertyMap.put(varName, input);
	                    idata.setVariable(namePrefix + "-" + propCount + "-name", varName);
	                    idata.setVariable(namePrefix + "-" + propCount + "-value", input);
	                    propCount++;
						break;
					}
					
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
        }
        
        while (true){
            try{
                System.out.println(propertyPrompt);
                input = console.getInput();
                String [] nameValuePair = input.split("=");
                if (!input.isEmpty()){
                    // If user already added this property or it does not pass
                    // validation, prompt to try again:
                    validatorCode = validateProperty(input, vendor);

                    if (validatorCode == 1) {
                        // Wrong format property/value.
                        System.out.println(errorMsgWrongFormat);
                        continue;
                    }

                    if (validatorCode == 2) {
                        // Invalid character error.
                        System.out.println(errorMsgBadCar);
                        continue;
                    }

                    if ((validatorCode == 3) || xaPropertyMap.containsKey(nameValuePair[0])) {
                        // Duplicate property error.
                        System.out.println(String.format(errorMsgDup,nameValuePair[0]));
                        continue;
                    }

                    xaPropertyMap.put(nameValuePair[0], nameValuePair[1]);
                    idata.setVariable(namePrefix + "-" + propCount + "-name", nameValuePair[0]);
                    idata.setVariable(namePrefix + "-" + propCount + "-value", nameValuePair[1]);

                    propCount++;
                } else {
                    break; 
                }               
            } catch (Exception e){e.printStackTrace();}
        }
    }

    /**
     * Validates property names by matching them against the regex
     * atJBossJDBCConstants.propertyRegExpPattern, and also ensuring
     * that the property name does not duplicate an existing property name
     * for the vendor.
     * @param input
     * @param vendor
     * @return  0: success
     *          1: Wrong Format Error
     *          2: Invalid Character Error
     *          3: Duplicate Property Error
     */
    private int validateProperty(String input, String vendor)
    {
        Pattern formatPattern = Pattern.compile(JBossJDBCConstants.hasEqualNotEmptyRegExpPattern);
        Pattern charsPattern = Pattern.compile(JBossJDBCConstants.propertyRegExpPattern);
        String [] parts = input.split("=");

        if (!formatPattern.matcher(input).matches()) {
            return 1;
        }

        if (!charsPattern.matcher(input).matches()) {
            return 2;
        }

        if (JBossJDBCConstants.vendorPropertyMap.containsKey(vendor + "." + parts[0])) {
            return 3;
        }

        return 0;
    }
}
