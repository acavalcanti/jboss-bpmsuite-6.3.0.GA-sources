package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Datasource extends PostInstallation {
    private static String panelid = "";
    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        if(serverCommands.isDomain()){
            panelid = args[2];
        }
        else {
            panelid = args[1];
        }
        panelid = panelid.replace("--text=", "") + ".";
        mHandler = handler;
        serverCommands = initServerCommands(Datasource.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installDatasource();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    protected static List<ModelNode> installDatasource() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        // datasource variables
        String dsJdbcName = idata.getVariable("jdbc.driver.name");
        String dsName = idata.getVariable(panelid + "jdbc.datasource.name");
        String dsJndiName = idata.getVariable(panelid + "jdbc.datasource.jndiname");
        String dsMinPool = idata.getVariable(panelid + "jdbc.datasource.minpoolsize");
        String dsMaxPool = idata.getVariable(panelid + "jdbc.datasource.maxpoolsize");
        String dsConnectionUrl = idata.getVariable(panelid + "jdbc.datasource.connectionurl");
        boolean dsIsXa = (idata.getVariable(panelid + "jdbc.datasource.datasourcetype") != null &&
                idata.getVariable(panelid + "jdbc.datasource.datasourcetype").equals("true"));

        // pack the xa props map
        Map<String,String> dsXaProps = new HashMap<String, String>();
        dsXaProps.put("ServerName", idata.getVariable("jdbc.datasource.xa.servername"));
        dsXaProps.put("DatabaseName", idata.getVariable("jdbc.datasource.xa.databasename"));
        dsXaProps.put("PortNumber", idata.getVariable("jdbc.datasource.xa.portnumber"));
        dsXaProps.put("URL", idata.getVariable("jdbc.datasource.xa.oracleurl"));
        dsXaProps.put("NetworkProtocol", idata.getVariable("jdbc.datasource.xa.sybaseprotocol"));
        dsXaProps.put("SelectMethod", idata.getVariable("jdbc.datasource.xa.microsoftcursor"));

        // reset counter for the next dynamicPanel
        int counter = 1;
        while (true) {
            String prop = idata.getVariable("jdbc.datasource.xa.extraprops-" + counter + "-name");
            if (prop == null) {
                break;
            }
            dsXaProps.put(prop, idata.getVariable("jdbc.datasource.xa.extraprops-" + counter + "-value"));
            counter++;
        }

        String dsUsername = idata.getVariable(panelid + "jdbc.datasource.username");
        String dsPassword = idata.getVariable(panelid + "jdbc.datasource.password");
        String dsXaRecoveryUser = idata.getVariable(panelid + "jdbc.datasource.xa.recoveryuser");
        String dsXaRecoveryPass = idata.getVariable(panelid + "jdbc.datasource.xa.recoverypass");
        String dsSecurityDomain = idata.getVariable(panelid + "jdbc.datasource.securitydomain");
        boolean isSecurityDomain = Boolean.parseBoolean(idata.getVariable(panelid + "jdbc.datasource.issecuritydomain"));

        if (dsIsXa) { // XA command!
            if (isSecurityDomain) {
                return serverCommands.installXaDatasourceSecurityDomain(dsName,
                        dsJndiName, dsJdbcName, dsMinPool, dsMaxPool,
                        dsSecurityDomain, dsXaProps, dsXaRecoveryUser,
                        dsXaRecoveryPass, null);
            } else {
                return serverCommands.installXaDatasourceUsernamePwd(dsName, dsJndiName, dsJdbcName, dsMinPool, dsMaxPool, dsUsername,
                        dsPassword, dsXaProps, dsXaRecoveryUser, dsXaRecoveryPass, null);
            }
        } else {
            if (isSecurityDomain) {
                return serverCommands.installDatasourceSecurityDomain(dsName, dsJndiName, dsJdbcName, dsConnectionUrl, dsMinPool,
                        dsMaxPool, dsSecurityDomain, null);
            } else {
                return serverCommands.installDatasourceUsernamePwd(dsName, dsJndiName, dsJdbcName, dsConnectionUrl, dsMinPool, dsMaxPool,
                        dsUsername, dsPassword, null);
            }
        }
    }
}
