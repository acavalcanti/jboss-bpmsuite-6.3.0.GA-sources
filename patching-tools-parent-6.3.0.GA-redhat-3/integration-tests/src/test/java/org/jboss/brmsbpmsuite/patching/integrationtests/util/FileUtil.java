package org.jboss.brmsbpmsuite.patching.integrationtests.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.tools.ant.DirectoryScanner;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileStatusCode;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.FileWithStatus;

/**
 * Contains util methods for handling files and directories.
 */
public final class FileUtil {

    /**
     * Compares if two directories are identical (contain same files and directory structure).
     * @param directory Directory to compare.
     * @param compareDirectory Directory to compare.
     * @param compareNames If true, also directory names are compared
     * (Names of input directories or files are compared).
     * @param compareContentNames If true, directories contents are tested also for name equality.
     * Files and directories that are inside input directories are compared also by name.
     * @return Fileset that contains files that didn't match in comparison.
     * @throws IOException
     */
    public static Set<File> compareDirectories(final File directory, final File compareDirectory,
            final boolean compareNames, final boolean compareContentNames) throws IOException {
        checkDirectoryExists(directory);
        checkDirectoryExists(compareDirectory);

        // This set is returned as result.
        final Set<File> resultSet = new HashSet<File>();

        if (compareNames) {
            if (!directory.getName().equals(compareDirectory.getName())) {
                resultSet.add(directory);
                resultSet.add(compareDirectory);
            }
        }

        final File[] directoryFiles = directory.listFiles();
        final File[] compareDirectoryFiles = compareDirectory.listFiles();

        if (directoryFiles == null && compareDirectoryFiles == null) {
            return resultSet;
        } else if (directoryFiles == null) {
            resultSet.addAll(traverseAllFiles(new HashSet<File>(Arrays.asList(compareDirectoryFiles)), true));
            return resultSet;
        } else if (compareDirectoryFiles == null) {
            resultSet.addAll(traverseAllFiles(new HashSet<File>(Arrays.asList(directoryFiles)), true));
            return resultSet;
        }

        // TreeSet is used because sorting is needed later.
        final Set<File> directoryFilesSet = new TreeSet<File>(Arrays.asList(directoryFiles));
        final Set<File> compareDirectoryFilesSet = new TreeSet<File>(Arrays.asList(compareDirectoryFiles));

        // This will contain all files that are not in intersection of both file sets.
        final Set<File> symmetricDifference = new HashSet<File>();
        symmetricDifference.addAll(removeAllFilenames(directoryFilesSet, getFileNames(compareDirectoryFilesSet)));
        symmetricDifference.addAll(removeAllFilenames(compareDirectoryFilesSet, getFileNames(directoryFilesSet)));

        resultSet.addAll(traverseAllFiles(symmetricDifference, true));
        directoryFilesSet.removeAll(symmetricDifference);
        compareDirectoryFilesSet.removeAll(symmetricDifference);

        // Now directoryFilesSet and compareDirectoryFilesSet contain only files that are the same by path.

        final Iterator<File> compareDirectoryFilesIterator = compareDirectoryFilesSet.iterator();
        for (File directoryFile : directoryFilesSet) {
            final File compareDirectoryFile = compareDirectoryFilesIterator.next();
            if (directoryFile.isDirectory() != compareDirectoryFile.isDirectory()) {
                resultSet.add(directoryFile);
                resultSet.add(compareDirectoryFile);
                resultSet.addAll(traverseAllFiles(directoryFile));
                resultSet.addAll(traverseAllFiles(compareDirectoryFile));
            }
            if (directoryFile.isDirectory()) {
                resultSet.addAll(compareDirectories(directoryFile, compareDirectoryFile, compareContentNames,
                        compareContentNames));
            } else {
                if (!compareFiles(directoryFile, compareDirectoryFile, compareContentNames)) {
                    resultSet.add(directoryFile);
                    resultSet.add(compareDirectoryFile);
                }
            }
        }

        return resultSet;
    }

