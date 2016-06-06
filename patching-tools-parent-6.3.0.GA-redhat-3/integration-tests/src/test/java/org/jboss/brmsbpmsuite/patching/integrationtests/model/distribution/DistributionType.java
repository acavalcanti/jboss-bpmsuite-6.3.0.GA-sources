package org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution;

import java.io.File;

import org.jboss.brmsbpmsuite.patching.integrationtests.util.Constants;

/**
 * Contains all supported distribution types that can be patched.
 */
public enum DistributionType {
    EAP6X_BUNDLE("eap6.x", "", "eap6.x" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR, "eap6.x"),
    EAP6X_BC("eap6.x-bc",
            "standalone" + File.separator + "deployments" + File.separator + "business-central.war" + File.separator,
            "eap6.x" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "standalone" + File.separator
                    + "deployments" + File.separator
                    + "business-central.war" + File.separator,
            "eap6.x"),
    EAP6X_DASHBUILDER("eap6.x-dashbuilder",
            "standalone" + File.separator + "deployments" + File.separator + "dashbuilder.war" + File.separator,
            "eap6.x" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "standalone" + File.separator
                    + "deployments" + File.separator
                    + "dashbuilder.war" + File.separator,
            "eap6.x"),
    EAP6X_KIE_SERVER("eap6.x-kie-server",
            "standalone" + File.separator + "deployments" + File.separator + "kie-server.war" + File.separator,
            "eap6.x" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "standalone" + File.separator
                    + "deployments" + File.separator
                    + "kie-server.war" + File.separator,
            "eap6.x"),

    GENERIC_BUNDLE("generic", "", "generic" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR, "generic"),
    GENERIC_BC("generic-bc",
            "business-central.war" + File.separator,
            "generic" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "business-central.war" + File.separator,
            "generic"),
    GENERIC_DASHBUILDER("generic-dashbuilder",
            "dashbuilder.war" + File.separator,
            "generic" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "dashbuilder.war" + File.separator,
            "generic"),
    GENERIC_KIE_SERVER("generic-kie-server",
            "kie-server.war" + File.separator,
            "generic" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "kie-server.war" + File.separator,
            "generic"),

    WAS8_BUNDLE("was8", "", "was8" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR, "was8"),
    WAS8_BC("was8-bc",
            "business-central.war" + File.separator,
            "was8" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "business-central.war" + File.separator,
            "was8"),
    WAS8_DASHBUILDER("was8-dashbuilder",
            "dashbuilder.war" + File.separator,
            "was8" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "dashbuilder.war" + File.separator,
            "was8"),
    WAS8_KIE_SERVER("was8-kie-server",
            "kie-server.war" + File.separator,
            "was8" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "kie-server.war" + File.separator,
            "was8"),

    WLS12C_BUNDLE("wls12c", "", "wls12c" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR, "wls12c"),
    WLS12C_BC("wls12c-bc",
            "business-central.war" + File.separator,
            "wls12c" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "business-central.war" + File.separator,
            "wls12c"),
    WLS12C_DASHBUILDER("wls12c-dashbuilder",
            "dashbuilder.war" + File.separator,
            "wls12c" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "dashbuilder.war" + File.separator,
            "wls12c"),
    WLS12C_KIE_SERVER("wls12c-kie-server",
            "kie-server.war" + File.separator,
            "wls12c" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR + File.separator
                    + "kie-server.war" + File.separator,
            "wls12c"),

    BRMS_ENGINE("brms-engine", "", "brms-engine" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR, "brms-engine"),
    BPMSUITE_ENGINE("bpmsuite-engine", "", "bpmsuite-engine" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR,
            "bpmsuite-engine"),
    PLANNER_ENGINE("planner-engine", "", "planner-engine" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR,
            "planner-engine"),
    SUPPLEMENTARY_TOOLS("supplementary-tools", "",
            "supplementary-tools" + File.separator + Constants.PATCH_TOOL_NEW_CONTENT_DIR, "supplementary-tools");

    /**
     * Name of distribution type. Passed to patch tool as a distribution type argument.
     */
    private final String name;

    /**
     * Relative path to patched directory within test distribution.
     * Added to testing distribution path and passed as an argument to patch tool.
     */
    private final String relativePath;

    /**
     * Relative path to distribution files directory within patch tool.
     */
    private final String relativePathPatchTool;

    /**
     * Relative path to root directory for distribution type files.
     */
    private final String relativeRootDirectoryPathPatchTool;

    DistributionType(final String name, final String relativePath, final String relativePathPatchTool,
            final String relativeRootDirectoryPathPatchTool) {
        this.name = name;
        this.relativePath = relativePath;
        this.relativePathPatchTool = relativePathPatchTool;
        this.relativeRootDirectoryPathPatchTool = relativeRootDirectoryPathPatchTool;
    }

    public String getName() {
        return name;
    }

    public String getRelativeRootDirectoryPathPatchTool() {
        return relativeRootDirectoryPathPatchTool;
    }

    /**
     * Gets appropriate relative path by defined relative path type. There are few relative path types.
     * They bound different relative paths to different patching process entities.
     * I.e. distribution files are located on different paths within patch tool and within testing distribution.
     * @param relativePathType Type of relative path that is returned.
     * @param additionalPrefixPath Additional custom path that is added as prefix path to returned relative path.
     * @return Relative path for this distribution type instance.
     */
    public String getRelativePath(final RelativePathType relativePathType, final String additionalPrefixPath) {
        switch (relativePathType) {
            case PATH_WITHIN_DISTRIBUTION:
                // Some distributions can have special content root dir, that is specific to them and cannot be
                // generalized, so it must be passed as a build parameter and then added to path.
                if (additionalPrefixPath == null || additionalPrefixPath.isEmpty()) {
                    return relativePath;
                } else {
                    return additionalPrefixPath + File.separator + relativePath;
                }
            case PATH_WITHIN_PATCH_TOOL:
                if (additionalPrefixPath == null || additionalPrefixPath.isEmpty()) {
                    return relativePathPatchTool;
                } else {
                    return additionalPrefixPath + File.separator + relativePathPatchTool;
                }
            default:
                throw new IllegalArgumentException("Relative path type " + relativePathType + " not supported!");
        }
    }

    public static DistributionType getDistributionTypeByName(final String name) {
        for (DistributionType type : DistributionType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unrecognized distribution type '" + name + "'! ");
    }
}
