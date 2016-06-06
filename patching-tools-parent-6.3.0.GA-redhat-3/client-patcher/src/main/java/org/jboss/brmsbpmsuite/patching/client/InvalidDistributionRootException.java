package org.jboss.brmsbpmsuite.patching.client;

/**
 * Indicates that the provided distribution root is not suitable for patching. The reason could be that the directory actually does
 * not exist, or contains unexpected content. This usually indicates user error.
 */
public class InvalidDistributionRootException extends ClientPatcherException {

    public InvalidDistributionRootException() {
    }

    public InvalidDistributionRootException(String message) {
        super(message);
    }

    public InvalidDistributionRootException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDistributionRootException(Throwable cause) {
        super(cause);
    }

}
