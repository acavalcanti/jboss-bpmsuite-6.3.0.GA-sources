package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ValidRootsDistributionCheckerFactoryTest extends BaseClientPatcherTest {

    private static File validRootsBasedir = getCPResourceAsFile("/distribution-checker-test/valid-roots");

    @Parameterized.Parameters(name = "{index}: {0}-{1}, root={2}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                        // bundles
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BRMS, "eap6.x-brms-1"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BRMS, "eap6.x-brms-2"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-1"},
                        {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-2"},
                        {DistributionType.GENERIC_BUNDLE, TargetProduct.BRMS, "generic-brms"},
                        {DistributionType.GENERIC_BUNDLE, TargetProduct.BPMSUITE, "generic-bpmsuite"},
                        {DistributionType.WAS8_BUNDLE, TargetProduct.BRMS, "was8-brms"},
                        {DistributionType.WAS8_BUNDLE, TargetProduct.BPMSUITE, "was8-bpmsuite"},
                        {DistributionType.WLS12C_BUNDLE, TargetProduct.BRMS, "wls12c-brms"},
                        {DistributionType.WLS12C_BUNDLE, TargetProduct.BPMSUITE, "wls12c-bpmsuite"},
                        // individual WARs
                        {DistributionType.EAP6X_BC, TargetProduct.BRMS, "eap6.x-brms-2/business-central.war"},
                        {DistributionType.EAP6X_BC, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-2/business-central.war"},
                        {DistributionType.EAP6X_KIE_SERVER, TargetProduct.BRMS, "eap6.x-brms-2/kie-server.war"},
                        {DistributionType.EAP6X_KIE_SERVER, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-2/kie-server.war"},
                        {DistributionType.EAP6X_DASHBUILDER, TargetProduct.BPMSUITE, "eap6.x-bpmsuite-2/dashbuilder.war"},
                        {DistributionType.GENERIC_BC, TargetProduct.BRMS, "generic-brms/business-central.war"},
                        {DistributionType.GENERIC_BC, TargetProduct.BPMSUITE, "generic-bpmsuite/business-central.war"},
                        {DistributionType.GENERIC_KIE_SERVER, TargetProduct.BRMS, "generic-brms/kie-server.war"},
                        {DistributionType.GENERIC_KIE_SERVER, TargetProduct.BPMSUITE, "generic-bpmsuite/kie-server.war"},
                        {DistributionType.GENERIC_DASHBUILDER, TargetProduct.BPMSUITE, "generic-bpmsuite/dashbuilder.war"},
                        {DistributionType.WAS8_BC, TargetProduct.BPMSUITE, "was8-bpmsuite/business-central.war"},
                        {DistributionType.WAS8_BC, TargetProduct.BRMS, "was8-brms/business-central.war"},
                        {DistributionType.WAS8_KIE_SERVER, TargetProduct.BRMS, "was8-brms/kie-server.war"},
                        {DistributionType.WAS8_KIE_SERVER, TargetProduct.BPMSUITE, "was8-bpmsuite/kie-server.war"},
                        {DistributionType.WAS8_DASHBUILDER, TargetProduct.BPMSUITE, "was8-bpmsuite/dashbuilder.war"},
                        {DistributionType.WLS12C_BC, TargetProduct.BPMSUITE, "wls12c-bpmsuite/business-central.war"},
                        {DistributionType.WLS12C_BC, TargetProduct.BRMS, "wls12c-brms/business-central.war"},
                        {DistributionType.WLS12C_KIE_SERVER, TargetProduct.BRMS, "wls12c-brms/kie-server.war"},
                        {DistributionType.WLS12C_KIE_SERVER, TargetProduct.BPMSUITE, "wls12c-bpmsuite/kie-server.war"},
                        {DistributionType.WLS12C_DASHBUILDER, TargetProduct.BPMSUITE, "wls12c-bpmsuite/dashbuilder.war"},
                        // rest of the distributions
                        {DistributionType.BRMS_ENGINE, TargetProduct.BRMS, "brms-engine"},
                        {DistributionType.BPMSUITE_ENGINE, TargetProduct.BPMSUITE, "bpmsuite-engine"},
                        {DistributionType.PLANNER_ENGINE, TargetProduct.BRMS_AND_BPMSUITE, "planner-engine"},
                        {DistributionType.SUPPLEMENTARY_TOOLS, TargetProduct.BRMS_AND_BPMSUITE, "supplementary-tools"}
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
    public void shouldRecognizeValidDistributionRoot() {
        DistributionChecker checker = DistributionCheckerFactory.create(distributionType, targetProduct);
        Assert.assertTrue("Distribution root should be marked as valid!",
                checker.check(new File(validRootsBasedir, distributionRootPath)));
    }

}
