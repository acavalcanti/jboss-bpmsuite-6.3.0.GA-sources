package com.redhat.installer.asconfiguration.ascontroller;


import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.*;

public class ServerCommandsHelper {
    private static final String TIMEOUT = "timeout";
    private static final String ATTEMPTS = "attempts";
    public static int DEFAULT_TIME_TO_SLEEP = 0;
    public static int DEFAULT_CONN_ATTEMPTS = 5;
    private static final Set<String> unsupportedCliCommands = new HashSet<String>();
    static {
        unsupportedCliCommands.add("batch");
        unsupportedCliCommands.add("run-batch");
        unsupportedCliCommands.add("connect");
        unsupportedCliCommands.add("deploy"); // this is unsupported because the command is not parameterizable
    }

    public static void connectContext(AbstractUIProcessHandler mHandler,
                                      ServerCommands serverCommands, int timeToSleep, int attemptLimit)
            throws InterruptedException {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();

        int attempts = 0;
        String connectSuccess = idata.langpack
                .getString("postinstall.processpanel.management.connectSuccess");
        String connectFail = idata.langpack
                .getString("postinstall.processpanel.management.connectFail");
        String connectAttempt = idata.langpack
                .getString("postinstall.processpanel.management.connecting");

        while (attempts < attemptLimit) {
            if (mHandler != null)
                ProcessPanelHelper.printToPanel(mHandler, String.format(connectAttempt, attempts + 1,
                        attemptLimit), false);
            try {
                // sleep first, to give the server some time
                Thread.sleep(timeToSleep * 1000);
                serverCommands.connectContext();
            } catch (CommandLineException e) {
                // We did not successfully connect, so try again after waiting,
                // but only
                // if we have attempts left.
                attempts++;
                if (attempts >= attemptLimit) {
                    if (mHandler != null)
                        ProcessPanelHelper.printToPanel(mHandler,
                                String.format(connectFail, attempts), false);
                    // e.printStackTrace();
                    return;
                }
                continue;
            }
            // If there is no exception, we've connected to the management
            // interface.
            if (mHandler != null)
                ProcessPanelHelper.printToPanel(mHandler, connectSuccess, false);
            // TODO: paramaterize the /jboss-eap-6.1/bin/ part of the location
            // of the jboss-cli-logging.properties.
            // We have successfully connected to the management interface.
            break;
        }
    }

    public static void connectContext(AbstractUIProcessHandler mHandler, ServerCommands serverCommands) throws InterruptedException {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String customTimeout = idata.getVariable(TIMEOUT);
        String customAttemptLimit = idata.getVariable(ATTEMPTS);
        int attemptLimit = customAttemptLimit != null ? Integer.parseInt(customAttemptLimit) : DEFAULT_CONN_ATTEMPTS;
        int timeToSleep = customTimeout != null ? Integer.parseInt(customTimeout) : DEFAULT_TIME_TO_SLEEP;
        connectContext(mHandler, serverCommands, timeToSleep, attemptLimit);
    }

    /**
     * Given a list of ModelNodes, returns a list of ModelNodes whose "outcome" attribute is not "success"
     *
     * @param list the list of ModelNodes which should be searched for failures
     * @return
     */

    public static List<ModelNode> findFailures(List<ModelNode> list) {
        List<ModelNode> failureList = new ArrayList<ModelNode>();
        for (ModelNode result : list) {
            if (!Operations.isSuccessfulOutcome(result)) {
                failureList.add(result);
            }
        }
        return failureList;
    }

