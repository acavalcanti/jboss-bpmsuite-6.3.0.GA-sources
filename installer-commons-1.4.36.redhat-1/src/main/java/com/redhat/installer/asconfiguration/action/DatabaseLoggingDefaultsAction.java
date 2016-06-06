package com.redhat.installer.asconfiguration.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;

/**
 * Non-general class for the express purpose of setting the defaults of the DV Logging panel
 * to be appropriate for a given database type. Necessary because of inflexibility in the izpack
 * combo box implementation
 * <p/>
 * <p/>
 * Created by thauser on 7/10/14.
 */
public class DatabaseLoggingDefaultsAction implements PanelAction {
    private AutomatedInstallData idata;
    @Override
    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler) {
        this.idata = idata;
        setDefaultH2Url();
        if (isJdbcInstalled()){
            resetUsernamePassword();
            resetComboBox();
            setSpecificJdbcUrl();
        }
    }

    private void setSpecificJdbcUrl() {
        String jdbcDriverName = idata.getVariable("jdbc.driver.name");
        String existingUrl = idata.getVariable("dblogging.url");
        String subProtocol = jdbcDriverName.equals(JBossJDBCConstants.ibmJdbcName) ? "db2" : jdbcDriverName;

        if (existingUrl == null || !existingUrl.startsWith("jdbc:" + subProtocol)) {
            String finalUrl = JBossJDBCConstants.connUrlMap.get(jdbcDriverName);
            idata.setVariable("dblogging.url", finalUrl);
        }
    }

    private void resetComboBox() {
        String defaultDriver = idata.getVariable("dblogging.driver.jdbc.default");
        idata.setVariable("dblogging.driver", defaultDriver);
    }

    private void resetUsernamePassword() {
        String defaultH2Password = idata.getVariable("dblogging.h2.password.default");
        String existingPassword = idata.getVariable("dblogging.password");
        String defaultJDBCUserName = idata.getVariable("dblogging.jdbc.username.default");

        idata.setVariable("dblogging.username", defaultJDBCUserName);
        if (existingPassword == null || existingPassword.equals(defaultH2Password)){
            idata.setVariable("dblogging.password", "");
        }
    }

    private void setDefaultH2Url() {
        String defaultUrl = idata.getVariable("dblogging.url.default");
        idata.setVariable("dblogging.url", defaultUrl);
    }

    private boolean isJdbcInstalled() {
        String driverChoice = idata.getVariable("jdbc.driver.install");
        return driverChoice != null && Boolean.parseBoolean(driverChoice);
    }

    @Override
    public void initialize(PanelActionConfiguration configuration) {}


}
