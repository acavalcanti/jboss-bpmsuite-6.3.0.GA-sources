package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.util.InstallationUtilities;

import java.io.File;
import java.io.IOException;

/**
 * Trivial class to create an empty file. Useful for creating / manipulating .dodeploy files
 * Created by thauser on 12/13/13.
 */
public class EmptyFileCreator {

    public static void run(AbstractUIProcessHandler handler, String[] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        for (String file : args) {
            File emptyFile = new File(file);
            if (!emptyFile.exists()) {
                try {
                    emptyFile.createNewFile();
                    ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("EmptyFileCreator.creation"), emptyFile.getAbsolutePath()), false);
                    InstallationUtilities.addFileToCleanupList(file);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("EmptyFileCreator.creationfailed"), emptyFile.getAbsolutePath()), true);
                }
            } else {
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("EmptyFileCreator.existing"), emptyFile.getAbsolutePath()), false);
            }

        }
    }
}
