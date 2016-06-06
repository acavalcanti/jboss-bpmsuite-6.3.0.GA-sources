package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.io.File;

/**
 * Created by thauser on 3/13/15.
 */
public class BusinessCentralExistsValidator implements DataValidator {
    private static String BUSINESS_CENTRAL_WAR_PATH = "standalone/deployments/business-central.war";
    private String errorId;
    private String formattedMessage;
    private AutomatedInstallData idata;

    @Override
    public Status validateData(AutomatedInstallData adata) {
        idata = adata;
        String installPath = idata.getInstallPath();
        return businessCentralExistsAt(installPath);
    }

    private Status businessCentralExistsAt(String installPath) {
        File jbossHome = new File(installPath);
        File businessCentralWar = new File(jbossHome, BUSINESS_CENTRAL_WAR_PATH);
        if (businessCentralWar.exists()){
            setErrorId("BusinessCentralExistsValidator.error");
            setFormattedMessage(String.format(idata.langpack.getString(getErrorMessageId()),jbossHome.toString()));
            return Status.ERROR;
        } else {
            return Status.OK;
        }
    }

    @Override
    public String getErrorMessageId() {
        return errorId;
    }

    @Override
    public String getWarningMessageId() {
        return errorId;
    }

    private void setErrorId(String id){
        errorId = id;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        return formattedMessage;
    }

    private void setFormattedMessage(String message){
        this.formattedMessage = message;
    }

}
