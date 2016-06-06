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

import java.io.PrintWriter;
import java.util.Properties;
import java.util.StringTokenizer;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.Shell;
/**
 * License Panel console helper
 *
 */
public class LicencePanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    private static final String CONSOLE_ASK = "console.ask";
    private static final String CONTINUE    = "HTMLLicence.continue";
    private static final String ERROR       = "HTMLLicence.Error";

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter)
    {
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
    {

        String license = null;
        String resNamePrefix = "LicencePanel.licence";
        try
        {
            // We read it
             license = ResourceManager.getInstance().getTextResource(resNamePrefix);
        }
        catch (Exception err)
        {
            license = idata.langpack.getString(ERROR) + " " + resNamePrefix;
            System.out.println(license);
            return false;
        }

        // controls # of lines to display at a time, to allow simulated scrolling down
        int lines=25;
        int l = 0;

        StringTokenizer st = new StringTokenizer(license, "\n");
        while (st.hasMoreTokens())
        {
             String token = st.nextToken();
             System.out.println(token);
             l++;
             if (l >= lines) {
                 if (! doContinue(idata)) {
                     return false;
                 }
                 l=0;
             }

        }

        int i = askToAcceptLicense(idata);

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

    private boolean doContinue(AutomatedInstallData idata)
    {
        Shell console = Shell.getInstance();

        System.out.println("\r");
        while (true)
        {
            System.out.println(idata.langpack.getString(CONTINUE));
            String strIn =  console.getInput();
            if (strIn.equalsIgnoreCase("x"))
            {
                return false;
            }

            return true;
        }

    }

    private int askToAcceptLicense(AutomatedInstallData idata)
    {
        Shell console = Shell.getInstance();
        String consoleAsk = idata.langpack.getString(CONSOLE_ASK);

        while (true)
        {
            System.out.println(consoleAsk);
            String strIn =  console.getInput();
            
            if (strIn.equals("1"))      { return 1; }
            else if (strIn.equals("2")) { return 2; }
            else if (strIn.equals("3")) { return 3; }
        }
    }
}
