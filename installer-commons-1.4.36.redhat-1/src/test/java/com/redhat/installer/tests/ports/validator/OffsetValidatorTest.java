package com.redhat.installer.tests.ports.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.ports.validator.OffsetValidator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by thauser on 2/3/14.
 */
public class OffsetValidatorTest extends ValidatorTester{
    static AutomatedInstallData idata;

    @BeforeClass
    public static void specificInit(){
        idata = new AutomatedInstallData();
        idata.setVariable("domain.port1", "10000");
        idata.setVariable("standalone.port1", "12000");
        idata.setVariable("domain.port2", "20000");
        idata.setVariable("standalone.port2", "22000");
        idata.setVariable("standalone.port3", "40000");
        idata.setVariable("domain.port3", "55000");
    }

    @AfterClass
    public static void specificDestroy() throws Exception {
        TestUtils.destroyIdataSingleton();
    }

    @Before
    public void setUp(){
        v = new OffsetValidator();
    }

    @Test
    public void testValidOffset(){
        mpc.addToFields("10000");
        assertTrueResult();
    }

    @Test
    public void testInvalidOffset(){
        // the invalidity of this number is based upon the content of idata
        mpc.addToFields("50000");
        assertFalseResult();
    }

    @Test
    public void testNegativeOffset(){
        mpc.addToFields("-1000");
    }

    @Test
    public void testNaNOffset(){
        mpc.addToFields("offset");
        assertFalseResult();
    }
}
