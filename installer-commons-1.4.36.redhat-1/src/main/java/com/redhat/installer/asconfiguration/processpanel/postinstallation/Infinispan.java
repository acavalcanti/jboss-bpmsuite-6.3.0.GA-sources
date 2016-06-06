package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import org.jboss.dmr.ModelNode;

import java.util.List;

public class Infinispan extends PostInstallation {

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        mHandler = handler;
        serverCommands = initServerCommands(Infinispan.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();;
            return false;
        }
        List<ModelNode> commandResults = installInfinispan();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }
    }

    private static List<ModelNode> installInfinispan() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String infinispanContainer = idata.getVariable("infinispan.container");
        String infinispanJndi = idata.getVariable("infinispan.jndiname");
        String infinispanLocalCache = idata.getVariable("infinispan.localcache");
        String infinispanTransMode = idata.getVariable("infinispan.transactionmode");
        String infinispanEvictStrat = idata.getVariable("infinispan.evictionstrat");
        String infinispanEvictMaxEntries = idata.getVariable("infinispan.evictionmax");
        String infinispanExpirationMax = idata.getVariable("infinispan.expirationmax");
        return serverCommands.addInfinispanCache(infinispanContainer,
                infinispanJndi, infinispanLocalCache, infinispanTransMode,
                infinispanEvictStrat, infinispanEvictMaxEntries,
                infinispanExpirationMax);
    }
}
