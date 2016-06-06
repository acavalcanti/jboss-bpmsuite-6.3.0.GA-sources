package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

/**
 * Checks whether the provided distribution root is a valid single war (business-central, kie-server or dashbuilder)
 */
public class WAS8SingleWarDistributionChecker implements DistributionChecker {

    private final String expectedWarFileName;

    public WAS8SingleWarDistributionChecker(String expectedWarFileName) {
        this.expectedWarFileName = expectedWarFileName;
    }

    @Override
    public boolean check(File distributionRoot) {
        return distributionRoot.isFile() && distributionRoot.getName().equals(expectedWarFileName);
    }

}
