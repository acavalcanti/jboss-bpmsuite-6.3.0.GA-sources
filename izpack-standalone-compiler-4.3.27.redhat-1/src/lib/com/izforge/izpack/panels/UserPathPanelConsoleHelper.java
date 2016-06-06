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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.VariableSubstitutor;
/**
 * The UserPath panel console helper class.
 * Based on the Target panel console helper
 * 
 * @author Mounir El Hajj
 * @author Dustin Kut Moy Cheung
 */
public class UserPathPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    public static final String PATH_VARIABLE;
    public static final String PATH_PACK_DEPENDS;
    public static final String PATH_ELEMENT;
    public static final String USER_PATH_INFO;
    public static final String USER_PATH_NODIR;
    public static final String USER_PATH_EXISTS;
    public static final String NO_DIR;

    private static final String EMPTY;
    private static final BufferedReader br;
    private AutomatedInstallData idata;
    static {
        PATH_VARIABLE       = UserPathPanel.pathVariableName;
        PATH_PACK_DEPENDS   = UserPathPanel.pathPackDependsName;
        PATH_ELEMENT        = UserPathPanel.pathElementName;
        USER_PATH_INFO      = "UserPathPanel.info";
        USER_PATH_NODIR     = "UserPathPanel.nodir";
        USER_PATH_EXISTS    = "UserPathPanel.exists_warn";
        EMPTY               = "";
        NO_DIR              = "TargetPanel.nodir";

        br = new BufferedReader(new InputStreamReader(System.in));
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        // not implemented
        return false;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData idata, Properties p)
    {
        // not implemented
        return false;
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
    {
        this.idata = idata;

        String userPathPanel        = null;
        String defaultUserPathPanel = null;
        String pathMessage          = null;

        VariableSubstitutor vs;

        vs          = new VariableSubstitutor(idata.getVariables());
        pathMessage = idata.langpack.getString(USER_PATH_INFO);
        defaultUserPathPanel = idata.getVariable(PATH_VARIABLE);

        if (defaultUserPathPanel == null) {
            defaultUserPathPanel = EMPTY;
        } else {
            defaultUserPathPanel = vs.substitute(defaultUserPathPanel, null);
        }

        out(EMPTY);
        out(pathMessage +" [" + defaultUserPathPanel + "]");

        userPathPanel = readInput();
        out(EMPTY);

        // check what the userPathPanel value should be
        if (userPathPanel == null) {
            return false;
        } else if (EMPTY.equals(userPathPanel)) {
            if (EMPTY.equals(defaultUserPathPanel)) {
                out(idata.langpack.getString(NO_DIR));
                return runConsole(idata, parent);
            } else {
                userPathPanel = defaultUserPathPanel;
            }
        } else {
            userPathPanel = vs.substitute(userPathPanel, null);
        }
        if (isPathAFile(userPathPanel) == false) {
            if (doesPathExists(userPathPanel) && isPathEmpty(userPathPanel) == false) {
                out(idata.langpack.getString(USER_PATH_EXISTS));

                switch(askEndOfConsolePanel(idata)) {
                    case 1: break;
                    case 2: return false;
                    default: return runConsole(idata, parent);
                }
            }
        } else {
            out(idata.langpack.getString(USER_PATH_NODIR));
            return runConsole(idata, parent);
        }
        // If you reached here, all data validation done!
        // ask the user if he wants to proceed to the next 
        switch (askEndOfConsolePanel(idata)) {
            case 1: idata.setVariable(PATH_VARIABLE, userPathPanel);
                    idata.setVariable(PATH_ELEMENT, userPathPanel);
                    return true;
            case 2: return false;
            default: return runConsole(idata, parent);
        }
    }
    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The XML tree to write the data in. 
     */
    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
    {
        new UserPathPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    private String readInput()
    {
        String strIn;

        try {
            strIn = br.readLine();
            return strIn.trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean doesPathExists (String path) {
        File file = new File(path);
        return file.exists();
    }
    private static boolean isPathAFile (String path) {
        File file = new File(path);
        return file.isFile();
    }
    private static boolean isPathEmpty (String path) {
        File file = new File(path);
        return (file.list().length == 0);
    }
    private static void out (String out) {
        System.out.println(out);
    }
}
