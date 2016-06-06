package org.jboss.brmsbpmsuite.patching.client;

public class UnsupportedDistributionTypeException extends ClientPatcherException {
    public UnsupportedDistributionTypeException() {
    }

    public UnsupportedDistributionTypeException(String message) {
        super(message);
    }

    public UnsupportedDistributionTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedDistributionTypeException(Throwable cause) {
        super(cause);
    }

}
