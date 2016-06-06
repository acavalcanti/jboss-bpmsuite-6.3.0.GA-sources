package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thauser on 7/29/15.
 */
public class AddQuartzDatasources extends PostInstallation{
    public static boolean run(AbstractUIProcessHandler handler, String[]args){
        mHandler = handler;
        serverCommands = initServerCommands(AddQuartzDatasources.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installQuartzDatasources();
        serverCommands.terminateSession();
        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    private static List<ModelNode> installQuartzDatasources() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        List<ModelNode> results = new ArrayList<ModelNode>();
        String quartzJdbcName = idata.getVariable("jdbc.driver.name");
        String quartzUsername = idata.getVariable("quartz.db.username");
        String quartzPassword = idata.getVariable("quartz.db.password");
        String quartzUrl = idata.getVariable("quartz.db.url");
        results.addAll(serverCommands.installDatasourceUsernamePwd("quartzDs", "java:jboss/datasources/quartzDs",quartzJdbcName,quartzUrl, "0", "20",quartzUsername, quartzPassword, null));
        results.addAll(serverCommands.installDatasourceUsernamePwd("quartzUnmanagedDs", "java:jboss/datasources/quartzUnmanagedDs",quartzJdbcName,quartzUrl, "0", "20",quartzUsername, quartzPassword, "false"));
        return results;
    }
}
