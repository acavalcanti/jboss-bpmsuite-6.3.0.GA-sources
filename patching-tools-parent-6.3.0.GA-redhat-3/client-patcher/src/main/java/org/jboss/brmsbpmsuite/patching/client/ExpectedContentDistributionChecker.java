package org.jboss.brmsbpmsuite.patching.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Checks whether the provided distribution root contains the expected content.
 */
public class ExpectedContentDistributionChecker implements DistributionChecker {
    private static final Logger logger = LoggerFactory.getLogger(ExpectedContentDistributionChecker.class);

    private final List<ExpectedDistributionEntry> expectedEntries;

    public ExpectedContentDistributionChecker(List<ExpectedDistributionEntry> expectedEntries) {
        this.expectedEntries = expectedEntries;
    }

    public List<ExpectedDistributionEntry> getExpectedEntries() {
        return expectedEntries;
    }

    @Override
    public boolean check(File distributionRoot) {
        logger.trace("Checking expected entries inside provided distribution root {}", distributionRoot.getAbsolutePath());
        for (ExpectedDistributionEntry expectedEntry : expectedEntries) {
            if (expectedEntry.isPresent(distributionRoot)) {
                logger.trace("Expected entry {} found.", expectedEntry);
            } else {
                // log just debug message as the missing entry may be expected by the caller. Caller needs to ultimately decide
                // if this is indeed an error or not
                logger.debug("Expected entry {} not found!", expectedEntry);
                return false;
            }
        }
        return true;
    }

}
