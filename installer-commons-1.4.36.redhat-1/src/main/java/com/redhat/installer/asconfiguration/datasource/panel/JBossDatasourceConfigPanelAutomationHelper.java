package com.redhat.installer.asconfiguration.datasource.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.panels.UserInputPanelAutomationHelper;

public class JBossDatasourceConfigPanelAutomationHelper implements PanelAutomation
{

	private static final String RUN_AUTOMATED = "JBossDatasourceConfigPanel.run.automated";
    /**
     * Asks to make the XML panel data.
     * 
     * @param idata The installation data.
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        String panelIds = idata.getVariable("datasourcePanelIDs");
        String[] ids = panelIds.split(",");
        for (String panelId : ids) {
            String xmlCheck = idata.getVariable(panelId+".xml.written");
            if (xmlCheck==null || xmlCheck.equals("false")) {
                String driverName = idata.getVariable(panelId + ".jdbc.driver.name");

                IXMLElement dsName = new XMLElementImpl(panelId + ".jdbc.datasource.name", panelRoot);
                String name = idata.getVariable(panelId + ".jdbc.datasource.name");
                dsName.setContent(name);

                IXMLElement dsJndi = new XMLElementImpl(panelId + ".jdbc.datasource.jndiname", panelRoot);
                String jndi = idata.getVariable(panelId + ".jdbc.datasource.jndiname");
                dsJndi.setContent(jndi);

                IXMLElement dsMin = new XMLElementImpl(panelId + ".jdbc.datasource.minpoolsize", panelRoot);
                String minPool = idata.getVariable(panelId + ".jdbc.datasource.minpoolsize");
                dsMin.setContent(minPool);

                IXMLElement dsMax = new XMLElementImpl(panelId + ".jdbc.datasource.maxpoolsize", panelRoot);
                String maxPool = idata.getVariable(panelId + ".jdbc.datasource.maxpoolsize");
                dsMax.setContent(maxPool);

                IXMLElement dsSecDom = new XMLElementImpl(panelId + ".jdbc.datasource.securitydomain", panelRoot);
                String secDom = idata.getVariable(panelId + ".jdbc.datasource.securitydomain");
                dsSecDom.setContent(secDom);

                IXMLElement dsUsername = new XMLElementImpl(panelId + ".jdbc.datasource.username", panelRoot);
                String username = idata.getVariable(panelId + ".jdbc.datasource.username");
                dsUsername.setContent(username);


                IXMLElement isSecDom = new XMLElementImpl(panelId + ".jdbc.datasource.issecuritydomain", panelRoot);
                String isSecurityDomain = idata.getVariable(panelId + ".jdbc.datasource.issecuritydomain");
                isSecDom.setContent(isSecurityDomain);
                boolean isSecurityDomainBool = Boolean.parseBoolean(isSecurityDomain);

                /**
                 * Passwords aren't displayed in the auto xml. Instead they are saved as
                 * 'autoPrompt' variables.
                 */
                IXMLElement dsPassword = new XMLElementImpl(panelId + ".jdbc.datasource.password", panelRoot);
                String dsPasswordStr = idata.getVariable(panelId + ".jdbc.datasource.password");
                if (dsPasswordStr != null && !dsPasswordStr.isEmpty()) {
                    idata.autoPromptVars.add(panelId + ".jdbc.datasource.password");
                    dsPassword.setAttribute("autoPrompt", "true");
                }

                IXMLElement dsType = new XMLElementImpl(panelId + ".jdbc.datasource.datasourcetype", panelRoot);
                String type = idata.getVariable(panelId + ".jdbc.datasource.datasourcetype");
                dsType.setContent(type);

                IXMLElement dsUrl = new XMLElementImpl(panelId + ".jdbc.datasource.connectionurl", panelRoot);
                String source = idata.getVariable(panelId + ".jdbc.datasource.connectionurl");
                dsUrl.setContent(source);

