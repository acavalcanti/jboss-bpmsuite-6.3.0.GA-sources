package com.redhat.installer.tests.installation.validator;

import com.redhat.installer.installation.validator.CompareIdataVariablesValidator;
import com.redhat.installer.framework.mock.MockProcessingClient;
import com.redhat.installer.framework.testers.DataValidatorTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by aabulawi on 19/08/14.
 */
public class CompareIdataVariablesValidatorTest extends DataValidatorTester {
    CompareIdataVariablesValidator civ;
    MockProcessingClient mpc;

    @Before
    public void setUp() throws Exception{
        civ = new CompareIdataVariablesValidator();
        mpc = new MockProcessingClient();
    }

    @After
    public void tearDown() throws Exception {
        civ = null;
        mpc = null;

    }

    @Test
    public void testMatchingValuesEquals() throws Exception {
        mpc.addToFields("testValue");
        idata.setVariable("TestValue", "testValue");
        mpc.addToParams("value", "TestValue");
        mpc.addToParams("operation", "=");
        assertTrue(civ.validate(mpc));
    }

    @Test
    public void testMatchingValuesNotEquals() throws Exception {
        mpc.addToFields("testValue");
        idata.setVariable("TestValue", "testValue");
        mpc.addToParams("value", "TestValue");
        mpc.addToParams("operation", "!=");
        assertFalse(civ.validate(mpc));
    }

    @Test
    public void testNotMatchingValuesEquals() throws Exception {
        mpc.addToFields("testValueThatDoesn'tMatch");
        idata.setVariable("TestValue", "testValue");
        mpc.addToParams("value", "TestValue");
        mpc.addToParams("operation", "=");
        assertFalse(civ.validate(mpc));
    }

    @Test
    public void testNotMatchingValuesNotEquals() throws Exception {
        mpc.addToFields("testValueThatDoesn'tMatch");
        idata.setVariable("TestValue", "testValue");
        mpc.addToParams("value", "TestValue");
        mpc.addToParams("operation", "!=");
        assertTrue(civ.validate(mpc));
    }

    @Test
    public void noParams() throws Exception {
        mpc.addToFields("forgotToSetTheParams");
        assertFalse(civ.validate(mpc));
    }
}

