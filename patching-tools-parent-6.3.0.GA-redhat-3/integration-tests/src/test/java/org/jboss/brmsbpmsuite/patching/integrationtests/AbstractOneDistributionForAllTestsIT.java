package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.BlackList;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.WorkspaceUtil;
import org.junit.BeforeClass;

/**
 * Abstract class for tests that need to create only one test distribution that is shared between the test methods.
 * I.e. blacklist tests.
 */
public abstract class AbstractOneDistributionForAllTestsIT extends AbstractIT {

    private static Distribution testingDistribution;
    private static Distribution testingPatchToolDistribution;

    private static BlackList blackList;

    @BeforeClass
    public static void prepareTestDistribution() throws DistributionException, IOException {
        testingPatchToolDistribution = WorkspaceUtil.getNewPatchToolDistributionInstance(true);
        testingPatchToolDistribution.clean();
        testingPatchToolDistribution.create(getStartingPatchToolDistribution());

        blackList = WorkspaceUtil.getBlacklist(getStartingDistribution().getType());
        blacklistRandomFile(getStartingDistribution());

        testingDistribution = WorkspaceUtil.getNewTestingDistributionInstance();
        testingDistribution.clean();
        testingDistribution.create(getStartingDistribution());
    }

    protected static Distribution getTestingDistribution() {
        return testingDistribution;
    }

    protected static Distribution getTestingPatchToolDistribution() {
        return testingPatchToolDistribution;
    }

    protected static BlackList getBlackList() {
        return blackList;
    }

    private static void blacklistRandomFile(final Distribution distribution) throws DistributionException, IOException {
        boolean unpackedWars = false;
        try {
            unpackedWars = distribution.unpackWars();
            // Modify and blacklist some file. This file cannot be then overwritten and must have marker file created.
            final Collection<File> filesFromDist = FileUtil.findFiles(distribution.getContentRootDirectory(), "*", null);
            if (filesFromDist != null && filesFromDist.size() > 0) {
                for (File fileFromDist : filesFromDist) {
                    // File must also exist in patch, because otherwise it will be ignored during patching process.
                    final File fileFromPatch = testingPatchToolDistribution.getFileFromOtherDistribution(fileFromDist, distribution);
                    if (fileFromPatch.exists()) {
                        FileUtil.appendTextToFile(fileFromDist, "someText");
                        // It looks like that sometimes getAbsolutePath() returns path with / and sometimes not.
                        String relativePath = fileFromDist.getAbsolutePath().replace(
                                distribution.getBundleRootDirectory().getAbsolutePath(), "");
                        if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
                            relativePath = relativePath.substring(1);
                        }
                        blackList.appendTextToOriginalFile(relativePath);
                        break;
                    }
                }
            }
        } finally {
            if (unpackedWars) {
                distribution.packWars();
            }
        }
    }
}