package com.redhat.installer.framework.constants;

public interface Paths
{
    public static final String mockLangpackFilename = "mock-langpack.xml";
    public static final String mockKeystoreFilename = "mock-keystore.keystore";
    public static final String mockSettingsFilename = "mock-settings.xml";
    public static final String testLogFilename = "test-installationlog.txt";
    public static final String testPropertiesFilename = "test-properties.properties";

    public static final String TEST_RESOURCE_DIR = "src/test/resources";
    public static final String WORKING_DIR = System.getProperty("user.dir");

    public static final String ldapWorkingDir = "ldap-server/";

    public String INSTALL_PATH = "junit-installdir/";
    public String ldapWorkingPath = INSTALL_PATH + ldapWorkingDir;

    public String mockLangpackPath = INSTALL_PATH + mockLangpackFilename;
    public String mockKeystorePath = INSTALL_PATH + mockKeystoreFilename;
    public String mockSettingsPath = INSTALL_PATH + mockSettingsFilename;
    public String testLogPath = INSTALL_PATH + testLogFilename;
    public String testPropertiesPath = INSTALL_PATH + testPropertiesFilename;

    public String layersConfPath = INSTALL_PATH + "/modules/layers.conf";
    public String productConfPath = INSTALL_PATH + "/bin/product.conf";


}
