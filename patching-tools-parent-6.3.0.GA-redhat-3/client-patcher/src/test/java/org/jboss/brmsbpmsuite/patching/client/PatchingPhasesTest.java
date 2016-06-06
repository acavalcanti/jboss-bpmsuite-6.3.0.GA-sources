package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class PatchingPhasesTest extends BaseClientPatcherTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                {Lists.newArrayList(PatchingPhase.CHECK_DISTRO)},
                {Lists.newArrayList(PatchingPhase.BACKUP)},
                {Lists.newArrayList(PatchingPhase.APPLY)},
                {Lists.newArrayList(PatchingPhase.VERIFY)},
                {Lists.newArrayList(PatchingPhase.CLEAN_UP)},

                {Lists.newArrayList(PatchingPhase.CHECK_DISTRO, PatchingPhase.APPLY, PatchingPhase.CLEAN_UP)},
                {Lists.newArrayList(PatchingPhase.CHECK_DISTRO, PatchingPhase.BACKUP, PatchingPhase.APPLY, PatchingPhase.VERIFY,
                        PatchingPhase.CLEAN_UP)}
        });
    }

    @Parameterized.Parameter(0)
    public List<PatchingPhase> expectedPhases;

    @Test
    public void shouldExecuteOnlySpecifiedPhases() {
        ClientPatcherConfig config = new ClientPatcherConfig();
        config.setPhasesToExecute(expectedPhases);
        ClientPatcherRunner runner = new ClientPatcherRunner(config);
        TestingPatcher patcher = new TestingPatcher();
        runner.runPatcher(patcher, new File("."));
        List<PatchingPhase> actualPhases = patcher.getExecutedPhases();
        Assert.assertEquals("Expected phases are different from the actual ones!", expectedPhases, actualPhases);
    }

    public class TestingPatcher implements Patcher {
        private List<PatchingPhase> executedPhases = Lists.newArrayList();

        public List<PatchingPhase> getExecutedPhases() {
            return executedPhases;
        }

        @Override
        public void checkDistro() {
            executedPhases.add(PatchingPhase.CHECK_DISTRO);
        }

        @Override
        public void backup(File backupBasedir) throws IOException {
            executedPhases.add(PatchingPhase.BACKUP);
        }

        @Override
        public void apply() throws IOException {
            executedPhases.add(PatchingPhase.APPLY);
        }

        @Override
        public void verify() {
            executedPhases.add(PatchingPhase.VERIFY);
        }

        @Override
        public void cleanUp() {
            executedPhases.add(PatchingPhase.CLEAN_UP);
        }
    }

}
