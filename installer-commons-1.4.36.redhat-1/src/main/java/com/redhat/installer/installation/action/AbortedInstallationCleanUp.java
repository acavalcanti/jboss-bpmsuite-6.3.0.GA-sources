package com.redhat.installer.installation.action;

import com.izforge.izpack.event.SimpleInstallerListener;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.Unpacker;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.CleanupClient;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Responsible for cleaning up leftover files after an aborted installation.
 * Warning: This cleanup agent needs to be run before the existingInstallationBackupAction's cleanup agent,
 * therefore it must be registered to the Housekeeper *after* the ExistingInstallationBackupAction, since
 * cleanup agents are run in first in, last out order.
 *
 * Created by fcanas on 5/22/14.
 */
public class AbortedInstallationCleanUp extends SimpleInstallerListener implements CleanupClient {
    @Override
    public void cleanUp() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String aborted = idata.getVariable("install.aborted");
        String installPath = idata.getVariable("INSTALL_PATH") + File.separator;
        File installFolder = new File(installPath);

        // If installation was aborted, we destroy the original installFolder
        if (aborted != null && Boolean.parseBoolean(aborted)) {

            // give the unpacker time to stop (if it's still running)
            if (!Unpacker.interruptAll(40000))
            {
                return;
            }

            try {
                FileUtils.deleteDirectory(installFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
/**
 * This method will be called from the unpacker before the installation of all packs will be
 * performed.
 *
 * @param idata   object containing the current installation data
 * @param npacks  number of packs which are defined for this installation
 * @param handler a handler to the current used UIProgressHandler
 * @throws Exception
 **/
    @Override
    public void beforePacks(AutomatedInstallData idata, Integer npacks,
                            AbstractUIProgressHandler handler) throws Exception
    {
        Debug.log("Registering AbortedInstallationCleanup for Cleanup.");
        Housekeeper.getInstance().registerForCleanup(this);
    }
}
