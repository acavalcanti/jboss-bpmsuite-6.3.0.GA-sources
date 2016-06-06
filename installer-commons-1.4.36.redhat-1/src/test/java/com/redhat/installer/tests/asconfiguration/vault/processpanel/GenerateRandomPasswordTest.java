package com.redhat.installer.tests.asconfiguration.vault.processpanel;

import com.redhat.installer.asconfiguration.vault.processpanel.GenerateRandomPassword;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Test the updated password generator for DTGov Workflows (and perhaps other things)
 * Created by thauser on 6/18/14.
 */
public class GenerateRandomPasswordTest extends ProcessPanelTester {
    GenerateRandomPassword grp;

    @Before
    public void setUp(){
        grp = new GenerateRandomPassword();
    }

    @Test
    public void testGeneratedPassword(){
        // generate a few thousand passwords and ensure they don't break our requirements
        for (int i =0; i < 10000; i++){
            String result = grp.addRandomChars("", 25);
            assertTrue(StringUtils.containsAny(result, grp.getValidAlphas()) && StringUtils.containsAny(result, grp.getValidNumbers()) && StringUtils.containsAny(result, grp.getValidSymbols()));
        }
    }

    @Test
    public void testVariableAssignment(){
        grp.run(handler, new String[]{"--variable=test.password"});
        assertNotNull(idata.getVariable("test.password"));
    }

    @Override
    public void testProcessPanelInstantiation() {

    }
}
