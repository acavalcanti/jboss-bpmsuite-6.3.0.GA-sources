package com.redhat.installer.tests.layering.action;

import com.redhat.installer.layering.action.SetCollisionsDefault;
import com.redhat.installer.framework.constants.TestPaths;
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

/**
 * Created by thauser on 9/3/14.
 */
public class SetCollisionsDefaultTest extends PanelActionTester{

    private final String resourcePath = "action/setCollisionsDefaultTest/";
    private String installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        installationDirectory = idata.getInstallPath() + File.separator;
        MockResourceBuilder.copyConfigFiles("collisions", installationDirectory, resourcePath);
        panelAction = new SetCollisionsDefault();
    }

    @Test
    public void testStandaloneExtensionCollision(){
        String ext = "org.jboss.as.jmx";
        idata.setVariable("colliding.extensions",ext);
        panelAction.executeAction(idata, handler);
        checkVariableValues(ext, "extension", TRUE);
    }

    @Test
    public void testNoExtensionCollision(){
        String ext = "no-collision";
        idata.setVariable("colliding.extensions", ext);
        panelAction.executeAction(idata,handler);
        checkVariableValues(ext, "extension", FALSE);
    }

    @Test
    public void testStandaloneSubsystemCollision(){
        String sub = "urn:jboss:domain:logging:1.2";
        idata.setVariable("colliding.subsystems", sub);
        panelAction.executeAction(idata, handler);
        checkVariableValues(sub, "subsystem", TRUE);
    }

    @Test
    public void testNoSubsystemCollision(){
        String sub = "no-collision";
        idata.setVariable("colliding.subsystems", sub);
        panelAction.executeAction(idata,handler);
        checkVariableValues(sub, "subsystem", FALSE);
    }

    private void checkVariableValues(String s, String type, String check) {
        String query = "%s."+s+"."+type+".exists";
        for (String config : TestPaths.standaloneDescriptors){
            assertEquals(check, idata.getVariable(String.format(query,config)));
        }
    }
}