    /**
     * Compares two files. Compares if file contents are identical (ignoring EOL). Also compares filenames based on
     * method argument.
     * @param file File to compare.
     * @param compareFile File to compare.
     * @param compareNames If true, also filenames are compared, else only file contents are compared.
     * @return True if defined input files are identical, else returns false.
     * @throws IOException
     */
    public static boolean compareFiles(final File file, final File compareFile, final boolean compareNames)
            throws IOException {
        if (file == null && compareFile == null) {
            return true;
        } else if (file == null || compareFile == null) {
            return false;
        }

        if (compareNames) {
            if (!file.getName().equals(compareFile.getName())) {
                return false;
            }
        }

        // If the files are war files or zip files, they need to be compared unpacked (i.e. because of was8 patching).
        if (FileExtensions.isZipArchive(file) && FileExtensions.isZipArchive(compareFile)) {
            return compareZips(file, compareFile);
        } else {
            return Files.equal(file, compareFile);
        }
    }

    /**
     * Finds file within a directory.
     * @param directory Directory that contains searched file.
     * @param filenamePattern Pattern for matching file. First occurence of a file whose filename matches this
     * pattern is returned.
     * @param directoryNamePattern Pattern for matching directories which are searched for file.
     * Only directories whose names match this pattern are searched for file.
     * @return File instance representing found file.
     * If searched file is not found within defined directory,
     * or the directory contains more files with searched filenamePattern, method raises exception.
     */
    public static File findFile(final File directory, final String filenamePattern, final String directoryNamePattern) {
        final Collection<File> foundFiles = findFiles(directory, filenamePattern, directoryNamePattern);
        if (foundFiles.isEmpty()) {
            throw new IllegalArgumentException("No file that matches pattern " + filenamePattern + " found!");
        }
        if (foundFiles.size() > 1) {
            throw new IllegalArgumentException("There are more files with name " + filenamePattern + " than one!");
        }
        //  Simply take the first one. There should be only one.
        return foundFiles.iterator().next();
    }

    /**
     * Finds files within a directory.
     * @param directory Directory that contains searched files.
     * @param filenamePattern Pattern for matching files. Files whose filenames match this pattern are returned.
     * @param directoryNamePattern Pattern for directories which are searched for files.
     * Only directories whose names match this pattern are searched for files.
     * @return File instances representing found files.
     */
    public static Collection<File> findFiles(final File directory, final String filenamePattern,
            final String directoryNamePattern) {
        checkDirectoryExists(directory);
        if (directoryNamePattern == null || directoryNamePattern.isEmpty()) {
            return FileUtils.listFiles(directory, new WildcardFileFilter(filenamePattern), TrueFileFilter.INSTANCE);
        } else {
            return FileUtils.listFiles(directory, new WildcardFileFilter(filenamePattern),
                    new WildcardFileFilter(directoryNamePattern));
        }
    }

    /**
     * Finds directories within a directory.
     * @param rootDirectory Directory that contains searched directory.
     * @param directoryNamePattern Patern for matching directories.
     * @param recursive If true, the whole directory tree within selected directory is searched.
     * Directories whose names match this pattern are returned.
     * @return File instances representing found directories.
     */
    public static Collection<File> findDirectories(final File rootDirectory, final String directoryNamePattern,
            final boolean recursive) {
        checkDirectoryExists(rootDirectory);

        final DirectoryScanner scanner = new DirectoryScanner();
        if (recursive) {
            scanner.setIncludes(new String[]{"**/" + directoryNamePattern});
        } else {
            scanner.setIncludes(new String[]{directoryNamePattern});
        }
        scanner.setBasedir(rootDirectory);
        scanner.setCaseSensitive(false);
        scanner.scan();
        final String[] foundFiles = scanner.getIncludedDirectories();

        final Set<File> resultSet = new HashSet<File>();
        if (foundFiles != null && foundFiles.length > 0) {
            for (String foundFile : foundFiles) {
                resultSet.add(new File(rootDirectory.getPath() + File.separator + foundFile));
            }
        }
        return resultSet;
    }

