package com.redhat.installer.ports.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.redhat.installer.ports.utils.PortUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This actions applies the port offsets.
 * Should only be applies when necessary (During installation, so after the summary panel)
 *
 * @author Alex Szczuczko <aszczucz@redhat.com>, Tom Hauser <thauser@redhat.com>
 */
public class OffsetVariableBridgeAction implements PanelAction {
    private AutomatedInstallData idata;

    private int domainManagementNativeNumber;
    private int domainHttpConsoleNumber;
    private int domainSSLConsoleNumber;

    private int standaloneManagementNativeNumber;
    private int standaloneHttpConsoleNumber;
    private int standaloneSSLConsoleNumber;

    private final String STANDALONE_NATIVE = "standalone.management-native";
    private final String STANDALONE_NATIVE_PROPERTY = "standalone.management-native-1";

    private final String STANDALONE_HTTP = "standalone.management-http";
    private final String STANDALONE_HTTP_PROPERTY = "standalone.management-http-1";

    private final String STANDALONE_HTTPS = "standalone.management-https";
    private final String STANDALONE_HTTPS_PROPERTY = "standalone.management-https-1";


    private final String DOMAIN_NATIVE = "domain.management-native";
    private final String DOMAIN_NATIVE_PROPERTY = "domain.management-native-1";
    
    private final String DOMAIN_HTTP = "domain.management-http";
    private final String DOMAIN_HTTP_PROPERTY = "domain.management-http-1";

    private final String DOMAIN_HTTPS = "domain.management-https";
    private final String DOMAIN_HTTPS_PROPERTY = "domain.management-https-1";

    private String[] standalonePortVariables = new String[]{STANDALONE_NATIVE, STANDALONE_NATIVE_PROPERTY,
                                                            STANDALONE_HTTP,   STANDALONE_HTTP_PROPERTY,
                                                            STANDALONE_HTTPS,  STANDALONE_HTTPS_PROPERTY};

    private String[] domainPortVariables = new String[]{DOMAIN_NATIVE, DOMAIN_NATIVE_PROPERTY,
                                                        DOMAIN_HTTP,   DOMAIN_HTTP_PROPERTY,
                                                        DOMAIN_HTTPS,  DOMAIN_HTTPS_PROPERTY};

    public void executeAction(final AutomatedInstallData idata, AbstractUIHandler handler) {
        this.idata = idata;
        int portOffset = getPortOffset();
        try {
            checkForInvalidVariables();
        } catch (InvalidVariablesException e){
            Debug.trace(e.getMessage());
        }
        setStandalonePortValues();
        setDomainPortValues();
        applyStandalonePortOffsets(portOffset);
        applyDomainPortOffsets(portOffset);
        configureShortcutOffsets(portOffset);
    }

    private void checkForInvalidVariables() throws InvalidVariablesException {
        List<String> invalidVariables = new ArrayList<String>();
        invalidVariables.addAll(findNonNumberVariables(standalonePortVariables));
        invalidVariables.addAll(findNonNumberVariables(domainPortVariables));
        if (!invalidVariables.isEmpty()){
            StringBuilder message = new StringBuilder();
            message.append("The following port variables contain invalid values: ");
            for (String variable : invalidVariables){
                message.append(variable);
                message.append(", ");
            }
            throw new InvalidVariablesException(message.toString());
        }
    }

    /**
     * Configure Additional Standalone Port Offsets.-shortcut is appended to these variables because we don't want them substituted into the standalone*.xml files
     * These Variables are used in ShortcutHelper.java to set the variables for shortcuts
     */
    private void configureShortcutOffsets(int portOffset) {
        idata.setVariable("standalone.management-native-with.offset", "${" + idata.getVariable(STANDALONE_NATIVE_PROPERTY) + ":" + (standaloneManagementNativeNumber + portOffset) + "}");
        idata.setVariable("standalone.management-http-with.offset", "${" + idata.getVariable(STANDALONE_HTTP_PROPERTY) + ":" + (standaloneHttpConsoleNumber + portOffset) + "}");
        idata.setVariable("standalone.management-https-with.offset", "${" + idata.getVariable(STANDALONE_HTTPS_PROPERTY) + ":" + (standaloneSSLConsoleNumber + portOffset) + "}");
    }

    private void applyDomainPortOffsets(int portOffset) {
        idata.setVariable("domain.port-offset", portOffset + "");
        idata.setVariable("domain.port-offset150", portOffset + 150 + "");
        idata.setVariable("domain.port-offset250", portOffset + 250 + "");
        idata.setVariable("domain.management-native", "${" + idata.getVariable(DOMAIN_NATIVE_PROPERTY) + ":" + (domainManagementNativeNumber + portOffset) + "}");
        idata.setVariable("domain.management-http", "${" + idata.getVariable(DOMAIN_HTTP_PROPERTY) + ":" + (domainHttpConsoleNumber + portOffset) + "}");
        idata.setVariable("domain.management-https", "${" + idata.getVariable(DOMAIN_HTTPS_PROPERTY) + ":" + (domainSSLConsoleNumber + portOffset) + "}");
        //Name for for master.domain.port should never change, port value can only be effected by the portOffset BZ1088844
        idata.setVariable("master.domain.port", "${jboss.domain.master.port:" + (9999 + portOffset) + "}");
    }

    private void applyStandalonePortOffsets(int portOffset) {
        idata.setVariable("standalone.port-offset", "${jboss.socket.binding.port-offset:" + portOffset + "}");
    }

    public void initialize(PanelActionConfiguration configuration) {
        //Dedicated class no need for configuration
    }


    private int getPortOffset() {
        String selectedPortOffsetDecision = idata.getVariable("portDecision");
        String selectedPortOffsetType = idata.getVariable("portOffsetType");
        String specificPortOffset = idata.getVariable("configurePortOffset");

        int portOffset = 0;

        if (selectedPortOffsetDecision != null && selectedPortOffsetDecision.equals("assist")) {
            try {
                if (selectedPortOffsetType.equals("specify") && !specificPortOffset.isEmpty()) {
                    portOffset = Integer.valueOf(specificPortOffset);
                } else {
                    portOffset = Integer.valueOf(selectedPortOffsetType);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return portOffset;
    }

    private List<String> findNonNumberVariables(String[] variables) {
        List<String> nonNumbers = new ArrayList<String>();
        for (String var : variables) {
            String currentVariable = idata.getVariable(var);
            if (currentVariable == null){
                nonNumbers.add(var);
            } else {
                try {
                    Integer.parseInt(currentVariable);
                } catch (NumberFormatException nfe){
                    nonNumbers.add(var);
                }
            }
        }
        return nonNumbers;
    }

    private void setStandalonePortValues(){
        this.standaloneManagementNativeNumber = PortUtils.getPort(idata.getVariable(STANDALONE_NATIVE));
        this.standaloneHttpConsoleNumber = PortUtils.getPort(idata.getVariable(STANDALONE_HTTP));
        this.standaloneSSLConsoleNumber = PortUtils.getPort(idata.getVariable(STANDALONE_HTTPS));
    }

    private void setDomainPortValues(){
        this.domainManagementNativeNumber = PortUtils.getPort(idata.getVariable(DOMAIN_NATIVE));
        this.domainHttpConsoleNumber = PortUtils.getPort(idata.getVariable(DOMAIN_HTTP));
        this.domainSSLConsoleNumber = PortUtils.getPort(idata.getVariable(DOMAIN_HTTPS));
    }

    private class InvalidVariablesException extends Exception {
        public InvalidVariablesException(String message){
            super(message);
        }

    }
}