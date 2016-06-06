package com.redhat.installer.tests.installation.processpanel;

import com.redhat.installer.framework.constants.CommonStrings;
import com.redhat.installer.framework.mock.MockInstallerStructure;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.installation.processpanel.TextReplacer;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static junit.framework.TestCase.assertEquals;


public class TextReplacerTest extends ProcessPanelTester implements CommonStrings
{
    private String binDirectory;
    private String standaloneConf;
    private String standaloneBat;
    private final String resourcePath = "action/securityDefaultTest/";

    @Before
    public void setUp() throws Exception
    {
        MockInstallerStructure.createTargetPath(tempFolder, idata);
        idata.langpack = TestUtils.createMockLangpack(tempFolder,"", "");
        binDirectory = idata.getInstallPath() + File.separator + "bin" + File.separator;
        standaloneConf =  binDirectory  + "standalone.conf";
        standaloneBat =  binDirectory  + "standalone.conf.bat";
    }



    @Test
    public void fswReplace() throws IOException
    {
        String unixText = "JAVA_OPTS=\"$JAVA_OPTS -Djava.security.manager \"-Djava.security.policy=$DIRNAME/security.policy\""+
                "\"-Drtgov.security.policy=$DIRNAME/rtgov.policy\" \"-Dkie.security.policy=$DIRNAME/kie.policy\"";
        String unixRegex = "^#?[ ]*JAVA_OPTS=\"\\$JAVA_OPTS -Djava.security.manager.*";

        String windowsText = "set \"SECURITY_OPTS=-Djava.security.manager \"-Djava.security.policy=%DIRNAME%security.policy\""+
                "\"-Drtgov.security.policy=%DIRNAME%rtgov.policy\" \"-Dkie.security.policy=%DIRNAME%kie.policy\"\"";
        String windowsRegex = "^(rem #)?[ ]*set[ ]*\"SECURITY_OPTS=-Djava.security.manager.*";


        String[][] files = {{"kie.conf", "kie.conf.bat"}, {"kieCommented.conf", "kieCommented.conf.bat"}};

        BufferedReader br = null;
        String line = null;
        for(String[] file : files)
        {
            MockResourceBuilder.copyResource(resourcePath + file[0], standaloneConf);
            MockResourceBuilder.copyResource(resourcePath + file[1], standaloneBat);

            TextReplacer.run(handler, new String[] {
                    "--file="+standaloneConf,
                    "--regex="+unixRegex,
                    "--text="+unixText
            });
            TextReplacer.run(handler, new String[] {
                    "--file="+standaloneBat,
                    "--regex="+windowsRegex,
                    "--text="+windowsText
            });

            br = new BufferedReader(new FileReader(standaloneConf));
            line = br.readLine();
            br.close();
            assertEquals("rtgoveKieSecure|"+file[0], unixText, line);

            br = new BufferedReader(new FileReader(standaloneBat));
            line = br.readLine();
            br.close();
            assertEquals("rtgoveKieSecure|"+file[1], windowsText, line);
        }
    }


    @Override
    public void testProcessPanelInstantiation(){}//?
}
