package com.redhat.installer.tests.ports.validator;

import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.ports.validator.PortWithinRangeValidator;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by thauser on 2/4/14.
 */
public class PortWithinRangeValidatorTest extends ValidatorTester {

    @Before
    public void setUp() throws Exception{
        v = new PortWithinRangeValidator();
        mpc.addToFields("0"); // dummy value because PortWithinRangeValidator ignores the first field
    }

    @Test
    public void testValidPort() {
        mpc.addToFields("9999");
        assertTrueResult();
    }

    @Test
    public void testInvalidPort() {
        mpc.addToFields("100000");
        assertFalseResult();
    }

    @Test
    public void testNegativePort(){
        mpc.addToFields("-10000");
        assertFalseResult();
    }

    @Test
    public void testNaNPort(){
        mpc.addToFields("notanumber");
        assertFalseResult();
    }



}
