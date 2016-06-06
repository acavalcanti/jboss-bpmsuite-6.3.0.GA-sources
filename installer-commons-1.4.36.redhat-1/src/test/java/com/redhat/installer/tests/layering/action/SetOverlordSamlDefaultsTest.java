package com.redhat.installer.tests.layering.action;

import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.layering.action.SetOverlordSamlDefaults;
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

public class SetOverlordSamlDefaultsTest extends PanelActionTester
{
    private AbstractUIHandler handler;
    private String installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException
    {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        handler = new MockAbstractUIProcessHandler();
        installationDirectory = idata.getInstallPath() + File.separator;
        panelAction = new SetOverlordSamlDefaults();
    }

    @Test
    public void existingSaml()
    {
        String samlKeystore = installationDirectory + "standalone/configuration/overlord-saml.keystore";
        MockResourceBuilder.createFile(samlKeystore);

        panelAction.executeAction(idata, handler);

        assertEquals("existingSaml | saml.keystore.pre.existing", "true",
                idata.getVariable("saml.keystore.pre.existing"));
    }

    @Test
    public void nonExistingSaml()
    {
        String samlKeystore = installationDirectory + "standalone/configuration/overlord-saml.keystore";

        panelAction.executeAction(idata, handler);

        assertEquals("existingSaml | saml.keystore.pre.existing", "false",
                idata.getVariable("saml.keystore.pre.existing"));
    }

}
