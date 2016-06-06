package com.redhat.installer.tests.asconfiguration.action;

import com.redhat.installer.asconfiguration.action.PostInstallDefaultReset;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.framework.constants.CommonStrings;
import com.redhat.installer.framework.testers.PanelActionTester;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Created by thauser on 9/2/14.
 */
public class PostInstallDefaultResetTest extends PanelActionTester implements CommonStrings {

    private String[] postInstallVars = new String[] { "installVault", "installSsl",
            "installLdap", "installInfinispan", "installSecurityDomain",
            "jdbc.driver.install", "datasource.install" };
    private String [] vaultVariables = {"vault.keystoreloc", "vault.encrdir", "vault.itercount", "vault.alias", "vault.salt", "vault.keystorepwd"};
    private String [] jdbcVariables = {"jdbc.driver.install", "jdbc.driver.name"};
    private String [] dbVariables ={"db.dialect", "db.driver", "db.url", "db.user", "db.password"};
    private String [] dbDefaultVariables = {"db.default.url", "db.default.user", "db.default.password"};

    @Before
    public void before(){
        panelAction = new PostInstallDefaultReset();
        setDefaultValues();
    }


    private void setDefaultValues() {
        idata.setVariable("requires.default.database","true");
        idata.setVariable("requires.vault.defaults.reset", "true");
        idata.setVariable("postinstallServer", TRUE);
        idata.setVariable("db.default.url","defaulturl");
        idata.setVariable("db.default.user", "defaultuser");
        idata.setVariable("db.default.password", "defaultpassword");
        idata.setVariable("domain.http.number", "9990");
        idata.setVariable("installSsl", "false");

        for (String vault : vaultVariables){
            idata.setVariable(vault, vault);
            idata.setVariable(vault+".default",vault+".default");
        }
        for (String db : dbVariables){
            idata.setVariable(db,db);
        }
        for (String db : dbDefaultVariables){
            idata.setVariable(db,db);
        }
        for (String jdbc : jdbcVariables){
            idata.setVariable(jdbc, jdbc);
        }
    }

    /**
     * test with installerMode in auto, so nothing should occur
     */
    @Test
    public void testAutoInstall(){
        idata.setVariable("jdbc.driver.name", JBossJDBCConstants.postgresqlJdbcName);
        idata.setVariable("installerMode", "AUTO");
        panelAction.executeAction(idata, handler);
        // since the AUTO value in installerMode makes everything get skipped, the value should not be reset
        assertEquals(JBossJDBCConstants.postgresqlJdbcName, idata.getVariable("jdbc.driver.name"));
    }

    /**
     * Tests that all of the checkboxes are reset to false if the user sets the radio to false
     **/
    @Test
    public void testPostInstallReset(){
        setPostInstallVars(TRUE);
        idata.setVariable("product.name", "soa");
        idata.setVariable("postinstallServer", FALSE);
        panelAction.executeAction(idata, handler);
        verifyFalseVariables(PostInstallDefaultReset.class.getName(), postInstallVars);
    }

    /**
     * Tests that vault values are reset to defaults and the existing values are saved to .backup
     */
    @Test
    public void testVaultSetDefaultsAndBackups() {
        idata.setVariable("installVault", FALSE);
        panelAction.executeAction(idata, handler);
        checkVaultBackups();
        checkVaultDefaults();
    }

    /**
     * Tests database default resets
     */
    @Test
    public void testDbSetDefaults(){
        idata.setVariable("jdbc.driver.install", FALSE);
        panelAction.executeAction(idata, handler);
        checkDBDefaults();
    }

    /**
     * Passwords are nulled out in this case
     */
    @Test
    public void testDbPasswordReset(){

        idata.setVariable("jdbc.driver.install", TRUE);
        panelAction.executeAction(idata, handler);
        assertNull(idata.getVariable("db.user"));
        assertNull(idata.getVariable("db.password"));
    }

    /**
     * Convenience method to set all install* variables to some value
     * @param value
     */
    private void setPostInstallVars(String value){
        for (String var : postInstallVars){
            idata.setVariable(var, value);
        }
    }

    private void checkDBDefaults(){
        assertEquals(JBossJDBCConstants.h2JdbcName, idata.getVariable("jdbc.driver.name"));
        assertEquals(JBossJDBCConstants.h2Dialect, idata.getVariable("db.dialect"));
        assertEquals(JBossJDBCConstants.h2JdbcName, idata.getVariable("db.driver"));
        assertEquals(idata.getVariable("db.default.url"), idata.getVariable("db.url"));
        assertEquals(idata.getVariable("db.default.user"), idata.getVariable("db.user"));
        assertEquals(idata.getVariable("db.default.password"), idata.getVariable("db.password"));
    }

    /**
     * Verifies that the vault variables have been reset to the default values.
     */
    private void checkVaultDefaults() {
        for (String vault: vaultVariables){
            assertEquals(idata.getVariable(vault), vault+".default");
        }
    }

    /**
     * Verifies that the vault ".backup" variables contain the originally assigned vault values.
     */
    private void checkVaultBackups() {
        for (String vault : vaultVariables){
            assertEquals(idata.getVariable(vault+".backup"), vault);
        }
    }
}
