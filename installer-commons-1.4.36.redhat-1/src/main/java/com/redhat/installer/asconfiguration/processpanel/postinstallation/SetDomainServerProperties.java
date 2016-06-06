package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.jboss.dmr.ModelNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thauser on 7/8/15.
 */
public class SetDomainServerProperties extends PostInstallation{
    public static boolean run (AbstractUIProcessHandler handler, String[]args){
        try {
            initPostInstallation(handler,args,SetDomainServerProperties.class);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = addServerProperties(args);
        serverCommands.terminateSession();
        return installResult(commandResults);
    }

    private static List<ModelNode> addServerProperties(String[]args) {
        List<ModelNode> results = new ArrayList<ModelNode>();
        String server = args[0];
        for (int i = 1; i < args.length; i++){
            String[] propSplit = args[i].split("=");
            String property = propSplit[0];
            String value = propSplit[1].replace('\\','/');
            results.addAll(serverCommands.addPropertyToIndividualServer(server, property, value, false));
        }
        return results;
    }
}
