package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NestedDirInBundlePatcherTest extends BaseClientPatcherTest {


    private File distroRoot = new File(tmpDir, "distro-root");
    private File backupDir = new File(tmpDir, "backup");
    private File patchDir = new File(tmpDir, "patched-files");

    @Before
    public void setup() throws Exception {
        super.setup();
        FileUtils.forceMkdir(distroRoot);
        FileUtils.copyDirectory(getCPResourceAsFile("/nested-dir-in-bundle-patcher-test/distro-root"), distroRoot);

        FileUtils.forceMkdir(patchDir);
        FileUtils.copyDirectory(getCPResourceAsFile("/nested-dir-in-bundle-patcher-test/patched-files"), patchDir);

        FileUtils.forceMkdir(backupDir);
    }

    @Test(expected = RuntimeException.class)
    public void shouldFailWhenNonExistingDistributionRootSpecified() throws Exception {
        GeneralDirPatcher patcher = new GeneralDirPatcher(new File("target/some/non-existing/path"),
                NOP_DISTRIBUTION_CHECKER, EMPTY_STR_LIST, EMPTY_PATCH_ENTRY_LIST, EMPTY_STR_LIST, EMPTY_CHECKSUMS_MAP);
        patcher.checkDistro();
    }

    @Test
    public void shouldCorrectlyBackupTheOldContent() throws Exception {
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, EMPTY_STR_LIST,
                EMPTY_PATCH_ENTRY_LIST, EMPTY_STR_LIST, EMPTY_CHECKSUMS_MAP);
        patcher.backup(backupDir);

        assertFileExists(new File(backupDir, "WEB-INF/web.xml"));
    }

    @Test
    public void shouldCorrectlyApplySimplePatch() throws Exception {
        String pathPrefix = "standalone/deployments/business-central.war/";
        List<String> blacklist = Lists.newArrayList(pathPrefix + "blacklisted.txt");
        List<String> removeList = Lists.newArrayList(pathPrefix + "WEB-INF/web.xml", pathPrefix + "WEB-INF/file.txt",
                pathPrefix + "login.jsp", pathPrefix + "blacklisted.txt");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry(pathPrefix + "WEB-INF/web.xml", new File(patchDir, "web.xml")));
        patchEntries.add(new PatchEntry(pathPrefix + "login.jsp", new File(patchDir, "login.jsp")));
        patchEntries.add(new PatchEntry("new-file.txt", new File(patchDir, "new-file.txt")));
        patchEntries.add(new PatchEntry(pathPrefix + "blacklisted.txt", new File(patchDir, "blacklisted.txt")));

        Map<String, List<Checksum>> checksums = new HashMap<String, List<Checksum>>();
        checksums.put(pathPrefix + "my-file", new ArrayList<Checksum>());

        NestedDirInBundlePatcher patcher = new NestedDirInBundlePatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, pathPrefix,
                removeList, patchEntries, blacklist, checksums);
        patcher.apply();

        // verify the files were actually updated
        assertFileExists(new File(distroRoot, "WEB-INF/web.xml"));
        assertFileContent("p01", new File(distroRoot, "WEB-INF/web.xml"));

        // blacklisted files should not be touched, new file with suffix ".new" should be created (with the updated content)
        assertFileExists(new File(distroRoot, "blacklisted.txt"));
        assertFileContent("ga", new File(distroRoot, "blacklisted.txt"));
        assertFileExists(new File(distroRoot, "blacklisted.txt.new"));
        assertFileContent("p01", new File(distroRoot, "blacklisted.txt.new"));

        // file only on remove list should have been removed
        assertFileNotExists(new File(distroRoot, "WEB-INF/file.txt"));

        // verify the paths in checksums map were correctly adapted
        Assert.assertEquals(1, patcher.getChecksums().size());
        Assert.assertTrue("Path not correctly adapted based on the prefix!", patcher.getChecksums().containsKey("my-file"));
    }

}
