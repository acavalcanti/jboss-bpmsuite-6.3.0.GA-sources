package com.redhat.installer.tests.layering.processpanel;

import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.layering.processpanel.PreExistingLdapSslHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by fcanas on 2/24/14.
 */
public class PreExistingLdapSslHelperTest extends ProcessPanelTester {
    private static String [] configFiles = new String [] {"standalone.xml"};
    static Document testConfigDoc;
    String testDocPath;
    PreExistingLdapSslHelper runner;

    @Override
    public void testProcessPanelInstantiation() {

    }

    @Before
    public void setUpTestDoc() {
        runner = new PreExistingLdapSslHelper();
        // Copy the test config files to the generated test dirs
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, "/sslLdapTest/standalone.xml","/standalone/configuration/standalone.xml");

        testDocPath = idata.getInstallPath() + File.separator + "standalone/configuration/standalone.xml";

        for (String config : configFiles) {
            idata.setVariable(config + ".pre.existing.ldap", "false");
            idata.setVariable(config + ".pre.existing.ssl", "false");
        }
    }

    @Test
    public void testLDAP() {
        for (String config : configFiles) {
            idata.setVariable(config + ".pre.existing.ldap", "true");
            idata.setVariable(config + ".pre.existing.ssl", "false");
        }
        removeLdap();
        restoreLdap();
    }

    @Test
    public void testSSL() {
        for (String config : configFiles) {
            idata.setVariable(config + ".pre.existing.ldap", "false");
            idata.setVariable(config + ".pre.existing.ssl", "true");
        }
        removeSsl();
        restoreSsl();
    }

    @Test
    public void noHtml() {
        for (String config : configFiles) {
            idata.setVariable(config + ".pre.existing.ldap", "true");
            idata.setVariable(config + ".pre.existing.ssl", "true");
        }

        String [] args = new String[]{"--remove=true"};
        PreExistingLdapSslHelper.run(handler, args);

        args = new String[]{"--restore=true"};
        PreExistingLdapSslHelper.run(handler, args);

        try {
            testConfigDoc = Jsoup.parse(new FileInputStream(new File(testDocPath)), "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert(testConfigDoc.select("html").isEmpty());
    }


    public void removeLdap() {
        String [] args = new String[]{"--remove=true"};
        runner.run(handler, args);

        try {
            testConfigDoc = Jsoup.parse(new FileInputStream(new File(testDocPath)), "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Condition is properly set:
        for (String config : configFiles) {
            assert(idata.getVariable(config + ".pre.existing.ldap").equals("true"));
        }

        // All ldap refs removed from management interfaces:
        Elements secRealms = testConfigDoc.select("management-interfaces > [security-realm=ldap_security_realm]");
        assert(secRealms.isEmpty());

        // Ldap refs saved for later restoration:
        assert(PreExistingLdapSslHelper.getManInterfaceMap().get("native-interface0").equals("ldap_security_realm"));
        assert(PreExistingLdapSslHelper.getManInterfaceMap().get("http-interface1").equals("ldap_security_realm2"));

        // Removed all ldap connections
        Elements ldapConns = testConfigDoc.select("outbound-connections > ldap");
        assert(ldapConns.isEmpty());
    }

    public void removeSsl() {
        String [] args = new String[]{"--remove=true"};
        runner.run(handler, args);

        try {
            testConfigDoc = Jsoup.parse(new FileInputStream(new File(testDocPath)), "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Condition is properly set:
        for (String config : configFiles) {
            assert(idata.getVariable(config + ".pre.existing.ssl").equals("true"));
        }

        // Ssl server identity removed from ManagementRealm
        Elements sslIdentities = testConfigDoc.select("security-realms > security-realm[name=ManagementRealm] > server-identities");
        assert(sslIdentities.isEmpty());

        // Changed secure sockets to regular sockets:
        Elements secureSockets = testConfigDoc.select("management-interfaces > http-interface > socket-binding");
        for (Element secureSocket : secureSockets){
            assert(secureSocket.hasAttr("http"));
            assert(secureSocket.attr("http").equals("management-http"));
        }
    }


    public void restoreLdap() {
        String [] args = new String[]{"--restore=true"};
        runner.run(handler, args);

        try {
            testConfigDoc = Jsoup.parse(new FileInputStream(new File(testDocPath)), "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // All ldap refs *restored* to their original state.
        Elements manInterfaces = testConfigDoc.select("management-interfaces > *");
        assert(manInterfaces.select("native-interface").first().attr("security-realm").equals("ldap_security_realm"));
        assert(manInterfaces.select("http-interface").first().attr("security-realm").equals("ldap_security_realm2"));

        // Restored ldap connections:
        Elements ldapConns = testConfigDoc.select("outbound-connections > ldap");
        assert(ldapConns.size() == 2);

    }

    public void restoreSsl() {
        String [] args = new String[]{"--restore=true"};
        runner.run(handler, args);

        try {
            testConfigDoc = Jsoup.parse(new FileInputStream(new File(testDocPath)), "UTF-8", "", Parser.xmlParser());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ManagementRealm's server-identities restored to their original state:
        Elements manRealmServerIdentities = testConfigDoc.select("security-realms > security-realm[name=ManagementRealm] > server-identities");
        assert(manRealmServerIdentities.size()==1);
        assert(manRealmServerIdentities.select("ssl").size() == 1);

        // Restored the secure-socket-bindings for http-interfaces:
        Elements httpInterfaces = testConfigDoc.select("management-interfaces > http-interface");
        for (Element httpInterface : httpInterfaces) {
            assert(httpInterface.select("socket-binding").hasAttr("https"));
            assert(httpInterface.select("socket-binding").attr("https").equals("management-https"));
        }
    }

}
