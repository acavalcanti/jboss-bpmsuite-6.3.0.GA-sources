package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RunWith(Parameterized.class)
public class WAS8BundlePatcherTest extends BaseClientPatcherTest {

    private File distroRoot = new File(tmpDir, "distro");
    private File backupDir = new File(tmpDir, "backup");
    private File patchDir = new File(tmpDir, "patched-files");

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                {TargetProduct.BRMS},
                {TargetProduct.BPMSUITE}
        });
    }

    @Parameterized.Parameter(0)
    public TargetProduct product;

    @Before
    public void setup() throws Exception {
        super.setup();
        FileUtils.forceMkdir(distroRoot);
        if (TargetProduct.BRMS == product) {
            FileUtils.copyDirectory(getCPResourceAsFile("/was8-patcher-test/brms-distro"), distroRoot);
        } else if (TargetProduct.BPMSUITE == product) {
            FileUtils.copyDirectory(getCPResourceAsFile("/was8-patcher-test/bpmsuite-distro"), distroRoot);
        } else {
            Assert.fail("Using product + " + product + " for this test is not expected!");
        }
        FileUtils.forceMkdir(patchDir);
        FileUtils.copyDirectory(getCPResourceAsFile("/was8-patcher-test/patched-files"), patchDir);

        FileUtils.forceMkdir(backupDir);
    }


    @Test
    public void shouldApplyUpdatesToBundleWithZippedWars() throws Exception {
        List<String> blacklist = Collections.emptyList();
        List<String> removeList = Lists.newArrayList("some-file.txt");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry("business-central.war/some-file.txt", new File(patchDir, "some-file.txt")));
        patchEntries.add(new PatchEntry("kie-server.war/some-file.txt", new File(patchDir, "some-file.txt")));
        if (TargetProduct.BPMSUITE == product) {
            patchEntries.add(new PatchEntry("dashbuilder.war/some-file.txt", new File(patchDir, "some-file.txt")));
        }

        WAS8BundlePatcher patcher = new WAS8BundlePatcher(
                product,
                distroRoot,
                removeList,
                patchEntries,
                blacklist,
                EMPTY_CHECKSUMS_MAP
        );

        patcher.checkDistro();
        patcher.backup(backupDir);
        patcher.apply();
        patcher.verify();
        patcher.cleanUp();

        // checks common for both products
        // check that correct set of files is still in the distro dir
        assertFileExists(new File(distroRoot, "business-central.war"));
        assertFileExists(new File(distroRoot, "kie-server.war"));

        // unzip the WARs and check the content to verify the updates were applied
        File unzippedBC = new File(tmpDir, "unzipped-bc-war");
        PatchingUtils.unzipFile(new File(distroRoot, "business-central.war"), unzippedBC);
        assertFileContent("patched", new File(unzippedBC, "some-file.txt"));

        File unzippedKieServer = new File(tmpDir, "unzipped-kie-server-war");
        PatchingUtils.unzipFile(new File(distroRoot, "kie-server.war"), unzippedKieServer);
        assertFileContent("patched", new File(unzippedKieServer, "some-file.txt"));

        if (TargetProduct.BPMSUITE == product) {
            assertFileExists(new File(distroRoot, "dashbuilder.war"));
            // unzip Dashbuilder WAR and check the content to verify the updates were applied
            File unzippedDashbuilder = new File(tmpDir, "unzipped-dashbuilder-war");
            PatchingUtils.unzipFile(new File(distroRoot, "dashbuilder.war"), unzippedDashbuilder);
            assertFileContent("patched", new File(unzippedDashbuilder, "some-file.txt"));
        }
    }

}
