package com.redhat.installer.tests.installation.action;

import com.redhat.installer.framework.testers.PanelActionTester;
import com.redhat.installer.installation.action.SetSolarisHPUXFlags;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Created by thauser on 9/3/14.
 */
public class SetSolarisHPUXFlagsTest extends PanelActionTester {

    @Before
    public void before(){
        panelAction = new SetSolarisHPUXFlags();
    }

    @Test
    public void testSolarisHPUXCase(){
        idata.setVariable("product.name", "eap");
        idata.setVariable("SYSTEM_os_name", "SunOS");
        panelAction.executeAction(idata, handler);
        // avoid failing erroneously if built on a 32 bit machine
        if (System.getProperty("sun.arch.data.model").equals("64")) {
            assertEquals("true", idata.getVariable("add.bits.to.configs"));

        } else {
            assertNull(idata.getVariable("add.bits.to.configs"));
        }
    }

    @Test
    public void testStandardCase(){
        idata.setVariable("product.name", "eap");
        idata.setVariable("SYSTEM_os_name", "TomOS");
        panelAction.executeAction(idata,handler);
        assertNull(idata.getVariable("add.bits.to.configs"));
    }



}
