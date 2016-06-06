package com.redhat.installer.tests.ports.action;

import com.redhat.installer.framework.testers.PanelActionTester;
import com.redhat.installer.ports.action.PortDefaultReset;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/3/14.
 */
public class PortDefaultResetTest extends PanelActionTester {

    @Before
    public void specificSetup(){
        panelAction = new PortDefaultReset();
        idata.setVariable("portDecision", "true");
        idata.setVariable("configureStandalone","true");
        idata.setVariable("configureDomain","true");
    }

    @Test
    public void testResetStandalone(){
        idata.setVariable("configureStandalone", "false");
        idata.setVariable("standalone.testport1", "1000");
        idata.setVariable("standalone.testport2", "2000");
        idata.setVariable("standalone.testport3", "3000");
        idata.setVariable("standalone.testport1.orig", "1");
        idata.setVariable("standalone.testport2.orig", "2");
        idata.setVariable("standalone.testport3.orig", "3");
        panelAction.executeAction(idata,handler);
        assertEquals("1", idata.getVariable("standalone.testport1"));
        assertEquals("2", idata.getVariable("standalone.testport2"));
        assertEquals("3", idata.getVariable("standalone.testport3"));
    }

    @Test
    public void testResetDomain(){
        idata.setVariable("configureDomain", "false");
        idata.setVariable("domain.testport1", "1000");
        idata.setVariable("domain.testport2", "2000");
        idata.setVariable("domain.testport3", "3000");
        idata.setVariable("domain.testport1.orig", "1");
        idata.setVariable("domain.testport2.orig", "2");
        idata.setVariable("domain.testport3.orig", "3");
        panelAction.executeAction(idata,handler);
        assertEquals("1", idata.getVariable("domain.testport1"));
        assertEquals("2", idata.getVariable("domain.testport2"));
        assertEquals("3", idata.getVariable("domain.testport3"));
    }

    @Test
    public void testNoReset(){
        idata.setVariable("standalone.testport1", "1000");
        idata.setVariable("standalone.testport2", "2000");
        idata.setVariable("standalone.testport3", "3000");
        idata.setVariable("domain.testport1", "1000");
        idata.setVariable("domain.testport2", "2000");
        idata.setVariable("domain.testport3", "3000");
        panelAction.executeAction(idata,handler);
        assertEquals("1000", idata.getVariable("standalone.testport1"));
        assertEquals("2000", idata.getVariable("standalone.testport2"));
        assertEquals("3000", idata.getVariable("standalone.testport3"));
        assertEquals("1000", idata.getVariable("domain.testport1"));
        assertEquals("2000", idata.getVariable("domain.testport2"));
        assertEquals("3000", idata.getVariable("domain.testport3"));

    }
}
