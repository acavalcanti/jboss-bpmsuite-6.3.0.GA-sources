package com.redhat.installer.framework.mock;

import com.redhat.installer.framework.constants.TestPaths;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;

import java.io.*;

/**
 * Responsible for providing methods used to copy resource files into the test file
 * structure at test time.
 * Created by fcanas on 3/20/14.
 */
public class MockResourceBuilder
{

    /**
     * Copy a file from the given path in resources to the generated base test dir at the given path.
     * @tempFolder the current test's TemporaryFolder
     * @param testPath
     */
    public static void copyResourceToBaseDir(TemporaryFolder tempFolder, String resourcePath, String testPath)
    {
        try
        {
            FileUtils.copyFile(new File(System.getProperty("user.dir")+File.separator+TestPaths.TEST_RESOURCE_DIR + resourcePath), new File(tempFolder.getRoot(),testPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Copies a resource file at resourcePath into the runtime test directory at destinationPath.
     * @param resourcePath
     * @param destinationPath
     */
    public static void copyResource(String resourcePath, String destinationPath)
    {
        File destination = new File(destinationPath);
        File source = new File(TestPaths.TEST_RESOURCE_DIR + "/" + resourcePath);
        destination.getParentFile().mkdirs();

        try
        {
            FileUtils.copyFile(source, destination);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a file
     * @param filePath
     */
    public static void deleteFile(String filePath)
    {
        try
        {
            FileUtils.forceDelete(new File(filePath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Creates a file
     * @param filePath
     */
    public static void createFile(String filePath)
    {
        try
        {
            FileUtils.touch(new File(filePath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Convenience method to copy all standalone and domain configuration files into your installation directory.
     * Convention is to use regular_file_name.xml.<extenstion> file names in your resources path
     *
     * @param extension                 Extention of the file you are looking for
     * @param installationDirectory     Path to your installation direcotyr INSTALL_PATH/INSTALL_SUBPATH/
     * @param resourcePath              Path to where the resources are located
     */
    public static void copyConfigFiles(String extension, String installationDirectory, String resourcePath)
    {
        String standaloneDirectory = installationDirectory + "standalone" + File.separator + "configuration" + File.separator;
        String domainDirectory = installationDirectory + "domain" + File.separator + "configuration" + File.separator;
        for (String standaloneConfigFile : TestPaths.standaloneDescriptors)
        {
            copyResource(
                    resourcePath + standaloneConfigFile + "." + extension,
                    standaloneDirectory + standaloneConfigFile);
        }
        for (String domainConfigFile : TestPaths.domainDescriptors)
        {
            copyResource(
                    resourcePath + domainConfigFile + "." + extension,
                    domainDirectory + domainConfigFile);
        }
    }
}
