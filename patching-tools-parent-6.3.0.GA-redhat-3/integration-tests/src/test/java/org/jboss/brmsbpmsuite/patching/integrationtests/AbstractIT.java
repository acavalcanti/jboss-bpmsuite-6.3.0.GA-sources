package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.BlackList;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileListException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileStatusCode;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileWithStatus;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.PatchUtil;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.WorkspaceUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root parent class for patch tool integration tests classes.
 */
public abstract class AbstractIT {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private static Distribution startingDistribution;
    private static Distribution startingPatchToolDistribution;
    private static Distribution destinedDistribution;

    /**
     * Prepares starting and destined distribution instances so they can be later used in tests.
     */
    @BeforeClass
    public static void prepareStartingDestinedDistributions() {
        startingDistribution = WorkspaceUtil.getNewStartingDistributionInstance();
        startingPatchToolDistribution = WorkspaceUtil.getNewPatchToolDistributionInstance(false);
        destinedDistribution = WorkspaceUtil.getNewDestinedDistributionInstance();
    }

    protected static Distribution getStartingDistribution() {
        return startingDistribution;
    }

    protected static Distribution getStartingPatchToolDistribution() {
        return startingPatchToolDistribution;
    }

    protected static Distribution getDestinedDistribution() {
        return destinedDistribution;
    }

    /**
     * Applies patch to defined distribution and checks if patch script ended correctly.
     * @param distribution Distribution that will be patched.
     * @throws IOException
     * @throws InterruptedException
     */
    protected static void patchDistributionAndValidSuccessfulPatchScriptRun(final Distribution distribution)
            throws IOException, InterruptedException {
        final int exitCode = PatchUtil.applyPatch(distribution);
        Assert.assertEquals("Script ended with exit code " + exitCode + "!", 0, exitCode);
    }

    /**
     * Validates that distribution was successfully patched. Validates blacklist and compares
     * patched distribution with destined distribution.
     * @param patchedDistribution Patched distribution.
     * @param startingDistribution Starting distribution.
     * @param destinedDistribution Destined distribution.
     * @param patchToolDistribution Patch tool distribution. Distribution containing update files.
     * @throws IOException
     * @throws DistributionException
     * @throws FileListException
     */
    protected void validSuccessfulDistributionPatch(final Distribution patchedDistribution,
            final Distribution startingDistribution, final Distribution destinedDistribution,
            final Distribution patchToolDistribution)
            throws IOException, DistributionException, FileListException {
        final BlackList distributionBlacklist = WorkspaceUtil.getBlacklist(startingDistribution.getType());

        // Some distributions contain only war files so they need to be unpacked.
        final boolean unpackedInStartDist = startingDistribution.unpackWars();
        try {
            final boolean unpackedInDestDist = getDestinedDistribution().unpackWars();
            try {
                final boolean unpackedInTestDist = patchedDistribution.unpackWars();
                final Set<FileWithStatus> incorrectlyBlacklistedFiles =
                        distributionBlacklist.verifyDistribution(patchedDistribution,
                                startingDistribution, patchToolDistribution);
                final Set<FileWithStatus> filesMissingInTestingDistribution =
                        FileUtil.filterFilesByStatus(incorrectlyBlacklistedFiles, FileStatusCode.MISSING);
                for (FileWithStatus missingFile : filesMissingInTestingDistribution) {
                    logger.info("Missing file in distribution: " + missingFile.getPath());
                }
                // There can be missing files in distribution (I.e. user deleted some file). So these missing
                // files are ignored (info logged).
                incorrectlyBlacklistedFiles.removeAll(filesMissingInTestingDistribution);

                final Set<FileWithStatus> filesNotInPatch =
                        FileUtil.filterFilesByStatus(incorrectlyBlacklistedFiles, FileStatusCode.NOT_IN_PATCH);
                for (FileWithStatus fileNotInPatch : filesNotInPatch) {
                    logger.info("File is not in patch: " + fileNotInPatch.getPath());
                }
                incorrectlyBlacklistedFiles.removeAll(filesNotInPatch);

                // It is needed to also filter out files that are out of scope of patched distribution.
                // E.g. when patching part of distribution.
                incorrectlyBlacklistedFiles.removeAll(
                        FileUtil.filterFilesByStatus(incorrectlyBlacklistedFiles, FileStatusCode.NOT_IN_SCOPE_OF_PATCHED_DIST));

                for (FileWithStatus file : incorrectlyBlacklistedFiles) {
                    logger.error("Incorrectly blacklisted file - STATUS: "
                            + file.getStatus() + ", FILE: " + file.getPath());
                }
                Assert.assertTrue("Patch ended with some incorrectly blacklisted files! " + Constants
                                .MESSAGE_SEE_RECENT_LOG,
                        incorrectlyBlacklistedFiles.isEmpty());

                patchedDistribution.correct();
                Set<File> filesThatAreDifferent = FileUtil.compareDirectories(
                        getDestinedDistribution().getContentRootDirectory(),
                        patchedDistribution.getContentRootDirectory(), false, true);
                // empty directories are removed by the patch tool since the removal is considered harmless
                // the comparison should ignore such directories
                filesThatAreDifferent = recursivelyFilterOutEmptyDirs(filesThatAreDifferent);
                for (File file : filesThatAreDifferent) {
                    logger.error("Compare fail testingDist <-> destinedDist: " + file.getPath());
                }
                Assert.assertTrue("Patched distribution is not the same as destined! " + Constants.MESSAGE_SEE_RECENT_LOG,
                        filesThatAreDifferent.isEmpty());

                // After correction, unpacked wars must be packed again.
                if (unpackedInTestDist) {
                    patchedDistribution.packWars();
                }
            } finally {
                if (unpackedInDestDist) {
                    destinedDistribution.packWars();
                }
            }
        } finally {
            if (unpackedInStartDist) {
                startingDistribution.packWars();
            }
        }
    }

    /**
     * Recursively filters out all empty directories within the provided set.
     *
     * Recursively means that it will also delete empty parents of all the empty dirs. There are cases where
     * directory only contains other empty directory, so they should be both removed.
     *
     * @param files set of files to filter
     * @return new set, based on the provided one, but without all empty dirs
     */
    private Set<File> recursivelyFilterOutEmptyDirs(final Set<File> files) {
        final Set<File> filtered = new HashSet<File>();
        for (File file : files) {
            if (isEmptyDirRecursive(file)) {
                logger.warn("Ignoring empty directory {}", file);
            } else {
                filtered.add(file);
            }
        }
        return filtered;
    }

    private boolean isEmptyDirRecursive(File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        final File[] nestedEntries = dir.listFiles();
        // null means something very bad happened (I/O error), so just fail fast
        if (nestedEntries == null) {
            throw new RuntimeException("Can't retrieve content of directory " + dir + ". File.listFiles() returned null.");
        }
        if (nestedEntries.length == 0) {
            return true;
        }
        // recursively check if all the nested entries are also empty directories, if so mark this directory as empty
        // one as well
        for (File nestedFile : nestedEntries) {
            if (!isEmptyDirRecursive(nestedFile)) {
                return false;
            }
        }
        return true;
    }
}
