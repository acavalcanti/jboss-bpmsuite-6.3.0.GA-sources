package com.redhat.installer.asconfiguration.ldap.validator;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * Created by thauser on 9/29/14.
 */
public class BxmsLdapRoleValidator extends SearchDnValidator {

    private static final String CONTEXT = "ldap.businesscentral.role.context";
    private static final String FILTER = "ldap.businesscentral.role.filter";

    @Override
    public String getContext() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        return idata.getVariable(CONTEXT);
    }

    @Override
    public String getFilter() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        return idata.getVariable(FILTER);
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
