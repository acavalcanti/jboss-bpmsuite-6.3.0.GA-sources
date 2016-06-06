package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

/**
 * Distribution checker that fails (throws exception) when called. Useful on places where some instance of the checker is
 * needed, but it should never be called.
 */
public class FailWhenCalledDistributionChecker implements DistributionChecker {

    public static final DistributionChecker INSTANCE = new FailWhenCalledDistributionChecker();

    @Override
    public boolean check(File distributionRoot) {
        throw new IllegalStateException("The FailWhenCalledDistributionChecker should never be called!");
    }

}
