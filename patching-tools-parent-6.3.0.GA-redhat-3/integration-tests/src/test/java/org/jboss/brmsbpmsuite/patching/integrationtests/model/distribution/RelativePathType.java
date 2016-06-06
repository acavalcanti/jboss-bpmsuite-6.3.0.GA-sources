package org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution;

public enum RelativePathType {

    /**
     * Represents path within patch tool. It can be path to new distribution files, etc.
     */
    PATH_WITHIN_PATCH_TOOL,

    /**
     * Represents path within distribution. May be path within starting distribution, testing distribution, etc.
     */
    PATH_WITHIN_DISTRIBUTION
}
