package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thauser on 10/29/14.
 */
public class LdapSecurityRealm extends PostInstallation {

    public static boolean run(AbstractUIProcessHandler handler, String[] args){
        mHandler = handler;
        serverCommands = initServerCommands(LdapSecurityRealm.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installLdapSecurityRealm();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    protected static List<ModelNode> installLdapSecurityRealm(){
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String ldapName = idata.getVariable("ldap.name");
        String ldapRealmName = idata.getVariable("ldap.realmname");
        String ldapBaseDn = idata.getVariable("ldap.basedn");
        String ldapRecursive = idata.getVariable("ldap.recursive");
        boolean ldapFilterType = idata.getVariable("ldap.filtertype") != null ? idata.getVariable("ldap.filtertype").equals("advanced") : false;
        String ldapFilter = idata.getVariable("ldap.filter");
        List<ModelNode> results = new ArrayList<ModelNode>();
        results.addAll(serverCommands.createLdapSecurityRealm(ldapName, ldapRealmName, ldapBaseDn, ldapFilter, ldapRecursive, ldapFilterType));

        // add ssl onto the realm if it's also being installed
        boolean installSsl = idata.getVariable("installSsl") != null ? Boolean.parseBoolean(idata.getVariable("installSsl")) : false;
        if (installSsl){
            String keystore = idata.getVariable("ssl.path");
            String keystorePassword = idata.getVariable("ssl.password");
            results.addAll(serverCommands.installSsl(keystore, keystorePassword, ldapRealmName));
        }
        results.addAll(serverCommands.installLdapToInterfaces(ldapRealmName));
        return results;
    }

}
