package com.redhat.installer.asconfiguration.javaopts;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.layering.util.PlatformUtil;

/**
 * Created by aabulawi on 12/06/15.
 */
public class SetJavaOptsDefaults implements PanelAction {
    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {
        if (PlatformUtil.isWindows()){
            adata.setVariable("javaopts.standalone", "-Xms1G -Xmx1G -XX:MaxPermSize=256M -Djava.net.preferIPv4Stack=true -Djboss.modules.policy-permissions=true -Djboss.modules.system.pkgs=org.jboss.byteman");
            adata.setVariable("javaopts.domain", "-Xms64M -Xmx512M -XX:MaxPermSize=256M -Djava.net.preferIPv4Stack=true -Djboss.modules.policy-permissions=true -Djboss.modules.system.pkgs=org.jboss.byteman");
        } else {
            adata.setVariable("javaopts.standalone", "-Xms1303m -Xmx1303m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=$JBOSS_MODULES_SYSTEM_PKGS -Djava.awt.headless=true -Djboss.modules.policy-permissions=true");
            adata.setVariable("javaopts.domain", "-Xms64m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=$JBOSS_MODULES_SYSTEM_PKGS -Djava.awt.headless=true -Djboss.modules.policy-permissions=true");
        }
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {

    }
}
