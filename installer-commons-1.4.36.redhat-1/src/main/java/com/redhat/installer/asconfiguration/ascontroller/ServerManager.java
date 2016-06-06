package com.redhat.installer.asconfiguration.ascontroller;

import com.izforge.izpack.event.SimpleInstallerListener;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.*;
import com.redhat.installer.asconfiguration.processpanel.PostInstallUserHelper;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import com.redhat.installer.ports.utils.PortReader;
import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


/**
 * ServerManager is responsible for starting and stopping down the JBoss
 * application server during installation process. It keeps track of server's
 * status (whether it's running or not, it's mode and configuration).
 * <p/>
 * It contains a single entry point: The run() method for calling a start/stop job
 * from process panel. The arguments passed into it decide whether to start
 * or stop a server.
 * <p/>
 * Arguments passed in from ProcessPanel.Spec.xml:
 * --postinstall-script:    The path to the server startup script to use.
 * --attempts:              Number of times to poll for server codes when starting.
 * --timeout:               Number of seconds between attempts.
 * --standalone-config      The config xml suffix: ha/full-ha/full/osgi or none
 * --host-config            The config file used for domain mode: host
 * startup                  Start the server.
 * shutdown                 Shut down the server.
 *
 * Run() method returns 'false' in the event that starting the server was
 * unsuccessful, or if the arguments supplied are invalid. Otherwise returns true.
 * @author fcanas
 */
