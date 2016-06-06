package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralDirPatcherTest extends BaseClientPatcherTest {

    private File distroRoot = new File(tmpDir, "distro-root");
    private File backupDir = new File(tmpDir, "backup");
    private File patchDir = new File(tmpDir, "patched-files");

    @Before
    public void setup() throws Exception {
        super.setup();
        FileUtils.forceMkdir(distroRoot);
        FileUtils.copyDirectory(getCPResourceAsFile("/general-dir-patcher-test/distro-root"), distroRoot);

        FileUtils.forceMkdir(patchDir);
        FileUtils.copyDirectory(getCPResourceAsFile("/general-dir-patcher-test/patched-files"), patchDir);

        FileUtils.forceMkdir(backupDir);
    }

    @Test(expected = InvalidDistributionRootException.class)
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

        assertFileExists(new File(backupDir, "file.txt"));
        assertFileExists(new File(backupDir, "nested-dir/file.txt"));
    }

    @Test
    public void shouldCorrectlyApplySimplePatch() throws Exception {
        List<String> blacklist = Lists.newArrayList("blacklisted.txt", "nested-dir/blacklisted.txt", "blacklisted-removed.txt");
        List<String> removeList = Lists.newArrayList("file.txt", "nested-dir/file*.txt", "blacklisted.txt",
                "blacklisted-removed.txt", "nested-dir/black*.txt", "nested-dir2");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry("file.txt", new File(patchDir, "file.txt")));
        patchEntries.add(new PatchEntry("nested-dir/file.txt", new File(patchDir, "nested-dir/file.txt")));
        patchEntries.add(new PatchEntry("new-file.txt", new File(patchDir, "new-file.txt")));
        patchEntries.add(new PatchEntry("blacklisted.txt", new File(patchDir, "blacklisted.txt")));
        patchEntries.add(new PatchEntry("nested-dir/blacklisted.txt", new File(patchDir, "nested-dir/blacklisted.txt")));
        patchEntries.add(new PatchEntry("whole-dir", new File(patchDir, "nested-dir")));

        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList, patchEntries,
                blacklist, EMPTY_CHECKSUMS_MAP);
        patcher.apply();

        // verify the files were actually updated
        assertFileExists(new File(distroRoot, "file.txt"));
        assertFileContent("p01", new File(distroRoot, "file.txt"));

        assertFileExists(new File(distroRoot, "nested-dir/file.txt"));
        assertFileContent("p01", new File(distroRoot, "nested-dir/file.txt"));

        assertFileExists(new File(distroRoot, "new-file.txt"));

        // blacklisted files should not be touched
        //   - new file with suffix ".new" should be created for updated files(with the updated content)
        //   - marker file with ".removed" suffix should be created for files that were only removed
        assertFileExists(new File(distroRoot, "blacklisted.txt"));
        assertFileContent("ga", new File(distroRoot, "blacklisted.txt"));
        assertFileExists(new File(distroRoot, "blacklisted.txt.new"));
        assertFileContent("p01", new File(distroRoot, "blacklisted.txt.new"));

        assertFileExists(new File(distroRoot, "nested-dir/blacklisted.txt"));
        assertFileContent("ga", new File(distroRoot, "nested-dir/blacklisted.txt"));
        assertFileExists(new File(distroRoot, "nested-dir/blacklisted.txt.new"));
        assertFileContent("p01", new File(distroRoot, "nested-dir/blacklisted.txt.new"));

        assertFileExists(new File(distroRoot, "blacklisted-removed.txt"));
        assertFileContent("ga", new File(distroRoot, "blacklisted-removed.txt"));
        assertFileExists(new File(distroRoot, "blacklisted-removed.txt.removed"));

        // whole dirs should be correctly removed
        assertDirNotExists(new File(distroRoot, "nested-dir2"));

        // whole dirs should be correctly copied
        assertDirExists(new File(distroRoot, "whole-dir"));
        assertFileExists(new File(distroRoot, "whole-dir/file.txt"));
    }

    @Test
    public void shouldCorrectlyPerformWholeLifecycleWithEmptyLists() throws Exception {
        File distroRoot = new File(tmpDir, "distro-root");
        File backupDir = new File(tmpDir, "backup-dir");
        FileUtils.copyDirectory(getCPResourceAsFile("/general-dir-patcher-test/distro-root"), distroRoot);
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, EMPTY_STR_LIST,
                EMPTY_PATCH_ENTRY_LIST, EMPTY_STR_LIST, EMPTY_CHECKSUMS_MAP);
        patcher.checkDistro();
        patcher.backup(backupDir);
        patcher.apply();
        patcher.cleanUp();
    }

    @Test
    public void shouldNotCreateMarkerFileForIdenticalSourceAndTargetFiles() throws Exception {
        List<String> blacklist = Lists.newArrayList("nested-dir/blacklisted-identical.txt");
        List<String> removeList = Lists.newArrayList("nested-dir/blacklisted-identical.txt");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry("nested-dir/blacklisted-identical.txt", new File(patchDir, "nested-dir/blacklisted-identical.txt")));

        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList, patchEntries,
                blacklist, EMPTY_CHECKSUMS_MAP);
        patcher.apply();

        assertFileExists(new File(distroRoot, "nested-dir/blacklisted-identical.txt"));
        assertFileNotExists(new File(distroRoot, "nested-dir/blacklisted-identical.txt.new"));
    }

    @Test
    public void shouldAutomaticallyOverwriteBlacklistedFilesWhichWereNotChangedByUser() throws Exception {
        List<String> blacklist = Lists.newArrayList("blacklisted-with-known-checksum.txt");
        List<String> removeList = Lists.newArrayList("blacklisted-with-known-checksum.txt");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry("blacklisted-with-known-checksum.txt", new File(patchDir, "blacklisted-with-known-checksum.txt")));

        Map<String, List<Checksum>> checksums = new HashMap<String, List<Checksum>>();
        checksums.put("blacklisted-with-known-checksum.txt", Lists.newArrayList(Checksum.md5("f817dc060c95704dc153b310d560a7a3")));
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList, patchEntries,
                blacklist, checksums);
        patcher.apply();

        File file = new File(distroRoot, "blacklisted-with-known-checksum.txt");
        assertFileExists(file);
        assertFileContent("File which was just changed by us, as part of patch. This is the latest version.", file);
        assertFileNotExists(new File(distroRoot, "blacklisted-with-known-checksum.txt.new"));
    }

    @Test
    public void shouldAutomaticallyRemoveBlacklistedFilesWhichWereNotChangedByUser() throws Exception {
        List<String> blacklist = Lists.newArrayList("blacklisted-with-known-checksum.txt");
        List<String> removeList = Lists.newArrayList("blacklisted-with-known-checksum.txt");

        Map<String, List<Checksum>> checksums = new HashMap<String, List<Checksum>>();
        checksums.put("blacklisted-with-known-checksum.txt", Lists.newArrayList(Checksum.md5("f817dc060c95704dc153b310d560a7a3")));
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList, EMPTY_PATCH_ENTRY_LIST,
                blacklist, checksums);
        patcher.apply();

        // no marker file should be created and the file should be removed
        assertFileNotExists(new File(distroRoot, "blacklisted-with-known-checksum.txt.removed"));
        assertFileNotExists(new File(distroRoot, "blacklisted-with-known-checksum.txt"));
    }

    @Test
    public void shouldNotAutomaticallyOverwriteBlacklistedFilesChangedByUser() throws Exception {
        List<String> blacklist = Lists.newArrayList("blacklisted-with-unknown-checksum.txt");
        List<String> removeList = Lists.newArrayList("blacklisted-with-unknown-checksum.txt");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry("blacklisted-with-unknown-checksum.txt", new File(patchDir, "blacklisted-with-unknown-checksum.txt")));

        Map<String, List<Checksum>> checksums = new HashMap<String, List<Checksum>>();
        // some random checksum, to simulate an entry which should match the file which was not updated by user
        checksums.put("blacklisted-with-known-checksum.txt", Lists.newArrayList(Checksum.md5("32d7508fe69220cb40af28441ef746d9")));
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList, patchEntries,
                blacklist, checksums);
        patcher.apply();

        File file = new File(distroRoot, "blacklisted-with-unknown-checksum.txt");
        assertFileExists(file);
        assertFileContent("Some file manually configured by user.", file);
        assertFileExists(new File(distroRoot, "blacklisted-with-unknown-checksum.txt.new"));
    }

    @Test
    public void shouldNotAutomaticallyRemoveBlacklistedFilesChangedByUser() throws Exception {
        List<String> blacklist = Lists.newArrayList("blacklisted-with-unknown-checksum.txt");
        List<String> removeList = Lists.newArrayList("blacklisted-with-unknown-checksum.txt");

        Map<String, List<Checksum>> checksums = new HashMap<String, List<Checksum>>();
        // some random checksum, to simulate an entry which should match the file which was not updated by user
        checksums.put("blacklisted-with-known-checksum.txt", Lists.newArrayList(Checksum.md5("32d7508fe69220cb40af28441ef746d9")));
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList, EMPTY_PATCH_ENTRY_LIST,
                blacklist, checksums);
        patcher.apply();

        File file = new File(distroRoot, "blacklisted-with-unknown-checksum.txt");
        assertFileExists(file);
        assertFileContent("Some file manually configured by user.", file);
        assertFileExists(new File(distroRoot, "blacklisted-with-unknown-checksum.txt.removed"));
        assertFileExists(new File(distroRoot, "blacklisted-with-unknown-checksum.txt"));
    }

    @Test
    public void shouldHandleFileNamesWithDollar() throws IOException {
        File distroRoot = new File(tmpDir, "distro-root");
        FileUtils.copyDirectory(getCPResourceAsFile("/general-dir-patcher-test/distro-root"), distroRoot);
        List<String> removeList = Lists.newArrayList("FileWithDollarInName$2", "FileWithDollarInName$3");
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        patchEntries.add(new PatchEntry("FileWithDollarInName$2", new File(patchDir, "FileWithDollarInName$2")));
        GeneralDirPatcher patcher = new GeneralDirPatcher(distroRoot, NOP_DISTRIBUTION_CHECKER, removeList,
                patchEntries, EMPTY_STR_LIST, EMPTY_CHECKSUMS_MAP);
        patcher.apply();

        assertFileExists(new File(distroRoot, "FileWithDollarInName$2"));
        assertFileContent("New content.", new File(distroRoot, "FileWithDollarInName$2"));
        assertFileNotExists(new File(distroRoot, "FileWithDollarInName$3"));
    }

}
