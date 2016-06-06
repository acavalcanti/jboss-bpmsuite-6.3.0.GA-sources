package com.redhat.installer.tests.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.layering.action.SetSecurityDefaults;
import com.redhat.installer.framework.constants.CommonStrings;
import com.redhat.installer.framework.mock.MockAbstractUIProcessHandler;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class SetSecurityDefaultsTest extends PanelActionTester implements CommonStrings
{
    private final String[] securityVariables = {
            "jvm.unix.has.policy", "jvm.windows.has.policy", "jvm.unix.policy.commented",
            "jvm.windows.policy.commented", "jvm.unix.security.policy", "jvm.windows.security.policy",
            "jvm.unix.kie.policy", "jvm.windows.kie.policy", "jvm.unix.rtgov.policy", "jvm.windows.rtgov.policy"
    };
    private final String[] securityDependentVariables = { "eap.needs.install",
            "vault.keystoreloc.default", "vault.encrdir.default" };

    private AbstractUIHandler handler;
    private String binDirectory;
    private final String resourcePath = "action/securityDefaultTest/";
    private String originalOs;

    public void setVariablesToFalse(String[] variables, AutomatedInstallData idata)
    {
        for (String variable : variables)
            idata.setVariable(variable, FALSE);
    }

    private void setPrexistingDefaults()
    {
        panelAction = new SetSecurityDefaults();
        panelAction.executeAction(idata, handler);
    }

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException
    {
        //PreExistingDefaults is based on layering, so create directory structure of existing installation
        originalOs = System.getProperty("os.name");
        MockInstallerStructure.createTargetPath(testFolder, idata);
        MockInstallerStructure.createDefaultDirectories(testFolder, idata);

        setVariablesToFalse(securityVariables, idata);
        setVariablesToFalse(securityDependentVariables, idata);

        handler = new MockAbstractUIProcessHandler();
        binDirectory = idata.getInstallPath() + File.separator + "bin" + File.separator;
    }

    @After
    public void resetOs(){
        System.setProperty("os.name", originalOs);
    }

    @Test
    public void rtgovKieSecurityUnix()
    {
        String[] trueVariables = {
            "jvm.unix.has.policy", "jvm.unix.security.policy",
            "jvm.unix.kie.policy", "jvm.unix.rtgov.policy",
        };

        MockResourceBuilder.copyResource(resourcePath + "rtgovKie.conf", binDirectory + "standalone.conf");

        setPrexistingDefaults();

        verifyVariables("rtgovKie", securityVariables, trueVariables);
    }

    @Test
    public void rtgovKieSecurityWindows()
    {
        System.setProperty("os.name", "Windows 95");
        String[] trueVariables = {
                "jvm.windows.has.policy", "jvm.windows.security.policy",
                "jvm.windows.kie.policy", "jvm.windows.rtgov.policy",
        };

        MockResourceBuilder.copyResource(resourcePath + "rtgovKie.conf.bat", binDirectory + "standalone.conf.bat");

        setPrexistingDefaults();

        verifyVariables("rtgovKie", securityVariables, trueVariables);
    }


    @Test
    public void kieSecurity()
    {
        String[] trueVariables = {
            "jvm.unix.has.policy",
            "jvm.unix.security.policy",
            "jvm.unix.kie.policy"
        };

        MockResourceBuilder.copyResource(resourcePath + "kie.conf", binDirectory + "standalone.conf");

        setPrexistingDefaults();

        verifyVariables("kieSecure", securityVariables, trueVariables);
    }


    @Test
    public void rtgovKieCommented()
    {
        MockResourceBuilder.copyResource(resourcePath + "rtgovKieCommented.conf", binDirectory + "standalone.conf");

        String[] trueVariables = {
                "jvm.unix.has.policy", "jvm.unix.security.policy",
                "jvm.unix.kie.policy", "jvm.unix.rtgov.policy",
                "jvm.unix.policy.commented"
        };

        setPrexistingDefaults();

        verifyVariables("rtgovKieCommented", trueVariables, securityVariables);
    }

    @Test
    public void freshInstall()
    {
        idata.setVariable("eap.needs.install", TRUE);

        setPrexistingDefaults();

        verifyFalseVariables("freshInstall", securityVariables);
    }
}
