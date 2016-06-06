package com.redhat.installer.tests.asconfiguration.ldap.validator;

import com.redhat.installer.asconfiguration.ldap.validator.LDAPValidator;
import com.redhat.installer.framework.mock.MockLdapServer;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import org.junit.*;

/**
 * Created by thauser on 2/4/14.
 */
public class LDAPValidatorTest extends DataValidatorTester
{
    private MockLdapServer ldapServer;

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "Ldap.error");
        dv = new LDAPValidator();
        ldapServer = new MockLdapServer(tempFolder.getRoot());
        ldapServer.startServer();
    }

    @After
    public void tearDown() throws Exception {
        ldapServer.stopServer();
        ldapServer = null;
    }

    @Test
    public void testCorrectCredentials() throws Exception {
        idata.setVariable("ldap.url", TestUtils.ldapTestUrl);
        idata.setVariable("ldap.dn", TestUtils.ldapAdminDn);
        idata.setVariable("ldap.password", TestUtils.ldapAdminPassword);
        assertStatusOk();
    }

    @Test
    public void testWrongPassword() throws Exception {
        idata.setVariable("ldap.url", TestUtils.ldapTestUrl);
        idata.setVariable("ldap.dn", TestUtils.ldapAdminDn);
        idata.setVariable("ldap.password", "incorrect");
        assertStatusWarning();
    }

    @Test
    public void testWrongDn() throws Exception {
        idata.setVariable("ldap.url", TestUtils.ldapTestUrl);
        idata.setVariable("ldap.dn", "uid=totallywrong,ou=wrongagain");
        idata.setVariable("ldap.password", TestUtils.ldapAdminPassword);
        assertStatusWarning();
    }

    @Test
    public void testWrongUrl() throws Exception {
        idata.setVariable("ldap.url", "ldap://wrongurl:1111");
        idata.setVariable("ldap.dn", TestUtils.ldapAdminDn);
        idata.setVariable("ldap.password", TestUtils.ldapAdminPassword);
        assertStatusWarning();
    }


}
