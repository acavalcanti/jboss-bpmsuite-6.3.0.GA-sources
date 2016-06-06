package com.redhat.installer.tests.asconfiguration.vault.action;

import com.redhat.installer.asconfiguration.vault.action.VaultMaskAdjuster;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 2/3/15.
 */
public class VaultMaskAdjusterTest extends PanelActionTester {
    @Before
    public void setup(){
        panelAction = new VaultMaskAdjuster();
        idata.setVariable("vault.salt", "8675309K");
        idata.setVariable("vault.itercount", "50");
    }

    @Test
    public void testValidPassword(){
        idata.setVariable("vault.keystorepwd", "hihihi1!");
        panelAction.executeAction(idata,handler);
        //unchanged iteration count, since the password is valid
        assertEquals(idata.getVariable("vault.itercount"), "50");
    }

    // https://bugzilla.redhat.com/show_bug.cgi?id=1163655
    // EAP 6.4.0 does not have this problem
    @Test
    @Ignore
    public void testScenarioOne(){
        idata.setVariable("vault.keystorepwd","teiid123$");
        panelAction.executeAction(idata,handler);
        // incremented because with 8675309K and 50, the mask is incorrectly padded
        assertEquals("51",idata.getVariable("vault.itercount"));
    }

    // https://bugzilla.redhat.com/show_bug.cgi?id=1125004
    // EAP 6.4.0 does not have this problem
    @Test
    @Ignore
    public void testScenarioTwo(){
        idata.setVariable("vault.keystorepwd", "vault1");
        idata.setVariable("vault.itercount", "44");
        panelAction.executeAction(idata, handler);
        assertEquals("45",idata.getVariable("vault.itercount"));
    }
}
