package com.redhat.installer.asconfiguration.securitydomain.constant;

import com.redhat.installer.constants.GeneralConstants;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/** Constants for Security Domain Panel
 * 
 * Implemented By: 
 *      SecurityDomainPanel
 *      SecurityDomainPanelConsoleHelper
 *      SecurityDomainPanelAutomationHelper
 *      SecurityDomainModule
 *      
 * Variable Format: BASE + ID + VARIABLE
 * Label    Format: BASE + ID + LABEL
 * Tip      Format: BASE + ID + TIP
 * Dynamic Variable Format:
 *      BASE + ID + DYNAMIC_HEADER + #COUNT# + VARIABLE
 *      => Represents the dynamic variable under a dynamic header column, row count
 *      BASE + ID + COUNT
 *      => Represents the number of variables contained in a dynamic component
 *      => Dynamic component IDs start with 'add'
 *      
 * @author mtjandra
 */
public interface SecurityDomainConstants extends GeneralConstants
{

    public static final String SECURITY_HEADLINE   = "SecurityDomainPanel.headline";
    public static final String SECURITY_XML_HEADER = "com.redhat.installer.asconfiguration.securitydomain.panel.SecurityDomainPanel";
    public static final String MAIN_TEXT = "securitydomain.text";
    public static final String BASE     = "securitydomain.";
    public static final String COUNT    = ".count";
    public static final String MODULE   = ".module";
    public static final String ERROR    = ".error";
    public static final String LEFT     = ".left";
    public static final String MIDDLE   = ".middle";
    public static final String RIGHT    = ".right";
    public static final String[] DYNAMIC_HEADERS = { LEFT , MIDDLE, RIGHT };
    public static final int NUM_DYNAMIC_COL = DYNAMIC_HEADERS.length;
    
    public static final String NAME_ID           = "name";
    public static final String CACHE_ID          = "cachetype";
    public static final String AUTHENTICATION_ID = "add.authen";
    public static final String AUTHORIZATION_ID  = "add.author";
    public static final String MAPPING_ID        = "add.mapping";
    public static final String[] DATUM           = {NAME_ID, CACHE_ID, AUTHENTICATION_ID, AUTHORIZATION_ID, MAPPING_ID};
    public static final String[] DEPENDANT_DATUM = {AUTHENTICATION_ID, AUTHORIZATION_ID, MAPPING_ID};
    
    public static final String DEFAULT_OPERATION = "testName=testValue";
    public static final String OPERATION_VALIDATOR = "([^ ,=]+=[^, =]+)(,[^ ,=]+=[^, =]+)*";
    
    // Authentication
    public static final String[][] authenticationOptions = {
      {"Client", "Certificate", "CertificateUsers", "CertificateRoles" , "Database", "DatabaseCertificate",
        "DatabaseUsers", "Identity", "Ldap", "LdapExtended", "RoleMapping", "RunAs", "Simple", "ConfiguredIdentity",
        "SecureIdentity", "PropertiesUsers", "SimpleUsers", "LdapUsers", "Kerberos", "SPNEGOUsers", "AdvancedLdap",
        "AdvancedADLdap", "UsersRoles"},
      {"Required", "Requisite", "Sufficient", "Optional" },
      {"testName=testValue"}};
    
    //Authorization
    public static final String[][] authorizationOptions = {
        {"DenyAll", "PermitAll", "Delegating", "Web" , "JACC", "XACML"},
        {"Required", "Requisite", "Sufficient", "Optional" },
        {"testName=testValue"}};
        
    //Mapping
    
    public static final String[][] mappingOptions = {
        {"PropertiesRoles", "SimpleRoles", "DeploymentRoles", "DatabaseRoles" , "LdapRoles"},
        {"principal","credential","role","attribute"},
        {"testName=testValue"}};   
    

    public static final LinkedHashMap<String[], Class<?>> UI_ELEMENTS = Defaults.uiElements();
    public static final Map<String, String[][]> DEFAULTS_MAP = Defaults.defaultsMap();
    public static final Map<String, Integer> DYNAMIC_MAP = Defaults.maxComponents();
    static class Defaults {
        private Defaults(){}
        private static Map<String, String[][]> defaultsMap()  //Block of code executed after the HashMap's Constructor is invoked
        {
            Map<String, String[][]> m = new HashMap<String, String[][]>();
            m.put(AUTHENTICATION_ID, authenticationOptions);
            m.put(AUTHORIZATION_ID, authorizationOptions);
            m.put(MAPPING_ID, mappingOptions);
            return Collections.unmodifiableMap(m); 
        }    
        private static Map<String, Integer> maxComponents()  //Block of code executed after the HashMap's Constructor is invoked
        {
            Map<String, Integer> m = new HashMap<String, Integer>();
            m.put(AUTHENTICATION_ID, 24);
            m.put(AUTHORIZATION_ID, 6);
            m.put(MAPPING_ID, 5);
            return Collections.unmodifiableMap(m); 
        }    
        private static LinkedHashMap<String[], Class<?>> uiElements()
        {
            LinkedHashMap<String[], Class<?>> contents = new LinkedHashMap<String[], Class<?>>();
            contents.put(new String[]{NAME_ID, "mySecurityDomain"}, JTextField.class);
            contents.put(new String[]{CACHE_ID, "None", "default", "infinispan"}, JComboBox.class);
            contents.put(new String[]{AUTHENTICATION_ID, "No", "Yes"}, JComboBox.class);
            contents.put(new String[]{AUTHORIZATION_ID, "No", "Yes"}, JComboBox.class);
            contents.put(new String[]{MAPPING_ID, "No", "Yes"}, JComboBox.class);
            return contents;
        }
    }

}
