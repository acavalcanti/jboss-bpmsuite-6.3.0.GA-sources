package com.redhat.installer.asconfiguration.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

import java.io.File;
/**
 * Created by eunderhi on 25/05/15.
 * Sets jboss.cli.config to jboss-cli.xml in the system variables.
 * This is done in order to stop the WARN: can't find jboss-cli.xml warning message
 * on Windows.
 */
public class SetJBossCliConfig implements PanelAction{

    private static final String JBOSS_XML_CONFIG = "jboss.cli.config";
    private static final String JBOSS_CLI_FILE = "jboss-cli.xml";

    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {
        System.setProperty(JBOSS_XML_CONFIG, pathToCliConfig(adata));
    }
    public String pathToCliConfig(AutomatedInstallData adata) {
        String path = new File(adata.getInstallPath()+"/bin", JBOSS_CLI_FILE).getPath();
        return path;
    }

    //No initialization
    @Override
    public void initialize(PanelActionConfiguration configuration) {

    }
}
