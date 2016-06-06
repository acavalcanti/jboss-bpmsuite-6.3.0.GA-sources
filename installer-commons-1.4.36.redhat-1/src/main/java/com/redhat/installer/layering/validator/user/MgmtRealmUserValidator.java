package com.redhat.installer.layering.validator.user;

import com.izforge.izpack.installer.AutomatedInstallData;
/**
 * Created by mtjandra on 2/19/14.
 */
public class MgmtRealmUserValidator extends DuplicateUserValidator {

    protected String getFileName() {
        return "mgmt-users.properties";
    }

    //Variable that holds the username
    protected String getUserVar() {
        return "adminUser";
    }

    //Variable that holdes the username
    protected String getCondVar() {
        return "addUser";
    }

    @Override
    protected void doExtraWork() {

       AutomatedInstallData idata = AutomatedInstallData.getInstance();
       String addUserRadio = idata.getVariable(getCondVar());

       if ((addUserRadio != null) && addUserRadio.equals("false"))
           idata.setVariable("add.new.user", "false");
       else
           idata.setVariable("add.new.user", "true");
    }

    protected Status getConflictStatus() {
        return Status.SKIP;
    }

}
