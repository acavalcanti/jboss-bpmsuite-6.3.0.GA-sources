package com.redhat.installer.tests.servercommands;

import com.redhat.installer.asconfiguration.processpanel.postinstallation.Ldap;
import com.redhat.installer.framework.testers.PostinstallTester;
import com.redhat.installer.tests.TestUtils;
import org.jsoup.select.Elements;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 8/11/15.
 */
public class LdapTest extends PostinstallTester {

    String[] ldapVars = new String[]{ "ldap.name", "ldap.url", "ldap.dn", "ldap.password",
                                    "ldap.realmname", "ldap.basedn", "ldap.recursive",
                                    "ldap.filtertype", "ldap.filter"};
    String[] sslVars = new String[]{"ssl.path", "ssl.password"};

    @Test
    public void testCorrectLdapNoSsl() throws Exception {
        setLdapVariables();
        Ldap.run(mockAbstractUIProcessHandler, new String[]{});
        Elements ldapRealm = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR + "standalone.xml", "security-realm[name=ldap.realmname]");
        Elements ldapConnection = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR + "standalone.xml", "outbound-connections > ldap");
        checkRealm(ldapRealm);
        checkConnection(ldapConnection);
    }

    private void checkRealm(Elements realm) {
        assertEquals("ldap.realmname", realm.attr("name"));
        Elements auth = realm.select("authentication > ldap");
        assertEquals("ldap.name",auth.attr("connection"));
        assertEquals("ldap.basedn", auth.attr("base-dn"));
        assertEquals("ldap.recursive", auth.attr("recursive"));
        assertEquals("ldap.filter", auth.select("username-filter").attr("attribute"));
    }

    private void checkConnection(Elements connection){
        assertEquals("ldap.name", connection.attr("name"));
        assertEquals("ldap.url", connection.attr("url"));
        assertEquals("ldap.password", connection.attr("search-credential"));
        assertEquals("ldap.dn", connection.attr("search-dn"));
    }


/*    @Test
    public void testCorrectLdapAndSsl(){
        setLdapVariables();
        setSslVariables();
    }*/


    private void setLdapVariables() {
        for (String var : ldapVars){
            idata.setVariable(var,var);
        }
    }

    private void setSslVariables(){
        for (String var : sslVars){
            idata.setVariable(var,var);
        }
    }





}
