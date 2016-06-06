package org.jboss.brmsbpmsuite.patching.integrationtests.model.list;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;

/**
 * General filelist class representing file lists from patch tool (blacklist, etc.).
 */
public abstract class FileList {

    /**
     * Files from file list. Set of relative paths to files from file list. These relative paths are relative
     * to distribution to which are they bound.
     */
    private final Set<String> filesFromListRelativePaths = new HashSet<String>();

    /**
     * Original file, from which is this list populated.
     */
    private final File originalFile;

    /**
     * Constructor.
     * @param file File containing file list items.
     * @throws IOException Raised when there's an error reading from defined file.
     */
    public FileList(final File file)
            throws IOException {
        this.originalFile = file;
        populateFileList(file);
    }

    /**
     * Method for verifying patched distribution regarding this file list instance.
     * @param patchedDistribution Distribution that is verified.
     * @param startingDistribution Original distribution from which patched
     * @param patchToolDistribution Distribution from patch tool. Distribution containing patch files.
     * distribution was created (by applying the patch).
     * @return Set of file test results that contains information about files that fails in verification.
     * @throws FileListException
     */
    public abstract Set<FileWithStatus> verifyDistribution(final Distribution patchedDistribution,
            final Distribution startingDistribution, final Distribution patchToolDistribution) throws FileListException;

    /**
     * Appends text to original file list file as a new line.
     * @param text Text that is appended.
     * @throws IOException Raises if there is an error writing to original file list file.
     */
    public final void appendTextToOriginalFile(final String text) throws IOException {
        FileUtil.appendTextToFile(originalFile, text);
        filesFromListRelativePaths.add(text);
    }

    /**
     * Replaces all occurences of text in original file with replacement text.
     * @param originalText Original text that is replaces.
     * @param replacement Replacement text.
     */
    public final void replaceTextInOriginalFile(final String originalText, final String replacement)
            throws IOException {
        final FileReader reader = new FileReader(originalFile);
        final BufferedReader bufferedReader = new BufferedReader(reader);

        // Must be done reading whole file into memory, because this method should be
        // able to replace also line separators.
        final StringBuffer textFromFileBuffer = new StringBuffer();
        char[] charBuffer = new char[2048];
        try {
            while (bufferedReader.read(charBuffer) != -1) {
                textFromFileBuffer.append(charBuffer);
            }
        } finally {
            bufferedReader.close();
        }

        final File tempFile = new File(originalFile.getPath() + ".tmp");
        final PrintWriter out =
                new PrintWriter(new BufferedWriter(new FileWriter(tempFile, false)));
        try {
            out.println(textFromFileBuffer.toString().replace(originalText, replacement));
        } finally {
            out.close();
        }

        originalFile.delete();
        tempFile.renameTo(originalFile);
    }

    /**
     * Returns all files from this file list instance that are missing from defined distribution or exist, but
     * are out of scope of patched distribution.
     * @param distribution Distribution that is checked for file existence.
     * @return Files that are missing or out of scope of defined distribution, based on this file list instance.
     */
    protected final Set<FileWithStatus> getFilesThatNotExistOrOutOfScopeOfPatchedDistribution(
            final Distribution distribution) {
        final Set<FileWithStatus> filesMissingOrOutOfScope = new HashSet<FileWithStatus>();
        final String distributionContentRootPath = distribution.getContentRootDirectory().getPath();
        for (String fileFromListPath : filesFromListRelativePaths) {
            final FileWithStatus distributionFile =
                    new FileWithStatus(distribution.getBundleRootDirectory() + File.separator + fileFromListPath,
                            FileStatusCode.OK);
            if (!distributionFile.exists()) {
                distributionFile.setStatus(FileStatusCode.MISSING);
                filesMissingOrOutOfScope.add(distributionFile);
            } else if (!distributionFile.getPath().startsWith(distributionContentRootPath)) {
                distributionFile.setStatus(FileStatusCode.NOT_IN_SCOPE_OF_PATCHED_DIST);
                filesMissingOrOutOfScope.add(distributionFile);
            }
        }
        return filesMissingOrOutOfScope;
    }

    /**
     * Creates initial files set that contains all files from this file list instance,
     * that point to files from defined distribution (also if they are nonexistent in the distribution).
     * @param distribution Distribution to which files from result set are pointing.
     * @return Files from this file list, that point to defined distribution (with their path).
     */
    protected final Set<FileWithStatus> getInitFileListTestResultSetForDistribution(final Distribution distribution) {
        final Set<FileWithStatus> resultSet = new HashSet<FileWithStatus>();
        for (String fileFromListPath : filesFromListRelativePaths) {
            resultSet.add(new FileWithStatus(distribution.getBundleRootDirectory() + File.separator + fileFromListPath,
                    FileStatusCode.OK));
        }
        return resultSet;
    }

    /**
     * Reads file list from file.
     * @param file File from which is this file list read.
     * @throws IOException Raised when reading from defined file fails.
     */
    private void populateFileList(final File file) throws IOException {
        final List<String> allLines = FileUtils.readLines(file);
        for (String line : allLines) {
            if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                filesFromListRelativePaths.add(line);
            }
        }
    }
}
