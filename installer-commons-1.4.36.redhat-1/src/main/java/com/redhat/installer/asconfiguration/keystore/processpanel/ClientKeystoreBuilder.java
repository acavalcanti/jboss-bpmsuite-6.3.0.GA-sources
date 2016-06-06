package com.redhat.installer.asconfiguration.keystore.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import com.redhat.installer.installation.util.InstallationUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Responsible for building any arbitrary number of keystores, and importing the existing server's certificate
 * into each one.
 * Arguments:
 * --number=x The number of client keystores to build.
 * --server-path=<path> Path to server keystore.
 * --client-path=<path> Path to folder that will contain client keystores.
 * --cert-path=<path>
 * --client-password=<password>
 * --server-password=<password>
 * --server-alias=<alias>
 * <p/>
 * Created by fcanas on 4/10/14.
 */
public class ClientKeystoreBuilder {
    private static final String ARG_NUMBER = "number";
    private static final String ARG_CLIENT_PATH = "client-path";
    private static final String ARG_CLIENT_PWD = "client-password";
    private static final String ARG_CERT_PATH = "cert-path";
    private static final String CLIENT_NAME_TEMPLATE = "client%s.keystore.jks";
    private static final String ARG_SERVER_ALIAS = "server-alias";

    private static AutomatedInstallData idata;
    private static AbstractUIProcessHandler mHandler;

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        idata = AutomatedInstallData.getInstance();
        boolean defaultGenerate = idata.getRules().isConditionTrue("generate.client.keystores");
        mHandler = handler;
        int numberOfKeystores;
        String certPath, clientsPath, clientPassword, serverAlias;


        ArgumentParser parser = new ArgumentParser();
        parser.deepParse(args);

