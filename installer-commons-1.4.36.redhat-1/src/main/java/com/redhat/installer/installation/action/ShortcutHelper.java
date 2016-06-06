package com.redhat.installer.installation.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Sets variables in idata associated with shortcuts.
 * @author Dustin Kut Moy Cheung
 */
public class ShortcutHelper implements PanelAction
{

    private final String STANDALONE_MANAGEMENT_NATIVE = "standalone.management-native-with.offset";
    private final String STANDALONE_MANAGEMENT_CONSOLE_HTTP = "standalone.management-http-with.offset";
    private final String STANDALONE_MANAGEMENT_CONSOLE_HTTPS = "standalone.management-https-with.offset";

    private final String SHORTCUT_STANDALONE_MANAGEMENT_NATIVE_PORT = "standalone.management.native.port";
    private final String SHORTCUT_STANDALONE_MANAGEMENT_CONSOLE_PORT = "standalone.management.console.port";

    //domain ports have also had the offset applied, they are not named as such because these variables needed to be set
    //befire the install panel for the patches
    private final String DOMAIN_MANAGEMENT_NATIVE = "domain.management-native";
    private final String DOMAIN_CONSOLE_HTTP = "domain.management-http";
    private final String DOMAIN_CONSOLE_HTTPS = "domain.management-https";

    private final String SHORTCUT_DOMAIN_MANAGEMENT_CONSOLE_PORT = "domain.management.console.port";
    private final String SHORTCUT_DOMAIN_MANAGEMENT_NATIVE_PORT = "domain.management.native.port";

    private final String SSL_ENABLED = "installSsl";
    private final String MODE = "url.protocol";

    /**
     * Entry point to run this action
     * @param handler Note, as per spec., if there isn't a GUI, handler is null
     */
    public void executeAction(final AutomatedInstallData idata, AbstractUIHandler handler)
    {
        int standaloneConsolePortNumber;
        int domainConsolePortNumber;
        int standaloneManagementNative = getNumber(getPortNumber(idata.getVariable(STANDALONE_MANAGEMENT_NATIVE)));
        int domainManagementNative = getNumber(getPortNumber(idata.getVariable(DOMAIN_MANAGEMENT_NATIVE)));


        if (idata.getVariable(SSL_ENABLED).equalsIgnoreCase("true")){
            standaloneConsolePortNumber = getNumber(getPortNumber(idata.getVariable(STANDALONE_MANAGEMENT_CONSOLE_HTTPS)));
            domainConsolePortNumber = getNumber(getPortNumber(idata.getVariable(DOMAIN_CONSOLE_HTTPS)));
            idata.setVariable(MODE, "https");
        }
        else {
            standaloneConsolePortNumber = getNumber(getPortNumber(idata.getVariable(STANDALONE_MANAGEMENT_CONSOLE_HTTP)));
            domainConsolePortNumber = getNumber(getPortNumber(idata.getVariable(DOMAIN_CONSOLE_HTTP)));
            idata.setVariable(MODE, "http");
        }

        idata.setVariable(SHORTCUT_STANDALONE_MANAGEMENT_NATIVE_PORT, "" + standaloneManagementNative);
        idata.setVariable(SHORTCUT_STANDALONE_MANAGEMENT_CONSOLE_PORT, "" + standaloneConsolePortNumber);

        idata.setVariable(SHORTCUT_DOMAIN_MANAGEMENT_NATIVE_PORT, "" +  domainManagementNative);
        idata.setVariable(SHORTCUT_DOMAIN_MANAGEMENT_CONSOLE_PORT, "" + domainConsolePortNumber);

    }

    /**
     * Parse a string that represents a number into an integer.
     * If we fail to parse the string into an integer return zero
     * @param number String representation of a number
     * @return Integer representation of the string passed in, return 0 if string cannot be parsed into an integer
     */
    private int getNumber(String number)
    {
        try
        {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * Retrieve port number (####) from string in format of ${some.port.reference:####}
     * If the string does not follow the ${<data>} pattern, then return the string passed in.
     * @param portData expected string of format ${some.port.reference:####}, or ####
     * @return string representation of the port (####)
     */
    private String getPortNumber(final String portData)
    {
        if (portData.startsWith("$") &&  portData.contains(":") &&  portData.endsWith("}"))
        {
            return portData.substring(portData.indexOf(":") + 1, portData.indexOf("}"));
        }
        else
        {
            return portData;
        }
    }

    /**
     * There shouldn't be any need for configuration, since this is a dedicated class
     * @param configuration null if no configuration block for the action
     */
    public void initialize(PanelActionConfiguration configuration) {
        // I am the last dinosaur on earth.
    }
}