    /**
     * Finds first occurence of directory with a given name that matches given pattern.
     * @param directory Directory that contains searched directory.
     * @param directoryNamePattern Pattern for matching directories.
     * @param recursive If true, the whole directory tree within selected directory is searched.
     * @return File instance representing found directory. If no directory is found, returns null.
     */
    public static File findFirstOccurenceOfDirectory(final File directory, final String directoryNamePattern,
            final boolean recursive) {
        final Collection<File> foundDirs = findDirectories(directory, directoryNamePattern, recursive);
        if (foundDirs != null && !foundDirs.isEmpty()) {
            return foundDirs.iterator().next();
        } else {
            return null;
        }
    }

    /**
     * Checks if directory exists. If not, raises an exception.
     * @param directory Directory that is checked for existence.
     */
    public static void checkDirectoryExists(final File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException("Directory " + directory.getPath() + " not exists!");
        }
    }

    /**
     * Traverses all files in directory trees and returns them.
     * @param files Files that are traversed.
     * @param addRootFilesToResult If false, only files that are on lower directory tree level than root
     * are added to resulting set. Else, all files are added.
     * All input files are considered to be on root level.
     * Other files that are accessed through traversal are on lower levels.
     * @return All files from directory trees of input files + optionally input files
     * (depends on addRootFilesToResult argument)
     */
    public static Set<File> traverseAllFiles(final Set<File> files,
            final boolean addRootFilesToResult) {
        final Set<File> resultSet = new HashSet<File>();
        if (files != null) {
            for (File file : files) {
                if (file.exists()) {
                    if (addRootFilesToResult) {
                        resultSet.add(file);
                    }
                    if (file.isDirectory()) {
                        resultSet.addAll(traverseAllFiles(file));
                    }
                }
            }
        }
        return resultSet;
    }

    /**
     * Traverses all files in directory and returns them. Traverses also all subdirectories.
     * @param directory File that is traversed.
     * @return All files from directory.
     */
    public static Set<File> traverseAllFiles(final File directory) {
        final Set<File> resultSet = new HashSet<File>();
        if (directory != null && directory.exists() && directory.isDirectory()) {
            final File[] directoryFiles = directory.listFiles();
            if (directoryFiles != null && directoryFiles.length > 0) {
                resultSet.addAll(Arrays.asList(directoryFiles));
                for (File file : directoryFiles) {
                    if (file.isDirectory()) {
                        resultSet.addAll(traverseAllFiles(file));
                    }
                }
            }
        }
        return resultSet;
    }

    /**
     * Filters set of files with statuses by status.
     * @param files Set of files with statuses that is filtered.
     * @param status Status by which the files are filtered.
     * @return Set of files from input set, which contains only files with defined status.
     */
    public static Set<FileWithStatus> filterFilesByStatus(final Set<FileWithStatus> files,
            final FileStatusCode status) {
        final Set<FileWithStatus> resultSet = new HashSet<FileWithStatus>();
        for (FileWithStatus file : files) {
            if (file.getStatus() == status) {
                resultSet.add(file);
            }
        }
        return resultSet;
    }

    /**
     * Deletes selected file.
     * @param file File to be deleted.
     */
    public static void deleteFile(final File file) {
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Cannot delete file " + file.getPath() + "!");
        }
    }

    /**
     * Renames selected file.
     * @param originalFile File to be renamed.
     * @param newFile File to which is originalFile renamed.
     */
    public static void renameFile(final File originalFile, final File newFile) {
        if (originalFile.exists() && !originalFile.renameTo(newFile)) {
            throw new IllegalStateException(
                    "Cannot rename file " + originalFile.getPath() + " to " + newFile.getPath() + "!");
        }
    }

    /**
     * Checks if checksum of specified file matches one of specified checksums.
     *
     * @param file File which checksum is being matched.
     * @param checksums List of checksums.
     * @return True, if checksum of specified file matches one of the specified checksums, else false.
     * @throws IOException
     */
    public static boolean compareChecksums(final File file, final List<String> checksums) throws IOException {
        if (checksums != null && checksums.size() > 0) {
            for (String checksum : checksums) {
                if (compareChecksums(file, checksum)) {
                    return true;
                }
            }
            return false;
        } else {
            throw new IllegalArgumentException("Checksums cannot be null or empty!");
        }
    }

    /**
     * Compare checksum of specified file with specified checksum.
     *
     * @param file File which checksum is compared.
     * @param checksum Checksum to compare.
     * @return True, if the checksums match, else false.
     * @throws IOException
     */
    public static boolean compareChecksums(final File file, final String checksum) throws IOException {
        if (checksum != null) {
            final String actualChecksum = Files.hash(file, Hashing.md5()).toString();
            return checksum.equals(actualChecksum);
        } else {
            throw new IllegalArgumentException("Specified checksum cannot be null!");
        }
    }

    /**
     * Returns filename from path.
     * @param path File path.
     * @return Filename.
     */
    public static String getFileNameFromPath(final String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null!");
        } else {
            final File file = new File(path);
            return file.getName();
        }
    }

    /**
     * Appends specified text to the end of specified file.
     * @param file Text file to append text to.
     * @param text Appended text.
     * @throws IOException
     */
    public static void appendTextToFile(final File file, final String text) throws IOException {
        final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
        try {
            out.println(System.getProperty("line.separator") + text);
        } finally {
            out.close();
        }
    }

    /**
     * Removes all files, with specified names from collection of filenames, from base file collection.
     * @param baseFiles Base collection of files.
     * @param subtractedFilenames Collection of filenames.
     * @return Set of files.
     */
    private static Set<File> removeAllFilenames(final Collection<File> baseFiles,
            final Collection<String> subtractedFilenames) {
        final Set<File> resultSet = new HashSet<File>();
        if (baseFiles != null) {
            for (File file : baseFiles) {
                if (!subtractedFilenames.contains(file.getName())) {
                    resultSet.add(file);
                }
            }
        }
        return resultSet;
    }

    /**
     * Extracts a set of filenames from collection of files.
     * @param files Collection of files.
     * @return Set of filenames. These filenames are names of files from specified collection of files.
     */
    private static Set<String> getFileNames(final Collection<File> files) {
        final Set<String> resultSet = new HashSet<String>();
        if (files != null) {
            for (File file : files) {
                resultSet.add(file.getName());
            }
        }
        return resultSet;
    }

    /**
     * Compares two zip files. Compares if they contain the same content and their names are the same.
     * @param zip1 Zip file compared with zip2.
     * @param zip2 Zip file compared with zip1.
     * @return True, if selected zips are the same, else false.
     * @throws IOException
     */
    private static boolean compareZips(final File zip1, final File zip2) throws IOException {
        if (!zip1.getName().equals(zip2.getName())) {
            return false;
        }
        final File parentUnpackedZipsDir = zip1.getParentFile();
        final File zip1Unpacked = new File(parentUnpackedZipsDir.getPath() + File.separator
                + zip1.getName() + Constants.UNPACKED_DIR_SUFFIX);
        final File zip2Unpacked = new File(parentUnpackedZipsDir.getPath() + File.separator
                + zip2.getName() + "-2-" + Constants.UNPACKED_DIR_SUFFIX);
        ZipUtil.unpack(zip1, zip1Unpacked.getPath());
        ZipUtil.unpack(zip2, zip2Unpacked.getPath());
        final Set<File> differences =
                compareDirectories(zip1Unpacked, zip2Unpacked, false, true);
        FileUtils.deleteDirectory(zip1Unpacked);
        FileUtils.deleteDirectory(zip2Unpacked);
        return differences.isEmpty();
    }

    private FileUtil() {
        // It is prohibited to instantiate util classes.
    }
}
