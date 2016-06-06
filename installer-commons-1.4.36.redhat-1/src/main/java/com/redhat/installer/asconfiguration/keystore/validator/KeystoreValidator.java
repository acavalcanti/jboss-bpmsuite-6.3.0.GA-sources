package com.redhat.installer.asconfiguration.keystore.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

/**
 * Superclass of the various keystore validators (SSL, Vault, SAML)
 * Created by thauser on 3/4/14.
 */
public abstract class KeystoreValidator implements DataValidator {
    private static final String PKCS12_AUTH_ERROR ="Given final block not properly padded";
    private static final String JCEKS_AUTH_ERROR = "Keystore was tampered with, or password was incorrect";
    private static final String JKS_AUTH_ERROR = "Password verification failed";
    private static final String PKCS12_TYPE_ERROR = "DerInputStream.getLength(): lengthTag=";
    private static final String JCEKS_JKS_TYPE_ERROR = "Invalid keystore format";

    protected String error;
    protected String message;

    private static String lastSuccessfulAlgorithm = "JKS";

    @Override
    public Status validateData(AutomatedInstallData idata) {
        Status currentStatus = Status.OK;
        String keystoreLoc = getKeystorePath();
        if(keystoreLoc.contains("${jboss.home.dir}")) {
            keystoreLoc = keystoreLoc.replace("${jboss.home.dir}", AutomatedInstallData.getInstance().getInstallPath());
        }
        char[] pwd = getKeystorePassword();
        int result = 0;

        if (getCondition()){
            boolean checkedJKS = false;
            if(!isJKSValid()){
                result = isValidKeystore(keystoreLoc, pwd, new String[] {"JKS"});
                checkedJKS = true;
            }

            if (checkedJKS && result == 0) {
                result = 7;
            } else {
                result = isValidKeystore(keystoreLoc, pwd, getSupportedFormats());
            }
        }

        switch (result) {
            case 1:
                setError("keystore.validator.authentication.failure");
                setMessage(String.format(idata.langpack.getString("keystore.validator.authentication.failure")));
                currentStatus = Status.WARNING;
                break;
            case 2:
                setError("keystore.validator.file.does.not.exist");
                setMessage(String.format(idata.langpack.getString("keystore.validator.file.does.not.exist")));
                currentStatus = Status.ERROR;
                break;
            case 3:
                setError("keystore.validator.jvm.cannot.read");
                setMessage(String.format(idata.langpack.getString("keystore.validator.jvm.cannot.read")));
                currentStatus = Status.WARNING;
                break;
            case 4:
            case 5:
                setError("keystore.validator.invalid.url");
                setMessage(String.format(idata.langpack.getString("keystore.validator.invalid.url")));
                currentStatus = Status.WARNING;
                break;
            case 6:
                setError("keystore.validator.file.is.empty");
                setMessage(String.format(idata.langpack.getString("keystore.validator.file.is.empty")));
                currentStatus = Status.ERROR;
                break;
            case 7:
                setError("keystore.validator.not.supported");
                setMessage(String.format(idata.langpack.getString("keystore.validator.not.supported")));
                currentStatus = Status.ERROR;
        }

        /**
         * If the keystore isn't valid, try running additional checks and return
         * that result / defer to it setting variables
         */
        if (result != 0) {
            if (hasAdditionalChecksOnFail()) {
                return performAdditionalChecksOnFail(result);
            }
        }else{
            if (hasAdditionalChecksOnSuccess()){
                return performAdditionalChecksOnSuccess(lastSuccessfulAlgorithm);
            }
        }


        /**
         * If the keystore was valid, set any needed variables and return OK.
         */
        setVariable();
        return currentStatus;
    }

