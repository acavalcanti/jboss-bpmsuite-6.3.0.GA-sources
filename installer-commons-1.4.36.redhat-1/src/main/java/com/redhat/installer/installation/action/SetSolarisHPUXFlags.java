package com.redhat.installer.installation.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.StringTool;

/**
 * Created by aabulawi on 01/08/14.
 */
public class SetSolarisHPUXFlags implements PanelAction {
    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {
        String OS = adata.getVariable("SYSTEM_os_name");
        String bits = System.getProperty("sun.arch.data.model");
        if (bits != null && bits.equals("64")) {
            if (StringTool.startsWithIgnoreCase(OS, "SunOS") || StringTool.startsWithIgnoreCase(OS, "HP-UX") || StringTool.startsWithIgnoreCase(OS, "Solaris")) {
                adata.setVariable("add.bits.to.configs", "true");
            }
        }
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {
    }
}
