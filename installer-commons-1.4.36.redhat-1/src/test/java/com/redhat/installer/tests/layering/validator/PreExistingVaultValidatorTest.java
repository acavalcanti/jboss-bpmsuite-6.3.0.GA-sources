package com.redhat.installer.tests.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.framework.mock.MockInstaller;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.layering.validator.PreExistingVaultValidator;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 1/30/14.
 */
public class PreExistingVaultValidatorTest extends DataValidatorTester
{
    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder,"ssl.password.incorrect");
        idata.setInstallPath(tempFolder.getRoot().toString());
        dv = new PreExistingVaultValidator();
    }

    @Test
    public void testWrongPassword() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"jceks");
        idata.setVariable("vault.resolved.keystoreloc", keystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", "incorrectpassword"); // must be > 6 chars for mock keystore generation
        idata.setVariable("preexisting.vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        assertStatusWarning();
        assertLangpack("keystore.validator.authentication.failure");
    }

    // vault keystores are always of type jceks, so this test needs updating
    @Test
    public void testCorrectPasswordExistingKey() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        TestUtils.createKeyInMockKeystore(keystore.getAbsolutePath(),"vaultalias", TestUtils.KeyType.SECRET_KEY, "jceks");
        idata.setVariable("vault.requires.secret.key", "true");
        setCorrectVaultKeystoreVariables(idata, keystore.getAbsolutePath());
        TestUtils.createMockKeystore(tempFolder,"jceks");
        assertStatusOk();
        assertEquals("true", idata.getVariable("installVault"));
        assertEquals("false", idata.getVariable("new.postinstall.vault"));
    }

    @Test
    public void testCorrectPasswordNoKey() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        TestUtils.createKeyInMockKeystore(keystore.getAbsolutePath(),"noalias", TestUtils.KeyType.SECRET_KEY, "jceks");
        setCorrectVaultKeystoreVariables(idata, keystore.getAbsolutePath());
        assertStatusOk();
        assertEquals("true", idata.getVariable("new.postinstall.vault"));
    }


    @Test
    public void testFileNotExist() throws Exception {
        setCorrectVaultKeystoreVariables(idata, "path-doesnt-exist");
        assertStatusWarning();
        assertLangpack("keystore.validator.file.does.not.exist");
    }

    @Test
    public void testNotJKSKeystore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"jceks");
        setCorrectVaultKeystoreVariables(idata, keystore.getAbsolutePath());
        assertStatusOk();
    }

    @Test
    public void testServerVariableSubstitution() throws Exception{
        File keystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        setCorrectVaultKeystoreVariables(idata, "${jboss.home.dir}/"+keystore.getName());
        assertStatusOk();
        assertEquals("true", idata.getVariable("new.postinstall.vault"));
    }

    /**
     * Convenience method to set vault keystore variables to correct values
     * @param idata
     */
    private void setCorrectVaultKeystoreVariables(AutomatedInstallData idata, String keystorePath){
        idata.setVariable("vault.alias", "vaultalias");
        idata.setVariable("vault.resolved.keystoreloc", keystorePath);
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("preexisting.vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
    }
}
