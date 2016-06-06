package com.redhat.installer.tests.layering.action;

import com.redhat.installer.layering.action.SetVaultDefaults;
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

import static org.junit.Assert.assertEquals;

public class SetVaultDefaultsTest extends PanelActionTester
{
    private final String[] vaultVariables = { "vault.keystoreloc", "vault.encrdir", "vault.preexisting" };
    private final String resourcePath = "action/setVaultDefaultsTest/";
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
        panelAction = new SetVaultDefaults();

        //Variables that should be set in variables.xml
        idata.setVariable("vault.keystoreloc.default", "default_keystore_loc");
        idata.setVariable("vault.encrdir.default", "default_encrdir");
    }

    @Test
    public void blankTest()
    {
        panelAction.executeAction(idata, handler);
        assertEquals("blankTest | vault.preexisting", "false", idata.getVariable("vault.preexisting"));
        assertEquals("blankTest | vault.keystoreloc.default", "default_keystore_loc", idata.getVariable("vault.keystoreloc.default"));
        assertEquals("blankTest | vault.encrdir.default", "default_encrdir", idata.getVariable("vault.encrdir.default"));
    }

    /**
     * Ensure that a vault is detected in all desciptors.
     * Note that domain.xml is not included because the vault is not configured in this file
     */
    @Test
    public void vaultTest()
    {
        MockResourceBuilder.copyConfigFiles("vault", installationDirectory, resourcePath);
        panelAction.executeAction(idata, handler);

        for (String standaloneDescriptor : TestPaths.standaloneDescriptors)
        {
            assertEquals("vaultTest | " + standaloneDescriptor + ".vault.preexisting",
                    "true",
                    idata.getVariable(standaloneDescriptor + ".vault.preexisting"));
        }
        assertEquals("vaultTest | host.xml.vault.preexisting", "true", idata.getVariable("host.xml.vault.preexisting"));

        assertEquals("vaultTest | vault.preexisting", "true", idata.getVariable("vault.preexisting"));
        assertEquals("vaultTest | vault.keystoreloc", "/home/yyz/mtjandra/f/brms_vault/vault.keystore", idata.getVariable("vault.keystoreloc"));
        assertEquals("vaultTest | vault.encrdir", "/home/yyz/mtjandra/f/brms_vault/vault/", idata.getVariable("vault.encrdir"));
        assertEquals("vaultTest | vault.alias", "vault", idata.getVariable("vault.alias"));
        assertEquals("vaultTest | vault.itercount", "44", idata.getVariable("vault.itercount"));

    }
}
