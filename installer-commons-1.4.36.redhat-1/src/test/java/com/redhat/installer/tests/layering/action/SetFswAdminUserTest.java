package com.redhat.installer.tests.layering.action;

import com.redhat.installer.layering.action.SetFswAdminUser;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SetFswAdminUserTest extends PanelActionTester
{
    private final String resourcePath = "action/setFswAdminUserTest/";
    private String installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException
    {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        installationDirectory = idata.getInstallPath() + File.separator;
        panelAction = new SetFswAdminUser();
    }

    @Test
    public void existingFsw()
    {
        System.out.println(installationDirectory);
        MockResourceBuilder.copyResource(resourcePath + "application-roles.properties.fsw",
                installationDirectory + "standalone/configuration/application-roles.properties");
        MockResourceBuilder.copyResource(resourcePath + "application-users.properties.fsw",
                installationDirectory + "standalone/configuration/application-users.properties");

        panelAction.executeAction(idata, handler);

        assertEquals("existingFsw | fsw.user", "testAdmin", idata.getVariable("fsw.user"));
        assertEquals("existingFsw | fsw.user.exists", "true", idata.getVariable("fsw.user.exists"));
    }


    @Test
    public void nonExistingFsw()
    {
        System.out.println(installationDirectory);
        MockResourceBuilder.copyResource(resourcePath + "application-roles.properties.no.fsw",
                installationDirectory + "standalone/configuration/application-roles.properties");
        MockResourceBuilder.copyResource(resourcePath + "application-users.properties.no.fsw",
                installationDirectory + "standalone/configuration/application-users.properties");

        panelAction.executeAction(idata, handler);

        assertEquals("existingFsw | fsw.user", "fswAdmin", idata.getVariable("fsw.user"));
        assertEquals("existingFsw | fsw.user.exists", "false", idata.getVariable("fsw.user.exists"));
    }

}
