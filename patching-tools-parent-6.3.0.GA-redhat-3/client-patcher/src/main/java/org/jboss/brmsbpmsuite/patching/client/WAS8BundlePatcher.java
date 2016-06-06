package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WAS8BundlePatcher extends AbstractPatcher {

    private final TargetProduct targetProduct;
    private final GeneralDirPatcher dirPatcher;

    public WAS8BundlePatcher(TargetProduct targetProduct, File distributionRoot, List<String> removeList,
                             List<PatchEntry> patchEntries, List<String> updateBlackList, Map<String, List<Checksum>> checksums) {
        super(distributionRoot, new WAS8BundleDistributionChecker(targetProduct), removeList, patchEntries, updateBlackList, checksums);
        this.targetProduct = targetProduct;
        // the general dir patcher needs DistributionChecker instance, but it should never be called. Pass specific
        // instance that will throw exception when called
        this.dirPatcher = new GeneralDirPatcher(distributionRoot, FailWhenCalledDistributionChecker.INSTANCE, removeList,
                patchEntries, updateBlackList, checksums);
    }

    @Override
    public void apply() throws IOException {
        List<File> warFiles = Lists.newArrayList(new File(distributionRoot, "business-central.war"),
                new File(distributionRoot, "kie-server.war"));
        if (TargetProduct.BPMSUITE == targetProduct) {
            warFiles.add(new File(distributionRoot, "dashbuilder.war"));
        }
        for (File warFile : warFiles) {
            PatchingUtils.unzipWarFileIntoDirWithSameName(warFile);
        }
        dirPatcher.apply();
        for (File warFile : warFiles) {
            PatchingUtils.zipExploadedWar(warFile);
        }
    }

}
