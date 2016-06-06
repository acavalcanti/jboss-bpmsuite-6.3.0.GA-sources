package org.jboss.brmsbpmsuite.patching.client;

import org.apache.commons.io.FileUtils;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class BaseClientPatcherTest {

    protected static final List<String> EMPTY_STR_LIST = Collections.emptyList();
    protected static final List<PatchEntry> EMPTY_PATCH_ENTRY_LIST = new ArrayList<PatchEntry>();
    protected static final List<ExpectedDistributionEntry> EMPTY_EXPECTED_ENTRIES_LIST = new ArrayList<ExpectedDistributionEntry>();
    protected static final Map<String, List<Checksum>> EMPTY_CHECKSUMS_MAP = new HashMap<String, List<Checksum>>();
    protected static final DistributionChecker NOP_DISTRIBUTION_CHECKER = new DistributionChecker() {
        @Override
        public boolean check(File distributionRoot) {
            return true;
        }
    };

    protected static File tmpDir = new File("./target/tmp-tests-dir");

    @Before
    public void setup() throws Exception {
        // delete the temp dirs before every so that we start with clean environment
        FileUtils.deleteDirectory(tmpDir);
        FileUtils.forceMkdir(tmpDir);
    }

    protected static File getCPResourceAsFile(String resourcePath) {
        return getCPResourceAsFile(resourcePath, BaseClientPatcherTest.class);
    }

    protected static File getCPResourceAsFile(String resourcePath, Class<?> context) {
        URL resourceURL = context.getResource(resourcePath);
        if (resourceURL == null) {
            fail("Can not find resource '" + resourcePath + "'!");
        }
        return new File(resourceURL.getFile());
    }

    protected void assertFileExists(File file) {
        assertFileExists("", file);
    }

    protected void assertFileExists(String msg, File file) {
        String absPath = file.getAbsolutePath();
        assertTrue(msg + " File " + absPath + " does not exist!", file.exists());
        assertTrue(msg + " File " + absPath + " is not a normal file!", file.isFile());
    }

    protected void assertFileNotExists(File file) {
        String absPath = file.getAbsolutePath();
        assertFalse("File " + absPath + " exists, but it is expected to not!", file.exists());
    }

    protected void assertFileContent(String expectedContent, File file) {
        try {
            String actualContent = FileUtils.readFileToString(file);
            assertEquals("File does no have expected content!", expectedContent, actualContent);
        } catch (IOException e) {
            throw new RuntimeException("Can not read content of file " + file.getAbsolutePath(), e);
        }
    }

    protected void assertDirExists(File dir) {
        assertDirExists("", dir);
    }
    protected void assertDirExists(String msg, File dir) {
        String absPath = dir.getAbsolutePath();
        assertTrue(msg + " Directory " + absPath + " does not exist!", dir.exists());
        assertTrue(msg + " " + absPath + " is not a directory!", dir.isDirectory());
    }

    protected void assertDirNotExists(File dir) {
        assertDirNotExists("", dir);
    }

    protected void assertDirNotExists(String msg, File dir) {
        String absPath = dir.getAbsolutePath();
        assertFalse(msg + " Directory " + absPath + " exists, expected not to!", dir.exists());
    }

    protected void assertEqualsIgnoreOrder(String msg, List<?> expectedList, List<?> actualList) {
        assertEquals(msg + " Different size of lists!", expectedList.size(), actualList.size());
        for (Object obj : expectedList) {
            assertTrue(msg + " Expecting " + obj + " to be in list " + actualList, actualList.contains(obj));
        }
    }

}
