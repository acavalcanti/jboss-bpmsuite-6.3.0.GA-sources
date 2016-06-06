package com.redhat.installer.tests.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.layering.validator.LDAPRealmNameValidator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by thauser on 2/18/14.
 */
public class LDAPRealmNameValidatorTest extends ValidatorTester {
    private static AutomatedInstallData idata;

    @BeforeClass
    public static void specificInit() throws Exception {
        idata = new AutomatedInstallData();
        idata.setVariable("ldap.preexisting.realm.names", "ldap_existing_realm,another_realm,");
    }

    @AfterClass
    public static void destroy() throws Exception {
        TestUtils.destroyIdataSingleton();
    }

    @Before
    public void setUp() throws Exception {
        v = new LDAPRealmNameValidator();
    }

    @Test
    public void testNoClash() throws Exception {
        mpc.addToFields("ldap_noclash_realm");
        assertTrueResult();
    }

    @Test
    public void testClash() throws Exception {
        mpc.addToFields("ldap_existing_realm");
        assertFalseResult();
    }
}
