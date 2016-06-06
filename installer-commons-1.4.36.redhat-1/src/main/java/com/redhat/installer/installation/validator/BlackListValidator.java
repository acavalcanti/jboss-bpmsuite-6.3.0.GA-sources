package com.redhat.installer.installation.validator;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;

import java.util.Map;

/**
 * Created by aabulawi on 23/05/14.
 *
 * Blacklist must be a comma separated set words
 * XML attribute must be blacklist
 *
 *<validator class="com.redhat.installer.installation.validator.BlackListValidator" id="some string">
 *<param name="blacklist" value="sample,blacklist"/>
 *</validator>
 *
 */
public class BlackListValidator implements Validator {

    private static final String LIST_PARAM = "blacklist";

    @Override
    public boolean validate(ProcessingClient client) {

        String blackListString;
        String userString;
        String[] blackList;


        if (client.hasParams())
        {
            Map<String, String> paramMap = client.getValidatorParams();
            blackListString = paramMap.get(LIST_PARAM);
        }
        else
        {
            blackListString = "";
        }

        blackList = blackListString.split(",");

        userString = getString(client);

        for (String word : blackList){
            if(word.equals(userString)){
                return false;
            }
        }

        return true;
    }

    private String getString(ProcessingClient client){

        String returnValue = client.getText();

        return returnValue;


    }
}

