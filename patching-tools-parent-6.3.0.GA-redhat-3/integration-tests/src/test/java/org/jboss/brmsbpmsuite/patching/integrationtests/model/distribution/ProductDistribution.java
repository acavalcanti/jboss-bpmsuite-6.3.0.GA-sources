package org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution;

import java.io.File;

/**
 * Class representing product distribution.
 */
public class ProductDistribution extends Distribution {

    public ProductDistribution(final File directory, final DistributionType type) {
        super(directory, type);
    }

    @Override
    public File getContentRootDirectory() {
        final File bundleRootDirectory = getBundleRootDirectory();
        final String bundleRootDirName = bundleRootDirectory == null ? null : bundleRootDirectory.getName();
        return new File(getBaseDirectory().getPath() + File.separator + getType().getRelativePath(
                RelativePathType.PATH_WITHIN_DISTRIBUTION, bundleRootDirName));
    }
}
