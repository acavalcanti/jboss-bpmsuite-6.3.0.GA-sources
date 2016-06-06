package com.redhat.installer.framework.testers;

import com.izforge.izpack.panels.Validator;
import com.redhat.installer.framework.mock.MockDirSetter;
import com.redhat.installer.framework.mock.MockProcessingClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 2/3/14.
 */
public abstract class ValidatorTester {
    public MockProcessingClient mpc;
    public Validator v;

    @Before
    public void start() {
        mpc = new MockProcessingClient();
        mpc.addToParams("key", "value");
    }

/*    @AfterClass
    public static void cleanUpInit() {
        MockDirSetter.removeMockSpecsDir();
    }*/

    @After
    public void end() {
        mpc = null;
        v = null;
    }

    @Test
    public void testParams() throws Exception {
        if (mpc.hasParams()) {
            Map<String,String> params = mpc.getValidatorParams();
            assertTrue("value".equals(params.get("key")));
        }
    }

    public void assertTrueResult(){
        assertTrue(v.validate(mpc));
    }

    public void assertFalseResult(){
        assertFalse(v.validate(mpc));
    }
}
