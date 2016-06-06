package com.redhat.installer.tests.ports.utils;

import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.InstallDataTester;
import com.redhat.installer.ports.utils.PortReader;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by eunderhi on 25/06/15.
 * Testing PortReader class to see if it correctly parses server xml config file.
 * Ports used in the assert equals portion are completely arbitrary, they can be set
 * to anything as long as they are set to the same thing in resources/portReaderTest
 */
public class PortReaderTest extends InstallDataTester{

    public static final String DOMAIN_RESOURCES_PATH = "/portReaderTest/host.xml";
    public static final String STANDALONE_RESOURCES_PATH = "/portReaderTest/standalone.xml";
    public static final String STANDALONE_FULL_HA_RESOURCES_PATH = "/portReaderTest/standalone-full-ha.xml";
    public static final String DOMAIN_CONFIG_PATH = "/domain/configuration/host.xml";
    public static final String STANDALONE_CONFIG_PATH = "/standalone/configuration/standalone.xml";
    public static final String STANDALONE_FULL_HA_CONFIG_PATH= "/standalone/configuration/standalone-full-ha.xml";

    @Test
    public void domainConfigTest() {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, DOMAIN_RESOURCES_PATH,
                                                              DOMAIN_CONFIG_PATH);
        assertEquals(PortReader.getManagementPort(DOMAIN_CONFIG_PATH), 11111);
    }
    @Test
    public void standaloneConfigTest() {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, STANDALONE_RESOURCES_PATH,
                                                              STANDALONE_CONFIG_PATH);
        assertEquals(PortReader.getManagementPort(STANDALONE_CONFIG_PATH), 3113);
    }
    @Test
    public void standaloneFullHaConfigTest() {
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, STANDALONE_FULL_HA_RESOURCES_PATH,
                                                              STANDALONE_FULL_HA_CONFIG_PATH);
        assertEquals(PortReader.getManagementPort(STANDALONE_FULL_HA_CONFIG_PATH), 2352);
    }
}
