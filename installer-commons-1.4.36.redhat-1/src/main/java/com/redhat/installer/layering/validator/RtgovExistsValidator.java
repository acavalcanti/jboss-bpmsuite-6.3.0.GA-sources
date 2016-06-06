package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import java.io.File;

/**
 * Ensures the presence of the rtgov server
 * This is a special case of installing SRAMP on FSW
 */
public class RtgovExistsValidator implements DataValidator
{
    private String errorId;
    private String warningId;

    final private static String rtgovPath = "modules" + File.separator +
            "system" + File.separator + "layers" + File.separator +
            "soa" +File.separator + "org" + File.separator + "overlord" + File.separator +
            "rtgov" + File.separator + "main" + File.separator + "rtgov-common-1.0.1.Final-redhat-4.jar";

    @Override
    public Status validateData(AutomatedInstallData idata)
    {
        String installPath = idata.getInstallPath();
        return null;
    }

    @Override
    public String getErrorMessageId()
    {
        return errorId;
    }

    @Override
    public String getWarningMessageId()
    {
        return warningId;
    }

    @Override
    public boolean getDefaultAnswer()
    {
        return false;
    }

    public String getFormattedMessage()
    {
        return null;
    }


}
