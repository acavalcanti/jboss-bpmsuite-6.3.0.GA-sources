package com.redhat.installer.tests.asconfiguration.action;

import com.redhat.installer.asconfiguration.action.DatabaseLoggingDefaultsAction;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Simple test to make sure the DBLogging default URL is being set correctly
 * Note that only the URL need change here;
 * the driver is handled by a separate class, since the change must take place after
 * validation for that variable
 * Created by thauser on 9/2/14.
 */
public class DatabaseLoggingDefaultsActionTest extends PanelActionTester {

    private static final String URL_DEFAULT = "dblogging.url.default";
    private static final String DRIVER_DEFAULT = "dblogging.driver.jdbc.default";
    private static final String JDBC_INSTALLED = "jdbc.driver.install";
    private static final String JDBC_DRIVER = "jdbc.driver.name";
    private static final String DBLOGGING_JDBC_USERNAME_DEFAULT = "dblogging.jdbc.username.default";

    private static final String URL = "dblogging.url";

    @Before
    public void before(){
        panelAction = new DatabaseLoggingDefaultsAction();
        setDefaultValues();
    }

    private void setDefaultValues() {
        idata.setVariable(URL_DEFAULT, "defaulturl");
        idata.setVariable(DRIVER_DEFAULT, "installed");
        idata.setVariable(JDBC_INSTALLED, "false");
        idata.setVariable(DBLOGGING_JDBC_USERNAME_DEFAULT, "defaultusername");
        idata.setVariable(JDBC_DRIVER, "sqlserver");
    }

    /**
     * Test that the variables are set to the defaults correctly when
     * no JDBC driver is installed
     */
    @Test
    public void testDefaults(){
        panelAction.executeAction(idata, handler);
        assertEquals("defaulturl",idata.getVariable(URL));
    }

    /**
     * Test that the variables are correctly set for an installed driver.
     * In this simple case we use postgresql.
     */
    @Test
    public void testInstalledDriver() {
        idata.setVariable(JDBC_INSTALLED, "true");
        idata.setVariable(JDBC_DRIVER, JBossJDBCConstants.postgresqlJdbcName);
        panelAction.executeAction(idata, handler);
        assertEquals(JBossJDBCConstants.postgresqlConnUrl, idata.getVariable(URL));
    }
}
