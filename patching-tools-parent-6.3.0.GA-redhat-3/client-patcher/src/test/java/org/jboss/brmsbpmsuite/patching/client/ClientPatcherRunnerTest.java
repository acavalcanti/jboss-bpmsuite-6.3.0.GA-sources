package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientPatcherRunnerTest extends BaseClientPatcherTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientPatcherRunnerTest.class);

    @Test
    public void shouldSucceedWhenCorrectDistributionPathAndTypeSpecified() throws Exception {
        File distroRoot = new File(tmpDir, "distro-root");
        FileUtils.copyDirectory(getCPResourceAsFile("/client-patcher-runner-test/simple-distro"), distroRoot);

        File patchDir = getCPResourceAsFile("/client-patcher-runner-test/simple-patch");
        ClientPatcherConfig config = new ClientPatcherConfig();
        config.setPatchBasedir(patchDir);
        config.setDistributionRoot(distroRoot);
        config.setProduct(TargetProduct.BRMS);
        config.setSupportedDistroTypes(Lists.newArrayList(DistributionType.BRMS_ENGINE));
        config.setDistributionType(DistributionType.BRMS_ENGINE);
        config.setBackupBaseDir(tmpDir);
        ClientPatcherRunner runner = new ClientPatcherRunner(config);
        runner.run();
        // just a dummy check that the patcher was correctly executed
        assertFileNotExists(new File(distroRoot, "drools-compiler-6.2.0.Final-redhat-3.jar"));
        assertFileExists(new File(distroRoot, "drools-compiler-6.2.0.Final-redhat-4.jar"));
    }

    @Test(expected = ClientPatcherException.class)
    public void shouldReportErrorWhenBackupFails() {
        ClientPatcherRunner runner = new ClientPatcherRunner(ClientPatcherConfig.empty());
        Patcher dummyPatcher = new Patcher() {
            @Override
            public void checkDistro() {
            }

            @Override
            public void backup(File backupBasedir) throws IOException {
                throw new IOException("Backup failed - expected testing exception!");
            }

            @Override
            public void apply() throws IOException {
            }

            @Override
            public void verify() {
            }

            @Override
            public void cleanUp() {
            }
        };
        runner.runPatcher(dummyPatcher, new File("."));
    }

    @Test(expected = ClientPatcherException.class)
    public void shouldReportErrorWhenApplyFails() {
        ClientPatcherRunner runner = new ClientPatcherRunner(ClientPatcherConfig.empty());
        Patcher dummyPatcher = new Patcher() {
            @Override
            public void checkDistro() {
            }

            @Override
            public void backup(File backupBasedir) throws IOException {
            }

            @Override
            public void apply() throws IOException {
                throw new IOException("Apply failed - expected testing exception!");
            }

            @Override
            public void verify() {
            }

            @Override
            public void cleanUp() {
            }
        };
        runner.runPatcher(dummyPatcher, new File("."));
    }

    @Test
    @SuppressWarnings("deprecated")
    public void shouldCreateCorrectBackupDirNameAndPath() {
        ClientPatcherRunner runner = new ClientPatcherRunner(ClientPatcherConfig.empty());
        Date now = new Date();
        File backupDir = runner.getBackupDir(new File("."), DistributionType.EAP6X_BC);
        assertEquals("The actual backup dir name is not equal to the distribution name!", DistributionType.EAP6X_BC.getName(),
                backupDir.getName());

        File parentDir = backupDir.getParentFile();
        // just check the year + month + day, checking the time can be tricky in different timezones, we also do not know the exact
        // time to compare against
        String expectedDirNamePrefix = new SimpleDateFormat("yyyy-MM-dd").format(now);
        assertTrue("Expected dir prefix " + expectedDirNamePrefix + ", but actual dir name is " + parentDir.getName(),
                parentDir.getName().startsWith(expectedDirNamePrefix));
    }

    @Test
    public void shouldCreateBackupDirWithSuffixIfDefaultDirAlreadyExists() throws Exception {
        File backupBaseDir = new File(tmpDir, "backup-base-dir");
        ClientPatcherRunner runner = new ClientPatcherRunner(ClientPatcherConfig.empty());
        Date now = new Date();
        String timestampDirName = ClientPatcherRunner.BACKUP_DIR_NAME_DATE_FORMAT.format(now);
        // make sure the default name exists
        FileUtils.forceMkdir(new File(backupBaseDir, timestampDirName + "/eap6.x-bc"));
        assertDirExists(new File(backupBaseDir, timestampDirName + "/eap6.x-bc"));

        File backupDir = runner.getBackupDir(backupBaseDir, DistributionType.EAP6X_BC, now);
        assertEquals("Default backup dir exists, new one with suffix should have been created!",
                new File(backupBaseDir, timestampDirName + "/eap6.x-bc_2"), backupDir);

        FileUtils.forceMkdir(new File(backupBaseDir, timestampDirName + "/eap6.x-bc_2"));
        backupDir = runner.getBackupDir(backupBaseDir, DistributionType.EAP6X_BC, now);
        assertEquals("Default backup dir exists, new one with suffix should have been created!",
                new File(backupBaseDir, timestampDirName + "/eap6.x-bc_3"), backupDir);
    }

}
