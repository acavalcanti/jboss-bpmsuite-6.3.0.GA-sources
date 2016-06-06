package com.redhat.installer.tests.ports.validator;

import com.redhat.installer.ports.validator.AddressValidator;
import com.redhat.installer.framework.mock.MockProcessingClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 2/3/14.
 */
public class AddressValidatorTest {
    AddressValidator av;
    MockProcessingClient mpc;
    private String teststring;

    @Before
    public void setUp() throws Exception{
        av = new AddressValidator();
        mpc = new MockProcessingClient();
        mpc.addToFields("dummy"); // needed because of strange design of AddressValidator
    }

    @After
    public void tearDown() throws Exception{
        av = null;
        mpc = null;
    }

    @Test
    public void testValidIPv4Address() throws Exception{
        addTestIp("192.168.1.1", "testValidIPv4Address()");
        assertTrue(av.validate(mpc));
    }

    @Test
    public void testInvalidIPv4Address() throws Exception {
        addTestIp("192.300.1.1", "testInvalidIPv4Address()");
        assertFalse(av.validate(mpc));
    }

    @Test
    public void testValidIPv6AddressIPv4Canonical() throws Exception {
        addTestIp("::ffff:192.168.1.1", "testValidIPv6AddressIPv4Canonical()");
        assertTrue(av.validate(mpc));
    }

    @Test
    public void testInvalidIPv6AddressIPv4Canonical() throws Exception {
        addTestIp("::ffff:192.300.1.1", "testInvalidIPv6AddressIPv4Canonical()");
        assertFalse(av.validate(mpc));
    }

    @Test
    public void testValidFullIPv6Address() throws Exception {
        addTestIp("FE80:0000:0000:0000:0202:00FF:FE1E:8329", "testValidFullIPv6Address()");
        assertTrue(av.validate(mpc));
    }

    @Test
    public void testInvalidFullIPv6Address() throws Exception{
        addTestIp("FE80:0000:0000:0000:0202:00FF:FE1E:832Q", "testInvalidFullIPv6Address()");
        assertFalse(av.validate(mpc));
    }

    @Test
    public void testValidCollapsedIPv6Address() throws Exception {
        addTestIp("FE80::202:FF:FE1E:8329", "testValidCollapsedIPv6Address()");
        assertTrue(av.validate(mpc));
    }

    @Test
    public void testInvalidCollapsedIPv6Address() throws Exception {
        addTestIp("FE80::202:FF:FE1E:832Q", "testInvalidCollapsedIPv6Address()");
        assertFalse(av.validate(mpc));
    }

    @Test
    public void testValidDomain() throws Exception {
        addTestIp("localhost", "testValidDoman()");
        assertTrue(av.validate(mpc));
    }

    @Test
    public void testInvalidDomain() throws Exception {
        addTestIp("superhouse", "testInvalidDomain()");
        assertFalse(av.validate(mpc));
    }

    /**
     * http://tools.ietf.org/html/rfc6874
     * May require more extensive testing
     * @throws Exception
     */

    @Test
    public void testValidIPv6ZoneId() throws Exception {
        addTestIp("FE80::202:FF:FE1E:8329%eth0", "testValidIPv6ZoneId()");
        assertTrue(av.validate(mpc));
    }

    @Test
    public void testInvalidIPv6ZoneId() throws Exception {
        addTestIp("FE80::202:FF:FE1E:8329%%eth0", "testInvalidIPv6ZoneId()");
        assertFalse(av.validate(mpc));
    }

    private void addTestIp(String ip, String methodname){
        teststring = ip;
        mpc.addToFields(ip);
        System.out.println(methodname + " : " + teststring);
    }
}
