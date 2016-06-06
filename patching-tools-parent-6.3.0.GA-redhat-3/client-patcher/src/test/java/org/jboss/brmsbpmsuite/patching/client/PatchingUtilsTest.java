package org.jboss.brmsbpmsuite.patching.client;

import org.apache.commons.io.FileUtils;
import org.jboss.brmsbpmsuite.patching.client.PatchingUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class PatchingUtilsTest extends BaseClientPatcherTest {

    @Test
    public void shouldSuccessfullyDeleteFileAndAllEmptyParentDirs() throws Exception {
        File basedir = new File(tmpDir, "remove-file-test");
        FileUtils.touch(new File(basedir, "some-file"));
        FileUtils.forceMkdir(new File(basedir, "nested-dir/dir1/dir2"));
        File fileToDelete = new File(basedir, "nested-dir/dir1/dir2/some-other-file");
        FileUtils.touch(fileToDelete);

        // whole "nested-dir/dir1/dir2" subtree should be deleted together with the file
        PatchingUtils.deleteFileAndParentsIfEmpty(fileToDelete, basedir);

        assertDirNotExists("Directory was empty so should have been deleted!", new File(basedir, "nested-dir"));
        assertDirNotExists("Directory was empty so should have been deleted!", new File(basedir, "nested-dir/dir1"));
        assertDirNotExists("Directory was empty so should have been deleted!", new File(basedir, "nested-dir/dir2"));
        assertDirExists("Basedir was not empty, so should not be deleted!", basedir);
    }

    @Test
    public void shouldStopAtBasedirWhenDeletingEmptyDirs() throws Exception {
        File basedir = new File(tmpDir, "remove-file-test/nested-dir");
        FileUtils.forceMkdir(new File(basedir, "nested-dir/dir1/dir2"));
        File fileToDelete = new File(basedir, "nested-dir/dir1/dir2/some-other-file");
        FileUtils.touch(fileToDelete);

        // "${basedir}/nested-dir" is empty, but should not be deleted as it is also the basedir
        PatchingUtils.deleteFileAndParentsIfEmpty(fileToDelete, basedir);
        assertDirNotExists("Directory was empty so should have been deleted!", new File(basedir, "nested-dir/dir1"));
        assertDirNotExists("Directory was empty so should have been deleted!", new File(basedir, "nested-dir/dir1/dir2"));
        assertDirExists("Directory was empty, but it was also basedir, so should not have been deleted!", basedir);
    }

    @Test
    public void shouldSuccessfullyDeleteWholeDirectory() throws IOException {
        File basedir = new File(tmpDir, "remove-dir-test");
        FileUtils.touch(new File(basedir, "some-file"));
        FileUtils.forceMkdir(new File(basedir, "nested-dir/dir1/dir2"));
        FileUtils.touch(new File(basedir, "nested-dir/dir1/dir2/some-other-file"));
        File dirToDelete = new File(basedir, "nested-dir");
        // whole "nested-dir" should be removed
        PatchingUtils.deleteDirAndParentsIfEmpty(dirToDelete, basedir);
        assertDirNotExists("Directory should have been deleted!", dirToDelete);
        assertDirExists("The basedir should not be touched when just deleting dir within it!", basedir);
    }

}
