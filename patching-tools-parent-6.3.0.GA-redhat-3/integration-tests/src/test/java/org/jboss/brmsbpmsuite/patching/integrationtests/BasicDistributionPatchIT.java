package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.IOException;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileListException;
import org.junit.Test;

/**
 * Integration tests that test if the testing distribution was patched correctly
 * compared with destined distribution.
 */
public class BasicDistributionPatchIT extends AbstractOneDistributionForAllTestsIT {

    /**
     * Test that compares patched distribution with destined distribution.
     * Also checks files blacklisting.
     * @throws IOException
     * @throws InterruptedException
     * @throws DistributionException
     * @throws FileListException
     */
    @Test
    public void testCompareWithDestDist()
            throws IOException, InterruptedException, DistributionException, FileListException {
        patchDistributionAndValidSuccessfulPatchScriptRun(getTestingDistribution());
        validSuccessfulDistributionPatch(getTestingDistribution(), getStartingDistribution(),
                getDestinedDistribution(), getTestingPatchToolDistribution());
    }
}

