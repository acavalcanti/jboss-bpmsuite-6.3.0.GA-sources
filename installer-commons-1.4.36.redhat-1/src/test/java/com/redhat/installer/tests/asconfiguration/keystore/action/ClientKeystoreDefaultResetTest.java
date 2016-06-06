package com.redhat.installer.tests.asconfiguration.keystore.action;

import com.redhat.installer.asconfiguration.keystore.action.ClientKeystoreDefaultReset;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/3/14.
 */
public class ClientKeystoreDefaultResetTest extends PanelActionTester {

    private static final String LOC_VAR = "generated.keystores.client.location";
    private static final String LOC_DEFAULT_VAR = "generated.keystores.client.location.default";

    @Before
    public void before(){
        panelAction = new ClientKeystoreDefaultReset();
        idata.setVariable(LOC_VAR, "chosenlocation");
        idata.setVariable(LOC_DEFAULT_VAR, "defaultlocation");
    }

    @Test
    public void testResetDefault(){
        panelAction.executeAction(idata,handler);
        assertEquals(idata.getVariable(LOC_DEFAULT_VAR), idata.getVariable(LOC_VAR));
    }

    @Test
    public void testKeepValue(){
        idata.setVariable("generateClientKeystores", FALSE);
        panelAction.executeAction(idata,handler);
        assertEquals("chosenlocation",idata.getVariable(LOC_VAR));
    }
}