    /**
     * Creates a logger and optionally associates it with a ServerCommands instance so the ServerCommands can log each
     * command running through it.
     * Returns null if the installation.logfile variable is not defined.
     *
     * @param name name for the logger
     * @param sc   The ServerCommands instance to assign the logger to
     */
    public static Logger createLogger(String name, ServerCommands sc) {
        // create a logger object to set to this class' serverCommands instance
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String logFile = idata.getVariable("installation.logfile");
        if (logFile == null) {
            return null;
        }
        String logPath = idata.getInstallPath() + File.separator + logFile;
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        Handler h = null;
        try {
            h = new FileHandler(logPath, true);
        } catch (IOException e) {
            //e.printStackTrace();
            return null;
        }
        h.setFormatter(new SimpleFormatter());
        h.setLevel(Level.INFO);
        logger.addHandler(h);
        logger.setLevel(Level.INFO);
        if (sc != null) {
            sc.setLogger(logger);
        }
        return logger;
    }

    /**
     * Creates a logger with the given name, the correct handler and returns it.
     *
     * @param name
     * @return
     */
    public static Logger createLogger(String name) {
        return createLogger(name, null);
    }


    public static boolean findRealFailures(List<ModelNode> failures) {
        return false;
    }

    /**
     * String parameter version of loadCommandsIntoList. Assumes path is already validated.
     *
     * @param path
     * @return
     */
    public static List<String> loadCommandsIntoList(String path, boolean hasDeployCommand) {
        return loadCommandsIntoList(new File(path), hasDeployCommand);
    }

    /**
     * Reads a jboss-cli script file, interpreting each line as a separate command.
     * Ignores lines starting with '#' or empty lines.
     *
     * @param file
     * @return
     */
    public static List<String> loadCommandsIntoList(File file, boolean hasDeployCommand) {
        ArrayList<String> commands = new ArrayList<String>();
        String line = "";
        boolean deployRemoved = false;
        if (hasDeployCommand){
            unsupportedCliCommands.remove("deploy");
            deployRemoved = true;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {
                String cmd = "";

                if (unsupportedCommand(line)) {
                    continue; // ignore comments and empty lines.
                }
                while (line.trim().endsWith("\\")) {
                    // we must go to the next line and add it onto the command, until the command no longer ends with a \; this means the end of the command has been reached.
                    cmd += line.trim().substring(0, line.trim().indexOf("\\"));
                    line = br.readLine();
                }
                cmd += line.trim();

                // Special additions for deploy commands; we assume that the command is meant to be invoked from JBOSS_HOME.
                // Unfortunately, the default location for the jboss-cli to look is the location from which the installer is run.
                if (cmd.startsWith("deploy")){
                    cmd = buildDeployCommand(cmd);
                }
                commands.add(cmd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (deployRemoved){
            unsupportedCliCommands.add("deploy");
        }
        return commands;
    }

    /**
     * Prepend the installation path onto the path to the artifact to be deployed. 
     * @param cmd
     * @return
     */
    private static String buildDeployCommand(String cmd){
        String[] cmdArray = cmd.split(" ");
        String installPath = AutomatedInstallData.getInstance().getInstallPath();
        if (!installPath.endsWith(File.separator)){
            installPath+=File.separator;
        }
        File normalizedPath = new File (installPath+cmdArray[1]);
        cmdArray[1] = normalizedPath.getAbsolutePath();
        String builtCommand = "";
        for (String cmdPart : cmdArray){
            builtCommand += cmdPart + " ";
        }
        return builtCommand;
    }


    /**
     * Returns if the given command is in the unsupported command set
     * @param cmd the command
     * @return presence of the given command in the set
     */
    private static boolean unsupportedCommand(String cmd){
        if (cmd.trim().startsWith("#") || cmd.trim().isEmpty()){
            return true;
        }
        for (String prefix : unsupportedCliCommands){
            if (cmd.trim().startsWith(prefix)){
                return true;
            }
        }
        return false;
    }

    /**
     * Given a ModelNode, attempts to get the value of the property named "command". This is kept in one place so modification is easy
     *
     * @param node
     * @return
     */
    public static String getCommand(ModelNode node) {
        return node.get("command").asString();
    }

    public static ModelNode setCommand(ModelNode node, String cmd) {
        return node.get("command").set(cmd);
    }

}
