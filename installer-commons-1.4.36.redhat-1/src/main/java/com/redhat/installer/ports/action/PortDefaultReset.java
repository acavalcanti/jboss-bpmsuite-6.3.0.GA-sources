package com.redhat.installer.ports.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Reset standalone and domain ports back to its default values when needed.
 * @author Jyoti Tripathi
 */
public class PortDefaultReset implements PanelAction
{
    private static AutomatedInstallData idata;
    private static String STANDALONE = "standalone";
    private static String DOMAIN = "domain";
    public void executeAction(final AutomatedInstallData idata, AbstractUIHandler handler)
    {
        this.idata = idata;
        String useCustomPorts = idata.getVariable("portDecision");
        String customDomainPorts = idata.getVariable("configureDomain");
        String customStandalonePorts = idata.getVariable("configureStandalone");

        if (!useCustomPorts.equals("true")) {
            resetPorts(STANDALONE);
            resetPorts(DOMAIN);
        }
        else {
            if (customStandalonePorts.equals("false")) {
                resetPorts(STANDALONE);
            }
            if (customDomainPorts.equals("false")) {
                resetPorts(DOMAIN);
            }
        }
    }

    /**
     * Reset the port values to its default values
     *
     * @param serverConfig Indicates weather you are installing standalone/domain ports
     * */
    private void resetPorts(String serverConfig)
    {
        String[] portVariableNames = idata.getVariables().stringPropertyNames().toArray(new String[0]);
        for (String portVariable : portVariableNames)
        {
            if ( portVariable.startsWith(serverConfig)
                    && !portVariable.endsWith("-1") && !portVariable.endsWith("-2")
                    && !portVariable.endsWith(".orig") && !portVariable.endsWith("port-offset"))
            {
                String value = idata.getVariable(portVariable + ".orig");
                if (value != null)
                {
                    idata.setVariable(portVariable, value);
                }
            }
        }
    }

    /**
     * There shouldn't be any need for configuration, since this is a dedicated class
     * @param configuration null if no configuration block for the action
     */
    public void initialize(PanelActionConfiguration configuration) {}
}
