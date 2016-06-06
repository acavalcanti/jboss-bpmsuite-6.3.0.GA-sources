package com.izforge.izpack.util;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * Created by aabulawi on 04/06/14.
 *
 * Used to reverse the substitution of or strings in place variables.
 * pathVariable = idata variable name i.e "INSTALL_DATA"
 * path is the string that you would like to make the substitution in
 */
public class ReversePathSubstitutor {

    public static String substitute(String pathVariable, String path)
    {
        AutomatedInstallData  idata = AutomatedInstallData.getInstance();
        String installPathValue = idata.getVariable(pathVariable);

        return path.replace(installPathValue, "${"+pathVariable+"}");

    }

}
