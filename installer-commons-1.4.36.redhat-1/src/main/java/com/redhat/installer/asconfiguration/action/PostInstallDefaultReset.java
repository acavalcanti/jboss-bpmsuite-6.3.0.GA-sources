package com.redhat.installer.asconfiguration.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.layering.constant.ValidatorConstants;

/**
 * This class resets all of the post-install variables to 'off' if the
 * postinstallServer variable evaluates to "false". It is run when the user hits
 * 'next' in the post-install panel, after the validation occurs.
 *
 * @author fcanas, thauser@redhat.com
 */
public class PostInstallDefaultReset implements PanelAction {
    private static final String[] postInstallVars = new String[]{"installVault", "installSsl",
            "installLdap", "installInfinispan", "installSecurityDomain",
            "jdbc.driver.install", "datasource.install"};

    private static final String[] vaultVariableStrings = {"vault.keystoreloc", "vault.encrdir", "vault.itercount", "vault.alias", "vault.salt", "vault.keystorepwd"};

    private static boolean loadVaultDefault = true;
    private static boolean loadVaultBackup = false;
    private AutomatedInstallData idata;

    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler) {
        this.idata = idata;
        if (isAutoInstall()) {
            return;
        }

        if (!isAdvancedConfigSelected()) {
            setAdvancedConfigVariables(false);
        }

        if (isInstallJdbc()) {
            if (requiresDefaultDatabase()) {
                unsetDatabaseUsernamePassword();
            }
        } else {
            setJdbcVariablesToH2();
            if (requiresDefaultDatabase()) {
                setDatabaseVariableDefaults();
            }
        }

        if (vaultExists()) {
            setVaultDefaults();
        }

        if (isInstallDbLogging() && isProductName(ValidatorConstants.dv)) {
            setDatabaseLoggingDefaults();
        }
    }

    private void setVaultDefaults() {
        if (isLoadDefaultVault()) {
            backupVaultVariables();
            loadDefaultVault();
        } else if (isLoadBackupVault()) {
            restoreBackedUpVault();
        }
    }

    private void loadDefaultVault() {
        for (String vaultVariable : vaultVariableStrings) {
            idata.setVariable(vaultVariable, idata.getVariable(vaultVariable + ".default"));
        }
    }

    private void unsetDatabaseUsernamePassword() {
        idata.getVariables().remove("db.user");
        idata.getVariables().remove("db.password");
    }

    private boolean vaultExists() {
        return idata.getVariable("installVault") != null;
    }

    private boolean isLoadDefaultVault() {
        return  !isInstallVault() && requiresDefaultVault() && loadVaultDefault;
    }

    private boolean isInstallVault() {
        String installVault = idata.getVariable("installVault");
        return Boolean.parseBoolean(installVault);
    }

    private boolean isLoadBackupVault() {
        return isInstallVault() && requiresDefaultVault() && loadVaultBackup;
    }

    private void backupVaultVariables() {
        for (String vaultString : vaultVariableStrings) {
            if (idata.getVariable(vaultString) == null) {
                idata.setVariable(vaultString + ".backup", "");
            } else {
                idata.setVariable(vaultString + ".backup", idata.getVariable(vaultString));
            }
        }
        loadVaultBackup = true;
        loadVaultDefault = false;
    }

    private void restoreBackedUpVault() {
        if (idata.getVariable("vault.keystorepwd") != null) {
            idata.setVariable("vault.keystorepwd.default", idata.getVariable("vault.keystorepwd"));
        }
        for (String vaultString : vaultVariableStrings) {
            if (idata.getVariable(vaultString + ".backup") != null) {
                idata.setVariable(vaultString, idata.getVariable(vaultString + ".backup"));
            }
        }
        loadVaultDefault = true;
        loadVaultBackup = false;
    }

    private void setDatabaseLoggingDefaults() {
        String defaultH2UserName = idata.getVariable("dblogging.h2.username.default");
        String defaultH2Password = idata.getVariable("dblogging.h2.password.default");
        String defaultUrl = idata.getVariable("dblogging.url.default");
        String defaultDriver = idata.getVariable("dblogging.driver.default");
        idata.setVariable("dblogging.driver", defaultDriver);
        idata.setVariable("dblogging.url", defaultUrl);
        idata.setVariable("dblogging.username", defaultH2UserName);
        idata.setVariable("dblogging.password", defaultH2Password);
    }

    private boolean isInstallDbLogging() {
        String dbLoggingOption = idata.getVariable("postinstall.dblogging.enabled");
        return dbLoggingOption != null && Boolean.parseBoolean(dbLoggingOption);
    }

    private void setDatabaseVariableDefaults() {
        idata.setVariable("db.url", idata.getVariable("db.default.url"));
        idata.setVariable("db.user", idata.getVariable("db.default.user"));
        idata.setVariable("db.password", idata.getVariable("db.default.password"));
    }

    private void setJdbcVariablesToH2() {
        idata.setVariable("jdbc.driver.name", JBossJDBCConstants.h2JdbcName);
        idata.setVariable("db.dialect", JBossJDBCConstants.h2Dialect);
        idata.setVariable("db.driver", JBossJDBCConstants.h2JdbcName);
    }

    private boolean isInstallJdbc() {
        String driverInstall = idata.getVariable("jdbc.driver.install");
        return driverInstall != null && Boolean.parseBoolean(driverInstall);
    }

    private boolean isInstallSsl() {
        return idata.getVariables().containsKey("installSsl") && idata.getVariable("installSsl").equalsIgnoreCase("true");
    }

    private boolean isProductName(String productName) {
        return idata.getVariable("product.name").equalsIgnoreCase(productName);

    }

    private void setAdvancedConfigVariables(boolean value) {
        for (String option : postInstallVars) {
            idata.setVariable(option, Boolean.toString(value));
        }
    }

    private boolean isAdvancedConfigSelected() {
        String radioButtonState = idata.getVariable("postinstallServer");
        return radioButtonState != null && Boolean.parseBoolean(radioButtonState);
    }

    private boolean isAutoInstall() {
        return idata.getVariable("installerMode").equals("AUTO");
    }

    /**
     * Attempt to set default values f) || the vault only if neccessary
     * Attempting to set deafult values for vault when no neccecarry may cause NPE
     * This is because the vault default variables have not been set
     */
    public boolean requiresDefaultDatabase() {
        return Boolean.parseBoolean(idata.getVariable("requires.default.database"));
    }

    public boolean requiresDefaultVault() {
        return Boolean.parseBoolean(idata.getVariable("requires.vault.defaults.reset"));
    }

    // Nothing to initialize.
    public void initialize(PanelActionConfiguration configuration) {
    }
}
