package com.redhat.installer.tests.layering.validator;

import com.redhat.installer.framework.constants.TestPaths;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.layering.validator.HornetQValidator;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for the HornetQValidator
 * Created by thauser on 2/27/14.
 */
public class HornetQValidatorTest extends DataValidatorTester
{
    private static final String resourcePath = "/hornetq-serverTest/";
    private static final String errorMessage = "The following descriptors contain incompatible hornetq-server configuration: %s. Please choose a different installation path.";
    private static final String warningMessage = "The following descriptors already contain hornetq-server configuration: %s. Errors could occur. Would you like to continue?";

    @Before
    public void setUp(){
        idata.setVariable("INSTALL_PATH", tempFolder.getRoot().getAbsolutePath());
        dv = new HornetQValidator();
    }

    @Test
    public void testNoConflicts() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "no-conflicting.xml", "/standalone/configuration/standalone.xml");
        assertStatusOk();
    }

    @Test
    public void testNoConfig() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "no-config.xml", "/standalone/configuration/standalone.xml");
        assertStatusOk();
    }

    @Test
    public void testWarnings() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible.xml", "/standalone/configuration/standalone.xml");
        assertStatusWarning();
        assertEquals(String.format(warningMessage, "standalone.xml"), dv.getFormattedMessage());
        assertEquals("true",idata.getVariable("standalone.xml.hornetq.exists"));
    }

    @Test
    public void testMultiWarnings() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible.xml", "/standalone/configuration/standalone.xml");
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible.xml", "/standalone/configuration/standalone-ha.xml");
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible.xml", "/standalone/configuration/standalone-full.xml");
        assertStatusWarning();
        assertEquals(String.format(warningMessage, "standalone.xml standalone-ha.xml standalone-full.xml"), dv.getFormattedMessage());
        assertEquals("true",idata.getVariable("standalone.xml.hornetq.exists"));
        assertEquals("true",idata.getVariable("standalone-ha.xml.hornetq.exists"));
        assertEquals("true",idata.getVariable("standalone-full.xml.hornetq.exists"));
    }

    @Test
    public void testErrors() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "incompatible.xml", "/standalone/configuration/standalone.xml");
        assertStatusError();
        assertEquals(String.format(errorMessage, "standalone.xml"), dv.getFormattedMessage());
    }

    @Test
    public void testMultiErrors() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "incompatible.xml", "/standalone/configuration/standalone.xml");
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "incompatible.xml", "/standalone/configuration/standalone-ha.xml");
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "incompatible.xml", "/standalone/configuration/standalone-full.xml");
        assertStatusError();
        assertEquals(String.format(errorMessage, "standalone.xml standalone-ha.xml standalone-full.xml"), dv.getFormattedMessage());
    }

    @Test
    public void testMoreThanOneConfig() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible-gt1.xml", "/standalone/configuration/standalone.xml");
        assertStatusWarning();
        assertEquals(String.format(warningMessage, "standalone.xml"), dv.getFormattedMessage());
        assertEquals( "true", idata.getVariable("standalone.xml.hornetq.exists"));
    }

    @Test public void testDomainNoConfig() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "no-config.xml", "/domain/configuration/domain.xml");
        assertStatusOk();
    }
    @Test 
    public void testDomainWarnings() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible-domain.xml", "/domain/configuration/domain.xml");
        assertStatusWarning();
        assertEquals(String.format(warningMessage, "domain.xml"), dv.getFormattedMessage());
        assertEquals("true", idata.getVariable("domain.xml.hornetq.exists"));
    }

    @Test
    public void testDomainNoConflicts() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "no-conflicting.xml", "/domain/configuration/domain.xml");
        assertStatusOk();
    }

    @Test
    public void testDomainErrors() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "incompatible-domain.xml", "/domain/configuration/domain.xml");
        assertStatusError();
        assertEquals(String.format(errorMessage, "domain.xml"), dv.getFormattedMessage());
    }

    @Test
    public void testDomainMultiWarnings() throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "compatible-domain-gt1.xml", "/domain/configuration/domain.xml");
        assertStatusWarning();
        assertEquals(String.format(warningMessage, "domain.xml"), dv.getFormattedMessage());
    }

    @Test
    public void testAdditionalConfig () throws Exception {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "additional.xml", "/standalone/configuration/standalone.xml");
        assertStatusWarning();
        assertEquals(String.format(warningMessage, "standalone.xml"), dv.getFormattedMessage());
        assertEquals("true",idata.getVariable("standalone.xml.hornetq.exists"));
    }
}
