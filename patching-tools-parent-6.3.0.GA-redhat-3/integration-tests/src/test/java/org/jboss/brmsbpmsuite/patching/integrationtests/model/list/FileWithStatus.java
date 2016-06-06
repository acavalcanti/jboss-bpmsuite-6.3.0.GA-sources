package org.jboss.brmsbpmsuite.patching.integrationtests.model.list;

import java.io.File;

/**
 * Represents a file with it's status.
 */
public class FileWithStatus extends File {

    /**
     * Status of the file. Status is bound to actual test process in which is this file instance present.
     */
    private FileStatusCode status;

    /**
     * Creates a new FileWithStatusInstance.
     * @param pathname Pathname of the file.
     * @param status Status of the file.
     */
    public FileWithStatus(final String pathname, final FileStatusCode status) {
        super(pathname);
        this.status = status;
    }

    public FileStatusCode getStatus() {
        return status;
    }

    public void setStatus(final FileStatusCode status) {
        this.status = status;
    }
}
