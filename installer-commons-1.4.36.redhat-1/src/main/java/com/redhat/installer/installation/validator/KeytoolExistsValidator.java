package com.redhat.installer.installation.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

/**
 * Responsible for detecting for the presence of a keytool on the system.
 * If keytool doesn't exist, we warn user but let them continue.
 * For automated installations, the warning can be ignored.
 * Created by fcanas on 8/8/14.
 */
public class KeytoolExistsValidator implements DataValidator {
    private String warningId = "KeytoolExistsValidator.noKeytoolFound.warning";
    private String message;
    private String error;
    private String warning;
    private AutomatedInstallData idata;

    @Override
    public Status validateData(AutomatedInstallData idata) {
        this.idata = idata;

        if (keytoolExists()) {
            return Status.OK;
        } else {
            this.message = idata.langpack.getString(warningId);
            this.warning = this.message;
            return Status.WARNING;
        }

    }

    /**
     * Check for presence of keytool.
     * @return true if keytool runs. False otherwise.
     */
    private boolean keytoolExists() {
        String [] args = new String[] {
            "keytool"
        };

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int returnVal = process.waitFor();

            if (returnVal == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getErrorMessageId() {
        return error;
    }

    @Override
    public String getWarningMessageId() {
        return warning;
    }

    @Override
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }
}
