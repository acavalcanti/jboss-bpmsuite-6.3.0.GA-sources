package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileExtensions;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.WorkspaceUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integration tests that test backup capability of patch tool.
 */
public class BackupIT extends AbstractOneDistributionForAllTestsIT {

    @Test
    public void testBackup() throws IOException, InterruptedException {
        patchDistributionAndValidSuccessfulPatchScriptRun(getTestingDistribution());
        final File backupDirectory = WorkspaceUtil.findFirstDistributionBackup(getTestingDistribution().getType());
        // If the content root directory is a zip file, only this file should be backed up,
        // so only this file is compared with file with the same name in backup directory.
        // And there should be nothing else.
        final File startDistContentRootDir = getStartingDistribution().getContentRootDirectory();
        if (FileExtensions.isZipArchive(startDistContentRootDir)) {
            final File backedWarFile =
                    new File(backupDirectory.getPath() + File.separator + startDistContentRootDir.getName());
            final boolean warsCompare = FileUtil.compareFiles(startDistContentRootDir, backedWarFile, true);
            if (!warsCompare) {
                logger.warn("File that is different: " + backedWarFile.getPath());
                logger.warn("File that is different: " + startDistContentRootDir.getPath());
                Assert.assertTrue("Backed up distribution war file is not the same as original! " + Constants.MESSAGE_SEE_RECENT_LOG,
                        warsCompare);
            }
            final File[] backupDirectoryFiles = backupDirectory.listFiles();
            Assert.assertTrue(
                    "Backed up directory " + backupDirectory.getPath() + " should contain only file " + backedWarFile.getName(),
                    backupDirectoryFiles != null && backupDirectoryFiles.length == 1);
        } else {
            final Set<File> filesThatAreDifferent = FileUtil.compareDirectories(
                    backupDirectory, getStartingDistribution().getContentRootDirectory(), false, true);
            for (File file : filesThatAreDifferent) {
                logger.warn("File that is different: " + file.getPath());
            }
            Assert.assertTrue("Backed up distribution is not the same as original! " + Constants.MESSAGE_SEE_RECENT_LOG,
                    filesThatAreDifferent.isEmpty());
        }
    }
}
