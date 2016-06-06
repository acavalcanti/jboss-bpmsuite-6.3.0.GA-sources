package com.redhat.installer.password.validator;


import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

/**
 * SharedPasswordValidator is used to compare a password to multiple usernames.
 *
 * Usage:
 *
 * In install.xml, in the shared password panel:
 * <validator classname="com.redhat.installer.password.validator.SharedPasswordValidator" id="shared.password.check" />
 *
 * In variables.xml, two comma-separated lists, one of conditions and one of idata variable names:
 * <variable name="SharedPasswordValidator.conditions" value="condition1,condition2"/>
 * <variable name="SharedPasswordValidator.usernameIDs" value="usernameID1,usernameID2"/>
 * Entries in the conditions list can be empty, corresponding to a username that is always compared to the password.
 *
 * In langpacks:
 * <str id="SharedPasswordValidator.match" txt="Shared password cannot match %s: %s."/>
 */
public class SharedPasswordValidator implements DataValidator {
    //TODO: implement conditional validation in izpack, and this whole class becomes unnecessary

    private String message;
    private String error;

    public Status validateData(AutomatedInstallData idata) {
        String samePasswordCheck = idata.getVariable("use.same.password");
        String addUser = idata.getVariable("addUser");

        if (samePasswordCheck.equals("true")) {
            String password;
            if(addUser.equals("false")){
                password = idata.getVariable("masterPassword");
            }
            else {
                password = idata.getVariable("adminPassword");
            }
            String conditions = idata.getVariable("SharedPasswordValidator.conditions");
            String usernameIDs = idata.getVariable("SharedPasswordValidator.usernameIDs");

            String[] conditionList = conditions.split(",");
            String[] usernameIDList = usernameIDs.split(",");

            for (int i = 0; i < usernameIDList.length; i++) {
                String condition = conditionList[i];
                String usernameID = usernameIDList[i];
                String username = idata.getVariable(usernameID);

                // isConditionTrue() returns true if the given condition doesn't exist.
                // Thus, the password is checked by default if no condition is provided.
                if (idata.getRules().isConditionTrue(condition) && password.equals(username)) {
                    // By convention, the langpack string corresponding to the usernameID is its name.
                    String usernameLabel = idata.langpack.getString(usernameID);

                    setError("SharedPasswordValidator.match");
                    setMessage(String.format(idata.langpack.getString(getErrorMessageId()), usernameLabel, username));
                    return Status.ERROR;
                }
            }
        }

        return Status.OK;
    }

    private void setError(String string) {
        error = string;
    }

    private void setMessage(String string) {
        message = string;
    }

    public String getErrorMessageId() {
        return error;
    }

    // Will never occur
    public String getWarningMessageId() {
        return "";
    }

    public boolean getDefaultAnswer() {
        return false;
    }

    public String getFormattedMessage() {
        return message;
    }
}
