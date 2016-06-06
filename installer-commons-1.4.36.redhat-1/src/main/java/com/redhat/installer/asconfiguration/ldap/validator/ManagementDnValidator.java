package com.redhat.installer.asconfiguration.ldap.validator;


import com.izforge.izpack.installer.AutomatedInstallData;

/** Validate that the ldap base DN is correct
 *        We split the two validators because of our LDAP Test button.
 */
public class ManagementDnValidator extends SearchDnValidator
{
    private static final String CONTEXT = "ldap.basedn";
    private static final String FILTER = "ldap.filter";
    private static final String FILTERTYPE = "ldap.filtertype";
    private static final String RECURSIVE = "ldap.recursive";

    @Override
    public String getContext() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        return idata.getVariable(CONTEXT);
    }

    @Override
    public String getFilter() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String type = idata.getVariable(FILTERTYPE);
        String filter = idata.getVariable(FILTER);
        String completeFilter = (type.equals("username")) ? "("+filter+"=*)" : filter;
        return completeFilter;
    }

    @Override
    public boolean isRecursive() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        return Boolean.parseBoolean(idata.getVariable(RECURSIVE));
    }
}
