package com.redhat.installer.tests.installation.processpanel;

import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.installation.processpanel.FileMover;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 7/9/15.
 */
public class FileMoverTest extends ProcessPanelTester {
    FileMover fm;

    @Before
    public void setUp() throws Exception{
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "FileMover.source.isdirectory", "FileMover.copy.failed", "FileMover.copy.success", "FileMover.missing.args, FileMover.dest.exists");
        fm = new FileMover();
    }

    @Test
    public void testCopyFile() throws Exception{
        File toCopy = tempFolder.newFile();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + "destination");
        fm.run(handler, new String[]{
                "--copy=true",
                "--dest-is-file=true",
                "--source="+toCopy.getAbsolutePath(),
                "--destination="+newDest.getAbsolutePath()});
        assertTrue(toCopy.exists());
        assertTrue(newDest.exists());
        assertFalse(newDest.isDirectory());
    }

    @Test
    public void testCopyDirectory() throws Exception{
        File toCopy = tempFolder.newFolder();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + "destination");
        fm.run(handler, new String[]{
                "--copy=true",
                "--dest-is-file=true",
                "--source="+toCopy.getAbsolutePath(),
                "--destination="+newDest.getAbsolutePath()});
        assertTrue(toCopy.exists());
        assertTrue(newDest.exists());
        assertTrue(newDest.isDirectory());
    }

    @Test
    public void testCopyToDirectory() throws Exception{
        File toCopy = tempFolder.newFolder();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + "destination");
        fm.run(handler, new String[]{
                "--copy=true",
                "--source="+toCopy.getAbsolutePath(),
                "--destination="+newDest.getAbsolutePath()});
        File newToCopyFile = new File(newDest.getAbsolutePath()+File.separator+toCopy.getName());
        assertTrue(toCopy.exists());
        assertTrue(newDest.exists());
        assertTrue(newDest.isDirectory());
        assertTrue(newToCopyFile.exists());
        assertTrue(newToCopyFile.isDirectory());
    }

    @Test
    public void testCopyDirectoryToDirectory() throws Exception{
        File toCopy = tempFolder.newFile();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + "destination");
        fm.run(handler, new String[]{
                "--copy=true",
                "--source="+toCopy.getAbsolutePath(),
                "--destination="+newDest.getAbsolutePath()});
        File newToCopyFile = new File(newDest.getAbsolutePath()+File.separator+toCopy.getName());
        assertTrue(toCopy.exists());
        assertTrue(newDest.exists());
        assertTrue(newDest.isDirectory());
        assertTrue(newToCopyFile.exists());
        assertTrue(newToCopyFile.isFile());
    }

    @Test
    public void testMoveFile() throws Exception{
        File toCopy = tempFolder.newFile();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + "destination");
        fm.run(handler, new String[]{
                "--dest-is-file=true",
                "--source="+toCopy.getAbsolutePath(),
                "--destination="+newDest.getAbsolutePath()});
        assertFalse(toCopy.exists());
        assertTrue(newDest.exists());
        assertFalse(newDest.isDirectory());
    }

    @Test
    public void testMoveDirectory() throws Exception{
        File toCopy = tempFolder.newFolder();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + File.separator + "destination");
        fm.run(handler, new String[]{
                "--dest-is-file=true",
                "--source="+toCopy.getAbsolutePath(),
                "--destination="+newDest.getAbsolutePath()});
        assertFalse(toCopy.exists());
        assertTrue(newDest.exists());
        assertTrue(newDest.isDirectory());
    }

    @Test
    public void testMoveToDirectory() throws Exception {
        File toMove = tempFolder.newFile();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + "destination");
        fm.run(handler, new String[]{"--source="+toMove.getAbsolutePath(), "--destination="+newDest.getAbsolutePath()});
        File newToMoveFile = new File(newDest.getAbsolutePath()+File.separator+toMove.getName());
        assertFalse(toMove.exists());
        assertTrue(newDest.exists());
        assertTrue(newDest.isDirectory());
        assertTrue(newToMoveFile.exists());
        assertTrue(newToMoveFile.isFile());
    }

    @Test
    public void testMoveDirectoryToDirectory() throws Exception {
        File toMove = tempFolder.newFolder();
        File newDest = new File(tempFolder.getRoot().getAbsolutePath() + "destination");
        fm.run(handler, new String[]{"--source="+toMove.getAbsolutePath(), "--destination="+newDest.getAbsolutePath()});
        File newToMoveFile = new File(newDest.getAbsolutePath()+File.separator+toMove.getName());
        assertFalse(toMove.exists());
        assertTrue(newDest.exists());
        assertTrue(newDest.isDirectory());
        assertTrue(newToMoveFile.exists());
        assertTrue(newToMoveFile.isDirectory());
    }

    @Test
    public void testCopyMultipleToDirectory() throws Exception {
        String[] args = new String [7];
        File[] copiedFiles = new File[5];
        File destinationFolder = new File(tempFolder.getRoot().getAbsolutePath() + "destination");
        args[0] = "--copy=true";
        args[1] = "--destination="+destinationFolder.getAbsolutePath();
        for (int i = 2; i < args.length; i++){
            copiedFiles[i-2] = tempFolder.newFile();
            args[i] = "--source="+copiedFiles[i-2].getAbsolutePath();

        }
        fm.run(handler, args);

        assertTrue(destinationFolder.exists());
        for (File f : copiedFiles){
            assertTrue(new File(destinationFolder.getAbsolutePath()+File.separator+f.getName()).exists());
            assertTrue(f.exists());
        }
    }

    @Test
    public void testMoveMultipleToDirectory() throws Exception {
        String[] args = new String [7];
        File[] movedFiles = new File[5];
        File destinationFolder = new File(tempFolder.getRoot().getAbsolutePath() + "destination");
        args[0] = "";
        args[1] = "--destination="+destinationFolder.getAbsolutePath();
        for (int i = 2; i < args.length; i++){
            movedFiles[i-2] = tempFolder.newFile();
            args[i] = "--source="+movedFiles[i-2].getAbsolutePath();

        }
        fm.run(handler, args);

        assertTrue(destinationFolder.exists());
        for (File f : movedFiles){
            assertTrue(new File(destinationFolder.getAbsolutePath()+File.separator+f.getName()).exists());
            assertFalse(f.exists());
        }
    }

    @Override
    public void testProcessPanelInstantiation() {

    }
}