public class ServerManager extends SimpleInstallerListener implements
        CleanupClient {
    /**
     * Server status strings.
     */
    public static final String UP = "up";
    public static final String DOWN = "down";
    private static volatile String STATUS = DOWN; // down/up
    public static final String STARTING = "starting";
    public static final String UNKNOWN = "unknown";
    public static final String DOMAIN = "domain";
    public static final String STANDALONE = "standalone";
    private static String MODE = STANDALONE; // standalone/domain

    // Possible server codes we read from output:
    public static final String CODE_START_OK = "JBAS015874";
    public static final String CODE_START_ERROR = "JBAS015875";
    public static final String CODE_STOP_OK = "JBAS015950";
    public static final String CODE_PROCESS_FINISH = "JBAS012015";
    public static final String CODE_HOST_FINISH = "JBAS012010";
    public static final String CODE_SSL_SERVER = "JBAS015962";

    // String ids:
    private static final String MSG_STARTING_SERVER = "postinstall.processpanel.startingServer";
    private static final String MSG_SERVER_STARTED = "postinstall.processpanel.serverStarted";
    private static final String MSG_SERVER_STARTED_WITH_ERRORS = "postinstall.processpanel.serverStartedWithErrors";

    // Argument string constants:
    private static final String POSTINSTALL_SCRIPT = "postinstall-script";
    private static final String SERVER_SCRIPT = "server-script";
    private static final String STANDALONE_CONFIG = "standalone-config";
    private static final String HOST_CONFIG = "host-config";
    private static final String ONFAIL_JOB = "failmode";
    private static final String TIMEOUT = "timeout";
    private static final String ATTEMPTS = "attempts";
    private static final String SLAVE_SERVER = "slave";


    // Server property constants:
    private static final int DEFAULT_STANDALONE_ATTEMPTS = 10;
    private static final int DEFAULT_DOMAIN_ATTEMPTS = 10;
    private static final int DEFAULT_HTTP = 8080;
    private static final int DEFAULT_MANAGEMENT = 9999;
    private static final int DEFAULT_MANAGEMENT_HTTP = 9990;
    private static final int DEFAULT_MANAGEMENT_HTTPS = 9443;
    private static final int DEFAULT_TIME_BETWEEN_ATTEMPTS = 4;
    private static final int[] DEFAULT_RESPONSE_CODES = new int[]{HttpURLConnection.HTTP_OK};
    public static final String DEFAULT_SERVER_URL = "http://localhost:";
    public static final String DEFAULT_SSL_SERVER_URL = "https://localhost:";
    public static final String OVERRIDE_DEFAULT_TIMEOUT_VARIABLE = "system.timeout.override";

    /**
     * These two are used in a work-around for a bug where the full-ha standalone config does not produce
     * the usual startup codes to console. Once this bz is fixed, we can probably refactor these out
     * and treat the full-ha config just like the other ones.
     * See https://bugzilla.redhat.com/show_bug.cgi?id=1013941
     */
    public static final String FULL_HA = "full-ha";
    public static final String CODE_START_OK_FULL_HA = "JBAS015888";


    /**
     * Map of standalone config to its port string representation.
     */
    private final static HashMap<String, String> portStringMap = new HashMap<String, String>();

    static {
        portStringMap.put("", "");
        portStringMap.put("ha", "h");
        portStringMap.put("full", "f");
        portStringMap.put("full-ha", "fa");
    }

    /**
     * Server configuration variables:
     */
    private static AutomatedInstallData idata;
    private static String currentServerScriptLocation = "";
    private static boolean currentAdminMode = false;
    private static boolean isFailMode = false;
    private static int[] responseCodes = null;
    private static String pingUrl = null;
    private static String CONFIG = ""; // ""/full/ha/full-ha
    private static volatile String CODE = ""; // the server code, see below:
    public static String SLAVE = "false";
    private static boolean sslEnabled = false;
    public static String startOfServerURL = DEFAULT_SERVER_URL;

    /**
     * The process panel run handler. This is the entry point when we call
     * this class from process panel jobs.
     *
     * @param handler the process handler running this job.
     * @param args    arguments passed into job from processPanelSpec.xml
     * @throws Exception
     */
    public static boolean run(AbstractUIProcessHandler handler, String[] args)
            throws Exception {
        boolean result = true;
        idata = AutomatedInstallData.getInstance();
        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);

        /**
         * Fail mode is called when process panel jobs terminate with error:
         * This job is called to terminate any running server instances.
         */
        isFailMode = parser.propertyIsTrue(ONFAIL_JOB);

        /**
         * Check if we are starting or shutting down server.
         */
        boolean isStarting = parser.hasProperty(SERVER_SCRIPT)
                || parser.hasProperty(POSTINSTALL_SCRIPT);

        if (parser.hasProperty(HOST_CONFIG)) {
            handler.logOutput("[ host-config detected! ]", false);
        }

        setURL(DEFAULT_SERVER_URL);
        setSslEnabled(false);

        if (isStarting) {

            /**
             * If preparing server returns false, there is something wrong
             * with the configuration. Fail now.
             */
            if (!prepareServerForStart(handler, parser)) {
                return false;
            }

            int timeBetweenAttempts = DEFAULT_TIME_BETWEEN_ATTEMPTS;
            String override_server_timeout = idata.getVariable(OVERRIDE_DEFAULT_TIMEOUT_VARIABLE);

            if (override_server_timeout != null && !override_server_timeout.isEmpty()) {
                timeBetweenAttempts = Integer.parseInt(override_server_timeout);
            } else {
                if (parser.hasProperty(TIMEOUT))
                    timeBetweenAttempts = Integer.parseInt(parser.getStringProperty(TIMEOUT));
            }

            int attempts = (parser.hasProperty(ATTEMPTS)) ?
                    Integer.parseInt(parser.getStringProperty(ATTEMPTS)) : getDefaultAttempts();

            result = startServer(getServerScriptLocation(),
                    getMode(),
                    getConfig(),
                    getSlave(),
                    isCurrentAdminMode(),
                    timeBetweenAttempts,
                    attempts,
                    handler);
        } else {
            result = callShutdownServer(handler);
        }
        return result;
    }

    /**
     * Responsible for gathering all of the server properties needed before starting server.
     *
     * @param handler The current Process handler.
     * @param parser  Arguments passed in from ProcessPanelSpec.xml
     */
    private static boolean prepareServerForStart(AbstractUIProcessHandler handler, ArgumentParser parser) {
        idata = AutomatedInstallData.getInstance();

        String scriptLocation = extractServerScriptLocation(handler, parser);

        if (scriptLocation == null) {
            /**
             * Fail immediately since we don't have a server startup script.
             */
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.noscript"), false);
            return false;
        } else {
            setServerScriptLocation(scriptLocation);
        }

        String mode = extractMode(handler, parser, scriptLocation);
        String config = extractConfigurations(handler, parser, scriptLocation);
        String slave = extractSlave(handler, parser, scriptLocation);

        if (mode == null || config == null) {
            /**
             * The starting script location is not valid as it doesn't contain either standalone or domain.
             * This is a cause for failure.
             */
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.wrongarg"), false);
            return false;
        } else {
            setMode(mode);
            setConfig(config);
            setSlave(slave);
        }

        pingUrl = extractUrl(parser);
        responseCodes = extractResponseCodes(parser);

        /**
         * If we got this far, then we're all good.
         */
        return true;
    }

    private static ServerScriptLauncher generateLaunchScript(AbstractUIProcessHandler handler, boolean adminOnly,
                                                             String mode, String config, String slave, String location) {
        ServerScriptLauncher serverScript;
        /**
         * Generate a ScriptLauncher with the proper parameters.
         */
        if (adminOnly) {
            if (mode.contains(STANDALONE)) {
                /**
                 * Append the hyphen to the config if it isn't blank, because the
                 * script file will be of the form: standalone-<config>.xml
                 */
                String configString = (config.equals("") ? config : "-"
                        + config);

                /**
                 * Standalone admin-only mode with specific config.
                 */
                serverScript = new ServerScriptLauncher(handler, location, "--admin-only",
                        "--server-config=standalone" + configString + ".xml");
            } else {

                /**
                 * Domain admin-only mode.
                 */
                if (!slave.equals("true")) {
                serverScript = new ServerScriptLauncher(handler, location,
                        "--admin-only", "--host-config=" + config + ".xml");
                } else {

                    /**
                     * If we're starting the server in slave mode, we need to pass in the argument
                     * -Djboss.domain.master.address. Normally, this argument would be set to the IP
                     * address of the server machine, but this is not necessary for our purposes.
                     */
                    serverScript = new ServerScriptLauncher(handler, location,
                            "--admin-only", "--host-config=" + config + ".xml",
                            "-Djboss.domain.master.address=0");
                }
            }

        } else {
            if (mode.contains(STANDALONE)) {
                serverScript = new ServerScriptLauncher(handler, location);
            } else {
                serverScript = new ServerScriptLauncher(handler, location,
                        "--host-config=" + config + ".xml");
            }

        }
        return serverScript;
    }

    /**
     * Responsible for running the script that starts the server, after all arguments have been
     * extracted by prepareServerForStart method.
     *
     * @param location The path to the server startup script.
     * @param mode     "standalone"|"domain"
     * @param config   ""|"ha"|"full"|"full-ha"
     * @param handler  Can be null, otherwise the current thread handler.
     */
    private static boolean startServer(String location, String mode, String config, String slave,
                                       boolean adminOnly, int timeBetweenAttempts, int attempts,
                                       AbstractUIProcessHandler handler) {
        idata = AutomatedInstallData.getInstance();

        String startingServerString = idata.langpack
                .getString(MSG_STARTING_SERVER);

        String serverStartedString = idata.langpack
                .getString(MSG_SERVER_STARTED);

        String ServerStartedWithErrors = idata.langpack
                .getString(MSG_SERVER_STARTED_WITH_ERRORS);

        int port = 8080; // default

        if (adminOnly) {
            // Admin-only start uses native management port.
            port = getManagementPort(mode, config);
        } else {
            // Full server start uses http port.
            port = getHttpPort(mode, config);
        }
        /**
         * Forge the URL out of FIRE AND STEEL!!!
         */
        String url = startOfServerURL + port;

        ServerScriptLauncher serverScript = generateLaunchScript(handler, adminOnly, mode, config, slave, location);

        /**
         * Record that we are trying to start server. This is done in case startup fails,
         * in which case we don't actually know in which state the server is in and
         * if we record it as 'STARTING', an onFail job later can attempt to shut it down.
         */
        updateStatus(STARTING, "");

        if (handler != null)
            handler.logOutput(String.format(startingServerString, getMode(), getConfigString()), false);

        serverScript.runScript(); // Launching server here.
        /**
         * Ensure server has successfully started and update status, but only
         * ping if it's not in admin-only mode as admin-only doesn't respond on ping
         * interface.
         */
        if (waitForServerCodeReady(url, timeBetweenAttempts, attempts, handler)) {
            updateStatus(UP, CODE);
            if (handler != null && CODE.equals(CODE_START_ERROR))
                handler.logOutput(ServerStartedWithErrors, false);
            else if (handler != null)
                handler.logOutput(String.format(serverStartedString), false);
        } else {
            /**
             * We weren't succesful, so don't change status except for marking
             * server status as 'unknown'. This way we can try to shut it down at the end
             * in order to avoid leaving dangling server processes.
             */
            updateStatus(UNKNOWN, "");
            return false;
        }
        return true;
    }

    /**
     * Calls the shutdown server method.
     *
     * @param handler
     * @throws Exception
     */
    private static boolean callShutdownServer(AbstractUIProcessHandler handler)
            throws Exception {

        if (isFailMode)
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.onfail.check"), false);

        /**
         * The server is already shut down, so we are done.
         */
        if (isServerDown()) {
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.nostart"), false);
        }

        if (isFailMode)
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.shutdown"), false);

        return shutdownServer(handler);
    }

    /**
     * Attempt to Shut down the server, then ping it to ensure it doesn't respond.
     * Note: Currently doesn't test the server, just assumes shutdown worked if no exception is thrown.
     *
     * @param handler The current Process handler.
     * @throws Exception
     */
    private static boolean shutdownServer(AbstractUIProcessHandler handler) throws CliInitializationException, InterruptedException {
        idata = AutomatedInstallData.getInstance();
        ModelNode jbossCliOutput;
        boolean result = false;
        String servermode = getMode();
        String username = idata.getVariable("postinstall.username");
        String serverShutdownString = idata.langpack
                .getString("postinstall.processpanel.serverStopped");
        char[] password = idata.getVariable("postinstall.password")
                .toCharArray();

        ServerCommands serverCommands = null;

        int port = getManagementPort();

        try {
            serverCommands =  ServerCommands.createLocalDomainUsernameSession(username, password, port, Boolean.parseBoolean(getSlave()), (String[]) null);

        } catch (NullPointerException e) {
            /**
             * Catching an exception here probably means the server was already
             * down, or at least in a non-receptive state. Usually it's an NPE that causes it,
             */
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.alreadyoff"), false);
        }

        ServerCommandsHelper.createLogger(ServerManager.class.getName(), serverCommands);

        ServerCommandsHelper.connectContext(handler, serverCommands);

        if (servermode.contains("domain")) {
            Debug.log("Attempting to shut down domain at port " + port);
            jbossCliOutput = serverCommands.shutdownDomainHost();
            result = Operations.isSuccessfulOutcome(jbossCliOutput);
        } else if (servermode.contains("standalone")) {
            Debug.log("Attempting to shut down standalone at port " + port);
            jbossCliOutput = serverCommands.shutdownHost();
            result = Operations.isSuccessfulOutcome(jbossCliOutput);

        } else {
            // No previous server info found.
            Debug.log("No running server info found. Aborting shutdown.");
            jbossCliOutput = null;
        }
        serverCommands.terminateSession();

        long startTime = System.currentTimeMillis();

        while (!isServerDown() && result) {
            /**
             * Give the server 10 seconds to complete shutdown before proceeding.
             */
            if ((System.currentTimeMillis() - startTime) > 10000) break;
        }

        if(!result){
            serverShutdownString = jbossCliOutput.asString();
        }

        if (handler != null)
            ProcessPanelHelper.printToPanel(handler, serverShutdownString, !result);
        updateStatus(DOWN, CODE_STOP_OK);
        return result;
    }

    /**
     * Extracts server script location info from the processPanelSpec.xml arguments.
     * Note: Also sets the admin mode!
     *
     * @param handler
     * @param parser
     * @return The server script location, or null if the args don't contain it.
     */
    private static String extractServerScriptLocation(AbstractUIProcessHandler handler, ArgumentParser parser) {
        String serverLocation = null;
        if (parser.hasProperty(SERVER_SCRIPT)) {
            serverLocation = parser.getStringProperty(SERVER_SCRIPT);
            setCurrentAdminMode(false);
        } else if (parser.hasProperty(POSTINSTALL_SCRIPT)) {
            serverLocation = parser.getStringProperty(POSTINSTALL_SCRIPT);
            setCurrentAdminMode(true);
        } else {
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("ServerManager.invalidargs"), false);
        }
        return serverLocation;
    }

    /**
     * Extract the server mode specified in the processPanelSpec.xml arguments.
     *
     * @param handler
     * @param parser
     * @param scriptLocation
     * @return the server mode (standalone or domain), or null if neither was specified.
     */
    private static String extractMode(AbstractUIProcessHandler handler, ArgumentParser parser, String scriptLocation) {
        String mode = null;
        String scriptName = new File(scriptLocation).getName();
        if (scriptName.contains(DOMAIN)) {
            mode = DOMAIN;
        } else if (scriptName.contains(STANDALONE)) {
            mode = STANDALONE;
        }
        return mode;
    }

    /**
     * Extracts the server configuration specified in arguments. If none is specified, then it returns
     * the default configs for the given server mode.
     *
     * @param handler
     * @param parser
     * @param scriptLocation
     * @return the specified or default config for the given server mode. only returns null if no server mode
     * is specified in the arguments, which is an invalid case that should result in failure.
     */
    private static String extractConfigurations(AbstractUIProcessHandler handler, ArgumentParser parser, String scriptLocation) {
        String config = null;
        String scriptName = new File(scriptLocation).getName();
        if (scriptName.contains(DOMAIN)) {
            if (parser.hasProperty(HOST_CONFIG)) {
                config = parser.getStringProperty(HOST_CONFIG);
                if (handler != null)
                    ProcessPanelHelper.printToPanel(handler,
                            "[ host-config detected: " + config
                                    + "]", false
                    );
            } else {
                config = "host"; // Default configuration for domain mode.
            }

        } else if (scriptName.contains(STANDALONE)) {
            if (parser.hasProperty(STANDALONE_CONFIG)) {
                config = parser.getStringProperty(STANDALONE_CONFIG);
            } else {
                config = ""; // The default standalone config is blank. ie: standalone.xml
            }
        }
        return config;
    }

    /**
     * Extract whether this server is a slave server from processPanelSpec.xml arguments.
     *
     * @param handler
     * @param parser
     * @param scriptLocation
     * @return the string "true" only if --slave=true. Returns "false" otherwise.
     */
    private static String extractSlave(AbstractUIProcessHandler handler, ArgumentParser parser, String scriptLocation) {
        boolean slave = false;
        if (parser.hasProperty(SLAVE_SERVER) && parser.getStringProperty(SLAVE_SERVER).toLowerCase().equals("true")) {
            slave = true;
        }
        return String.valueOf(slave);
    }

    /**
     * @param parser
     * @return A custom-specified url, or null if args don't contain one.
     */
    private static String extractUrl(ArgumentParser parser) {
        String url = null;
        /**
         * Check for explicit ping interfaces and response codes.
         */
        if (parser.hasProperty("url-ext")) {
            url = parser.getStringProperty("url-ext");
        }
        return url;
    }

    /**
     * Arguments can specify a custom response code that the server will reply to ping
     * requests with. This is useful in some cases where the server doesn't reply with the conventional
     * code, but is still running and should be detected as such.
     *
     * @param parser
     * @return Custom specified response codes, or null if args don't contain any.
     */
    private static int[] extractResponseCodes(ArgumentParser parser) {
        int[] codes = null;
        if (parser.hasProperty("url-resp")) {
            codes = new int[]{HttpURLConnection.HTTP_OK,
                    Integer.parseInt(parser.getStringProperty("url-resp"))
            };
        }
        return codes;
    }

    /**
     * Server status getters/setters.
     */
    public static String getStatus() {
        return STATUS;
    }

    public static String  getMode() {
        return MODE;
    }

    public static void setMode(String mode) {
        ServerManager.MODE = mode;
    }

    public static String getConfig() {
        return CONFIG;
    }

    public static String getSlave() {
        return SLAVE;
    }
    public static String getConfigString() {
        String config = getConfig();
        if (getMode().contains(STANDALONE)) {
            return (config.isEmpty() ? config : "-" + config) + ".xml";
        } else {
            return getConfig() + ".xml";
        }
    }
    public static String getConfigPath() {
        String fileName = getConfigString();
        if(fileName.contains("host")) {
            return "domain/configuration/"+fileName;
        }
        return "standalone/configuration/"+"standalone"+fileName;
    }

    public static boolean isServerUp() {
        return STATUS.equalsIgnoreCase(UP);
    }

    public static boolean isServerStarting() {
        return STATUS.equalsIgnoreCase(STARTING);
    }

    public static boolean isServerDown() {
        return STATUS.equalsIgnoreCase(DOWN);
    }

    public static void setServerUp() {
        STATUS = UP;
    }

    public static String getServerCode() {
        return CODE;
    }

    public static void setServerCode(String code) {
        CODE = code;
    }

    public static void setServerDown() {
        STATUS = DOWN;
    }

    public static boolean isCurrentAdminMode() {
        return currentAdminMode;
    }

    public static void setCurrentAdminMode(boolean currentAdminMode) {
        ServerManager.currentAdminMode = currentAdminMode;
    }

    public static String getServerScriptLocation() {
        return currentServerScriptLocation;
    }

    public static void setServerScriptLocation(String location) {
        ServerManager.currentServerScriptLocation = location;
    }

    /**
     * @return true if one of the server ready codes has been detected. false otherwise.
     */
    private static boolean IsServerCodeReady() {
        return CODE.contains(CODE_START_OK) ||
                CODE.contains(CODE_START_ERROR) ||
                CODE.contains(CODE_START_OK_FULL_HA);
    }

    /**
     * @return The http console interface port for current mode and
     * configuration.
     */
    public static int getHttpPort() {
        return getPort(DEFAULT_HTTP, "http");
    }

    /**
     * @return The default url used by the application server, including colon.
     * ie: http://localhost:
     */
    public static void setURL(String url) {
        startOfServerURL = url;
    }

    /**
     * @return The default url used by the application server, including colon.
     * ie: http://localhost:
     */
    public static String getURL() {
        return startOfServerURL;
    }

    /**
     * @return The native cli management interface port for current mode and
     * configuration.
     */
    public static int getManagementPort() {
        return PortReader.getManagementPort(getConfigPath());
    }

    /**
     * @return The management port used by the current server configuration.
     */
    public static int getManagementConsolePort() {
        String portName = "management-http";
        int defaultPortNumber = DEFAULT_MANAGEMENT_HTTP;
        if (isSslEnabled()){
            portName = "management-https";
            defaultPortNumber = DEFAULT_MANAGEMENT_HTTPS;
        }
        return getPort(defaultPortNumber, portName);
    }

    private static boolean waitForServerCodeReady(String url, int timeout,
                                                  int attemptLimit,
                                                  AbstractUIProcessHandler handler) {
        String checkingServerString = idata.langpack
                .getString("postinstall.processpanel.checkingServer");
        String serverNotStartedString = idata.langpack
                .getString("postinstall.processpanel.serverNotStarted");
        String serverCodeNotDetectedString = idata.langpack.getString("postinstall.processpanel.serverCodeNotDetected");

        int currentAttempt = 0;
        int totalTime = timeout * attemptLimit;
        String timeUnit = "seconds";

        if (totalTime > 60) {
            totalTime = totalTime / 60;
            timeUnit = "minutes";
        }


        if (handler != null)
            ProcessPanelHelper.printToPanel(handler, String.format(checkingServerString, totalTime, timeUnit), false);

        while (!IsServerCodeReady()) {

            if (handler != null) ProcessPanelHelper.printToPanel(handler, ".", false);

            try {
                Thread.sleep(timeout * 1000);
            } catch (InterruptedException e) {
                // What to do here?!
            }

            currentAttempt++;

            if (currentAttempt > attemptLimit) {
                /**
                 * If we haven't seen the ready code by the time we time out,
                 * poll the http interface one last time to see if should
                 * proceed or fail.
                 */
                ProcessPanelHelper.printToPanel(handler, String.format(serverCodeNotDetectedString), true);
                if (isLinkAlive(url)) {
                    return true;
                } else {
                    if (handler != null)
                        ProcessPanelHelper.printToPanel(handler, String.format(serverNotStartedString,
                                attemptLimit + 1), true);

                    // Set status to unknown, since the ping verifies the server
                    // didn't finish going up successfully.
                    updateStatus(UNKNOWN, "");
                    return false;
                }
            }

        }
        /**
         * If we break out of loop, it's because server is code ready.
         */
        return true;
    }


    /**
     * @return The http console interface port for specified mode and config.
     */
    private static int getHttpPort(String mode, String config) {
        return getPort(mode, config, DEFAULT_HTTP, "http");
    }

    /**
     * @return The native cli management interface port for the specified mode
     * and configuration.
     */
    private static int getManagementPort(String mode, String config) {


        /**
         * Note: Domain's management mode port is not affected by the offset.
         */
        return getPort(mode, config, DEFAULT_MANAGEMENT,
                "management-native");
    }

    private static int getPort(int defaultPort, String portName) {
        String serverMode = getMode();
        String standaloneConfig = getConfig();
        return getPort(serverMode, standaloneConfig, defaultPort, portName);
    }

    /**
     * Given the server mode and config, returns the prefix needed to find the
     * server's custom port variables.
     *
     * @param serverMode       Either standalone, or domain mode.
     * @param standaloneConfig One of the config modes: ha, full, full-ha, etc.
     * @return The full servermode and config prefix, for example: standalone-full-ha
     */
    private static String getPortPrefix(String serverMode,
                                        String standaloneConfig) {
        String prefix;

        if (standaloneConfig.equals("")) {
            prefix = serverMode;
        } else {
            prefix = serverMode.contains("standalone") ? serverMode
                    + "." + portStringMap.get(standaloneConfig) : serverMode;
        }

        return prefix;
    }

    /**
     * Finds and returns the correct port number based on user's customized port
     * binding configurations, or default port if none is found.
     *
     * @param defaultPort The default port for the current server mode and config.
     * @param portName    The name of the port we need as used in the userInput specs.
     * @return the specified port.
     */
    private static int getPort(String serverMode, String standaloneConfig,
                               int defaultPort, String portName) {
        idata = AutomatedInstallData.getInstance();

        int port;

        String prefix = getPortPrefix(serverMode, standaloneConfig);

        String var = prefix + "." + portName;
        String val = idata.getVariable(var);

        if (val != null) {
            String portString = idata.getVariable(prefix + "." + portName);

            /**
             * Some port variables are in the form of other variables. Yikes!
             */
            if (portString.contains("$")) {

                port = Integer.parseInt(portString.substring(
                        portString.indexOf(':') + 1, portString.length() - 1));

            } else {
                port = Integer.parseInt(portString);
            }

        } else {
            /**
             * Others are simple numbers.
             */
            port = defaultPort;
        }

        /**
         * It says domain here, but this is actually the offset used by the
         * standalone. Not sure how this naming scheme came to be!
         */
        int offset = idata.getVariable("domain.port-offset") != null ? Integer
                .parseInt(idata.getVariable("domain.port-offset")) : 0;

        /**
         * Ignore the offset if we're getting the management-native or
         * management-http port for domain server.
         */
        if (serverMode.contains("domain")
                && (portName.contains("management-native") || portName
                .contains("management-http"))) {
            offset = 0;
        }

        return port + offset;

    }

    /**
     * Tests an HTTP connection and returns true if the other side
     * responds with an HTTP_OK.
     *
     * @param URLName Full URL to the server.
     * @return true if the server reponds, false otherwise.
     */
    private static boolean isLinkAlive(String URLName) {
        String url = (pingUrl == null) ? URLName : URLName + pingUrl;
        int[] ports = (responseCodes == null) ? DEFAULT_RESPONSE_CODES : responseCodes;
        return isLinkAlive(url, ports);

    }

    /**
     * Tests an HTTP connection and returns true if the other side
     * responds with an HTTP_OK.
     *
     * @param URLName   Full URL to the server.
     * @param goodcodes A list of integers. Each int is an expected HTTP Response
     *                  code.
     * @return True if the http interface returned one of the goodcodes.
     * False otherwise.
     */
    public static boolean isLinkAlive(String URLName, int[] goodcodes) {
        HttpURLConnection urlConn;

        try {
            URL url = new URL(URLName);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();

            Debug.log("Server Response Code: " + urlConn.getResponseCode());

            /**
             * Check the response code against the list of desired
             * HTTP response codes.
             */
            for (int code : goodcodes) {
                if (urlConn.getResponseCode() == code) return true;
            }

            return false;
        } catch (IOException e) {
            Debug.log(e.getMessage());
            return false;
        }
    }

    /**
     * Queries the given URL and returns true if the other side responds
     * after a given number of tries with time outs.
     *
     * @param url                 The URL to query.
     * @param attemptLimit        Number of times to attempt a ping.
     * @param timeBetweenAttempts Number of seconds between each ping.
     * @param handler             Process handler to report output to.
     * @return True if the server responds within alloted limit/attempts, false otherwise.
     */
    private static boolean pingServer(String url, int timeBetweenAttempts, int attemptLimit,
                                      AbstractUIProcessHandler handler) {
        String checkingServerString = idata.langpack
                .getString("postinstall.processpanel.checkingServer");
        String serverNotStartedString = idata.langpack
                .getString("postinstall.processpanel.serverNotStarted");
        int currentAttempt = 0;

        Debug.log(getServerStatusString());

        /**
         * Here we send the ping to the server. If it doesn't respond, go to
         * sleep and try again. Repeat until we receive a response or we run out
         * of attempts.
         */
        while (!isLinkAlive(url) && !IsServerCodeReady()) {
            if (handler != null)
                ProcessPanelHelper.printToPanel(handler, String.format(checkingServerString,
                        currentAttempt + 1,
                        attemptLimit + 1), false);
            try {
                Thread.sleep(timeBetweenAttempts * 1000);
            } catch (InterruptedException e) {
                // What to do here?!
            }

            currentAttempt++;

            if (currentAttempt > attemptLimit) {
                // Timed out without getting server response.
                if (handler != null)
                    ProcessPanelHelper.printToPanel(handler,
                            String.format(serverNotStartedString, attemptLimit + 1),
                            true);
                return false;
            }

        }

        /**
         * Server responded, so return true.
         */
        return true;
    }

    private static String getServerStatusString() {
        return "Status: " + STATUS + "\n" + "Mode: " + MODE + "\n"
                + "Config: " + CONFIG;

    }

    /**
     * @return the number of attempts for the current server config.
     */
    private static int getDefaultAttempts() {
        return getMode().contains(STANDALONE) ? DEFAULT_STANDALONE_ATTEMPTS : DEFAULT_DOMAIN_ATTEMPTS;
    }

    /**
     * Records the new status for the server.
     *
     * @param status (up or down)
     */
    private static void updateStatus(String status, String code) {
        STATUS = status;
        CODE = code;
        Debug.log("Recording Server Status:" + getServerStatusString());
    }

    public static void setConfig(String config) {
        ServerManager.CONFIG = config;
    }

    public static void setSlave(String slave) {
        ServerManager.SLAVE = slave;
    }

    public static void setSslEnabled(boolean sslEnabled) {
        ServerManager.sslEnabled = sslEnabled;
    }

    public static boolean isSslEnabled() {
        return sslEnabled;
    }

    /**
     * Run this to register to the Housekeeper. We do this after all
     * installation of packs.
     *
     * @param idata   Automated Install Data instance.
     * @param handler The current Process handler.
     * @throws Exception
     */
    @Override
    public void beforePacks(AutomatedInstallData idata, Integer x,
                            AbstractUIProgressHandler handler) throws Exception {
        Debug.log("Registering ServerManager for Cleanup.");
        Housekeeper.getInstance().registerForCleanup(this);
    }

    /**
     * This method will be run at shutdown when the user prematurely quits an
     * installation, whether the install failed or not.
     */
    public void cleanUp() {
        idata = AutomatedInstallData.getInstance();

        String aborted = idata.getVariable("install.aborted");

        Debug.log("Housekeeper: Running Server Manager Clean Up.");

        if ((aborted == null && !Boolean.parseBoolean(aborted))
                || (isServerDown())) {
            // clean exit, so remove the post install user and call it a day.
            Debug.log("Housekeeper: Server Not Up.");
            PostInstallUserHelper.removePostInstallUser();
            return;
        }

        try {
            Debug.log("Housekeeper: Server Up. Shutting Down.");
            shutdownServer(null);

        } catch (Exception e) {
            // This likely threw an exception because there is no server to shut
            // down or it already went down.

            Debug.log("Shutdown Server Exception: " + e.getMessage());
        }
    }
}
