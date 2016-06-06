package com.redhat.installer.tests.layering.action;

import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.PanelActionTester;
import com.redhat.installer.layering.action.PreExistingSetter;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/5/14.
 */
public class PreExistingSetterTest extends PanelActionTester {
    private final String resourcePath = "action/preExistingSetterTest/";
    private String installationDirectory;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();


    @Before
    public void before() throws Exception {
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        installationDirectory = idata.getInstallPath() + File.separator;
        panelAction = new PreExistingSetter() {

            @Override
            protected void setDefaults(String xml, Document doc) {
                Elements testElements = doc.select("these > are > test1");
                Elements testElements2 = doc.select("these > are > test2");
                if (testElements.size() > 0) {
                    idata.setVariable("preexisting.var1", "setvar1");
                }

                if (testElements2.size() > 0) {
                    idata.setVariable("preexisting.var2", "setvar2");
                }
            }

            @Override
            protected void resetDefaults() {
                idata.setVariable("preexisting.var1", "default1");
                idata.setVariable("preexisting.var2", "default2");
            }
        };
    }

    @Test
    public void testSetBothVars(){
        MockResourceBuilder.copyConfigFiles("setter", installationDirectory, resourcePath);
        panelAction.executeAction(idata, handler);
        assertEquals("setvar1", idata.getVariable("preexisting.var1"));
        assertEquals("setvar2", idata.getVariable("preexisting.var2"));
    }

    @Test
    public void testSetNoVars(){
        panelAction.executeAction(idata, handler);
        assertEquals("default1", idata.getVariable("preexisting.var1"));
        assertEquals("default2", idata.getVariable("preexisting.var2"));
    }




}
