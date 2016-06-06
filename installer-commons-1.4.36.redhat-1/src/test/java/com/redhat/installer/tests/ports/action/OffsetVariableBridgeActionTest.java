package com.redhat.installer.tests.ports.action;

import com.redhat.installer.framework.testers.PanelActionTester;
import com.redhat.installer.ports.action.OffsetVariableBridgeAction;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/3/14.
 */
public class OffsetVariableBridgeActionTest extends PanelActionTester {
    
    private final String STANDALONE_PORT_OFFSET =  "standalone.port-offset";
    private final String STANDALONE_MANAGEMENT_NATIVE = "standalone.management-native-with.offset";
    private final String STANDALONE_MANAGEMENT_HTTP = "standalone.management-http-with.offset";
    private final String STANDALONE_MANAGEMENT_HTTPS = "standalone.management-https-with.offset";
    
    private final String DOMAIN_PORT_OFFSET = "domain.port-offset";
    private final String DOMAIN_PORT_OFFSET150 = "domain.port-offset150";
    private final String DOMAIN_PORT_OFFSET250 = "domain.port-offset250";
    private final String DOMAIN_MANAGEMENT_NATIVE = "domain.management-native";
    private final String DOMAIN_MANAGEMENT_HTTP = "domain.management-http";
    private final String DOMAIN_MANAGEMENT_HTTPS = "domain.management-https";
    private final String DOMAIN_MASTER_PORT = "master.domain.port";

    @Before
    public void before(){
        panelAction = new OffsetVariableBridgeAction();
        idata.setVariable("domain.management-native", "9999");
        idata.setVariable("domain.management-http", "9990");
        idata.setVariable("domain.management-https", "9443");
        idata.setVariable("standalone.management-native", "5555");
        idata.setVariable("standalone.management-http", "5550");
        idata.setVariable("standalone.management-https", "5443");

        idata.setVariable("domain.management-native-1", "test");
        idata.setVariable("domain.management-http-1", "test");
        idata.setVariable("domain.management-https-1", "test");
        idata.setVariable("standalone.management-native-1", "test");
        idata.setVariable("standalone.management-http-1", "test");
        idata.setVariable("standalone.management-https-1", "test");


    }

    @Test
    public void applyAssistedOffset(){
        idata.setVariable("portDecision", "assist");
        idata.setVariable("portOffsetType", "100");
        idata.setVariable("configurePortOffset", "");
        panelAction.executeAction(idata, handler);

        assertEquals("${jboss.socket.binding.port-offset:100}", idata.getVariable(STANDALONE_PORT_OFFSET));
        assertEquals("100", idata.getVariable(DOMAIN_PORT_OFFSET));
        assertEquals("250", idata.getVariable(DOMAIN_PORT_OFFSET150));
        assertEquals("350", idata.getVariable(DOMAIN_PORT_OFFSET250));
        assertEquals("${jboss.domain.master.port:10099}", idata.getVariable(DOMAIN_MASTER_PORT));

        assertEquals("${test:10090}", idata.getVariable(DOMAIN_MANAGEMENT_HTTP));
        assertEquals("${test:9543}", idata.getVariable(DOMAIN_MANAGEMENT_HTTPS));
        assertEquals("${test:10099}", idata.getVariable(DOMAIN_MANAGEMENT_NATIVE));

        assertEquals("${test:5650}", idata.getVariable(STANDALONE_MANAGEMENT_HTTP));
        assertEquals("${test:5543}", idata.getVariable(STANDALONE_MANAGEMENT_HTTPS));
        assertEquals("${test:5655}", idata.getVariable(STANDALONE_MANAGEMENT_NATIVE));



    }

    @Test
    public void applySpecifiedOffset(){

        idata.setVariable("portDecision", "assist");
        idata.setVariable("portOffsetType", "specify");
        idata.setVariable("configurePortOffset", "101");
        panelAction.executeAction(idata, handler);

        assertEquals("${jboss.socket.binding.port-offset:101}", idata.getVariable(STANDALONE_PORT_OFFSET));
        assertEquals("101", idata.getVariable(DOMAIN_PORT_OFFSET));
        assertEquals("251", idata.getVariable(DOMAIN_PORT_OFFSET150));
        assertEquals("351", idata.getVariable(DOMAIN_PORT_OFFSET250));
        assertEquals("${jboss.domain.master.port:10100}", idata.getVariable(DOMAIN_MASTER_PORT));

        assertEquals("${test:10091}", idata.getVariable(DOMAIN_MANAGEMENT_HTTP));
        assertEquals("${test:9544}", idata.getVariable(DOMAIN_MANAGEMENT_HTTPS));
        assertEquals("${test:10100}", idata.getVariable(DOMAIN_MANAGEMENT_NATIVE));

        assertEquals("${test:5651}", idata.getVariable(STANDALONE_MANAGEMENT_HTTP));
        assertEquals("${test:5544}", idata.getVariable(STANDALONE_MANAGEMENT_HTTPS));
        assertEquals("${test:5656}", idata.getVariable(STANDALONE_MANAGEMENT_NATIVE));

    }

    @Test
    public void applyNoOffset(){
        idata.setVariable("portDecision", "false");
        idata.setVariable("portOffsetType", "specify");
        idata.setVariable("configurePortOffset", "101");
        panelAction.executeAction(idata, handler);

        assertEquals("${jboss.socket.binding.port-offset:0}", idata.getVariable(STANDALONE_PORT_OFFSET));
        assertEquals("0", idata.getVariable(DOMAIN_PORT_OFFSET));
        assertEquals("150", idata.getVariable(DOMAIN_PORT_OFFSET150));
        assertEquals("250", idata.getVariable(DOMAIN_PORT_OFFSET250));
        assertEquals("${jboss.domain.master.port:9999}", idata.getVariable(DOMAIN_MASTER_PORT));

        assertEquals("${test:9990}", idata.getVariable(DOMAIN_MANAGEMENT_HTTP));
        assertEquals("${test:9443}", idata.getVariable(DOMAIN_MANAGEMENT_HTTPS));
        assertEquals("${test:9999}", idata.getVariable(DOMAIN_MANAGEMENT_NATIVE));

        assertEquals("${test:5550}", idata.getVariable(STANDALONE_MANAGEMENT_HTTP));
        assertEquals("${test:5443}", idata.getVariable(STANDALONE_MANAGEMENT_HTTPS));
        assertEquals("${test:5555}", idata.getVariable(STANDALONE_MANAGEMENT_NATIVE));

    }
}
