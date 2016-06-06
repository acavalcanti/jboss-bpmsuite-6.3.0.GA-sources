package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

/**
 * Checks whether the provided distribution root (directory) satisfies the expected conditions.
 * <p/>
 * Usually used to check that the specified directory is correct distribution root for chosen distribution type.
 */
public interface DistributionChecker {

    /**
     * Decides if the provided directory is valid distribution root (according to conditions tied to specific checker
     * implementation).
     *
     * @param distributionRoot directory with the distribution contents
     * @return true if the specified root satisfies the conditions, otherwise false
     */
    public boolean check(File distributionRoot);

}
