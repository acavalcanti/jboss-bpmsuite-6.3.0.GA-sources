package com.redhat.installer.tests.layering.action;

import com.redhat.installer.layering.action.SetSslDefaults;
import com.redhat.installer.framework.constants.TestPaths;
import com.redhat.installer.framework.mock.MockAbstractUIProcessHandler;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class SetSslDefaultsTest extends PanelActionTester
{
    private final String resourcePath = "action/setSslDefaultsTest/";
    private String standaloneDirectory, domainDirectory, installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();


    @Before
    public void setUp() throws IOException
    {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        handler = new MockAbstractUIProcessHandler();
        installationDirectory = idata.getInstallPath() + File.separator;
        standaloneDirectory = installationDirectory + "standalone" + File.separator + "configuration" + File.separator;
        domainDirectory = installationDirectory + "standalone" + File.separator + "configuration" + File.separator;
        panelAction = new SetSslDefaults();
    }

    /**
     * Note that the domain.xml will never have ssl configured
     */
    @Test
    public void sslInstalled()
    {
        MockResourceBuilder.copyConfigFiles("ssl", installationDirectory, resourcePath);
        panelAction.executeAction(idata, handler);

        for (String standaloneDescriptor : TestPaths.standaloneDescriptors)
        {
            assertEquals("sslInstalled | " + standaloneDescriptor + ".pre.existing.ssl", "true", idata.getVariable(standaloneDescriptor + ".pre.existing.ssl"));
        }

        assertEquals("sslInstalled | " + "host.xml.pre.existing.ssl", "true", idata.getVariable("host.xml.pre.existing.ssl"));
        assertEquals("sslInstalled | " + "domain.xml.pre.existing.ssl", "false", idata.getVariable("domain.xml.pre.existing.ssl"));

    }

    @Test
    public void sslNotInstalled()
    {
        MockResourceBuilder.copyConfigFiles("no.ssl", installationDirectory, resourcePath);
        panelAction.executeAction(idata, handler);

        for (String descriptor : TestPaths.descriptors)
        {
            assertEquals("sslNotInstalled | " + descriptor + ".pre.existing.ssl", "false", idata.getVariable(descriptor + ".pre.existing.ssl"));
        }
    }
}
