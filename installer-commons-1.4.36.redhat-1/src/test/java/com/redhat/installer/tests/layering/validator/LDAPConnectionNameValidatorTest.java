package com.redhat.installer.tests.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.layering.validator.LDAPConnectionNameValidator;
import org.junit.*;

/**
 * Created by thauser on 2/18/14.
 */
public class LDAPConnectionNameValidatorTest extends ValidatorTester {
    private static AutomatedInstallData idata;

    @BeforeClass
    public static void specificInit() throws Exception {
        idata = new AutomatedInstallData();
        idata.setVariable("ldap.preexisting.conn.names","ldap_existing_connection,another_connection,");
    }

    @AfterClass
    public static void destroy() throws Exception {
        TestUtils.destroyIdataSingleton();
    }

    @Before
    public void setUp() throws Exception {
        v = new LDAPConnectionNameValidator();
    }

    @Test
    public void testNoClash() throws Exception {
        mpc.addToFields("ldap_noclash_connection");
        assertTrueResult();
    }

    @Test
    public void testClash() throws Exception {
        mpc.addToFields("ldap_existing_connection");
        assertFalseResult();
    }



}
