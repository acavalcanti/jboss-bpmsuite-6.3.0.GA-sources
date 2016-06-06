package com.redhat.installer.password.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thauser on 2/10/15.
 */
public class SetMasterPassword {


    private static final String MASTER_VARS = "master.password.vars";
    private static final String ADMIN_PASSWORD = "master.password.var";
    private static final String MASTER_PASSWORD = "master.password";

    private static AutomatedInstallData idata;

    public static void run(AbstractUIProcessHandler handler, String[] args) {
        idata = AutomatedInstallData.getInstance();
        setAllPasswordsToMasterPassword();
    }

    private static Map<String,String> getPasswordVariables(){
        String passwordVariables = idata.getVariable(MASTER_VARS);

        Map<String,String> variablesToConditions = new HashMap<String,String>();
        if (passwordVariables != null){
            String[] split = passwordVariables.split(",");
            for (String var : split){
                String[] temp = var.split("=");
                variablesToConditions.put(temp[0],temp[1]);
            }
        } else {
            return null;
        }
        return variablesToConditions;
    }

    private static void setAllPasswordsToMasterPassword() {
        boolean useSamePassword = idata.getRules().isConditionTrue("use.same.password");
        boolean addUser = idata.getRules().isConditionTrue("add.user");
        Map<String,String> passwords = getPasswordVariables();
        String masterPasswordVariable;
        String masterPassword;
        if (useSamePassword && !addUser){
            masterPasswordVariable = idata.getVariable(MASTER_PASSWORD);
            masterPassword = idata.getVariable(masterPasswordVariable);
        }
        else {
            masterPasswordVariable = idata.getVariable(ADMIN_PASSWORD);
            masterPassword = idata.getVariable(masterPasswordVariable);
        }
        for (Map.Entry<String,String> entry : passwords.entrySet()) {
            if (useSamePassword && masterPassword != null && !idata.getRules().isConditionTrue(entry.getValue()))
                idata.setVariable(entry.getKey(), masterPassword);
        }
    }
}
