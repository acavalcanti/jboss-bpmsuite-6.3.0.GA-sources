package com.redhat.installer.ports.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.redhat.installer.ports.utils.PortUtils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A validator template to check for port collisions in a configuration
 * <p/>
 * This validator can be extended and used on any custom ports panel
 *
 * @author Jyoti Tripathi, thauser
 */
public abstract class PortCollisionValidator implements DataValidator
{
    private static final String ERROR = "port.collision.check.template";
    String message = AutomatedInstallData.getInstance().langpack.getString(ERROR);
    private AutomatedInstallData idata;
    private String[] portVariableNames;

    public Status validateData (AutomatedInstallData idata) {
        this.idata = idata;
        portVariableNames  = idata.getVariables().stringPropertyNames().toArray(new String[0]);
        return findPortCollisions();
    }

    private Status findPortCollisions() {
        HashSet<Integer> ports         = new HashSet<Integer>();
        String serverConfig = getConfig();
        ArrayList<String> exclusions = getExclusions();
        for (String portVariable : portVariableNames) {
            if (portVariable.startsWith(serverConfig+".") && !portVariable.startsWith(serverConfig+".h.")
                    && !portVariable.startsWith(serverConfig+".f.") && !portVariable.startsWith(serverConfig+".fa.")
                    && !portVariable.endsWith("-2") && !portVariable.endsWith(".orig")) {
                String value = idata.getVariable(portVariable);
                try {
                    if (!ports.add(PortUtils.getPort(value))) {
                        if (exclusions == null || !exclusions.contains(portVariable) || Integer.parseInt(value) != 0) {
                            return Status.ERROR;
                        }
                    }
                } catch (Exception e){}
            }
        }
        return Status.OK;
    }


    public boolean getDefaultAnswer() {
        return true;
    }

    public String getWarningMessageId() {
        return null;
    }

    public String getErrorMessageId() {
        return ERROR;
    }

    public String getFormattedMessage() {
        return message;
    }

    protected abstract String getConfig();

    protected ArrayList<String> getExclusions() {
        return null;
    }
}
