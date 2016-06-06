package com.redhat.installer.asconfiguration.ssl.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.asconfiguration.keystore.validator.KeystoreValidator;

public class SSLKeystoreValidator extends KeystoreValidator
{
    @Override
    protected Status performAdditionalChecksOnSuccess(String algorithm) {
        return Status.OK;
    }

    @Override
    protected boolean hasAdditionalChecksOnSuccess() {
        return false;
    }

    @Override
    public String getKeystorePath() {
        return AutomatedInstallData.getInstance().getVariable("ssl.path");
    }

    @Override
    public String getEncryptedDirPath() {
        return null;
    }

    @Override
    public String[] getSupportedFormats() {
        return AutomatedInstallData.getInstance().getVariable("ssl.allowed.keystore.types").split(",");
    }

    @Override
    public char[] getKeystorePassword() {
        return AutomatedInstallData.getInstance().getVariable("ssl.password").toCharArray();
    }

    @Override
    public boolean hasAdditionalChecksOnFail() {
        return false;
    }

    @Override
    public Status performAdditionalChecksOnFail(int result) {
        return Status.OK;
    }

    @Override
    public void setVariable() {
        return;
    }

    @Override
    public Status getFailureStatus() {
        return Status.WARNING;
    }

    @Override
    protected boolean getCondition() {
        return true;
    }
}
