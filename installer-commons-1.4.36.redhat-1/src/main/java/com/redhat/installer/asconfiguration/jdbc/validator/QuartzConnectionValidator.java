package com.redhat.installer.asconfiguration.jdbc.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;

/**
 * Created by thauser on 7/28/15.
 */
public class QuartzConnectionValidator extends DatabaseConnectionValidator {
    @Override
    protected Object getDatabaseConnection() {
        Driver driver = getDriverInstance();
        String quartzUser = idata.getVariable("quartz.db.username");
        String quartzPassword = idata.getVariable("quartz.db.password");
        String quartzUrl = idata.getVariable("quartz.db.url");
        if (driver == null) {
            setErrorMessageId("QuartzConnectionValidator.driver.instantiation.failed");
            setFormattedMessage(String.format(idata.langpack.getString(getErrorMessageId())));
            return null;
        }
        return JDBCConnectionUtils.getDatabaseConnection(driver, quartzUser, quartzPassword, quartzUrl);
    }

    @Override
    protected String getDriverName() {
        return idata.getVariable("jdbc.driver.name");
    }

    @Override
    protected boolean isDriverNameValid() {
        return true;
    }
}