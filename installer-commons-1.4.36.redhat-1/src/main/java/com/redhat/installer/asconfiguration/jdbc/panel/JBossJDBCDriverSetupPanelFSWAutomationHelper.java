package com.redhat.installer.asconfiguration.jdbc.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.panels.UserInputPanelAutomationHelper;
import com.izforge.izpack.util.VariableSubstitutor;

public class JBossJDBCDriverSetupPanelFSWAutomationHelper extends JBossJDBCDriverSetupPanelAutomationHelper implements PanelAutomation
{

    /**
     * Asks to make the XML panel data.
     *
     * @param idata     The installation data.
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
    	super.makeXMLData(idata, panelRoot);
    	
    	IXMLElement dbUser = new XMLElementImpl("db.user",panelRoot);
    	String user = idata.getVariable("db.user");
    	dbUser.setContent(user);
    	
    	IXMLElement dbPassword = new XMLElementImpl("db.password",panelRoot);
    	String password = idata.getVariable("db.password");

    	//dbPassword.setContent(password);
        dbPassword.setAttribute("autoPrompt","true");
        idata.autoPromptVars.add("db.password");
    	
    	IXMLElement dbUrl = new XMLElementImpl("db.url",panelRoot);
    	String url = idata.getVariable("db.url");
    	dbUrl.setContent(url);
    	
    	IXMLElement prev = panelRoot.getFirstChildNamed("db.user");
    	IXMLElement prev2 = panelRoot.getFirstChildNamed("db.password");
    	IXMLElement prev3 = panelRoot.getFirstChildNamed("db.url");
    	
    	if (prev != null){
    		panelRoot.removeChild(prev);
    	} 
    	
    	if (prev2 != null){
    		panelRoot.removeChild(prev2);
    	} 
    	
    	if (prev3 != null){
    		panelRoot.removeChild(prev3);
    	} 
    	
    	if (dbUser != null && dbPassword != null && dbUrl != null){
    		panelRoot.addChild(dbUser);
    		panelRoot.addChild(dbPassword);
    		panelRoot.addChild(dbUrl);
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
    	super.runAutomated(idata, panelRoot);
    	IXMLElement dbUser = panelRoot.getFirstChildNamed("db.user");
    	IXMLElement dbPassword = panelRoot.getFirstChildNamed("db.password");
    	IXMLElement dbUrl = panelRoot.getFirstChildNamed("db.url");
    	
    	if (dbUser != null && dbPassword != null && dbUrl != null){
    		VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
    		String user = dbUser.getContent();
    		//String password = dbPassword.getContent();
    		String url = dbUrl.getContent();
    		
    		user = vs.substitute(user);
    		//password = vs.substitute(password);
    		url = vs.substitute(url);
    		
    		idata.setVariable("db.user", user);
    		//idata.setVariable("db.password", password);
    		idata.setVariable("db.url", url);

            /**
             * AutoPrompt handler for db password.
             * Currently a little hacky since this panel doesn't use
             * the normal key value pair way of storing vars for automation.
             */
            UserInputPanelAutomationHelper.getAutoPromptVariable(
                    idata,
                    "db.password",
                    "true",
                    idata.langpack.getString("db.password.text"),
                    idata.langpack.getString("db.password.re.text"));
    	}
    }
}