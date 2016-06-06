package org.jboss.brmsbpmsuite.patching.client;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class AbstractPatcher implements Patcher {
    private static final Logger logger = LoggerFactory.getLogger(AbstractPatcher.class);

    protected final File distributionRoot;
    protected final DistributionChecker distributionChecker;
    protected final List<String> removeList;
    protected final List<PatchEntry> patchEntries;
    protected final List<String> blackList;
    /** File (path) -> checksums mapping (there can be multiple checksums for one path -- coming from different versions) **/
    protected final Map<String, List<Checksum>> checksums;

    public AbstractPatcher(File distributionRoot, DistributionChecker distributionChecker, List<String> removeList,
            List<PatchEntry> patchEntries, List<String> blackList, Map<String, List<Checksum>> checksums) {
        this.distributionRoot = distributionRoot;
        this.distributionChecker = distributionChecker;
        this.removeList = removeList;
        this.patchEntries = patchEntries;
        this.blackList = blackList;
        this.checksums = checksums;
    }

    public File getDistributionRoot() {
        return distributionRoot;
    }

    public DistributionChecker getDistributionChecker() {
        return distributionChecker;
    }

    public List<String> getRemoveList() {
        return removeList;
    }

    public List<PatchEntry> getPatchEntries() {
        return patchEntries;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public Map<String, List<Checksum>> getChecksums() {
        return checksums;
    }

    @Override
    public void checkDistro() {
        // fail fast if the distribution root or patch root does not exist
        if (!distributionRoot.exists()) {
            throw new InvalidDistributionRootException("Distribution root " + distributionRoot + " does not exist!");
        }
        if (!distributionChecker.check(distributionRoot)) {
            throw new InvalidDistributionRootException("Distribution root " + distributionRoot + " is not valid " +
                    "for the specified distribution type! Please double-check that (1) the specified directory or file " +
                    "exists and (2) it matches the specified distribution type.");

        }
    }

    @Override
    public void backup(File backupDir) throws IOException {
        // backup the whole distribution
        // ordinary war file is valid distribution root in some cases (e.g. individual WAS8 wars)
        if (distributionRoot.isFile()) {
            logger.info("Backing-up (coping) file {} to {}", distributionRoot, backupDir);
            FileUtils.copyFileToDirectory(distributionRoot, backupDir);
        } else {
            logger.info("Backing-up (copying) contents of directory {} to {}.", distributionRoot, backupDir);
            FileUtils.copyDirectory(distributionRoot, backupDir);
        }
    }

    @Override
    public void verify() {

    }

    @Override
    public void cleanUp() {

    }

}
