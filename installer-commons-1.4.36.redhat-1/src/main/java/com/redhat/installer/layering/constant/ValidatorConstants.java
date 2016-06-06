package com.redhat.installer.layering.constant;

import java.io.File;

public final class ValidatorConstants {

	// variable names
	public static final String existingLayers = "existing.layers.conf";
	public static final String existingProduct = "existing.product.conf";
	public static final String newLayers = "new.layers.conf";
	public static final String newProduct = "new.product.conf";
    public static final String productName = "product.name";
	public static final String productReadableName = "platform.readable.name";
    //public static final String INSTALL_SUBPATH = "INSTALL_SUBPATH";
    public static final String CONFIG_DIR = "configuration";
	
	// file locations
	public static final String backupExt = ".backup";
	public static final String productConfLoc = File.separator + "bin" + File.separator + "product.conf";
	public static final String layersConfLoc = File.separator + "modules" + File.separator + "layers.conf";
	public static final String standaloneStartLocUnix = File.separator + "bin" + File.separator + "standalone.sh";
	public static final String standaloneStartLocWindows = File.separator + "bin" + File.separator + "standalone.bat";
	public static final String standaloneConfigFolderLoc = File.separator + "standalone" + File.separator + "configuration" + File.separator;
	public static final String standaloneConfigFolder = File.separator + "standalone" + File.separator + "configuration" + File.separator;
	public static final String standaloneXmlLoc = File.separator + "standalone" + File.separator + "configuration" + File.separator + "standalone.xml";
	public static final String standaloneMgmtUsersLoc = File.separator + "standalone" + File.separator + "configuration" + File.separator + "mgmt-users.properties";
    public static final String standaloneAppUsersLoc = File.separator + "standalone" + File.separator + "configuration" + File.separator + "application-users.properties";
    public static final String standaloneAppRolesLoc = File.separator + "standalone" + File.separator + "configuration" + File.separator + "application-roles.properties";
	public static final String domainStartLocUnix = File.separator + "bin" + File.separator + "domain.sh";
	public static final String domainStartLocWindows = File.separator + "bin" + File.separator + "domain.bat";
	public static final String domainXmlLoc = File.separator + "domain" + File.separator + "configuration" + File.separator + "domain.xml";
	public static final String domainConfigFolderLoc = File.separator + "domain" + File.separator + "configuration" + File.separator;
	public static final String domainMgmtUsersLoc = File.separator + "domain" + File.separator + "configuration" + File.separator + "mgmt-users.properties";
    public static final String domainAppUsersLoc = File.separator + "domain" + File.separator + "configuration" + File.separator + "application-users.properties";
    public static final String domainAppRolesLoc = File.separator + "domain" + File.separator + "configuration" + File.separator + "application-roles.properties";
	public static final String hostXmlLoc = File.separator + "domain" + File.separator + "configuration" + File.separator + "host.xml";
	public static final String addUserLocUnix = File.separator + "bin" + File.separator + "add-user.sh";
	public static final String addUserLocWindows = File.separator + "bin" + File.separator + "add-user.bat";

	public static final String[] requiredScriptsUnix = {ValidatorConstants.standaloneStartLocUnix, ValidatorConstants.domainStartLocUnix, ValidatorConstants.addUserLocUnix};
	public static final String[] requiredScriptsWindows = {ValidatorConstants.standaloneStartLocWindows, ValidatorConstants.domainStartLocWindows, ValidatorConstants.addUserLocWindows};
	public static final String[] requiredStandalonConfigDV = {"standalone","standalone-full-ha", "standalone-ha"};
	public static final String[] requiredStandalonConfig = {"standalone","standalone-full-ha", "standalone-ha", "standalone-full", "standalone-osgi"};
	public static final String[] requiredDomainConfigNonEap = {"host", "domain"};

	// product.conf possible contents
	public static final String eap = "eap";
	public static final String brms = "brms";
	public static final String bpms = "bpms";
	public static final String soa = "soa";
	public static final String sramp = "sramp";

    /**
     * Note: Order matters because we are using this order for the validator's Truth Table.
     * Don't change the order!
     */
    public static final String [] products = {
            ValidatorConstants.bpms, ValidatorConstants.brms, ValidatorConstants.sramp,
            ValidatorConstants.dv, ValidatorConstants.soa, ValidatorConstants.eap
    };

    /**
     * Note: Order matters because we are using this order for the validator's Truth Table.
     * Don't change the order!
     */
    public static final String [] layers = {
            ValidatorConstants.soa, ValidatorConstants.sramp, ValidatorConstants.bpms,
            ValidatorConstants.brms, ValidatorConstants.dv
    };
	
	// layers.conf possible combinations.
	public static final String dv = "dv";
	public static final String soaEds = ValidatorConstants.soa+","+ValidatorConstants.dv;
	public static final String soaBrms = ValidatorConstants.soa+","+ValidatorConstants.brms;
	public static final String soaBrmsEds = ValidatorConstants.soa+","+ValidatorConstants.brms+","+ValidatorConstants.dv;
	public static final String soaBpms = ValidatorConstants.soa+","+ValidatorConstants.bpms;
	public static final String brmsEds = ValidatorConstants.brms+","+ValidatorConstants.dv;
	public static final String soaSramp = ValidatorConstants.soa+","+ValidatorConstants.sramp;
	public static final String soaSrampEds = ValidatorConstants.soa+","+ValidatorConstants.sramp+","+ValidatorConstants.dv;
	public static final String soaSrampBrms = ValidatorConstants.soa+","+ValidatorConstants.sramp+","+ValidatorConstants.brms;
	public static final String soaSrampBrmsEds = ValidatorConstants.soa+","+ValidatorConstants.sramp+","+ValidatorConstants.brms+","+ValidatorConstants.dv;
	public static final String soaSrampBpms = ValidatorConstants.soa+","+ValidatorConstants.sramp+","+ValidatorConstants.bpms;
	public static final String srampBrms = ValidatorConstants.sramp+","+ValidatorConstants.brms;
	public static final String srampBrmsEds = ValidatorConstants.sramp+","+ValidatorConstants.brms+","+ValidatorConstants.dv;
	public static final String srampBpms = ValidatorConstants.sramp+","+ValidatorConstants.bpms;
	public static final String bpmsEds = ValidatorConstants.bpms+","+ValidatorConstants.dv;
	public static final String srampBpmsEds  = ValidatorConstants.sramp+","+ValidatorConstants.bpms+","+ValidatorConstants.dv;

    public static final String srampEds = ValidatorConstants.sramp+","+ValidatorConstants.dv;
    public static final String soaBpmsEds = ValidatorConstants.soa+","+ValidatorConstants.bpms+","+ValidatorConstants.dv;
}
