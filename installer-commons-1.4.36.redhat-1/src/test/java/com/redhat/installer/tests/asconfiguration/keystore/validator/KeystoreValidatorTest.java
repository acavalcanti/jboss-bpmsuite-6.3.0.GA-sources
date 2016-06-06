package com.redhat.installer.tests.asconfiguration.keystore.validator;

import com.redhat.installer.asconfiguration.keystore.validator.KeystoreValidator;
import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.tests.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assume.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static com.redhat.installer.tests.TestUtils.*;
import static junit.framework.TestCase.*;


/**
 * Created by thauser on 2/4/14.
 */
public class KeystoreValidatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void isValidKeystoreTestValidKeystore() throws Exception{
        File keystore = createMockKeystore(tempFolder);
        assertEquals(0, KeystoreValidator.isValidKeystore(keystore.getAbsolutePath() , mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void isValidKeystoreTestWrongPassword() throws Exception {
        File keystore = createMockKeystore(tempFolder);
        assertEquals(1, KeystoreValidator.isValidKeystore(keystore.getAbsolutePath() , "incorrectpassword".toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void isValidKeystoreTestFileNotExist() throws Exception {
        assertEquals(2, KeystoreValidator.isValidKeystore("doesnt-exist" , mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void isValidKeystoreTestNonJKSKeystore() throws Exception {
        File keystore = createMockKeystore(tempFolder, "jceks");
        assertEquals(0, KeystoreValidator.isValidKeystore(keystore.getAbsolutePath() , mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void testValidFileUriPath() throws Exception {
        File keystore = createMockKeystore(tempFolder);
        String normalizedKeystore = keystore.getAbsolutePath().replace(File.separator, "/");
        assertEquals(0, KeystoreValidator.isValidKeystore("file:///" + normalizedKeystore , TestUtils.mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void testInvalidFileUriPath() throws Exception {
        assertEquals(2, KeystoreValidator.isValidKeystore("file:///doesntexist" , TestUtils.mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void testMalformedFileUriPath() throws Exception {
        assertEquals(2, KeystoreValidator.isValidKeystore("file:///\\malformedURI", TestUtils.mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    // since we also allow straight up File paths, this never gets caught. So we should be ok here
    @Test
    public void testNotAbsoluteFileUriPath() throws Exception {
        assertEquals(2, KeystoreValidator.isValidKeystore("file:///../nonrelativeURI" , TestUtils.mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void testNoEncodingCharsUrlPath() throws Exception {
        assertEquals(4, KeystoreValidator.isValidKeystore("http://www.google.com/needs^encoding", TestUtils.mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void testEncodedInvalidUrlPath() throws Exception {
        assertEquals(2, KeystoreValidator.isValidKeystore("http://www.google.com/needs%5Eencoding", TestUtils.mockKeystorePassword.toCharArray(), new String[]{"JKS", "CASEEXACTJKS", "JCEKS", "PKCS12"}));
    }

    @Test
    public void isValidReadableFileTestValidFile() throws Exception {
        File testFile = tempFolder.newFile();
        assertTrue(KeystoreValidator.isValidReadableFile(testFile.getAbsolutePath()));
    }

    @Test
    public void isValidReadableFileTestFileNotExist() throws Exception {
        assertFalse(KeystoreValidator.isValidReadableFile("non-existing-file.txt"));
    }

    @Test
    public void isValidReadableFileTestDirectory() throws Exception {
        assertFalse(KeystoreValidator.isValidReadableFile(tempFolder.getRoot().getAbsolutePath()));
    }

    @Test
    public void isValidReadableFileTestFileNotReadable() throws Exception{
        String OS = System.getProperty("os.name");

        File unreadable = MockFileBuilder.makeUnreadableFileAtBaseDir(tempFolder);
        //Added so test would be ignored in Docker Containers
        assumeTrue(unreadable.canRead() == false);
        if (!OS.contains("Windows")){
            assertFalse(KeystoreValidator.isValidReadableFile(unreadable.getAbsolutePath()));
        } else { // windows isn't able to setReadable / setExecutable flags
            assertFalse(unreadable.canWrite());
        }
        revertUnreadableFile(unreadable.getAbsolutePath());
    }

    @Test
    public void isValidAccessibleUrlTestValidUrl() throws Exception {
        assertTrue(KeystoreValidator.isValidAccessibleUrl("http://www.google.com"));
    }

    @Test
    public void isValidAccessibleUrlTestInvalidUrl() throws Exception {
        assertFalse(KeystoreValidator.isValidAccessibleUrl("http://www.thisshouldntexisttt.com/thefilehere.jar"));
    }
    //throws IllegalArgumentException?
    //TODO: fix code to make this test succeed.
/*    @Test
    public void isValidAccessibleUrlTestInvalidProtocol() throws Exception {
        assertFalse(KeystoreUtils.isValidAccessibleUrl("http::::://www.google.com"));
    }*/
}
