package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.IOException;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.BlackList;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileListException;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.WorkspaceUtil;
import org.junit.Test;

/**
 * Integration tests that test blacklist capability of patch tool using blacklist, that was created
 * in different OSA environment.
 */
public class BlacklistFromDifferentOsIT extends AbstractNewDistributionForEachTestIT {

    /**
     * Tests if patch tool works correctly also with blacklist containing opposite line separator
     * than is used on actual OS. Opposite because there are now two different line separators used in
     * all major OS environments. *nix and osx uses \n, Windows uses \r\n.
     * @throws IOException
     * @throws InterruptedException
     * @throws FileListException
     * @throws DistributionException
     */
    @Test
    public void testOppositeLineSeparator()
            throws IOException, InterruptedException, FileListException, DistributionException {
        final Distribution startingDistribution = getStartingDistribution();
        final BlackList blackList = WorkspaceUtil.getBlacklist(startingDistribution.getType());
        final String systemLineSeparator = System.getProperty("line.separator");
        blackList.replaceTextInOriginalFile(
                systemLineSeparator, getOppositeLineSeparator(systemLineSeparator));
        patchDistributionAndValidSuccessfulPatchScriptRun(getTestingDistribution());
        validSuccessfulDistributionPatch(getTestingDistribution(), startingDistribution,
                getDestinedDistribution(), getTestingPatchToolDistribution());
    }

    private String getOppositeLineSeparator(final String lineSeparator) {
        if (lineSeparator.equals("\n")) {
            return "\r\n";
        } else {
            return "\n";
        }
    }
}
