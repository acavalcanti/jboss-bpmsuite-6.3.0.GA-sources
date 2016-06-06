package org.jboss.brmsbpmsuite.patching.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PatchingUtils {
    private static final Logger logger = LoggerFactory.getLogger(PatchingUtils.class);

    /**
     * Deletes the specified file and also all parent directories that are empty.
     * It will stop the deleting when reaching the specified basedir.
     *
     * @param file    file to delete
     * @param basedir base directory where to stop when searching for empty parent dirs
     */
    public static void deleteFileAndParentsIfEmpty(File file, File basedir) {
        if (!file.delete()) {
            throw new RuntimeException("Can't remove file '" + file + "' (unknown reason)!");
        }
        deleteDirIfEmptyAndParentsIfEmpty(file.getParentFile(), basedir);
    }

    /**
     * Recursively deletes the specified directory and also all parent directories that are empty.
     * It will stop the deleting when reaching the specified basedir.
     *
     * @param dir     dir to delete
     * @param basedir base directory where to stop when searching for empty parent dirs
     * @throws IOException if the
     */
    public static void deleteDirAndParentsIfEmpty(File dir, File basedir) throws IOException {
        FileUtils.deleteDirectory(dir);
        deleteDirIfEmptyAndParentsIfEmpty(dir.getParentFile(), basedir);
    }

    private static void deleteDirIfEmptyAndParentsIfEmpty(File dir, File basedir) {
        // we need to stop when reaching the basedir
        if (dir.equals(basedir)) {
            logger.debug("Encountered basedir, returning. Current dir='{}', basedir='{}'", dir, basedir);
            return;
        }
        if (dir.list().length == 0) {
            logger.trace("Found empty dir {}, removing it.", dir);
            dir.delete();
            deleteDirIfEmptyAndParentsIfEmpty(dir.getParentFile(), basedir);
        }
    }

    public static String createRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder("^");
        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                // escape chars which have special meaning in regexps
                case '.':
                case '\\':
                case '$':
                case '(':
                case '[':
                case '{':
                case '^':
                case '+':
                case '-':
                case '=':
                case '!':
                case '|':
                case ']':
                case '}':
                case ')':
                    out.append("\\");
                    out.append(c);
                    break;
                default:
                    out.append(c);
            }
        }
        out.append('$');
        return out.toString();
    }

    /**
     * Unzips specified WAR file into directory with the same name as the WAR file itself and removes the original file.
     * <p/>
     * This basically creates exploaded WAR file from the the zipped one, keeping the original name.
     */
    public static void unzipWarFileIntoDirWithSameName(File warFile) throws IOException {
        if (!warFile.exists()) {
            throw new FileNotFoundException("File " + warFile + " does not exist!");
        }
        logger.debug("Unzipping WAR file {}.", warFile);
        // first rename the file so that we can use the actual file for the new directory
        File movedFile = new File(warFile.getAbsolutePath() + "_file");
        FileUtils.moveFile(warFile, movedFile);
        // now extract the moved .war file into directory with the same as the original file
        FileUtils.forceMkdir(warFile);
        PatchingUtils.unzipFile(movedFile, warFile);
        FileUtils.forceDelete(movedFile);
        logger.debug("File {} successfully unzipped.", warFile);
    }

    public static void zipExploadedWar(File war) throws IOException {
        if (!war.exists()) {
            throw new FileNotFoundException("Directory " + war + " does not exist!");
        }
        logger.debug("Zipping exploaded WAR {}.", war);
        File movedDir = new File(war.getAbsolutePath() + "_dir");
        FileUtils.moveDirectory(war, movedDir);
        ZipUtil.pack(movedDir, war, false);
        FileUtils.forceDelete(movedDir);
    }

    public static void unzipFile(File zipFile, File destinationDir) throws IOException {
        logger.debug("Unzipping file {} into directory {}. ", zipFile, destinationDir);
        ZipUtil.unpack(zipFile, destinationDir);
        logger.debug("File {} successfully unzipped.", zipFile);
    }

}
