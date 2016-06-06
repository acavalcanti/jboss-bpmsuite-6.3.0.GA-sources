package com.redhat.installer.ports.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * @author Jyoti Tripathi
 */
public class PortDefaultSave implements PanelAction
{
    /**
     * Entry point to run this action
     * @param handler Note, as per spec., if there isn't a GUI, handler is null
     */
    public void executeAction(final AutomatedInstallData idata, AbstractUIHandler handler)
    {
        String[] portVariableNames = idata.getVariables().stringPropertyNames().toArray(new String[0]);
        for (String portVariable : portVariableNames)
        {
            if ( (portVariable.startsWith("domain") || portVariable.startsWith("standalone"))
                    && !portVariable.endsWith("-1") && !portVariable.endsWith("-2") && !portVariable.endsWith(".orig"))
            {
                String value = idata.getVariable(portVariable);
                idata.setVariable(portVariable+".orig",value);
            }
        }
    }

    /**
     * There shouldn't be any need for configuration, since this is a dedicated class
     * @param configuration null if no configuration block for the action
     */
    public void initialize(PanelActionConfiguration configuration) {}
}
