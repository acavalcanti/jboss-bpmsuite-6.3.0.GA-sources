package org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution;

/**
 * Exception representing error occurring in distributions domain.
 */
public class DistributionException extends Exception {

    public DistributionException() {
    }

    public DistributionException(final String message) {
        super(message);
    }

    public DistributionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DistributionException(final Throwable cause) {
        super(cause);
    }

}
