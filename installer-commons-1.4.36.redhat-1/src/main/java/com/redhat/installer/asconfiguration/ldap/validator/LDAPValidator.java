package com.redhat.installer.asconfiguration.ldap.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.redhat.installer.asconfiguration.ldap.utils.LdapUtils;

import javax.naming.directory.DirContext;

/** Verify that we can authenticate with the LDAP server. */
public class LDAPValidator implements DataValidator, Validator {

    String warning;
    String message;

    public Status validateData(AutomatedInstallData adata) {
        setWarningMessageId("Ldap.error");
        message = adata.langpack.getString(warning);
        String dnServer = adata.getVariable("ldap.url");
        String dn = adata.getVariable("ldap.dn");
        String dnPassword = adata.getVariable("ldap.password");

        DirContext ctx = LdapUtils.makeConnection(dnServer, dn, dnPassword);
        if (ctx == null) return Status.WARNING;
        return Status.OK;
    }

    //Used through test button of userInputPanel
    public boolean validate(ProcessingClient client)
    {
        Status status =  validateData(AutomatedInstallData.getInstance());
        if (status == Status.OK)
            return true;
        else
            return false;
    }
    public String getErrorMessageId() {
        return null;
    }

    public String getWarningMessageId() {
        return warning;
    }

    private void setWarningMessageId(String warning) {
      this.warning = warning;
    }

    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }
}