package org.jboss.brmsbpmsuite.patching.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Responsible for running the actual client patcher and its phases.
 */
public class ClientPatcherRunner {
    private static final Logger logger = LoggerFactory.getLogger(ClientPatcherRunner.class);

    public static final SimpleDateFormat BACKUP_DIR_NAME_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");

    private final ClientPatcherConfig config;

    public ClientPatcherRunner(ClientPatcherConfig config) {
        this.config = config;
    }

    public void run() {
        config.validate();
        logger.debug("Running client patcher with following configuration: {}", config);
        Patcher patcher = PatcherFactory.newPatcher(config.getDistributionType(), config.getProduct(),
                config.getDistributionRoot(), config.getPatchBasedir());
        runPatcher(patcher);
    }

    public void runPatcher(Patcher patcher) {
        runPatcher(patcher, getBackupDir(config.getBackupBaseDir(), config.getDistributionType()));
    }

    public void runPatcher(Patcher patcher, File backupDir) {
        List<PatchingPhase> phasesToExecute = config.getPhasesToExecute();
        logger.debug("Executing following phases: " + phasesToExecute);
        if (phasesToExecute.contains(PatchingPhase.CHECK_DISTRO)) {
            patcher.checkDistro();
        }

        if (phasesToExecute.contains(PatchingPhase.BACKUP)) {
            try {
                patcher.backup(backupDir);
            } catch (IOException e) {
                String msg = "Unexpected error occurred when backing-up the distribution!";
                logger.error(msg, e);
                throw new ClientPatcherException(msg, e);
            }
        }
        if (phasesToExecute.contains(PatchingPhase.APPLY)) {
            try {
                patcher.apply();
            } catch (IOException e) {
                String msg = "Unexpected error occurred when applying the patch!";
                logger.error(msg, e);
                throw new ClientPatcherException(msg, e);
            }
        }
        if (phasesToExecute.contains(PatchingPhase.VERIFY)) {
            try {
                patcher.verify();
            } catch (Exception e) {
                String msg = "Unexpected error occurred when verify the patched distribution!";
                logger.error(msg, e);
                throw new ClientPatcherException(msg, e);
            }
        }
        if (phasesToExecute.contains(PatchingPhase.CLEAN_UP)) {
            patcher.cleanUp();
        }
    }

    protected File getBackupDir(File backupBaseDir, DistributionType distroType) {
        return getBackupDir(backupBaseDir, distroType, new Date());
    }

    protected File getBackupDir(File backupBaseDir, DistributionType distroType, Date date) {
        String timestampDirName = BACKUP_DIR_NAME_DATE_FORMAT.format(date);
        File backupDir = new File(backupBaseDir, timestampDirName + "/" + distroType.getName());
        // make sure the directory does not exist, if it does append "_2" or "_3" (etc) to make it unique
        int counter = 2;
        while (backupDir.exists()) {
            backupDir = new File(backupBaseDir, timestampDirName + "/" + distroType.getName() + "_" + counter);
            counter++;
        }
        return backupDir;
    }
}
