package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Collection;

@RunWith(Parameterized.class)
public class PatcherFactoryCorrectImplClassTest extends BaseClientPatcherTest {

    // we just need some existing dir for the purpose of this test
    private static final File distributionRoot = tmpDir;
    // dir that contains dummy patch contents to satisfy the PatcherFactory
    private static final File patchBaseDir = getCPResourceAsFile("/patcher-factory-test/patch-dir");

    @Parameterized.Parameters(name = "{index}: {0}-{1}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                // BRMS distribution types
                // will return NestedDirInBundlePatcher because it assumes the distro root is dir with the WARs and not the
                // EAP_HOME. This is special case for EAP distro
                {DistributionType.EAP6X_BUNDLE, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.EAP6X_BC, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.EAP6X_KIE_SERVER, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.GENERIC_BUNDLE, TargetProduct.BRMS, GeneralDirPatcher.class},
                {DistributionType.GENERIC_BC, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.GENERIC_KIE_SERVER, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.WLS12C_BUNDLE, TargetProduct.BRMS, GeneralDirPatcher.class},
                {DistributionType.WLS12C_BC, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.WLS12C_KIE_SERVER, TargetProduct.BRMS, NestedDirInBundlePatcher.class},
                {DistributionType.WAS8_BUNDLE, TargetProduct.BRMS, WAS8BundlePatcher.class},
                {DistributionType.WAS8_BC, TargetProduct.BRMS, WAS8SingleWarPatcher.class},
                {DistributionType.WAS8_KIE_SERVER, TargetProduct.BRMS, WAS8SingleWarPatcher.class},
                {DistributionType.BRMS_ENGINE, TargetProduct.BRMS, GeneralDirPatcher.class},
                {DistributionType.PLANNER_ENGINE, TargetProduct.BRMS, GeneralDirPatcher.class},
                {DistributionType.SUPPLEMENTARY_TOOLS, TargetProduct.BRMS, GeneralDirPatcher.class},
                // BPM Suite distribution types
                {DistributionType.EAP6X_BUNDLE, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.EAP6X_BC, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.EAP6X_KIE_SERVER, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.EAP6X_DASHBUILDER, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.GENERIC_BUNDLE, TargetProduct.BPMSUITE, GeneralDirPatcher.class},
                {DistributionType.GENERIC_BC, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.GENERIC_KIE_SERVER, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.GENERIC_DASHBUILDER, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.WLS12C_BUNDLE, TargetProduct.BPMSUITE, GeneralDirPatcher.class},
                {DistributionType.WLS12C_BC, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.WLS12C_KIE_SERVER, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.WLS12C_DASHBUILDER, TargetProduct.BPMSUITE, NestedDirInBundlePatcher.class},
                {DistributionType.WAS8_BUNDLE, TargetProduct.BPMSUITE, WAS8BundlePatcher.class},
                {DistributionType.WAS8_BC, TargetProduct.BPMSUITE, WAS8SingleWarPatcher.class},
                {DistributionType.WAS8_KIE_SERVER, TargetProduct.BPMSUITE, WAS8SingleWarPatcher.class},
                {DistributionType.WAS8_DASHBUILDER, TargetProduct.BPMSUITE, WAS8SingleWarPatcher.class},
                {DistributionType.BPMSUITE_ENGINE, TargetProduct.BPMSUITE, GeneralDirPatcher.class},
                {DistributionType.PLANNER_ENGINE, TargetProduct.BPMSUITE, GeneralDirPatcher.class},
                {DistributionType.SUPPLEMENTARY_TOOLS, TargetProduct.BPMSUITE, GeneralDirPatcher.class},

        });
    }

    @Parameterized.Parameter(0)
    public DistributionType distroType;

    @Parameterized.Parameter(1)
    public TargetProduct product;

    @Parameterized.Parameter(2)
    public Class<?> expectedImplClass;

    @Test
    public void shouldReturnCorrectImplClassForSpecifiedDistribution() {
        Patcher patcher = PatcherFactory.newPatcher(distroType, product, distributionRoot, patchBaseDir);
        Assert.assertTrue("Unexpected patcher impl class returned by PatcherFactory!",
                expectedImplClass.isAssignableFrom(patcher.getClass()));
    }

}
