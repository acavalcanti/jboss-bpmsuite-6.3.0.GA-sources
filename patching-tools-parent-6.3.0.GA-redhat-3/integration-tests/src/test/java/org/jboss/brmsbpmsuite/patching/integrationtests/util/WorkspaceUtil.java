package org.jboss.brmsbpmsuite.patching.integrationtests.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.DistributionType;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.PatchToolDistribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.ProductDistribution;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.list.BlackList;

/**
 * Contains util methods for getting instances and information from workspace.
 */
public final class WorkspaceUtil {

    /**
     * Creates new testing distribution instance. Testing distribution will be created with path
     * workspace.dir + testdist.dir + customPathAddition + type modifier (based on distributon type).
     * workspace.dir, testdist.dir and distribution type are environmental variables.
     * @param customPathAddition Custom path that is added to default testing distribution path.
     * @return Instance of testing distribution.
     */
    public static Distribution getNewTestingDistributionInstance(final String customPathAddition) {
        return new ProductDistribution(
                new File(getTestingDistributionDirFile().getPath() + File.separator + customPathAddition),
                getStartingDistributionType());
    }

    /**
     * Creates new testing distribution instance. Testing distribution will be created with path
     * workspace.dir + testdist.dir + type modifier (based on distributon type).
     * workspace.dir, testdist.dir and distribution type are environmental variables.
     * @return Instance of testing distribution.
     */
    public static Distribution getNewTestingDistributionInstance() {
        return new ProductDistribution(getTestingDistributionDirFile(), getStartingDistributionType());
    }

    /**
     * Creates new starting distribution instance. Starting distribution will be created with path
     * workspace.dir + startdist.dir + type modifier (based on distribution type).
     * workspace.dir, startdist.dir and distribution type are environmental variables.
     * @return Instance of starting distribution.
     */
    public static Distribution getNewStartingDistributionInstance() {
        return new ProductDistribution(getStartingDistributionDirFile(), getStartingDistributionType());
    }

    /**
     * Creates new destined distribution instance. Destined distribution will be created with path
     * workspace.dir + destdist.dir + type modifier (based on distribution type).
     * workspace.dir, destdist.dir and distribution type are environmental variables.
     * @return Instance of destined distribution.
     */
    public static Distribution getNewDestinedDistributionInstance() {
        return new ProductDistribution(getDestinedDistributionDirFile(), getStartingDistributionType());
    }

    /**
     * Creates new patch tool distribution instance.
     * Patch tool distribution will be created with path according to distribution type attribute.
     * @param testingDistribution If true, returned instance is for testing patch tool distribution,
     * else it's for starting patch tool distribution.
     * @return Instance of testing patch tool distribution.
     */
    public static Distribution getNewPatchToolDistributionInstance(final boolean testingDistribution) {
        final File distributionDirectory;
        if (testingDistribution) {
            distributionDirectory = getPatchToolTestingDistributionDirFile();
        } else {
            distributionDirectory = getPatchToolStartingDistributionDirFile();
        }
        return new PatchToolDistribution(distributionDirectory, getStartingDistributionType());
    }

