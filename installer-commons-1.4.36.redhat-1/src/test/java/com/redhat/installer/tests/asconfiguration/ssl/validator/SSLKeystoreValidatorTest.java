package com.redhat.installer.tests.asconfiguration.ssl.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.asconfiguration.ssl.validator.SSLKeystoreValidator;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * This class is essentially identical to PreExistingVaultValidatorTests
 * Created by thauser on 1/30/14.
 */
public class SSLKeystoreValidatorTest extends DataValidatorTester
{
    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "ssl.password.incorrect");
        dv = new SSLKeystoreValidator();
    }

    @Test
    public void testWrongPassword() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"jks");
        idata.setVariable("ssl.path", keystore.getAbsolutePath());
        idata.setVariable("ssl.password", "incorrectpassword"); // must be > 6 chars for mock keystore generation
        idata.setVariable("ssl.allowed.keystore.types", "JKS,CASEEXACTJKS");
        assertStatusWarning();
        assertLangpack("keystore.validator.authentication.failure");
    }

    @Test
    public void testCorrectPassword() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"jks");
        idata.setVariable("ssl.allowed.keystore.types", "JKS,CASEEXACTJKS");
        setCorrectSSLKeystoreVariables(idata, keystore.getAbsolutePath());
        assertStatusOk();
    }

    @Test
    public void testFileNotExist() throws Exception {
        setCorrectSSLKeystoreVariables(idata, "doesn'texist");
        assertStatusError();
        assertLangpack("keystore.validator.file.does.not.exist");
    }
    /**
     * Convenience method to set ssl keystore variables to correct values
     * @param idata
     */
    private void setCorrectSSLKeystoreVariables(AutomatedInstallData idata, String keystorePath){
        idata.setVariable("ssl.path", keystorePath);
        idata.setVariable("ssl.password", TestUtils.mockKeystorePassword);
        idata.setVariable("ssl.allowed.keystore.types", "JKS,CASEEXACTJKS");
    }
}
