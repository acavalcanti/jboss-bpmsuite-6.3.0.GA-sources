package com.redhat.installer.asconfiguration.processpanel;


import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommands;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.dmr.ModelNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CliScriptHelper {
    private static ServerCommands serverCommands;
    private static List<String> cliScripts;
    private static final String RESOLVE_PARAMS = "resolve-params";
    private static final String SCRIPT = "script";
    private static final String HAS_DEPLOY = "has-deploy";
    // ServerCommands to run the commands with
    private static ArgumentParser parser;
    private static AutomatedInstallData idata;
    private static AbstractUIProcessHandler mHandler;
    private static boolean hasDeployCommand;


    public static boolean run(AbstractUIProcessHandler handler, String[] args) throws Exception {
        return initializeStaticVariables(handler, args) && isCliScriptExecutionSuccessful();
    }

    private static boolean initializeStaticVariables(AbstractUIProcessHandler handler, String[]args) throws InterruptedException {
        idata = AutomatedInstallData.getInstance();
        mHandler = handler;
        parser = new ArgumentParser();
        parser.parse(args);
        cliScripts = parser.getListProperty(SCRIPT);
        hasDeployCommand = parser.hasProperty(HAS_DEPLOY) && Boolean.parseBoolean(parser.getStringProperty(HAS_DEPLOY));

        return allScriptsExist() && initializeServerCommands();
    }

    private static boolean allScriptsExist() {
        for (String script : cliScripts) {
            File check = new File(script);
            if (!check.exists()) {
                ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("CliScriptHelper.notexist"), check.getAbsolutePath()), true);
                return false;
            }
        }
        return true;
    }

    private static boolean initializeServerCommands() throws InterruptedException {
        String username = idata.getVariable("postinstall.username");
        char[] password = idata.getVariable("postinstall.password").toCharArray();
        serverCommands = null;

        int port = ServerManager.getManagementPort();

        try {
            serverCommands = ServerCommands.createSession(username, password, port);

            if (parser.hasProperty(RESOLVE_PARAMS)) {
                String resolution = parser.getStringProperty(RESOLVE_PARAMS);
                serverCommands.setResolveParameterValues(Boolean.parseBoolean(resolution));
            } else {
                serverCommands.setResolveParameterValues(true);
            }
        } catch (CliInitializationException e) {
            e.printStackTrace();
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("postinstall.processpanel.init.error"), true);
            return false;
        }
        ServerCommandsHelper.createLogger(CliScriptHelper.class.getName(), serverCommands);
        ServerCommandsHelper.connectContext(mHandler, serverCommands, 0, 5);
        return true;
    }


    private static boolean isCliScriptExecutionSuccessful() {
        try {
            executeScripts();
        } catch (ScriptFailedException sfe) {
            printFailureMessage(sfe.getFailedScriptName(), sfe.getFailedCommands());
            return false;
        }
        return true;
    }


    private static List<ModelNode> executeScripts() throws ScriptFailedException {
        List<ModelNode> returnVal = new ArrayList<ModelNode>();
        for (String cliScript : cliScripts) {
            File cliScriptFile = new File(cliScript);
            List<String> commands = ServerCommandsHelper.loadCommandsIntoList(cliScript, hasDeployCommand);
            commands.remove("/:reload");
            commands.remove("/host=master:reload(restart-servers=true)");
            List<ModelNode> cliScriptResults = serverCommands.runCommandsInList(commands);
            List<ModelNode> failures = ServerCommandsHelper.findFailures(cliScriptResults);
            if (!failures.isEmpty()) {
                serverCommands.terminateSession();
                throw new ScriptFailedException(cliScriptFile.getAbsolutePath(), failures);
            } else {
                ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("CliScriptHelper.success"), cliScriptFile.getAbsolutePath()), false);
                returnVal.addAll(cliScriptResults);
            }
        }
        serverCommands.terminateSession();
        return returnVal;
    }

    private static void printFailureMessage(String failedScriptName, List<ModelNode> failures) {
        ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("CliScriptHelper.failure"), failedScriptName), true);
        for (ModelNode failure : failures){
            ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("CliScriptHelper.command.failed"), ServerCommandsHelper.getCommand(failure)), true);
            ProcessPanelHelper.printToPanel(mHandler, failure.toString(), true);
        }
    }

    private static class ScriptFailedException extends Exception {
        private String failedScriptName;
        private List<ModelNode> failedCommands;

        ScriptFailedException(String failedScript, List<ModelNode> failedCommands) {
            this.failedScriptName = failedScript;
            this.failedCommands = failedCommands;
        }

        public String getFailedScriptName(){
            return failedScriptName;
        }


        public List<ModelNode> getFailedCommands(){
            return failedCommands;
        }
    }
}
