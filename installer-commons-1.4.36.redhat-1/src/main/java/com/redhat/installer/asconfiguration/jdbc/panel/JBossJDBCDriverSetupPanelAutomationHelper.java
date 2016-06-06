package com.redhat.installer.asconfiguration.jdbc.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.VariableSubstitutor;

public class JBossJDBCDriverSetupPanelAutomationHelper implements PanelAutomation
{

    /**
     * Asks to make the XML panel data.
     *
     * @param idata     The installation data.
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
	IXMLElement jdbcJarPaths = new XMLElementImpl("jdbc.driver.jar",panelRoot);
	int count = 1;
	while (true){
	    String path = idata.getVariable("jdbc.driver.jar-"+count+"-path");
	    if (path != null){
	        IXMLElement jarPath = new XMLElementImpl("path-"+count, panelRoot);
	        jarPath.setContent(path);
	        jdbcJarPaths.addChild(jarPath);
	        count++;
	    } else {
	        // no more 
	        break;
	    }
	}

	IXMLElement jdbcName = new XMLElementImpl("jdbc.driver.name",panelRoot);
	String name = idata.getVariable("jdbc.driver.name");
	jdbcName.setContent(name);

	IXMLElement jdbcModuleName = new XMLElementImpl("jdbc.driver.module.name",panelRoot);
	String moduleName = idata.getVariable("jdbc.driver.module.name");
	jdbcModuleName.setContent(moduleName);

	IXMLElement jdbcXaClass = new XMLElementImpl("jdbc.driver.xa.class",panelRoot);
	String xaClass = idata.getVariable("jdbc.driver.xads.name");
	jdbcXaClass.setContent(xaClass);

	IXMLElement jdbcDirStruct = new XMLElementImpl("jdbc.driver.dir.struct",panelRoot);
	String dirStruct = idata.getVariable("jdbc.driver.dir.struct");
	jdbcDirStruct.setContent(dirStruct);
	
	IXMLElement dbDriver = new XMLElementImpl("db.driver",panelRoot);
	String driver = idata.getVariable("db.driver");
	dbDriver.setContent(driver);
	
	IXMLElement dbDialect = new XMLElementImpl("db.dialect",panelRoot);
	String dialect = idata.getVariable("db.dialect");
	dbDialect.setContent(dialect);


	/*IXMLElement jdbcProfileName = new XMLElementImpl("jdbc.driver.profile",panelRoot);
	String xmlName = idata.getVariable("jdbc.driver.profile");
	jdbcProfileName.setContent(xmlName);*/
	
	// do these even do anything?

        IXMLElement prev = panelRoot.getFirstChildNamed("jdbc.driver.jar");
        IXMLElement prev2 = panelRoot.getFirstChildNamed("jdbc.driver.name");
        IXMLElement prev3 = panelRoot.getFirstChildNamed("jdbc.driver.module.name");
        IXMLElement prev4 = panelRoot.getFirstChildNamed("jdbc.driver.xa.class");
        IXMLElement prev5 = panelRoot.getFirstChildNamed("jdbc.driver.dir.struct");
        IXMLElement prev6 = panelRoot.getFirstChildNamed("db.driver");
        IXMLElement prev7 = panelRoot.getFirstChildNamed("db.dialect");
        
       // IXMLElement prev6 = panelRoot.getFirstChildNamed("jdbc.driver.profile");
        if (prev != null)
        {
            panelRoot.removeChild(prev);
        }
        if (prev2 != null){
            panelRoot.removeChild(prev2);
        }
        if (prev3 != null){
            panelRoot.removeChild(prev3);
        }
        if (prev4 != null){
            panelRoot.removeChild(prev4);
        }
        if (prev5 != null){
            panelRoot.removeChild(prev5);
        } 
        if (prev6 != null) {
        	panelRoot.removeChild(prev6);
		}
        if (prev7 != null) {
        	panelRoot.removeChild(prev7);
		}

        if (jdbcJarPaths != null && jdbcName != null && jdbcModuleName != null && jdbcXaClass != null && jdbcDirStruct != null) {
            panelRoot.addChild(jdbcJarPaths);
            panelRoot.addChild(jdbcName);
            panelRoot.addChild(jdbcModuleName);
            panelRoot.addChild(jdbcXaClass);
            panelRoot.addChild(jdbcDirStruct);
            panelRoot.addChild(dbDriver);
            panelRoot.addChild(dbDialect);
            //panelRoot.addChild(jdbcProfileName);
        }
    }

    /**
     * Asks to run in the automated mode.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML tree to read the data from.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        //IXMLElement jdbcDriverInstall = panelRoot.getFirstChildNamed("jdbc.driver.install");
        IXMLElement jdbcJarPaths = panelRoot.getFirstChildNamed("jdbc.driver.jar");
        IXMLElement jdbcName = panelRoot.getFirstChildNamed("jdbc.driver.name");
        IXMLElement jdbcModuleName = panelRoot.getFirstChildNamed("jdbc.driver.module.name");
        IXMLElement jdbcXaClass = panelRoot.getFirstChildNamed("jdbc.driver.xa.class");
        IXMLElement jdbcDirStruct = panelRoot.getFirstChildNamed("jdbc.driver.dir.struct");
        IXMLElement dbDriver = panelRoot.getFirstChildNamed("db.driver");
        IXMLElement dbDialect = panelRoot.getFirstChildNamed("db.dialect");
	//IXMLElement jdbcProfileName = panelRoot.getFirstChildNamed("jdbc.driver.profile");
        if (jdbcJarPaths != null && jdbcName != null && jdbcModuleName != null && jdbcXaClass != null && jdbcDirStruct != null 
        		&& dbDriver != null && dbDialect != null) {
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
          //  String install = jdbcDriverInstall.getContent();
            // jar path support
            int count = 1;
            if (jdbcJarPaths.hasChildren())
            {
                while (true)
                {
                    IXMLElement path = jdbcJarPaths.getFirstChildNamed("path-" + count);
                    if (path != null)
                    {
                        idata.setVariable("jdbc.driver.jar-" + count + "-path", path.getContent());
                        count++;
                    }
                    else
                    {
                        break;
                    }
                }
            }
            
            String name = jdbcName.getContent();
            String module = jdbcModuleName.getContent();
            String xaClass = jdbcXaClass.getContent();
            String dir = jdbcDirStruct.getContent();
            String driver = dbDriver.getContent();
            String dialect = dbDialect.getContent();
            name = vs.substitute(name);
            module = vs.substitute(module);
            xaClass = vs.substitute(xaClass);
            dir = vs.substitute(dir);
            driver = vs.substitute(driver);
            dialect = vs.substitute(dialect);
            //profile = vs.substitute(profile);
            idata.setVariable("jdbc.driver.name", name);
            idata.setVariable("jdbc.driver.module.name", module);
            idata.setVariable("jdbc.driver.xads.name", xaClass);
            idata.setVariable("jdbc.driver.dir.struct", dir);
            idata.setVariable("db.driver", driver);
            idata.setVariable("db.dialect", dialect);
        }
    }
}
