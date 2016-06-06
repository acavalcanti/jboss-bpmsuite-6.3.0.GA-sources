package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;

import org.jboss.dmr.ModelNode;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import java.util.List;

/**
 * Created by mvaghela on 16/06/15.
 */
public class SystemProperties extends PostInstallation {

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {

        mHandler = handler;
        serverCommands = initServerCommands(SystemProperties.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = null;
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        Elements element = (Elements)idata.getAttribute("system-properties");

        for(Element property : element) {
            String propertyName = property.attr("name");
            String propertyValue = property.attr("value");
            idata.setVariable(propertyName, propertyValue);
            commandResults = serverCommands.addSystemProperty(propertyName, propertyValue);
        }

        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            // we didn't run anything
            return true;
        }
    }
}
