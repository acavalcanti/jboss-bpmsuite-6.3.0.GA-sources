package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.asconfiguration.keystore.validator.KeystoreValidator;
import com.redhat.installer.asconfiguration.vault.validator.VaultValidator;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PreExistingVaultValidator extends VaultValidator {
    @Override
    public String getKeystorePath() {
        String path = AutomatedInstallData.getInstance().getVariable("vault.resolved.keystoreloc");
        if(path.contains("${jboss.home.dir}")) {
            path = path.replace("${jboss.home.dir}", AutomatedInstallData.getInstance().getInstallPath());
        }
        return path;
    }

    @Override
    public String getEncryptedDirPath() {
        return null;
    }

    @Override
    public String[] getSupportedFormats() {
        return AutomatedInstallData.getInstance().getVariable("preexisting.vault.allowed.keystore.types").split(",");
    }

    @Override
    public boolean hasAdditionalChecksOnFail() {
        return true;
    }

    /**
     * This method attempts to check default locations if the user used a default keystore,
     * since querying the server for jboss.home.dir (the default location) isn't possible at the
     * time we need to check
     *
     * @param result
     * @return
     */
    @Override
    public Status performAdditionalChecksOnFail(int result) {

        String keystoreLoc = AutomatedInstallData.getInstance().getInstallPath() + File.separator + "vault.keystore";
        char[] pwd = getKeystorePassword();
        int newResult = isValidKeystore(keystoreLoc, pwd, getSupportedFormats());

        /**
         * We just check the isValidKeystore again, attempting to use the default installer location of the vault.
         * If this fails, we assume the first check was correct, and use its message and its error Id, and simply return a warning.
         */
        if (newResult == 0) {
            setVariable();
            return Status.OK;
        } else {
            return Status.WARNING;
        }
    }

    @Override
    public void setVariable() {
        super.setVariable();
        AutomatedInstallData.getInstance().setVariable("installVault", "true");
    }

    @Override
    public Status getFailureStatus() {
        return Status.ERROR;
    }

    @Override
    protected boolean getCondition() {
        return true;
    }
}
