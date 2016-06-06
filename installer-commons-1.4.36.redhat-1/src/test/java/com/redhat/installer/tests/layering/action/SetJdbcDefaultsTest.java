package com.redhat.installer.tests.layering.action;

import com.redhat.installer.layering.action.SetJdbcDefaults;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static junit.framework.TestCase.assertEquals;

public class SetJdbcDefaultsTest extends PanelActionTester
{
    private final String[] jdbcVariables = { "jdbc.driver.preexisting" };
    private final String[] jdbcDependentVariables = { "eap.needs.install" };
    private final String[] jarPaths = {
            "modules/", "modules/system/layers/base/", "modules/system/layers/sramp/",
            "modules/system/layers/soa/", "modules/system/layers/dv/", "modules/system/layers/brms/", "modules/system/layers/bpms/"
    };

    private HashSet<File> moduleSubdirs = new HashSet<File>();

    private final String resourcePath = "action/setJdbcDefaultsTest/";
    private String installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException
    {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        installationDirectory = idata.getInstallPath() + File.separator;
        panelAction = new SetJdbcDefaults();
    }

    @Test
    public void postgres()
    {
        MockResourceBuilder.copyConfigFiles("postgres", installationDirectory, resourcePath);
        String postgresPath = "org/postgresql/main/";
        String postgresFile = "postgresql.jar";

        for (String jarPath : jarPaths)
        {
            String source = resourcePath + postgresFile;
            String destination = installationDirectory + jarPath + postgresPath + postgresFile;

            panelAction.executeAction(idata, handler);
            assertEquals("postgresJar | no jar | " + destination + " | jdbc.driver.preexisting", "false", idata.getVariable("jdbc.driver.preexisting"));

            MockResourceBuilder.copyResource(source, destination);

            panelAction.executeAction(idata, handler);
            assertEquals("postgresJar | jar | " + destination + " | jdbc.driver.preexisting", "true", idata.getVariable("jdbc.driver.preexisting"));

            MockResourceBuilder.deleteFile(destination);
        }
    }
}
