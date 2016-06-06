package com.redhat.installer.framework.mock;

import com.izforge.izpack.installer.AutomatedInstallData;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class MockInstallerStructure
{
    /** Use the @Rule annotation instead, so test directory gets deleted automatically */
    public AutomatedInstallData idata;
    final private static String resourcePath = "src/test/resources/mockFiles/";

    public static final String mainPath = "jboss" + File.separator;
    public static final String subPath = mainPath + "subdir" + File.separator;


    /**
     * Create the main target path folders
     * Create the directory where a pre-existing jboss installation was installed
     * Create a subdirectory where a pre-existing jboss installation was installed
     * @param testFolder
     * @param idata
     * @throws IOException
     */
    public static void createTargetPath(TemporaryFolder testFolder, AutomatedInstallData idata) throws IOException
    {
        String mainPath = "jboss" + File.separator;
        File mainDir = testFolder.newFolder(mainPath);
        idata.setInstallPath(mainDir.getAbsolutePath());
    }

    /**
     * Create the default directories and content from a pre-existing jboss installation
     * TODO: This is not yet complete and does not create ALL necessary directories and content
     * @param testFolder
     * @param idata
     * @throws IOException
     */
    public static void createDefaultDirectories(TemporaryFolder testFolder, AutomatedInstallData idata) throws IOException
    {
        String[] directories = {
                "appclient", "bin", "bundle", "cli-scripts", "docs",
                "domain", "modules", "standalone", "welcome-comment"
        };

        for (String directory : directories)
            testFolder.newFolder(directory);

        String standalonePath = "standalone" + File.separator;
        createStandaloneContent(testFolder, standalonePath);

        String domainPath = "domain" + File.separator;
        createDomainContent(testFolder, domainPath);
    }

    private static void createStandaloneContent(TemporaryFolder testFolder, String standalonePath) throws IOException
    {
        String[] folders = {
                "configuration", "data", "deployment", "lib", "log", "tmp"
        };

        for (String folder : folders)
            testFolder.newFolder(standalonePath + folder);

        createStandaloneConfigurationConent(testFolder, standalonePath + "configuration" + File.separator);
    }

    private static void createDomainContent(TemporaryFolder testFolder, String domainPath) throws IOException
    {
        String[] folders = {
                "configuration", "data", "deployment", "lib", "log", "tmp"
        };

        for (String folder : folders)
            testFolder.newFolder(domainPath + folder);

        createDomainConfigurationConent(testFolder, domainPath + "configuration" + File.separator);
    }

    private static void createDomainConfigurationConent(TemporaryFolder testFolder, String configPath) throws IOException
    {
        String[] files = {
                "application-roles.properties", "application-users.properties", "default-server-logging.properties",
                "domain.xml", "host-master.xml", "host-slave.xml", "host.xml", "logging.properties", "mgmt-users.properties"
        };

        for (String file : files)
            testFolder.newFile(configPath + file);
    }

    private static void createStandaloneConfigurationConent(TemporaryFolder testFolder, String configPath) throws IOException
    {
        String[] files = {
                "application-roles.properties", "logging.properties","standalone-full-ha.xml",
                "standalone-ha.xml", "standalone.xml","application-users.properties",
                "mgmt-users.properties", "standalone-full.xml", "standalone-osgi.xml"
        };

        for (String file : files)
            testFolder.newFile(configPath + file);
    }
}
