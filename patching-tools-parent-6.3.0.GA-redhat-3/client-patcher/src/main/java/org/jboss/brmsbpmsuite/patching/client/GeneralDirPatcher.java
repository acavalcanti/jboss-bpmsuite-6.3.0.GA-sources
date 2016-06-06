package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Patches specified directory. The distribution root is the directory, so the relative paths start from inside that dir.
 */
public class GeneralDirPatcher extends AbstractPatcher {
    private static final Logger logger = LoggerFactory.getLogger(GeneralDirPatcher.class);

    public GeneralDirPatcher(File distributionRoot, DistributionChecker distributionChecker, List<String> removeList,
            List<PatchEntry> patchEntries, List<String> updateBlackList, Map<String, List<Checksum>> checksums) {
        super(distributionRoot, distributionChecker, removeList, patchEntries, updateBlackList, checksums);
    }

    @Override
    public void apply() throws IOException {
        removeOldFiles();
        applyUpdates();
    }

    private void removeOldFiles() throws IOException {
        logger.info("Removing old files from distribution root {}", distributionRoot.getAbsolutePath());
        // remove all files - beware of the black list
        List<String> pathsToRemove = findPathsToRemove(removeList);
        for (String relativePath : pathsToRemove) {
            // by default mark the file for removal, unless decided otherwise below
            boolean shouldRemove = true;
            File entryToRemove = new File(distributionRoot, relativePath);
            if (blackList.contains(relativePath)) {
                shouldRemove = shouldRemoveBlacklistedFile(entryToRemove, relativePath);
            }
            if (shouldRemove) {
                removeEntryIfExists(entryToRemove);
            }
        }
    }

    /**
     * Checks whether specified blacklisted file should be removed.
     *
     * Blacklisted file can be removed only if:
     *  - it no longer exists in the new distribution
     *  - AND it does not contain any user changes
     *
     * @return true if the specified blacklisted file should be removed
     */
    private boolean shouldRemoveBlacklistedFile(File blacklistedFile, String relativePath) throws IOException {
        // blacklisted files should not usually be removed, there is just one exception:
        //  - the file was removed in the newer version and the file in current distribution does not contain any custom changes
        boolean shouldRemove = false;
        final String targetMD5Hex = Files.hash(blacklistedFile, Hashing.md5()).toString();
        logger.trace("Considering blacklisted file [{}] with MD5 checksum [{}] for removal.", blacklistedFile, targetMD5Hex);

        if (isInUpdateList(relativePath)) {
            logger.debug("File {} in update list, not removing it. It will be properly handled when applying the new changes.", relativePath);
        } else {
            // the file is not in update list, which means it was removed in the new distribution
            // check whether the old file has user changes, if it does, just create marker file, otherwise remove it
            if (checksums.containsKey(relativePath) && containsMatchingChecksum(checksums.get(relativePath), targetMD5Hex)) {
                shouldRemove = true;
                logger.info("File {} is on blacklist, but no user changes were detected. Deleting it directly.", relativePath);
            } else {
                logger.warn("File {} is on black list and it only exists in the old distribution. Creating just marker " +
                        "file with .removed suffix instead of removing it. Please investigate the change manually and " +
                        "remove the file if needed.", blacklistedFile);
                FileUtils.touch(new File(distributionRoot, relativePath + ".removed"));
                logger.trace("File {} is on black list, ignoring it.", relativePath);
            }
        }
        return shouldRemove;
    }

    private void removeEntryIfExists(File entry) throws IOException {
        if (entry.exists()) {
            logger.trace("Removing entry {}.", entry.getAbsolutePath());
            if (entry.isFile()) {
                PatchingUtils.deleteFileAndParentsIfEmpty(entry, distributionRoot);
            } else if (entry.isDirectory()) {
                PatchingUtils.deleteDirAndParentsIfEmpty(entry, distributionRoot);
            } else {
                // this should not really happen as the remove-list should only contain files and dirs. Throw exception to
                // indicate callers are passing incorrect remove-list
                throw new RuntimeException("Specified file '" + entry + "' is in fact directory. Remove list " +
                        "should only contain actual files.");
            }
        } else {
            logger.trace("File {} not found, can not remove it.", entry);
            // nothing to do here
        }
    }

    private boolean isInUpdateList(final String relativePath) {
        return Iterables.any(patchEntries, new Predicate<PatchEntry>() {
            @Override
            public boolean apply(PatchEntry entry) {
                return entry.getRelativePath().equals(relativePath);
            }
        });
    }

