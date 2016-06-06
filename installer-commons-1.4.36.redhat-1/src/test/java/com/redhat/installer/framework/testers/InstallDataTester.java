package com.redhat.installer.framework.testers;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.tests.TestUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

/**
 * Created by thauser on 9/2/14.
 */
public class InstallDataTester {
    public static AutomatedInstallData idata;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();


    @BeforeClass
    public static void beforeClass() throws Exception {
        idata = new AutomatedInstallData();
        TestUtils.setVariableSubstitutorIdata(idata);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        TestUtils.destroyVariableSubstitutorIdata();
        TestUtils.destroyIdataSingleton();
        idata = null;
    }

    @Before
    public void beforeTest(){
        idata.setVariable("INSTALL_PATH", tempFolder.getRoot().getAbsolutePath());
        idata.setVariable("installation.logfile", TestUtils.testLogFilename);
        idata.setVariable("installerMode", "CLI");
    }

    @After
    public void afterTest() {
        idata.getVariables().clear();
    }
}
