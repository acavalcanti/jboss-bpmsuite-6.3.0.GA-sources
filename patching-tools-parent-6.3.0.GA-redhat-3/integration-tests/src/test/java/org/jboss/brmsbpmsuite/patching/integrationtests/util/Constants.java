package org.jboss.brmsbpmsuite.patching.integrationtests.util;

/**
 * Contains general constants used in integration tests.
 */
public final class Constants {

    /**
     * Property key for retrieving environmental variable that defines path of workspace directory
     * of integration tests. Everything that is happening in integration tests is contained within
     * workspace directory.
     */
    public static final String PROPERTY_KEY_WORKSPACE_DIR = "workspace.dir.path";

    /**
     * Property key for retrieving environmental variable that defines name of directory that contains
     * unzipped patch tool.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_UNZIPPED_DIR = "patchtool.unzipped.dir";

    /**
     * Property key for retrieving environmental variable that defines name of directory that contains
     * patch backups.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_BACKUP_DIR = "patchtool.backup.dir";

    /**
     * Property key for retrieving environmental variable that defines name of directory that contains
     * patch updates for various distributions.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_UPDATES_DIR = "patchtool.updates.dir";

    /**
     * Property key for retrieving environmental variable that defines pattern for name of patch tool root directory.
     * Used for searching for patch tool root directory.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_ROOT_DIR_PATTERN = "patchtool.root.dir.pattern";

    /**
     * Property key for retrieving environmental variable that defines name of shell script that
     * is used to run patch tool.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_SCRIPT_APPLYPATCH_SHELL = "patchtool.script.applypatch.shell";

    /**
     * Property key for retrieving environmental variable that defines name of batch script that
     * is used to run patch tool.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_SCRIPT_APPLYPATCH_BATCH = "patchtool.script.applypatch.batch";

    /**
     * Property key for retrieving environmental variable that defines name of file containing
     * blacklist used in patch tool.
     */
    public static final String PROPERTY_KEY_PATCHTOOL_BLACKLIST = "patchtool.list.blacklist";

    /**
     * Property key for retrieving environmental variable that defines name of directory that contains
     * starting distribution. It's the base distribution from which is testing distribution cloned.
     */
    public static final String PROPERTY_KEY_STARTDIST_DIR = "startdist.dir";

    /**
     * Property key for retrieving environmental variable that defines type of starting distribution.
     * It's passed to patch tool as distribution type argument.
     */
    public static final String PROPERTY_KEY_STARTDIST_TYPE = "startdist.type";

    /**
     * Property key for retrieving environmental variable that defines name of directory that contains
     * destined distribution. It's the destined distribution of the patch.
     */
    public static final String PROPERTY_KEY_DESTDIST_DIR = "destdist.dir";

    /**
     * Default directory within workspace that contains testing distribution.
     */
    public static final String DEFAULT_DIRECTORY_TESTDIST = "testdist";

    /**
     * Default directory within workspace that contains patch tool testing distribution.
     */
    public static final String DEFAULT_DIRECTORY_PATCHTOOL_TESTDIST = "patchtooltestdist";

    /**
     * Suffix added to created marker file when patch tool tries to delete file that is on blacklist.
     */
    public static final String FILE_SUFFIX_REMOVED = ".removed";

    /**
     * Suffix added to new file when patch tool tries to update file that is on blacklist.
     * (Wants to update file xy with updated version, but the file is blacklisted so it creates file xy.new instead)
     */
    public static final String FILE_SUFFIX_NEW = ".new";

    /**
     * Message appended to asserts that are bound to some recent log. I.e. few files does not
     * meet blacklist rules so they are logged and then there is an assert that fails.
     */
    public static final String MESSAGE_SEE_RECENT_LOG = "See recent log for more information.";

    /**
     * Suffix appended to extracted WAR files.
     */
    public static final String UNPACKED_DIR_SUFFIX = "-unpacked";

    /**
     * Name of directory within each specific distribution update directory of patch tool that contains
     * patch content. (e.g. for eap6.x bundle ...../updates/eap6.x/new-content/)
     */
    public static final String PATCH_TOOL_NEW_CONTENT_DIR = "new-content";

    /**
     * Name of file that contains checksums list.
     * This file contains checksums of files from previous distribution versions.
     */
    public static final String CHECKSUMS_FILENAME = "checksums.txt";

    private Constants() {
        // It is prohibited to instantiate util classes.
    }
}
