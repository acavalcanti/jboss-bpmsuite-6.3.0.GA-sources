package org.jboss.brmsbpmsuite.patching.integrationtests.model.list;

/**
 * Represents status of tests or operations.
 */
public enum FileStatusCode {
    /**
     * Represents status "Everything is fine".
     */
    OK,

    /**
     * Represents status " ... exists, but is not in the scope of patched distribution."
     */
    NOT_IN_SCOPE_OF_PATCHED_DIST,

    /**
     * Represents status " ... is missing from ... ".
     */
    MISSING,

    /**
     * Represents status " ... has no .new or .removed marker file".
     */
    MARKER_FILE_MISSING,

    /**
     * Represents status " ... has a .new marker file, but should be overwritten, because user didn't change it".
     */
    MARKER_FILE_NOT_NECESSARY,

    /**
     * Represents status " ... is not on blacklist".
     */
    NOT_ON_BLACKLIST,

    /**
     * Represents status " ... is not in patch".
     */
    NOT_IN_PATCH,

    /**
     * Represents status "File ... was overwritten during patching process".
     */
    OVERWRITTEN
}
