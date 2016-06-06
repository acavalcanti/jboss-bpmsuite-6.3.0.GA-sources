package com.redhat.installer.tests.layering.validator;

import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.layering.validator.IsValidVaultValidator;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by thauser on 6/25/15.
 */
public class IsValidVaultValidatorTest extends DataValidatorTester {
    @Before
    public void init() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "IsValidVaultValidator.missing.keystore");
        idata.setInstallPath(tempFolder.getRoot().toString());
        dv = new IsValidVaultValidator();
    }

    @Test
    public void noVaultTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, "/IsValidVaultValidatorTest/no-vault.xml","/standalone/configuration/standalone.xml");
        assertStatusOk();
    }

    @Test
    public void serverVariableTest(){
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "vault.keystore");
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, "/IsValidVaultValidatorTest/server-properties.xml", "/standalone/configuration/standalone.xml");
        assertStatusOk();
    }

    @Test
    public void userVariableTest(){
        MockFileBuilder.makeEmptyFileAtPath(tempFolder,"vault.keystore");
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, "/IsValidVaultValidatorTest/user-properties.xml","/standalone/configuration/standalone.xml");
        assertStatusOk();
    }

    @Test
    public void invalidVault(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, "/IsValidVaultValidatorTest/invalid-vault.xml","/standalone/configuration/standalone.xml");
        assertStatusError();
    }
}
