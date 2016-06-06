package com.redhat.installer.asconfiguration.vault.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommands;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author thauser
 */

public class ServerVariableHelper
{
	public static void run(AbstractUIProcessHandler handler, String [] args)
    {
		AutomatedInstallData idata = AutomatedInstallData.getInstance();
		ArgumentParser parser = new ArgumentParser();
        parser.parse(args);

        String username = idata.getVariable("postinstall.username");
        char[] password = idata.getVariable("postinstall.password").toCharArray();

        // todo : abstract this?
        ServerCommands serverCommands = null;
        try {
            serverCommands = ServerCommands.createSession(username, password, ServerManager.getManagementPort());
            ServerCommandsHelper.createLogger(ServerVariableHelper.class.getName(), serverCommands);
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (CliInitializationException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }


        for (int i = 0; i < args.length; i++)
        {
            if (!args[i].startsWith("--"))
            {
                ModelNode result = serverCommands.submitCommand("/path="+args[i]+":read-attribute(name=path)");
                if (Operations.isSuccessfulOutcome(result)){
                    String varValue = result.get("result").asString();
                    idata.setVariable(args[i],varValue);
                    ProcessPanelHelper.printToPanel(handler, args[i] + " = " + varValue, false);
                }
            }
        }
        serverCommands.terminateSession();
	}
}
