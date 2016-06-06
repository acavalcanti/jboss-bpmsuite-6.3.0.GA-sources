package com.izforge.izpack.gui.dynamic;

import java.awt.Component;

import com.izforge.izpack.installer.AutomatedInstallData;


public interface DynamicValidator
{
    /**
     * tag-name of the datavalidator
     */
    public static final String DATA_VALIDATOR_TAG = "validator";

    /**
     * attribute for class to use
     */
    public static final String DATA_VALIDATOR_CLASSNAME_TAG = "classname";

    /**
     * Method to validate on {@link AutomatedInstallData}
     * 
     * @param adata
     * @return {@link Status} the result of the validation
     */
    public boolean validateData(final String property, final String value);

    /**
     * Returns the string with messageId for an error
     * 
     * @return String the messageId
     */
    public String getErrorMessageId();

    /**
     * Returns the string with messageId for a warning
     * 
     * @return String the messageId
     */
    public String getWarningMessageId();

    /**
     * if Installer is run in automated mode, and validator returns a warning, this method is asked,
     * how to go on
     * 
     * @return boolean
     */

    public boolean getDefaultAnswer();
}
