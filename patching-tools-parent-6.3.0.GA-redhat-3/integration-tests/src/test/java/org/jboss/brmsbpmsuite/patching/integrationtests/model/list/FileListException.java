package org.jboss.brmsbpmsuite.patching.integrationtests.model.list;

/**
 * Exception representing error occurring in file lists domain.
 */
public class FileListException extends Exception {

    public FileListException() {
    }

    public FileListException(final String message) {
        super(message);
    }

    public FileListException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FileListException(final Throwable cause) {
        super(cause);
    }
}
