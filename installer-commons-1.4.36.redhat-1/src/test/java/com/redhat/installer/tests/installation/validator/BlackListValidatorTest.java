package com.redhat.installer.tests.installation.validator;

import com.redhat.installer.installation.validator.BlackListValidator;
import com.redhat.installer.framework.mock.MockProcessingClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by aabulawi on 23/05/14.
 */
public class BlackListValidatorTest  {
    BlackListValidator blv;
    MockProcessingClient mpc;
    private String testString;

    @Before
    public void setUp() throws Exception{
        blv = new BlackListValidator();
        mpc = new MockProcessingClient();
        mpc.addToParams("blacklist", "admin,love,potatoe");

    }

    @After
    public void tearDown() throws Exception {
        blv = null;
        mpc = null;
    }

    @Test
    public void testValidUserName() throws Exception{
        mpc.addToFields("ahmed");
        assertTrue(blv.validate(mpc));
    }

    @Test
    public void testInvalidUserName() throws Exception{
        mpc.addToFields("admin");
        assertFalse(blv.validate(mpc));
    }

    @Test
    public void testNoInput() throws Exception {
        mpc.addToFields("");
        assertTrue(blv.validate(mpc));
    }

}
