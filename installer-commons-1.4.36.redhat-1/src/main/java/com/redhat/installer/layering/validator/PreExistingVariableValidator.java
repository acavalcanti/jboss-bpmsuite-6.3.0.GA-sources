package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

/**
 * Superclass of specific name validators
 * Created by thauser on 2/18/14.
 */
public abstract class PreExistingVariableValidator implements Validator {

    @Override
    public boolean validate(ProcessingClient client){
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String preexistingNames = idata.getVariable(getPreexistingVar());
        for (int i = 0; i < client.getNumFields(); i++) {
            String name = client.getFieldContents(i);

            if (preexistingNames == null || preexistingNames.isEmpty()) {
                return true;
            }

            if (preexistingNames.contains(name)) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    // provides the validator with the variable to look at
    public abstract String getPreexistingVar();
}
