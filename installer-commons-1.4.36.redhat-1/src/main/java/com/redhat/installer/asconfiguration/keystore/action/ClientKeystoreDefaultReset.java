package com.redhat.installer.asconfiguration.keystore.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * This class is needed in order to ensure strange results don't occur when the user goes
 * forward and back
 *
 * Created by thauser on 7/30/14.
 */
public class ClientKeystoreDefaultReset implements PanelAction {
    private static final String LOC_VAR = "generated.keystores.client.location";
    private static final String LOC_DEFAULT_VAR = "generated.keystores.client.location.default";
    private static final String RADIO_VAR = "generateClientKeystores";
    @Override
    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler) {
        // like the PostInstallDefaultReset, we do not run during automatic installs
        if (idata.getVariable("installerMode").equals("AUTO")){
            return;
        }

        String var = idata.getVariable(RADIO_VAR);

        boolean generateKeystores = (var == null) || Boolean.parseBoolean(var);

        if (generateKeystores){
            String defaultLoc = idata.getVariable(LOC_DEFAULT_VAR);
            if (defaultLoc != null) {
                idata.setVariable(LOC_VAR, defaultLoc);
            }
        }
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {}
}
