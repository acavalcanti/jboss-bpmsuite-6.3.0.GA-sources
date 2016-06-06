package org.jboss.brmsbpmsuite.patching.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for patching nested directories inside the specific bundles.
 * <p/>
 * Examples:
 * - patching just single WAR, but having the diff for entire bundle.
 * - patching just helix (or zookeeper), but having the diff for entire suppl. tool bundle
 */
public class NestedDirInBundlePatcher implements Patcher {
    private static final Logger logger = LoggerFactory.getLogger(NestedDirInBundlePatcher.class);

    private final GeneralDirPatcher dirPatcher;

    public NestedDirInBundlePatcher(File distributionRoot, DistributionChecker distributionChecker, String prefixPath,
                                    List<String> removeList, List<PatchEntry> patchEntries, List<String> blackList,
                                    Map<String, List<Checksum>> checksums) {
        // general dir patcher is used after adapting the paths (removing the prefix)
        this.dirPatcher = new GeneralDirPatcher(
                distributionRoot,
                distributionChecker,
                adaptPaths(removeList, prefixPath),
                filterAndAdaptPatchEntries(patchEntries, prefixPath),
                adaptPaths(blackList, prefixPath),
                adaptChecksums(checksums, prefixPath)
        );
    }

    @Override
    public void checkDistro() {
        dirPatcher.checkDistro();
    }

    @Override
    public void backup(File backupBasedir) throws IOException {
        dirPatcher.backup(backupBasedir);
    }

    @Override
    public void apply() throws IOException {
        dirPatcher.apply();
    }

    @Override
    public void verify() {
        dirPatcher.verify();
    }

    @Override
    public void cleanUp() {
        dirPatcher.cleanUp();
    }

    public Map<String, List<Checksum>> getChecksums() {
        return dirPatcher.getChecksums();
    }

    private List<String> adaptPaths(List<String> paths, String prefixToRemove) {
        List<String> result = new ArrayList<String>();
        for (String path : paths) {
            if (path.startsWith(prefixToRemove)) {
                logger.trace("Found path '{}' with desired prefix '{}', removing the prefix.", path, prefixToRemove);
                result.add(path.substring(prefixToRemove.length()));
            } else {
                // entries without the prefix should be included as is
                result.add(path);
            }
        }
        return result;
    }

    private List<PatchEntry> filterAndAdaptPatchEntries(List<PatchEntry> patchEntries, String prefixToRemove) {
        List<PatchEntry> result = new ArrayList<PatchEntry>();
        for (PatchEntry patchEntry : patchEntries) {
            String path = patchEntry.getRelativePath();
            if (path.startsWith(prefixToRemove)) {
                logger.trace("Found patch entry with relative path '{}'. Prefix '{}' will be removed.", path, prefixToRemove);
                result.add(new PatchEntry(path.substring(prefixToRemove.length()), patchEntry.getActualFile()));
            }
        }
        return result;
    }

    private Map<String, List<Checksum>> adaptChecksums(Map<String, List<Checksum>> checksums, String prefixToRemove) {
        Map<String, List<Checksum>> adaptedChecksums = new HashMap<String, List<Checksum>>();
        for (Map.Entry<String, List<Checksum>> entry : checksums.entrySet()) {
            String path = entry.getKey();
            if (path.startsWith(prefixToRemove)) {
                adaptedChecksums.put(path.substring(prefixToRemove.length()), entry.getValue());
            } else {
                adaptedChecksums.put(path, entry.getValue());
            }
        }
        return adaptedChecksums;
    }

}
