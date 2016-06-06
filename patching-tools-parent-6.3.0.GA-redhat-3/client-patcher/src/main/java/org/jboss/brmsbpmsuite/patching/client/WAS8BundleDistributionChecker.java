package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * Checks whether the specified distribution root is valid WAS8 bundle.
 * <p/>
 * The WAS8 bundle contains zipped WAR files, instead of the exploaded ones.
 */
public class WAS8BundleDistributionChecker implements DistributionChecker {

    private final TargetProduct targetProduct;

    public WAS8BundleDistributionChecker(TargetProduct targetProduct) {
        this.targetProduct = targetProduct;
    }

    @Override
    public boolean check(File distributionRoot) {
        List<ExpectedDistributionEntry> expectedEntries = Lists.newArrayList();
        expectedEntries.add(new ExpectedExistingFile("business-central.war"));
        expectedEntries.add(new ExpectedExistingFile("kie-server.war"));
        if (TargetProduct.BPMSUITE.equals(targetProduct)) {
            expectedEntries.add(new ExpectedExistingFile("dashbuilder.war"));
        }
        ExpectedContentDistributionChecker checker = new ExpectedContentDistributionChecker(expectedEntries);
        return checker.check(distributionRoot);
    }

}
