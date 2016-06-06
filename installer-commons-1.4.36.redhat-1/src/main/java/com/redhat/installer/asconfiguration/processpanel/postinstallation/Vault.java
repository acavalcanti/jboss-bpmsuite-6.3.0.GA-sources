package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.security.vault.VaultSession;
import org.jboss.dmr.ModelNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * These all must be static because of izpack's strange ProcessPanel requirements
 */
public abstract class Vault extends PostInstallation {
    protected static String resolvedVaultKeystoreUrl;
    protected static String resolvedVaultEncrDir;
    protected static String vaultKeystoreUrl;
    protected static String vaultEncrDir;
    protected static int vaultIterCount;
    protected static String vaultAlias;
    protected static String vaultEncrDirSubbed = "";
    protected static String vaultKeystoreUrlSubbed = "";

    protected static void initVaultSession() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String vaultKeystorePwd = idata.getVariable("vault.keystorepwd");
        String vaultSalt = idata.getVariable("vault.salt");

        if (!vaultEncrDir.endsWith(File.separator)) {
            vaultEncrDir += File.separator;
        }
        if (!resolvedVaultEncrDir.endsWith(File.separator)) {
            resolvedVaultEncrDir += File.separator;
        }

        // create a vault session with the information we gathered
        try {
            serverCommands.createVaultSession(resolvedVaultKeystoreUrl,
                    vaultKeystorePwd, resolvedVaultEncrDir, vaultSalt, vaultIterCount,
                    vaultAlias);
        } catch (Throwable e) {
            ProcessPanelHelper.printToPanel(PostInstallation.mHandler,
                    idata.langpack.getString("postinstall.processpanel.vault.failure"), true);
            e.printStackTrace();
        }
    }

    private static void modifyVaultValues(){
        /**
         * Substitute jboss home vars if they are referenced in the vault paths:
         */
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        String configDir = idata.getVariable("jboss.server.config.dir");
        String homeDir = idata.getVariable("jboss.home.dir");
        String defaultKeystoreUrl = vs.substitute(idata.getVariable("vault.keystoreloc.default"));
        String defaultEncrDir = vs.substitute(idata.getVariable("vault.encrdir.default"));

        if (serverCommands.isDomain()){
            vaultKeystoreUrl = resolvedVaultKeystoreUrl;
            vaultEncrDir = resolvedVaultEncrDir;
        }

        if (defaultKeystoreUrl != null) {
            if (vaultKeystoreUrl.equals(defaultKeystoreUrl)) {
                vaultKeystoreUrlSubbed = "${jboss.home.dir}/vault.keystore";
            } else {
                if (configDir != null) {
                    vaultKeystoreUrl = vaultKeystoreUrl.replace(configDir, "${jboss.server.config.dir}");
                }
                if (homeDir != null) {
                    vaultKeystoreUrl = vaultKeystoreUrl.replace(homeDir, "${jboss.home.dir}");
                }
                vaultKeystoreUrlSubbed = vaultKeystoreUrl;
            }
        }

        if (defaultEncrDir != null) {
            if (vaultEncrDir.equals(defaultEncrDir)) {
                vaultEncrDirSubbed = "${jboss.home.dir}/vault";
            } else {
                if (configDir != null) {
                    vaultEncrDir = vaultEncrDir.replace(configDir, "${jboss.server.config.dir}");
                }
                if (homeDir != null) {
                    vaultEncrDir = vaultEncrDir.replace(homeDir, "${jboss.home.dir}");
                }

                vaultEncrDirSubbed = vaultEncrDir;
            }
        }
        // begin hacks because of ldap stupidity
        /**
         * Explanation of the below: since LDAP subsystem ignores the directive to not try to resolve
         * VAULT expressions, it will always fail with our current scheme of doing things (vaulting passwords as needed).
         * To workaround this, we vault the password before we install it into the server. This means that the server
         * will not need a restart, and we can just use the VAULT expression which will be (erroneously) resolved correctly
         * by the LDAP subsystem.
         */
        boolean installLdap = idata.getRules().isConditionTrue("install.ldap");
        if (installLdap) {
            vaultLdapPassword();
        }
    }

    private static void vaultLdapPassword() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String plainLdapPwd = idata.getVariable("ldap.password");
        String ldapName = idata.getVariable("ldap.name");
        VaultSession vault = serverCommands.getVaultSession();
        try {
            if (idata.getVariable("ldap.vaulted.password") == null) {
                idata.setVariable("ldap.vaulted.password", "${" + vault.addSecuredAttribute("ldap", ldapName + ".password", plainLdapPwd.toCharArray()) + "}");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method simply tells the serverCommands instance to install the Vault
     * onto the server it is currently connected to, using the details
     * previously supplied by calling serverCommands.createVaultSession
     */
    protected static List<ModelNode> installVault() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        List<ModelNode> result = new ArrayList<ModelNode>();
        String preExist = idata.getVariable(PostInstallation.xmlDescriptor + ".vault.preexisting");
        boolean vaultPreExisting = preExist != null ? Boolean.parseBoolean(preExist) : false;
        if (!vaultPreExisting) {
            modifyVaultValues();
            if (!vaultKeystoreUrlSubbed.isEmpty() && !vaultEncrDirSubbed.isEmpty()) {
                result = serverCommands.installVault(vaultKeystoreUrlSubbed, vaultEncrDirSubbed);
            } else {
                result = serverCommands.installVault(vaultKeystoreUrl, vaultEncrDir);
            }
        }
        return result;
    }
}
