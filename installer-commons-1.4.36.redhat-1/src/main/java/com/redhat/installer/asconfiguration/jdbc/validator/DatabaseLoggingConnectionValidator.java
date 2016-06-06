package com.redhat.installer.asconfiguration.jdbc.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator used by the Verify Database Connection button on DV's dblogging panel
 * Created by thauser on 7/10/14.
 */
public class DatabaseLoggingConnectionValidator extends DatabaseConnectionValidator {
    @Override
    protected Object getDatabaseConnection() {
        Driver driver = getDriverInstance();
        String dbUser = idata.getVariable("dblogging.username");
        String dbPassword = idata.getVariable("dblogging.password");
        String dbUrl = idata.getVariable("dblogging.url");
        if (driver == null){
            setErrorMessageId("DatabaseLoggingConnectionValidator.driver.instantiation.failed");
            setFormattedMessage(String.format(idata.langpack.getString(getErrorMessageId())));
            return null;
        }
        // note: this getDatabaseConnection method returns either a String or a Connection object; this allows passing the failure string from
        // the database along.
        return JDBCConnectionUtils.getDatabaseConnection(driver, dbUser, dbPassword, dbUrl);
    }

    protected String getDriverName() {
        String driverName = idata.getVariable("dblogging.driver");
        if (driverName.equals("installed")) {
            driverName = idata.getVariable("jdbc.driver.name");
        }
        return driverName;
    }
}
