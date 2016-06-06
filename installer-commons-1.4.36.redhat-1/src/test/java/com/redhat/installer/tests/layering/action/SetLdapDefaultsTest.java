package com.redhat.installer.tests.layering.action;

import com.redhat.installer.framework.constants.TestPaths;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.PanelActionTester;
import com.redhat.installer.layering.action.SetLdapDefaults;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SetLdapDefaultsTest extends PanelActionTester
{
    private final String resourcePath = "action/setLdapDefaultsTest/";
    private String installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();


    @Before
    public void setUp() throws IOException
    {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        installationDirectory = idata.getInstallPath() + File.separator;
        panelAction = new SetLdapDefaults();
    }

    @Test
    public void blankTest() throws IOException
    {
        panelAction.executeAction(idata, handler);
        for (String descriptor : TestPaths.descriptors)
        {
            assertEquals("blankTest | " + descriptor + ".pre.existing.ldap",
                    "false",
                    idata.getVariable(descriptor + ".pre.existing.ldap"));
        }
    }

    /**
     * Test the ldap pre-existing variables have been set appropriately
     * Notice that domain.xml does not configure ldap
     * @throws IOException
     */
    @Test
    public void ldapDefaultsTest() throws IOException
    {
        MockResourceBuilder.copyConfigFiles("ldap", installationDirectory, resourcePath);
        panelAction.executeAction(idata, handler);
        for (String standaloneDescriptor : TestPaths.standaloneDescriptors)
        {
            assertEquals("ldapDefaultsTest | " + standaloneDescriptor + ".pre.existing.ldap",
                    "true",
                    idata.getVariable(standaloneDescriptor + ".pre.existing.ldap"));
        }
        assertEquals("ldapDefaultsTest | " + "host.xml.pre.existing.ldap",
                "true",
                idata.getVariable("host.xml.pre.existing.ldap"));

        assertEquals("ldapDefaultsTest | " + "domain.xml.pre.existing.ldap",
                "false",
                idata.getVariable("domain.xml.pre.existing.ldap"));
    }
}
