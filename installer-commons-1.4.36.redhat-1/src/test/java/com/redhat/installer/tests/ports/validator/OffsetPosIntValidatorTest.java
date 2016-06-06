package com.redhat.installer.tests.ports.validator;

import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.ports.validator.OffsetPosIntValidator;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by thauser on 2/3/14.
 */
public class OffsetPosIntValidatorTest extends ValidatorTester {
    @Before
    public void setUp(){
        v = new OffsetPosIntValidator();
    }

    @Test
    public void testPositiveInteger(){
        mpc.addToFields("500");
        assertTrueResult();
    }

    @Test
    public void testNegativeInteger(){
        mpc.addToFields("-500");
        assertFalseResult();
    }

    @Test
    public void testNonInteger(){
        mpc.addToFields("5.60");
        assertFalseResult();
    }

    @Test
    public void testNonNumber(){
        mpc.addToFields("Not a number");
        assertFalseResult();
    }

    @Test
    public void testZero(){
        mpc.addToFields("0");
        assertTrueResult();
    }

}
