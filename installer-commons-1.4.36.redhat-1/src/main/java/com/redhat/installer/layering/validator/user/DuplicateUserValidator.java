package com.redhat.installer.layering.validator.user;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.SkippableDataValidator;
import com.izforge.izpack.util.Debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Checks the user-mgmnt-properties.xml files for both domain and standalone modes
 * to ensure that the installer is not overwriting an existing username in either
 * file.
 * <p/>
 * Will warn if a duplicate username is found, so the user can decide to
 * overwrite or not.
 *
 * @author fcanas
 */
abstract public class DuplicateUserValidator implements SkippableDataValidator {

    private final String[] REALMS = new String[]{"standalone", "domain"};
    private final String CONFIG_DIR = "configuration";
    //private final String INSTALL_SUBPATH = "INSTALL_SUBPATH";

    /**
     * Error and warning messages:
     */
    private static final String error = "DuplicateUsername.errorMsg";
    private static final String warning = "DuplicateUsername.warningMsg";
    private String message;
    private static String skipMessage = "DuplicateUsername.warning.title";
    private static String overwrite = "usermsg.overwrite";
    private static String skip = "usermsg.skip";
    private static String cancel = "usermsg.cancel";
    private static String consoleOptions = "DuplicateUsername.console.options";

    /**
     * We use a hashmap of hashsets to keep track of existing usernames in their
     * respective realm (domain or standalone).
     */
    private HashMap<String, HashSet<String>> usersMap = new HashMap<String, HashSet<String>>();

    protected abstract String getUserVar();

    protected abstract String getCondVar();

    protected abstract String getFileName();

    protected abstract Status getConflictStatus();

    public Status validateData(AutomatedInstallData idata) {

        for (String realm : REALMS) {
            usersMap.put(realm, new HashSet<String>());
        }

        String userFile = getFileName();

        String userVar = getUserVar();
        String user = (String) idata.getVariable(userVar);

        String addCondVar = getCondVar();
        Status conflictStatus = getConflictStatus();

        if (addCondVar != null) {
            String addUser = idata.getVariable(addCondVar);
            if ((addUser != null) && addUser.equals("false")) {
                idata.setVariable("add.new.user", "false");
                return Status.OK;
            }
        }

        for (String realm : REALMS) {
            String filePath = getPath(realm, idata, userFile);

            // Fill the user map:
            if (readExistingUsers(realm, filePath)) {

                if (usersMap.get(realm).contains(user)) {
                    if (conflictStatus == Status.ERROR)
                        message = String.format(idata.langpack.getString(error), realm, user);
                    if (conflictStatus == Status.WARNING)
                        message = String.format(idata.langpack.getString(warning), user);
                    if (conflictStatus == Status.SKIP) message = String.format(idata.langpack.getString(warning), user);
                    return conflictStatus;
                }

            }
        }

        doExtraWork();

        return Status.OK;
    }

    /**
     * Opens the user file and reads existing users into idata.
     * This occurs for both management realms: domain and standalone.
     */
    public boolean readExistingUsers(String realm, String filePath) {

        String line;
        BufferedReader in = null;
        try {

            FileReader reader = new FileReader(filePath);
            in = new BufferedReader(reader);

            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) continue;
                // Upon finding a username entry, map the username.
                if (line.contains("=")) {
                    String username = line.substring(0, line.indexOf('='));
                    usersMap.get(realm).add(username);
                }
            }
        } catch (IOException e) {
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;

    }


    private String getPath(String realm, AutomatedInstallData idata, String fileName) {

        String rootDir = idata.getInstallPath();
        //String subDir   = idata.getVariable(INSTALL_SUBPATH);
        String filePath = rootDir + File.separator +
                realm + File.separator + CONFIG_DIR + File.separator + fileName;
        return filePath;

    }

    public String getErrorMessageId() {
        return error;
    }

    public String getFormattedMessage() {
        return message;
    }

    public String getWarningMessageId() {
        return warning;
    }

    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public int getDefaultChoice() {
        return 1;
    }

    @Override
    public boolean skipActions(AutomatedInstallData idata, int userChoice) {
        boolean returnValue = false;

        if (userChoice == 0) {
            idata.setVariable("add.new.user", "true");
            returnValue = true;
            Debug.trace("... but user decided to go on!");
        } else if (userChoice == 1) {
            // Don't write the dup user to file later.
            idata.setVariable("add.new.user", "false");
            returnValue = true;
            Debug.trace("... but user decided to go on!");
        } else {
            returnValue = false;
            Debug.trace("... and the user decided to stop.");
        }

        return returnValue;
    }

    @Override
    public String[] getSkipOptionLabels(AutomatedInstallData idata) {
        return new String[]{
                idata.langpack.getString(overwrite),
                idata.langpack.getString(skip),
                idata.langpack.getString(cancel)};
    }

    @Override
    public String getSkipMessageId() {
        return skipMessage;
    }

    @Override
    public String getConsoleOptionsId() {
        return consoleOptions;
    }


    protected void doExtraWork() {
        //Empty
    }
}
