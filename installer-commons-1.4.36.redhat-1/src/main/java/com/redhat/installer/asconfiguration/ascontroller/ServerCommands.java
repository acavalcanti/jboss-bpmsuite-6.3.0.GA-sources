package com.redhat.installer.asconfiguration.ascontroller;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import org.jboss.as.cli.*;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.security.vault.VaultSession;
import org.jboss.dmr.ModelNode;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class ServerCommands {


    /**
     * Useful commands made final, change in one place if needed
     */
    private static final String RUN_BATCH_CMD = "run-batch";
    private static final String RELOAD_CMD = "reload --admin-only=true";
    private static final String DOMAIN_CMD_PREFIX = "/host=%s";
    private static final String SHUTDOWN_CMD = ":shutdown";

    /**
     * Vault related variables. *
     */
    private static VaultSession vault = null; // the vault session, if it is chosen to be
    private String keystore;
    private String alias;
    private String encrDir;
    private String salt;
    private int iterations;


    /**
     * Command Context related variables *
     */
    private CommandContext context;
    private boolean isDomain; // commands need to be modified if we're a domain
    private String[] domainProfiles; // the profile the domain commands should be
    // added to
    private static String domainHostname = "master"; //the default name of the domain,
    // for use with DOMAIN_CMD_PREFIX

    /** This class needs to know about these. */
    //TODO: use JBossJDBCConstants?

    /**
     * Security-domain validation values *
     */
    private final String[] validAuthenticationCodes = {"Client", "org.jboss.security.ClientLoginModule", "Certificate", "org.jboss.security.auth.spi.BaseCertLoginModule", "CertificateUsers",
            "org.jboss.security.auth.spi.BaseCertLoginModule", "CertificateRoles", "org.jboss.security.auth.spi.CertRolesLoginModule", "Database",
            "org.jboss.security.auth.spi.DatabaseServerLoginModule", "DatabaseCertificate", "org.jboss.security.auth.spi.DatabaseCertLoginModule", "DatabaseUsers",
            "org.jboss.security.auth.spi.DatabaseServerLoginModule", "Identity", "org.jboss.security.auth.spi.IdentityLoginModule", "Ldap", "org.jboss.security.auth.spi.LdapLoginModule",
            "LdapExtended", "org.jboss.security.auth.spi.LdapExtLoginModule", "RoleMapping", "org.jboss.security.auth.spi.RoleMappingLoginModule", "RunAs",
            "org.jboss.security.auth.spi.RunAsLoginModule", "Simple", "org.jboss.security.auth.spi.SimpleServerLoginModule", "ConfiguredIdentity",
            "org.picketbox.datasource.security.ConfiguredIdentityLoginModule", "SecureIdentity", "org.picketbox.datasource.security.SecureIdentityLoginModule", "PropertiesUsers",
            "org.jboss.security.auth.spi.PropertiesUsersLoginModule", "SimpleUsers", "org.jboss.security.auth.spi.SimpleUsersLoginModule", "LdapUsers",
            "org.jboss.security.auth.spi.LdapUsersLoginModule", "Kerberos", "com.sun.security.auth.module.K5b5LoginModule", "SPNEGOUsers", "org.jboss.security.negotiation.spnego.SPNEGOLoginModule",
            "AdvancedLdap", "org.jboss.security.negotiation.AdvancedLdapLoginModule", "AdvancedADLdap", "org.jboss.security.negotiation.AdvancedADLoginModule", "UsersRoles",
            "org.jboss.security.auth.spi.UsersRolesLoginModule"};

    private final String[] validAuthorizationCodes = {"DenyAll", "org.jboss.security.authorization.modules.AllDenyAuthorizationModule", "PermitAll",
            "org.jboss.security.authorization.modules.AllPermitAuthorizationModule", "Delegating", "org.jboss.security.authorization.modules.DelegatingAuthorizationModule", "Web",
            "org.jboss.security.authorization.modules.WebAuthorizationModule", "JACC", "org.jboss.security.authorization.modules.JACCAuthorizationModule", "XACML",
            "org.jboss.security.authorization.modules.XACMLAuthorizationModule"};

    private final String[] validMappingCodes = {"PropertiesRoles", "org.jboss.security.mapping.providers.role.PropertiesRolesMappingProvider", "SimpleRoles",
            "org.jboss.security.mapping.providers.role.SimpleRolesMappingProvider", "DeploymentRoles", "org.jboss.security.mapping.providers.DeploymentRolesMappingProvider", "DatabaseRoles",
            "org.jboss.security.mapping.providers.role.DatabaseRolesMappingProvider", "LdapRoles", "org.jboss.security.mapping.providers.role.LdapRolesMappingProvider"};

    private final String[] validFlags = {"Required", "Requisite", "Sufficient", "Optional"};

    private final String[] validTypes = {"principal", "credential", "role", "attribute"};

    /**
     * These fields are logger related. For our uses, we simply use a List of
     * the commands we have entered on the context, unless we're in Batch Mode;
     * then we log the batch commands
     */
    private String logFilePath = null;
    private List<String> naiveLogger;
    private Logger logger;

    /**
     * Create an instance to connect using the given username / password If the
     * host parameter is null, the context will connect to the localhost.
     */
    private ServerCommands(String user, char[] pwd, String host, int port, boolean slave, String... profile) throws CliInitializationException {
        if (profile != null) {
            domainProfiles = profile;
            isDomain = true;
        }

        Map<String, String> newEnv = new HashMap<String, String>();
        newEnv.put("JBOSS_HOME", AutomatedInstallData.getInstance().getInstallPath() + File.separator);
        addEnv(newEnv);

        context = CommandContextFactory.getInstance().newCommandContext(host, port, user, pwd);
        context.setSilent(false);
        context.setResolveParameterValues(false);
        naiveLogger = new ArrayList<String>(1);
    }

    /**
     * Method to return an instance of ServerCommands that connects to a domain
     * ascontroller using a username and password
     * @param user    The username for authenticating with the management interface
     *                of the server in question
     * @param pwd     The password associated with the given username
     * @param port    The port upon which the management interface is listening
     * @param slave
     * @param profile The profile(s) that the Context should connect to on the host
     */

    public static ServerCommands createLocalDomainUsernameSession(String user, char[] pwd, int port, boolean slave, String... profile) throws CliInitializationException {
        return new ServerCommands(user, pwd, null, port, slave, profile);
    }

    /**
     * Method to return an instance of ServerCommands that connects to a
     * standalone server using a username and password
     *
     * @param user The username for authenticating with the management interface
     *             of the server in question
     * @param pwd  The password associated with the given username
     * @param port The port upon which the management interface is listening
     */
    public static ServerCommands createLocalStandaloneUsernameSession(String user, char[] pwd, int port) throws CliInitializationException {
        return new ServerCommands(user, pwd, null, port, false, (String[]) null);
    }

    /**
     * Method to return an instance of ServerCommands that connects to a remote
     * management interface, using username and password authentication.
     *
     * @param user    The username for authenticating with the management interface
     *                of the server in question
     * @param pwd     The password associated with the given username
     * @param port    The port upon which the management interface is listening
     * @param profile The profile that the Context should connect to. Only
     *                applicable if the server is running in domain mode. null value
     *                means remote server is in standalone mode
     * @param host    The host upon which the management interface is running
     */
    public static ServerCommands createRemoteUsernameSession(String user, char[] pwd, int port, String profile, String host) throws CliInitializationException {
        return new ServerCommands(user, pwd, host, port, false, profile);
    }


    /**
     * Returns a general session that makes no assumptions about what kind of server that it's on.
     *
     * @throws CliInitializationException
     */

    public static ServerCommands createSession(String user, char[] pwd, int port) throws CliInitializationException {
        return new ServerCommands(user, pwd, null, port, false, (String[]) null);
    }

    /**
     * Calling this method creates a new batch. A batch is a construct that
     * takes all subsequent commands into a list, and upon calling the
     * executeBatch() method, all of the commands in the currently active batch
     * will be run consecutively as an atomic unit. This means that any failures
     * in the commands in the batch will cause all of the other commands to be
     * rolled back. If the commands to be run include a :reload command, it is
     * recommended to use the Batch functionality
     */
    public void createNewBatch() {
        context.getBatchManager().activateNewBatch();
    }

    /**
     * Stores the current batch with a given name to be executed later.
     *
     * @param name the name to give the stored batch
     */
    public void storeCurrentBatch(String name) {
        context.getBatchManager().holdbackActiveBatch(name);
    }

    /**
     * Get the list of domain profiles that this context is modifying.
     *
     * @return the array of domain profiles being modified
     */
    public String[] getDomainProfiles() {
        return domainProfiles;
    }

    /**
     * Set the domain profile that this context is modifying. Do not use this in
     * between issuing commands.
     *
     * @param domainProfile an array containing all profiles that should be modified
     */
    public void setDomainProfiles(String... domainProfile) {
        this.domainProfiles = domainProfile;
    }

    /**
     * Set the flag for the context to evaluate properties within the commands. Defaults to false.
     *
     * @param resolve true to resolve properties, false otherwise
     */

    public void setResolveParameterValues(boolean resolve) {
        context.setResolveParameterValues(resolve);
    }

    /**
     * If true, the context is currently using a password vault to mask
     * passwords. This value is set only by the createVaultSession() method.
     *
     * @return boolean indicating whether or not the vault object is null
     */
    boolean hasVault() {
        return vault != null;
    }

    /**
     * Sets the location to output the logfile. If this location is not set (it
     * is not by default), no log will be output.
     *
     * @param loc path in which to output a naive log of the commands run
     */
    public void setLogFilePath(String loc) {
        this.logFilePath = loc;
    }

    /**
     * Closes all open log handlers
     */
    public void closeLogHandlers() {
        logger.info("Finished commands from: " + logger.getName());
        if (logger != null) {
            for (Handler h : logger.getHandlers()) {
                h.close();
                logger.removeHandler(h);
            }
        }
    }

    /**
     * Gets the current logfile location
     *
     * @return the current path to the logFile
     */
    public String getLogFilePath() {
        return logFilePath;
    }

    /**
     * Disconnects the context from the host. The hostname and port are not
     * retained.
     */
    public void terminateSession() {
        closeLogHandlers();
        if (!context.isTerminated()) {
            context.terminateSession();
        }
    }

    /**
     * Connects the ascontroller to the server that the commandContext was
     * instantiated with. also loads in all commands that were accumulated in
     * disconnected batch mode.
     * TODO: fix to use DMR composite operations
     */
    public void connectContext() throws CommandLineException {
        if (context.getControllerHost() == null && context.isBatchMode()) {
            // we were in disconnected batch mode.
            context.connectController();
            // add all of the commands in the log to the real batch.
            // these should all be safe already
            for (String command : naiveLogger) {
                try {
                    handle(command);
                } catch (CommandLineException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // nothing fancy needed
            context.connectController();
        }
    }

    /**
     * Connects the context to the specified host and port. Uses the same
     * authentication as when the instance was created
     *
     * @throws CommandLineException
     */
    public void connectContext(String host, int port) throws CommandLineException {
        context.connectController(host, port);
    }

    /**
     * Creates a VaultSession object which allows the ServerCommands instance to
     * mask plaintext passwords. Exception handling is deferred to the caller. We made this static because of changes in the PostInstall that
     * require changes to all profiles / server xml descriptors.
     *
     * @param keystore    The location of the keystore on the filesystem that is to be
     *                    used to store the vault
     * @param keystorePwd The password for the given keystore
     * @param encrDir     The directory into which the vault should store files
     * @param salt        The salt for the Vault. This must be 8 characters long and can
     *                    be alphanumeric
     * @param iterations  The iteration count.
     * @param alias       The alias of the keystore.
     */

    public void createVaultSession(String keystore, String keystorePwd, String encrDir, String salt, int iterations, String alias) throws Throwable {
        this.keystore = keystore;
        this.encrDir = encrDir;
        this.salt = salt;
        this.iterations = iterations;
        this.alias = alias;

        if (!encrDir.endsWith(File.separator))
            encrDir += File.separator;
        if (!hasVault()) {
            vault = new VaultSession(keystore, keystorePwd, encrDir, salt, iterations);
            vault.startVaultSession(alias);
        }
    }

    /**
     * Install the password vault onto the server our context is connected to.
     * We use the same details as the vault that has been initialized, if any.
     * Error handling is deferred to the caller via the exit code. It is
     * recommended to use this method only while using Batch mode. If this
     * method is called, any subsequent calls to methods that contain password
     * fields (datasources, SSL, LDAP) will attempt to use the vault to mask the
     * password. However, if the target host has not been issued a :reload
     * command in between adding the vault and submitting the next command with
     * a password, the command may fail because the Vault properties are not yet
     * visible to the host.
     *
     * @return -1 if there is no vault connection and hence no vault information
     */

    public List<ModelNode> installVault() {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (!hasVault()) {
            result.add(getFailureResult("ServerCommands.installVault()", "ServerCommands : No vault has been initialized. No Vault installation attempted."));
            return result;
        }
        String masked = vault.getKeystoreMaskedPassword();
        String addVaultCmd = "/core-service=vault:add(vault-options=[(\"KEYSTORE_URL\" => \""
                + keystore.replaceAll("\\\\",
                "/")
                + "\"), "
                + "(\"KEYSTORE_PASSWORD\" => \""
                + masked
                + "\"), "
                + "(\"KEYSTORE_ALIAS\" => \""
                + alias
                + "\"), "
                + "(\"SALT\" => \""
                + salt
                + "\"), "
                + "(\"ITERATION_COUNT\" => \""
                + iterations
                + "\"), "
                + "(\"ENC_FILE_DIR\" => \""
                + encrDir
                .replaceAll("\\\\", "/")
                + "\")])";

        result.add(submitCommand(addVaultCmd));
        return result;
    }

    /**
     * This allows us to install the vault with different locations for the keystore / encrDir. Useful when the location of the keystore / encrDir contain
     * server variables.
     *
     * @param keystore The path to the keystore that should be used.
     * @param encrDir The path where encrypted vault files should be kept
     * @return A list of ModelNodes that denote the operation's success or failure
     */

    public List<ModelNode> installVault(String keystore, String encrDir) {
        List<ModelNode> result = new ArrayList<ModelNode>();

        if (!hasVault()) {
            result.add(getFailureResult("ServerCommands.installVault()", "ServerCommands : No vault has been initialized. No Vault installation attempted."));
            return result;
        }

        String masked = vault.getKeystoreMaskedPassword();
        String addVaultCmd = "/core-service=vault:add(vault-options=[(\"KEYSTORE_URL\" => \""
                + keystore.replaceAll("\\\\",
                "/")
                + "\"), "
                + "(\"KEYSTORE_PASSWORD\" => \""
                + masked
                + "\"), "
                + "(\"KEYSTORE_ALIAS\" => \""
                + alias
                + "\"), "
                + "(\"SALT\" => \""
                + salt
                + "\"), "
                + "(\"ITERATION_COUNT\" => \""
                + iterations
                + "\"), "
                + "(\"ENC_FILE_DIR\" => \""
                + encrDir
                .replaceAll("\\\\", "/")
                + "\")])";

        result.add(submitCommand(addVaultCmd));
        return result;
    }

    public List<ModelNode> addSystemProperty(String propertyName, String propertyValue){
        return addSystemProperty(propertyName, propertyValue, false);
    }
    /**
     * Adds system properties to configuration files
     *
     * @param propertyName The name of the property
     * @param propertyValue The path that the property name represents
     * @return A list of ModelNodes that denote the operation's success or failure
     */
    public List<ModelNode> addSystemProperty(String propertyName, String propertyValue, boolean resolve){
        if (!resolve){
            context.setResolveParameterValues(false);
        }
        List<ModelNode> result = new ArrayList<ModelNode>();
        String checkPropertyCmd = "/system-property="+propertyName+":read-attribute(name=value)";
        boolean existsAlready = Operations.isSuccessfulOutcome(submitCommand(checkPropertyCmd));
        String addPropertyCmd = "/system-property="
                + propertyName
                + ":add(value="
                + propertyValue
                +")";
        if (!existsAlready) {
            result.add(submitCommand(addPropertyCmd));
        }
        if (!context.isResolveParameterValues()){
            context.setResolveParameterValues(true);
        }
        return result;
    }

    /**
     * Adds system properties to individual server configuration files
     *
     * @param serverConfig Server configuration
     * @param propertyName The name of the property
     * @param propertyValue The path that the property name represents
     * @param bootTime
     * @return A list of ModelNodes that denote the operation's success or failure
     */
    public List<ModelNode> addPropertyToIndividualServer(String serverConfig, String propertyName, String propertyValue, boolean bootTime){
        List<ModelNode> result = new ArrayList<ModelNode>();
        if(isDomain) {
            ModelNode check = getServerConfigList();
            for (ModelNode c : check.get("result").asList()) {
                if (c.asString().equals(serverConfig)) {
                    String addPropertyCommand = "/host=master/server-config="
                            + serverConfig
                            + "/system-property="
                            + propertyName
                            + ":add(value="
                            + propertyValue
                            + ",boot-time="
                            + bootTime;
                    result.add(addToLoggerAndHandle(addPropertyCommand));
                }
            }
        }
        return result;
    }


    public List<ModelNode> setServerAutoStart(String serverConfig, boolean autoStart){
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (isDomain){
            ModelNode configCheck = getServerConfigList();
            for (ModelNode c : configCheck.get("result").asList()){
                if (c.asString().equals(serverConfig)){
                    String autoStartCommand = "/host=master/server-config="+serverConfig+":"+writeAttribute("auto-start",String.valueOf(autoStart));
                    result.add(addToLoggerAndHandle(autoStartCommand));
                }
            }
        }
        return result;
    }

    private ModelNode getServerConfigList(){
        String command = "/host=master:read-children-names(child-type=server-config)";
        return addToLoggerAndHandle(command);
    }

    /**
     *
     * @param artifactPath
     * @return A list of ModelNodes that denote the operation's success or failure
     */
    public List<ModelNode> deployArtifact(String artifactPath){
        List<ModelNode> result = new ArrayList<ModelNode>();
        String command;
        if(isDomain){
            command = "deploy "
                    + artifactPath
                    + " --all-server-groups";
        }
        else{
            command = "deploy " + artifactPath;
        }
        result.add(addToLoggerAndHandle(command));
        return result;
    }


    /**
     * Create a module.xml at a given basePath+modulePath location.
     *
     * @param moduleName      the name of the module inside the xml
     * @param resourceNames   an array of Strings that represent paths to files that are to
     *                        be placed within basePath+modulePath.
     * @param dependencyNames an array of Strings that name other modules as dependencies
     *                        for this one
     * @throws ParserConfigurationException a DocumentBuilder cannot be provided for the given settings
     * @throws TransformerException         irrecoverable error while transforming XML source into
     *                                      Result
     * @throws IOException                  error occurred while writing Result
     */
    public void createModuleXml(String basePath, String modulePath, String moduleName, List<String> resourceNames, List<String> dependencyNames) throws ParserConfigurationException,
            TransformerException, IOException {
        File module = new File(basePath, modulePath + File.separator + "module.xml");

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // module element and attributes
        Document doc = docBuilder.newDocument();
        Element root = doc.createElement("module");
        Attr xmlns = doc.createAttribute("xmlns");
        Attr modName = doc.createAttribute("name");
        xmlns.setValue("urn:jboss:module:1.0");
        modName.setValue(moduleName);
        root.setAttributeNode(xmlns);
        root.setAttributeNode(modName);
        doc.appendChild(root);

        // resources element and attributes (child of root)
        Element resources = doc.createElement("resources");
        root.appendChild(resources);

        // resource-root element and attributes (child of resources)
        for (String res : resourceNames) {
            Element resourceRoot = doc.createElement("resource-root");
            Attr resourcePath = doc.createAttribute("path");
            resourcePath.setValue(res);
            resourceRoot.setAttributeNode(resourcePath);
            resources.appendChild(resourceRoot);
        }

        // dependencies element and attributes (child of root)
        Element dependencies = doc.createElement("dependencies");
        root.appendChild(dependencies);

        // module element and attributes (child of dependencies)
        for (String dep : dependencyNames) {
            Element dependency = doc.createElement("module");
            Attr depName = doc.createAttribute("name");
            depName.setValue(dep);
            dependency.setAttributeNode(depName);
            dependencies.appendChild(dependency);
        }

        TransformerFactory tFactory = TransformerFactory.newInstance();
        tFactory.setAttribute("indent-number", 4);
        Transformer trans = tFactory.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);

        // Write to file from a String to preserve indentation
        StreamResult result = new StreamResult(new StringWriter());
        trans.transform(source, result);

        String outputString = result.getWriter().toString();
        BufferedWriter writeOut = null;
        try {
            writeOut = new BufferedWriter(new FileWriter(module));
            writeOut.write(outputString);
        } finally {
            if (writeOut != null) {
                writeOut.close();
            }
        }
    }

    /**
     * Helper method that attempts to return a masked password from the vault.
     *
     * @param vaultBlock     the vaultBlock for this password (used for identification)
     * @param vaultAttribute the vaultAttribute for this password (more specific
     *                       identification)
     * @param password       the password to be masked
     * @return the full VAULT value, or the empty string if no vault is defined
     */

    String maskPassword(String vaultBlock, String vaultAttribute, String password) {
        String ret = "";
        if (vault != null) {
            try {
                ret = "${" + vault.addSecuredAttribute(vaultBlock, vaultAttribute, password.toCharArray()) + "}";
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Helper method that returns a masked password from the vault without the curled braces surrounding it.
     * @param vaultBlock
     * @param vaultAttribute
     * @param password
     * @return
     */
    public String maskPasswordPlain(String vaultBlock, String vaultAttribute, String password) {
        String ret = "";
        if (vault != null) {
            try {
                ret = vault.addSecuredAttribute(vaultBlock, vaultAttribute, password.toCharArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Configure the management interfaces to use an SSL cert If a vault is
     * configured in this instance of ServerCommands, the given password will be
     * passed through the vault before the SSL element is configured. Allows customization of the vaulted string's vaultBlock and attributeName fields
     *
     * @param keystore    Location of the keystore
     * @param keystorePwd Password to the keystore
     */
    public List<ModelNode> installSslCustom(String keystore, String keystorePwd, String realmName, String vaultBlock, String attributeName) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        String password = keystorePwd;
        if (hasVault()) {
            password = maskPassword(vaultBlock, attributeName, password);
        }

        String addSslCmd = "/core-service=management/security-realm=" + realmName + "/server-identity=ssl:add(keystore-path=\""
                + keystore.replaceAll("\\\\",
                "/")
                + "\",keystore-password=\"" + password + "\")";
        result.add(submitCommand(addSslCmd));
        String addTrustStoreCmd = "/core-service=management/security-realm="+realmName+"/authentication=truststore:add(keystore-path=\""
                + keystore.replaceAll("\\\\",
                "/")
                + "\",keystore-password=\""+password+"\"";
        result.add(submitCommand(addTrustStoreCmd));

        return result;
    }

    /**
     * Configure the management interfaces to use an SSL cert If a vault is
     * configured in this instance of ServerCommands, the given password will be
     * passed through the vault before the SSL element is configured. Supplies default vaultBlock / attributeName values as
     * "ssl" and "password"
     *
     * @param keystore    Location of the keystore
     * @param keystorePwd Password to the keystore
     * @param realmName   The security-realm to attach the certificate to
     */
    public List<ModelNode> installSsl(String keystore, String keystorePwd, String realmName) {
        return installSslCustom(keystore, keystorePwd, realmName, "ssl", "password");
    }

    /**
     * Removes the http-interface (if it exists) and adds a secure http-interface.
     *
     * Used during configuration of SSL cert.
     */
    public List<ModelNode> addHttps(String realm) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        List<ModelNode> result = new ArrayList<ModelNode>();
        String removeCommand = "/core-service=management/management-interface=http-interface:remove()";
        String addCommand;
        if (isDomain){
            addCommand = "/core-service=management/management-interface=http-interface:add(interface=management,secure-port="+ idata.getVariable("domain.management-https") +",security-realm=\""+realm+"\")";
        } else {
            addCommand = "/core-service=management/management-interface=http-interface:add(secure-socket-binding=management-https,security-realm=\""+realm+"\")";
        }
        ModelNode check = submitCommand("/core-service=management:read-children-names(child-type=management-interface)");

        for (ModelNode c : check.get("result").asList()){
            if (c.asString().equals("http-interface")){
                // if http-interface exists, remove it.
                result.add(submitCommand(removeCommand));
            }
        }
        result.add(submitCommand(addCommand));
        return result;
    }


    /**
     * A refactoring of functionality that used to exist within submitCommand. This method allows other methods to see what the command
     * actually being run is, rather than being stuck with a default command
     *
     * @param cmd jboss-cli.sh format command to process
     * @return the modified jboss-cli.sh command to fit the current server mode (domain, profiles, etc)
     */
    private String prepareCommand(String cmd) {
        if (isDomain) {
            if (cmd.contains("subsystem")) {
                cmd = "/profile=%s" + cmd;
                return cmd;
            } else if (cmd.contains(RELOAD_CMD)) {
                cmd = RELOAD_CMD + " --host=" + getDomainHostname();
                return cmd;
            } else if (cmd.contains("core-service") || cmd.contains(SHUTDOWN_CMD)) {
                cmd =  String.format(DOMAIN_CMD_PREFIX, getDomainHostname()) + cmd;
                return cmd;
            } else {
                // the logic is destroyed because of the new "apply domain commands to every profile in domainProfiles"
                return cmd;
            }
        } else {
            return cmd;
        }
    }

    /**
     * Helper method to submit a command to the context. This method exists to
     * help with logging all the commands, instead of explicitly adding log
     * statements to each command.
     *
     * @param cmd The jboss-cli.sh command to execute.
     */
    public ModelNode submitCommand(String cmd) {

        ModelNode result = null;
        cmd = prepareCommand(cmd);
        if (isDomain) {
            if (cmd.contains("subsystem")) {
                for (String profile : domainProfiles) {
                    String formattedCmd = String.format(cmd, profile);
                    // run the command for each profile
                    // FIXME: this will only have the result of the final command, not all of them. got to fix this
                    result = addToLoggerAndHandle(formattedCmd);
                }
            } else {
                result = addToLoggerAndHandle(cmd);
            }

        } else {
            result = addToLoggerAndHandle(cmd);
        }
        return result;
    }

    /**
     * Helper method to make the submitCommand method less cluttered.
     * Adds the given command to the logger, and also calls handle with it,
     * as long as it is safe to do so.
     * <p/>
     * Note: can return null if the context is not connect AND is in Batch mode
     *
     * @param command the jboss-cli.sh command to execute
     * @return a ModelNode describing the operation's success or failure
     */
    private ModelNode addToLoggerAndHandle(String command) {
        ModelNode result = null;
        naiveLogger.add(command);
        // if we are NOT in disconnected batch mode
        if (!(context.getControllerHost() == null && context.isBatchMode())) {
            try {
                result = handle(command);
            } catch (CommandLineException e) {
                result = getFailureResult(command, e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                result = getFailureResult(command, e.getMessage());
                e.printStackTrace();
            }
        }
        if (Operations.isSuccessfulOutcome(result)){
            ServerCommandsHelper.setCommand(result,command);
        }
        return result;
    }

    /**
     * This method takes a List of commands and attempts to submit them all in order, treating
     * each element in the List as a command to be run. If the ServerCommands instance is in
     * batch mode, all commands in the List will be added to the batch.
     * A mapping of command to resultant ModelNode will be returned
     *
     * @param commands A List of commands that should be executed in order
     * @return A List of ModelNodes that denote the success or failure of each command
     */
    public List<ModelNode> runCommandsInList(List<String> commands) {
        List<ModelNode> ret = new ArrayList<ModelNode>();
        for (String cmd : commands) {
            ModelNode result = submitCommand(cmd);
            ret.add(result);
/*            if (!Operations.isSuccessfulOutcome(result)) {
                return ret; // fail fast on a failure, meaning the last command in the list is the one causing the failure
            }*/
        }
        return ret;
    }

    /**
     * Install a JDBC Driver using the supplied values
     */

    public List<ModelNode> installJdbcDriver(String jdbcName, String jdbcModuleName, String jdbcXaDsName) {
        List<ModelNode> results = new ArrayList<ModelNode>();
        String addJdbcCmd
                = "/subsystem=datasources/jdbc-driver=" + jdbcName +
                ":add(driver-name=\"" + jdbcName + "\",driver-module-name=\"" + jdbcModuleName + "\",driver-xa-datasource-class-name=\"" + jdbcXaDsName + "\")";
        results.add(submitCommand(addJdbcCmd));
        return results;
    }

    /**
     * Install an xa datasource using a username and password instead of
     * Security Domain
     *
     * @param dsName           name of the datasource
     * @param dsJndiName       JNDI name for the datasource
     * @param driverName       the name of the driver this datasource should use
     * @param dsMinPool        the minimum pool size
     * @param dsMaxPool        the maximum pool size
     * @param dsUsername       the username for the datasource to be secured by
     * @param dsPassword       the password for the username the datasource is to be secured
     *                         by
     * @param xaProps          a map of XA properties where the key is the property name, and
     *                         the value associated with the key is the property value
     * @param dsXaRecoveryUser the recovery user for the XA datasource
     * @param dsXaRecoveryPass the recovery password for the recovery user
     */
    public List<ModelNode> installXaDatasourceUsernamePwd(String dsName,
                                                          String dsJndiName,
                                                          String driverName,
                                                          String dsMinPool,
                                                          String dsMaxPool,
                                                          String dsUsername,
                                                          String dsPassword,
                                                          Map<String, String> xaProps,
                                                          String dsXaRecoveryUser,
                                                          String dsXaRecoveryPass,
                                                          String jta) {
        return installDatasource(dsName, dsJndiName, driverName, null, dsMinPool, dsMaxPool, null, dsUsername, dsPassword, xaProps, dsXaRecoveryUser, dsXaRecoveryPass, jta);
    }

    /**
     * Install an xa datasource using a Security Domain instead of username and
     * password
     *
     * @param dsName           name of the datasource
     * @param dsJndiName       JNDI name for the datasource
     * @param driverName       the name of the driver this datasource should use
     * @param dsMinPool        the minimum pool size
     * @param dsMaxPool        the maximum pool size
     * @param dsSecurityDomain the security domain to use to secure the datasource
     * @param xaProps          a map of XA properties where the key is the property name, and
     *                         the value associated with the key is the property value
     * @param dsXaRecoveryUser the recovery user for the XA datasource
     * @param dsXaRecoveryPass the recovery password for the recovery user
     */
    public List<ModelNode> installXaDatasourceSecurityDomain(String dsName,
                                                             String dsJndiName,
                                                             String driverName,
                                                             String dsMinPool,
                                                             String dsMaxPool,
                                                             String dsSecurityDomain,
                                                             Map<String, String> xaProps,
                                                             String dsXaRecoveryUser,
                                                             String dsXaRecoveryPass,
                                                             String jta) {
        return installDatasource(dsName, dsJndiName, driverName, null, dsMinPool, dsMaxPool, dsSecurityDomain, null, null, xaProps, dsXaRecoveryUser, dsXaRecoveryPass, jta);
    }

    /**
     * Install a datasource using a username and password instead of a security
     * domain. If the ServerCommands instance has a Vault initialized and
     * installed, the given password will be masked using the Vault
     *
     * @param dsName        name of the datasource
     * @param dsJndiName    JNDI name for the datasource
     * @param driverName    the name of the driver this datasource should use
     * @param connectionUrl the URL for the database
     * @param dsMinPool     the minimum pool size
     * @param dsMaxPool     the maximum pool size
     * @param dsUsername    the username for the datasource to be secured by
     * @param dsPassword    the password for the username the datasource is to be secured
     *                      by
     */
    public List<ModelNode> installDatasourceUsernamePwd(String dsName, String dsJndiName, String driverName, String connectionUrl, String dsMinPool, String dsMaxPool, String dsUsername, String dsPassword, String jta) {
        return installDatasource(dsName, dsJndiName, driverName, connectionUrl, dsMinPool, dsMaxPool, null, dsUsername, dsPassword, null, null, null, jta);
    }

    /**
     * Install a datasource using a security domain.
     *
     * @param dsName           name of the datasource
     * @param dsJndiName       JNDI name for the datasource
     * @param driverName       the name of the driver this datasource should use
     * @param connectionUrl    the URL for the database
     * @param dsMinPool        the minimum pool size
     * @param dsMaxPool        the maximum pool size
     * @param dsSecurityDomain the security domain to use to secure the datasource
     */
    public List<ModelNode> installDatasourceSecurityDomain(String dsName, String dsJndiName, String driverName, String connectionUrl, String dsMinPool, String dsMaxPool, String dsSecurityDomain, String jta) {
        return installDatasource(dsName, dsJndiName, driverName, connectionUrl, dsMinPool, dsMaxPool, dsSecurityDomain, null, null, null, null, null, jta);
    }

    /**
     * This method is used to add a datasource with minimal required information from the user.
     * For datasources that need to use a security domain for authentication
     *
     * @param dsName name for the datasource
     * @param dsJndiName jndi name for the datasource
     * @param driverName driver the datasource should use (must exist in the server already)
     * @param connectionUrl the URL for the database which should back this datasource
     * @param dsSecurityDomain the security domain used for authentication with this datasource
     * @return A List of ModelNodes denoting the success of each required command.
     */
    public List<ModelNode> installDatasourceSecurityDomainMinimal(String dsName, String dsJndiName, String driverName, String connectionUrl, String dsSecurityDomain) {
        return installDatasource(dsName, dsJndiName, driverName, connectionUrl, null, null, dsSecurityDomain, null, null, null, null, null, null);
    }

    /**
     * This method is used to add a datasource with minimal required information from the user. For datasources
     * that need to use a username + password authentication mechanism
     *
     * @param dsName name for the datasource
     * @param dsJndiName jndi name for the datasource
     * @param driverName driver the datasource should use (must exist in the server already)
     * @param connectionUrl the URL for the database which should back this datasource
     * @param dsUsername the username to authenticate with this datasource
     * @param dsPassword the password to use with the username for this datasource
     * @return A List of ModelNodes denoting the success of each required command.
     */
    public List<ModelNode> installDatasourceUserPwdMinimal(String dsName, String dsJndiName, String driverName, String connectionUrl, String dsUsername, String dsPassword) {
        return installDatasource(dsName, dsJndiName, driverName, connectionUrl, null, null, null, dsUsername, dsPassword, null, null, null, null);
    }

    /**
     * This method should only be called by the more specific public methods, in
     * order to more closely control the values being passed into this method.
     * Install a datasource using the supplied values. If xaProps is null, the
     * datasource will be added as a non-XA one. Additionally, if the
     * ServerCommands instance has an associated VaultSession, and a dsPassword
     * is specified, this password will be masked using the vault with the
     * vaultBlock = "ds_" and the vaultAttribute = "password"<br>
     *
     * @param dsName           name of the datasource
     * @param dsJndiName       jndiname of the datasource
     * @param driverName       the driver name this datasource should use
     * @param connectionUrl    URL of the database that this datasource should use
     * @param dsSecurityDomain Name of the security-domain this datasource should be secured
     *                         by. dsSecurityDomain == null iff dsUsername != null &&
     *                         dsPassword != null
     * @param dsUsername       username for the user to secure the datasource. dsUsername ==
     *                         null iff dsSecurityDomain != null
     * @param dsPassword       password for the user to secure the datasource. dsPassword ==
     *                         null iff dsSecurityDomain != null
     * @param dsMinPool        the minimum pool size for this datasource
     * @param dsMaxPool        the maximum pool size
     * @param xaProps          a map of XA properties where the key is the property name, and
     *                         the value associated with the key is the property value
     * @param dsXaRecoveryUser the user to be used for recovery in an XA datasource
     * @param dsXaRecoveryPass the password to be used with the recovery user for an XA
     *                         datasource
     * @return A List of ModelNodes denoting the result of each required command
     */
    private List<ModelNode> installDatasource(String dsName,
                                              String dsJndiName,
                                              String driverName,
                                              String connectionUrl,
                                              String dsMinPool,
                                              String dsMaxPool,
                                              String dsSecurityDomain,
                                              String dsUsername,
                                              String dsPassword,
                                              Map<String, String> xaProps,
                                              String dsXaRecoveryUser,
                                              String dsXaRecoveryPass,
                                              String jta) {

        boolean dsIsXa = false;
        String addDsCmd = "";
        List<ModelNode> result = new ArrayList<ModelNode>();

        // these situations can't be allowed to continue.
        if (dsName == null || dsJndiName == null || driverName == null) {
            result.add(getFailureResult("ServerCommands.installDatasource()", "Add data-source command missing required values, one of 'dsName, dsJndiName, driverName'."));
            return result;
        }
        if (connectionUrl == null && xaProps == null) {
            result.add(getFailureResult("ServerCommands.installDatasource()", "Add data-source command missing either xa properties or connection url."));
            return result;
        }
        if (dsSecurityDomain == null && dsUsername == null && dsPassword == null) {
            result.add(getFailureResult("ServerCommands.installDatasource()", "Add data-source command missing either a security-domain or a user-name / password pair."));
            return result;
        }

        // if xaProps is not null, it's safe to assume the datasource we're
        // adding is an XA datasource
        if (xaProps != null) {
            dsIsXa = true;
        }

        addDsCmd += "add --name=" + dsName + " --jndi-name=\"" + dsJndiName + "\" --driver-name=" + driverName;

        if (dsMinPool != null) {
            addDsCmd += " --min-pool-size=" + dsMinPool;
        }
        if (dsMaxPool != null) {
            addDsCmd += " --max-pool-size=" + dsMaxPool;
        }

        if (jta != null){
            addDsCmd += " --jta="+jta;
        }
        if (dsSecurityDomain != null) {
            addDsCmd += " --security-domain=" + dsSecurityDomain;
        } else {
            if (hasVault()) {
                // only attempt this if the dsPassword is actually being used
                if (dsPassword != null) {
                    dsPassword = maskPassword("datasource." + dsName, "password", dsPassword);
                }
            }
            addDsCmd += " --user-name=" + dsUsername + " --password=\"" + dsPassword + "\"";
        }

        addDsCmd = setUniqueDsElements(addDsCmd, driverName);

        if (dsIsXa) {
            addDsCmd += " --xa-datasource-properties=";
            for (String property : xaProps.keySet()) {
                if (xaProps.get(property) != null) {
                    addDsCmd += property
                            + ","
                            + xaProps.get(property) + ",";
                }
            }
            addDsCmd = addDsCmd.substring(0, addDsCmd.length() - 1); // to
            // remove
            // the
            // last
            // ","
            // from
            // the
            // xa
            // properties

            if (driverName.equals(JBossJDBCConstants.ibmJdbcName)) { // ibm needs special recovery
                // plugin elements
                addDsCmd += " --recovery-plugin-class-name=org.jboss.jca.core.recovery.ConfigurableRecoveryPlugin --recovery-plugin-properties=EnabledIsValid=false,IsValidOverride=false,EnableClose=false";
            } // recovery password stuff
            // If we are using a vault, vault the password:
            if (hasVault()) {
                if (dsXaRecoveryPass != null) {
                    dsXaRecoveryPass = maskPassword("datasource." + dsName,
                            "recoveryPassword", dsXaRecoveryPass);
                }
            }

            // If null, we just ignore these.
            if (dsXaRecoveryUser != null && dsXaRecoveryPass != null) { // if

                addDsCmd += " --recovery-username=" + dsXaRecoveryUser
                        + " --recovery-password=" + dsXaRecoveryPass;
            }
        } else {
            addDsCmd += " --connection-url=\"" + connectionUrl + "\"";
        }
        // submit this crazy command
        if (isDomain) {
            addDsCmd = " --profile=%s " + addDsCmd;
        }
        if (dsIsXa) {
            addDsCmd = "xa-data-source " + addDsCmd;
        } else {
            addDsCmd = "data-source " + addDsCmd;
        }
        // special case command, cannot use the submitCommand method, because
        // the submitCommand method appends things based upon isDomain; this
        // command instead
        // has the --profile= component.
        if (isDomain) {
            for (String profile : domainProfiles) {
                result.add(addToLoggerAndHandle(String.format(addDsCmd, profile)));
            }
        } else {
            result.add(addToLoggerAndHandle(addDsCmd));
        }

        // if there wasn't an error adding the datasource(s), enable them
        if (ServerCommandsHelper.findFailures(result).isEmpty()) {
            String enableCmd = "";
            if (isDomain){
                enableCmd = "/profile=%s";
            }
            if (dsIsXa) { // enable the datasource
                enableCmd = enableCmd + "/subsystem=datasources/xa-data-source=" + dsName + ":enable";
            } else {
                enableCmd = enableCmd + "/subsystem=datasources/data-source=" + dsName + ":enable";
            }
            if (isDomain){
                for (String profile : domainProfiles){
                    result.add(addToLoggerAndHandle(String.format(enableCmd, profile)));
                }
            } else {
                result.add(addToLoggerAndHandle(enableCmd));
            }
        }
        return result;
    }

    /**
     * Method adds all of the special Class names for different vendors.
     * Unfortunate but necessary special case logic.
     *
     * @param addDsCmd the command being used to create the datasource
     * @param driverName the database driver that the datasource will be backed by
     * @return the command string with added driver-dependent parameters
     */
    private String setUniqueDsElements(String addDsCmd, String driverName) {
        // --same-rm-override doesn't seem to exist anymore in jboss-cli.sh
        String dsExceptionSorter, dsValidChecker, dsStaleChecker;
        if (driverName.equals(JBossJDBCConstants.ibmJdbcName)) {
            // addDsCmd += " --same-rm-override=false";
            dsExceptionSorter = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2ExceptionSorter";
            dsValidChecker = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2ValidConnectionChecker";
            dsStaleChecker = "org.jboss.jca.adapters.jdbc.extensions.db2.DB2StaleConnectionChecker";

        } else if (driverName.equals(JBossJDBCConstants.sybaseJdbcName)) {
            dsExceptionSorter = "org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseExceptionSorter";
            dsValidChecker = "org.jboss.jca.adapters.jdbc.extensions.sybase.SybaseValidConnectionChecker";
            dsStaleChecker = null;

        } else if (driverName.equals(JBossJDBCConstants.mysqlJdbcName)) {
            dsExceptionSorter = "org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLExceptionSorter";
            dsValidChecker = "org.jboss.jca.adapters.jdbc.extensions.mysql.MySQLValidConnectionChecker";
            dsStaleChecker = null;

        } else if (driverName.equals(JBossJDBCConstants.postgresqlJdbcName)) {
            dsExceptionSorter = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter";
            dsValidChecker = "org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker";
            dsStaleChecker = null;

        } else if (driverName.equals(JBossJDBCConstants.microsoftJdbcName)) {
            // addDsCmd += " --same-rm-override=false";
            dsExceptionSorter = null;
            dsValidChecker = "org.jboss.jca.adapters.jdbc.extensions.mssql.MSSQLValidConnectionChecker";
            dsStaleChecker = null;

        } else if (driverName.equals(JBossJDBCConstants.oracleJdbcName)) {
            // addDsCmd += " --same-rm-override=false";
            dsExceptionSorter = "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter";
            dsValidChecker = "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker";
            dsStaleChecker = "org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker";
        } else {
            dsExceptionSorter = null;
            dsValidChecker = null;
            dsStaleChecker = null;
        }

        if (dsExceptionSorter != null) {
            addDsCmd += " --exception-sorter-class-name=" + dsExceptionSorter;
        }
        if (dsValidChecker != null) {
            addDsCmd += " --valid-connection-checker-class-name=" + dsValidChecker;
        }
        if (dsStaleChecker != null) {
            addDsCmd += " --stale-connection-checker-class-name=" + dsStaleChecker;
        }
        return addDsCmd;
    }

    /**
     * This method performs three tasks. 1. Creates a new LDAP connection using
     * the supplied ldapName, ldapPassword, ldapUrl, and ldapAuthDn
     * (Authentication Distinguished Name) 2. Creates a new Security Realm,
     * named ldapRealmName, which uses the newly created LDAP connection, along
     * with ldapBaseDn, ldapFilter, and ldapRecursive. 3. Applies this new
     * Security Realm to the management interface.
     *
     * @param ldapName the name for the ldap connection
     * @param ldapPassword the password to authenticate with the ldap server
     * @param ldapUrl the URL to the ldap server
     * @param ldapAuthDn the ldap query that denotes a user that is authorized to perform searches
     * @param ldapRealmName the name for the security realm which will use the ldap connection to check credentials
     * @param ldapBaseDn the ldap query that denotes the location to begin user searches
     * @param ldapRecursive indicates whether recursive searches should be performed
     * @param ldapFilter additional configuration to filter attributes for the username
     * @return a List of ModelNodes denoting the success or failure of each sub-step
     */

    public List<ModelNode> installLdap(String ldapName,
                                       String ldapPassword,
                                       String ldapUrl,
                                       String ldapAuthDn,
                                       String ldapRealmName,
                                       String ldapBaseDn,
                                       String ldapFilter,
                                       String ldapRecursive,
                                       boolean isAdvancedFilter) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        // we check each sub-command for success individually, because we can short-circuit and not perform unnecessary work

        result.addAll(createLdapConn(ldapName, ldapPassword, ldapUrl, ldapAuthDn));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result; // fail fast

        result.addAll(createLdapSecurityRealm(ldapName, ldapRealmName, ldapBaseDn, ldapFilter, ldapRecursive, isAdvancedFilter));

        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result;

        result.addAll(installLdapToInterfaces(ldapRealmName));
        return result;

        /*if (!Operations.isSuccessfulOutcome(retVal))
            return retVal;
		retVal = createLdapSecurityRealm(ldapName, ldapRealmName, ldapBaseDn, ldapFilter, ldapRecursive, isAdvancedFilter);
        if (!Operations.isSuccessfulOutcome(retVal))
			return retVal;
        retVal = installLdapToInterfaces(ldapRealmName);
        return retVal;*/
    }

    /**
     * Creates the connection that will be referenced by the new security realm.
     * Contains the credentials for connecting to the ldap server to perform
     * searches
     *
     * @param ldapName name of the connection. is arbitrary
     * @param ldapPwd  credential used for authentication with the LDAP server
     * @param ldapUrl  location of the LDAP server, including port
     * @param ldapDn   the user to authenticate to the LDAP server with
     */

    public List<ModelNode> createLdapConn(String ldapName, String ldapPwd, String ldapUrl, String ldapDn) {
        List<ModelNode> result = new ArrayList<ModelNode>();

        String addLdapConnCmd = "/core-service=management/ldap-connection=" + ldapName + "/:add(search-credential=\"" + ldapPwd + "\",url=\"" + ldapUrl + "\",search-dn=\"" + ldapDn + "\")";

        result.add(submitCommand(addLdapConnCmd));
        return result;
    }

    /**
     * Method to create a new ldap security realm, and add the required
     * characteristics to it. This method performs two steps: The creation of an
     * LDAP security realm, and addition of the desired characteristics to the
     * newly created LDAP realm.
     *
     * @param ldapName      name of the ldap connection this security realm should use
     * @param ldapRealmName the name of the security realm
     * @param ldapBaseDn    the distinguished name that the LDAP realm should begin it's
     *                      search for users at
     * @param ldapRecursive indicate whether or not recursive search should be used
     * @param ldapFilter    indicates, using either an advanced filter or a simple
     *                      username-attribute, which value in the ldap server to use as a
     *                      username
     * @return 0: success of both operations (create and add) 1: failure of
     * create operation 2: success of create operation, failure of add
     * operation
     */

    public List<ModelNode> createLdapSecurityRealm(String ldapName, String ldapRealmName, String ldapBaseDn, String ldapFilter, String ldapRecursive, boolean isAdvancedFilter) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        String createLdapSecRealmCmd = "/core-service=management/security-realm=\"" + ldapRealmName + "\":add";
        String addLdapSecRealmCmd = "/core-service=management/security-realm=\"" + ldapRealmName + "\"/authentication=ldap:add(base-dn=\"" + ldapBaseDn + "\", recursive=\"" + ldapRecursive
                + "\", connection=\"" + ldapName + "\"";

        if (isAdvancedFilter) {
            addLdapSecRealmCmd += ",advanced-filter=\"" + ldapFilter + "\")";
        } else {
            addLdapSecRealmCmd += ",username-attribute=\"" + ldapFilter + "\")";
        }

        //Note: restructuring here to use ModelNode; the old methodology has been preserved
/*		submitCommand(createLdapSecRealmCmd);
		if (exitCode != 0)
			return 1;

		submitCommand(addLdapSecRealmCmd);
		if (exitCode != 0)
			return 2;*/

        result.add(submitCommand(createLdapSecRealmCmd));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            // we return at this point, because if the creation of the realm fails, the next command can't possibly succeed
            return result;

        result.add(submitCommand(addLdapSecRealmCmd));
        return result; // we can simply return the ModelNode, success or fail here, since the caller will be able to handle it correctly
    }


    public List<ModelNode> writeSecurityRealmAttribute(String interfaceName, String realmName) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        ModelNode check;
        String checkCmd = "/core-service=management:read-children-names(child-type=management-interface)";
        check = submitCommand(checkCmd);
        for (ModelNode c : check.get("result").asList()){
            // only write the security-realm attribute if the management-interface exists.
            if (c.asString().equals(interfaceName)){
        String writeSecurityRealmCmd = "/core-service=management/management-interface=" + interfaceName + "/:" + writeAttribute("security-realm", "\"" + realmName + "\"");
        result.add(submitCommand(writeSecurityRealmCmd));
            }
        }
        return result;
    }

    /**
     * Writes the security-realm attribute of the http-interface to the value in ldapRealmName
     *
     * @param ldapRealmName the realm to add to the http interface
     * @return the exit code of the operation on the command context
     */
    private List<ModelNode> installLdapToHttpInterface(String ldapRealmName) {
        return writeSecurityRealmAttribute("http-interface", ldapRealmName);
    }

    /**
     * Writes the security-realm attribute of the native-interface to the value in ldapRealmName
     *
     * @param ldapRealmName the realm to add to the native interface
     * @return the exit code of the operation on the command context
     */
    private List<ModelNode> installLdapToNativeInterface(String ldapRealmName) {
        return writeSecurityRealmAttribute("native-interface", ldapRealmName);
    }

    /**
     * Writes the attribute of the http-interface and native-interface to the security-realm named in ldapRealmName
     *
     * @param ldapRealmName the name of the realm to add to the http / native interfaces
     * @return A List of ModelNodes denoting the success or failure of this step
     */
    public List<ModelNode> installLdapToInterfaces(String ldapRealmName) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        result.addAll(installLdapToHttpInterface(ldapRealmName));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result;
        result.addAll(installLdapToNativeInterface(ldapRealmName));
        return result;
    }

    /**
     * Allows the user to configure logging on the CommandContext.
     */
    public void setContextLoggingConfig(String file) {
        System.setProperty("logging.configuration", file);
    }

    /**
     * Writes out a Log of the raw commands used by the CommandContext. This
     * allows users to see what commands were executed on their host. If
     * setLogFileLocation already exists, this method will overwrite it.
     *
     * @throws IOException either the file location has not been specified, or writing
     *                     to the given location failed
     */

    public void writeLogFile() throws FileNotFoundException{
        // if setLogFileLocation has been called
        if (logFilePath != null) {
            File logFile = new File(logFilePath);
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(logFile));
                for (String line : naiveLogger) {
                    bw.write(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            throw new FileNotFoundException("writeLogFile: the logFileLocation has not been set");
        }
    }

    /**
     * Issues a reload command to the currently connected host.
     */
    public ModelNode reloadHost() {
        return submitCommand(RELOAD_CMD);
    }

    /**
     * Issues a shutdown command to the currently connected host.
     */

    public ModelNode shutdownHost() {
        return submitCommand(SHUTDOWN_CMD);
    }

    /**
     * Utility method for use when the ServerCommands instance is connected to a domain host, but the
     * ServerCommands instance may not be aware of this (not created through the *DomainSession* factory methods. <br/>
     * Explicitly calls shutdown on a domain host. This will result in failure if used on a standalone host.
     *
     * @return A ModelNode denoting the success or failure of the shutdown operation
     */
    public ModelNode shutdownDomainHost() {
        return submitCommand(String.format(DOMAIN_CMD_PREFIX, getDomainHostname()) + SHUTDOWN_CMD);
    }

    /**
     * Submits the current batch.
     *
     * @throws BatchNotActiveException if there is no active batch
     * @throws CommandLineException
     */
    public ModelNode runBatch() throws BatchNotActiveException, CommandLineException, BatchIsEmptyException {
        if (naiveLogger.isEmpty()) {
            throw new BatchIsEmptyException("ServerCommands: The batch is empty.");
        }
        if (context.getControllerHost() == null) {
            throw new CommandLineException("ServerCommands: You are not connected to any host. runBatch() can only be run while connected");
        }
        if (context.isBatchMode()) {
            return submitCommand(RUN_BATCH_CMD);
        } else {
            throw new BatchNotActiveException("ServerCommands: there is no currently active batch");
        }
    }

    /**
     * Submits a batch with the given name. This will run the currently active batch if there is no held back batch with
     * the same name
     *
     * @param name the name of the batch with the given name
     * @return a ModelNode denoting the success or the failure of the batch
     * @throws BatchIsEmptyException
     * @throws CommandLineException
     * @throws BatchNotActiveException
     */
    public ModelNode runBatch(String name) throws BatchNotActiveException, CommandLineException, BatchIsEmptyException {
        context.getBatchManager().activateHeldbackBatch(name);
        return runBatch();
    }

    /**
     * Checks if a given batch exists as either the active or held back batch
     */
    public boolean isStoredBatch(String name) {
        return context.getBatchManager().isHeldback(name);
    }

    /**
     * Sets the transaction manager's default timeout to the given value
     * *
     *
     * @param timeout the desired new timeout
     */

    public ModelNode setTransactionManagerTimeout(int timeout) {
        String setTimeoutCmd = "";
        return submitCommand(setTimeoutCmd);
    }

    /**
     * Helper method to create a logger level with only parentHandlers set to
     * either false or true
     *
     * @param name              name of the logger level
     * @param level             name of the logger level
     * @param useParentHandlers true or false; useParentHandlers or not
     * @return A List of ModelNodes denoting the success or failure of each operation
     */

    public List<ModelNode> createLoggerLevel(String name, String level, String useParentHandlers) {
        boolean useParents = (useParentHandlers != null) && useParentHandlers.equals("true");
        return createLoggerLevel(name, level, null, null, null, null, useParents);
    }

    // TODO: add more helpers

    /**
     * Adds a new logger level to the connected server. All parameters can be
     * null except for the name, which must be supplied. Non-zero exit status
     * indicates an error has occurred.
     *
     * @param name              Name of the new logger level
     * @param level             level of the new logger level (DEBUG, TRACE etc)
     * @param category          the category of the logger
     * @param filter            filter for the logger
     * @param filterSpec        specification of the filter for the logger
     * @param handlers          handlers for the logger
     * @param useParentHandlers true if the parent handlers should be used for the new logger;
     *                          false otherwise
     */

    public List<ModelNode> createLoggerLevel(String name, String level, String category, String filter, String filterSpec, String handlers, boolean useParentHandlers) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (name == null) {
            result.add(getFailureResult("ServerCommands.createLoggerLevel()", "The 'name' for the new logger was null."));
            return result;
        }
        String createLoggerCmd = "/subsystem=logging/logger=" + name + ":add(";

        if (level != null) {
            createLoggerCmd += "level=" + level + ",";
        }
        if (category != null) {
            createLoggerCmd += "category=" + category + ",";
        }
        if (filter != null) {
            createLoggerCmd += "filter=" + filter + ",";
        }
        if (filterSpec != null) {
            createLoggerCmd += "filter-spec=" + filterSpec + ",";
        }
        if (handlers != null) {
            createLoggerCmd += "handlers=" + handlers + ",";
        }
        if (useParentHandlers) {
            createLoggerCmd += "use-parent-handlers=true";
        } else {
            createLoggerCmd += "use-parent-handlers=false";
        }

        result.add(submitCommand(createLoggerCmd));
        return result;
    }

    // this method is designed to enable use without knowledge of the underlying
    // CLI API

    /**
     * Adds a security-domain with the given options to the server this context
     * is connected to.
     *
     * @param domainName     required. Name of the security-domain
     * @param cacheType      optional. Can take two possible values: default or infinispan.
     *                       Other values will result in no cache-type attribute being
     *                       defined.
     * @param authenCode     optional. Required to define the authentication element
     * @param authenFlag     optional, required if authenCode is provided. Contains one of
     *                       the following values: required, requisite, sufficient,
     *                       optional
     * @param authenOptions  optional. Contains a map of (name,value)s for filling the
     *                       module-options element of the authentication element
     * @param authorCode     optional. Required to define the authorization element
     * @param authorFlag     optional, required if authorCode is provided. Contains one of
     *                       the following values: required, requisite, sufficient,
     *                       optional
     * @param authorOptions  optional. Contains a map of (name,values)s for filling the
     *                       module-options element of the authorization element
     * @param mappingCode    optional. Required to define the mapping element
     * @param mappingFlag    optional, required if mappingCode is provided. Contains one of
     *                       the following values: required, requisite, sufficient,
     *                       optional
     * @param mappingOptions optional. Contains a map of (name,value)s for filling the
     *                       module-options element of the mapping element
     * @return A List of ModelNodes denoting the success or failure of each sub-operation
     */
    public List<ModelNode> addSecurityDomain(String domainName,
                                             String cacheType,
                                             List<String> authenCode,
                                             List<String> authenFlag,
                                             List<Map<String, String>> authenOptions,
                                             List<String> authorCode,
                                             List<String> authorFlag,
                                             List<Map<String, String>> authorOptions,
                                             List<String> mappingCode,
                                             List<String> mappingFlag,
                                             List<Map<String, String>> mappingOptions,
                                             Map<String, String> jsseAttrs,
                                             Map<String, String> jsseKeystoreAttrs,
                                             Map<String, String> jsseKeystoreManagerAttrs,
                                             Map<String, String> jsseTruststoreAttrs,
                                             Map<String, String> jsseTruststoreManagerAttrs,
                                             Map<String, String> jsseAdditionalProps) {
        List<ModelNode> result = new ArrayList<ModelNode>();

        result.addAll(createSecurityDomain(domainName, cacheType));
        if (!ServerCommandsHelper.findFailures(result).isEmpty()) {
            return result;
        }

        if (authenCode != null && authenFlag != null && !authenCode.isEmpty() && !authenFlag.isEmpty()) {
            result.addAll(addSecurityDomainAuthentication(domainName, authenCode, authenFlag, authenOptions));
            if (!ServerCommandsHelper.findFailures(result).isEmpty()) {
                return result;
            }
        }

        if (authorCode != null && authorFlag != null && !authorCode.isEmpty() && !authorFlag.isEmpty()) {
            result.addAll(addSecurityDomainAuthorization(domainName, authorCode, authorFlag, authorOptions));
            if (!ServerCommandsHelper.findFailures(result).isEmpty()) {
                return result;
            }
        }

        if (mappingCode != null && mappingFlag != null && !mappingCode.isEmpty() && !mappingFlag.isEmpty()) {
            result.addAll(addSecurityDomainMapping(domainName, mappingCode, mappingFlag, mappingOptions));
            if (!ServerCommandsHelper.findFailures(result).isEmpty()) {
                return result;
            }
        }

        // seems sketchy. needs improvement
        if (jsseAttrs != null || jsseKeystoreAttrs != null || jsseKeystoreManagerAttrs != null || jsseTruststoreAttrs != null || jsseTruststoreManagerAttrs != null) {
            result.addAll(addSecurityDomainJsse(domainName, jsseAttrs, jsseKeystoreAttrs, jsseKeystoreManagerAttrs, jsseTruststoreAttrs, jsseTruststoreManagerAttrs, jsseAdditionalProps));
            if (!ServerCommandsHelper.findFailures(result).isEmpty()) {
                return result;
            }
        }

        return result;
    }

    /**
     * This method takes a bunch of maps. Unfortunately, given the size of the
     * jsse element, I can't come up with a better solution. However, I have not
     * given it much though, perhaps it will be reworked at a later stage. The
     * maps *must* be formatted like this: each Key is a valid attribute within
     * the CLI; each Value is a valid value for it's associated attribute. For
     * the installer, this will be checked in the panel and guaranteed. For
     * others utilizing this class, you will have to sanitize the input.
     * See https://docs.jboss.org/author/display/AS71/Security+subsystem+configuration for a complete definition of such
     * valid mappings
     *
     * @param jsseAttrs A mapping of valid JSSE attributes to their values
     * @param jsseKeystoreAttrs A mapping of valid Keystore attributes to their values
     * @param jsseKeystoreManagerAttrs A mapping of valid KeystoreManager attributes to their values
     * @param jsseTruststoreAttrs A mapping of valid Truststore attributes to their values
     * @param jsseTruststoreManagerAttrs A mapping of valid TruststoreManager attributes to their values
     * @return A List of ModelNodes that denotes the success or failure of each sub-operation
     */
    private List<ModelNode> addSecurityDomainJsse(String domainName,
                                                  Map<String, String> jsseAttrs,
                                                  Map<String, String> jsseKeystoreAttrs,
                                                  Map<String, String> jsseKeystoreManagerAttrs,
                                                  Map<String, String> jsseTruststoreAttrs,
                                                  Map<String, String> jsseTruststoreManagerAttrs,
                                                  Map<String, String> jsseAdditionalProps) {

        String addSecurityDomainJsseCmd = "/subsystem=security/security-domain=" + domainName + "/jsse=classic:add("; // initial
        // command
        List<ModelNode> result = new ArrayList<ModelNode>();

        if (jsseAttrs != null) {
            for (Entry<String, String> entry : jsseAttrs.entrySet()) {
                // assume that this will result in a valid command. in the
                // installer's case, it is guaranteed to
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    addSecurityDomainJsseCmd += entry.getKey() + "=\"" + entry.getValue() + "\",";
                }
            }
        }

        if (jsseKeystoreAttrs != null) {
            addSecurityDomainJsseCmd += listString(domainName, "keystore", jsseKeystoreAttrs);

            if (jsseKeystoreManagerAttrs != null) {
                addSecurityDomainJsseCmd += listString(domainName, "key-manager", jsseKeystoreManagerAttrs);

            }
        }

        if (jsseTruststoreAttrs != null) {
            addSecurityDomainJsseCmd += listString(domainName, "truststore", jsseTruststoreAttrs);

            if (jsseTruststoreManagerAttrs != null) {
                addSecurityDomainJsseCmd += listString(domainName, "trust-manager", jsseTruststoreManagerAttrs);
            }
        }

        result.add(submitCommand(addSecurityDomainJsseCmd));

        return result;

    }

    /**
     * Creates a properly formatted LIST type parameter for CLI commands.
     *
     * @param listName the name of the list parameter
     * @param attributes the mapping of key=value that the LIST parameter should contain
     * @return a properly formatted String representing a valid LIST type CLI parameter
     */

    private String listString(String domainName, String listName, Map<String, String> attributes) {
        String returnValue = listName + "={";
        for (Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                String value = entry.getValue();
                //TODO: not a very good long term solution. passable for now.
                if (entry.getKey().contains("password")) {
                    // attempt to vault this value
                    if (hasVault()) {
                        value = maskPassword(domainName + "." + listName, "password", value);
                    }
                }
                returnValue += entry.getKey()
                        + "=\""
                        + value.replaceAll("\\\\",
                        "/") + "\",";
            }
        }
        returnValue += "},";
        if (returnValue.equals(listName + "={}")) {
            return "";
        } else {// the list is empty
            return returnValue;
        }
    }

    /**
     * Creates a new security domain security domain with the given attributes
     */

    List<ModelNode> createSecurityDomain(String domainName, String cacheType) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        String addSecurityDomainCmd;
        if (cacheType.equals("infinispan") || cacheType.equals("default")) {
            addSecurityDomainCmd = "/subsystem=security/security-domain=" + domainName + ":add(cache-type=" + cacheType + ")";
        } else {
            addSecurityDomainCmd = "/subsystem=security/security-domain=" + domainName + ":add()";
        }

        result.add(submitCommand(addSecurityDomainCmd));
        return result;
    }

    /**
     * Helper to complete the security-domain list command
     *
     * @param codes         list of codes for this command
     * @param flags         list of flags or types for this command
     * @param moduleOptions list of module-options for this command
     * @return  The completed String representing a jboss-cli.sh command to add a new security-domain
     */
    private String completeSecurityDomainCmd(List<String> codes, List<String> flags, List<Map<String, String>> moduleOptions, String typeOrFlag) {
        String cmd = "";

        for (int i = 0; i < codes.size(); i++) {
            if (i > 0) cmd += ",";
            cmd += "{\"code\"=>\"" + codes.get(i) + "\",\"" + typeOrFlag + "\"=>\"" + flags.get(i) + "\""; // add the code and the flag
            if (moduleOptions != null) {
                Map<String, String> moduleOption = moduleOptions.get(i);
                cmd += ",\"module-options\"=>[";
                for (String name : moduleOption.keySet()) {
                    cmd += "(\"" + name + "\"=>\"" + moduleOption.get(name) + "\"),";
                }
                cmd += "]}";
            } else {
                cmd += "}";
            }
        }
        cmd += "])";
        return cmd;
    }

    /**
     * Convenience method for the authorization / authentication case
     *
     * @param codes list of codes to add
     * @param flags list of flags for each code to add
     * @param moduleOptions additional arbitrary options
     * @return the formatted jboss-cli.sh String to add the security-domain
     */
    private String completeSecurityDomainCmd(List<String> codes, List<String> flags, List<Map<String, String>> moduleOptions) {
        return completeSecurityDomainCmd(codes, flags, moduleOptions, "flag");
    }

    /**
     * Add the authentication element to a given security domain
     *
     * @param codes         contains a short name or fully classified class name from a
     *                      long list here: https://docs.jboss.org/author/display/AS71/Security+subsystem+configuration
     * @param flags         contains the value required, requisite, sufficient, or
     *                      optional
     * @param moduleOptions contains a map of name,value pairs for the module-option
     *                      elements, if any TODO: find out why the authentication-jaspi element is not available from jboss-cli.sh
     * @return A List of ModelNodes that denote the success or failure of each sub-operation
     */
    List<ModelNode> addSecurityDomainAuthentication(String domainName, List<String> codes, List<String> flags, List<Map<String, String>> moduleOptions) {
        String addSecurityDomainAuthCmd;
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (codes.size() == 0) {
            result.add(getFailureResult("ServerCommands.addSecurityDomainAuthentication()", "Code element is empty."));
            return result;
        }
        if (codes.size() != flags.size() || flags.size() != moduleOptions.size()) {
            result.add(getFailureResult("ServerCommands.addSecurityDomainAuthentication()", "Mismatch of code / flags / options sizes."));
            return result;
        }

        addSecurityDomainAuthCmd = "/subsystem=security/security-domain=" + domainName + "/authentication=classic:add(login-modules=[";
        for (int i = 0; i < codes.size(); i++) {
            if (invalidCodes(codes.get(i), validAuthenticationCodes)) {
                result.add(getFailureResult("ServerCommands.addSecurityDomainAuthentication()", String.format("The value for authentication code \"%s\" is not valid.", codes.get(i))));
                return result;
            }
            if (invalidCodes(flags.get(i), validFlags)) {
                result.add(getFailureResult("ServerCommands.addSecurityDomainAuthentication()", String.format("The value for flag \"%s\" is not valid. It must be one of: \"Required\", \"Requisite\", \"Sufficient\", \"Optional\"", flags.get(i))));
                return result;
            }
        }
        addSecurityDomainAuthCmd += completeSecurityDomainCmd(codes, flags, moduleOptions);
        result.add(submitCommand(addSecurityDomainAuthCmd));
        return result;
    }

    /**
     * Adds the authorization element to the security domain
     *
     * @param domainName    name of the security domain to add authorization to.
     * @param codes         list of codes for the authorization element. See documentation for
     *                      valid values.
     * @param flags         list of flags for the authorization element (one of required,
     *                      requisite, sufficient, or optional)
     * @param moduleOptions a map of (name, value)s for the module-options elements, if
     *                      desired. This can be null.
     * @return A List of ModelNodes that denote the success or failure of each sub-operation
     */
    List<ModelNode> addSecurityDomainAuthorization(String domainName, List<String> codes, List<String> flags, List<Map<String, String>> moduleOptions) {
        String addSecurityDomainAuthCmd;
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (codes.size() == 0) {
            result.add(getFailureResult("ServerCommands.addSecurityDomainAuthorization()", "Code element is empty."));
            return result;
        }
        if (codes.size() != flags.size() || flags.size() != moduleOptions.size()) {
            result.add(getFailureResult("ServerCommands.addSecurityDomainAuthorization()", "Mismatch of code / flags / options sizes."));
            return result;
        }

        addSecurityDomainAuthCmd = "/subsystem=security/security-domain=" + domainName + "/authorization=classic:add(policy-modules=[";
        for (int i = 0; i < codes.size(); i++) {
            if (invalidCodes(codes.get(i), validAuthorizationCodes)) {
                result.add(getFailureResult("ServerCommands.addSecurityDomainAuthorization()", String.format("The value for authentication code %s is not valid.", codes.get(i))));
                return result;
            }
            if (invalidCodes(flags.get(i), validFlags)) {
                result.add(getFailureResult("ServerCommands.addSecurityDomainAuthorization()", String.format("The value for flag %s is not valid.", flags.get(i))));
                return result;
            }
        }
        addSecurityDomainAuthCmd += completeSecurityDomainCmd(codes, flags, moduleOptions);
        result.add(submitCommand(addSecurityDomainAuthCmd));
        return result;
    }

    /**
     * A helper method for calling addSecurityDomain with only authentication options
     * @param domainName
     * @param cacheType
     * @param authenCodes
     * @param authenFlags
     * @param authenOptions
     */
    public List<ModelNode> addSecurityDomainAuthenOnly(String domainName, String cacheType, List<String> authenCodes, List<String> authenFlags, List<Map<String, String>> authenOptions) {
        return addSecurityDomain(domainName, cacheType, authenCodes, authenFlags, authenOptions, null, null, null, null, null, null, null, null, null, null, null, null);

    }
    public List<ModelNode> addSecurityDomainAuthorOnly(String domainName, String cacheType, List<String> authorCodes, List<String> authorFlags, List<Map<String,String>> authorOptions){
        return addSecurityDomain(domainName, cacheType, null, null, null, authorCodes, authorFlags, authorOptions, null, null, null, null, null, null, null, null, null);
    }

    public List<ModelNode> addSecurityDomainMappingOnly(String domainName, String cacheType, List<String> mappingCodes, List<String> mappingFlags, List<Map<String,String>> mappingOptions){
        return addSecurityDomain(domainName, cacheType, null, null, null, null, null, null, mappingCodes, mappingFlags, mappingOptions, null, null, null, null, null, null);
    }
    /**
     * Adds the mapping element to the given security domain
     *
     * @param domainName    name of the domain to add the mapping element to.
     * @param codes         code for the mapping-module. See documentation for valid
     *                      values.
     * @param types         flag for the mapping-module (one of required, requisite,
     *                      sufficient, optional)
     * @param moduleOptions Map of (name,value)s for module-options entries.
     * @return A List of ModelNodes that denote the success or failure of each sub-operation
     */
    List<ModelNode> addSecurityDomainMapping(String domainName, List<String> codes, List<String> types, List<Map<String, String>> moduleOptions) {
        String addSecurityDomainMappingCmd;
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (codes.size() == 0) {
            result.add(getFailureResult("ServerCommands.addSecurityDomainMapping()", "Code element is empty."));
            return result;
        }
        if (codes.size() != types.size() || types.size() != moduleOptions.size()) {
            result.add(getFailureResult("ServerCommands.addSecurityDomainMapping()", "Mismatch of code / flags / options sizes."));
            return result;
        }
        addSecurityDomainMappingCmd = "/subsystem=security/security-domain=" + domainName + "/mapping=classic:add(mapping-modules=[";
        for (int i = 0; i < codes.size(); i++) {
            if (invalidCodes(codes.get(i), validMappingCodes)) {
                result.add(getFailureResult("ServerCommands.addSecurityDomainMapping()", String.format("The value for authentication code \"%s\" is not valid.", codes.get(i))));
                return result;
            }
            if (invalidCodes(types.get(i), validTypes)) {
                result.add(getFailureResult("ServerCommands.addSecurityDomainMapping()", String.format("The value for flag \"%s\" is not valid. It must be one of: \"principal\", \"credential\", \"role\", \"attribute\"", types.get(i))));
                return result;
            }
        }
        addSecurityDomainMappingCmd += completeSecurityDomainCmd(codes, types, moduleOptions, "type");
        result.add(submitCommand(addSecurityDomainMappingCmd));
        return result;
    }

    /**
     * Helper method to help validate the code attribute of the authentication
     * element of security-domains For the list of valid values, see <a href=
     * "https://docs.jboss.org/author/display/AS71/Security+subsystem+configuration#Securitysubsystemconfiguration-securitydomains"
     * >here</a>
     *
     * @param code  the string value in question
     * @param codes the array containing the valid values for what we're checking
     * @return true upon valid result, false otherwise
     */
    private boolean invalidCodes(String code, String[] codes) {
        for (String a : codes) {
            if (code.equals(a)) {
                return false;
            }
        }
        return true; // if we get here, the code must be invalid.

    }

    /**
     * Modifies the redirect port of the http web connector
     * TODO: return map?
     */

    public ModelNode modifyHttpRedirectPort(String port) {
        String modifyPortCmd = "/subsystem=web/connector=http:" + writeAttribute("redirect-port", port);

        return submitCommand(modifyPortCmd);
    }

    /**
     * Disables the standard welcome screen of the AS
     * TODO: return map?
     */
    public ModelNode disableWelcomeScreen() {
        String disableWelcomeCmd = "/subsystem=web/virtual-server=default-host:" + writeAttribute("enable-welcome-root", "false");

        return submitCommand(disableWelcomeCmd);
    }

    /**
     * RHQ specific modification of smtp port.
     * TODO: return map?
     */

    public ModelNode writeSmtpPort(String port) {
        String writeSmtpPortCmd = "/socket-binding-group=full-sockets/remote-destination-outbound-socket-binding=mail-smtp:" + writeAttribute(port, "${rhq.server.email.smtp-port:25}");

        return submitCommand(writeSmtpPortCmd);
    }

    /**
     * Helper method to add the required string to modify an attribute. Used to
     * reduce code duplication
     */
    private String writeAttribute(String name, String value) {
        return "write-attribute(name=" + name + ", value=" + value + ")";
    }

    /**
     * Add a JMS queue with all options available.
     *
     * @param address  the address of this queue
     * @param durable  should the queue be durable or not
     * @param entries  a list of entries for the queue
     * @param selector the selector for the queue.
     */

    public List<ModelNode> addJmsQueue(String address, List<String> entries, boolean durable, List<String> headers, String selector) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (!isDomain) {
            result.add( getFailureResult("ServerCommands.addJmsQueue()", "A JMS queue cannot be added to a non-domain server."));
            return result; // we cannot add a JMS queue to a non-domain server. This seems highly dubious
        }

        if (address == null) {
            result.add(getFailureResult("ServerCommands.addJmsQueue()", "The 'address' was null."));
            return result;
        }

        if (entries == null || entries.size() == 0) {
            result.add(getFailureResult("ServerCommands.addJmsQueue()", "There were no entries for the 'entries' element."));
            return result; // can't add a queue with no entries.
        }

        // TODO: domainProfiles is definitely a bug here.
        String addJmsQueueCmd = "jms-queue --profile=%s add --queue-address=" + address + " --entries=";

        // add entry list
        for (String entry : entries) {
            addJmsQueueCmd += entry + ",";
        }

        if (durable) { // we add this explicitly. it may not be needed, but it's
            // more verbose than to have no element for durable =
            // false
            addJmsQueueCmd += " --durable=true";
        } else {
            addJmsQueueCmd += " --durable=false";
        }

        // headers
        if (headers != null) {
            for (String header : headers) {
                // FIXME: add the headers. not sure how to do this yet
                // TODO: figure it out
            }
        }

        // TODO: ask someone about what format these selectors should be in
        if (selector != null) {
            addJmsQueueCmd += " --selector=" + selector;
        }

        // special case; cannot use submitCommand method
        for (String domain : domainProfiles) {
            naiveLogger.add(String.format(addJmsQueueCmd, domain));
            result.add(addToLoggerAndHandle(addJmsQueueCmd));
        }
        return result;
    }

    /**
     * Adds an infinispan queue with the given values
     */
    public List<ModelNode> addInfinispanCache(String name, String jndiName, String localCacheName, String transactionMode, String evictionStrategy, String evictionMaxEntries, String expirationMaxIdle) {
        List<ModelNode> result = new ArrayList<ModelNode>();

        result.addAll(createInfinispanContainer(name, jndiName));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result;

        result.addAll(addInfinispanLocalCache(name, localCacheName));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result;

        result.addAll(addInfinispanEviction(name, localCacheName, evictionStrategy, evictionMaxEntries));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result;

        result.addAll(addInfinispanTransaction(name, localCacheName, transactionMode));
        if (!ServerCommandsHelper.findFailures(result).isEmpty())
            return result;

        result.addAll(addInfinispanExpiration(name, localCacheName, expirationMaxIdle));
        return result;
    }

    private List<ModelNode> createInfinispanContainer(String name, String jndiName) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (name == null || jndiName == null) {
            result.add(getFailureResult("ServerCommands.createInfinispanContainer()", "Either the 'name' or 'jndiName' element were null."));
            return result; // fail immediately
        }
        String addInfinispanContainerCmd = "/subsystem=infinispan/cache-container=" + name + ":add(jndi-name=\"" + jndiName + "\")";

        result.add(submitCommand(addInfinispanContainerCmd));
        return result;
    }

    private List<ModelNode> addInfinispanLocalCache(String name, String localCacheName) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (localCacheName == null) {
            result.add(getFailureResult("ServerCommands.addInfinispanLocalCache()", "The local-cache attribute was null."));
            return result;
        }
        String addInfinispanLocalCacheCmd = "/subsystem=infinispan/cache-container=" + name + "/local-cache=" + localCacheName + ":add()";

        result.add(submitCommand(addInfinispanLocalCacheCmd));
        return result;
    }

    // TODO: add validation on the evictionStrategy string.
    // TODO: add validation for all possible options
    private List<ModelNode> addInfinispanEviction(String name, String localCacheName, String evictionStrategy, String evictionMaxEntries) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (evictionMaxEntries != null) {
            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(evictionMaxEntries);
            } catch (NumberFormatException e) {
                result.add(getFailureResult("ServerCommands.addInfinispanEviction()", "The value for the 'max-entries' element was not a number."));
                return result; // fail out of any strings that need to be integers are
                // not, in fact, integers.
            }
        }

        String addInfinispanEvictionCmd = "/subsystem=infinispan/cache-container=" + name + "/local-cache=" + localCacheName + "/eviction=EVICTION:add(strategy=" + evictionStrategy + ",max-entries="
                + evictionMaxEntries + ")";

        result.add(submitCommand(addInfinispanEvictionCmd));
        return result;
    }

    // TODO: add validations on the transaction mode string
    // TODO: add parameters for all possible options
    private List<ModelNode> addInfinispanTransaction(String name, String localCacheName, String transactionMode) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        String addInfinispanTransactionCmd = "/subsystem=infinispan/cache-container=" + name + "/local-cache=" + localCacheName + "/transaction=TRANSACTION:add(mode=" + transactionMode + ")";

        result.add(submitCommand(addInfinispanTransactionCmd));
        return result;
    }

    // TODO: add parameters for all possible options
    private List<ModelNode> addInfinispanExpiration(String name, String localCacheName, String expirationMaxIdle) {
        List<ModelNode> result = new ArrayList<ModelNode>();
        if (expirationMaxIdle != null) {
            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(expirationMaxIdle);
            } catch (NumberFormatException e) {
                result.add(getFailureResult("ServerCommands.addInfinispanExpiration()", "The value for the 'max-idle' element was not a number."));
                return result;
            }
        }

        String addInfinispanExpirationCmd = "/subsystem=infinispan/cache-container=" + name + "/local-cache=" + localCacheName + "/expiration=EXPIRATION:add(max-idle=" + expirationMaxIdle + ")";

        result.add(submitCommand(addInfinispanExpirationCmd));
        return result;
    }


    public void setLogger(Logger logger) {
        this.logger = logger;
        //TODO: is this string appropriate here? Also, possible for more efficiency?
        logger.info("Running commands from: " + logger.getName());
    }

    /**
     * simple class to indicate that a Batch is not active!
     *
     * @author thauser
     */
    public class BatchNotActiveException extends Exception {
        private static final long serialVersionUID = -5463741947772468327L;

        public BatchNotActiveException(String m) {
            super(m);
        }

        public BatchNotActiveException() {
            super();
        }
    }

    public class BatchIsEmptyException extends Exception {
        /**
         *
         */
        private static final long serialVersionUID = 8465278229646760882L;

        public BatchIsEmptyException() {
            super();
        }

        public BatchIsEmptyException(String s) {
            super(s);
        }
    }

    /**
     * Reads java properties from the given file, for use in substituting values into commands from an external source.
     *
     * @param file A File object denoting the file from which to load properties
     * @throws IOException
     */
    void readPropertiesFile(File file) {
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            System.getProperties().load(inStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Simple wrapper on readPropertiesFile(File) for use with a String path rather than a File object.
     *
     * @param path The path to the file from which to load properties
     * @throws IOException
     */
    public void readPropertiesFile(String path) throws IOException {
        readPropertiesFile(new File(path));
    }

    public void disconnect() {
        context.disconnectController();
    }

    /**
     * Set environment variables into memory
     */
    /**
     * Append or overwrite environment variables to system variables in memory.
     *
     * @param newenv Map of additional variables you want to add
     */
    private static void addEnv(Map<String, String> newenv) {
        setEnv(newenv, true);
    }

    private static void setEnv(Map<String, String> newenv, final boolean append) {

        Map<String, String> env = System.getenv();
        if (append) {
            for (String key : env.keySet())
                if (newenv.get(key) == null) newenv.put(key, env.get(key));
        }

        Class[] classes = Collections.class.getDeclaredClasses();
        try {
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that gets database properties from the idata
     *
     * @param properties The properties to add
     */

    public void addProperties(String[] properties) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        for (String prop : properties) {
            System.getProperties().setProperty(prop, idata.getVariable(prop));
        }
    }

    /**
     * Replacement for the .handle method in the CommandContext, because it will print to console regardless of our settings.
     */
    private ModelNode handle(String command) throws CommandFormatException, IOException {
        ModelNode returnValue;
        if (logger != null) {
            logger.info(command);
        }
        ModelControllerClient mcc = context.getModelControllerClient();
        ModelNode request = context.buildRequest(command);
        if (mcc != null){
            returnValue = mcc.execute(request);
        } else {
            returnValue = getFailureResult(command, "No ModelControllerClient was available to execute the request.");
        }
        return returnValue;
    }

    /**
     * returns a ModelNode containing a failure. used for situations which fail-fast
     */
    private ModelNode getFailureResult(String cmd, String failureMsg) {
        ModelNode failedNode = new ModelNode();
        failedNode.get("outcome").set("failed");
        failedNode.get("failure-description").set(failureMsg);
        ServerCommandsHelper.setCommand(failedNode, cmd);
        return failedNode;
    }

    /**
     * returns the vault session
     *
     * @return the VaultSession object if it exists, null otherwise.
     */
    public VaultSession getVaultSession() {
        return vault;
    }

    public boolean isBatchMode() {
        return context.isBatchMode();
    }

    /**
     * sets the domainHostname to a non-default value
     */
    public static void setDomainHostname(String name) {
        domainHostname = name;
    }

    /**
     * gets the domainHostname.
     */
    public static String getDomainHostname() {
        return domainHostname;
    }

    public boolean isDomain(){
        return isDomain;
    }

}
