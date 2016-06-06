package com.redhat.installer.tests;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.VariableCondition;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.framework.mock.MockFileBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.rules.TemporaryFolder;
import sun.security.x509.*;

import javax.crypto.KeyGenerator;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;

/**
 * Collection of various actions involving creating files / directories and more for testing purposes
 * Created by thauser on 1/29/14.
 */
public class TestUtils {
    public static final String ldapWorkingDir = "ldap-server";
    public static final String ldapTestUrl =  "ldap://localhost:10389";
    public static final String ldapAdminDn = "uid=admin,ou=system";
    public static final String ldapAdminPassword = "secret";
    public static final String mockKeystoreFilename = "mock-keystore.keystore";
    public static final String mockKeystorePassword = "testpass";
    public static final String mockKeystoreAlias = "testalias";
    public static final String mockSettingsPath = "mock-settings.xml";
    public static final String testLogPath = "test-installationlog.txt";
    public static final String testPropertiesPath = "test-properties.properties";
    public static final String layersConfPath = "/modules/layers.conf";
    public static final String productConfPath = "/bin/product.conf";
    public static final String testLogFilename = "test-log.txt";
    public static final String SCRIPTS_DIR = "/bin/";
    public static final String STANDALONE_CONFIG_DIR = "/standalone/configuration/";
    public static final String DOMAIN_CONFIG_DIR = "/domain/configuration/";


    public static final double javaVersion = getJavaVersion();

    public enum KeyType {SECRET_KEY, KEY_PAIR}


    /**
     * makes the given file readable again
     *
     * @param path
     * @throws Exception
     */
    public static void revertUnreadableFile(String path) throws Exception {
        File file = new File(path);
        file.setReadable(true);
        file.setWritable(true);
        file.setExecutable(true);
    }

    /**
     * Convenience method to create a product.conf in the appropriate location
     */
    public static void createProductConf(TemporaryFolder tempFolder, String products){
        MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/bin/product.conf", "slot=" + products);
    }

    /**
     * Convenience method to create a layers.conf in the appropriate location
     */
    public static void createLayersConf(TemporaryFolder tempFolder, String layers){
        MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/modules/layers.conf", "layers=" + layers);
    }

    /**
     * Convenience method to create a version.txt in the appropriate location
     */
    public static void createVersionTxt(TemporaryFolder tempFolder, String version){
        MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/version.txt", version);
    }


    /**
     * Convenience method to create required files for EAP to exist
     */
    public static void createEAPScripts(TemporaryFolder tempFolder) throws Exception{
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/standalone/configuration/standalone.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/bin/standalone.sh");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/bin/domain.sh");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/bin/add-user.sh");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/domain/configuration/domain.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/domain/configuration/host.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/standalone/configuration/standalone-ha.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/standalone/configuration/standalone-full-ha.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/standalone/configuration/standalone-full.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/standalone/configuration/standalone-osgi.xml");
    }

    /**
     * Hack to destroy the internal reference to the idata object
     * @throws Exception
     */
    public static void destroyIdataSingleton() throws Exception{
        Class<?> clazz = AutomatedInstallData.class;
        Field self = clazz.getDeclaredField("self");
        self.setAccessible(true);
        self.set(null, null);
    }

/*
    public static void destroyProcessPanelHelperIdata() throws Exception {
        Class<?> clazz = ProcessPanelHelper.class;
        Field
    }
*/

    /**
     * Creates a mock langpack in which keys == values. Use to set idata's langpack field.
     * @param strings
     * @return
     * @throws Exception
     */

