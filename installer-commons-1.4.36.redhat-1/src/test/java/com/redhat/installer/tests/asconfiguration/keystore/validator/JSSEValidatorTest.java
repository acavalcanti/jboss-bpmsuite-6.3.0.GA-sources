package com.redhat.installer.tests.asconfiguration.keystore.validator;

import com.redhat.installer.asconfiguration.keystore.validator.JSSEValidator;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by thauser on 1/30/14.
 */
public class JSSEValidatorTest extends DataValidatorTester {

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder,"securitydomain.jsse.keystore.reqs",
                "securitydomain.jsse.keystore.passincorrect",
                "securitydomain.jsse.keystore.inaccessible",
                "securitydomain.jsse.truststore.reqs",
                "securitydomain.jsse.truststore.passincorrect",
                "securitydomain.jsse.truststore.inaccessible",
                "securitydomain.jsse.requirements",
                "securitydomain.jsse.keystore.wrongtype",
                "securitydomain.jsse.truststore.wrongtype");

        idata.setVariable("securityDomainAddJsse", "true");
        idata.setVariable("securityDomainJsseAddKeystore", "true");
        idata.setVariable("securityDomainJsseAddTruststore", "true");
        idata.setVariable("securitydomain.jsse.keystore.type", "JKS");
        idata.setVariable("securitydomain.jsse.truststore.type","JKS");



        dv = new JSSEValidator();
    }

    @Test
    public void testNoJsseSelected() throws Exception {
        idata.setVariable("securityDomainAddJsse", "false");
        assertStatusOk();
    }

    @Test
    public void testValidKeystore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusOk();
    }

    @Test
    public void testValidTruststore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusError();
    }

    @Test
    public void testValidKeystoreAndTruststore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusOk();
    }

    @Test
    public void testNoValidKeystoreOrTruststore() throws Exception {
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        assertStatusError();
        assertLangpack("securitydomain.jsse.requirements");
    }


    @Test
    public void testWrongKeystorePassword() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        idata.setVariable("securitydomain.jsse.keystore.password", "wrongpassword");
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.keystore.passincorrect");
    }

    @Test
    public void testWrongKeystorePasswordWithValidTruststore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securitydomain.jsse.keystore.password", "wrongpassword");
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.keystore.passincorrect");
    }

    @Test
    public void testWrongTruststorePassword() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        idata.setVariable("securitydomain.jsse.truststore.password", "wrongpassword");
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.truststore.passincorrect");
    }

    @Test
    public void testWrongTruststorePasswordWithValidKeystore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.truststore.password", "wrongpassword");
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.truststore.passincorrect");
    }

    @Test
    public void testWrongTruststorePath() throws Exception {
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", "incorrectpath'");
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.truststore.inaccessible");
    }

    @Test
    public void testWrongTruststorePathWithValidKeystore() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", "incorrectpath'");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.truststore.inaccessible");
    }

    @Test
    public void testWrongKeystorePath() throws Exception {
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", "incorrectpath");
        assertStatusWarning();
        assertLangpack("securitydomain.jsse.keystore.inaccessible");
    }

    @Test
    public void testFileUriPath() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        String normalizedKeystore = keystore.getAbsolutePath().replace(File.separator,"/");
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", "file:///"+normalizedKeystore);
        assertStatusOk();
    }

    @Test
    public void testUriEncodingError() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"jks","goofy\\ksname");
        //String invalidKeystoreURI = keystore.getAbsolutePath().replace(File.separator,"\\");
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", "file:///"+keystore.getAbsolutePath());
        assertStatusError();
    }


    @Test
    public void testInvalidFileUriPath() throws Exception {
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", "file:///doesn'texist");
        assertStatusWarning();
    }

    @Test
    public void testWrongKeystoreTypeSpecified() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.keystore.type", "PKCS12");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddTruststore","false");
        assertStatusError();
        assertLangpack("securitydomain.jsse.keystore.wrongtype");
    }

    @Test
    public void testWrongTruststoreTypeSpecified() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.truststore.type", "PKCS12");
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        assertStatusError(); // wrong type throws error.
        assertLangpack("securitydomain.jsse.truststore.wrongtype");
    }
    @Test
    public void testPKCS12KeystoreType() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"PKCS12");
        idata.setVariable("securitydomain.jsse.keystore.type", "PKCS12");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        assertStatusOk();
    }

    @Test
    public void testJCEKSKeystoreType() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"JCEKS");
        idata.setVariable("securitydomain.jsse.keystore.type", "JCEKS");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        assertStatusOk();
    }

    @Test
    public void testJKSKeystoreType() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.keystore.type", "JKS");
        idata.setVariable("securitydomain.jsse.keystore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.keystore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddTruststore", "false");
        assertStatusOk();
    }

    @Test
    public void testPKCS12TruststoreType() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"PKCS12");
        idata.setVariable("securitydomain.jsse.truststore.type", "PKCS12");
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        assertStatusOk();
    }

    @Test
    public void testJCEKSTruststoreType() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder,"JCEKS");
        idata.setVariable("securitydomain.jsse.truststore.type", "JCEKS");
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        assertStatusOk();
    }

    @Test
    public void testJKSTruststoreType() throws Exception {
        File keystore = TestUtils.createMockKeystore(tempFolder);
        idata.setVariable("securitydomain.jsse.truststore.type", "JKS");
        idata.setVariable("securitydomain.jsse.truststore.password", TestUtils.mockKeystorePassword);
        idata.setVariable("securitydomain.jsse.truststore.url", keystore.getAbsolutePath());
        idata.setVariable("securityDomainJsseAddKeystore", "false");
        assertStatusOk();
    }
}
