package org.jboss.brmsbpmsuite.patching.integrationtests.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

/**
 * Represents file extensions used in integration tests.
 */
public enum FileExtensions {
    WAR, ZIP;

    public static boolean isZipArchive(final File file) {
        if (file != null && file.isFile()) {
            final String fileExtension = FilenameUtils.getExtension(file.getName());
            return (!fileExtension.isEmpty())
                    && ((WAR.toString().equals(fileExtension.toUpperCase()))
                        || (ZIP.toString().equals(fileExtension.toUpperCase())));
        } else {
            return false;
        }
    }
}
