package com.redhat.installer.framework.constants;

/**
 * Static class container for paths and filenames common to all tests.
 */
public class TestPaths {
    /**
     * The base resources folder where spec and config files needed for
     * unit tests are kept.
     */
    public static final String TEST_RESOURCE_DIR = "src/test/resources";

    /**
     * The filname of the langpack file used in testing.
     */
    public static final String MOCK_LANGPACK_FILENAME = "/mock-langpack.xml";

    /**
     * Configuration file names for standalone and domain.
     */
    public static final String[] domainDescriptors = new String[] {"domain.xml", "host.xml" };
    public static final String[] standaloneDescriptors = new String[] {
            "standalone.xml", "standalone-ha.xml", "standalone-osgi.xml",
            "standalone-full-ha.xml", "standalone-full.xml"
    };
    public static final String[] descriptors = new String[] {
            "standalone.xml", "standalone-ha.xml", "standalone-osgi.xml",
            "standalone-full-ha.xml", "standalone-full.xml", "host.xml", "domain.xml"
    };

}
