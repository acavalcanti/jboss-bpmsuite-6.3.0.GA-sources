package com.redhat.installer.tests.asconfiguration.action;

import com.redhat.installer.asconfiguration.action.DatabaseLoggingDriverSetter;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/2/14.
 */
public class DatabaseLoggingDriverSetterTest extends PanelActionTester {

    private static final String DRIVER = "dblogging.driver";
    private static final String JDBC_DRIVER = "jdbc.driver.name";


    @Before
    public void before(){
        panelAction = new DatabaseLoggingDriverSetter();
        setDefaultValues();
    }

    private void setDefaultValues() {
        idata.setVariable(DRIVER, "default");
        idata.setVariable(JDBC_DRIVER, "realDriverName");
    }

    /**
     * Tests the case where the user selected h2 from the combobox
     */
    @Test
    public void testDefaultDriver() {
        panelAction.executeAction(idata,handler);
        assertEquals("default", idata.getVariable(DRIVER));
    }

    /**
     * User selects a driver to install previously, and uses that driver for DB logging
     */
    @Test
    public void testInstalledDriver() {
        idata.setVariable(DRIVER,"installed");
        panelAction.executeAction(idata,handler);
        assertEquals("realDriverName", idata.getVariable(DRIVER));
    }


}