        if (!parser.hasProperty(ARG_NUMBER) || !parser.hasProperty(ARG_CLIENT_PATH)) {
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("ClientKeystoreGenerator.args.error"), true);
            return false;
        }

        try {
            numberOfKeystores = Integer.parseInt(parser.getStringProperty(ARG_NUMBER));
            certPath = parser.getStringProperty(ARG_CERT_PATH);
            clientsPath = parser.getStringProperty(ARG_CLIENT_PATH);
            clientPassword = parser.getStringProperty(ARG_CLIENT_PWD);
            serverAlias = parser.getStringProperty(ARG_SERVER_ALIAS);
        } catch (NumberFormatException e) {
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("ClientKeystoreGenerator.numeric.arg.error"), true);
            return false;
        } catch (NoSuchElementException e) {
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("ClientKeystoreGenerator.missing.args.error"), true);
            return false;
        }


        return buildClientKeystores(defaultGenerate, numberOfKeystores, clientsPath, certPath, clientPassword, serverAlias);
    }

    /**
     * Sets up all of the keystores by generating them, then importing the given certificate into them.
     *
     * @param numberOfKeystores
     * @param pathToClients     The folder where the clients will be generated.
     * @param pathToCertificate Path to the certificate file to import into clients.
     * @param clientPassword
     * @param serverAlias
     * @return
     */
    private static boolean buildClientKeystores(boolean defaultGenerate, int numberOfKeystores,
                                                String pathToClients,
                                                String pathToCertificate,
                                                String clientPassword,
                                                String serverAlias) {


           /*
            Either pathToClients contains:
            1) a Single valid keystore
            2) a directory containing files that are all valid keystores
            3) an empty / non-existent directory
             */
        File clientLocation = new File(pathToClients);

        if (clientLocation.exists()) {
            if (clientLocation.isFile()) {
                // single valid keystore. import the certificate into it after checking that the alias we're using doesn't exist, and modifying it
                // if it does
                Debug.log("importing server certificate into  user specified client keystore: " + clientLocation.getAbsolutePath());
                if (aliasClash(clientLocation, clientPassword, serverAlias)){
                    serverAlias += System.currentTimeMillis();
                }

                 if (!importServerCertificate(clientLocation.getAbsolutePath(), clientPassword, pathToCertificate, serverAlias)) {
                    return false;
                }
            } else if (clientLocation.isDirectory()) {
                if (clientLocation.listFiles().length == 0) {
                    // generate keystores within.
                    createKeystoresInDirectory(numberOfKeystores, pathToClients, clientPassword, pathToCertificate, serverAlias);
                    // only add the files to remove if the user isn't specifying their own keystore.
                    InstallationUtilities.addFileToCleanupList(pathToClients);
                } else {
                    // a directory in which all files are valid keystores. iterate and add certificate to each
                    for (File keystore : clientLocation.listFiles()) {
                        if (keystore.isDirectory()){
                            continue;
                        }
                        Debug.log("Importing server certificate into keystore: " + keystore.getAbsolutePath());
                        if (aliasClash(keystore, clientPassword, serverAlias)) {
                            serverAlias += System.currentTimeMillis();
                        }
                        if (!importServerCertificate(keystore.getAbsolutePath(), clientPassword, pathToCertificate, serverAlias)) {
                            return false;
                        }
                    }
                }
            }
        } else if (!clientLocation.exists()) {
            // create the directory
            clientLocation.mkdirs();
            // generate keystores within.
            if (!createKeystoresInDirectory(numberOfKeystores, pathToClients, clientPassword, pathToCertificate, serverAlias)){
                return false;
            }
            // only add the files to remove if the user isn't specifying their own keystore.
            InstallationUtilities.addFileToCleanupList(pathToClients);
        }
        return true;
    }

    private static boolean createKeystoresInDirectory(int numberOfKeystores, String destinationPath, String clientPassword, String pathToCertificate, String serverAlias){
        for (int i = 0; i < numberOfKeystores; i++) {
            String alias = String.format(CLIENT_NAME_TEMPLATE, i);
            String path = destinationPath + File.separator + alias;
            Debug.log("Generating client keystore: " + new File(path).getAbsolutePath());

            if (!buildClientKeystore(path, clientPassword, alias)) {
                return false;
            }

            Debug.log("Importing Certificate:");
            if (!importServerCertificate(path, clientPassword, pathToCertificate, serverAlias)) {
                return false;
            }
        }
        return true;
    }

    private static boolean aliasClash(File keystore, String password, String alias) {
        KeyStore ks = null;
        FileInputStream keystoreStream = null;
        try {
            // this shouldn't happen if our validator works correctly
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            keystoreStream = new FileInputStream(keystore.getAbsolutePath());
            ks.load(keystoreStream, password.toCharArray());
            if (ks.containsAlias(alias)) {
                return true;
            } else {
                return false;
            }
        } catch (KeyStoreException e) {
            Debug.log(e.getMessage());
            return false;
        } catch (CertificateException e) {
            Debug.log(e.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            Debug.log(e.getMessage());
            return false;
        } catch (FileNotFoundException e) {
            Debug.log(e.getMessage());
            return false;
        } catch (IOException e) {
            Debug.log(e.getMessage());
            return false;
        } finally {
            try {
                keystoreStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Builds a single client keystore.
     * Cmd: -genkeypair -keystore client.path -storepass clientpassword -keyalg RSA
     * -keysize 1024 -alias client.alias -storetype jceks -validity 36500 -dname
     * CN=Picketbox Vault,OU=JBoss,O=RedHat,L=Westford,ST=Mass,C=US
     *
     * @param location
     * @param password
     * @param alias
     */
    private static boolean buildClientKeystore(String location, String password, String alias) {
        boolean result = false;
        Map<String, String> genKeyStoreArgsMap = new HashMap<String, String>();
        genKeyStoreArgsMap.put("-genkeypair", "");
        genKeyStoreArgsMap.put("-keystore", location);
        genKeyStoreArgsMap.put("-storepass", password);
        genKeyStoreArgsMap.put("-keypass", password);
        genKeyStoreArgsMap.put("-keysize", "1024");
        genKeyStoreArgsMap.put("-keyalg", "RSA");
        genKeyStoreArgsMap.put("-alias", alias);
        genKeyStoreArgsMap.put("-storetype", "jks");
        genKeyStoreArgsMap.put("-validity", "36500");
        genKeyStoreArgsMap.put("-dname", "CN=Picketbox Vault,OU=JBoss,O=RedHat,L=Westford,ST=Mass,C=US");

        ArrayList<String> args = mapToArrayList(genKeyStoreArgsMap);

        try {
            result = KeystoreGenerator.runKeytool(args);
        } catch (Exception e) {
            Debug.log(e.getStackTrace());
        }
        return result;
    }

    /**
     * Imports the given certificate into an existing client keystore.
     * Cmd:-importcert -keystore client-n.jks -storepass blah -file blah.cert -alias <server keystore alias>
     *
     * @param pathToClient
     * @param clientPassword
     * @param pathToCertificate
     * @param serverAlias
     * @return
     */
    private static boolean importServerCertificate(String pathToClient,
                                                   String clientPassword,
                                                   String pathToCertificate,
                                                   String serverAlias) {
        Map<String, String> importKeystoreArgs = new HashMap<String, String>();
        importKeystoreArgs.put("-importcert", "");
        importKeystoreArgs.put("-keystore", pathToClient);
        importKeystoreArgs.put("-storepass", clientPassword);
        importKeystoreArgs.put("-keypass", clientPassword);
        importKeystoreArgs.put("-file", pathToCertificate);
        importKeystoreArgs.put("-alias", serverAlias);

        ArrayList<String> args = mapToArrayList(importKeystoreArgs);

        Debug.log("Importing Server Keystore Certificate");
        try {
            KeystoreGenerator.runKeytool(args);
        } catch (Exception e) {
            Debug.log(e.getStackTrace());
            return false;
        }
        return true;
    }

    /**
     * Unpacks a map into an array list made up of its keys and values, if the value
     * is not empty.
     *
     * @param map
     * @return [key1, value1, key2, value2, ...]
     */
    private static ArrayList<String> mapToArrayList(Map<String, String> map) {
        ArrayList<String> list = new ArrayList<String>();
        for (String key : map.keySet()) {
            list.add(key);
            String val = map.get(key);
            if (val != null && !val.isEmpty()) list.add(val);
        }
        return list;
    }

    public static String getClientKeystoreTemplate() {
        return CLIENT_NAME_TEMPLATE;
    }
}
