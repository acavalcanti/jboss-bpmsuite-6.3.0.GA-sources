package com.redhat.installer.asconfiguration.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Small class that sets the dblogging.driver variable to the correct value.
 * Necessary because of IzPacks combobox implementation not able to perform
 * variable substitution on value fields. This is separate because the
 * variable needs to be set only after validation has passed (the user clicked next
 * and the panel will advance.)
 * Created by thauser on 7/10/14.
 */
public class DatabaseLoggingDriverSetter implements PanelAction {
    private final String VAR = "dblogging.driver";

    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {

        String dbDriver = adata.getVariable(VAR);

        if (dbDriver.equals("installed")){
            adata.setVariable(VAR, adata.getVariable("jdbc.driver.name"));
        }

    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {
        // no need
    }
}
