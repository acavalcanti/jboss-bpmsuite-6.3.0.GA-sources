package com.redhat.installer.tests.installation.processpanel;

import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.installation.processpanel.FileDeleter;
import com.redhat.installer.tests.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;

/**
 * Created by thauser on 2/6/14.
 */
public class FileDeleterTest extends ProcessPanelTester {
    FileDeleter fd;

    @Before
    public void setUp() throws Exception{
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "FileDeleter.failure","FileDeleter.success");
        fd = new FileDeleter();
    }

    @After
    public void tearDown() throws Exception {
        fd = null;
    }

    @Test
    public void testSingleFileDelete() throws Exception {
        File toDelete = tempFolder.newFile();
        fd.run(handler, new String[] {toDelete.getAbsolutePath()});
        assertFalse(toDelete.exists());
    }

    @Test
    public void testMultiFileDelete() throws Exception {

        List<File> tempFiles = new ArrayList<File>();
        List<String> fullPaths = new ArrayList<String>();
        tempFiles.add(tempFolder.newFile());
        tempFiles.add(tempFolder.newFile());
        tempFiles.add(tempFolder.newFile());

        for (File file : tempFiles){
            fullPaths.add(file.getAbsolutePath());
        }

        fd.run(handler, fullPaths.toArray(new String[3]));
        for (File file : tempFiles){
            assertFalse(file.exists());
        }
    }


    @Override
    public void testProcessPanelInstantiation() {

    }
}
