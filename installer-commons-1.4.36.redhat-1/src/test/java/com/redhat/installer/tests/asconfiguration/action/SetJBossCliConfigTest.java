package com.redhat.installer.tests.asconfiguration.action;

import com.redhat.installer.asconfiguration.action.SetJBossCliConfig;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Test;
import java.io.File;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by eunderhi on 26/05/15.
 */

public class SetJBossCliConfigTest extends PanelActionTester{
    private static final String JBOSS_CLI_FILE = "jboss-cli.xml";
    private static final String JBOSS_XML_CONFIG = "jboss.cli.config";

    @Before
    public void setup() {
       panelAction = new SetJBossCliConfig();
    }
    @Test
    public void testSetSystemVariable() {
        panelAction.executeAction(idata, handler);
        String path = new File(idata.getInstallPath()+"/bin", JBOSS_CLI_FILE).getPath();

        assertEquals(System.getProperty(JBOSS_XML_CONFIG), path);
    }

}
