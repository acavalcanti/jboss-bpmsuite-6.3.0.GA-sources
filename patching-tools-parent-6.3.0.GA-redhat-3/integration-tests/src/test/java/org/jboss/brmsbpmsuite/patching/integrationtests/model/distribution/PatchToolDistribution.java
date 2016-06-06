package org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution;

import java.io.File;

import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;
import org.jboss.brmsbpmsuite.patching.integrationtests.util.FileUtil;

/**
 * Class representing distribution that is within patch tool
 * (Distribution containing files that patch testing distribution).
 */
public class PatchToolDistribution extends Distribution {

    public PatchToolDistribution(final File directory, final DistributionType type) {
        super(directory, type);
    }

    @Override
    public File getContentRootDirectory() {
        final String updatesDirName = System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_UPDATES_DIR);
        final File updatesDirectory =
                FileUtil.findFirstOccurenceOfDirectory(getBaseDirectory(), updatesDirName, true);
        if (updatesDirectory == null) {
            throw new IllegalStateException("Directory with name " + updatesDirName + " doesn't exist!");
        }
        return new File(updatesDirectory.getPath() + File.separator
                + getType().getRelativePath(RelativePathType.PATH_WITHIN_PATCH_TOOL, null));
    }
}