    /**
     * This method checks that a given keystore<br/>
     * a) exists (file) or is accessible (remote)<br/>
     * b) the provided password is correct for the keystore<br/>
     * 0 : everything is fine <br/>
     * 1 : authentication failure<br/>
     * 2 : file doesn't exist / hostname not accessible <br/>
     * 3 : the JVM doesn't have a provider that can read the keystore <br/>
     * 4 : the given URL contains characters that require encoding and does not do this correctly <br/>
     * 5 : the given URI is not absolute <br/>
     * 6 : the file is empty
     * 7 : the keystore is not in a JBOSS supported format.
     *
     * @param keystoreUrl
     * @param pwd
     * @param supportedFormats
     * @return int status
     */
    public static int isValidKeystore(String keystoreUrl, char[] pwd, String[] supportedFormats)
    {
        int SUCCESS = 0;
        int AUTHENTICATION_FAILURE = 1;
        int NO_FILE = 2;
        int JVM_NO_PROVIDER = 3;
        int BAD_ENCODING = 4;
        int URI_NOT_ABSOLUTE = 5;
        int EMPTY_FILE = 6;
        int NOT_SUPPORTED = 7;
        int status = SUCCESS;

        if (!isValidReadableFile(keystoreUrl) && !isValidAccessibleUrl(keystoreUrl))
        {
            return NO_FILE;
        }


        KeyStore ks = null;

        for (String algorithm : supportedFormats)
        {
            // WINDOWS-ROOT and WINDOWS-MY keystores will not error out on a ks.load() call. They are not supported by any
            // feature of note in the installer, so we skip them entirely
            if (!Security.getAlgorithms("KeyStore").contains(algorithm.toUpperCase())
                    || algorithm.equalsIgnoreCase("WINDOWS-ROOT")
                    || algorithm.equalsIgnoreCase("WINDOWS-MY"))
            {
                status = NOT_SUPPORTED;
                continue;
            }
            FileInputStream keystoreStream = null;
            try
            {
                ks = KeyStore.getInstance(algorithm);
                if (isValidAccessibleUrl(keystoreUrl))
                {
                    ks.load(new URI(keystoreUrl).toURL().openStream(), pwd);
                }
                else
                {
                    keystoreStream = new FileInputStream(new File(keystoreUrl));
                    ks.load(keystoreStream, pwd);
                }

                status = SUCCESS;
                lastSuccessfulAlgorithm = algorithm;
                break;
            }
            catch (IllegalArgumentException iae)
            {
                /**
                 * assume that this exception indicates that the URI is not absolute. Indeed,
                 * docs: http://docs.oracle.com/javase/7/docs/api/java/net/URI.html
                 * indicate that URI.toURL() will throws this only in this case.
                 * currently, previous checks seem to make this impossible. still accounted for in usages though
                 *
                 * it is safe to short circuit here, since an invalid URI will never load successfully.
                 */
                return URI_NOT_ABSOLUTE;

            }
            catch (NoSuchAlgorithmException e)
            {
                // may occur if the user is using a JRE that doesn't include the format the user is trying to use
                status = JVM_NO_PROVIDER;

            }
            catch (CertificateException e)
            {
                status = JVM_NO_PROVIDER;
            }
            catch (FileNotFoundException e)
            {
                // If connection is legit, but file is not accessible/doesn't exist at remote location.
                status = NO_FILE;

            }
            catch (EOFException e)
            {
                status = EMPTY_FILE;
            }
            catch (IOException e)
            {
                /**
                 * This is thrown on incorrect passwords by most keystore algo  implementations.
                 * Its use is overloaded.
                 */
                String message = e.getMessage();
                String causeMessage = e.getCause() != null ? e.getCause().getMessage() : "";
                /**
                 * First check if the IOException is the result of a type mismatch.
                 */
                if (message.equals(JCEKS_JKS_TYPE_ERROR) || (message.startsWith(PKCS12_TYPE_ERROR))){
                    status = NOT_SUPPORTED;
                }
                /**
                 * It wasn't the keystore type; let's see if it's an Authentication problem
                 */
                else if(message.equals(JCEKS_AUTH_ERROR) || causeMessage.equals(PKCS12_AUTH_ERROR) || causeMessage.equals(JKS_AUTH_ERROR)) {
                    status = AUTHENTICATION_FAILURE;
                    return status; // short circuiting here is safe now, because of the authentication / type error differentiation additions
                } else {
                    // unknown IOException. Possible to just consider it a general Authentication problem.
                    status = JVM_NO_PROVIDER;
                }

            }
            catch (KeyStoreException e)
            {
                /**
                 * this shouldn't really happen ever
                 * if this is thrown, again, the JRE doesn't include a Provider that can provide a KeyStore instance of type "JKS", which
                 * means that either the JRE is very old or is non-standard in some critical way
                 */
                status = JVM_NO_PROVIDER;

            }
            catch (URISyntaxException e)
            {
                // some values weren't encoded on in the URI, so we give a better message to users
                status = BAD_ENCODING;
            } finally {
                if (keystoreStream != null){
                    try {
                        keystoreStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return status;
    }

    /**
     * Determines if a given file exists, can be read, and isn't a directory
     * TODO: This could probably be moved to another util class to be utilized by other classes
     *
     * @param file
     * @return
     */
    public static boolean isValidReadableFile(String file) {
        File check = new File(file);
        if (check.exists() && check.canRead() && !check.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines if a given URL is both valid and accessible
     * TODO: this could probably be moved to another util class and utilized elsewhere
     *
     * @param url
     * @return
     */
    public static boolean isValidAccessibleUrl(String url) {
        URL check;
        try {
            check = new URL(url);
            URLConnection conn = check.openConnection();
            conn.connect();
        } catch (MalformedURLException e1) {
            // bad URL
            return false;
        } catch (IOException e) {
            // the connection couldn't be established
            return false;
        }
        return true;
    }

    private boolean isJKSValid(){
        String[] supportedFormats = getSupportedFormats();
        for (int i = 0; i <= supportedFormats.length -1; i++){
            String format = supportedFormats[i];
            if(format.toUpperCase().equals("JKS"))
                return true;
        }
        return false;
    }

    // provides the path
    public abstract String getKeystorePath();

    public abstract String getEncryptedDirPath();

    // allow the subclasses to set the relevant keystore formats for the check
    public abstract String[] getSupportedFormats();

    // provides the keystore password
    public abstract char[] getKeystorePassword();

    // some keystores may require additional checks, like the vault
    public abstract boolean hasAdditionalChecksOnFail();

    // perform the additional checks, true = pass, false = fail
    // this method should set the error string appropriately for its return value also
    public abstract Status performAdditionalChecksOnFail(int result);

    // set the variable requied by the validator, if any.
    public abstract void setVariable();

    // returns what Status to return in the case of a failure
    public abstract Status getFailureStatus();


    protected abstract Status performAdditionalChecksOnSuccess(String algorithm);

    protected abstract boolean hasAdditionalChecksOnSuccess();

    // returns the key to use for the generic "failure" langpack string
    public String getLangpackKey() {
        return "ssl.password.incorrect";
    }

    protected void setError(String s) {
        error = s;
    }

    protected void setMessage(String s) {
        message = s;
    }

    @Override
    public String getErrorMessageId() {
        return error;
    }

    @Override
    public String getWarningMessageId() {
        return error;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    /**
     * Method to return whether or not the conditions for the check exist.
     * @return
     */
	protected abstract boolean getCondition();

}
