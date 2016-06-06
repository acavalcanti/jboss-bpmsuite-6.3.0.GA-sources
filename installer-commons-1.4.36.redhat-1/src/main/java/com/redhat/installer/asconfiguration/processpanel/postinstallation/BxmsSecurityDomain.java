package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ProcessPanel job implementing the steps required to get LDAP working with the BRMS / BPMS business-central and dashboard-builder
 * applications: https://mojo.redhat.com/docs/DOC-977872
 * Created by thauser on 9/24/14.
 */
public class BxmsSecurityDomain extends PostInstallation {

    public static boolean run(AbstractUIProcessHandler handler, String[] args){
        mHandler = handler;
        serverCommands = initServerCommands(BxmsSecurityDomain.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }


        List<ModelNode> commandResults = installBusinessCentralLdap();
        serverCommands.terminateSession();
        
        return installResult(commandResults);
    }

    /**
     * The method utilizes values from the LDAP connection panel. The values must exist or this job will
     * not execute
     */
    private static List<ModelNode> installBusinessCentralLdap() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        List<ModelNode> commandResults = new ArrayList<ModelNode>();

        String secdomName = "business-central-ldap";
        String cacheType = "default";
        String authenCode = "LdapExtended";
        String flag = "Required";
        String ldapUrl = idata.getVariable("ldap.url");
        String ldapDn = idata.getVariable("ldap.dn");
        String ldapPlainPwd = idata.getVariable("ldap.password");
        String ldapVaultedPwd = idata.getVariable("ldap.vaulted.password");
        String ldapPwd = (ldapVaultedPwd != null) ? ldapVaultedPwd : ldapPlainPwd;
        String ldapBrmsUserContext = idata.getVariable("ldap.businesscentral.user.context");
        String ldapBrmsUserFilter = idata.getVariable("ldap.businesscentral.user.filter");
        String ldapBrmsUserRolesFilter = idata.getVariable("ldap.businesscentral.user.roles.filter");
        String ldapBrmsRoleContext = idata.getVariable("ldap.businesscentral.role.context");
        String ldapBrmsRoleFilter = idata.getVariable("ldap.businesscentral.role.filter");
        String ldapBrmsRoleAttribute = idata.getVariable("ldap.businesscentral.role.attributeid");
        String ldapBrmsRoleName = idata.getVariable("ldap.businesscentral.role.nameid");
        String ldapBrmsRoleAttributeIsDn = idata.getVariable("ldap.businesscentral.role.attr.dn");

        Map<String,String> ldapBrmsAuthenOptions = new HashMap<String,String>();

        ldapBrmsAuthenOptions.put("java.naming.provider.url",ldapUrl);
        ldapBrmsAuthenOptions.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        ldapBrmsAuthenOptions.put("java.naming.referral", "follow");
        ldapBrmsAuthenOptions.put("java.naming.security.authentication", "simple");
        ldapBrmsAuthenOptions.put("bindDN",ldapDn);
        ldapBrmsAuthenOptions.put("bindCredential",ldapPwd);
        ldapBrmsAuthenOptions.put("baseCtxDN", ldapBrmsUserContext);
        ldapBrmsAuthenOptions.put("baseFilter", ldapBrmsUserFilter);
        ldapBrmsAuthenOptions.put("rolesCtxDN", ldapBrmsRoleContext);
        ldapBrmsAuthenOptions.put("roleFilter", ldapBrmsUserRolesFilter);
        ldapBrmsAuthenOptions.put("roleAttributeID", ldapBrmsRoleAttribute);
        ldapBrmsAuthenOptions.put("roleNameAttributeID", ldapBrmsRoleName);
        ldapBrmsAuthenOptions.put("roleRecursion", "2");
        ldapBrmsAuthenOptions.put("roleAttributeIsDN", ldapBrmsRoleAttributeIsDn);
        ldapBrmsAuthenOptions.put("parseRoleNameFromDN", "false");
        ldapBrmsAuthenOptions.put("searchScope", "SUBTREE_SCOPE");
        ldapBrmsAuthenOptions.put("throwValidateError", "true");

        // prepare values for passing to serverCommands
        List<String> authenCodes = new ArrayList<String>();
        authenCodes.add(authenCode);
        List<String> authenFlags = new ArrayList<String>();
        authenFlags.add(flag);
        List<Map<String,String>> authenOptions = new ArrayList<Map<String,String>>();
        authenOptions.add(ldapBrmsAuthenOptions);


        // add the securitydomain required for BRMS ldap
        commandResults.addAll(serverCommands.addSecurityDomainAuthenOnly(secdomName, cacheType, authenCodes, authenFlags, authenOptions));
        // add and enable security domain on the messaging subsystem if we're installing bpms
        if (idata.getVariable("new.product.conf")!=null && idata.getVariable("new.product.conf").equalsIgnoreCase("bpms")) {
            commandResults.add(serverCommands.submitCommand("/subsystem=messaging/hornetq-server=default:write-attribute(name=security-enabled,value=true"));
            commandResults.add(serverCommands.submitCommand("/subsystem=messaging/hornetq-server=default:write-attribute(name=security-domain,value=" + secdomName));
        }

        return commandResults;
    }
}
