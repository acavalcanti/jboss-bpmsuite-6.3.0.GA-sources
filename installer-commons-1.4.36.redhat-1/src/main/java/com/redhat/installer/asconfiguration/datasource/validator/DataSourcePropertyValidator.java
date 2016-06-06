package com.redhat.installer.asconfiguration.datasource.validator;


import java.util.regex.Pattern;


import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.gui.dynamic.DynamicValidator;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;

/** 
 * This class validates user-specified custom properties in the DataSourceConfig
 * panel. It checks them against the JBossJDBCConstants regex, and it also
 * double-checks that the specified jdbc vendor doesn't already use properties
 * by the same name.
 * @author fcanas@redhat.com
 *
 */
public class DataSourcePropertyValidator implements DynamicValidator
{   
    AutomatedInstallData idata = AutomatedInstallData.getInstance();
    private String errorMsg;
    private String badProperty; 
    private String badValue;
    
    public boolean validateData(String property, String value) {
            if (!validateProperty(property, value)) {
                badProperty = property;
                badValue = value;
                return false; 
                }
        return true;
    }

    /**
     * Pattern validation. Checks in stages and sets error messages as necessary.
     * @param property
     * @param value
     * @return True if the value matches all required patterns.
     */
    public boolean validateProperty(String property, String value) {
        /*
         * Validation has been moved into the dynamicComponentsPanel
         * We want to validate data before it goes into idata
         */
    	Pattern equalPattern = Pattern.compile(JBossJDBCConstants.hasEqualNotEmptyRegExpPattern);
        Pattern propPattern = Pattern.compile(JBossJDBCConstants.propRegExpPattern);
        Pattern valuePattern = Pattern.compile(JBossJDBCConstants.valRegExpPattern);

        if (property.equals("") ||  value.equals("")) {
            // Error: Cannot leave a property or value field empty.
            errorMsg = idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.wrongFormat");
            return false;
        }

        //String [] parts = propValuePair.split("=");
        
        
        if (!propPattern.matcher(property).matches()) {
            // Error: Property name may not contain the following characters, unless
            // they are surrounded by quotes: ,{}[]
            errorMsg = idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.invalidChars");
            return false;
        }

        if (!valuePattern.matcher(value).matches()) {
            // Error: Value may not contain the following characters unless they are
            // surrounded by quotes: , {} []
            errorMsg = idata.langpack.getString("JBossDatasourceConfigPanel.xa.error.invalidChars");
            return false;
        }
        return true;
    }

    /**
     * Checks the install data for an existing vendor property of the same name
     * as propertyName.
     * @param propertyName The name of a property to test against.
     * @return True if iData does not contain a property by this name.
     */
    public boolean noVendorPropertyMatch(String propertyName) {
        return !JBossJDBCConstants.vendorPropertyMap.containsKey(propertyName);
    }

    public String getErrorMessageId() {
        // TODO: Add a real string from langpacks along with translations.
        return String.format(errorMsg, badProperty, badValue);
    }

    public String getWarningMessageId() {
        // Should never throw a warning. Either the properties are valid or not.
        return idata.langpack.getString("JBossDatasourceConfigPanel.warning.xaProperty");
    }

    public boolean getDefaultAnswer() {
        return true;
    }
}
