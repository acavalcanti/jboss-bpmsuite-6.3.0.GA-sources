package com.redhat.installer.asconfiguration.keystore.validator;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.util.Properties;

/**
 *  Validates the security domain JSSE element panel.
 *  If user selects to add a JSSE element, then this validator will 
 *  check that either situation is true:
 *  1) securitydomain.keystore.password != null && securitydomain.keystore.url != null
 *      OR
 *  2) securitydomain.truststore.password != null && securitydomain.truststore.url != null
 *        If both of these are false, the user is prompted to change their input.
 * @author fcanas
 */
public class JSSEValidator implements DataValidator
{
    String message;
    String error;
    String warning;
    
    public Status validateData(AutomatedInstallData idata) {
        Properties variables = idata.getVariables();
        String isKeyStore = (String) variables.get("securityDomainJsseAddKeystore");
        String isTrustStore = (String) variables.get("securityDomainJsseAddTruststore");
        String keystorePwd = (String)variables.get("securitydomain.jsse.keystore.password");
        String keystoreUrl = (String)variables.get("securitydomain.jsse.keystore.url");
        String keystoreType = (String)variables.get("securitydomain.jsse.keystore.type");
        String truststorePwd = (String)variables.get("securitydomain.jsse.truststore.password");
        String truststoreUrl = (String)variables.get("securitydomain.jsse.truststore.url");
        String truststoreType = (String)variables.get("securitydomain.jsse.truststore.type");
        Status keyStoreStatus = Status.OK;
        Status trustStoreStatus = Status.OK;
        boolean installTruststore = Boolean.parseBoolean(isTrustStore);
        boolean installKeystore = Boolean.parseBoolean(isKeyStore);
        
        if (idata.getVariable("securityDomainAddJsse").contains("false")) {
            return Status.OK;
        }

        // If user selected the keystore, validate their keystore input.
        if (installKeystore) {
            switch(validateKeystore(keystorePwd, keystoreUrl, idata, keystoreType)){
                case 0:
                    keyStoreStatus = Status.OK;
                    break;
                case 1:
                    warning = "securitydomain.jsse.keystore.passincorrect";
	            	message = idata.langpack.getString(warning);
                    keyStoreStatus = Status.WARNING;
                    break;
                case 2:
                    warning = "securitydomain.jsse.keystore.inaccessible";
	            	message = idata.langpack.getString(warning);
                    keyStoreStatus = Status.WARNING;
                    break;
				case 3:
                    warning = "securitydomain.jsse.keystore.invalid";
					message = idata.langpack.getString(warning);
					keyStoreStatus = Status.WARNING;
                    break;
                case 4:
                    error = "securitydomain.jsse.keystore.encoding";
                    message = idata.langpack.getString(error);
                    keyStoreStatus = Status.ERROR;
                    break;
                case 5:
                    error = "securitydomain.jsse.keystore.absolute";
                    message = idata.langpack.getString(error);
                    keyStoreStatus = Status.ERROR;
                    break;
                case 6:
                    error = "securitydomain.jsse.keystore.reqs";
                    message = idata.langpack.getString(error);
                    keyStoreStatus = Status.ERROR;
                    break;
                case 7:
                    error = "securitydomain.jsse.keystore.wrongtype";
                    message = idata.langpack.getString(error);
                    keyStoreStatus = Status.ERROR;
                    break;
            }
        }
        // If user selected the truststore, validate their truststore input.
        if (installTruststore) {
            switch (validateKeystore(truststorePwd, truststoreUrl, idata, truststoreType)) {
                case 0:
                    trustStoreStatus = Status.OK;
                    break;
                case 1:
                    warning = "securitydomain.jsse.truststore.passincorrect";
            	    message = idata.langpack.getString(warning);
                    trustStoreStatus = Status.WARNING;
                    break;
                case 2:
                    warning = "securitydomain.jsse.truststore.inaccessible";
            	    message = idata.langpack.getString(warning);
                    trustStoreStatus = Status.WARNING;
                    break;
                case 3:
                    warning = "securitydomain.jsse.keystore.invalid";
					message = idata.langpack.getString(warning);
					trustStoreStatus = Status.WARNING;
					break;
				case 4:
                    error = "securitydomain.jsse.truststore.encoding";
					message = idata.langpack.getString(error);
                    trustStoreStatus = Status.ERROR;
                    break;
                case 5:
                    warning = "securitydomain.jsse.truststore.absolute";
                    message = idata.langpack.getString(warning);
                    trustStoreStatus = Status.WARNING;
                    break;
                case 6:
                    error = "securitydomain.jsse.truststore.reqs";
                    message = idata.langpack.getString(error);
                    trustStoreStatus = Status.ERROR;
                    break;
                case 7:
                    error = "securitydomain.jsse.truststore.wrongtype";
                    message = idata.langpack.getString(error);
                    trustStoreStatus = Status.ERROR;
                    break;
            }
        }

        // neither truststore nor keystore selected.
        if (!(installTruststore || installKeystore))
        {
            error = "securitydomain.jsse.requirements";
	        message = idata.langpack.getString(error);
	        return Status.ERROR;
        }

        if (installKeystore && keystoreType.equals("PKCS11") || installTruststore && truststoreType.equals("PKCS11")){
            error = "securitydomain.jsse.keystore.pkcs11.warning";
            message = idata.langpack.getString(error);
            return Status.WARNING;
        }
        
        if (keyStoreStatus != Status.OK){
            return keyStoreStatus;
        }

        if (trustStoreStatus != Status.OK){
            return trustStoreStatus;
        }



        return Status.OK;
    }
    
    /**
     * Returns true if any string in the parameter list is either null or empty
     * @param values
     * @return
     */
    private boolean nullOrEmpty(String ... values)
    {
    	boolean anyFailure = false;
    	for (String value : values){
    		if (value == null || value.isEmpty()){
    			anyFailure = true;
    		}
    	}
        return anyFailure;
    }

    private int validateKeystore(String keyPwd, String keyUrl, AutomatedInstallData idata, String storeType){
        if (nullOrEmpty(keyPwd, keyUrl)){
            error = "securitydomain.jsse.keystore.reqs";
            message = idata.langpack.getString(error);
            return 6;
        }
        // we make sure that the keystore is of the right type
        String[] storeTypes;
        if (storeType.equalsIgnoreCase("JKS")){
            storeTypes = new String[]{"CASEEXACTJKS", storeType};
        } else {
            storeTypes = new String[]{storeType};
        }
        return KeystoreValidator.isValidKeystore(keyUrl, keyPwd.toCharArray(), storeTypes);
    }

    public String getErrorMessageId() 
    {
        return error;
    }

    public String getWarningMessageId() 
    {
        return warning;
    }

    public boolean getDefaultAnswer() 
    {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }

}

