package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates only the ldap connection. Can be run as a separate step as desired in the ProcessPanel.
 *
 *
 * Created by thauser on 10/29/14.
 */
public class LdapConnection extends PostInstallation{

    public static boolean run(AbstractUIProcessHandler handler, String[] args){
        mHandler = handler;
        serverCommands = initServerCommands(LdapConnection.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installLdapConnection();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    protected static List<ModelNode> installLdapConnection() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String ldapName = idata.getVariable("ldap.name");
        String ldapUrl = idata.getVariable("ldap.url");
        String ldapDn = idata.getVariable("ldap.dn");
        String ldapPlainPwd = idata.getVariable("ldap.password");
        String ldapVaultedPwd = idata.getVariable("ldap.vaulted.password");
        String ldapPwd = (ldapVaultedPwd != null ) ? ldapVaultedPwd : ldapPlainPwd;
        List<ModelNode> results = new ArrayList<ModelNode>();
        results.addAll(serverCommands.createLdapConn(ldapName, ldapPwd, ldapUrl, ldapDn));
        return results;
    }
}
