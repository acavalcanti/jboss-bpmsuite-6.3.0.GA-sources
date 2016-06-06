package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatcherFactoryTest extends BaseClientPatcherTest {

    @Test
    public void shouldRecognizeBrmsEngineDistroAndCreateCorrectPatcher() {
        File distroRoot = getCPResourceAsFile("/patcher-factory-test/brms-engine-distro");
        File patchBasedir = getCPResourceAsFile("/patcher-factory-test/patch-dir");
        Patcher patcher = PatcherFactory.newPatcher(DistributionType.BRMS_ENGINE, TargetProduct.BRMS, distroRoot, patchBasedir);
        assertTrue("Incorrect patcher type!", patcher instanceof GeneralDirPatcher);
        GeneralDirPatcher dirPatcher = (GeneralDirPatcher) patcher;
        assertEquals("Incorrect distribution root!", distroRoot, dirPatcher.getDistributionRoot());

        List<String> expectedRemoveList = Lists.newArrayList(
                "drools-compiler-6.2.0.Final-redhat-3.jar",
                "drools-core-6.2.0.Final-redhat-3.jar",
                "lib/ecj-4.3.1.redhat-1.jar",
                "lib/maven-core-3.2.2.jar",
                "SecurityPolicy/kie.policy"
        );
        assertEqualsIgnoreOrder("Incorrect remove list!", expectedRemoveList, dirPatcher.getRemoveList());

        List<String> expectedBlackList = Lists.newArrayList("SecurityPolicy/kie.policy", "SecurityPolicy/security.policy");
        assertEqualsIgnoreOrder("Incorrect black list!", expectedBlackList, dirPatcher.getBlackList());

        File newContentDir = new File(patchBasedir, "updates/" + DistributionType.BRMS_ENGINE.getRelativePath() + "/new-content");
        List<PatchEntry> expectedPatchEntries = Lists.newArrayList(
                newPatchEntry("drools-compiler-6.2.0.Final-redhat-4.jar", newContentDir),
                newPatchEntry("drools-core-6.2.0.Final-redhat-4.jar", newContentDir),
                newPatchEntry("lib/ecj-4.3.1.redhat-2.jar", newContentDir),
                newPatchEntry("lib/maven-core-3.2.3.jar", newContentDir),
                newPatchEntry("SecurityPolicy/kie.policy", newContentDir)
        );
        assertEqualsIgnoreOrder("Incorrect patch entries!", expectedPatchEntries, dirPatcher.getPatchEntries());

    }

    private PatchEntry newPatchEntry(String relPath, File basedir) {
        return new PatchEntry(relPath, new File(basedir, relPath));
    }

}
