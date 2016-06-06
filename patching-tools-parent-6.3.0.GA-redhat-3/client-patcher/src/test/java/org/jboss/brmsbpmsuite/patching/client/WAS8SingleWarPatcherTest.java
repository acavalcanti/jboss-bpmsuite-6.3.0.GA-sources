package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
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
public class WAS8SingleWarPatcherTest extends BaseClientPatcherTest {

    private static File brmsDistroRoot = new File(tmpDir, "brms-distro");
    private static File bpmsuiteDistroRoot = new File(tmpDir, "bpmsuite-distro");
    private static File backupDir = new File(tmpDir, "backup");
    private static File patchDir = new File(tmpDir, "patched-files");


    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                {new File(brmsDistroRoot, "business-central.war"), "business-central.war"},
                {new File(brmsDistroRoot, "kie-server.war"), "kie-server.war"},
                {new File(bpmsuiteDistroRoot, "business-central.war"), "business-central.war"},
                {new File(bpmsuiteDistroRoot, "kie-server.war"), "kie-server.war"},
                {new File(bpmsuiteDistroRoot, "dashbuilder.war"), "dashbuilder.war"}
        });
    }

    @Parameterized.Parameter(0)
    public File distroRoot;

    @Parameterized.Parameter(1)
    public String warName;

    @Before
    public void setup() throws Exception {
        super.setup();
        FileUtils.forceMkdir(brmsDistroRoot);
        FileUtils.copyDirectory(getCPResourceAsFile("/was8-patcher-test/brms-distro"), brmsDistroRoot);
        FileUtils.forceMkdir(bpmsuiteDistroRoot);
        FileUtils.copyDirectory(getCPResourceAsFile("/was8-patcher-test/bpmsuite-distro"), bpmsuiteDistroRoot);

        FileUtils.forceMkdir(patchDir);
        FileUtils.copyDirectory(getCPResourceAsFile("/was8-patcher-test/patched-files"), patchDir);

        FileUtils.forceMkdir(backupDir);
    }

    @Test
    public void shouldApplyUpdatesToBRMSBundleWithZippedWars() throws Exception {
        List<String> blacklist = Collections.emptyList();
        List<String> removeList = Lists.newArrayList("some-file.txt");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry(warName + "/some-file.txt", new File(patchDir, "some-file.txt")));

        WAS8SingleWarPatcher patcher = new WAS8SingleWarPatcher(
                warName,
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

        // verify that the WAR file still exists
        assertFileExists(distroRoot);

        // unzip the WAR and check the content to verify the updates were applied
        File unzippedWar = new File(tmpDir, "unzipped-war");
        PatchingUtils.unzipFile(distroRoot, unzippedWar);
        assertFileContent("patched", new File(unzippedWar, "some-file.txt"));
    }

}