                IXMLElement dsXaRecoveryUser = new XMLElementImpl(panelId + ".jdbc.datasource.xa.recoveryuser",
                        panelRoot);
                String recoveryUser = idata.getVariable(panelId + ".jdbc.datasource.xa.recoveryuser");
                dsXaRecoveryUser.setContent(recoveryUser);

                IXMLElement dsXaRecoveryPass = new XMLElementImpl(panelId + ".jdbc.datasource.xa.recoverypass",
                        panelRoot);

                /**
                 * Passwords are handled as the autoPrompt variables.
                 */
                String recoveryPass = idata.getVariable(panelId + ".jdbc.datasource.xa.recoverypass");
                if (recoveryPass != null && !recoveryPass.isEmpty()) {
                    idata.autoPromptVars.add(panelId + ".jdbc.datasource.xa.recoverypass");
                    dsXaRecoveryPass.setAttribute("autoPrompt", "true");
                }

                IXMLElement dsXaExtraProps = new XMLElementImpl(panelId + ".jdbc.datasource.xa.extraprops", panelRoot);
                int count = 1;
                while (true) {
                    String propName = idata.getVariable(panelId + ".jdbc.datasource.xa.extraprops-" + count + "-name");
                    String propVal = idata.getVariable(panelId + ".jdbc.datasource.xa.extraprops-" + count + "-value");
                    if (propName != null && propVal != null) {
                        IXMLElement xaPropName = new XMLElementImpl("prop-" + count + "-name", panelRoot);
                        IXMLElement xaPropVal = new XMLElementImpl("prop-" + count + "-value", panelRoot);
                        xaPropName.setContent(propName);
                        xaPropVal.setContent(propVal);
                        dsXaExtraProps.addChild(xaPropName);
                        dsXaExtraProps.addChild(xaPropVal);
                        count++;
                    } else {
                        // no more valid properties
                        break;
                    }
                }

