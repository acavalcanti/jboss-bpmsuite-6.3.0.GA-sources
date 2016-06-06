package com.izforge.izpack.panels;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

/**
 * Implemented by DataValidators that allow the user to skip entering an invalid input.
 * Used by IzPanel.java, AutomatedInstaller.java and ConsoleInstaller.java's validatePanel
 * methods.
 * See Installer-common's DuplicateUserValidator as an example of an implementor.
 * Implementor...is that a real word?
 *
 * Created by fcanas@redhat.com on 12/30/13.
 */
public interface SkippableDataValidator extends DataValidator {

    /**
     * Runs when a user chooses to skip the currently-validating input.
     * Contains any additional logic needed by the installer when a user input
     * is skipped.
     * @param iData
     */
    public boolean skipActions(AutomatedInstallData iData, int userChoice);

    /**
     * @return Messages for the skip warning's option labels.
     */
    public String [] getSkipOptionLabels(AutomatedInstallData iData);

    /**
     * @return The id of the msg displayed to the user when the choice to skip this input comes up.
     */
    public String getSkipMessageId();

    /**
     * @return The id of the msg displayed during console installs showing the options that the
     * user has.
     */
    public String getConsoleOptionsId();

    /**
     * This returns the skip validator's default 'userChoice' for any
     * skipActions it may perform. Used by automated installations.
     * @return the default 'userChoice' in skipActions.
     */
    public int getDefaultChoice();
}
