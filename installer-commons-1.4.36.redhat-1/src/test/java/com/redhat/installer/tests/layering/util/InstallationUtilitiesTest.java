package com.redhat.installer.tests.layering.util;

import com.izforge.izpack.installer.UninstallData;
import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.installation.util.InstallationUtilities;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 3/11/15.
 */
public class InstallationUtilitiesTest {
    private final static String standaloneMgmtFile = File.separator + "standalone" + File.separator + "configuration" + File.separator + "mgmt-users.properties";
    private final static String domainMgmtFile = File.separator + "domain" + File.separator + "configuration" + File.separator + "mgmt-users.properties";
    private String standaloneFilePath;
    private String domainFilePath;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp(){
        standaloneFilePath = temporaryFolder.getRoot().getAbsolutePath() + standaloneMgmtFile;
        domainFilePath = temporaryFolder.getRoot().getAbsolutePath() + domainMgmtFile;
    }

    @Test
    public void testUserRemoval() throws Exception{
        createTestUserFiles();
        InstallationUtilities.removeManagementUser("testuser",temporaryFolder.getRoot().getAbsolutePath());
        Set<String> standaloneContents = TestUtils.getFileLinesAsSet(standaloneFilePath);
        Set<String> domainContents = TestUtils.getFileLinesAsSet(domainFilePath);
        assertFalse(standaloneContents.contains("testuser=testhash"));
        assertFalse(domainContents.contains("testuser=testhash"));
        assertTrue(standaloneContents.contains("otheruser=stillhere"));
        assertTrue(domainContents.contains("otheruser=stillhere"));
    }

    @Test
    public void testCleanupAdd(){
        File newFile = MockFileBuilder.makeEmptyFile(temporaryFolder);
        InstallationUtilities.addFileToCleanupList(newFile.getAbsolutePath());
        assertTrue(UninstallData.getInstance().getInstalledFilesList().contains(newFile.getAbsolutePath()));
    }

    private void createTestUserFiles(){
        MockFileBuilder.makeNewFileFromStringsAtPath(temporaryFolder, standaloneMgmtFile, "testuser=testhash","otheruser=stillhere");
        MockFileBuilder.makeNewFileFromStringsAtPath(temporaryFolder, domainMgmtFile, "testuser=testhash","otheruser=stillhere");
    }
}
