package org.jboss.brmsbpmsuite.patching.integrationtests;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.WorkspaceUtil;
import org.junit.Before;

/**
 * Abstract class for tests that need to create new test distribution for each test method.
 * I.e. patch scope tests.
 */
public abstract class AbstractNewDistributionForEachTestIT extends AbstractIT {

    private Distribution testingDistribution;
    private Distribution testingPatchToolDistribution;

    @Before
    public void prepareTestDistribution() throws DistributionException {
        testingDistribution = WorkspaceUtil.getNewTestingDistributionInstance();
        testingDistribution.clean();
        testingDistribution.create(getStartingDistribution());

        testingPatchToolDistribution = WorkspaceUtil.getNewPatchToolDistributionInstance(true);
        testingPatchToolDistribution.clean();
        testingPatchToolDistribution.create(getStartingPatchToolDistribution());
    }

    protected Distribution getTestingDistribution() {
        return testingDistribution;
    }

    protected Distribution getTestingPatchToolDistribution() {
        return testingPatchToolDistribution;
    }
}