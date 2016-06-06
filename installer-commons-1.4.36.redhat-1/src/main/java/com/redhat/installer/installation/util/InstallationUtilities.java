package com.redhat.installer.installation.util;

import com.izforge.izpack.installer.UninstallData;
import com.izforge.izpack.util.Debug;

import java.io.*;

public class InstallationUtilities {
    private final static String SEP = File.separator;
    private final static String pathToMgmtFile = SEP + "configuration" + SEP + "mgmt-users.properties";
    private final static String[] profiles = new String[]{"standalone", "domain"};

    /**
     * Removes a user/pwd line from the mgmt-users.properties file.
     *
     * @param username The username to look for and remove.
     * @param path     JBOSS_HOME of the installation with the user.
     */
    public static void removeManagementUser(String username, String path) {
        String line;
        BufferedReader in = null;
        FileWriter writer = null;
        try {
            StringBuilder file = new StringBuilder();
            for (String profile : profiles) {
                FileReader reader = new FileReader(path + SEP + profile + pathToMgmtFile);
                in = new BufferedReader(reader);
                boolean foundUsername = false;
                file.setLength(0);

                // Loop through the entire file and copy it.
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("#")) { // skip comments immediately
                        continue;
                    }
                    if (line.contains(username)) {
                        foundUsername = true;
                    } else {
                        file.append(line);
                        file.append(System.getProperty("line.separator"));
                    }
                }

                if (foundUsername) {
                    writer = new FileWriter(path + "/" + profile + pathToMgmtFile, false);
                    writer.write(file.toString());
                    writer.close();
                }
            }

        } catch (Exception e) {
            Debug.log("Error replacing mgmt-users.properties file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Adds the file at the given path to the list of files to delete upon an aborted installation
     *
     * @param path the path to the file
     */
    public static void addFileToCleanupList(String path) {
        UninstallData udata = UninstallData.getInstance();
        udata.getInstalledFilesList().add(path);
    }
}
