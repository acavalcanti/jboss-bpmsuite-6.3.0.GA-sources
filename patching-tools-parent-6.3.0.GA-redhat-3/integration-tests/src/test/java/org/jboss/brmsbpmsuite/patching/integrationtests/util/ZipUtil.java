package org.jboss.brmsbpmsuite.patching.integrationtests.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Util class for manipulation with zip and zip compatible archives.
 */
public final class ZipUtil {

    /**
     * Unpacks all archives of a selected archive type that are located in selected root directory.
     * @param rootDirectory Directory from which all archives are unpacked.
     * @param archiveType Archive type of unpacked archives.
     * @return True, if there were some archives unpacked.
     * @throws IOException
     */
    public static boolean unpackAll(final File rootDirectory, final String archiveType) throws IOException {
        final Collection<File> packedFiles = FileUtil.findFiles(rootDirectory, "*." + archiveType, null);
        for (File packedFile : packedFiles) {
            // /oook/eeek/something.war -> /oook/eeek/something
            final String unpackedDirName =
                    packedFile.getPath().substring(0, packedFile.getPath().lastIndexOf("." + archiveType))
                            + Constants.UNPACKED_DIR_SUFFIX;
            unpack(packedFile, unpackedDirName);
            FileUtil.deleteFile(packedFile);
            FileUtil.renameFile(new File(unpackedDirName), packedFile);
        }
        return !packedFiles.isEmpty();
    }

    /**
     * Unpacks selected archive into selected destined directory.
     * @param zipFile Archive that is unpacked.
     * @param destDirectoryPath Destined directory path.
     * @throws IOException
     */
    public static void unpack(final File zipFile, final String destDirectoryPath) throws IOException {
        final int buffer = 2048;
        final ZipFile zip = new ZipFile(zipFile);
        try {
            new File(destDirectoryPath).mkdir();

            final Enumeration zipFileEntries = zip.entries();

            while (zipFileEntries.hasMoreElements()) {
                final ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                final File destFile = new File(destDirectoryPath, entry.getName());

                destFile.getParentFile().mkdirs();

                if (!entry.isDirectory()) {
                    final BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    try {
                        // write the current file to disk
                        final FileOutputStream fos = new FileOutputStream(destFile);
                        final BufferedOutputStream dest = new BufferedOutputStream(fos,
                                buffer);
                        try {
                            int currentByte;
                            // establish buffer for writing file
                            byte data[] = new byte[buffer];
                            // read and write until last byte is encountered
                            while ((currentByte = is.read(data, 0, buffer)) != -1) {
                                dest.write(data, 0, currentByte);
                            }
                        } finally {
                            dest.flush();
                            dest.close();
                        }
                    } finally {
                        is.close();
                    }
                } else {
                    destFile.mkdirs();
                }
            }
        } finally {
            zip.close();
        }
    }

    /**
     * Packs all unpacked directories with selected directory name suffix into archives of selected type
     * and deletes original directories.
     * @param rootDirectory Directory from which all matching directories are packed into archives.
     * @param archiveType Archive type of resulting archives.
     * @throws IOException
     */
    public static void packAll(final File rootDirectory, final String archiveType) throws IOException {
        final Collection<File> unpackedArchives = FileUtil.findDirectories(rootDirectory, "*." + archiveType, true);
        for (File unpackedDir : unpackedArchives) {
            final String archiveName = unpackedDir.getPath() + ".tmp";
            packDirectory(unpackedDir, archiveName);
            FileUtils.deleteDirectory(unpackedDir);
            FileUtil.renameFile(new File(archiveName), unpackedDir);
        }
    }

    /**
     * Packs selected directory into archive with a given name.
     * @param directory Directory that is archived.
     * @param archiveFileName Name of resulting archive.
     * @throws IOException
     */
    public static void packDirectory(final File directory, final String archiveFileName) throws IOException {
        final FileOutputStream fileOutputStream = new FileOutputStream(archiveFileName);
        final ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
        try {
            packDirectoryInternal(directory, zipOutputStream, directory.getPath());
        } finally {
            zipOutputStream.flush();
            zipOutputStream.close();
        }
    }

    /**
     * Recursively zips selected directory into selected ZipOutputStream.
     * @param directory Directory that is zipped.
     * @param destZip Destined ZipOutputStream.
     * @param basePath Root path of resulting zip.
     * @throws IOException
     */
    private static void packDirectoryInternal(final File directory, final ZipOutputStream destZip,
            final String basePath) throws IOException {
        final File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                final String zipEntryName = file.getAbsolutePath().substring(basePath.length());
                if (file.isDirectory()) {
                    if (file.list().length == 0) {
                        destZip.putNextEntry(new ZipEntry(zipEntryName + File.separator));
                    } else {
                        packDirectoryInternal(file, destZip, basePath);
                    }
                } else {
                    destZip.putNextEntry(new ZipEntry(zipEntryName));
                    final InputStream inputStream = new FileInputStream(file);
                    try {
                        IOUtils.copy(inputStream, destZip);
                        destZip.closeEntry();
                    } finally {
                        inputStream.close();
                    }
                }
            }
        }
    }

    private ZipUtil() {
        // It is prohibited to instantiate util classes.
    }
}
