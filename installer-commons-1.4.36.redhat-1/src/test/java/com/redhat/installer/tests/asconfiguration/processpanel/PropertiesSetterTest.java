package com.redhat.installer.tests.asconfiguration.processpanel;

import com.redhat.installer.asconfiguration.processpanel.PropertiesSetter;
import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by thauser on 2/6/14.
 */
public class PropertiesSetterTest extends ProcessPanelTester {
    private static final String TEST_PROP = "testprop";
    private static final String OLD_VALUE = "oldvalue";
    private static final String NEW_VALUE = "newvalue";

    PropertiesSetter ps;

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "PropertiesSetter.notexist",
                "PropertiesSetter.newfile", "PropertiesSetter.success", "PropertiesSetter.failure");
        ps = new PropertiesSetter();
    }

    @After
    public void tearDown() throws Exception {
        ps = null;
    }

    @Test
    public void testValidProperties() throws Exception {
        File propsFile = MockFileBuilder.makeNewFileFromStrings(tempFolder, TEST_PROP + " = " + OLD_VALUE);
        ps.run(handler, new String[] {propsFile.getAbsolutePath(), TEST_PROP+"=" + NEW_VALUE});
        assertEquals(NEW_VALUE, loadNewProperties(propsFile).getProperty(TEST_PROP));
    }

    @Test
    public void testInvalidProperties() throws Exception {
        File propsFile = MockFileBuilder.makeNewFileFromStrings(tempFolder, TEST_PROP + " : " + OLD_VALUE);
        ps.run(handler, new String[]{propsFile.getAbsolutePath(), TEST_PROP+"=" + NEW_VALUE});
        assertEquals(NEW_VALUE, loadNewProperties(propsFile).getProperty(TEST_PROP));
    }

    @Test
    public void testCommentPreservation() throws Exception {
        File propsFile = MockFileBuilder.makeNewFileFromStrings(tempFolder,"#the comment is here.", TEST_PROP + "=" + OLD_VALUE);
        ps.run(handler, new String[]{propsFile.getAbsolutePath(), TEST_PROP + "=" + NEW_VALUE});
        assertEquals(NEW_VALUE, loadNewProperties(propsFile).getProperty(TEST_PROP));
        // ensure comments exist
        List<String> lines = FileUtils.readLines(propsFile);
        assertEquals("#the comment is here.", lines.get(0));
     }

    // deceiving test. the file is left with ambiguous information that should be removed, but the result is "technically" right
    @Test
    public void testMultiLinePropertyValue() throws Exception {
        File propsFile = MockFileBuilder.makeNewFileFromStrings(tempFolder, TestUtils.testPropertiesPath, "multi-line-prop=thisis\\\namultiline\\\nprop");
        ps.run(handler, new String[] {propsFile.getAbsolutePath(), "multi-line-prop=new\\\nline\\\nprop"});
        assertEquals("new\\\nline\\\nprop", loadNewProperties(propsFile).getProperty("multi-line-prop"));
    }

    @Test
    public void testAddingNewFile() throws Exception {
        ps.run(handler, new String[] {tempFolder.newFile("add-new-props-file.props").getAbsolutePath(), TEST_PROP+"="+NEW_VALUE});
        assertEquals(NEW_VALUE, loadNewProperties(new File (tempFolder.getRoot(), "add-new-props-file.props")).getProperty(TEST_PROP));
    }

    @Test
    public void testAddingNewProperty() throws Exception {
        File propsFile = MockFileBuilder.makeNewFileFromStrings(tempFolder, "otherprop=somevalue");
        ps.run(handler, new String[] {propsFile.getAbsolutePath(), TEST_PROP+"="+NEW_VALUE});
        assertEquals(NEW_VALUE, loadNewProperties(propsFile).getProperty(TEST_PROP));
    }

    public Properties loadNewProperties(File propsFile) throws Exception {
        Properties check = new Properties();
        check.load(new FileReader(propsFile));
        return check;
    }

    // this test doesn't really seem to work as desired.
    // the attempted goal was to emulate how IzPack loads classes, but the classes seem
    // fine when we're building (since they're needed at compile time and exist on the classpath)
    // maybe we can start an external java JVM to try and get it to work. for the time being, this test
    // always passes
    @Override
    public void testProcessPanelInstantiation() {
        boolean f = false;
        try {
           MockProcessPanelWorker mppw = new MockProcessPanelWorker();
           mppw.loadClass(ps.getClass().getName(), handler);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            assertTrue("The class: " + this.getClass().getName() + " did not contain the correct method signature.", f);
        } catch (InstantiationException e) {
            e.printStackTrace();
            assertTrue("Unable to create a new instance of: " + this.getClass().getName(), f);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            assertTrue("Access denied to instantiate: "+ this.getClass().getName(), f);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            assertTrue("Class not found: " + this.getClass().getName(), f);
        }
    }
}
