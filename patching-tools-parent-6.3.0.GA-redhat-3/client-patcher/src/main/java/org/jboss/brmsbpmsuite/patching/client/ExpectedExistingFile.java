package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

public class ExpectedExistingFile implements ExpectedDistributionEntry {

    private final String relativePath;

    public ExpectedExistingFile(String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public boolean isPresent(File rootDir) {
        File expectedFile = new File(rootDir, relativePath);
        return expectedFile.exists() && expectedFile.isFile();
    }

    @Override
    public String getPath() {
        return relativePath;
    }

    @Override
    public ExpectedDistributionEntry withPath(String newRelativePath) {
        return new ExpectedExistingFile(newRelativePath);
    }

    @Override
    public String toString() {
        return "ExpectedExistingFile{" +
                "relativePath='" + relativePath + '\'' +
                '}';
    }
}
