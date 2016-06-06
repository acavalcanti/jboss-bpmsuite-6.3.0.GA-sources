package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

public class ExpectedExistingDir implements ExpectedDistributionEntry {

    private final String relativePath;

    public ExpectedExistingDir(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public boolean isPresent(File rootDir) {
        File expectedDir = new File(rootDir, relativePath);
        return expectedDir.exists() && expectedDir.isDirectory();
    }

    @Override
    public String getPath() {
        return relativePath;
    }

    @Override
    public ExpectedDistributionEntry withPath(String newRelativePath) {
        return new ExpectedExistingDir(newRelativePath);
    }

    @Override
    public String toString() {
        return "ExpectedExistingDir{" +
                "relativePath='" + relativePath + '\'' +
                '}';
    }
}
