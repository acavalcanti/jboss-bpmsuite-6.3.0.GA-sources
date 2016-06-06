package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;

import java.io.*;

public class SetSecurityDefaults implements PanelAction {
    private AutomatedInstallData idata;

    @Override
    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler) {
        this.idata = idata;
        boolean needInstall = Boolean.parseBoolean(idata.getVariable("eap.needs.install"));

        resetDefaults();

        if (needInstall) {
            return;
        } else {
            BufferedReader reader = null;
            FileInputStream fis = null;
            String line = null;


            String[] standaloneConf;
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                standaloneConf = new String[]{idata.getInstallPath() + "/bin/standalone.conf.bat"};
            } else {
                standaloneConf = new String[]{(idata.getInstallPath() + "/bin/standalone.conf")};
            }
            ;

            String hasPolicy = "^#?[ ]*JAVA_OPTS=\"\\$JAVA_OPTS -Djava.security.manager.*";
            String policyCommented = "^#[ ]*JAVA_OPTS=\"\\$JAVA_OPTS -Djava.security.manager.*";
            String securityPolicy = "^#?[ ]*JAVA_OPTS=\"\\$JAVA_OPTS.*-Djava.security.manager.*security\\.policy.*";
            String rtGovPolicy = "^#?[ ]*JAVA_OPTS=\"\\$JAVA_OPTS.*-Djava.security.manager.*rtgov\\.policy.*";
            String kiePolicy = "^#?[ ]*JAVA_OPTS=\"\\$JAVA_OPTS.*-Djava.security.manager.*kie\\.policy.*";

            String windowsHasPolicy = "^(rem #)?[ ]*set[ ]*\"SECURITY_OPTS=-Djava.security.manager.*";
            String windowsPolicyCommented = "^rem #[ ]*set[ ]*\"SECURITY_OPTS=-Djava.security.manager.*";
            String windowsSecurityPolicy = "^(rem #)?[ ]*set.*\"-Djava.security.policy=%DIRNAME%security\\.policy.*";
            String windowsRtGovPolicy = "^(rem #)?[ ]*set[ ]*\"SECURITY_OPTS=-Djava.security.manager.*rtgov\\.policy\".*";
            String windowsKiePolicy = "^(rem #)?[ ]*set[ ]*\"SECURITY_OPTS=-Djava.security.manager.*kie\\.policy\".*";

            for (String conf : standaloneConf) {

                try {
                    fis = new FileInputStream(new File(conf));
                    reader = new BufferedReader(new InputStreamReader(fis));
                    while ((line = reader.readLine()) != null) {
                        if (line.matches(hasPolicy)) idata.setVariable("jvm.unix.has.policy", "true");
                        if (line.matches(windowsHasPolicy)) idata.setVariable("jvm.windows.has.policy", "true");
                        if (line.matches(policyCommented)) idata.setVariable("jvm.unix.policy.commented", "true");
                        if (line.matches(windowsPolicyCommented))
                            idata.setVariable("jvm.windows.policy.commented", "true");
                        if (line.matches(securityPolicy)) idata.setVariable("jvm.unix.security.policy", "true");
                        if (line.matches(rtGovPolicy)) idata.setVariable("jvm.unix.rtgov.policy", "true");
                        if (line.matches(kiePolicy)) idata.setVariable("jvm.unix.kie.policy", "true");
                        if (line.matches(windowsSecurityPolicy))
                            idata.setVariable("jvm.windows.security.policy", "true");
                        if (line.matches(windowsRtGovPolicy)) idata.setVariable("jvm.windows.rtgov.policy", "true");
                        if (line.matches(windowsKiePolicy)) idata.setVariable("jvm.windows.kie.policy", "true");
                    }
                } catch (Exception e) {
                    Debug.log(e.getStackTrace());
                    return;
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {
        return;
    }

    private void resetDefaults() {
        idata.setVariable("jvm.unix.security.policy", "false");
        idata.setVariable("jvm.unix.rtgov.policy", "false");
        idata.setVariable("jvm.unix.kie.policy", "false");
        idata.setVariable("jvm.windows.security.policy", "false");
        idata.setVariable("jvm.windows.rtgov.policy", "false");
        idata.setVariable("jvm.windows.kie.policy", "false");
        idata.setVariable("jvm.unix.has.policy", "false");
        idata.setVariable("jvm.windows.has.policy", "false");
        idata.setVariable("jvm.unix.policy.commented", "false");
        idata.setVariable("jvm.windows.policy.commented", "false");
    }
}
