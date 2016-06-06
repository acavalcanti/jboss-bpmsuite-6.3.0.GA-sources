
package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommands;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.asconfiguration.processpanel.PostInstallUserHelper;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.dmr.ModelNode;

import java.util.List;

/**
 * @author dcheung@redhat.com, dmondega@redhat.com, thauser@redhat.com
 */
abstract public class PostInstallation {
    public static final String DESCRIPTOR = "xml-file";
    static ServerCommands serverCommands;
    static AbstractUIProcessHandler mHandler; // output to the
                                              // processpanel
    protected static ArgumentParser parser;
    static String xmlDescriptor;

    /**
     * PostInstallation cleanup: write to log, print errors to panel, checks for failures, terminate the
     *                           connection to the server, and return the result of the installation
     *
     */
    protected static boolean installResult(List<ModelNode> results) {
        boolean failed = false;
        if (results == null){
            return failed;
        }
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        List<ModelNode> failures = ServerCommandsHelper.findFailures(results);
        if (!failures.isEmpty()) {
            failed = true;
            // errors
            // print the failed command(s)
            for (ModelNode failure : failures) {
                // command that failed
                ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("postinstall.processpanel.command.failure"), ServerCommandsHelper.getCommand(failure)), true);
                // ModelNode result
                ProcessPanelHelper.printToPanel(mHandler, failure.toString(), true);
            }
        } else {
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("postinstall.processpanel.success"), false);
        }
        return !failed;
    }

    /**
     * Gives a ServerCommands instance back that is appropriate for the current server execution.
     * @param caller The classname of the class that is getting the ServerCommands, to instantiate the logger correctly.
     * @return
     */

    public static ServerCommands initServerCommands(Class caller)
    {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        ServerCommands serverCommands = null;
        try
        {
            int managementPort = ServerManager.getManagementPort();
            // the current server's type.
            boolean isDomain = ServerManager.getMode().equals(ServerManager.DOMAIN);
            String username = idata.getVariable(PostInstallUserHelper.USERNAME_VAR);
            char[] password = idata.getVariable(PostInstallUserHelper.PWD_VAR).toCharArray();
            boolean slave = Boolean.parseBoolean(ServerManager.getSlave());

            if (isDomain) {
                serverCommands = ServerCommands.createLocalDomainUsernameSession(username, password, managementPort, slave, new String[]{"default","ha","full","full-ha"});
            } else {
                serverCommands = ServerCommands.createLocalStandaloneUsernameSession(username, password, managementPort);
            }

        }
        catch (CliInitializationException e)
        {
            ProcessPanelHelper.printToPanel(mHandler,idata.langpack.getString("postinstall.processpanel.init.error") +
                    e.getMessage() + "\n" + e.getCause() + " : ", true);
        }

        serverCommands.setContextLoggingConfig(idata.getVariable("INSTALL_PATH") + "/bin/jboss-cli-logging.properties");
        ServerCommandsHelper.createLogger(caller.getName(), serverCommands);
        return serverCommands;
    }

    protected static void initPostInstallation(AbstractUIProcessHandler handler, String[]args, Class c) throws InterruptedException{
        mHandler = handler;
        serverCommands = initServerCommands(c);
        ServerCommandsHelper.connectContext(handler, serverCommands);
        parser = new ArgumentParser();
        parser.parse(args);
        if (parser.hasProperty(DESCRIPTOR)){
            xmlDescriptor = parser.getStringProperty(DESCRIPTOR);
        }
    }


}
