package org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileExtensions;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract distribution definition. Contains general distribution handling and provides
 * basic information about distribution.
 */
public abstract class Distribution {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Directory of distribution. Directory containing all distribution files.
     */
    private final File baseDirectory;

    /**
     * Type of distribution.
     */
    private final DistributionType type;

    public Distribution(final File baseDirectory, final DistributionType type) {
        this.baseDirectory = baseDirectory;
        this.type = type;
    }

    public final DistributionType getType() {
        return type;
    }

    /**
     * Gets the most top directory of distribution. E.g. testdist/.
     * @return The most top directory of distribution.
     */
    public final File getBaseDirectory() {
        return baseDirectory;
    }

    /**
     * Gets the bundle root directory of distribution. Unzipped distributions usually contain some root directory,
     * e.g. jboss-6.1.0-deployable-something/. This is the returned directory.
     * @return Bundle root directory.
     */
    public final File getBundleRootDirectory() {
        return FileUtil.findFirstOccurenceOfDirectory(getBaseDirectory(), "*", false);
    }

    /**
     * Gets content root of distribution. Gets content, which is part of patching operation. (e.g. business-central.war/)
     * @return Content root of distribution.
     */
    public abstract File getContentRootDirectory();

    /**
     * Gets file from this distribution that has the same relative path within
     * this distribution as in the other distribution. Not checks for real existence of the file.
     * @param file File that is searched for in this distribution.
     * @param distribution Other distribution that contains specified file.
     * @return File from this distribution with same relative path as it has in specified distribution.
     * In other words, returns "the same file" from this distribution that also is present in specified distribution.
     */
    public final File getFileFromOtherDistribution(final File file, final Distribution distribution) {
        final String filePathWithinThisDistribution = file.getPath().replace(
                distribution.getContentRootDirectory().getPath(),
                this.getContentRootDirectory().getPath());
        return new File(filePathWithinThisDistribution);
    }

    /**
     * Cleans directory that is associated with this distribution instance (Directory passed to constructor).
     * Cleaning means deleting the whole directory.
     * @throws DistributionException Raised, when an error occurs during distribution directory deletion.
     */
    public final void clean() throws DistributionException {
        try {
            FileUtils.deleteDirectory(baseDirectory);
        } catch (IOException e) {
            throw new DistributionException(e.getMessage(), e);
        }
    }

    /**
     * Creates testing distribution by cloning defined starting distribution.
     * @param startingDistribution Distribution that is cloned as testing distribution.
     * @throws DistributionException Raised, when an error occurs during starting distribution cloning.
     */
    public final void create(final Distribution startingDistribution)
            throws DistributionException {
        try {
            FileUtils.copyDirectory(startingDistribution.getBaseDirectory(), baseDirectory);
        } catch (IOException e) {
            throw new DistributionException(e.getMessage(), e);
        }
    }

    /**
     * Corrects distribution. That means it processes all marker files (.removed, .new) and does
     * needed operations to get distribution to state, when it contains no unresolved file conflicts.
     * In case of .removed files, deletes marker files and original files that should be removed.
     * In case of .new files, it replaces original files with .new marker files.
     * @throws DistributionException Raised when there is an error manipulating files.
     */
    public final void correct() throws DistributionException {
        correctRemovedFiles();
        correctNewFiles();
    }

    /**
     * Unpacks all wars that are located in content root directory of this distribution (in the whole distribution).
     * @return True, if there were unpacked some war files, else false.
     * @throws DistributionException Raised when there is an error unpacking war files.
     */
    public final boolean unpackWars() throws DistributionException {
        try {
            return ZipUtil.unpackAll(getBaseDirectory(), FileExtensions.WAR.name())
                    | ZipUtil.unpackAll(getBaseDirectory(), FileExtensions.WAR.name().toLowerCase());
        } catch (IOException e) {
            throw new DistributionException(e.getMessage(), e);
        }
    }

    /**
     * Packs all unpacked war files into war archives.
     * Unpacked war file directory has suffix {@link Constants#UNPACKED_DIR_SUFFIX}.
     * @throws DistributionException
     */
    public final void packWars() throws DistributionException {
        try {
            ZipUtil.packAll(getBaseDirectory(), FileExtensions.WAR.name().toLowerCase());
        } catch (IOException e) {
            throw new DistributionException(e.getMessage(), e);
        }
    }

    /**
     * Removes .removed files and their counterparts that should be removed
     * during patching phase, but were on blacklist.
     */
    private void correctRemovedFiles() {
        final Collection<File> removedFiles =
                FileUtil.findFiles(getContentRootDirectory(), "*" + Constants.FILE_SUFFIX_REMOVED, null);
        for (File removedFile : removedFiles) {
            if (!removedFile.delete()) {
                logger.warn("Unable to delete file " + removedFile.getName() + "!");
            }

            final int indexOfRemovedSuffix = removedFile.getPath().lastIndexOf(Constants.FILE_SUFFIX_REMOVED);
            final String originalRemovedFilename = removedFile.getPath().substring(0, indexOfRemovedSuffix);
            final File originalRemovedFile = new File(originalRemovedFilename);

            if (!originalRemovedFile.delete()) {
                logger.warn("Unable to delete file " + originalRemovedFile.getName() + "!");
            }
        }
    }

    /**
     * Updates files that should be updated during patching phase, but were blacklisted.
     * Finds files with .new suffix, then their original counterparts and replaces original files
     * with .new files.
     * @throws DistributionException Raised when there is an error manipulating files.
     */
    private void correctNewFiles() throws DistributionException {
        final Collection<File> newFiles = FileUtil.findFiles(getContentRootDirectory(), "*" + Constants.FILE_SUFFIX_NEW, null);
        for (File newFile : newFiles) {
            final int indexOfNewSuffix = newFile.getPath().lastIndexOf(Constants.FILE_SUFFIX_NEW);
            final String originalFilename = newFile.getPath().substring(0, indexOfNewSuffix);
            final File originalFile = new File(originalFilename);
            if (!originalFile.delete()) {
                logger.warn("Unable to delete file " + originalFile.getName() + "!");
            }
            if (!newFile.renameTo(originalFile)) {
                throw new DistributionException("Cannot rename file " + newFile.getName()
                        + " to file " + originalFile.getName() + "!");
            }
        }
    }
}
