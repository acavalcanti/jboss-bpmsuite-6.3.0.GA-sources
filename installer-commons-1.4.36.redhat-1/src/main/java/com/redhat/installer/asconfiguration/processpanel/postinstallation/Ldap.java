package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

public class Ldap extends PostInstallation {

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        mHandler = handler;
        serverCommands = initServerCommands(Ldap.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installLdap();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    static List<ModelNode> installLdap() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String ldapName = idata.getVariable("ldap.name");
        String ldapUrl = idata.getVariable("ldap.url");
        String ldapDn = idata.getVariable("ldap.dn");
        String ldapPlainPwd = idata.getVariable("ldap.password");
        String ldapVaultedPwd = idata.getVariable("ldap.vaulted.password");
        String ldapPwd = (ldapVaultedPwd != null) ? ldapVaultedPwd : ldapPlainPwd;
        String ldapRealmName = idata.getVariable("ldap.realmname");
        String ldapBaseDn = idata.getVariable("ldap.basedn");
        String ldapRecursive = idata.getVariable("ldap.recursive");
        boolean ldapFilterType = idata.getVariable("ldap.filtertype") != null ? idata.getVariable("ldap.filtertype").equals("advanced") : false;
        String ldapFilter = idata.getVariable("ldap.filter");
        List<ModelNode> results = new ArrayList<ModelNode>();
        results.addAll(serverCommands.installLdap(ldapName, ldapPwd, ldapUrl, ldapDn,
                ldapRealmName, ldapBaseDn, ldapFilter, ldapRecursive,
                ldapFilterType));
        boolean installSsl = idata.getVariable("installSsl") != null ? Boolean.parseBoolean(idata.getVariable("installSsl")) : false;
        if (installSsl) {
            String keystore = idata.getVariable("ssl.path");
            String keystorePassword = idata.getVariable("ssl.password");
            results.addAll(serverCommands.installSsl(keystore, keystorePassword, ldapRealmName));
        }
        return results;
    }
}
