package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.asconfiguration.keystore.validator.KeystoreValidator;

/**
 * Created by thauser on 3/4/14.
 */
public class PreExistingSAMLKeystoreValidator extends KeystoreValidator
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
        return AutomatedInstallData.getInstance().getInstallPath() + "/standalone/configuration/overlord-saml.keystore";
    }

    @Override
    public String getEncryptedDirPath() {
        return null;
    }

    @Override
    public String[] getSupportedFormats() {
        return AutomatedInstallData.getInstance().getVariable("preexisting.saml.allowed.keystore.types").split(",");
    }

    @Override
    public char[] getKeystorePassword() {
        return AutomatedInstallData.getInstance().getVariable("saml.storepass").toCharArray();
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
        return Status.ERROR;
    }

    @Override
    public String getLangpackKey() {
        return "saml.preexisting.incorrect";
    }

    @Override
    protected boolean getCondition() {
        return true;
    }


}
