package com.redhat.installer.framework.mock;

import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Responsible for providing convenience methods for making files in the test file structure
 * during unit tests.
 * Created by fcanas on 3/20/14.
 */
public class MockFileBuilder {

    /**
     * Usage: Every string following the first parameter is taken as a new line of text in the
     * produced file. Creates the new file in the root of the give TemporaryFolder
     * @param folder the temporary folder from the specific test
     *
     * @param contents
     * @throws Exception
     */
    public static File makeNewFileFromStringsAtPath(TemporaryFolder folder, String path, String... contents) {
        File newFile = null;
        if (path != null) {
            File fullpath = new File(folder.getRoot(), path);
            File parent = fullpath.getParentFile();

            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                newFile = folder.newFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                newFile = folder.newFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PrintWriter fw = null;
        try {
            fw = new PrintWriter(new FileWriter(newFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("createNewFileWithContent(): failed to instantiate PrintWriter");
        }
        for (String line : contents){
            fw.println(line);
        }
        fw.close();
        return newFile;
    }

    public static File makeNewFileFromStrings(TemporaryFolder folder, String... contents){
        return makeNewFileFromStringsAtPath(folder, null, contents);
    }

    /**
     * Creates a new file with no write, read, or executable permissions
     */
    public static File makeUnreadableFileAtBaseDir(TemporaryFolder folder) throws Exception {
        File file = folder.newFile();
        file.createNewFile();
        System.out.println(file.setWritable(false,false));
        System.out.println(file.setReadable(false,false));
        System.out.println(file.setExecutable(false,false));

        System.out.println("Created file with permissions:");
        System.out.println("file.canWrite(): " + file.canWrite());
        System.out.println("file.canRead(): " + file.canRead());
        System.out.println("file.canExecute(): " +file.canExecute());
        return file;
    }

    /**
     * Creates a new empty zip file within the temporaryfolder
     */
    public static File makeEmptyZipFile(TemporaryFolder folder) throws Exception {
        File zipFile = folder.newFile();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile.getAbsoluteFile()));
        zos.putNextEntry(new ZipEntry(zipFile.getAbsolutePath()));
        zos.close();
        return zipFile;
    }

    /**
     * Creates a new zip file with minimal content
     */
    public static File makeZipFile(TemporaryFolder folder) throws Exception {
        File zipFile = folder.newFile();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        zos.putNextEntry(new ZipEntry(zipFile.getAbsolutePath()));
        zos.write(100);
        zos.close();
        return zipFile;
    }

    /**
     * Creates an empty file under the temporary folder, returning a reference to it.
     * @param tempFolder
     * @return
     */
    public static File makeEmptyFile(TemporaryFolder tempFolder) {
        File returnVal = null;
        try {
            returnVal = tempFolder.newFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnVal;
    }

    /**
     * Creates an empty file at the given path. If the parent directories (if any) in fullpath don't exist in the tempFolder,
     * they will be created. Used for when files must have specific names (EapExistsValidator, etc)
     * @param tempFolder
     * @param fullpath
     */
    public static void makeEmptyFileAtPath(TemporaryFolder tempFolder, String fullpath) {
        File location = new File(tempFolder.getRoot(), fullpath);
        File parent = location.getParentFile();
        // creates parent directories if they don't already exist
        if (parent!=null && !parent.exists()){
            parent.mkdirs();
        }
        try {
            tempFolder.newFile(fullpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
