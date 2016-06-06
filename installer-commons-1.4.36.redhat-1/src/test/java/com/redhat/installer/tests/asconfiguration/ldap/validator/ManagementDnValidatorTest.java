package com.redhat.installer.tests.asconfiguration.ldap.validator;

import com.redhat.installer.asconfiguration.ldap.validator.ManagementDnValidator;
import com.redhat.installer.framework.mock.MockLdapServer;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by thauser on 2/5/14.
 */
public class ManagementDnValidatorTest extends DataValidatorTester
{
    private MockLdapServer ldapServer;

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "Ldap.error2", "ldap.emptysearch.warning", "ldap.namingexception.warning");
        dv = new ManagementDnValidator();
        idata.setVariable("ldap.url", TestUtils.ldapTestUrl);
        idata.setVariable("ldap.dn", TestUtils.ldapAdminDn);
        idata.setVariable("ldap.password", TestUtils.ldapAdminPassword);
        idata.setVariable("ldap.filter","uid");
        idata.setVariable("ldap.filtertype","username");
        ldapServer = new MockLdapServer(tempFolder.getRoot());
        ldapServer.startServer();
    }

    @After
    public void tearDown() throws Exception {
        ldapServer.stopServer();
        ldapServer = null;
    }


    @Test
    public void testExistingDn() throws Exception {
        ldapServer.addUser("cn=Jiggalow Smith,o=TestOrganization","jsmith","Jiggalow Smith", "Smith");
        idata.setVariable("ldap.basedn", "o=TestOrganization");
        assertStatusOk();
    }

    @Test
    public void testNonExistingDn() throws Exception {
        ldapServer.addUser("cn=Jiggalow Smith,o=TestOrganization", "jsmith", "Jiggalow Smith", "Smith");
        idata.setVariable("ldap.basedn", "o=NonExistentOrganization");
        assertStatusWarning();
    }
}
