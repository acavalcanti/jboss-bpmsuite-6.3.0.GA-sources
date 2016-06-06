package com.redhat.installer.asconfiguration.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Created by thauser on 8/19/15.
 */
public class SupplementaryToolsPackSelect implements PanelAction {
    private static AutomatedInstallData idata;
    private static final String TOOL_IDS = "supplementary.tools.pack.ids";
    private static final String CONFIG_CONDITION = "supplementary.tools.condition";

    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {
        idata = adata;
        String[] ids = idata.getVariable(TOOL_IDS).split(",");
        if (clusterConfigSelected()){
            for (String id : ids){
                adata.addPackToSelected(id);
            }
        } else {
            for (String id : ids){
                adata.removePackFromSelected(id);
            }
        }
    }

    private boolean clusterConfigSelected() {
        return idata.getRules().isConditionTrue(idata.getVariable(CONFIG_CONDITION));
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {

    }
}
