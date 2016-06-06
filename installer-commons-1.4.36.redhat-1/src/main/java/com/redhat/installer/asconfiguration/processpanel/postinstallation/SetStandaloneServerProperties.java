package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.util.AbstractUIProcessHandler;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 11/26/2015.
 */
public class SetStandaloneServerProperties extends PostInstallation {
    public static boolean run(AbstractUIProcessHandler handler, String[]args){
        try{
            initPostInstallation(handler, args, SetStandaloneServerProperties.class);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }

        List<ModelNode> commandResults = addStandaloneProperties(args);
        serverCommands.terminateSession();
        return installResult(commandResults);
    }

    private static List<ModelNode> addStandaloneProperties(String[]args){
        List<ModelNode> results = new ArrayList<ModelNode>();
        for (int i = 0; i < args.length; i++){
            String[] propSplit = args[i].split("=");
            String property = propSplit[0];
            String value = propSplit[1].replace('\\', '/');
            results.addAll(serverCommands.addSystemProperty(property, value));
        }
        return results;
    }

}
