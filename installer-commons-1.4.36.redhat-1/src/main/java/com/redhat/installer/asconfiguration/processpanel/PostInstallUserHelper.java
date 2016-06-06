package com.redhat.installer.asconfiguration.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.installation.util.InstallationUtilities;
import com.redhat.installer.installation.processpanel.ArgumentParser;

/**
 * We use this process panel class to temporarily add an administrative account
 * user so we can modify install settings using the CLI during the post-install
 * portion of the installation.
 * 
 * We call the cleanUp method during installation shutdown in order to shut off
 * any running instances of the server, and then to remove the post-install user
 * from the server config files.
 * 
 * @author fcanas
 * 
 */
public class PostInstallUserHelper {
    public static final String USERNAME_VAR = "postinstall.username";
    public static final String PWD_VAR = "postinstall.password";
    public static final String REALM_VAR = "postinstall.realm";
    public static final String PATH_VAR = "postinstall.path";
    public static final String username = "POSTINSTALL.TEMPUSER";
    public static final String password = "POSTINSTALL.TEMPPASSWORD";

    private static AutomatedInstallData idata;
    private static final String ADD_USER = "add_user";
    private static final String REMOVE_USER = "remove_user";
    private static final String ADD_POSTUSER = "PostInstallUser.user.add";
    private static final String FAIL_POSTUSER = "PostInstallUser.user.fail";

    public static void run(AbstractUIProcessHandler handler, String[] args)
            throws Exception {
        idata = AutomatedInstallData.getInstance();

        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);

        String action = args[0];

        if (action.contains(REMOVE_USER)) {
            removePostInstallUser();
        }
    }

    /**
     * Remove the temporary user used during post install to connect to the CLI.
     */
    public static void removePostInstallUser() {
        idata = AutomatedInstallData.getInstance();
        String username = idata.getVariable("postinstall.username");
        String rawPath = idata.getVariable("postinstall.path");

        /**
         * Must substitute for correct INSTALL_PATH value.
         */
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        String path = vs.substitute(rawPath);
        InstallationUtilities.removeManagementUser(username, path);
    }
}
