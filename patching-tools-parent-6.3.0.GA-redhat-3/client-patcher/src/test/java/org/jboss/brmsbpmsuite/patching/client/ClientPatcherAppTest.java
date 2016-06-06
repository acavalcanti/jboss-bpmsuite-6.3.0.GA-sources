package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

public class ClientPatcherAppTest extends BaseClientPatcherTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientPatcherAppTest.class);

    @Test
    public void shouldCorrectlyParseTwoArguments() {
        ClientPatcherConfig config = new ClientPatcherConfig();
        ClientPatcherApp.ParsingResult result = ClientPatcherApp.parseArgs(new String[]{"some-dir", "brms-engine"}, config);

        Assert.assertEquals("Unexpected arguments parsing result!", ClientPatcherApp.ParsingResult.SUCCESS, result);
        Assert.assertEquals("Unexpected distribution root!", new File("some-dir"), config.getDistributionRoot());
        Assert.assertEquals("Unexpected distribution type!", DistributionType.BRMS_ENGINE, config.getDistributionType());
    }

    @Test
    public void shouldFailWhenParsingArgumentsWithUnknownDistributionType() {
        ClientPatcherConfig config = new ClientPatcherConfig();
        ClientPatcherApp.ParsingResult result = ClientPatcherApp.parseArgs(new String[]{"some-dir", "unknown-type"}, config);
        Assert.assertEquals("Unexpected arguments parsing result!",
                ClientPatcherApp.ParsingResult.ERROR_UNKNOWN_DISTRIBUTION_TYPE, result);
    }

    @Test
    public void shouldParseArgumentsWithPhasesInCorrectFormat() {
        ClientPatcherConfig config = new ClientPatcherConfig();
        ClientPatcherApp.ParsingResult result = ClientPatcherApp.parseArgs(
                new String[]{"some-dir", "brms-engine", "--phases=backup,apply"}, config);

        Assert.assertEquals("Unexpected arguments parsing result!", ClientPatcherApp.ParsingResult.SUCCESS, result);
        Assert.assertEquals("Unexpected distribution root!", new File("some-dir"), config.getDistributionRoot());
        Assert.assertEquals("Unexpected distribution type!", DistributionType.BRMS_ENGINE, config.getDistributionType());
        Assert.assertEquals("Unexpected list of phases to execute!",
                Lists.newArrayList(PatchingPhase.BACKUP, PatchingPhase.APPLY), config.getPhasesToExecute());
    }

    @Test
    public void shouldFailWhenParsingArgumentsWithPhasesInInCorrectFormat() {
        ClientPatcherConfig config = new ClientPatcherConfig();
        ClientPatcherApp.ParsingResult result = ClientPatcherApp.parseArgs(
                new String[]{"some-dir", "brms-engine", "incorrect-format-for-phases-list"}, config);

        Assert.assertEquals("Unexpected arguments parsing result!", ClientPatcherApp.ParsingResult.ERROR_INVALID_ARGS, result);
    }

    @Test
    public void shouldPrintHelpCorrectly() {
        ClientPatcherApp app = new ClientPatcherApp();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream tmpSysout = new PrintStream(baos);
        PrintStream origSysout = System.out;
        System.setOut(tmpSysout);
        try {
            app.printHelp(DistributionType.getBPMSuiteDistributionTypes());
        } finally {
            System.setOut(origSysout);
        }
        String helpString = baos.toString();
        logger.info("Help string (for BPM Suite supported distros): " + helpString);
        // do just basic sanity checks; comparing by String.equals() would be too error prone to even small changes
        assertTrue("Help string does not contain product name!", helpString.contains("BPM Suite"));
        assertTrue("Help string does not contain usage info!", helpString.contains("Usage"));
        assertTrue("Help string does not contain list of supported types!",
                helpString.contains("Supported distribution types"));
    }

}
