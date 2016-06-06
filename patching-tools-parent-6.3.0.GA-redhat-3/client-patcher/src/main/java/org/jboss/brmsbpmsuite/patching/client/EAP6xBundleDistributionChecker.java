package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class EAP6xBundleDistributionChecker implements DistributionChecker {

    private static final Logger logger = LoggerFactory.getLogger(EAP6xBundleDistributionChecker.class);

    private final List<ExpectedDistributionEntry> eapHomeExpectedEntries;

    public EAP6xBundleDistributionChecker(List<ExpectedDistributionEntry> eapHomeExpectedEntries) {
        this.eapHomeExpectedEntries = eapHomeExpectedEntries;
    }

    @Override
    public boolean check(File distributionRoot) {
        ExpectedContentDistributionChecker eapHomeChecker = new ExpectedContentDistributionChecker(eapHomeExpectedEntries);
        logger.debug("Checking if the provided distribution root {} starts at EAP_HOME dir.", distributionRoot);
        if (eapHomeChecker.check(distributionRoot)) {
            logger.info("Directory {} is valid distribution root.", distributionRoot);
            return true;
        }
        // failed for the EAP_HOME case, try the deployments dir
        List<ExpectedDistributionEntry> deployDirExpectedEntries = removePathPrefix(eapHomeExpectedEntries,
                "standalone/deployments");
        ExpectedContentDistributionChecker deployDirChecker = new ExpectedContentDistributionChecker(deployDirExpectedEntries);
        logger.debug("Checking if the provided distribution root {} starts at deployment dir dir.", distributionRoot);
        if (deployDirChecker.check(distributionRoot)) {
            logger.info("Directory {} is valid distribution root.", distributionRoot);
            return true;
        }
        logger.error("Invalid distribution root {}! It does not point to the EAP 6.x bundle!", distributionRoot);
        return false;
    }

    private List<ExpectedDistributionEntry> removePathPrefix(List<ExpectedDistributionEntry> expectedEntries,
            final String pathPrefix) {
        final int pathPrefixLen = pathPrefix.length();
        return Lists.transform(expectedEntries, new Function<ExpectedDistributionEntry, ExpectedDistributionEntry>() {
            @Override
            public ExpectedDistributionEntry apply(ExpectedDistributionEntry entry) {
                String withoutPrefix = entry.getPath().substring(pathPrefixLen);
                return entry.withPath(withoutPrefix);
            }
        });
    }

}
