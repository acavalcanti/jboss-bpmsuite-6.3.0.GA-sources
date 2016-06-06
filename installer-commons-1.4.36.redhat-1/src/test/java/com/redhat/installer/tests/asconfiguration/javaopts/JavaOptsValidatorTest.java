package com.redhat.installer.tests.asconfiguration.javaopts;

import com.redhat.installer.asconfiguration.javaopts.JavaOptsValidator;
import com.redhat.installer.framework.mock.MockProcessingClient;
import com.redhat.installer.installation.validator.BlackListValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by aabulawi on 17/04/15.
 */
public class JavaOptsValidatorTest {

    JavaOptsValidator jov;
    MockProcessingClient mpc;

    @Before
    public void setUp() throws Exception{
        jov = new JavaOptsValidator();
        mpc = new MockProcessingClient();
    }

    @After
    public void tearDown() throws Exception {
        jov = null;
        mpc = null;
    }

    @Test
    public void testValidJavaOpts() throws Exception{
        mpc.addToFields("-Xmx300m -DskipTests=True");
        assertTrue(jov.validate(mpc));
    }

    @Test
    public void testInvalidJavaOpts() throws Exception{
        mpc.addToFields("-Xmx500m -Xms600m -DskipTests");
        assertFalse(jov.validate(mpc));
    }

    @Test
    public void testVersionInJavaOpts() throws Exception {
        mpc.addToFields("-Xmx500m -Xms600m -version");
        assertFalse(jov.validate(mpc));
    }

    @Test
    public void testEmptyJavaOpts() throws Exception {
        mpc.addToFields("");
        assertTrue(jov.validate(mpc));
    }

}
