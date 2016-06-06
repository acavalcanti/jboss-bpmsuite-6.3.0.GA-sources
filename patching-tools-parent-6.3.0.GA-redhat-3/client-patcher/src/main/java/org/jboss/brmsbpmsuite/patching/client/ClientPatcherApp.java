package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Main application class. Should be used when running the client patcher from command-line only.
 * Contains {@code System.exit()} calls which is considered harmful for standard unit tests.
 */
public class ClientPatcherApp {
    private static Logger logger = LoggerFactory.getLogger(ClientPatcherApp.class);

    protected enum ParsingResult {
        PRINT_HELP, ERROR_INVALID_ARGS, ERROR_UNKNOWN_DISTRIBUTION_TYPE, SUCCESS
    }

    protected static void commonMain(TargetProduct product, List<DistributionType> supportedDistroTypes, String... args) {
        File basedir = new File(".");
        // assume the basedir is current working directory
        ClientPatcherConfig config = new ClientPatcherConfig();
        config.setProduct(product);
        config.setSupportedDistroTypes(supportedDistroTypes);
        config.setBackupBaseDir(new File(basedir, "backup"));
        ParsingResult parsingResult = parseArgs(args, config);
        switch (parsingResult) {
            case PRINT_HELP:
                // just print help and we are done
                printHelp(supportedDistroTypes);
                System.exit(0);
            case ERROR_INVALID_ARGS:
                logger.error("Incorrect arguments specified! The program expects two mandatory parameters: " +
                        "<path-to-distribution> and <type-of-distribution>!");
                printHelp(supportedDistroTypes);
                System.exit(1);
            case ERROR_UNKNOWN_DISTRIBUTION_TYPE:
                logger.error("Unknown distribution type '{}' specified! Please select one of the supported ones.", args[1]);
                printHelp(supportedDistroTypes);
                System.exit(2);
            case SUCCESS:
                // do nothing, just continue with the execution
        }

        try {
            new ClientPatcherRunner(config).run();
        } catch (ClientPatcherException e) {
            logger.error("Error occurred during the execution!", e);
            printHelp(supportedDistroTypes);
            System.exit(2);
        } catch (Exception e) {
            logger.error("Unexpected error occurred. Please contact Red Hat Support if you think this is a bug in the " +
                    "patching tool itself.", e);
            System.exit(10);
        }
    }

    /**
     * Parses the arguments and fills client patcher configuration with the parsed values.
     *
     * @param args   arguments passed to the application
     * @param config patcher configuration
     * @return results of the parsing
     */
    protected static ParsingResult parseArgs(String[] args, ClientPatcherConfig config) {
        if (args == null) {
            throw new IncorrectArgumentsException("Null arguments specified!");
        }
        if (args.length == 1 && "-h".equals(args[0])) {
            return ParsingResult.PRINT_HELP;
        }
        if (args.length == 2 || args.length == 3) {
            if (args.length == 2) {
                logger.debug("Two arguments specified, assuming first is the path to the distribution root and second is the " +
                        "distribution type.");
            } else {
                logger.debug("Three arguments specified, assuming first is the path to the distribution root, second is the " +
                        "distribution type and third is list of phases to execute");
            }
            config.setDistributionRoot(new File(args[0]));
            try {
                config.setDistributionType(DistributionType.fromString(args[1]));
            } catch (IllegalArgumentException e) {
                return ParsingResult.ERROR_UNKNOWN_DISTRIBUTION_TYPE;
            }

            // third (optional) argument is the comma-separated list of patching phases to execute
            // should be used just for testing/debugging, it is not even documented
            // it needs to be in the form of "--phases=<comma-separated-list>" to prevent users accidentally specifying
            // some dummy parameter
            if (args.length == 3) {
                String argValue = args[2].trim();
                if (!argValue.startsWith("--phases=")) {
                    return ParsingResult.ERROR_INVALID_ARGS;
                }
                String phasesString = argValue.substring("--phases=".length());
                // parse the comma-separated list of phases to execute
                String[] phaseNames = phasesString.split(",");
                List<PatchingPhase> phasesToExecute = Lists.newArrayList();
                for (String phaseName : phaseNames) {
                    phasesToExecute.add(PatchingPhase.fromString(phaseName.trim()));
                }
                config.setPhasesToExecute(phasesToExecute);
            }
            return ParsingResult.SUCCESS;
        } else {
            return ParsingResult.ERROR_INVALID_ARGS;
        }
    }

    protected static void printHelp(List<DistributionType> supportedDistroTypes) {
        StringBuilder helpSB = new StringBuilder();
        helpSB.append("\nUsage: apply-updates.[sh|bat] <path-to-distribution-root> <type-of-distribution>\n" +
                        "Description: JBoss BRMS/BPM Suite client patching tool used to apply updates to existing installations."
        );

        helpSB.append("\n\nIMPORTANT: Do not apply the updates to running applications. Shutdown the server first.");

        helpSB.append("\n\nSupported distribution types:\n");
        for (DistributionType distroType : supportedDistroTypes) {
            helpSB.append("\t - " + distroType.getName() + "\n");
        }

        helpSB.append("\nExamples:\n" +
                        "\tPatch EAP 6.x Business Central WAR:\n" +
                        "\t\t./apply-updates.[sh|bat] <some-path>/jboss-eap-6.4/standalone/deployments/business-central.war eap6.x-bc\n\n" +
                        "\tPatch Generic KIE Server WAR:\n" +
                        "\t\t./apply-updates.[sh|bat] <some-path-to-tomcat-home>/webapps/kie-server.war generic-kie-server\n\n" +
                        "\tPatch whole WebLogic 12c bundle:\n" +
                        "\t\t./apply-updates.[sh|bat] <path-to-unzipped-wls12c-bundle> wls12c\n\n" +
                        "\tPatch Planner engine bundle:\n" +
                        "\t\t./apply-updates.[sh|bat] <path-to-unzipped-planner-bundle> planner-engine\n"
        );

        helpSB.append("\nNotes:\n" +
                        "  - Working dir needs to be the directory of this script!\n" +
                        "  - Java is recommended to be JDK and version 6 or later\n" +
                        "  - The environment variable JAVA_HOME should be set to the JDK installation directory\n" +
                        "      For example (linux): export JAVA_HOME=/usr/lib/jvm/java-6-sun\n" +
                        "      For example (mac): export JAVA_HOME=/Library/Java/Home"
        );

        System.out.println(helpSB.toString());
    }

}
