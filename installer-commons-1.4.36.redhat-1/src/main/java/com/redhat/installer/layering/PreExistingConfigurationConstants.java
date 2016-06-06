package com.redhat.installer.layering;

/**
 * Created by thauser on 2/27/14.
 */
public class PreExistingConfigurationConstants {
    public static final String modulesPath = "/modules";
    public static final String systemModulesPath = modulesPath + "/system/layers";
    public static final String baseModulesPath = systemModulesPath + "/base/";
    public static final String srampModulesPath = systemModulesPath + "/sramp/";
    public static final String soaModulesPath = systemModulesPath + "/soa/";
    public static final String dvModulesPath = systemModulesPath + "/dv/";
    public static final String brmsModulesPath = systemModulesPath + "/brms/";
    public static final String bpmsModulesPath = systemModulesPath + "/bpms/";
    public static final String[] domainDescriptors = new String[]{"host.xml", "domain.xml"};
    public static final String[] standaloneDescriptors = new String[]{"standalone.xml", "standalone-ha.xml", "standalone-osgi.xml", "standalone-full-ha.xml", "standalone-full.xml"};
    public static final String[] descriptors = new String[] {
            "standalone.xml", "standalone-ha.xml", "standalone-osgi.xml",
            "standalone-full-ha.xml", "standalone-full.xml", "host.xml", "domain.xml"
    };
    public static final String fswUserRoles = "overlorduser,admin.sramp,dev,qa,stage,prod,manager,arch,ba";
}
