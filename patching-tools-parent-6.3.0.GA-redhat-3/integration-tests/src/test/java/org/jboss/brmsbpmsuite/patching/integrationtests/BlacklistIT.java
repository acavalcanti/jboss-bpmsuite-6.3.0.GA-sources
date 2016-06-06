package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileListException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileStatusCode;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileWithStatus;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests that test blacklist capability of patch tool.
 */
public class BlacklistIT extends AbstractOneDistributionForAllTestsIT {

    private static final Logger logger = LoggerFactory.getLogger(BlacklistIT.class);

    private static final String FILE_NOT_IN_DISTRIBUTION = "not/in/distribution.txt";

    private static boolean unpackedInStartDist = false;
    private static boolean unpackedInTestDist = false;

    /**
     * Prepares environment for individual tests.
     * @throws IOException
     * @throws InterruptedException
     * @throws DistributionException
     */
    @BeforeClass
    public static void prepareTests() throws IOException, InterruptedException, DistributionException {
        final Distribution testingDistribution = getTestingDistribution();
        getBlackList().appendTextToOriginalFile(FILE_NOT_IN_DISTRIBUTION);

        patchDistributionAndValidSuccessfulPatchScriptRun(testingDistribution);
        unpackedInTestDist = getTestingDistribution().unpackWars();
        unpackedInStartDist = getStartingDistribution().unpackWars();
    }

    /**
     * Finalizes tests. Corrects environment, etc.
     * @throws DistributionException
     */
    @AfterClass
    public static void finalizeTests() throws DistributionException {
        if (unpackedInStartDist) {
            getStartingDistribution().packWars();
        }

        if (unpackedInTestDist) {
            getTestingDistribution().packWars();
        }
    }

    /**
     * Tests if blacklists contains file that is not in distribution, that the file is still missing after
     * patching process. Some user can accidentally add some file that is not in distribution to blacklist.
     * This tests this case.
     * @throws FileListException, DistributionException
     */
    @Test
    public void testFileNotInDistribution() throws FileListException {

        final Set<FileWithStatus> notSuccessfullyVerifiedFiles = getBlackList().verifyDistribution(
                getTestingDistribution(), getStartingDistribution(), getTestingPatchToolDistribution());
        final Set<FileWithStatus> filesMissingFromDistribution =
                FileUtil.filterFilesByStatus(notSuccessfullyVerifiedFiles, FileStatusCode.MISSING);
        final FileWithStatus missingFile = new FileWithStatus(
                getTestingDistribution().getBundleRootDirectory() + File.separator + FILE_NOT_IN_DISTRIBUTION,
                FileStatusCode.MISSING);
        Assert.assertTrue("File " + FILE_NOT_IN_DISTRIBUTION + " should not be in patched distribution!",
                filesMissingFromDistribution.contains(missingFile));
    }

    /**
     * Tests if patch tool creates marker files that are not on blacklist. Marker files should be
     * created only for files that are on blacklist.
     * @throws FileListException
     */
    @Test
    public void testMarkerFilesCreatedForFilesNotOnBlacklist() throws FileListException {
        final Set<FileWithStatus> notSuccessfullyVerifiedFiles = getBlackList().verifyDistribution(
                getTestingDistribution(), getStartingDistribution(), getTestingPatchToolDistribution());
        final Set<FileWithStatus> filesNotOnBlacklist =
                FileUtil.filterFilesByStatus(notSuccessfullyVerifiedFiles, FileStatusCode.NOT_ON_BLACKLIST);

        for (FileWithStatus file : filesNotOnBlacklist) {
            logger.error("Patch tool created marker file " + file.getPath() + " for file that is not on blacklist!");
        }
        Assert.assertTrue("Patch tool created marker files for files that are not on blacklist! "
                        + Constants.MESSAGE_SEE_RECENT_LOG,
                filesNotOnBlacklist.isEmpty());
    }

    /**
     * Tests if files that are on blacklist are not overwritten.
     * @throws FileListException
     */
    @Test
    public void testFileOverwrite() throws FileListException {
        final Set<FileWithStatus> notSuccessfullyVerifiedFiles = getBlackList().verifyDistribution(
                getTestingDistribution(), getStartingDistribution(), getTestingPatchToolDistribution());
        final Set<FileWithStatus> overwrittenFiles =
                FileUtil.filterFilesByStatus(notSuccessfullyVerifiedFiles, FileStatusCode.OVERWRITTEN);

        for (FileWithStatus file : overwrittenFiles) {
            logger.error("Patch tool have overwritten file " + file.getPath() + " that is on blacklist!");
        }
        Assert.assertTrue("Patch tool have overwritten some files that are on blacklist! "
                        + Constants.MESSAGE_SEE_RECENT_LOG,
                overwrittenFiles.isEmpty());
    }

    /**
     * Tests if all blacklisted files that exist in distribution have appropriate marker files created.
     * @throws FileListException
     */
    @Test
    public void testMarkerFiles() throws FileListException {
        final Set<FileWithStatus> notSuccessfullyVerifiedFiles = getBlackList().verifyDistribution(
                getTestingDistribution(), getStartingDistribution(), getTestingPatchToolDistribution());
        final Set<FileWithStatus> filesMissingMarkerFiles =
                FileUtil.filterFilesByStatus(notSuccessfullyVerifiedFiles, FileStatusCode.MARKER_FILE_MISSING);

        for (FileWithStatus file : filesMissingMarkerFiles) {
            logger.error("Patch tool have not created marker file for file " + file.getPath() + "!");
        }
        Assert.assertTrue("Patch tool have not created marker files for some files that are on blacklist! "
                        + Constants.MESSAGE_SEE_RECENT_LOG,
                filesMissingMarkerFiles.isEmpty());
    }

    /**
     * Tests if blacklisted files that were not changed by the user have created marker files.
     * Files that are on blacklist, but aren't changed by the user, don't need marker files.
     * They can be overwritten.
     * @throws FileListException
     */
    @Test
    public void testUnnecessaryMarkerFiles() throws FileListException {
        final Set<FileWithStatus> notSuccessfullyVerifiedFiles = getBlackList().verifyDistribution(
                getTestingDistribution(), getStartingDistribution(), getTestingPatchToolDistribution());
        final Set<FileWithStatus> filesWithUnnecessaryMarkerFiles =
                FileUtil.filterFilesByStatus(notSuccessfullyVerifiedFiles, FileStatusCode.MARKER_FILE_NOT_NECESSARY);

        for (FileWithStatus file : filesWithUnnecessaryMarkerFiles) {
            logger.error("Patch tool created unnecessary marker file for file " + file.getPath() + "!");
        }
        Assert.assertTrue("Patch tool created unnecessary marker files for some files that are on blacklist! "
                        + Constants.MESSAGE_SEE_RECENT_LOG,
                filesWithUnnecessaryMarkerFiles.isEmpty());
    }
}
