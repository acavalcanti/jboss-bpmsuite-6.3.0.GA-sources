package com.redhat.installer.asconfiguration.vault.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import org.jboss.security.Base64Utils;
import org.jboss.security.plugins.PBEUtils;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Checks the length of the resulting password mask and tries to ensure that the error here will not occur:
 * https://bugzilla.redhat.com/show_bug.cgi?id=1125004
 *
 * Note: This validator is only needed for installers installing something older than 6.4.0
 * Created by thauser on 2/3/15.
 */
public class VaultMaskValidator implements DataValidator {
    String errorLangpackId;
    String formattedMessage;

    @Override
    public Status validateData(AutomatedInstallData idata) {
        try {
            String salt = idata.getVariable("vault.salt");
            int iterCount = Integer.parseInt(idata.getVariable("vault.itercount"));
            String password = getPassword();
            if (password == null) {
                setErrorLangpackId("VaultMaskValidator.missing.password");
                setFormattedMessage(idata.langpack.getString(getErrorMessageId()));
                return Status.ERROR;
            }
            String maskedPassword = mockVaultEncode64(password, salt, iterCount);
            // if the length is not divisible by 8, vault encoding will throw an IllegalBlockSizeException
            if (Base64Utils.fromb64(maskedPassword).length % 8 != 0) {
                setErrorLangpackId("VaultMaskValidator.invalid.password");
                setFormattedMessage(String.format(idata.langpack.getString(getErrorMessageId())));
                return Status.ERROR;
            }
        } catch (Exception e) {
            setErrorLangpackId("VaultMaskValidator.exception.occurred");
            setFormattedMessage(String.format(idata.langpack.getString(getErrorMessageId()), e.getLocalizedMessage()));
            e.printStackTrace();
        }
        return Status.OK;
    }

    /**
     * This encode method is taken from the Vault code. We copy it here so we don't need
     * to fully instantiate a VaultSession, because then we're bound to a keystore existing
     *
     * @param input
     * @param salt
     * @param iterCount
     * @return
     * @throws Exception
     */
    private String mockVaultEncode64(String input, String salt, int iterCount) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEwithMD5andDES");
        char[] password = "somearbitrarycrazystringthatdoesnotmatter".toCharArray();
        PBEParameterSpec cipherSpec = new PBEParameterSpec(salt.getBytes(), iterCount);
        PBEKeySpec keySpec = new PBEKeySpec(password);
        SecretKey cipherKey = factory.generateSecret(keySpec);
        String maskedPass = PBEUtils.encode64(input.getBytes(), "PBEwithMD5andDES", cipherKey, cipherSpec);
        return maskedPass;
    }

    @Override
    public String getErrorMessageId() {
        return errorLangpackId;
    }

    @Override
    public String getWarningMessageId() {
        return errorLangpackId;
    }

    @Override
    // assume the auto.xml is fine
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setErrorLangpackId(String errorLangpackId) {
        this.errorLangpackId = errorLangpackId;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public String getPassword(){
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        return idata.getVariable("vault.keystorepwd");
    }

}