    /**
     * Creates new distribution blacklist instance from blacklist file. Blacklist filename is defined
     * in an environmental variable.
     * @return Blacklist instance.
     * @throws IOException Raised when an error occurs reading blacklist file.
     */
    public static BlackList getBlacklist(final DistributionType distributionType) throws IOException {
        final String blacklistFilename = System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_BLACKLIST);
        final String patchToolRootDirPattern = System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_ROOT_DIR_PATTERN);
        return new BlackList(FileUtil.findFile(getPatchToolTestingDistributionDirFile(), blacklistFilename,
                patchToolRootDirPattern),
                getDistFilesChecksums(distributionType));
    }

    /**
     * Finds first directory that contains distribution backup (Finds first backup).
     * If there is no backup directory or backup is not a directory, but a file, method throws Exception.
     * @param distributionType Type of backed up distribution. This is needed because there can be backups for
     * different kinds of distributions.
     * @return First backup found in patch tool backup directory.
     * If there is no backup in backup directory, returns null.
     */
    public static File findFirstDistributionBackup(final DistributionType distributionType) {
        final String backupDirName = System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_BACKUP_DIR);
        final File backupDir = FileUtil.findFirstOccurenceOfDirectory(
                getPatchToolTestingDistributionDirFile(), backupDirName, true);
        if (backupDir == null) {
            throw new IllegalStateException("Directory with name " + backupDirName + " doesn't exist!");
        }

        final File[] backups = backupDir.listFiles();
        if (backups == null || backups.length == 0) {
            return null;
        } else {
            final File testDistributionBackupDir =
                    FileUtil.findFirstOccurenceOfDirectory(backups[0], distributionType.getName(), true);

            if (testDistributionBackupDir == null) {
                throw new IllegalStateException("Test distribution backup doesn't exist!");
            }
            return testDistributionBackupDir;
        }
    }

    /**
     * Gets name of directory that contains starting distribution from environmental variable.
     * @return Name of directory that contains starting distribution.
     */
    public static File getStartingDistributionDirFile() {
        return new File(System.getProperty(Constants.PROPERTY_KEY_WORKSPACE_DIR) + File.separator
                + System.getProperty(Constants.PROPERTY_KEY_STARTDIST_DIR));
    }

    /**
     * Gets name of directory that contains destined distribution from environmental variable.
     * @return Name of directory that contains destined distribution.
     */
    public static File getDestinedDistributionDirFile() {
        return new File(System.getProperty(Constants.PROPERTY_KEY_WORKSPACE_DIR) + File.separator
                + System.getProperty(Constants.PROPERTY_KEY_DESTDIST_DIR));
    }

    /**
     * Gets name of directory that contains testing distribution.
     * @return Name of directory that contains testing distribution.
     */
    public static File getTestingDistributionDirFile() {
        return new File(System.getProperty(Constants.PROPERTY_KEY_WORKSPACE_DIR) + File.separator
                + Constants.DEFAULT_DIRECTORY_TESTDIST);
    }

    /**
     * Gets name of directory that contains unzipped patch tool from environmental variable.
     * @return Name of directory that contains unzipped patch tool.
     */
    public static File getPatchToolStartingDistributionDirFile() {
        return new File(System.getProperty(Constants.PROPERTY_KEY_WORKSPACE_DIR) + File.separator
                + System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_UNZIPPED_DIR));
    }

    /**
     * Gets name of directory that contains testing patch tool distribution.
     * @return Name of directory that contains testing patch tool distribution.
     */
    public static File getPatchToolTestingDistributionDirFile() {
        return new File(System.getProperty(Constants.PROPERTY_KEY_WORKSPACE_DIR) + File.separator
                + Constants.DEFAULT_DIRECTORY_PATCHTOOL_TESTDIST);
    }

    /**
     * Reads starting distribution type from environmental variable.
     * @return Starting distribution type.
     */
    public static DistributionType getStartingDistributionType() {
        return DistributionType.getDistributionTypeByName(
                System.getProperty(Constants.PROPERTY_KEY_STARTDIST_TYPE));
    }

    /**
     * Gets files checksums for specified distribution type from patch tool bundle.
     * Patch tool contains checksums of files from original distribution, so it can check if some file
     * was changed by the user.
     * @param distributionType
     * @return Checksums of files. Key is file name, value is list of checksums.
     * File can have more checksums, because patch bundle is able to patch more previous distribution versions.
     */
    public static Map<String, List<String>> getDistFilesChecksums(final DistributionType distributionType) {
        final File updatesDir = FileUtil.findFirstOccurenceOfDirectory(
                getPatchToolTestingDistributionDirFile(),
                System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_UPDATES_DIR), true);
        final File distFilesRootDir = new File(updatesDir, distributionType.getRelativeRootDirectoryPathPatchTool());
        final File checksumsFile = new File(distFilesRootDir, Constants.CHECKSUMS_FILENAME);
        return extractChecksumsFromFile(checksumsFile);
    }

    private static Map<String, List<String>> extractChecksumsFromFile(final File checksumsFile) {
        Properties checksumProps = new Properties();
        try {
            checksumProps.load(new FileReader(checksumsFile));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Map<String, List<String>> checksumsMap = new HashMap<String, List<String>>();
        for (String path : checksumProps.stringPropertyNames()) {
            String checksumsStr = checksumProps.getProperty(path);
            if (checksumsStr == null || checksumsStr.trim().isEmpty()) {
                throw new RuntimeException("No actual checksums found for path" + path + "! Using checksums file " +
                        checksumsFile.getAbsolutePath());
            }
            checksumsMap.put(path, extractChecksums(checksumsStr));
        }
        return checksumsMap;
    }

    private static List<String> extractChecksums(final String checksumsString) {
        String[] parts = checksumsString.trim().split(",");
        return Arrays.asList(parts);
    }

    private WorkspaceUtil() {
        // It is prohibited to instantiate util classes.
    }
}
