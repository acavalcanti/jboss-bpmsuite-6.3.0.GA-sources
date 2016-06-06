package com.redhat.installer.tests.asconfiguration.jdbc.validator;

import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;
import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.framework.mock.MockResourceBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 2/5/14.
 */
public class JDBCConnectionUtilsTest {

    public String tempPath;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        tempPath = tempFolder.getRoot().getAbsolutePath();
    }

    @Test
    public void testConvertToUrlArrayValidUrls() throws Exception{
        String[] urls = new String[6];
        URL[] comparison = new URL[6];
        urls[0] = "file:///" + tempPath +"/testurl.jar";
        urls[1] = "https://s01.yyz.redhat.com/thauser/datasourcejars/ibm/db2/main/db2jcc4.jar";
        urls[2] = "ftp://localhost/datasourcejars/db2jcc.jar";
        urls[3] = "file:///some/file/here";
        urls[4] = "http://vanilla.com/http/protocol";
        urls[5] = "file:///W:%5Ctest%5Craw%5Cwindows%5Cpath";

        for (int i = 0; i < comparison.length; i++){
            if (urls[i].contains("\\")){
                urls[i] = urls[i].replaceAll("\\\\","%5C");
            }
            comparison[i] = new URI(urls[i]).toURL();
        }
        URL[] result = JDBCConnectionUtils.convertToUrlArray(urls);
        for (int i = 0; i < result.length; i++){
            assertEquals(comparison[i].toString(), result[i].toString());
        }
    }

    @Test
    public void testConvertToUrlArrayInvalidUrls() throws Exception {
        String [] urls = new String[2];
        URL [] comparison = new URL[2];
        urls[0] = "file:///local/file/is/here";
        urls[1] = "ftps://unsupported/protocol"; // doing new URI().toURL() will result in an exception
        // TODO: rest of the test once the new behavior is defined.
    }

    @Test
    public void testGetDatabaseConnectionValid() throws Exception {

    }

    @Test
    public void testGetDatabaseConnectionInvalidDriver(){

    }

    @Test
    public void testGetDatabaseConnectionWrongUsername(){

    }

    @Test
    public void testGetDatabaseConnectionWrongPassword(){

    }

    @Test
    public void testGetDatabaseConnectionWrongUrl(){

    }

    @Test
    public void testVerifyJarPathValid() throws Exception{
        File emptyZip = MockFileBuilder.makeZipFile(tempFolder);
        assertEquals(0, JDBCConnectionUtils.verifyJarPath(emptyZip.getAbsolutePath()));
    }

    @Test
    public void testVerifyJarPathInvalid() throws Exception {
        String path = "/zip-file.zip";
        int expected;
        /**
         * Note: This test returns a '1' on jdk1.7+, but a 2 if jdk <= 1.6
         */
        if ( Float.valueOf( System.getProperty("java.specification.version")) >= 1.7 ) {
            expected = 1;
        }
        else {
            expected = 2;
        }
        assertEquals(expected, JDBCConnectionUtils.verifyJarPath(path));
    }

    @Test
    public void testVerifyJarPathNonZip() throws Exception {
        File emptyFile = tempFolder.newFile();
        assertEquals(2, JDBCConnectionUtils.verifyJarPath(emptyFile.getAbsolutePath()));
    }

    @Test
    public void testVerifyJarPathEmptyZip() throws Exception{
        String name = "/empty-zip.zip";
        MockResourceBuilder.copyResourceToBaseDir(tempFolder, name, name);
        int expected;
        /**
         * jdk 1.7 throws a '3', jdk 1.6 throws a '2'.
         */
        if ( Float.valueOf( System.getProperty("java.specification.version")) >= 1.7 ) {
            expected = 3;
        }
        else {
            expected = 2;
        }
        assertEquals(expected, JDBCConnectionUtils.verifyJarPath(tempPath + name));
    }

    @Test
    public void testVerifyJarPathUnreachable() throws Exception {
        assertEquals(4, JDBCConnectionUtils.verifyJarPath("http://unreachablehostforsure.com/non-existent-file.jar"));
    }

    //TODO: perhaps reference remote driver jars for these tests? seems a little fragile / hard to test
    @Test
    public void testFindDriverClassValid(){

    }

    @Test
    public void testFindDriverClassInvalid(){

    }
}
