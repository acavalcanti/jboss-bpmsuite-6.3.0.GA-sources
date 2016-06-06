package com.redhat.installer.installation.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.izforge.izpack.util.Debug;

import java.util.Map;

/**
 * Created by aabulawi on 19/08/14.
 *
 * PasswordUsernameMatchValidator is used to ensure that the password and username fields provided to not match
 *
 * Usage:
 * <validator class="com.redhat.installer.installation.validator.PasswordUsernameMatchValidator" id="some string">
 * <param name="value" value="id_of_field_you_want_to_compare_to"/>
 * <param name="operation" value="="
 * </validator>
 * Valid values for operation for value are ['=', '!=']
 */
public class CompareIdataVariablesValidator implements Validator{

    private String VALUE_KEY = "value";
    private String OPERATION_KEY = "operation";

    @Override
    public boolean validate(ProcessingClient client) {
        boolean result = false;
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        if (client.hasParams()) {
            String fieldValue = client.getFieldContents(0);
            Map<String, String> paramMap = client.getValidatorParams();
            String operation = paramMap.get(OPERATION_KEY);
            String value = idata.getVariable(paramMap.get(VALUE_KEY));

            if (value == null || operation == null ){
                if (value == null)
                    Debug.log("Parameter value for CompareIdataVariablesValidator is null");
                if (operation == null)
                    Debug.log("Parameter operation for CompareIdataVariablesValidator is null");
                // comparison is not possible so we should return true here so we don't erroneously cause
                // installations to fail
                return true;
            }

            switch (operation.charAt(0)) {
                case '=':
                    result = fieldValue.equals(value);
                    break;
                case '!':
                    result = ! (fieldValue.equals(value));
                    break;
            }
        }
        else {
            result = false;
        }

        return  result;
    }
}


