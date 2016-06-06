package com.redhat.installer.tests.asconfiguration.keystore.validator;

import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngine;
import com.redhat.installer.asconfiguration.keystore.validator.JMSKeystoreValidator;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.testers.DataValidatorTester;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thauser on 6/10/14.
 */
public class JMSKeystoreValidatorTest extends DataValidatorTester {

    @BeforeClass
    public static void init() throws Exception{
        /** Setup the condition we care about; true / false can be toggled using the variable **/

        Map<String,Condition> condMap = new HashMap<String,Condition>(1);
        TestUtils.insertVariableCondition(condMap, "generate.client.keystores", "generateClientKeystores", "true");
        RulesEngine rules = new RulesEngine(condMap, idata);
        idata.setRules(rules);
    }

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "keystore.validator.authentication.failure",
                "keystore.validator.file.does.not.exist","keystore.validator.jvm.cannot.read",
                "keystore.validator.invalid.url","keystore.validator.file.is.empty",
                "keystore.validator.not.supported", "JMSKeystoreValidator.empty.dir",
                "JMSKeystoreValidator.file.not.exist");
        dv = new JMSKeystoreValidator();
    }

    @Test
    public void testWrongPasswordExistingKeystore() throws Exception{
       idata.setVariable("generateClientKeystores", "false");
       File keystore = TestUtils.createMockKeystore(tempFolder,"jks");
       idata.setVariable("existing.keystores.client.location", keystore.getAbsolutePath());
       idata.setVariable("generated.keystores.client.storepass", "somewrongpassword");
       assertStatusWarning();
    }

    @Test
    public void testCorrectPasswordExistingKeystore() throws Exception {
        idata.setVariable("generateClientKeystores", "false");
        File keystore = TestUtils.createMockKeystore(tempFolder,"jks");
        idata.setVariable("existing.keystores.client.location", keystore.getAbsolutePath());
        idata.setVariable("generated.keystores.client.storepass", TestUtils.mockKeystorePassword);
        assertStatusOk();
    }

    @Test
    public void testNonExistingKeystore() throws Exception {
        idata.setVariable("generateClientKeystores", "false");
        idata.setVariable("existing.keystores.client.location", "non-existing-keystore.keystore");
        idata.setVariable("generated.keystores.client.storepass", TestUtils.mockKeystorePassword);
        assertStatusError();
    }

    @Test
    public void testConditionFalse() throws Exception {
        idata.setVariable("generateClientKeystores", "true");
        assertStatusOk();
    }

    @Test
    public void testEmptyDirectory() throws Exception {
        idata.setVariable("generateClientKeystores", "false");
        idata.setVariable("existing.keystores.client.location", tempFolder.newFolder().getAbsolutePath());
        idata.setVariable("generated.keystores.client.storepass", TestUtils.mockKeystorePassword);
        assertStatusError();
    }

    @Test
    public void testDirectoryMultipleKeystores() throws Exception {
        idata.setVariable("generateClientKeystores", "false");
        File newFolder = tempFolder.newFolder();
        idata.setVariable("existing.keystores.client.location", newFolder.getAbsolutePath());
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/1");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/2");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/3");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/4");
        idata.setVariable("generated.keystores.client.storepass", TestUtils.mockKeystorePassword);
        assertStatusOk();
    }

    @Test
    @Ignore
    public void testDirectoryMultipleFilesInvalid() throws Exception {
        idata.setVariable("generateClientKeystores", "false");
        File newFolder = tempFolder.newFolder();
        idata.setVariable("existing.keystores.client.location", newFolder.getAbsolutePath());
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/1");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/2");
        TestUtils.createMockKeystore(tempFolder, "pkcs12", newFolder.getName()+"/3");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/4");
        idata.setVariable("generated.keystores.client.storepass", TestUtils.mockKeystorePassword);
        assertStatusError();
    }

    @Test
    public void testDirectoryMultipleKeystoresWrongPassword() throws Exception {
        idata.setVariable("generateClientKeystores", "false");
        File newFolder = tempFolder.newFolder();
        idata.setVariable("existing.keystores.client.location", newFolder.getAbsolutePath());
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/1");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/2");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/3");
        TestUtils.createMockKeystore(tempFolder, "jks", newFolder.getName()+"/4");
        idata.setVariable("generated.keystores.client.storepass", "derper");
        assertStatusWarning();
    }
}

