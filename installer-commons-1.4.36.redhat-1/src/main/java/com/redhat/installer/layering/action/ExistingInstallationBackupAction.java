package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.installer.PanelActionDialog;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Housekeeper;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.layering.constant.ValidatorConstants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class ExistingInstallationBackupAction extends PanelActionDialog implements CleanupClient
{
    private volatile static AutomatedInstallData idata;
    private static String installPath;
    private static String backupPath;

    private Logger logger;
    private boolean loggerExists;

    private String backupDialogMessage;
    private String restoreDialogMessage;

    @Override
    public void executeAction(AutomatedInstallData adata, AbstractUIHandler handler)
    {
        setLogger(ServerCommandsHelper.createLogger(this.getClass().getName()));

        // Registered so that this runs cleanUp when user quits an installation.
        Housekeeper.getInstance().registerForCleanup(this);

        installPath = idata.getVariable("INSTALL_PATH");
        backupPath = installPath + ValidatorConstants.backupExt;

        File installPathFile = new File(installPath);
        File backupPathFile = new File(backupPath);

        /**
         * If backup path already exists, the user might have changed their target panel
         * to something else. So we clear out current backup and back up the new
         * target path.
         */
        if (backupPathFile.exists())
        {
            try
            {
                FileUtils.deleteDirectory(backupPathFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        /**
         * Back up the entire jboss-eap-6.* directory if existing installation is detected.
         * This way we can restore it without mangling the former installation in the event of some dastardly user quitting early.
         * An action progress dialog will appear during backup.
         */
        if (installPathFile.exists())
        {
            String installerMode = idata.getVariable("installerMode");
            if(installerMode.equals("GUI")) {
                startDialog(this.backupDialogMessage);
                recursiveDirectoryCopy(installPath, backupPath);
                stopDialog();
            }
            else {
                System.out.print(this.backupDialogMessage);
                recursiveDirectoryCopy(installPath, backupPath);
                progressBar();
            }

            idata.setVariable("backup.created", "true");
            if (loggerExists())
            {
                logger.info(String.format(idata.langpack.getString("ExistingInstallationBackupAction.backup.created"), installPath, backupPath));
            }
        }
        closeLogHandlers();
    }

    /**
     * Restores backups of all files as needed.
     */
    public void cleanUp()
    {
        File backupFolder = new File(backupPath);
        File newlyInstalledFolder = new File(installPath);

        String aborted = idata.getVariable("install.aborted");
        boolean wasAborted = aborted != null ? Boolean.parseBoolean(aborted) : false;

        if (backupFolder.exists() && (!idata.installSuccess || wasAborted))
        {
            //Restore the backed up eap installation.
            startDialog(this.restoreDialogMessage);

            //Delete newly created installation
            if (newlyInstalledFolder.exists())
            {
                try
                {
                    String relativeLogsDir = File.separator + "installation";
                    File fileLogDirs = new File(installPath+relativeLogsDir);
                    File backupFolderLogsDir = new File(backupFolder.getAbsolutePath()+relativeLogsDir);
                    if (!backupFolderLogsDir.exists()){
                        backupFolderLogsDir.mkdir();
                    }
                    if (fileLogDirs.isDirectory() && fileLogDirs.exists()) {

                        FileUtils.copyDirectory(fileLogDirs, backupFolderLogsDir);
                    }
                    FileUtils.deleteDirectory(newlyInstalledFolder);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            recursiveDirectoryCopy(backupPath, installPath);
            
            stopDialog();

            if (loggerExists())
            {
                logger.info(String.format(idata.langpack.getString("ExistingInstallationBackupAction.restore.success"), backupPath, installPath));
            }
        }

        if (backupFolder.exists())
        {
            try
            {
                FileUtils.deleteDirectory(backupFolder);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Pre-order traversal of the file system rooted at rootPath while re-creating
     * the file structure but rooted at rootDestPath.
     *
     * @param rootPath     the location to copy from
     * @param rootDestPath the location to copy to
     */
    private void recursiveDirectoryCopy(String rootPath, String rootDestPath)
    {
        Queue<File> files = new LinkedList<File>();
        File root = new File(rootPath);
        files.add(root);

        while (!files.isEmpty())
        {
            File current = files.remove();
            File backup = new File(current.getPath().replace(rootPath, rootDestPath));

            if (current.isDirectory())
            {
                File[] listFiles = current.listFiles();
                if (listFiles != null)
                {
                    Collections.addAll(files, listFiles);
                }

                boolean success = backup.mkdir();
                if (!success && loggerExists())
                {
                    logger.warning(String.format(idata.langpack.getString("ExistingInstallationBackupAction.directory.creation.failure"), backup.getAbsolutePath()));
                }
            }

            copyRetainingPermissions(current, backup);
        }
    }

    private void closeLogHandlers()
    {
        if (loggerExists())
        {
            for (Handler h : logger.getHandlers())
            {
                h.close();
                logger.removeHandler(h);
            }
        }
    }

    /**
     * Attempts to retain read/write/exec permissions when copying files from source to destination.
     *
     * @param source input file
     * @param dest   destination file
     */
    private void copyRetainingPermissions(File source, File dest)
    {
        boolean exec = source.canExecute();
        boolean read = source.canRead();
        boolean write = source.canWrite();

        try
        {
            if (source.isDirectory())
            {
                FileUtils.forceMkdir(dest);
            }
            else
            {
                FileUtils.copyFile(source, dest, true);
            }

            boolean execResult = dest.setExecutable(exec);
            boolean readResult = dest.setReadable(read);
            boolean writeResult = dest.setWritable(write);

            if (!execResult || !readResult || !writeResult)
            {
                if (loggerExists())
                {
                    logger.info(String.format(idata.langpack.getString("ExistingInstallationBackupAction.retain.permission.error"), source.getAbsolutePath()));
                }

            }
        }
        // Throws if it's copying a dir
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sets the instance's logger to the specified Logger object
     *
     * @param logger The logger to set the class' logger to
     */
    private void setLogger(Logger logger)
    {
        this.logger = logger;
        if (logger != null)
        {
            loggerExists = true;
        }
    }

    /**
     * Check if the logger isn't null
     *
     * @return returns a boolean specifying if the logger is null or not
     */
    private boolean loggerExists()
    {
        return loggerExists;
    }

    /**
     * Initializes the logger and the strings used for the titles of the progress dialogs
     *
     * @param configuration additional configuration as specified by the panel
     */
    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
        // just to save time, since izpack doesn't have a good
        // "execute this before any installation occurs, but only once."
        if (idata == null)
        {
            idata = AutomatedInstallData.getInstance();
        }

        backupDialogMessage = idata.langpack.getString("ExistingInstallationBackupAction.backup.dialog.title");
        restoreDialogMessage = idata.langpack.getString("ExistingInstallationBackupAction.restore.dialog.title");
    }

    public void progressBar(){
        String anim = "==============";
        String space = "             ";
        int x = 0;
        while (x < anim.length()) {
            String substring = anim.substring(0, x++ % anim.length());
            System.out.print("\r"+ this.backupDialogMessage
                    +" ["
                    + substring
                    + ">"
                    + space.substring(0, (space.length() - substring.length()))
                    + "]");
            try { Thread.sleep(50); }
            catch (Exception e) {};
        }
        System.out.print("\n");
    }
}
