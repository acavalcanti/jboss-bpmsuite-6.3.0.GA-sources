package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class DistributionTypeTest extends BaseClientPatcherTest {

    @Test
    public void shouldReturnCorrectListOfBRMSDistributionTypes() {
        List<DistributionType> actualTypes = DistributionType.getBRMSDistributionTypes();
        List<DistributionType> expectedTypes = Lists.newArrayList(
                DistributionType.EAP6X_BUNDLE,
                DistributionType.EAP6X_BC,
                DistributionType.EAP6X_KIE_SERVER,
                DistributionType.GENERIC_BUNDLE,
                DistributionType.GENERIC_BC,
                DistributionType.GENERIC_KIE_SERVER,
                DistributionType.WAS8_BUNDLE,
                DistributionType.WAS8_BC,
                DistributionType.WAS8_KIE_SERVER,
                DistributionType.WLS12C_BUNDLE,
                DistributionType.WLS12C_BC,
                DistributionType.WLS12C_KIE_SERVER,
                DistributionType.BRMS_ENGINE,
                DistributionType.PLANNER_ENGINE,
                DistributionType.SUPPLEMENTARY_TOOLS
        );
        assertEqualsIgnoreOrder("List of BRMS distribution types differs from the expected!", expectedTypes, actualTypes);
    }

    @Test
    public void shouldReturnCorrectListOfBPMSuiteDistributionTypes() {
        List<DistributionType> actualTypes = DistributionType.getBPMSuiteDistributionTypes();
        List<DistributionType> expectedTypes = Lists.newArrayList(
                DistributionType.EAP6X_BUNDLE,
                DistributionType.EAP6X_BC,
                DistributionType.EAP6X_DASHBUILDER,
                DistributionType.EAP6X_KIE_SERVER,
                DistributionType.GENERIC_BUNDLE,
                DistributionType.GENERIC_BC,
                DistributionType.GENERIC_DASHBUILDER,
                DistributionType.GENERIC_KIE_SERVER,
                DistributionType.WAS8_BUNDLE,
                DistributionType.WAS8_BC,
                DistributionType.WAS8_DASHBUILDER,
                DistributionType.WAS8_KIE_SERVER,
                DistributionType.WLS12C_BUNDLE,
                DistributionType.WLS12C_BC,
                DistributionType.WLS12C_DASHBUILDER,
                DistributionType.WLS12C_KIE_SERVER,
                DistributionType.BPMSUITE_ENGINE,
                DistributionType.PLANNER_ENGINE,
                DistributionType.SUPPLEMENTARY_TOOLS
        );
        assertEqualsIgnoreOrder("List of BPM Suite distribution types differs from the expected!", expectedTypes, actualTypes);
    }
}
