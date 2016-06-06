package com.redhat.installer.asconfiguration.vault.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import org.jboss.security.Base64Utils;
import org.jboss.security.plugins.PBEUtils;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Detects if there's a problem with a user's Vault password, and adjusts other
 * vault parameters to make sure the problem is rectified.
 * Created by thauser on 2/3/15.
 */
public class VaultMaskAdjuster implements PanelAction{
    private String password;

    @Override
    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler) {
        try {
            String password = getPassword();
            String salt = idata.getVariable("vault.salt");
            Integer iterCount = Integer.parseInt(idata.getVariable("vault.itercount"));
            if (password != null) {
                String maskedPassword = mockVaultEncode64(password, salt, iterCount);
                while (Base64Utils.fromb64(maskedPassword).length % 8 != 0) {
                    iterCount++;
                    maskedPassword = mockVaultEncode64(password, salt, iterCount);
                }
                idata.setVariable("vault.itercount", iterCount.toString());
                idata.setVariable("vault.itercount.default", iterCount.toString());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {

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

    public String getPassword() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        return idata.getVariable("vault.keystorepwd");
    }
}