/*    public static LocaleDatabase createMockLangpack(String ... strings) throws Exception {
        ArrayList<String> langpack = new ArrayList<String>(10);
        langpack.add("<langpack>");
        File specs = new File(TestPaths.MOCK_INSTALLER_SPECS_DIR);
        if (!specs.exists()) {
            MockDirSetter.makeMockSpecsDir();
        }
        for (String s : strings) {
            langpack.add("<str id=\""+s+"\" txt=\""+s+"\"/>");
        }
        langpack.add("</langpack>");

        MockFileBuilder.makeNewFileFromStrings(TestPaths.MOCK_LANGPACK_PATH, langpack.toArray(new String[langpack.size()]));
        LocaleDatabase ld = new LocaleDatabase(new FileInputStream(TestPaths.MOCK_LANGPACK_PATH));
        return ld;
    }*/

    /**
     * Create a mock langpack which can assign values to keys. If the string is not in the form id=txt, it will assign the string to itself, ie
     * s=s
     * @param strings all of the strings which should be in the langpack
     * @return
     * @throws Exception
     */
    public static LocaleDatabase createMockLangpack (TemporaryFolder tempFolder, String ... strings) throws Exception {
        ArrayList<String> langpack = new ArrayList<String>(10);
        langpack.add("<langpack>");
        for (String s : strings){
            String[] splitString = s.split("=", 2);
            if (splitString.length == 1){
                langpack.add("<str id=\""+s+"\" txt=\""+s+"\"/>");
            } else {
                String id = splitString[0];
                String txt = splitString[1];
                langpack.add("<str id=\"" + id + "\" txt=\"" + txt + "\"/>");
            }
        }
        langpack.add("</langpack>");
        File langpackFile = MockFileBuilder.makeNewFileFromStrings(tempFolder, langpack.toArray(new String[langpack.size()]));
        LocaleDatabase ld = new LocaleDatabase(new FileInputStream(langpackFile));
        return ld;
    }

    /**
     * Creates a mock keystore of the given type, under INSTALL_PATH/path
     * @param type
     * @throws Exception
     */
    public static File createMockKeystore(TemporaryFolder tempFolder, String type, String path) throws Exception{
        File newKs = null;
        if (path != null){
            newKs = tempFolder.newFile(path);
        } else {
            newKs = tempFolder.newFile();
        }
        KeyStore ks = KeyStore.getInstance(type);
        ks.load(null, TestUtils.mockKeystorePassword.toCharArray());
        KeyStore.ProtectionParameter prot = new KeyStore.PasswordProtection(TestUtils.mockKeystorePassword.toCharArray());
        KeyStore.Builder builder = KeyStore.Builder.newInstance(ks, prot);
        KeyStore finalKs = builder.getKeyStore();
        FileOutputStream fos = new FileOutputStream(newKs);
        finalKs.store(fos, TestUtils.mockKeystorePassword.toCharArray());
        return newKs;
    }

    public static File createMockKeystore(TemporaryFolder tempFolder) throws Exception {
        return createMockKeystore(tempFolder, "jks", null);
    }

    public static File createMockKeystore(TemporaryFolder tempFolder, String type) throws Exception {
        return createMockKeystore(tempFolder, type, null);
    }

    public static void createKeyInMockKeystore(String mockKeystorePath, String keyAlias, KeyType type, String keystoreAlgorithm) {
        try {
            KeyStore mockKeystore = KeyStore.getInstance(keystoreAlgorithm);
            FileInputStream keystoreInputSteam = new FileInputStream(mockKeystorePath);
            mockKeystore.load(keystoreInputSteam, mockKeystorePassword.toCharArray());
            keystoreInputSteam.close();

            KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(mockKeystorePassword.toCharArray());
            if (type == KeyType.KEY_PAIR) {
                mockKeystore.setEntry(keyAlias,  generatePrivateKeyEntry(), passwordProtection);
            } else if (type == KeyType.SECRET_KEY) {
                mockKeystore.setEntry(keyAlias, generateSecretKeyEntry(), passwordProtection);
            }
            FileOutputStream keystoreOutputStream = new FileOutputStream(mockKeystorePath);
            mockKeystore.store(keystoreOutputStream, mockKeystorePassword.toCharArray());
            keystoreOutputStream.close();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static KeyStore.Entry generateSecretKeyEntry(){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            KeyStore.Entry secretKeyEntry = new KeyStore.SecretKeyEntry(keyGenerator.generateKey());
            return secretKeyEntry;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static KeyStore.Entry generatePrivateKeyEntry(){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Certificate[] certChain = new Certificate[]{generateCertifcate(keyPair)};
            KeyStore.Entry privateKeyEntry = new KeyStore.PrivateKeyEntry(keyPair.getPrivate(), certChain);
            return privateKeyEntry;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Certificate generateCertifcate(KeyPair keypair){
        X509CertImpl cert = null;
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + 365);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());

        try {
            X500Name owner = new X500Name("CN=Unkown,OU=Unkown,O=Unkown,L=Unkown,ST=Unkown,C=Unkown");
            info.set(X509CertInfo.VALIDITY, interval);
            info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
            if (javaVersion > 1.7 ) {
                info.set(X509CertInfo.SUBJECT, owner);
                info.set(X509CertInfo.ISSUER, owner);
            }
            else {
                info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
                info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
            }
            info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
            info.set(X509CertInfo.KEY, new CertificateX509Key(keypair.getPublic()));
            AlgorithmId algo = new AlgorithmId(AlgorithmId.DSA_oid);
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
            cert = new X509CertImpl(info);
            cert.sign(keypair.getPrivate(), "DSA");
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return cert;
    }

    public static Set<String> getFileLinesAsSet(String filePath) throws Exception {
        Set<String> set = new HashSet<String>();
        BufferedReader r = new BufferedReader(new FileReader(new File(filePath)));
        String line ="";
        while ((line = r.readLine()) != null){
            set.add(line);
        }

        return set;
    }

    public static void instantiateClassThroughReflection(Class<?> clazz, String myClassName) throws NoSuchMethodException, InstantiationException, IllegalAccessException, ExceptionInInitializerError, ClassNotFoundException{
        boolean result;
        ClassLoader loader = clazz.getClassLoader();
        Class procClass = loader.loadClass(myClassName);

        Object o = procClass.newInstance();
        Method m = procClass.getMethod("run", new Class[]{AbstractUIProcessHandler.class,
                String[].class});


    }

    public static void destroyVariableSubstitutorIdata() throws IllegalAccessException, NoSuchFieldException {
        Class<?> clazz = VariableSubstitutor.class;
        Field idata = clazz.getDeclaredField("idata");
        idata.setAccessible(true);
        Field mods = Field.class.getDeclaredField("modifiers"); // hackily remove final status
        mods.setAccessible(true);
        mods.setInt(idata, idata.getModifiers() & ~Modifier.FINAL); // whoa
        idata.set(null, null);
    }


    public static void setVariableSubstitutorIdata(AutomatedInstallData idata) throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = VariableSubstitutor.class;
        Field idataField = clazz.getDeclaredField("idata");
        idataField.setAccessible(true);
        Field mods = Field.class.getDeclaredField("modifiers");
        mods.setAccessible(true);
        mods.setInt(idataField, idataField.getModifiers() & ~Modifier.FINAL);
        idataField.set(null,idata);

    }

    public static VariableCondition createVariableCondition(String condId, String condVarname, String condValue) {
        VariableCondition testCond = new VariableCondition();
        testCond.setId(condId);
        testCond.setVariablename(condVarname);
        testCond.setValue(condValue);
        return testCond;
    }

    public static void insertVariableCondition(Map<String, Condition> condMap, String condId, String condVarname, String condValue) {
        VariableCondition testCond = createVariableCondition(condId, condVarname, condValue);
        condMap.put(condId, testCond);
    }

    private static double getJavaVersion(){
        String version = System.getProperty("java.version");
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos+1);
        return Double.parseDouble (version.substring (0, pos));
    }

    public static Elements getXMLTagsFromConfig(String configFile, String selector) throws Exception {
        Document document = Jsoup.parse(new File(configFile), null);
        return  document.select(selector);
    }
}