                if (dsName != null && dsJndi != null && dsMin != null && dsMax != null && dsSecDom != null
                        && dsPassword != null && dsUsername != null && dsType != null && dsUrl != null
                        && dsXaExtraProps != null && isSecDom != null) {
                    panelRoot.addChild(dsName);
                    panelRoot.addChild(dsJndi);
                    panelRoot.addChild(dsMin);
                    panelRoot.addChild(dsMax);
                    panelRoot.addChild(isSecDom);
                    if (isSecurityDomainBool) {
                        panelRoot.addChild(dsSecDom);
                    } else {
                        panelRoot.addChild(dsUsername);
                        panelRoot.addChild(dsPassword);
                    }
                    panelRoot.addChild(dsType);

                    if (dsType.getContent().equals("true")) { // xa datasource;
                        panelRoot.addChild(dsXaExtraProps);
                        panelRoot.addChild(dsXaRecoveryUser);
                        panelRoot.addChild(dsXaRecoveryPass);
                    } else {
                        panelRoot.addChild(dsUrl);
                    }
                }
                idata.setVariable(panelId+"xml.written", "true");
            }
        }
    }

    /**
     * Asks to run in the automated mode.
     * 
     * @param idata The installation data.
     * @param panelRoot The XML tree to read the data from.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        String panelId = idata.getVariable("panelID");
        IXMLElement dsName = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.name");
        IXMLElement dsJndi = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.jndiname");
        IXMLElement dsMin = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.minpoolsize");
        IXMLElement dsMax = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.maxpoolsize");
        IXMLElement dsSecDom = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.securitydomain");
        IXMLElement dsUsername = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.username");
        IXMLElement dsType = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.datasourcetype");
        IXMLElement dsUrl = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.connectionurl");
        IXMLElement dsXaRecoveryUser = panelRoot
                .getFirstChildNamed(panelId + ".jdbc.datasource.xa.recoveryuser");
        IXMLElement dsXaRecoveryPass = panelRoot
                .getFirstChildNamed(panelId + ".jdbc.datasource.xa.recoverypass");
        IXMLElement dsXaExtraProps = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.xa.extraprops");
        IXMLElement isSecDom = panelRoot.getFirstChildNamed(panelId + ".jdbc.datasource.issecuritydomain");

        String name, jndi, min, max, type, securityDomain, username, password, connUrl, recoveryuser, recoverypass, isSecurityDomain;
        name = jndi = min = max = type = securityDomain = username = password = connUrl = recoveryuser = recoverypass = null;
        // get the strings
        if (dsName != null && dsJndi != null && dsMin != null && dsMax != null && dsType != null && isSecDom != null)
        {
            name = dsName.getContent();
            jndi = dsJndi.getContent();
            min = dsMin.getContent();
            max = dsMax.getContent();
            type = dsType.getContent();
            isSecurityDomain = isSecDom.getContent();
            idata.setVariable(panelId + ".jdbc.datasource.name", name);
            idata.setVariable(panelId + ".jdbc.datasource.jndiname", jndi);
            idata.setVariable(panelId + ".jdbc.datasource.minpoolsize", min);
            idata.setVariable(panelId + ".jdbc.datasource.maxpoolsize", max);
            idata.setVariable(panelId + ".jdbc.datasource.datasourcetype", type);
            idata.setVariable(panelId + ".jdbc.datasource.issecuritydomain", isSecurityDomain);

        }

        if (dsSecDom != null)
        {
            securityDomain = dsSecDom.getContent();
            idata.setVariable(panelId + ".jdbc.datasource.securitydomain", securityDomain);
        }
        else
        {
            username = dsUsername.getContent();
            idata.setVariable(panelId + ".jdbc.datasource.username", username);

            /**
             * The password is handled via the autoPrompt variables because we can't display it in the auto xml.
             */
            UserInputPanelAutomationHelper.getAutoPromptVariable(idata, panelId + ".jdbc.datasource.password", "true",
                    String.format(idata.langpack.getString("JBossDatasourceConfigPanel.datasource.pass.prompt"), panelId),
                    String.format(idata.langpack.getString("JBossDatasourceConfigPanel.datasource.pass.reprompt"), panelId));
        }

        if (dsUrl != null)
        {
            connUrl = dsUrl.getContent();
            idata.setVariable(panelId + ".jdbc.datasource.connectionurl", connUrl);
        }
        else
        { // save a little time
            if (dsXaRecoveryUser != null)
            {
                recoveryuser = dsXaRecoveryUser.getContent();
                idata.setVariable(panelId + ".jdbc.datasource.xa.recoveryuser", recoveryuser);
            }
            if (dsXaRecoveryPass != null)
            {
                /**
                 * The password is handled via the autoPrompt variables because we can't display it in the auto xml.
                 */
                UserInputPanelAutomationHelper.getAutoPromptVariable(idata, ".jdbc.datasource.xa.recoverypass", "true",
                        idata.langpack.getString("JBossDatasourceConfigPanel.recoverypass.prompt"),
                        idata.langpack.getString("JBossDatasourceConfigPanel.recoverypass.reprompt"));
            }
            if (dsXaExtraProps != null)
            {
                int count = 1;
                // if there aren't children, we do nothing
                if (dsXaExtraProps.hasChildren())
                {
                    while (true)
                    {
                        IXMLElement propName = dsXaExtraProps.getFirstChildNamed("prop-" + count + "-name");
                        IXMLElement propVal = dsXaExtraProps.getFirstChildNamed("prop-" + count + "-value");
                        if (propName != null && propVal != null)
                        {
                            idata.setVariable(panelId + ".jdbc.datasource.xa.extraprops-"+count+"-name", propName.getContent());
                            idata.setVariable(panelId + ".jdbc.datasource.xa.extraprops-"+count+"-value", propVal.getContent());
                            count++;
                        }
                        // no more valid properties
                        else
                        {
                            break;
                        }
                    }
                }
            }
        }
    }
}
