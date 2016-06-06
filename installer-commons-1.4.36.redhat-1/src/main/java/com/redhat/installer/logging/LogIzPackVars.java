package com.redhat.installer.logging;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import com.redhat.installer.installation.util.InstallationUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.Set;

public class LogIzPackVars {
    private static AutomatedInstallData idata;
    private static final String LOG_FILE_VAR = "installation.logfile";
    private static final String DEFAULT_FILE = "defaultInstallLog.txt";
    private static PrintStream logPrintStream;
    private static File logFile;

    public static void run(AbstractUIProcessHandler handler, String[] args) {
        initializeStaticVariables();
        writeIzPackVariablesToLog();
        ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("LogIzPackVars.message") + " " + logFile.getAbsolutePath(), false);
    }

    private static void initializeStaticVariables() {
        idata = AutomatedInstallData.getInstance();
        setLogFile();
        logPrintStream = new PrintStream(getLogFileStream());
    }

    private static void setLogFile() {
        String installLogPath = idata.getVariable(LOG_FILE_VAR);

        if (installLogPath != null) {
            logFile = new File(idata.getInstallPath() + "/" + installLogPath);
            File logFileParent = logFile.getParentFile();
            if (!logFileParent.exists()) {
                logFileParent.mkdirs();
            }
        } else {
            // fall back guaranteed to exist
            logFile = new File(idata.getInstallPath() + "/" + DEFAULT_FILE);
        }
    }

    private static FileOutputStream getLogFileStream() {
        FileOutputStream returnStream = null;
        try {
            returnStream = new FileOutputStream(logFile.getAbsoluteFile());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InstallationUtilities.addFileToCleanupList(logFile.getAbsolutePath());
        return returnStream;
    }

    private static void writeIzPackVariablesToLog() {
        Properties allVars = idata.getVariables();
        Set<Object> keys = allVars.keySet();
        for (Object key : keys) {
            String realKey = (String) key;
            String realValue = (String) allVars.get(key);
            if (realKey.toLowerCase().contains("password")
                    || realKey.toLowerCase().contains("pwd")
                    || realKey.toLowerCase().contains("pass")) {
                // don't print password fields
                continue;
            } else if (realKey.toLowerCase().contains("system_sun_java_command")) {
                // don't print passwords when passed in as cmnd line args:
                logPrintStream.println(realKey + " = " + obfuscateCmdLineVariablePasswords(realValue));
            } else {
                logPrintStream.println(realKey + " = " + realValue);
            }
        }
    }


    /**
     * Given a comma separated list of key=value pairs, obfuscate the
     * values of all the keys that contain the words password or pwd in them.
     *
     * @param values
     * @return
     */
    public static String obfuscateCmdLineVariablePasswords(String values) {
        String reg = "((P|p)(assword|wd)=)(.*?)(,|$)";
        String ret = values.replaceAll(reg, "$1********$5");
        return ret;
    }


}
