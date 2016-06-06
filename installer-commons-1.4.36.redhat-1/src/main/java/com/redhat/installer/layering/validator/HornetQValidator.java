package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple validator to ensure that the target installation does not contain a hornetq-server that will cause RTGov / BPMS .cli commands to fail, nor one that
 * is configured incorrectly for these products (they use identical configuration)
 * Only needed in the BPMS / FSW installers
 * Created by thauser on 2/27/14.
 */
public class HornetQValidator implements DataValidator {
    AutomatedInstallData idata;
    private String error = "";
    private String message;
    // monstrous query that ensures all required options exist in the configured hornetq-server element
    private final String standaloneHornetQQuery = "subsystem > hornetq-server:not([name])";
    //TODO: is this right? by default, domain.xml contains hornetq-server elements in full and full-ha profiles; we ignore these, because no .cli script adds to them
    private final String domainHornetQQuery = "profile:not([name=full]):not([name=full-ha]) > subsystem > hornetq-server:not([name])";
    private final String requiredHornetQConfigQuery = ":has(persistence-enabled:containsOwn(true)):has(journal-type:containsOwn(NIO)):has(journal-min-files:containsOwn(2)):has(connectors:has(netty-connector[name=netty][socket-binding=messaging])):has(connectors:has(netty-connector[name=netty-throughput][socket-binding=messaging-throughput]):has(param[key=batch-delay][value=50])):has(connectors:has(in-vm-connector[name=in-vm][server-id=0])):has(acceptors > netty-acceptor[name=netty][socket-binding=messaging]):has(acceptors:has(netty-acceptor[name=netty-throughput][socket-binding=messaging-throughput]):has(param[key=batch-delay][value=50])):has(param[key=direct-deliver][value=false]):has(acceptors:has(in-vm-acceptor[name=in-vm][server-id=0])):has(security-settings:has(security-setting[match=#]:has(permission[type=send][roles=guest]):has(permission[type=consume][roles=guest]):has(permission[type=createNonDurableQueue][roles=guest]):has(permission[type=deleteNonDurableQueue][roles=guest]))):has(address-settings:has(address-setting[match=#]:has(dead-letter-address:containsOwn(jms.queue.DLQ)):has(expiry-address:containsOwn(jms.queue.ExpiryQueue)):has(redelivery-delay:containsOwn(0)):has(max-size-bytes:contains(10485760)):has(message-counter-history-day-limit:contains(10)))):has(jms-connection-factories:has(connection-factory[name=InVmConnectionFactory]:has(connectors:has(connector-ref[connector-name=in-vm])):has(entries:has(entry[name=java:/ConnectionFactory]))):has(connection-factory[name=RemoteConnectionFactory]:has(connectors:has(connector-ref[connector-name=netty])):has(entries:has(entry[name=java:jboss/exported/jms/RemoteConnectionFactory]))):has(pooled-connection-factory[name=hornetq-ra]:has(transaction[mode=xa]):has(connectors:has(connector-ref[connector-name=in-vm])):has(entries:has(entry[name=java:/JmsXA]))))";


    // this method will be run after the EapExistsValidator and IsSupportedPlatformValidator, so we know that the descriptors will exist already.
    // the reason these methods don't reside within the SetPreExistingDefaults is because they require user feedback, which actions don't provide.
    @Override
    public Status validateData(AutomatedInstallData idata) {
        this.idata = idata;
        // TODO: localize
        String warningMessage = "The following descriptors already contain hornetq-server configuration: %s. Errors could occur. Would you like to continue?";
        String errorMessage = "The following descriptors contain incompatible hornetq-server configuration: %s. Please choose a different installation path.";
        boolean eapNeedsInstall = Boolean.parseBoolean(idata.getVariable("eap.needs.install"));
        List<String> hornetQWarnings = new ArrayList<String>();
        List<String> hornetQErrors = new ArrayList<String>();

        if (eapNeedsInstall){
            return Status.OK; // all good
        } else {
            // find where hornetq is defined
            String basePath = idata.getVariable("INSTALL_PATH");
            for (String descriptor : PreExistingConfigurationConstants.standaloneDescriptors){
                String descPath = basePath +  "/standalone/configuration/" + descriptor;
                File descFile = new File(descPath);
                if (descFile.exists()){
                    switch (validateHornetQ(descriptor, descFile, standaloneHornetQQuery)){
                        case 0:
                            // no default hornetq-server element
                            break;
                        case 1:
                            // default hornetq-server that contains at least all required config
                            hornetQWarnings.add(descriptor);
                            break;
                        case 2:
                            hornetQErrors.add(descriptor);
                            // default hornetq-server that doesn't contain all required config
                            break;
                    }
                }
            }
            for (String descriptor : PreExistingConfigurationConstants.domainDescriptors){
                String descPath = basePath + "/domain/configuration/" + descriptor;
                File descFile = new File(descPath);
                if (descFile.exists()){
                    switch (validateHornetQ(descriptor, descFile, domainHornetQQuery)){
                        case 0:
                            break;
                        case 1:
                            // default hornetq-server that contains at least all required config
                            hornetQWarnings.add(descriptor);
                            break;
                        case 2:
                            // default hornetq-server that doesn't contain all required config
                            hornetQErrors.add(descriptor);
                            break;
                    }
                }
            }

            if (!hornetQErrors.isEmpty()){
                // errors detected, enumerate the descriptors in which they are found, and return an error.
                String errorDescs = "";
                for (String desc : hornetQErrors){
                    errorDescs += desc + " ";
                }
                setMessage(String.format(errorMessage,errorDescs.trim()));
                return Status.ERROR;
            } else if (!hornetQWarnings.isEmpty()){
                // warn the user about their pre-existing configuration. there may be unforeseeable problems with configurations like this
                String warningDescs = "";
                for (String desc : hornetQWarnings){
                    warningDescs += desc + " ";
                }
                setMessage(String.format(warningMessage,warningDescs.trim()));
                return Status.WARNING;
            }

        }
        return Status.OK;



    }

    private int validateHornetQ(String descriptor, File descFile, String baseQuery) {
        int status = 0;
        try{
            Document doc = Jsoup.parse(descFile, "UTF-8", "");
            // check for a default hornetq-server element
            Elements defaultHornetQServer = doc.select(baseQuery);

            // a default hornetq-server exists. further checks
            if (!defaultHornetQServer.isEmpty()){
                // this will also match with hornetq-servers that have more than the required config.
                Elements withRequiredConfig = defaultHornetQServer.select(requiredHornetQConfigQuery);
                if (!withRequiredConfig.isEmpty()){
                    idata.setVariable(descriptor+".hornetq.exists", "true");
                    return 1;
                } else {
                    // the default hornetq-server does NOT contain all required configuration. the installation cannot continue
                    return 2;
                }
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
        return 0;
    }

    private void setError(String s){
        error = s;
    }

    private void setMessage(String s) {
        message = s;
    }

    @Override
    public String getErrorMessageId() {
        return error;
    }

    @Override
    public String getWarningMessageId() {
        return error;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }
}
