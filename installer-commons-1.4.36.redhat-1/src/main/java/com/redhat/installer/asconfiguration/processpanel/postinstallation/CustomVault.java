package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.util.InstallationUtilities;
import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * ProcessPanel task that initializes a custom vault
 */
public class CustomVault extends Vault {
    private static final String DESCRIPTOR = "xml-file";
    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        mHandler = handler;
        serverCommands = initServerCommands(CustomVault.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            return false;
        }
        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);
        if (parser.hasProperty(DESCRIPTOR)){
            PostInstallation.xmlDescriptor = parser.getStringProperty(DESCRIPTOR);
        }
        initializeCustomVault();
        initVaultSession();
        List<ModelNode> commandResults = installVault();
        serverCommands.terminateSession();

        if (commandResults != null) {
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    private static void initializeCustomVault() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        vaultKeystoreUrl = idata.getVariable("vault.keystoreloc");
        vaultEncrDir = idata.getVariable("vault.encrdir");
        resolvedVaultKeystoreUrl = vs.deepSubstitute(vaultKeystoreUrl, null);
        resolvedVaultEncrDir = vs.deepSubstitute(vaultEncrDir, null);
        vaultIterCount = Integer.parseInt(idata.getVariable("vault.itercount"));
        vaultAlias = vs.substitute(idata.getVariable("vault.alias"));
        InstallationUtilities.addFileToCleanupList(vaultEncrDir + "/VAULT.dat");
        InstallationUtilities.addFileToCleanupList(vaultEncrDir);
    }
}
