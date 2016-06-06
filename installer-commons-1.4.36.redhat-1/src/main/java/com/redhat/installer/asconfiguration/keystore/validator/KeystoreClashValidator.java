package com.redhat.installer.asconfiguration.keystore.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.keystore.processpanel.ClientKeystoreBuilder;

import java.io.File;

/**
 * Class that checks the installation for conflicting keystores in the JMS keystore generation
 * panel
 * Created by thauser on 6/13/14.
 */
public class KeystoreClashValidator implements DataValidator {

    private final String CONDITION = "generate.client.keystores";
    private final String SERVER_LOC_VAR = "generated.keystores.server.location";
    private final String CLIENT_LOC_VAR = "generated.keystores.client.location";
    private final String CLIENT_NUM_VAR = "generated.keystores.client.number";

    private String error;
    private String message;

    @Override
    public Status validateData(AutomatedInstallData idata) {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        // check for the server keystore first
        String serverStorePath = vs.substitute(idata.getVariable(SERVER_LOC_VAR));

        // if the value in the variable exists, check if the path exists; if it does, we abort because server keystore generation will fail.
        if (serverStorePath != null) {
            File checkServer = new File(serverStorePath);
            if (checkServer.exists()){
                setError("KeystoreClashValidator.generated.server.keystore.path.exists");
                setMessage(String.format(idata.langpack.getString(getErrorMessageId()), checkServer.getAbsolutePath()));
                return Status.ERROR;
            }
        }

        // if the condition isn't true, we aren't generating the client keystores. so we don't need to check clashes for them
        if (idata.getRules().isConditionTrue(CONDITION)) {
            int numGeneratedClients = Integer.parseInt(idata.getVariable(CLIENT_NUM_VAR));
            String clientBasePath = vs.substitute(idata.getVariable(CLIENT_LOC_VAR));

            // check if the path to the parent folder we use is a file, and fail then also
            File checkDir = new File(clientBasePath);
            if (checkDir.isFile()){
                setError("KeystoreClashValidator.generated.client.keystore.path.isfile");
                setMessage(String.format(idata.langpack.getString(getErrorMessageId()), checkDir.getAbsolutePath()));
                return Status.ERROR;
            }

            if(checkDir.exists() && checkDir.listFiles().length > 0){
                setError("KeystoreClashValidator.generated.client.keystore.path.non.empty");
                setMessage(String.format(idata.langpack.getString(getErrorMessageId()),checkDir.getAbsoluteFile()));
                return Status.ERROR;
            }

            for (int i = 0; i < numGeneratedClients; i++) {
                File checkFile = new File(clientBasePath + File.separator + String.format(ClientKeystoreBuilder.getClientKeystoreTemplate(), i));
                if (checkFile.exists()) {
                    // failure; the user somehow has a keystore already in existence that will clash with the client keystore generation.
                    setError("KeystoreClashValidator.generated.client.keystore.clash");
                    setMessage(String.format(idata.langpack.getString(getErrorMessageId()), checkFile.getAbsolutePath()));
                    return Status.ERROR;
                }
            }
        }
        return Status.OK;
    }


    private void setError(String s){
        this.error = s;
    }

    private void setMessage(String s){
        this.message = s;
    }

    @Override
    public String getErrorMessageId() {
        return error;
    }

    @Override
    public String getWarningMessageId() {
        return error;
    }

    /**
     * Make sure that automatic installations which fail validation are stopped.
     * This only applies when Status.WARNING is thrown.
      * @return
     */
    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }
}
