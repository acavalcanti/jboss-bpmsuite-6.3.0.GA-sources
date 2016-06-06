package com.redhat.installer.asconfiguration.jdbc.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Shell;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;

import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JBossJDBCDriverSetupPanelFSWConsoleHelper extends JBossJDBCDriverSetupPanelConsoleHelper implements PanelConsole
{
    private Map<String, Object[]> driverNames;
    final private String[] descriptors = new String[]{
            "standalone.xml", "standalone-ha.xml", "standalone-osgi.xml", "standalone-full-ha.xml",
            "standalone-full.xml", "host.xml", "domain.xml" };
	public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
	{
		new JBossJDBCDriverSetupPanelFSWAutomationHelper().makeXMLData(idata, panelRoot);
	}

	public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
	{
		this.idata = idata;
		hasDriver = false;
		hasRemote = false;

		String product = idata.getVariable("product.name");
		boolean isEap = product != null ? product.equals("eap") : false;
		if (isEap)
        {
			driverOptions =	createArray(JBossJDBCConstants.ibmVendorName, JBossJDBCConstants.oracleVendorName,
                    JBossJDBCConstants.mysqlVendorName, JBossJDBCConstants.postgresqlVendorName,
                    JBossJDBCConstants.sybaseVendorName, JBossJDBCConstants.microsoftVendorName);
		}
        else
        {
			driverOptions = createArray(JBossJDBCConstants.ibmVendorName, JBossJDBCConstants.oracleVendorName, JBossJDBCConstants.mysqlVendorName,
                    JBossJDBCConstants.postgresqlVendorName, JBossJDBCConstants.microsoftVendorName);
		}

        setDriverNames();
        chooseDriver(driverNames);
        if (!skipJarPrompt)
        {
            while(!enterJarPaths()){} // repeat this until the function reports false, which means the paths are chosen correctly
        }
        else
        {

            System.out.println(idata.langpack.getString("JBossJDBCDriverSetupPanel.existing.driver.prompt"));
            Object[] drivers = driverNames.get(idata.getVariable("db.driver"));
            ArrayList<String> jarPaths = (ArrayList<String>) drivers[1];
            int i = 1;
            for ( String jarPath : jarPaths )
            {
                System.out.println(jarPath);
                idata.setVariable("jdbc.driver.jar-" + (i) + "-path", jarPath);
                i++;
            }
            hasDriver = true;
        }
		
		do
        {
            setDbUser();
            enterDbPassword();
            setDbUrl();
            summarizeDbData();
		} while (testConnection());
		
	
		
		int i = askEndOfConsolePanel(idata);
		if (i == 1)
		{
			return true;
		}
		else if (i == 2)
		{
			return false;
		}
		else
		{
			return runConsole(idata, parent);
		}
	}

	private boolean testConnection() {
		int answer = askYesNo(idata.langpack.getString("db.test.connection")+"?", false);
		if (answer == AbstractUIHandler.ANSWER_YES){
			if (!hasDriver){ 
				System.out.println(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanelFSW.path.nodriver"), JBossJDBCConstants.classnameMap.get(idata.getVariable("db.driver"))));
				return false;
			}
			
			// the driver exists; try to create a driver instance
			Driver jdbcDriver = null;
			URL[] jarUrls = JDBCConnectionUtils.convertToUrlArray(JDBCConnectionUtils.readIdataForJarPaths("jdbc.driver.jar").toArray());
			Class<?> driverClass = JDBCConnectionUtils.findDriverClass(JBossJDBCConstants.classnameMap.get(idata.getVariable("db.driver")), jarUrls);
			
			try {
				jdbcDriver = (Driver) driverClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			Object conn = JDBCConnectionUtils.getDatabaseConnection(jdbcDriver, idata.getVariable("db.user"),idata.getVariable("db.password"),idata.getVariable("db.url"));

			if (conn != null){
				try {
                    if (conn.getClass().equals(String.class)) {
                        System.out.println((String) conn);
                        return true;
                    }
                    ((Connection) conn).close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				System.out.println(idata.langpack.getString("db.test.success"));
				return false;
			} else {
				System.out.println(idata.langpack.getString("db.test.failure"));
				return true;
			}

		}	else {	
			return false;
		}
	}

	protected void setDbUrl(){
		String prevChoice = idata.getVariable("db.url");
		Shell console = Shell.getInstance();
		boolean unset = true;
		
		if (prevChoice == null){
			// display the appropriate default for the given driver:
			prevChoice = idata.getVariable("jdbc.driver.console.vendor");
		}
		
		while (unset){
			try {
				String dbUrlPrompt = idata.langpack.getString("db.url.text");
				System.out.println(dbUrlPrompt + "["+prevChoice + "]");
				String input = null;
				input = console.getInput();
				if (!input.trim().isEmpty()){
					idata.setVariable("db.url", input);
					unset = false;
				} else {
					idata.setVariable("db.url", prevChoice); // default
					unset = false;
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}
		
	protected void setDbUser(){
		String prevChoice = idata.getVariable("db.user");
		Shell console = Shell.getInstance();
		boolean unset = true;
		
		if (prevChoice  == null){
			prevChoice = "";
		} 
		
		while (unset){
			try{
				String dbUserPrompt = idata.langpack.getString("db.username.text");
				System.out.println(dbUserPrompt + "[ " + prevChoice + " ]");
				String input = null;
				input = console.getInput();
				if (!input.trim().isEmpty()){
					idata.setVariable("db.user",input);
					unset = false;
				} else {
					idata.setVariable("db.user",prevChoice); // default
					unset = false;
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}

	// thanks for the inheritance IzPack /s

	protected void enterDbPassword() {
		String passwordPrompt = idata.langpack.getString("db.password.text");// "JBossDatasourceConfigPanel.password");
		String passwordReprompt = idata.langpack.getString("db.password.re.text");
		String prevPwd = idata.getVariable("db.password");// "jdbc.datasource.password");
		String noMatch = idata.langpack.getString("username.no.match.password");
		String passwordEmptyPrompt = idata.langpack.getString("username.no.password");
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
				if (pwd.equals("")) 
				{ 
					pwd = prevPwd; 
					System.out.println(passwordEmptyPrompt); 
					continue; 
					}
				
			} catch (Exception e) {
                e.printStackTrace();
            }
		
    		// Produce new masked password
            set = "";
            for (int j = 0; j < pwd.length(); j++){
                set += "*";
            }
				

			System.out.println(passwordReprompt + " [" + set + "]");
			pwdCheck = new String(console.getPassword());
			if (pwdCheck.equals("")) { pwdCheck = prevPwd; }
			if (pwd.equals(pwdCheck)) { break; } 
			else { System.out.println(noMatch); continue; }
		}
		idata.setVariable("db.password", pwd);
	}
	
	protected void summarizeDbData(){
		System.out.println(idata.langpack.getString("db.title"));
		System.out.println(idata.langpack.getString("db.username.text") +  " " + idata.getVariable("db.user"));
		System.out.println(idata.langpack.getString("db.url.text") +  " " + idata.getVariable("db.url"));
	}

	/** 
	 * Code duplication from GUI version of this class.
	 */
	private String[] createArray(String ... array) {
		return array;
	}

    private void setDriverNames(){
        driverNames = new HashMap<String, Object[]>();
        String preExistingDrivers = idata.getVariable("jdbc.driver.preexisting");
        if (preExistingDrivers.equals("true")) {
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

                    if(driverNameInternal != null && !driverNames.containsKey(driverNameInternal)){
                        driverLocations.add(driverLocation);
                        driverNames.put(driverNameInternal, new Object[] {driverName, driverLocations});
                    }
                    else {
                        driverLocations = (ArrayList<String>) driverNames.get(driverNameInternal)[1];
                        driverLocations.add(driverLocation);
                        driverNames.put(driverNameInternal,  new Object[] {driverName, driverLocations});
                    }
                }
            }
        }
    }
}
