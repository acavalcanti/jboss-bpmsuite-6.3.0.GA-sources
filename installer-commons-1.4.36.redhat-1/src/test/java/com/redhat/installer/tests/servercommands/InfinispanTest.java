package com.redhat.installer.tests.servercommands;

import com.redhat.installer.asconfiguration.processpanel.postinstallation.Infinispan;
import com.redhat.installer.framework.testers.PostinstallTester;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by aabulawi on 31/07/15.
 */
public class InfinispanTest extends PostinstallTester {

    @Test
    public void installInfinispan(){
        idata.setVariable("infinispan.container", "myTestInfinispan");
        idata.setVariable("infinispan.jndiname", "java:jboss/infinispan/jbosseap");
        idata.setVariable("infinispan.localcache", "jbosseap-cache");
        idata.setVariable("infinispan.transactionmode", "NON_DURABLE_XA");
        idata.setVariable("infinispan.evictionstrat", "UNORDERED");
        idata.setVariable("infinispan.evictionmax", "3");
        idata.setVariable("infinispan.expirationmax", "6");
        assertTrue(Infinispan.run(mockAbstractUIProcessHandler, new String[]{}));
    }


}
