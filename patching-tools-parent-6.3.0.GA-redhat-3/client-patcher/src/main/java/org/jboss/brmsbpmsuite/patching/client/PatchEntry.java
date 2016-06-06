package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

/**
 * Represents single patched/added entry. It contains the relative path of the file/directory inside the distribution
 * and also absolute path of the actual updated/new file/directory.
 * 
 * This class is intended to be immutable.
 */
public class PatchEntry {
    private final String relativePath;
    private final File actualFile;

    public PatchEntry(String relativePath, File actualFile) {
        this.relativePath = relativePath;
        this.actualFile = actualFile;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public File getActualFile() {
        return actualFile;
    }

    @Override
    public String toString() {
        return "PatchEntry{" +
                "relativePath='" + relativePath + '\'' +
                ", actualFile=" + actualFile +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatchEntry that = (PatchEntry) o;

        if (actualFile != null ? !actualFile.equals(that.actualFile) : that.actualFile != null) return false;
        if (relativePath != null ? !relativePath.equals(that.relativePath) : that.relativePath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = relativePath != null ? relativePath.hashCode() : 0;
        result = 31 * result + (actualFile != null ? actualFile.hashCode() : 0);
        return result;
    }
}
