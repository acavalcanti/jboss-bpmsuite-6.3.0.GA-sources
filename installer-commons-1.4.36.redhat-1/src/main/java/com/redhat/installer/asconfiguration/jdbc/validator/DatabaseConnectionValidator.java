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
 * Created by thauser on 7/28/15.
 */
public abstract class DatabaseConnectionValidator implements DataValidator, Validator {

    protected AutomatedInstallData idata;
    protected String errorMessageId;
    protected String formattedMessage;

    protected abstract String getDriverName();
    protected abstract Object getDatabaseConnection();

    @Override
    public Status validateData(AutomatedInstallData idata){
        this.idata = idata;

        if (!isDriverNameValid()){
            setErrorMessageId("DatabaseConnectionValidator.missing.using.h2");
            setFormattedMessage(idata.langpack.getString(getErrorMessageId()));
            return Status.ERROR; // can't test with H2
        }

        if (!connectToDatabase()){
            return Status.ERROR;
        }
        return Status.OK;
    }

    protected boolean connectToDatabase(){
        Object connection = getDatabaseConnection();

        if (connection != null){
            if (connection.getClass().equals(String.class)){
                setErrorMessageId("DatabaseConnectionValidator.connection.failed");
                setFormattedMessage(idata.langpack.getString(getErrorMessageId()));
                return false;
            }
            try {
                ((Connection) connection).close();
            } catch (SQLException e){
                e.printStackTrace();
            }
            setErrorMessageId("DatabaseConnectionValidator.connection.success");
            setFormattedMessage(idata.langpack.getString(getErrorMessageId()));
            return true;
        } else {
            return false;
        }
    }

    protected Driver getDriverInstance() {
        Class driverClass = findDriverClass();
        if (driverClass == null) {
            return null;
        }
        Driver driver;
        try {
            driver = (Driver) driverClass.newInstance();
        } catch (InstantiationException e){
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
        return driver;
    }
    
    /**
     * Small wrapper around the loadDriverClass in JDBCConnectionUtils
     * @return
     */
    private Class findDriverClass() {
        Set<String> jarPaths = loadJarPaths();
        String driverClassName = JBossJDBCConstants.classnameMap.get(getDriverName());
        Class returnClass = null;
        if (jarPaths.size() > 0){
            returnClass = JDBCConnectionUtils.findDriverClass(driverClassName, JDBCConnectionUtils.convertToUrlArray(jarPaths.toArray()));
        }
        return returnClass;
    }

    /**
     * Loads the jar paths from the JDBC panel
     */
    private Set<String> loadJarPaths() {
        Set<String> jars = new HashSet<String>();
        for (int i = 1; ; i++){
            // format for the variable name is dictated by the DynamicComponentsPanel.serialize method.
            String driverPath = idata.getVariable("jdbc.driver.jar-"+i+"-path");
            if (driverPath==null){
                break;
            } else {
                jars.add(driverPath);
            }
        }
        return jars;
    }

    /**
     * A valid name means that the variable has non null contents and is not "h2"
     * @return
     */
    protected boolean isDriverNameValid() {
        String driverName = getDriverName();
        if (driverName == null){
            setErrorMessageId("DatabaseConnectionValidator.missing.driver.name");
            return false;
        }
        if (driverName.equals(JBossJDBCConstants.h2JdbcName)){
            return false; // no tests performed
        }
        return true;
    }

    @Override
    public boolean validate(ProcessingClient client) {
        Status status = validateData(AutomatedInstallData.getInstance());
        if (status == Status.OK){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getErrorMessageId() {
        return errorMessageId;
    }

    @Override
    public String getWarningMessageId() {
        return errorMessageId;
    }

    @Override
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getFormattedMessage() {
        return null;
    }

    public void setErrorMessageId(String errorMessageId) {
        this.errorMessageId = errorMessageId;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }
}
