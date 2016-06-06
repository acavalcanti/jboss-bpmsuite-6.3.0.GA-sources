package com.redhat.installer.asconfiguration.keystore.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.keystore.validator.KeystoreValidator;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import com.redhat.installer.installation.util.InstallationUtilities;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for generating a keystore for use with password vaults
 * Since we know that this class is only going to be used within ProcessPanel.spec, we can make assumptions about the format /
 * contents of the arguements.
 *
 * @author thauser
 */
public class KeystoreGenerator {

    private static final String IMPORTCERT = "-importcert";
    private static final String EXPORTCERT = "-exportcert";
    private static final String FILE = "-file";
    private static final String GENKEY = "-genkeypair";
    private static final String SECKEY = "-genseckey";
    private static final String KEYSTORE = "-keystore";
    private static final String KEY_PASS = "-keypass";
    private static final String KEY_SIZE = "-keysize";
    private static final String ALIAS = "-alias";
    private static final String STORE_PASS = "-storepass";
    private static final String DNAME = "-dname";
    private static final String VALIDITY = "-validity";
    private static final String OPERATION = "operation";
    private static final String PARAMETER = "parameter";
    private static final String ALLOW_EXISTING = "allow-existing";
    private static final String PUT_LOCATION_IN_PROPS = "put-location-in-props";
    private static final String[] GENKEY_PARAMS = new String[] {KEYSTORE, KEY_SIZE, ALIAS, STORE_PASS, DNAME, VALIDITY};
    private static final String[] SECKEY_PARAMS = new String[] {KEYSTORE, ALIAS, STORE_PASS, VALIDITY};
    private static final String[] IMPORTEXPORT_PARAMS = new String[] {KEYSTORE, STORE_PASS, FILE, ALIAS};

    private static BufferedReader in;
    private static BufferedWriter out;

    private static AutomatedInstallData idata;
    private static AbstractUIProcessHandler mHandler;

    /**
     * A semi-rigid wrapper on the keytool java binary. Designed to be called from ProcessPanel.Spec.xml. Can generate new keystores, as well as import and export certificate files from existing keystores.
     *
     * @param handler the IzPack AbstractUIProcessHandler. Allows output to be printed to the IzPack ProcessPanel or console
     * @param args the set of args parsed from the ProcessPanel.Spec.xml
     * @return true if the operation was successful, false otherwise
     */
    public static boolean run(AbstractUIProcessHandler handler, String[] args) {

        idata = AutomatedInstallData.getInstance();
        mHandler = handler;
        ArgumentParser parser = new ArgumentParser();
        parser.setListDelimiter('\uFFFF'); // we do this so that the dname property is not turned into a list.
        parser.deepParse(args);

        // make sure there's at least some operation we support being specified in the spec as well as parameters for said argument
        if (!parser.hasProperty(PARAMETER) || !parser.hasProperty(OPERATION)) {
            // fail
            //ProcessPanelHelper.printToPanel(mHandler,  "KeystoreGenerator called with insufficient parameters.", true);
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("KeystoreGenerator.paramerror"), true);
            return false;
        }

        String operation = parser.getStringProperty(OPERATION);
        ArrayList<String> keytoolArguments = new ArrayList<String>();

