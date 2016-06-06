package com.redhat.installer.asconfiguration.ldap.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.redhat.installer.asconfiguration.ldap.utils.LdapUtils;

import javax.naming.directory.DirContext;

/**
 * Created by thauser on 9/29/14.
 */
public abstract class SearchDnValidator implements Validator, DataValidator {

    private String message;
    private String warningId;

    public abstract String getContext();

    public abstract String getFilter();

    public abstract boolean isRecursive();

    @Override
    public Status validateData(AutomatedInstallData idata) {
        String ldapServerDn = idata.getVariable("ldap.dn");
        String ldapServerUrl = idata.getVariable("ldap.url");
        String ldapServerPassword = idata.getVariable("ldap.password");
        String searchDn = getContext();
        String searchFilter = getFilter();

        DirContext ctx = LdapUtils.makeConnection(ldapServerUrl, ldapServerDn, ldapServerPassword);
        if (ctx == null) {
            return Status.WARNING;
        }

        if (!LdapUtils.validateBaseDn(ctx, searchDn, searchFilter, isRecursive())) {
            return Status.WARNING;
        }
        return Status.OK;
    }

    private void setMessage(String string) {
        this.message = string;
    }

    @Override
    public String getErrorMessageId() {
        return null;
    }

    @Override
    public String getWarningMessageId() {
        return warningId;
    }

    private void setWarningMessageId(String warning) {
        this.warningId = warning;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return null;
    }

    @Override
    public boolean validate(ProcessingClient client) {
        Status status = validateData(AutomatedInstallData.getInstance());
        if (status == Status.OK)
            return true;
        else
            return false;
    }
}
