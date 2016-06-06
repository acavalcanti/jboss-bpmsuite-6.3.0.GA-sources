package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.util.InstallationUtilities;
import org.jboss.dmr.ModelNode;

import java.util.List;

public class DefaultVault extends Vault {
    private static final String DESCRIPTOR = "xml-file";

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        try {
            initPostInstallation(handler, args, DefaultVault.class);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        initializeDefaultVault();
        initVaultSession();
        List<ModelNode> commandResults = installVault();
        serverCommands.terminateSession();

        return installResult(commandResults);
    }

    static void initializeDefaultVault() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        vaultKeystoreUrl = vs.substitute(idata.getVariable("vault.keystoreloc.default"));
        vaultEncrDir = vs.substitute(idata.getVariable("vault.encrdir.default"));
        resolvedVaultKeystoreUrl = vaultKeystoreUrl;
        resolvedVaultEncrDir = vaultEncrDir;
        vaultIterCount = Integer.parseInt(idata.getVariable("vault.itercount.default"));
        vaultAlias = vs.substitute(idata.getVariable("vault.alias.default"));
        InstallationUtilities.addFileToCleanupList(vaultEncrDir + "/VAULT.dat");
        InstallationUtilities.addFileToCleanupList(vaultEncrDir);
    }
}
