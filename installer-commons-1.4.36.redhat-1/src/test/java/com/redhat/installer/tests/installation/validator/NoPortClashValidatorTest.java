package com.redhat.installer.tests.installation.validator;

import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.installation.validator.NoPortClashValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Tests for the NoPortClashValidator in installer-commons
 * Created by thauser on 1/29/14.
 */
public class NoPortClashValidatorTest extends DataValidatorTester
{

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "NoPortClashValidator.warning");
        dv = new NoPortClashValidator();

    }

    @After
    public void tearDown() throws Exception {
        dv = null;
    }


    @Test
    public void testPortInUse() throws Exception {
        ServerSocket ss = new ServerSocket(9999, 50, InetAddress.getByName("127.0.0.1"));
        ss.setReuseAddress(true);
        assertStatusWarning();
    }

    @Test
    public void testPortNotInUse() throws Exception{
        ServerSocket ss = new ServerSocket(9998, 50, InetAddress.getByName("127.0.0.1"));
        ss.setReuseAddress(true);
        assertStatusOk();
        ss.close();
    }


}