    /**
     * Creates list of concrete relative paths that should be removed from the distribution.
     *
     * The input list may contain wildcards (e.g. *), aka globs.
     *
     * @param removeListWithGlobs list of relative paths, possibly with wildcards
     * @return list of relative paths to remove, these are concrete paths exist in the distribution (no wildcards)
     */
    private List<String> findPathsToRemove(List<String> removeListWithGlobs) {
        logger.debug("Gathering paths that will be removed, based on remove list with wildcards.");
        final Iterable<Pattern> regexPatterns = Iterables.transform(removeListWithGlobs, new Function<String, Pattern>() {
            @Override
            public Pattern apply(String input) {
                return Pattern.compile(PatchingUtils.createRegexFromGlob(input));
            }
        });
        // gather all paths in the distribution
        Iterable<File> files = FileUtils.listFilesAndDirs(distributionRoot, TrueFileFilter.INSTANCE,
                DirectoryFileFilter.INSTANCE);
        // remove the distribution root as we will not delete that one in any case
        Iterables.removeIf(files, new Predicate<File>() {
            @Override
            public boolean apply(File file) {
                return file.equals(distributionRoot);
            }
        });
        // transform the files into relative paths
        Iterable<String> paths = Iterables.transform(files, new Function<File, String>() {
            @Override
            public String apply(File file) {
                // strip the distribution root from the path; +1 for name separator
                String relPath = file.getAbsolutePath().substring(distributionRoot.getAbsolutePath().length() + 1);
                return relPath.replace(File.separatorChar, Patcher.CANONICAL_NAME_SEPARATOR_CHAR);
            }
        });
        return Lists.newArrayList(Iterables.filter(paths, new Predicate<String>() {
            @Override
            public boolean apply(String path) {
                return matchesOneOfRegexs(path, regexPatterns);
            }
        }));
    }

    private boolean matchesOneOfRegexs(final String path, Iterable<Pattern> regexPatterns) {
        return Iterables.any(regexPatterns, new Predicate<Pattern>() {
            @Override
            public boolean apply(Pattern pattern) {
                return pattern.matcher(path).matches();
            }
        });
    }


    private void applyUpdates() throws IOException {
        logger.info("Applying updates to distribution root {}", distributionRoot.getAbsolutePath());
        for (PatchEntry patchEntry : patchEntries) {
            String relPath = patchEntry.getRelativePath();
            File entryToCopy = patchEntry.getActualFile();
            File target = new File(distributionRoot, relPath);
            if (blackList.contains(relPath) && target.exists()) {
                final String sourceMD5Hex = Files.hash(entryToCopy, Hashing.md5()).toString();
                final String targetMD5Hex = Files.hash(target, Hashing.md5()).toString();
                logger.trace("Source file ({}) MD5 checksum: {}", entryToCopy, sourceMD5Hex);
                logger.trace("Target file ({}) MD5 checksum: {}", target, targetMD5Hex);
                // do not create ".new" marker file for files that are identical (it would only confuse users)
                if (sourceMD5Hex.equals(targetMD5Hex)) {
                    logger.debug("Blacklisted file {} will be ignored, because there are no changes. The patched file" +
                            " is identical to the file being patched (current file in the distribution).", entryToCopy);
                    continue;
                }
                // check if the current file has a known checksum - e.g. it was updated by one of the patches and thus
                // still has the default value shipped by one the previous versions. In this case it is safe to upgrade
                // the file automatically
                if (checksums.containsKey(relPath) && containsMatchingChecksum(checksums.get(relPath), targetMD5Hex)) {
                    logger.info("File {} is on blacklist, but no user changes were detected. Replacing it with the latest version.", relPath);
                } else {
                    logger.warn("File {} is on blacklist, creating a new file with suffix .new instead of overwriting. " +
                            "Please investigate the differences and apply them manually.", relPath);
                    target = new File(distributionRoot, relPath + ".new");
                }
            }
            if (entryToCopy.isFile()) {
                logger.trace("Copying file {} to {}", entryToCopy, target);
                FileUtils.copyFile(entryToCopy, target);
            } else {
                logger.trace("Copying contents of directory {} to {}", entryToCopy, target);
                FileUtils.copyDirectory(entryToCopy, target);
            }
        }
    }

    private boolean containsMatchingChecksum(List<Checksum> checksums, final String targetChecksum) {
        return Iterables.tryFind(checksums, new Predicate<Checksum>() {
            @Override
            public boolean apply(Checksum checksum) {
                return checksum.getHexValue().equals(targetChecksum);
            }
        }).isPresent();
    }

}
