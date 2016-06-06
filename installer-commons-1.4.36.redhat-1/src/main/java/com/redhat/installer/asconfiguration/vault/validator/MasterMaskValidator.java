package com.redhat.installer.asconfiguration.vault.validator;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * Created by thauser on 2/3/15.
 */
public class MasterMaskValidator extends VaultMaskValidator {

    @Override
    public String getPassword() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String returnValue = null;
        if (idata.getRules().isConditionTrue("use.same.password")){
            String masterPasswordVariable = idata.getVariable("master.password.var");
            returnValue = idata.getVariable(masterPasswordVariable);
        }
        return returnValue;
    }
}
