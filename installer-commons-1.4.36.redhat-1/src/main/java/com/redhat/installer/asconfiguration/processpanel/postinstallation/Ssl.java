package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

public class Ssl extends PostInstallation {

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        mHandler = handler;
        serverCommands = initServerCommands(Ssl.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installSsl();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    /**
     * Passes the gathered IncludeSSLAndLDAP information (keystore location and password) to
     * serverCommands. ServerCommands adds the IncludeSSLAndLDAP configuration to the
     * descriptor
     */
    static List<ModelNode> installSsl() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String keystore = idata.getVariable("ssl.path");
        String keystorePassword = idata.getVariable("ssl.password");
        List<ModelNode> results = new ArrayList<ModelNode>();
        results.addAll(serverCommands.installSsl(keystore, keystorePassword, "ManagementRealm"));
        results.addAll(serverCommands.addHttps("ManagementRealm"));
        return results;
    }
}
