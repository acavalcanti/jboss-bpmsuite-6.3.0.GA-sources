package org.jboss.brmsbpmsuite.patching.integrationtests;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionException;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileListException;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.WorkspaceUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Integration tests that test if patch tool can patch testing distribution that is on different special paths.
 * I.e. on path that contains whitespace.
 */
@RunWith(Parameterized.class)
public class PathIT extends AbstractIT {

    private String testDistributionAdditionalPath;

    @Parameterized.Parameters
    public static Collection<String[]> getPathParameters() {
        return Arrays.asList(new String[][]{{"whitespace path"},
                {"long/path/to/the/distribution/so/long/path/wow/very/path/very/long" +
                        "/longdistnameverylongdirnamelongwowverylongverydirsuchpathwowlongsuchdirwowdistribution" +
                        "/idontknowwhattowritenowbecausethepath/issolongthat/myrandomtextgenerator/isoverloaded" +
                        "/butimust/carryonbecause/testingmustbedone"}});
    }

    public PathIT(final String testDistributionAdditionalPath) {
        this.testDistributionAdditionalPath = testDistributionAdditionalPath;
    }

    /**
     * Tests patching distribution that is on some special path.
     * Tests files blacklisting, compares patched distribution with destined distribution.
     * Also tests, if the patch tool script exited correctly.
     * @throws IOException
     * @throws InterruptedException
     * @throws DistributionException
     * @throws FileListException
     */
    @Test
    public void testPath() throws IOException, InterruptedException, DistributionException, FileListException {
        final Distribution testingDistribution =
                WorkspaceUtil.getNewTestingDistributionInstance(testDistributionAdditionalPath);
        testingDistribution.clean();
        testingDistribution.create(getStartingDistribution());

        final Distribution testingPatchToolDistribution =
                WorkspaceUtil.getNewPatchToolDistributionInstance(true);
        testingPatchToolDistribution.clean();
        testingPatchToolDistribution.create(WorkspaceUtil.getNewPatchToolDistributionInstance(false));

        patchDistributionAndValidSuccessfulPatchScriptRun(testingDistribution);
        validSuccessfulDistributionPatch(testingDistribution, getStartingDistribution(),
                getDestinedDistribution(), testingPatchToolDistribution);
    }
}
