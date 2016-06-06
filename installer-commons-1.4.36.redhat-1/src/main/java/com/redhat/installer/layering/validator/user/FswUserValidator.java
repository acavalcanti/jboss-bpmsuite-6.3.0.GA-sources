package com.redhat.installer.layering.validator.user;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * Created by thauser on 2/19/14.
 */
public class FswUserValidator extends ApplicationRealmUserValidator {

    @Override
    protected String getUserVar() {
        return "fsw.user";
    }

    @Override
    protected String getCondVar(){
        return "add.fsw.user";
    }

    @Override
    protected void doExtraWork(){
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String addUserRadio = idata.getVariable(getCondVar());
        if ((addUserRadio != null) && addUserRadio.equals("false")){
            idata.setVariable("add.new.fsw.user", "false");
        } else {
            idata.setVariable("add.new.fsw.user", "true");
        }
    }
}
