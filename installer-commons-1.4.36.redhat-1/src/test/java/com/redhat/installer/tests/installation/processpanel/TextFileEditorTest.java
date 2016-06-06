package com.redhat.installer.tests.installation.processpanel;

import com.redhat.installer.framework.constants.TestPaths;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.installation.processpanel.TextFileEditor;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created by aabulawi on 23/04/15.
 */
public class TextFileEditorTest extends ProcessPanelTester {

    String resourcePath = "/processpanel/TextFileEditor";

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder);
    }

    @Test
    public void AfterLineTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=#Djboss.modules.policy-permissions=true;editmode#=#after_line;text#=#   JAVA_OPTS=\"$JAVA_OPTS -d64\"",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedForLineAfter"));

    }

    @Test
    public void AppendLineTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=#PRESERVE_JAVA_OPTS=true;editmode#=#append;text#=#I am appending this",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedForAppend"));

    }

    @Test
    public void ReplaceTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=#-Djava.net.preferIPv4Stack=true;editmode#=#replace;text#=#-Djava.net.preferIPv4Stack=false",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedForReplace"));

    }

    @Test
    public void ReplaceGroupTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=##(?=.*JAVA_HOME=.*);editmode#=#replace;text#=#",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedForReplaceGroup"));

    }

    @Test
    public void PrependTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=#(?<=\\s+)JAVA_OPTS;editmode#=#prepend;text#=##",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedForPrepend"));

    }

    @Test
    public void LineBeforeTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=##JAVA_HOME;editmode#=#before_line;text#=##This is the comment before",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedForLineBefore"));

    }

    @Test
    public void LineBeforeAfterAndRemoveTest(){
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, resourcePath + "/standalone.conf", "/bin/standalone.conf");
        TextFileEditor.run(handler, new String[]{
                "--file=" + tempFolder.getRoot().getPath() + "/bin/standalone.conf",
                "--config=regex#=##JAVA_HOME;editmode#=#before_line;text#=##This is a line before comment",
                "--config=regex#=##JAVA_HOME;editmode#=#after_line;text#=##This is a line after comment",
                "--config=regex#=#JAVA_OPTS;editmode#=#remove",
        });
        assertTrue(filesAreEqual(tempFolder.getRoot()+"/bin/standalone.conf", resourcePath + "/expectedBeforeAfterRemove"));

    }

    @Override
    @Ignore
    public void testProcessPanelInstantiation() {

    }

    public boolean filesAreEqual(String editedFile, String expectedFile){
        try {
            File file1 = new File(System.getProperty("user.dir")+File.separator+ TestPaths.TEST_RESOURCE_DIR + expectedFile);
            File file2 = new File(editedFile);
            return FileUtils.contentEquals(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
