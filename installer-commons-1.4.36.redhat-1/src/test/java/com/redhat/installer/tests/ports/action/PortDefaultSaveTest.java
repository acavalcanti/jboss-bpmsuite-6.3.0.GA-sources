package com.redhat.installer.tests.ports.action;

import com.redhat.installer.framework.testers.PanelActionTester;
import com.redhat.installer.ports.action.PortDefaultSave;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/3/14.
 */
public class PortDefaultSaveTest extends PanelActionTester{

    @Before
    public void specificSetup(){
        panelAction = new PortDefaultSave();
    }

    @Test
    public void testPortSave(){
        idata.setVariable("standalone.testport1","10000");
        idata.setVariable("domain.testport1", "12000");
        panelAction.executeAction(idata,handler);
        assertEquals("10000", idata.getVariable("standalone.testport1"));
        assertEquals("12000", idata.getVariable("domain.testport1"));
    }


}
