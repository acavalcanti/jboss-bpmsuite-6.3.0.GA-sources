package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;
import org.jboss.brmsbpmsuite.patching.client.PatchingUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class PatchingUtilsRegexFromGlobTest extends BaseClientPatcherTest {

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        return Lists.newArrayList(new Object[][]{
                        {
                                "business-central.war/WEB-INF/lib/drools-core-6.2.0.Final-redhat-*.jar",
                                "^business\\-central\\.war/WEB\\-INF/lib/drools\\-core\\-6\\.2\\.0\\.Final\\-redhat\\-.*\\.jar$"
                        },
                        {
                                "simple-file-name.txt",
                                "^simple\\-file\\-name\\.txt$"
                        },
                        {
                                "some-?text\\",
                                "^some\\-.text\\\\$"
                        },
                        {
                                "([{\\^-=$!|]})?*+.",
                                "^\\(\\[\\{\\\\\\^\\-\\=\\$\\!\\|\\]\\}\\)..*\\+\\.$"

                        }
                }
        );
    }

    @Parameterized.Parameter(0)
    public String glob;

    @Parameterized.Parameter(1)
    public String expectedRegex;

    @Test
    public void shouldCreateCorrectRegexFromGlob() {
        String actualRegex = PatchingUtils.createRegexFromGlob(glob);
        Assert.assertEquals("Glob incorrectly converted to regex!", expectedRegex, actualRegex);
    }

}
