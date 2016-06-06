package org.jboss.brmsbpmsuite.patching.client;

import java.io.File;

/**
 * Represents an entry (file, directory, file with specific content) that is expected to be in distribution.
 *
 * It moves the responsibility (decision) from caller to callee. The expected paths can involve for example regular expressions,
 * so there can be different implementations handling plain string values, regular expressions, etc. The {@code #isPresent}
 * will be called with the distribution root and will decide if the expected path is there or not.
 */
public interface ExpectedDistributionEntry {

    /**
     * Decides if entry represented by this object is available in the distribution or not.
     *
     * @param dir distribution root
     * @return true if the entry is in the provided distribution root, otherwise false
     */
    public boolean isPresent(File dir);

    /**
     * Returns relative path represented by this object.
     */
    public String getPath();

    /**
     * Creates a clone of this object, with the relative path set to the provided one
     *
     * @return _new_ object of the same type, but with the specified relative path set
     */
    public ExpectedDistributionEntry withPath(String newRelativePath);

}
