package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WAS8SingleWarPatcher extends AbstractPatcher {

    private final NestedDirInBundlePatcher nestedDirPatcher;

    public WAS8SingleWarPatcher(String warName, File distributionRoot, List<String> removeList, List<PatchEntry> patchEntries,
                                List<String> updateBlackList, Map<String, List<Checksum>> checksums) {
        super(distributionRoot, new WAS8SingleWarDistributionChecker(warName), removeList, patchEntries, updateBlackList, checksums);
        this.nestedDirPatcher = new NestedDirInBundlePatcher(distributionRoot, FailWhenCalledDistributionChecker.INSTANCE,
                warName + "/", removeList, patchEntries, updateBlackList, checksums);
    }

    @Override
    public void apply() throws IOException {
        PatchingUtils.unzipWarFileIntoDirWithSameName(distributionRoot);
        nestedDirPatcher.apply();
        PatchingUtils.zipExploadedWar(distributionRoot);
    }

}
