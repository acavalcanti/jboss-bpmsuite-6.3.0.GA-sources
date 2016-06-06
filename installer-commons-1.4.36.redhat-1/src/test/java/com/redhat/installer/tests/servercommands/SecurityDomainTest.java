package com.redhat.installer.tests.servercommands;

import com.redhat.installer.asconfiguration.processpanel.postinstallation.SecurityDomain;
import com.redhat.installer.framework.testers.PostinstallTester;
import com.redhat.installer.tests.TestUtils;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by aabulawi on 30/07/15.
 */
public class SecurityDomainTest extends PostinstallTester {

    @Test
    public void testSecurityDomainNoOptions() throws Exception {
        idata.setVariable("securitydomain.name.variable", "mySecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");
        idata.setVariable("securitydomain.add.authen.count.variable", "0");
        idata.setVariable("securitydomain.add.author.count.variable", "0");
        idata.setVariable("securitydomain.add.mapping.count.variable", "0");
        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        Elements result = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR + "standalone.xml", "security-domains > security-domain[name=mySecurityDomain]");
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).children().isEmpty());
    }

    @Test
    public void testSecurityDomainMultipleAuthenSameCode() throws Exception {
        idata.setVariable("securitydomain.name.variable", "mySecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.author.count.variable", "0");
        idata.setVariable("securitydomain.add.mapping.count.variable", "0");

        idata.setVariable("securitydomain.add.authen.count.variable", "6");
        idata.setVariable("securitydomain.add.authen.left.0.variable", "Client");
        idata.setVariable("securitydomain.add.authen.middle.0.variable", "Requisite");
        idata.setVariable("securitydomain.add.authen.right.0.variable", "apples=oranges");
        idata.setVariable("securitydomain.add.authen.left.1.variable", "Client");
        idata.setVariable("securitydomain.add.authen.middle.1.variable", "Requisite");
        idata.setVariable("securitydomain.add.authen.right.1.variable", "pizza=unhealthy");

        assertTrue(!SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
    }

    @Test
    public void testSecurityDomainMultipleAuthenDifferentCode() throws Exception {
        idata.setVariable("securitydomain.name.variable", "testSecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.author.count.variable", "0");
        idata.setVariable("securitydomain.add.mapping.count.variable", "0");

        idata.setVariable("securitydomain.add.authen.count.variable", "6");
        idata.setVariable("securitydomain.add.authen.left.0.variable", "Client");
        idata.setVariable("securitydomain.add.authen.middle.0.variable", "Requisite");
        idata.setVariable("securitydomain.add.authen.right.0.variable", "apples=oranges");
        idata.setVariable("securitydomain.add.authen.left.1.variable", "Certificate");
        idata.setVariable("securitydomain.add.authen.middle.1.variable", "Requisite");
        idata.setVariable("securitydomain.add.authen.right.1.variable", "pizza=unhealthy");

        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        assertTrue(TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR +
                "standalone.xml", "security-domains > security-domain[name=testSecurityDomain] > authentication > login-module").size() == 2);
    }

    @Test
    public void testSecurityDomainMultipleAuthorDifferentCode() throws Exception {
        idata.setVariable("securitydomain.name.variable", "testSecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.authen.count.variable", "0");
        idata.setVariable("securitydomain.add.mapping.count.variable", "0");

        idata.setVariable("securitydomain.add.author.count.variable", "6");
        idata.setVariable("securitydomain.add.author.left.0.variable", "DenyAll");
        idata.setVariable("securitydomain.add.author.middle.0.variable", "Requisite");
        idata.setVariable("securitydomain.add.author.right.0.variable", "apples=oranges");
        idata.setVariable("securitydomain.add.author.left.1.variable", "JACC");
        idata.setVariable("securitydomain.add.author.middle.1.variable", "Requisite");
        idata.setVariable("securitydomain.add.author.right.1.variable", "pizza=unhealthy");

        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        assertTrue(TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR +
                "standalone.xml", "security-domains > security-domain[name=testSecurityDomain] > authorization > policy-module").size() == 2);
    }

    @Test
    public void testSecurityDomainMultipleMappingDifferentCode() throws Exception {
        idata.setVariable("securitydomain.name.variable", "testSecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.authen.count.variable", "0");
        idata.setVariable("securitydomain.add.author.count.variable", "0");

        idata.setVariable("securitydomain.add.mapping.count.variable", "6");
        idata.setVariable("securitydomain.add.mapping.left.0.variable", "PropertiesRoles");
        idata.setVariable("securitydomain.add.mapping.middle.0.variable", "principal");
        idata.setVariable("securitydomain.add.mapping.right.0.variable", "apples=oranges");
        idata.setVariable("securitydomain.add.mapping.left.1.variable", "LdapRoles");
        idata.setVariable("securitydomain.add.mapping.middle.1.variable", "principal");
        idata.setVariable("securitydomain.add.mapping.right.1.variable", "pizza=unhealthy");

        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        assertTrue(TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR +
                "standalone.xml", "security-domains > security-domain[name=testSecurityDomain] > mapping > mapping-module").size() == 2);
    }

    @Test
    public void testSecurityDomainMapping() throws Exception {
        idata.setVariable("securitydomain.name.variable", "testSecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.authen.count.variable", "0");
        idata.setVariable("securitydomain.add.author.count.variable", "0");

        idata.setVariable("securitydomain.add.mapping.count.variable", "3");
        idata.setVariable("securitydomain.add.mapping.left.0.variable", "PropertiesRoles");
        idata.setVariable("securitydomain.add.mapping.middle.0.variable", "principal");
        idata.setVariable("securitydomain.add.mapping.right.0.variable", "apples=oranges");

        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        Elements result = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR +
                "standalone.xml", "security-domains > security-domain[name=testSecurityDomain] > mapping > mapping-module");

        assertEquals("PropertiesRoles", result.attr("code"));
        assertEquals("principal", result.attr("type"));
        assertEquals("apples", result.get(0).child(0).attr("name"));
        assertEquals("oranges", result.get(0).child(0).attr("value"));
    }

    @Test
    public void testSecurityDomainAuthen() throws Exception {
        idata.setVariable("securitydomain.name.variable", "testSecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.author.count.variable", "0");
        idata.setVariable("securitydomain.add.mapping.count.variable", "0");

        idata.setVariable("securitydomain.add.authen.count.variable", "3");
        idata.setVariable("securitydomain.add.authen.left.0.variable", "Certificate");
        idata.setVariable("securitydomain.add.authen.middle.0.variable", "Requisite");
        idata.setVariable("securitydomain.add.authen.right.0.variable", "pizza=unhealthy");

        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        Elements result = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR +
                "standalone.xml", "security-domains > security-domain[name=testSecurityDomain] > authentication > login-module");

        assertEquals("Certificate", result.attr("code"));
        assertEquals("requisite", result.attr("flag"));
        assertEquals("pizza", result.get(0).child(0).attr("name"));
        assertEquals("unhealthy", result.get(0).child(0).attr("value"));
    }

    @Test
    public void testSecurityDomainAuthor() throws Exception {
        idata.setVariable("securitydomain.name.variable", "testSecurityDomain");
        idata.setVariable("securitydomain.cachetype.variable", "None");

        idata.setVariable("securitydomain.add.authen.count.variable", "0");
        idata.setVariable("securitydomain.add.mapping.count.variable", "0");

        idata.setVariable("securitydomain.add.author.count.variable", "3");
        idata.setVariable("securitydomain.add.author.left.0.variable", "Delegating");
        idata.setVariable("securitydomain.add.author.middle.0.variable", "Requisite");
        idata.setVariable("securitydomain.add.author.right.0.variable", "pizza=unhealthy");

        assertTrue(SecurityDomain.run(mockAbstractUIProcessHandler, new String[]{"--xml-file=standalone.xml"}));
        Elements result = TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR +
                "standalone.xml", "security-domains > security-domain[name=testSecurityDomain] > authorization > policy-module");

        assertEquals("Delegating", result.attr("code"));
        assertEquals("requisite", result.attr("flag"));
        assertEquals("pizza", result.get(0).child(0).attr("name"));
        assertEquals("unhealthy", result.get(0).child(0).attr("value"));
    }

}
