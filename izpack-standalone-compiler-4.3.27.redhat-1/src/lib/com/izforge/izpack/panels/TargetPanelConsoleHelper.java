/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.panels;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.Shell;
import com.izforge.izpack.util.VariableSubstitutor;

import java.io.File;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * The Target panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class TargetPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    private final static String PATH_NOT_VALID      = "TargetPanel.nodir";
    private final static String PATH_INFO           = "TargetPanel.info";
    private static boolean FIRST_ITERATION = true;
    private boolean mustExist = false;

    private static String getTranslation(final AutomatedInstallData idata, final String text) {
        return idata.langpack.getString(text);
    }
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        printWriter.println(ScriptParser.INSTALL_PATH + "=");
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        String strTargetPath = p.getProperty(ScriptParser.INSTALL_PATH);
        if (strTargetPath == null || "".equals(strTargetPath.trim()))
        {
            System.err.println(getTranslation(installData, PATH_NOT_VALID));
            return false;
        }
        else
        {
            VariableSubstitutor vs = new VariableSubstitutor(installData.getVariables());
            strTargetPath = vs.substitute(strTargetPath, null);
            installData.setInstallPath(strTargetPath);
            return true;
        }
    }

    @Override
    public String getSummaryBody(AutomatedInstallData idata)
    {
        return (idata.getInstallPath());
    }

    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
    {
        new TargetPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
    {
        setMustExist(Boolean.valueOf(idata.getVariable("TargetPanel.mustExist")));
        String strTargetPath = "";
        if (FIRST_ITERATION) // On the first iteration load the default installation path defined by TargetPanel.dir.<os> into idata
        {
            String path = TargetPanel.loadDefaultDirFromVariables(idata.getVariables());
            if (path != null)
            {
                idata.setInstallPath(path);//Previously something like $HOME/JBoss EAP-6.2.0 
            }
            FIRST_ITERATION = false;
        }
        String strDefaultPath = idata.getInstallPath();
        
        System.out.println(getTranslation(idata, PATH_INFO) + " [" + strDefaultPath + "] ");
        Shell auto_inpt = Shell.getInstance();
        String strIn =  auto_inpt.getLocation(true);
        if (!strIn.trim().equals(""))
        {
            strTargetPath = strIn;
        }
        else
        {
            strTargetPath = strDefaultPath;
        }

        strTargetPath = new File(strTargetPath).getAbsolutePath();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        strTargetPath = vs.substitute(strTargetPath, null);
        strTargetPath = IoHelper.expandHomePath(strTargetPath);

        //Check on windows if we have to transform relative path to a full path
        if(System.getProperty("os.name").toLowerCase().contains("window"))
        {
            if (!strTargetPath.endsWith("\\"))  //Required as windows and backslashes are very picky...
            {
                strTargetPath = strTargetPath + "\\";
            }
            String windowPathValidator = "[a-zA-Z]:.*";
            if (!Pattern.matches(windowPathValidator, strTargetPath))
            {
                strTargetPath = System.getProperty("user.dir") + File.separator + strTargetPath;
                strTargetPath = strTargetPath.replaceAll("/", "\\\\");
            }
        }
        else //Check if we have to transform relative path to a full path
        {
            if (!strTargetPath.startsWith(File.separator))
            {
                strTargetPath =  System.getProperty("user.dir") + File.separator + strTargetPath;
            }
        }

        idata.setInstallPath(strTargetPath);

        /** Validate the installation path */
        if (strTargetPath != null && strTargetPath.length() > 0)  // This should always be true
        {
            File selectedDir = new File(strTargetPath);
            idata.setInstallPath(selectedDir.getPath());

            File existParent = IoHelper.existingParent(new File(selectedDir.getPath())); //Check to see if directory can be written to

            if (isMustExist()){
                if (!selectedDir.exists()) {
                    System.out.println(vs.substitute(idata.langpack.getString("PathInputPanel.required")));
                    return runConsole(idata, parent);
                }

            }

            if (existParent == null || !existParent.canWrite())
            {
                System.out.println(idata.langpack.getString("TargetPanel.notwritable"));
                return runConsole(idata, parent);
            }
            if (selectedDir.exists() && selectedDir.isDirectory() && selectedDir.list().length > 0) {
                int answer = askEndOfConsolePanel(idata);
                if (answer == 2)
                {
                    return false;
                }
                else if (answer == 3)
                {
                    return runConsole(idata, parent);
                } else {
                    // If they confirm over-writing dir, we can
                    // end console here and not ask user to re-confirm again.
                    return true;
                }
            }
        }

        int i = askEndOfConsolePanel(idata);
        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(idata, parent);
        }
    }

    private void setMustExist(boolean mustExist){
        this.mustExist = mustExist;
    }

    private boolean isMustExist(){
        return mustExist;
    }
}
