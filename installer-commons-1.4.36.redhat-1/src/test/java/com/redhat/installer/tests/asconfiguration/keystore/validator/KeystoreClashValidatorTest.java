package com.redhat.installer.tests.asconfiguration.keystore.validator;

import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.rules.VariableCondition;
import com.redhat.installer.asconfiguration.keystore.processpanel.ClientKeystoreBuilder;
import com.redhat.installer.asconfiguration.keystore.validator.KeystoreClashValidator;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thauser on 6/13/14.
 */
public class KeystoreClashValidatorTest extends DataValidatorTester {

    @BeforeClass
    public static void init() throws Exception {
        VariableCondition testCond = TestUtils.createVariableCondition("generate.client.keystores", "generateClientKeystores", "true");
        Map<String,Condition> condMap = new HashMap<String,Condition>(1);
        condMap.put("generate.client.keystores", testCond);
        RulesEngine rules = new RulesEngine(condMap,idata);
        rules.addCondition(testCond);
        idata.setRules(rules);
    }

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "ClientKeystoreClashValidator.generated.client.keystore.clash");
        dv = new KeystoreClashValidator();
    }

    @Test
    public void testNoClash() throws Exception {
        idata.setVariable("generateClientKeystores", "true");
        idata.setVariable("generated.keystores.server.location", idata.getInstallPath() + "/server.keystore");
        idata.setVariable("generated.keystores.client.number", "10");
        idata.setVariable("generated.keystores.client.location", idata.getInstallPath() + "/clients");
        assertStatusOk();
    }

    @Test
    public void testClientFileClash() throws Exception {
        idata.setVariable("generateClientKeystores", "true");
        idata.setVariable("generated.keystores.client.number", "10");
        idata.setVariable("generated.keystores.client.location", idata.getInstallPath());
        TestUtils.createMockKeystore(tempFolder,"jks", String.format(ClientKeystoreBuilder.getClientKeystoreTemplate(),5));
        assertStatusError();
    }

    @Test
    public void testClientDirectoryClash() throws Exception {
        File clash = tempFolder.newFile();
        idata.setVariable("generateClientKeystores", "true");
        idata.setVariable("generated.keystores.client.number", "1");
        idata.setVariable("generated.keystores.client.location", clash.getAbsolutePath());
        assertStatusError();
    }

    @Test
    public void testServerDirectoryClash() throws Exception {
        File clash = tempFolder.newFolder();
        idata.setVariable("generated.keystores.server.location", clash.getAbsolutePath());

        assertStatusError();
    }

    @Test
    public void testServerFileClash() throws Exception {
        File clash = tempFolder.newFile();
        idata.setVariable("generated.keystores.server.location", clash.getAbsolutePath());

        assertStatusError();
    }
}
