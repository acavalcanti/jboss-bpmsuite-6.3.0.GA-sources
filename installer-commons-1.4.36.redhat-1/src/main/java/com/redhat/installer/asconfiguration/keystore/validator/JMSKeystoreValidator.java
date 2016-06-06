package com.redhat.installer.asconfiguration.keystore.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.io.File;

/**
 * Validator to ensure that the user-specified client keystore is accessible with the
 * password they have given. Hacked to only run if a given condition is met.
 * Created by thauser on 6/10/14.
 */
public class JMSKeystoreValidator implements DataValidator {
    private static final String[] supportedFormats = new String[]{"JKS", "JCEKS", "CASEEXACTJKS"};
    private String message;
    private String warningId;

    public char[] getKeystorePassword() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String pwd = idata.getVariable("generated.keystores.client.storepass");
        return (pwd != null) ? pwd.toCharArray() : null;
    }

    /**
     * Only runs when the condition "generate.client.keystores" is false. 2 ways to pass validation:
     * 1) givenLocation is a single file, and this file is a valid keystore which is accessible with the given password
     * 2) givenLocation specifies a directory containing > 0 files, all of which are valid keystores accessible with the given password
     *
     * @param idata
     * @return
     */
    @Override
    public Status validateData(AutomatedInstallData idata) {
        if (idata.getRules().isConditionTrue("generate.client.keystores")) {
            return Status.OK;
        }
        String givenLocation = idata.getVariable("existing.keystores.client.location");
        char[] givenPassword = idata.getVariable("generated.keystores.client.storepass").toCharArray();
        File givenFile = new File(givenLocation);

        int result = 0;

        if (givenFile.exists()) {
            if (givenFile.isFile()) {
                // single keystore
                result = KeystoreValidator.isValidKeystore(givenFile.getAbsolutePath(), givenPassword, supportedFormats);
                if (result != 0) {
                    return evaluateKeystoreValidatorResult(givenFile.getAbsolutePath(),result);
                }
            } else if (givenFile.isDirectory()) {

                if (givenFile.listFiles().length == 0){
                    setMessage(String.format(idata.langpack.getString("JMSKeystoreValidator.empty.dir"), givenFile.getAbsoluteFile()));
                    return Status.ERROR;
                }

                // check all files within; any failure we fail
                for (File file : givenFile.listFiles()) {
                    if (file.isDirectory()){
                        continue;
                    }
                    result = KeystoreValidator.isValidKeystore(file.getAbsolutePath(), givenPassword, supportedFormats);
                    if (result != 0) {
                        return evaluateKeystoreValidatorResult(file.getAbsolutePath(), result);
                    }
                }
            }
        } else {
            setMessage(String.format(idata.langpack.getString("JMSKeystoreValidator.file.not.exist"), givenFile.getAbsolutePath()));
            return Status.ERROR;
        }

        // we got here, that means result == 0 for every checked file.
        return Status.OK;
    }

    private Status evaluateKeystoreValidatorResult(String keystore, int result){
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String messagePrefix = String.format(idata.langpack.getString("JMSKeystoreValidator.message.prefix"), keystore);
        Status currentStatus = Status.OK;
        switch (result) {
            case 1:
                setWarningId("keystore.validator.authentication.failure");
                setMessage(messagePrefix + String.format(idata.langpack.getString("keystore.validator.authentication.failure")));
                currentStatus = Status.WARNING;
                break;
            case 2:
                setWarningId("keystore.validator.file.does.not.exist");
                setMessage(messagePrefix + String.format(idata.langpack.getString("JMSKeystoreValidator.is.directory")));
                currentStatus = Status.ERROR;
                break;
            case 3:
                setWarningId("keystore.validator.jvm.cannot.read");
                setMessage(messagePrefix + String.format(idata.langpack.getString("keystore.validator.jvm.cannot.read")));
                currentStatus = Status.WARNING;
                break;
            case 4:
            case 5:
                setWarningId("keystore.validator.invalid.url");
                setMessage(messagePrefix + String.format(idata.langpack.getString("keystore.validator.invalid.url")));
                currentStatus = Status.WARNING;
                break;
            case 6:
                setWarningId("keystore.validator.file.is.empty");
                setMessage(messagePrefix + String.format(idata.langpack.getString("keystore.validator.file.is.empty")));
                currentStatus = Status.ERROR;
                break;
            case 7:
                setWarningId("keystore.validator.not.supported");
                setMessage(messagePrefix + String.format(idata.langpack.getString("keystore.validator.not.supported")));
                currentStatus = Status.ERROR;
        }
        return currentStatus;
    }

    @Override
    public String getErrorMessageId() {
        return warningId;
    }

    @Override
    public String getWarningMessageId() {
        return warningId;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public void setWarningId(String warningId) {
        this.warningId = warningId;
    }
}
