package com.redhat.installer.tests.asconfiguration.vault.validator;

import com.redhat.installer.asconfiguration.vault.validator.VaultValidator;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by thauser on 1/30/14.
 */
public class VaultValidatorTest extends DataValidatorTester
{
    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "vault.path.existing");
        dv = new VaultValidator();
        idata.setVariable("vault.keystorepwd", "something");
    }

    @Test
    public void testExistingEmptyFile() throws Exception{
        File existing = tempFolder.newFile();
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        idata.setVariable("vault.keystoreloc", existing.getAbsolutePath());
        assertStatusError();
     //   assertLangpack("keystore.validator.file.is.empty");
    }

    /**
     * This tests that the validation can move up the dir tree until it finds an existing, writable directory
     * Even if this is the case, a warning is still printed to the user.
     * @throws Exception
     */
    @Test
    public void testNonExistingParent() throws Exception{
        String noParent = idata.getInstallPath() + File.separator + "somenonexistingparent" + File.separator + "mock-keystore.keystore";
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        idata.setVariable("vault.keystoreloc", noParent);
        assertStatusWarning();
    }

    @Test
    public void testExistingParentNonExistingFile() throws Exception {
        File nonexist = new File(tempFolder.getRoot(), "doesnt-exist");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        idata.setVariable("vault.keystoreloc", nonexist.getAbsolutePath());
        assertStatusOk();
    }

    @Test
    public void testDefaultDirectoryExclusion() throws Exception {
        String nonExistingParent = idata.getInstallPath() + "/this/path/doesnt/exist/keystore.keystore";
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        idata.setVariable("vault.keystoreloc", nonExistingParent);
        idata.setVariable("vault.keystoreloc.default", nonExistingParent);
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        assertStatusOk();
    }

    @Test
    public void testExistingPathButNoFile() throws Exception {
        String path = idata.getInstallPath();
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        idata.setVariable("vault.keystoreloc", path);
        idata.setVariable("vault.keystoreloc.default", path);
        assertStatusError();
    }

    @Test
    public void testNoExistingPathTwoLevelsDeep() throws Exception {
        String keystorePath = tempFolder.getRoot().getAbsolutePath() + File.separator + idata.getInstallPath() + File.separator + "fakedir" + File.separator + "secondfakedir" + File.separator + "keystore.jks";
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        idata.setVariable("vault.keystoreloc", keystorePath);
        idata.setVariable("vault.keystoreloc.default", keystorePath);
        assertStatusOk();
    }

    @Test
    public void testUserSelectsSamePathForBoth() throws Exception {
        String keystorePath = tempFolder.getRoot().getAbsolutePath() + File.separator + idata.getInstallPath() + File.separator + "fakedir" + File.separator + "secondfakedir" + File.separator + "keystore.jks";
        idata.setVariable("vault.keystorepwd","asdqwe123`");
        idata.setVariable("vault.keystoreloc", keystorePath);
        idata.setVariable("vault.encrdir", keystorePath);
        idata.setVariable("vault.keystoreloc.default", keystorePath);
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusError();
    }

    @Test
    public void testJCEKSKeystoreDifferentAliasSecretKey() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"differentAlias", TestUtils.KeyType.SECRET_KEY, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        idata.setVariable("vault.requires.secret.key", "false");
        assertStatusOk();
    }

    @Test
    public void testJCEKSKeystoreSameAliasSecretKey() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"testVault", TestUtils.KeyType.SECRET_KEY, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        idata.setVariable("vault.requires.secret.key", "false");
        assertStatusOk();
    }


    @Test
    public void testJCEKSKeystoreDifferentAliasPrivateKey() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"differentAlias", TestUtils.KeyType.KEY_PAIR, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        idata.setVariable("vault.requires.secret.key", "false");
        assertStatusOk();
    }

    @Test
    public void testJCEKSKeystoreSameAliasPrivateKey() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"testVault", TestUtils.KeyType.KEY_PAIR, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        idata.setVariable("vault.requires.secret.key", "false");
        assertStatusOk();
    }

    @Test
    public void testJKSKeystoreDifferentAliasPrivateKey() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"differentAlias", TestUtils.KeyType.KEY_PAIR, "jks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        idata.setVariable("vault.requires.secret.key", "false");
        assertStatusError();
    }

    @Test
    public void testJKSKeystoreSameAliasPrivateKey() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"testVault", TestUtils.KeyType.KEY_PAIR, "jks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JKS,CASEEXACTJKS,JCEKS");
        idata.setVariable("vault.requires.secret.key", "false");
        assertStatusOk();
    }

    @Test
    public void testJCEKSKeystoreDifferentAliasSecretKeyProductRequiresSecret() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"differentAlias", TestUtils.KeyType.SECRET_KEY, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusOk();
    }

    @Test
    public void testJCEKSKeystoreSameAliasSecretKeyProductRequiresSecret() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"testVault", TestUtils.KeyType.SECRET_KEY, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusOk();
    }


    @Test
    public void testJCEKSKeystoreDifferentAliasPrivateKeyProductRequiresSecret() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"differentAlias", TestUtils.KeyType.KEY_PAIR, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusOk();
    }

    @Test
    public void testJCEKSKeystoreSameAliasPrivateKeyProductRequiresSecret() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jceks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"testVault", TestUtils.KeyType.KEY_PAIR, "jceks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusError();
    }

    @Test
    public void testJKSKeystoreDifferentAliasPrivateKeyProductRequiresSecret() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"differentAlias", TestUtils.KeyType.KEY_PAIR, "jks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusError();
    }

    @Test
    public void testJKSKeystoreSameAliasPrivateKeyProductRequiresSecret() throws Exception {
        File tempKeystore = TestUtils.createMockKeystore(tempFolder, "jks");
        TestUtils.createKeyInMockKeystore(tempKeystore.getAbsolutePath(),"testVault", TestUtils.KeyType.KEY_PAIR, "jks");
        idata.setVariable("vault.keystoreloc", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.keystorepwd", TestUtils.mockKeystorePassword);
        idata.setVariable("vault.keystoreloc.default", tempKeystore.getAbsolutePath());
        idata.setVariable("vault.alias", "testVault");
        idata.setVariable("vault.allowed.keystore.types", "JCEKS");
        idata.setVariable("vault.requires.secret.key", "true");
        assertStatusError();
    }

}
