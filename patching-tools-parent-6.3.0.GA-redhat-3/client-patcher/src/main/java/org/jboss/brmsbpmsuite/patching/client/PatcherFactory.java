package org.jboss.brmsbpmsuite.patching.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PatcherFactory {
    private static final Logger logger = LoggerFactory.getLogger(PatcherFactory.class);

    public static final String BLACKLIST_FILENAME = "blacklist.txt";
    public static final String REMOVE_LIST_FILENAME = "remove-list.txt";
    public static final String CHECKSUMS_FILENAME = "checksums.txt";
    public static final String NEW_CONTENT_DIRNAME = "new-content";

    public static Patcher newPatcher(DistributionType distributionType, TargetProduct product, File distributionRoot,
            File patchBasedir) {
        File distroUpdatesDir = new File(patchBasedir, "updates/" + distributionType.getRelativePath());
        File blacklist = new File(patchBasedir, BLACKLIST_FILENAME);

        DistributionChecker distroChecker = DistributionCheckerFactory.create(distributionType, product);

        switch (distributionType) {
            case EAP6X_BUNDLE:
                // if the standalone/deployments dir exists assume the distribution root is EAP_HOME
                if (new File(distributionRoot, "standalone/deployments").exists()) {
                    return new GeneralDirPatcher(
                            distributionRoot,
                            distroChecker,
                            readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                            createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                            getBlacklistedPaths(blacklist),
                            getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                    );
                } else {
                    return new NestedDirInBundlePatcher(
                            distributionRoot,
                            distroChecker,
                            "standalone/deployments/",
                            readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                            createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                            getBlacklistedPaths(blacklist),
                            getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                    );
                }

            case WAS8_BUNDLE:
                return new WAS8BundlePatcher(
                        product,
                        distributionRoot,
                        readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                        createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                        getBlacklistedPaths(blacklist),
                        getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))

                );

            case GENERIC_BUNDLE:
            case WLS12C_BUNDLE:
            case BRMS_ENGINE:
            case BPMSUITE_ENGINE:
            case PLANNER_ENGINE:
            case SUPPLEMENTARY_TOOLS:
                return new GeneralDirPatcher(
                        distributionRoot,
                        distroChecker,
                        readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                        createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                        getBlacklistedPaths(blacklist),
                        getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                );

            case EAP6X_BC:
            case EAP6X_DASHBUILDER:
            case EAP6X_KIE_SERVER:
            case GENERIC_BC:
            case GENERIC_DASHBUILDER:
            case GENERIC_KIE_SERVER:
            case WLS12C_BC:
            case WLS12C_DASHBUILDER:
            case WLS12C_KIE_SERVER:
                return new NestedDirInBundlePatcher(
                        distributionRoot,
                        distroChecker,
                        determinePathPrefix(distributionType),
                        readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                        createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                        getBlacklistedPaths(blacklist),
                        getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                );

            case WAS8_BC:
                return new WAS8SingleWarPatcher(
                        "business-central.war",
                        distributionRoot,
                        readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                        createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                        getBlacklistedPaths(blacklist),
                        getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                );

            case WAS8_DASHBUILDER:
                return new WAS8SingleWarPatcher(
                        "dashbuilder.war",
                        distributionRoot,
                        readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                        createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                        getBlacklistedPaths(blacklist),
                        getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                );

            case WAS8_KIE_SERVER:
                return new WAS8SingleWarPatcher(
                        "kie-server.war",
                        distributionRoot,
                        readLines(new File(distroUpdatesDir, REMOVE_LIST_FILENAME)),
                        createPatchEntries(new File(distroUpdatesDir, NEW_CONTENT_DIRNAME)),
                        getBlacklistedPaths(blacklist),
                        getChecksums(new File(distroUpdatesDir, CHECKSUMS_FILENAME))
                );

            default:
                throw new IllegalArgumentException("Unknown distribution type '" + distributionType + "'!");
        }
    }

    private static Map<String, List<Checksum>> getChecksums(File checksumsFile) {
        logger.debug("Parsing checkums file {}", checksumsFile.getAbsolutePath());
        Properties checksumProps;
        try {
            checksumProps = new Properties();
            checksumProps.load(new FileReader(checksumsFile));
        } catch (IOException e) {
            String msg = "Can not read checksums inside properties file " + checksumsFile.getAbsolutePath();
            logger.error(msg);
            throw new RuntimeException(msg, e);
        }
        Map<String, List<Checksum>> checksumsMap = new HashMap<String, List<Checksum>>();
        for (String path: checksumProps.stringPropertyNames()) {
            logger.trace("Parsing checksums for path '{}'", path);
            String checksumsStr = checksumProps.getProperty(path);
            if (checksumsStr == null || checksumsStr.equals("")) {
                throw new RuntimeException("No checksums provided for path " + path + " (and the path is specific in the file). "
                 + "Path can be specified only if there is at one checksum associated with it.");
            }
            checksumsMap.put(path, parseChecksums(checksumsStr));

        }
        return checksumsMap;
    }

    private static List<Checksum> parseChecksums(String checksumsStr) {
        List<Checksum> checksums = new ArrayList<Checksum>();
        String[] parts = checksumsStr.trim().split(",");
        for (String checksum : parts) {
            checksums.add(Checksum.md5(checksum));
        }
        return checksums;
    }

    private static List<String> getBlacklistedPaths(File blacklist) {
        if (blacklist.exists() && blacklist.isFile()) {
            logger.info("File blacklist.txt found at {}.", blacklist.getAbsolutePath());
            return readLines(blacklist);
        } else {
            logger.info("File blacklist.txt _not_ found. It was expected at {}.", blacklist.getAbsolutePath());
            return Collections.emptyList();
        }
    }

    private static List<String> readLines(File file) {
        List<String> allLines;
        try {
            allLines = FileUtils.readLines(file);
        } catch (IOException e) {
            String msg = "Can not read content of file " + file.getAbsolutePath();
            logger.error(msg, e);
            throw new RuntimeException(msg);
        }
        // remove empty lines and comments (lines starting with #)
        List<String> filtered = new ArrayList<String>();
        for (String line : allLines) {
            if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                continue;
            }
            filtered.add(line);
        }
        return filtered;
    }

    private static List<PatchEntry> createPatchEntries(File newContentDir) {
        List<PatchEntry> patchEntries = new ArrayList<PatchEntry>();
        Collection<File> files = FileUtils.listFiles(newContentDir, TrueFileFilter.INSTANCE, DirectoryFileFilter.INSTANCE);
        for (File file : files) {
            // strip the base dir name from absolute path
            int basedirPathLength = newContentDir.getAbsolutePath().length();
            String relPath = file.getAbsolutePath().substring(basedirPathLength + 1); // +1 for name separator
            // replace env. specific name separators with canonical ones for the client patcher
            String canonicalRelPath = relPath.replace(File.separatorChar, Patcher.CANONICAL_NAME_SEPARATOR_CHAR);
            patchEntries.add(new PatchEntry(canonicalRelPath, file));
        }
        logger.trace("Created patch entries:" + patchEntries);
        return patchEntries;
    }

    private static String determinePathPrefix(DistributionType distributionType) {
        switch (distributionType) {
            case EAP6X_BC:
                return "standalone/deployments/business-central.war/";

            case EAP6X_DASHBUILDER:
                return "standalone/deployments/dashbuilder.war/";

            case EAP6X_KIE_SERVER:
                return "standalone/deployments/kie-server.war/";

            case GENERIC_BC:
            case WAS8_BC:
            case WLS12C_BC:
                return "business-central.war/";

            case GENERIC_DASHBUILDER:
            case WAS8_DASHBUILDER:
            case WLS12C_DASHBUILDER:
                return "dashbuilder.war/";

            case GENERIC_KIE_SERVER:
            case WAS8_KIE_SERVER:
            case WLS12C_KIE_SERVER:
                return "kie-server.war/";

            default:
                throw new IllegalArgumentException(
                        "Can not determine path prefix (inside bundle) for distribution type " + distributionType);
        }
    }

}
