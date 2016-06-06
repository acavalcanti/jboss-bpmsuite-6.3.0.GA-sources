package com.redhat.installer.tests.servercommands;


import com.redhat.installer.asconfiguration.keystore.processpanel.KeystoreGenerator;
import com.redhat.installer.asconfiguration.processpanel.postinstallation.CustomVault;
import com.redhat.installer.framework.testers.PostinstallTester;
import com.redhat.installer.tests.TestUtils;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by aabulawi on 29/07/15.
 */

public class CustomVaultTest extends PostinstallTester {

    @Test
    public void createCustomVault() throws Exception {
        createInstallerVaultKeystore();

        idata.setVariable("jboss.home.dir", idata.getInstallPath());
        idata.setVariable("vault.keystoreloc.default", idata.getInstallPath() + "/vault.keystore");
        idata.setVariable("vault.encrdir.default", idata.getInstallPath() + "/vault");

        idata.setVariable("vault.keystoreloc", idata.getInstallPath() + "/vault.keystore");
        idata.setVariable("vault.encrdir", idata.getInstallPath() + "/vault");
        idata.setVariable("vault.itercount", "44");
        idata.setVariable("vault.alias", "vault");
        idata.setVariable("vault.salt", "88888888");
        idata.setVariable("vault.keystorepwd", "qwer1234");
        CustomVault customVault = new CustomVault();
        String[] args = {"--xml-file=standalone-ha.xml"};
        customVault.run(mockAbstractUIProcessHandler, args);

        Elements results = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR + "standalone.xml", "vault");
        assertEquals("${jboss.home.dir}/vault.keystore", getVaultOption(results, "KEYSTORE_URL"));
        assertEquals("${jboss.home.dir}/vault/", getVaultOption(results, "ENC_FILE_DIR"));
        assertEquals("88888888", getVaultOption(results, "SALT"));
        assertEquals("vault", getVaultOption(results, "KEYSTORE_ALIAS"));
        assertEquals("44", getVaultOption(results, "ITERATION_COUNT"));

    }

    private String getVaultOption(Elements vaultOptions, String key){
        return vaultOptions.get(0).getElementsByAttributeValue("name", key).get(0).attr("value");
    }

    private void createInstallerVaultKeystore(){
        KeystoreGenerator.run(mockAbstractUIProcessHandler, new String[] {"--operation=-genseckey", "--parameter=-keystore ${INSTALL_PATH}/vault.keystore",
                "--parameter=-storepass qwer1234", "--parameter=-keyalg AES", "--parameter=-keysize 128",
                "--parameter=-alias vault", "--parameter=-storetype jceks", "--parameter=-validity 36500", "--allow-existing=true"});
    }

}
