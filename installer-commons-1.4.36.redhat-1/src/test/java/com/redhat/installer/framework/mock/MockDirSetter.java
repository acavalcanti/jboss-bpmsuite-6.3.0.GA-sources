package com.redhat.installer.framework.mock;

import org.junit.rules.TemporaryFolder;

import java.io.IOException;

/**
 * Responsible for setting up the parts of the directory and file structure needed
 * for certain unit tests.
 */
public class MockDirSetter {

    /**
     * Used for creating specified paths for testing.
     * @param paths
     */
    public static void makeMockDir(TemporaryFolder folder, String ... paths) {
        try {
            folder.newFolder(paths);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
