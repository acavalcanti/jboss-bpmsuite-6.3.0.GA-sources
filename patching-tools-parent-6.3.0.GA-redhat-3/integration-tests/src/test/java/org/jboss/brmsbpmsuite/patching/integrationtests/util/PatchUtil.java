package org.jboss.brmsbpmsuite.patching.integrationtests.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.jboss.brmsbpmsuite.patching.integrationtests.model.distribution.Distribution;

/**
 * Contains util methods for patching.
 */
public final class PatchUtil {

    /**
     * Applies patch on distribution.
     * @param distribution Distribution on which is applied patch by this method.
     * @return Exit code of patch tool script.
     */
    public static int applyPatch(final Distribution distribution)
            throws IOException, InterruptedException {
        return executeApplyPatchScript(distribution.getContentRootDirectory(), distribution.getType().getName(),
                SystemUtils.IS_OS_WINDOWS);
    }

    /**
     * Finds and executes main patch tool script ("executes patch tool").
     * @param testDistDir Directory containing testing distribution. This distribution is patched by patch tool.
     * @param testDistType Name of testing distribution type.
     * @param forOSWindows Defines if the script executed should be script for OS Windows.
     * @return Exit code of patch tool script.
     */
    private static int executeApplyPatchScript(final File testDistDir, final String testDistType,
            final boolean forOSWindows) throws IOException, InterruptedException {
        final File applyPatchScript = getApplyPatchScript(
                WorkspaceUtil.getPatchToolTestingDistributionDirFile(), forOSWindows);

        final ProcessBuilder pb;
        if (forOSWindows) {
            pb = new ProcessBuilder("cmd.exe", "/C", applyPatchScript.getPath(), testDistDir.getPath(), testDistType);
        } else {
            pb = new ProcessBuilder("sh", applyPatchScript.getPath(), testDistDir.getPath(), testDistType);
        }
        final File applyPatchScriptParentFile = applyPatchScript.getParentFile();
        if (applyPatchScriptParentFile != null) {
            pb.directory(applyPatchScriptParentFile);
        }
        Process p = pb.start();

        final StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
        final StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");

        // start gobblers
        outputGobbler.start();
        errorGobbler.start();
        return p.waitFor();
    }

    /**
     * Finds main patch tool script file within a directory.
     * @param directory Directory which is searched for main patch tool script.
     * @param forOSWindows Defines if the script returned should be script for OS Windows.
     * @return File instance representing main patch tool script. If the script is not found, or too many scripts are
     * found, method raises exception.
     */
    private static File getApplyPatchScript(final File directory, final boolean forOSWindows) {
        return FileUtil.findFile(directory, getApplyPatchScriptFileName(forOSWindows), null);
    }

    /**
     * Gets patch tool main script name from environmental variable.
     * @param forOSWindows Defines if the script name returned should be name of script for OS Windows.
     * @return If isOSWindow argument is true, returns name of batch script, else returns name of shell script.
     */
    private static String getApplyPatchScriptFileName(final boolean forOSWindows) {
        if (forOSWindows) {
            return System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_SCRIPT_APPLYPATCH_BATCH);
        } else {
            return System.getProperty(Constants.PROPERTY_KEY_PATCHTOOL_SCRIPT_APPLYPATCH_SHELL);
        }
    }

    private PatchUtil() {
        // It is prohibited to instantiate util classes.
    }
}
