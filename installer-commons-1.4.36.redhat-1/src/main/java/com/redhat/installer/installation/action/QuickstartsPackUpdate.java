package com.redhat.installer.installation.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Responsible for adding/removing the quickstarts pack from the selected packs
 * list after the quickstarts panel runs, based on the user's selection.
 *
 * Necessary because the quickstarts panel is now displayed after the treepacks
 * panel.
 *
 * Created by fcanas on 4/16/14.
 */
public class QuickstartsPackUpdate implements PanelAction {
    private static final String PACK_ID = "application-platform.quickstarts";
    private static final String INSTALL_CONDITION_VARIABLE = "installQuickStarts";
    private static final String INSTALL_TRUE = "true";

    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {
        String installVar = adata.getVariable(INSTALL_CONDITION_VARIABLE);

        if (installVar != null && isTrue(installVar)) {
            adata.addPackToSelected(PACK_ID);
        } else {
            adata.removePackFromSelected(PACK_ID);
        }
    }

    /**
     * Logic to determine whether the pack in question needs to be installed or not.
     * @param installVar
     * @return
     */
    private boolean isTrue(String installVar) {
        if (installVar != null && installVar.equals(INSTALL_TRUE)) {
            return true;
        }
        return false;
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {
        // Not needed.
    }
}
