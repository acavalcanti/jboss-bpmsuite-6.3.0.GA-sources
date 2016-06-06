package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * Deploys an artifact onto the currently running server
 * Created by thauser on 7/8/15.
 */
public class DeployArtifact extends PostInstallation {
    private static final String ARTIFACT_PATH = "path";

    public static boolean run(AbstractUIProcessHandler handler, String[]args){
        List<ModelNode> commandResults = null;
        try {
            initPostInstallation(handler, args, DeployArtifact.class);
            commandResults = deployArtifact();
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        } catch (OutOfMemoryError e){
            ProcessPanelHelper.printToPanel(handler, AutomatedInstallData.getInstance().langpack.getString("ProcessPanel.outofmemory"), true);
            ProcessPanelHelper.printToLog(e.toString());
            ProcessPanelHelper.printExceptionToLog(e.getStackTrace());
            return false;
        }


        serverCommands.terminateSession();
        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    private static List<ModelNode> deployArtifact() {
        return serverCommands.deployArtifact("\""+parser.getStringProperty(ARTIFACT_PATH)+"\"");
    }
}
