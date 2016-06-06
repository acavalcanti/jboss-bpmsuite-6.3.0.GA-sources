package org.jboss.brmsbpmsuite.patching.integrationtests.model.list;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;

/**
 * Represents files blacklist from patch tool.
 */
public class BlackList extends FileList {

    private final Map<String, List<String>> distFilesChecksums;

    /**
     * Constructor.
     * @param file File containing blacklist items.
     * @param distFilesChecksums Checksums of all files from distribution.
     * @throws IOException Raised when there's an error reading from defined file.
     */
    public BlackList(final File file, final Map<String, List<String>> distFilesChecksums) throws IOException {
        super(file);
        this.distFilesChecksums = distFilesChecksums;
    }

    /**
     * Verifies patched distribution against this blacklist instance.
     * Verifies that files, that are on this blacklist aren't
     * modified and are processed according to correct blacklist rules.
     * I.e. all requried marker files are created.
     * @param patchedDistribution Distribution that is verified.
     * @param startingDistribution Original distribution from which patched
     * @param patchToolDistribution Distribution from patch tool. Distribution containing patch files.
     * distribution was created (by applying the patch).
     * @return Set of files from this blacklist instance, that were not correctly blacklisted.
     * @throws FileListException
     */
    @Override
    public Set<FileWithStatus> verifyDistribution(final Distribution patchedDistribution,
            final Distribution startingDistribution, final Distribution patchToolDistribution) throws FileListException {

        final Set<FileWithStatus> blacklistedFiles = getInitFileListTestResultSetForDistribution(patchedDistribution);
        final Set<FileWithStatus> resultSet = getFilesThatNotExistOrOutOfScopeOfPatchedDistribution(patchedDistribution);

        blacklistedFiles.removeAll(resultSet);

        // There should be a .new or .removed marker file for each one of these files that user changed.
        for (FileWithStatus file : blacklistedFiles) {
            final File newMarkerFile = new File(file.getPath() + Constants.FILE_SUFFIX_NEW);
            final File removedMarkerFile =
                    new File(file.getPath() + Constants.FILE_SUFFIX_REMOVED);
            final boolean newMarkerFileExists = newMarkerFile.exists();
            final boolean removedMarkerFileExists = removedMarkerFile.exists();
            final boolean fileChangedByUser = !compareChecksumForFile(file, startingDistribution, patchedDistribution);
            if (!newMarkerFileExists && !removedMarkerFileExists) {
                // Blacklist may contain files that are not in patch, so these files should be ignored.
                final File fileFromPatchToolDist = patchToolDistribution.getFileFromOtherDistribution(file, patchedDistribution);
                if (fileFromPatchToolDist.exists()) {
                    // If the original file was changed by the user marker file must exist for this file.
                    if (fileChangedByUser) {
                        file.setStatus(FileStatusCode.MARKER_FILE_MISSING);
                        resultSet.add(file);
                    }
                } else {
                    file.setStatus(FileStatusCode.NOT_IN_PATCH);
                    resultSet.add(file);
                }
            } else {
                // Check that created marker file is necessary. If the file wasn't changed by the user,
                // the file should be overwritten and marker file should not be created.
                // Also when there are both marker files, it is wrong.
                if ((!fileChangedByUser) || (newMarkerFileExists && removedMarkerFileExists)) {
                    file.setStatus(FileStatusCode.MARKER_FILE_NOT_NECESSARY);
                    resultSet.add(file);
                }
            }

            // Also it's needed to test if the file was not overwritten during patching process
            // (there can be marker file, but file can be also overwritten).
            if (fileChangedByUser && !compareFileFromDistributions(file, patchedDistribution, startingDistribution)) {
                resultSet.add(new FileWithStatus(file.getPath(), FileStatusCode.OVERWRITTEN));
            }
        }

        // Checks if there are some marker files whose parent files are not on blacklist.
        resultSet.addAll(crossCheckMarkerFiles(patchedDistribution));

        return resultSet;
    }

