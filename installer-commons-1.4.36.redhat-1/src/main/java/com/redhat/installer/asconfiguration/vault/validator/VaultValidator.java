package com.redhat.installer.asconfiguration.vault.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.keystore.validator.KeystoreValidator;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;


/**
 * This class validates the fields given by the user, to make sure vault generation and vault creation succeed
 *
 * @author thauser
 */

public class VaultValidator extends KeystoreValidator {
    private static final int DOES_NOT_EXIST = 2;
    private static final String NEW_VAULT = "new.postinstall.vault";
    private boolean keyExists = false;

    @Override
    protected Status performAdditionalChecksOnSuccess(String algorithm) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String keystorePath = getKeystorePath();
        char[] keystorePassword = getKeystorePassword();
        String keyAlias = idata.getVariable("vault.alias");
        keyExists = false;
        FileInputStream keystoreStream = null;
        try {
            KeyStore keystore = KeyStore.getInstance(algorithm);
            if (KeystoreValidator.isValidAccessibleUrl(keystorePath)) {
                keystore.load(new URI(keystorePath).toURL().openStream(), keystorePassword);
            } else if (KeystoreValidator.isValidReadableFile(keystorePath)) {
                keystoreStream = new FileInputStream(keystorePath);
                keystore.load(keystoreStream, keystorePassword);
            }
            if (keystore.containsAlias(keyAlias)) {
                Key vaultKey = keystore.getKey(keyAlias, keystorePassword);
                keyExists = true;
                if (requiresSecretKey()) {
                    if (vaultKey instanceof SecretKey) {
                        setVariable();
                        return Status.OK;
                    } else {
                        setError("vault.key.algorithm.not.supported");
                        setMessage(String.format(idata.langpack.getString(getErrorMessageId()), keyAlias));
                        return Status.ERROR;
                    }
                }
            }
            //TODO find way to produce the correct kind of key depending on keystore algorithm
            else if (!algorithm.toLowerCase().equals("jceks")) {
                setError("vault.key.creation.keystore.type.error");
                setMessage(String.format(idata.langpack.getString(getErrorMessageId())));
                return Status.ERROR;
            }

        } catch (KeyStoreException e) {
        } catch (CertificateException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (UnrecoverableKeyException e) {
        } catch (URISyntaxException e) {
        } finally {
            if (keystoreStream != null) {
                try {
                    keystoreStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        setVariable();
        return Status.OK;
    }

    @Override
    protected boolean hasAdditionalChecksOnSuccess() {
        return true;
    }

    @Override
    public String getKeystorePath() {
        String path = AutomatedInstallData.getInstance().getVariable("vault.keystoreloc");
        if(path.contains("${jboss.home.dir}")) {
            path = path.replace("${jboss.home.dir}", AutomatedInstallData.getInstance().getInstallPath());
        }
        return path;
    }

    @Override
    public String getEncryptedDirPath() {
        return AutomatedInstallData.getInstance().getVariable("vault.encrdir");
    }

    @Override
    public String[] getSupportedFormats() {
        return AutomatedInstallData.getInstance().getVariable("vault.allowed.keystore.types").split(",");
    }

    @Override
    public char[] getKeystorePassword() {
        return AutomatedInstallData.getInstance().getVariable("vault.keystorepwd").toCharArray();
    }

    @Override
    public boolean hasAdditionalChecksOnFail() {
        return true;
    }

    @Override
    public void setVariable() {
        /**
         * This runs when the initial keystore check on an existing keystore
         * succeeds.
         */
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (keyExists)
            idata.setVariable(NEW_VAULT, "false");
        else
            idata.setVariable(NEW_VAULT, "true");
    }

    @Override
    public Status getFailureStatus() {
        return Status.ERROR;
    }

    @Override
    public Status performAdditionalChecksOnFail(int result) {

        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        if (DOES_NOT_EXIST != result) {
            return Status.ERROR;
        }

        /**
         * If the validator gets here, it's because the keystore doesn't yet exist
         * and we need to validate the user's chosen path, so update the
         * given condition:
         */
        idata.setVariable(NEW_VAULT, "true");

        // all of the other variables are validated in panel
        String path = getKeystorePath();

        // Can't have both keystore and encrypted dir be equal:
        if (path.equals(getEncryptedDirPath())) {
            setError("vault.path.same.as.encrypted");
            setMessage(idata.langpack.getString(error));
            return Status.ERROR;
        }


        File file = new File(path);
        if (path.isEmpty()) {
            // If the user's selected path is empty, it's an error.
            setError("vault.path.is.directory");
            setMessage(idata.langpack.getString(error));
            return Status.ERROR;
        } else if (file.isDirectory()) {
            // If the user's selected path is a directory, and not a filename:
            setError("vault.path.is.directory");
            setMessage(idata.langpack.getString(error));
            return Status.ERROR;
        } else if (file.exists()) {
            // if the given file exists, fail fast.
            setError("vault.path.existing");
            setMessage(idata.langpack.getString(error));
            return Status.ERROR;
        } else {
            // Default vault location is always ok.
            if (path.equals(vs.substitute(idata.getVariable("vault.keystoreloc.default")))) {
                return Status.OK;
            }

            File absoluteParent = IoHelper.existingParent(file);
            File immediateParent = file.getParentFile();

            // Check that absolute parent is writeable
            if (absoluteParent == null || !absoluteParent.canWrite()) {
                setError("vault.path.no.write.permission");
                setMessage(idata.langpack.getString(error));
                return Status.ERROR;
            }

            // If immediate parent dir of the given location does not exist
            if (immediateParent == null || !immediateParent.exists()) {
                // Warn that this dir will be created when the keystore generator runs.
                setError("vault.path.parent.notexisting.warning");
                setMessage(idata.langpack.getString(error));
                return Status.WARNING;
            }
        }
        return Status.OK;
    }

    private boolean requiresSecretKey() {
        return AutomatedInstallData.getInstance().getVariable("vault.requires.secret.key").toLowerCase().equals("true");
    }

    private String getProductName() {
        return AutomatedInstallData.getInstance().getVariable("product.name");
    }

    @Override
    public String getErrorMessageId() {
        // TODO Auto-generated method stub
        return error;
    }

    @Override
    public String getWarningMessageId() {
        return error;
    }

    @Override
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    protected boolean getCondition() {
        return true;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }

}
