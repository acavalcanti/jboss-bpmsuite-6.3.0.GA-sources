package com.redhat.installer.asconfiguration.datasource.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

/**
 * Created by thauser on 8/19/15.
 */
public class SetBxmsDatasourcePanelIds implements PanelAction{

    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler) {
        boolean businessCentral = adata.getRules().isConditionTrue("install.businessCentral.datasource");
        boolean dashboard = adata.getRules().isConditionTrue("install.dashbuilder.datasource");
        String results = "";
        if (businessCentral) {
            results = "BusinessCentralDatasource";
        }
        if (dashboard) {
            if (results.isEmpty()){
                results = "DashbuilderDatasource";
            } else {
                results += ",DashbuilderDatasource";
            }
        }
        adata.setVariable("datasourcePanelIDs", results);
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {

    }
}
