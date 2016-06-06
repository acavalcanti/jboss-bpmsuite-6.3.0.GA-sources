package com.redhat.installer.asconfiguration.jdbc.panel;

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
import java.net.URL;
import java.util.*;

public class JBossJDBCDriverSetupPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{

	protected static AutomatedInstallData idata;
	protected boolean hasDriver;
	protected boolean hasRemote;
	protected String[] driverOptions;
    protected boolean skipJarPrompt;

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
	{
		return true;
	}

	public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
			PrintWriter printWriter)
	{
		return true;
	}

	public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
	{
		new JBossJDBCDriverSetupPanelAutomationHelper().makeXMLData(idata, panelRoot);
	}

	private boolean decideToInstall(){
		String info = StringTool.removeHTML(idata.langpack.getString("JBossJDBCDriverSetupPanel.info"));
		String stored = idata.getVariable("jdbc.driver.install");
		String q1 = idata.langpack.getString("JBossJDBCDriverSetupPanel.choice.yes"); //Install JDBC Driver
		String q2 = idata.langpack.getString("JBossJDBCDriverSetupPanel.choice.no"); // This string might be missing from lang pack.
		Shell console = Shell.getInstance();
		boolean prevPath = false;
		if (stored != null){
			prevPath = (stored.equals("true"));
		}
		System.out.println(StringTool.removeHTML(info));
		while (true){
			try {
				System.out.println(" 0 [" + (!prevPath ? "x" : " ") +"] " + StringTool.removeHTML(q2));
				System.out.println(" 1 [" + (prevPath ? "x" : " ") +"] " + StringTool.removeHTML(q1));
				String input = console.getInput();
				if (!input.trim().isEmpty()){
					if (input.equals("0")){
						idata.setVariable("jdbc.driver.install","false");
						return false;
					} else if (input.equals("1")){
						idata.setVariable("jdbc.driver.install","true");
						return true;
					}
				} else {
					return prevPath;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}	

	protected boolean setDriverChoice(String input, Map<String, Object[]>  driverNames){

		String jdbcNameLbl = idata.langpack.getString("JBossJDBCDriverSetupPanel.jdbcname.label");
		String moduleNameLbl = idata.langpack.getString("JBossJDBCDriverSetupPanel.modulename.label");
		String xaDsNameLbl = idata.langpack.getString("JBossJDBCDriverSetupPanel.xaclassname.label");
		String dirStructLbl = idata.langpack.getString("JBossJDBCDriverSetupPanel.dirstruct.label");

		
		idata.setVariable("jdbc.driver.console.vendor", input);
		if (setDriverVariables(driverOptions[Integer.parseInt(input)], driverNames)){
			return true;
		}
		
		System.out.println(jdbcNameLbl + " " + idata.getVariable("db.driver"));
		System.out.println(moduleNameLbl + " " + idata.getVariable("jdbc.driver.module.name"));
		System.out.println(xaDsNameLbl + " " + idata.getVariable("jdbc.driver.xads.name"));
		System.out.println(dirStructLbl + " " + idata.getVariable("jdbc.driver.dir.struct"));
		return false;
	}

	protected boolean setDriverVariables(String input, Map<String, Object[]>  driverNames) {
		String jdbcName;
		String moduleName;
		String xaDsName;
		String dirStruct;

		if (input.equals(JBossJDBCConstants.ibmVendorName)){	
			jdbcName = JBossJDBCConstants.ibmJdbcName;
			moduleName = JBossJDBCConstants.ibmModuleName;
			xaDsName = JBossJDBCConstants.ibmXaDsName;
			dirStruct = JBossJDBCConstants.ibmDirStruct;
			
		}
		else if (input.equals(JBossJDBCConstants.sybaseVendorName)){
			jdbcName = JBossJDBCConstants.sybaseJdbcName;
			moduleName = JBossJDBCConstants.sybaseModuleName;
			xaDsName = JBossJDBCConstants.sybaseXaDsName;
			dirStruct = JBossJDBCConstants.sybaseDirStruct;
		}
		else if (input.equals(JBossJDBCConstants.mysqlVendorName)){
			jdbcName = JBossJDBCConstants.mysqlJdbcName;
			moduleName = JBossJDBCConstants.mysqlModuleName;
			xaDsName = JBossJDBCConstants.mysqlXaDsName;
			dirStruct = JBossJDBCConstants.mysqlDirStruct;
		}
		else if (input.equals(JBossJDBCConstants.postgresqlVendorName)){
			jdbcName = JBossJDBCConstants.postgresqlJdbcName;
			moduleName = JBossJDBCConstants.postgresqlModuleName;
			xaDsName = JBossJDBCConstants.postgresqlXaDsName;
			dirStruct = JBossJDBCConstants.postgresqlDirStruct;
		}
		else if (input.equals(JBossJDBCConstants.microsoftVendorName)){
			jdbcName = JBossJDBCConstants.microsoftJdbcName;
			moduleName = JBossJDBCConstants.microsoftModuleName;
			xaDsName = JBossJDBCConstants.microsoftXaDsName;
			dirStruct = JBossJDBCConstants.microsoftDirStruct;
		}
		else if (input.equals(JBossJDBCConstants.oracleVendorName)){
			jdbcName = JBossJDBCConstants.oracleJdbcName;
			moduleName = JBossJDBCConstants.oracleModuleName;
			xaDsName = JBossJDBCConstants.oracleXaDsName;
			dirStruct = JBossJDBCConstants.oracleDirStruct;
		} else {
			return true;
		}

        if (driverNames.containsKey(jdbcName)){
            ArrayList<String> jarLocations = (ArrayList<String>) driverNames.get(jdbcName)[1];
            String jarLocation = jarLocations.get(0);
            idata.setVariable("jdbc.driver.path", jarLocation);
            idata.setVariable("jdbc.driver.name",  (String)  driverNames.get(jdbcName)[0]);
            skipJarPrompt = true;
        }
        else {
            idata.setVariable("jdbc.driver.path", "");
            idata.setVariable("jdbc.driver.name", jdbcName);
            skipJarPrompt = false;
        }
        if (idata.getVariable("db.driver") == null || !idata.getVariable("db.driver").equals(jdbcName))
        {
            idata.setVariable("db.url", JBossJDBCConstants.connUrlMap.get(jdbcName));
        }

		idata.setVariable("jdbc.driver.vendor.name", input);
		idata.setVariable("jdbc.driver.module.name", moduleName);
		idata.setVariable("jdbc.driver.xads.name", xaDsName);
		idata.setVariable("jdbc.driver.dir.struct", dirStruct);	
		idata.setVariable("db.driver",  jdbcName);
		idata.setVariable("db.dialect", JBossJDBCConstants.sqlDialectMap.get(jdbcName));


		return false;
	}

	private void chooseProfile(){
		String prevChoice = idata.getVariable("jdbc.driver.profile");
		Shell console = Shell.getInstance();
		boolean unset = true;
		if (prevChoice == null){
			prevChoice = "0";
		}

		while (unset){
			try{ 
				String profilePrompt = idata.langpack.getString("JBossJDBCDriverSetupPanel.dropdown.text");
				System.out.println(profilePrompt);
				System.out.println(" 0 [" + (prevChoice.equals("0") ? "x" : " ") +"]  " + idata.langpack.getString("JBossJDBCDriverSetupPanel.config.dropdown.1"));
				System.out.println(" 1 [" + (prevChoice.equals("1") ? "x" : " ") +"]  " + idata.langpack.getString("JBossJDBCDriverSetupPanel.config.dropdown.2"));
				String input = null;
				input = console.getInput();
				if (!input.trim().isEmpty()){
					unset = setProfileChoice(input); 
				} else{
					unset = setProfileChoice(prevChoice);
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}

	private boolean setProfileChoice(String input){
		if (input.equals("0")){
			idata.setVariable("jdbc.driver.profile","standalone");	
			return false;
		} else if (input.equals("1")){
			idata.setVariable("jdbc.driver.profile","domain");	
			return false;
		} else {
			return true;
		}
	}

    protected void chooseDriver()
    {
        chooseDriver( new HashMap<String, Object[]>() );
    }
	protected void chooseDriver(Map<String, Object[]>  driverNames)
    {
		String prevChoice = idata.getVariable("jdbc.driver.console.vendor");
		Shell console = Shell.getInstance();
		boolean unset = true;
		if (prevChoice == null){
			prevChoice = "0";
		}
		while (unset){
			try{
				String driverPrompt = idata.langpack.getString("JBossJDBCDriverSetupPanel.dropdown.label");
				System.out.println(driverPrompt);
				for (int i = 0; i < driverOptions.length; i++){
					System.out.println(" " + i + " ["+(Integer.parseInt(prevChoice) == i ? "x" : " " )+"] "+driverOptions[i]);
					
				}
				String input = null;
				input = console.getInput(); //Choose driver

				if (!input.isEmpty()){
					unset = setDriverChoice(input, driverNames);
				} else {
					unset = setDriverChoice(prevChoice, driverNames);
				}

			}
            // if the user inputs a non-number, or a number > driverOptions.length, we'll hit these, so we do a continue;
            catch (NumberFormatException e) {
                continue;
            } catch (ArrayIndexOutOfBoundsException e) {
                continue;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

	protected boolean enterJarPaths() {
		String jarPathPrompt = idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.label");
		String jarPathPrompt2 = idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.col1.label");
        String jarPathPrompt3 = idata.langpack.getString("JBossJDBCDriverSetupPanel.jars.col3.label");

		String input = null;
		List<String> jarPaths = new ArrayList<String>();
		int pathCount = 0;

		System.out.println(jarPathPrompt);
		Shell console = Shell.getInstance();
		while (true) {
			try
            {
                if (jarPaths.size() > 0)
                {
                    System.out.println(jarPathPrompt3);
                }
                else
                {
				    System.out.println(jarPathPrompt2);
                }

				input = console.getLocation(false);
				if (!input.isEmpty()) {
                    if (jarPaths.contains(input)) {
                        System.out.println(idata.langpack.getString("JBossJDBCDriverSetupPanel.duplicate.path"));
                        continue;
                    }
					if (!verifyJar(input)) {
						// verifyJar takes care of error printing
						// System.out.println(error);
						continue;
					}
					jarPaths.add(input);
					// idata.setVariable("jdbc.driver.jar" + "-" + pathCount +
					// "-path", input);
					pathCount++;
				} else {
					if (pathCount == 0 || (!hasDriver && !hasRemote)) {
						// instead of forcing user to enter some jar before they
						// can go back and change driver
						// no jars contained the driver, and there are no remote
						// jars (which we can't inspect at install time)
						// unfortunately, we are
						// stuck having to clear the list and start again
						if (pathCount == 0){
							System.out.println(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.nopaths"));
						} else {
							int answer = PanelConsoleHelper.askYesNo(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.nodriver"), JBossJDBCConstants.classnameMap.get(idata.getVariable("jdbc.driver.name"))), true);
							if (answer == AbstractUIHandler.ANSWER_YES){
								// if the user thinks this is ok, we let them continue.
								break;
							}
						}
						int answer = PanelConsoleHelper.askYesNo(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.changedriver"), true);
						if (answer == AbstractUIHandler.ANSWER_YES) {
							chooseDriver(); // choose the drive and return false to restart the jar path process
						}
						hasDriver = false;
						return false;
					} else {
						// we have remote jars. break!
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// we got here, so now we add all of the jars entered into idata, because we know them to be valid and true
		for (int i = 0; i< pathCount; i++)
        {
			idata.setVariable("jdbc.driver.jar-" + (i+1) + "-path", jarPaths.get(i));
		}
		return true;
	}
	
	/**
	 * Makes sure that the given path meets the following criteria:<br/>
	 * 1) Is not a directory or a non-existent file <br/>
	 * 2) Is not an empty zip file (no ZipEntries)<br/>
	 * Also, if the jar contains the driver class, the hasDriver boolean will be set
	 * correctly, allowing us to know if the user input a jar containing the driver
	 * 
	 * @param path
	 * @return
	 */
	protected boolean verifyJar(String path){
		switch (JDBCConnectionUtils.verifyJarPath(path)){
		case 0:
			break;
		case 1:
			System.out.println(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.error"), path));
			return false;
		case 2:
			System.out.println(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.notzip"), path));
			return false;
		case 3:
			System.out.println(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.emptyzip"), path));
			return false;
		case 4: 
			System.out.println(String.format(idata.langpack.getString("JBossJDBCDriverSetupPanel.path.remote.error"), path));
			return false;
		case 5: 
			return false;
		}
		
		Object[] jarPath = new Object[]{path};
		
		URL[] jarUrls = JDBCConnectionUtils.convertToUrlArray(jarPath);
		
		Class<?> jdbcDriver = JDBCConnectionUtils.findDriverClass(JBossJDBCConstants.classnameMap.get(idata.getVariable("jdbc.driver.name")), jarUrls);

        // if we haven't already identified a driver
		if (!hasDriver) {
            if (jdbcDriver != null) {
                hasDriver = true;
            }
        }
		return true;
	}

	public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
	{

		this.idata = idata;
		hasDriver = false;
		hasRemote = false;
		String product = idata.getVariable("product.name");
		//copied directly from GUI class. who needs inheritance when you have copy paste? /s
		boolean isEap = product != null ? product.equals("eap") : false;
		if (isEap)
        {
			driverOptions =	createArray(JBossJDBCConstants.ibmVendorName, JBossJDBCConstants.oracleVendorName,
                    JBossJDBCConstants.mysqlVendorName, JBossJDBCConstants.postgresqlVendorName,
                    JBossJDBCConstants.sybaseVendorName, JBossJDBCConstants.microsoftVendorName);
		}
        else
        {
			driverOptions = createArray(JBossJDBCConstants.ibmVendorName, JBossJDBCConstants.oracleVendorName,
                    JBossJDBCConstants.mysqlVendorName, JBossJDBCConstants.postgresqlVendorName, JBossJDBCConstants.microsoftVendorName);
		}


        chooseDriver();
        while(!enterJarPaths()){} // repeat this until the function reports false, which means the paths are chosen correctly





			//	chooseProfile();
		//}
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

	/** 
	 * Code duplication from GUI version of this class.
	 */
	private String[] createArray(String ... array) {
		return array;
	}
}
