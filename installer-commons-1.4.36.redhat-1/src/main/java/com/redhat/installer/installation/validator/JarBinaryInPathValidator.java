package com.redhat.installer.installation.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.io.IOException;

/**
 * Created by eunderhi on 22/10/15.
 * Cluster configuration calls the "jar" command in postinstallation
 * this validator makes sure the jar binary exists in the path so it can
 * fail the installation ahead of time and be more helpful to the user.
 */
public class JarBinaryInPathValidator implements DataValidator {
    private static String error = "cluster.demo.no.jar";

    @Override
    public Status validateData(AutomatedInstallData adata) {
        error = adata.langpack.getString(error);
        return executeCommand();
    }

    private Status executeCommand() {
        try {
            new ProcessBuilder("jar").start();
        }
        catch (IOException e) {
            return Status.ERROR;
        }
        return Status.OK;
    }

    @Override
    public String getErrorMessageId() {
        return error;
    }

    @Override
    public String getWarningMessageId() {
        return null;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return error;
    }
}
