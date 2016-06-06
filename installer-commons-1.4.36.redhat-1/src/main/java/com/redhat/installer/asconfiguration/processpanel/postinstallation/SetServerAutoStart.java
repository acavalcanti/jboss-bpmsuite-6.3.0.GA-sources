package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.util.AbstractUIProcessHandler;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thauser on 7/15/15.
 */
public class SetServerAutoStart extends PostInstallation{
    private static final String SERVER_CONFIG = "server-config";
    private static final String AUTO_START = "auto-start";

    public static boolean run (AbstractUIProcessHandler handler, String[] args){
        try {
            initPostInstallation(handler, args, SetServerAutoStart.class);
        } catch (InterruptedException e){
            e.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = setAutoStart(args);
        serverCommands.terminateSession();
        return installResult(commandResults);
    }

    private static List<ModelNode> setAutoStart(String[] args) {
        List<ModelNode> results = new ArrayList<ModelNode>();
        List<String> servers = parser.getListProperty(SERVER_CONFIG);
        boolean autoStart = Boolean.parseBoolean(parser.getStringProperty(AUTO_START));
        for (String server : servers){
            results.addAll(serverCommands.setServerAutoStart(server, autoStart));
        }
        return results;
    }
}