        if (operation.equals(IMPORTCERT) || operation.equals(EXPORTCERT)){
           if (missingRequiredParams(parser, IMPORTEXPORT_PARAMS)){
               ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("KeystoreGenerator.paramerror"), true);
               return false;
           }
           if (!keystoreExists(parser)){
               // this can't be allowed, importing / exporting with a keystore that doesn't exists doesn't make sense
               ProcessPanelHelper.printToPanel(mHandler,String.format(idata.langpack.getString("KeystoreGenerator.notexist.error"),operation,searchForParameter(parser,KEYSTORE)),false);
           }
        } else if (operation.equals(GENKEY)){
            // check for specific missing parameters for a genkey execution
            if (missingRequiredParams(parser, GENKEY_PARAMS)){
                ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("KeystoreGenerator.paramerror"), true);
                return false;
            }

            // if the keystore exists, we have to check and see if this situation is allowed.
            if (keystoreExists(parser)){
                // the situation is allowed. we shortcircuit and assume that the already existing keystore meets our needs
                if (parser.hasProperty(ALLOW_EXISTING)){
                    ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.exists.allowed"), searchForParameter(parser,KEYSTORE)), false);
                    return true;
                } else {
                    // this situation means that the keystore existing at this location was unexpected. Thus, we error out.
                    ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.exists.error"),operation, searchForParameter(parser,KEYSTORE)), true);
                    return false; // nothing to do.
                }
            } else {
                // If the keystore doesn't exist, make sure it's parent paths do.
                makeKeystorePath(parser);
            }
            // if the ProcessPanel doesn't specify an explicit KEY_PASS parameter, the STORE_PASS value is used for that field
            if (!parser.hasProperty(KEY_PASS)){
                keytoolArguments.add(KEY_PASS);
                keytoolArguments.add(searchForParameter(parser, STORE_PASS));
            }
        } else if (operation.equals(SECKEY)){
            // check for specific missing parameters for a genkey execution
            if (missingRequiredParams(parser, SECKEY_PARAMS)){
                ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("KeystoreGenerator.paramerror"), true);
                return false;
            }

            // if the keystore exists, we have to check and see if this situation is allowed.
            if (keystoreExists(parser)){
                // the situation is allowed. we shortcircuit and assume that the already existing keystore meets our needs
                if (parser.hasProperty(ALLOW_EXISTING)){
                    ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.exists.allowed"), searchForParameter(parser,KEYSTORE)), false);
                } else {
                    // this situation means that the keystore existing at this location was unexpected. Thus, we error out.
                    ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.exists.error"),operation, searchForParameter(parser,KEYSTORE)), true);
                    return false; // nothing to do.
                }
            } else {
                // If the keystore doesn't exist, make sure it's parent paths do.
                makeKeystorePath(parser);
            }
            // if the ProcessPanel doesn't specify an explicit KEY_PASS parameter, the STORE_PASS value is used for that field
            if (!parser.hasProperty(KEY_PASS)){
                keytoolArguments.add(KEY_PASS);
                keytoolArguments.add(searchForParameter(parser, STORE_PASS));
            }
        } else {
            // this error means misconfiguration in the ProcessPanel.spec to use an unsupported operation
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("KeystoreGenerator.operation.unsupported"), true);
            return false;
        }
        // add the operation here
        keytoolArguments.add(operation);
        Map<String,String> keytoolParameters = processParameters(parser);
        for (String key : keytoolParameters.keySet()){
            keytoolArguments.add(key);
            keytoolArguments.add(keytoolParameters.get(key));
        }
        if (parser.hasProperty(PUT_LOCATION_IN_PROPS)){
            String keystorePath = searchForParameter(parser,KEYSTORE);
            if (keystorePath != null) {
                // make sure paths use '/' as the separator in properties, for consistency
                keystorePath = keystorePath.replace('\\', '/');
                System.setProperty(parser.getStringProperty(PUT_LOCATION_IN_PROPS), keystorePath);
            }
        }
        return runKeytool(keytoolArguments);
    }

    private static Map<String, String> processParameters(ArgumentParser parser) {
        Map<String,String> retVal = new HashMap<String,String>();
        for (String param : parser.getListProperty(PARAMETER)){
            String key = param.substring(0, param.indexOf(' '));
            String value = extractArgumentValue(param);
            retVal.put(key,value);
        }
        return retVal;
    }

    /**
     * Returns whether the keystore within parser exists or not.
     * @param parser the parser containing the KEYSTORE property
     * @return true if the given keystore exists as a file at the location, false otherwise
     */
    private static boolean keystoreExists(ArgumentParser parser) {
        String keystoreLocation = searchForParameter(parser, KEYSTORE);
        String password = searchForParameter(parser, STORE_PASS);

        if (keystoreLocation == null || password == null) {
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("KeystoreGenerator.paramerror"), true);
            return false;
        }

        // make sure that the keystore doesn't already exist, so that keytool doesn't complain
        switch (KeystoreValidator.isValidKeystore(keystoreLocation, password.toCharArray(), new String[]{"JKS", "JCEKS", "PKCS11", "PKCS12"})) {
            case 0:
            case 1:
            case 3:
                // if the file exists and is a valid keystore, and the property "allow-existing" exists, we are ok. we also just return true immediately.
                return true;
            case 2: // it doesn't exist; just continue.
            case 4:
            case 5:
            default:
                return false;
        }
    }

    /**
     * Tests whether the given keystore path's parents exist, and creates them
     * if they do not so that the keytool doesn't fail when generating the keystore.
     * @param parser
     */
    private static void makeKeystorePath(ArgumentParser parser) {
        String path = searchForParameter(parser, KEYSTORE);
        File keystoreFile = new File(path);
        File parentFile = keystoreFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
    }

    /** Checks for a set of parameters within a list of "required" parameters
     * @param parser the parser that will be searched for the given parameters
     * @param requiredParams the list of required parameters that should exist within parser
     * @return returns false if there's a parameter not present in parser that needs to be for the corresponding operation
     */
    private static boolean missingRequiredParams(ArgumentParser parser, String[] requiredParams){
        // importing or exporting requires a KEYSTORE, STOREPASS, ALIAS, and FILE parameters
        for (String arg : requiredParams){
            if (searchForParameter(parser, arg) == null){
                ProcessPanelHelper.printToPanel(mHandler, String.format("Missing required parameter: %s", arg), true);
                return true;
            }
        }
        return false;
    }


    /**
     * Executes keytool using the ProcessBuilder java API. The argument list is gathered from the ProcessPanel.Spec.xml, or from an arraylist, one element per argument (-keystore /some/path.keystore is 2 elements in the list)
     * the first three arguments are always 'keytool','-debug','-noprompt'. The rest is required to be within otherKeytoolArguments
     * @param otherKeytoolArguments The list of arguments to give to keytool.
     * @return true on a 0 value exit status from keytool, false on a non-zero exit status.
     */
    public static boolean runKeytool(ArrayList<String> otherKeytoolArguments){
        ArrayList<String> keytoolArguments = new ArrayList<String>();
        keytoolArguments.add("keytool");
        keytoolArguments.add("-debug");
        keytoolArguments.add("-noprompt");
        keytoolArguments.addAll(otherKeytoolArguments);
        String keystorePath = null;
        String operation = null;
        // find the name of the operation and the path to the keystore
        for (String arg : otherKeytoolArguments){
            if (arg.equals(KEYSTORE)){
                // get the next entry in the list after -keystore, since it must be the path. Normalize the path for display to user.
                keystorePath = new File(otherKeytoolArguments.get(otherKeytoolArguments.indexOf(arg)+1)).getAbsolutePath();
            } else if (arg.equals(IMPORTCERT) || arg.equals(EXPORTCERT) || arg.equals(GENKEY) || arg.equals(SECKEY)){
                operation = arg;
            }
        }

        ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.start"), operation, keystorePath), false);
        String[] args = new String[7];
        String [] realArgs = keytoolArguments.toArray(args);
        int tries = 0;
        while (tries < 2)
        {
            try
            {
                ProcessBuilder pb = new ProcessBuilder(realArgs);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

                Thread eater = new Thread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        String line;
                        try
                        {
                            while ((line = in.readLine()) != null)
                            {
                                ProcessPanelHelper.printToPanel(mHandler, line, false);
                                out.flush();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
                eater.start();

                int retVal = p.waitFor();
                in.close();
                out.close();
                if (retVal != 0)
                {
                    ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.error"), operation, keystorePath), true);
                    return false;
                } else
                {
                    ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.success"), operation, keystorePath), false);
                    InstallationUtilities.addFileToCleanupList(keystorePath);
                    return true;
                }
            }
            catch (Exception e)
            {
                if(tries > 0)
                {
                    //Something bad happened, print out the stacktrace
                    e.printStackTrace();
                }

                //Keytool is not avilable from the $PATH try obtaining the keytool executable from the jre directory
                String keytoolFromJre = System.getProperty("java.home") + File.separator + "bin" + File.separator + "keytool";
                System.out.println("KEYTOOLFROMJRE: " + keytoolFromJre);
                realArgs[0] = keytoolFromJre;
                tries += 1;
            }
        }
        ProcessPanelHelper.printToPanel(mHandler, String.format(idata.langpack.getString("KeystoreGenerator.error"), operation, keystorePath), true);
        return false;
    }

    /**
     * Searches the given ArgumentParser for a keytool parameter with the given name. If it is found, the value is extracted and returned. If it is not found,
     * null is returned.
     * @param paramName the name of the keytool parameter to look for (keypass, keystore, etc)
     * @param p the ArgumentParser which contains all arguments which should be checked
     * @return returns the value of the paramName in the argument parser, or null if it doesn't exist
     */
    private static String searchForParameter(ArgumentParser p, String paramName) {
        for (String argument : p.getListProperty(PARAMETER)) {
            if (argument.startsWith(paramName)) {
                return extractArgumentValue(argument);
            }
        }
        return null;
    }

    /**
     * Returns the value of a given argument for keytool. The argument should be in keytool syntax ("paramname""space""value")
     *
     * @param argument the keytool argument in the form "paramname""space""value"
     * @return the value of the given parameter
     */
    private static String extractArgumentValue(String argument) {
        return argument.substring(argument.indexOf(' ')+1);
    }
}