    /**
     * Compares checksum of a file from starting distribution with checksums from patch.
     * @param file Checked file.
     * @param startingDist Starting distribution.
     * @param patchedDist Patched distribution.
     * @return True, if file's checksum matches one of checksums specified for this file in patch, else return false.
     * @throws FileListException
     */
    private boolean compareChecksumForFile(final File file, final Distribution startingDist,
            final Distribution patchedDist) throws FileListException {
        final List<String> fileChecksums = distFilesChecksums.get(
                getRelativePathForFile(file, patchedDist).replace("\\", "/"));
        final File fileFromStartDist = startingDist.getFileFromOtherDistribution(file, patchedDist);
        try {
            return FileUtil.compareChecksums(fileFromStartDist, fileChecksums);
        } catch (IOException e) {
            throw new FileListException(e.getMessage(), e);
        }
    }

    /**
     * Crosschecks marker files. Finds all .new .removed marker files from distribution and compares it with
     * blacklist. Returns all files that are not in blacklist.
     * @param distribution Distribution which is searched for marker files.
     * @return Marker files, that are not part of input marker files set.
     */
    private Set<FileWithStatus> crossCheckMarkerFiles(final Distribution distribution) {
        final Set<FileWithStatus> resultSet = new HashSet<FileWithStatus>();

        final Set<FileWithStatus> blacklistedFiles = getInitFileListTestResultSetForDistribution(distribution);

        checkMarkerFilesFromDistribution(distribution, resultSet, blacklistedFiles, Constants.FILE_SUFFIX_NEW);
        checkMarkerFilesFromDistribution(distribution, resultSet, blacklistedFiles, Constants.FILE_SUFFIX_REMOVED);

        return resultSet;
    }

    /**
     * Checks marker files with defined suffix. Finds all marker files with defined suffix and checks
     * if they are on defined blacklist.
     * @param distribution Distribution which is searched for marker files.
     * @param resultSet Resulting set of files.
     * @param blacklistedFiles Blacklisted files set.
     * @param markerFileSuffix Suffix of checked marker files.
     */
    private void checkMarkerFilesFromDistribution(final Distribution distribution, final Set<FileWithStatus> resultSet,
            final Set<FileWithStatus> blacklistedFiles, final String markerFileSuffix) {
        final Set<File> markerFiles =
                new HashSet<File>(FileUtil.findFiles(distribution.getContentRootDirectory(), "*" + markerFileSuffix, null));
        for (File markerFile : markerFiles) {
            String originalFilePath = markerFile.getPath();
            originalFilePath = originalFilePath.substring(0, originalFilePath.lastIndexOf(markerFileSuffix));
            if (!blacklistedFiles.contains(new FileWithStatus(originalFilePath, FileStatusCode.OK))) {
                resultSet.add(new FileWithStatus(markerFile.getPath(), FileStatusCode.NOT_ON_BLACKLIST));
            }
        }
    }

    /**
     * Compares one file from distribution with it's another version from another distribution.
     * @param file File that is compared.
     * @param distribution Distribution from which is the compared file.
     * @param distributionToCompareWith Distribution containing another version of compared file.
     * @return True, if compared versions of defined file are identical, else returns false.
     * @throws FileListException
     */
    private boolean compareFileFromDistributions(final File file, final Distribution distribution,
            final Distribution distributionToCompareWith) throws FileListException {
        final String filePathFromDistributionToCompare = file.getPath().replace(
                distribution.getContentRootDirectory().getPath(),
                distributionToCompareWith.getContentRootDirectory().getPath());
        try {
            return FileUtil.compareFiles(file, new File(filePathFromDistributionToCompare), true);
        } catch (IOException e) {
            throw new FileListException(e.getMessage(), e);
        }
    }

    private String getRelativePathForFile(final File file, final Distribution distribution) {
        return file.getAbsolutePath().substring(distribution.getBundleRootDirectory().getAbsolutePath().length() + 1);
    }
}
