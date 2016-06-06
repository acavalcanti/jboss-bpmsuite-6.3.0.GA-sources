package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InvalidRootsDistributionCheckerFactoryTest extends BaseClientPatcherTest {

    private static File validRootsBasedir = getCPResourceAsFile("/distribution-checker-test/invalid-roots");

    @Parameterized.Parameters(name = "{index}: {0}-{1}, root={2}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                        // bundles
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BRMS, "eap6.x-brms-1"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BRMS, "eap6.x-brms-2"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-1"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-2"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-3"},
                        {DistributionType.GENERIC_BUNDLE, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WAS8_BUNDLE, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WLS12C_BUNDLE, TargetProduct.BPMSUITE, "unknown-content"},
                        // individual WARs
                        {DistributionType.EAP6X_BC, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.EAP6X_KIE_SERVER, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.EAP6X_BC, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.EAP6X_KIE_SERVER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.EAP6X_DASHBUILDER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.GENERIC_BC, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.GENERIC_KIE_SERVER, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.GENERIC_BC, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.GENERIC_KIE_SERVER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.GENERIC_DASHBUILDER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WAS8_BC, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.WAS8_KIE_SERVER, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.WAS8_BC, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WAS8_KIE_SERVER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WAS8_DASHBUILDER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WLS12C_BC, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.WLS12C_KIE_SERVER, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.WLS12C_BC, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WLS12C_KIE_SERVER, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.WLS12C_DASHBUILDER, TargetProduct.BPMSUITE, "unknown-content"},
                        // rest of the distributions
                        {DistributionType.BRMS_ENGINE, TargetProduct.BRMS, "unknown-content"},
                        {DistributionType.BPMSUITE_ENGINE, TargetProduct.BPMSUITE, "unknown-content"},
                        {DistributionType.PLANNER_ENGINE, TargetProduct.BRMS_AND_BPMSUITE, "unknown-content"},
                        {DistributionType.SUPPLEMENTARY_TOOLS, TargetProduct.BRMS_AND_BPMSUITE, "unknown-content"},
                }
        );
    }

    @Parameterized.Parameter(0)
    public DistributionType distributionType;

    @Parameterized.Parameter(1)
    public TargetProduct targetProduct;

    @Parameterized.Parameter(2)
    public String distributionRootPath;

    @Test
    public void shouldReportInValidDistributionRoot() {
        DistributionChecker checker = DistributionCheckerFactory.create(distributionType, targetProduct);
        Assert.assertFalse("Distribution root should be marked as invalid!",
                checker.check(new File(validRootsBasedir, distributionRootPath)));
    }

}
