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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.installer.*;
import com.izforge.izpack.util.*;

/**
 * Finish Panel console helper
 *
 * @author Mounir el hajj
 */
public class FinishPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {

    private static final String SUCCESS     = "FinishPanel.success";
    private static final String FAIL        = "FinishPanel.fail";
    private static final String AUTO_PATH   = "FinishPanel.autoxml.info";
    private static final String INSTALL     = "FinishPanel.install";
    private static final String AUTO_INSTALL = "FinishPanel.automatic.install";
    private static final String AUTO_WARN    = "FinishPanel.automatic.warning";
    private static final String DIRECTORY    = "FinishPanel.directory";
    private static final String FILE_EXISTS  = "FinishPanel.file.already.exists";
    private static final String FILE_UNWRITE = "FinishPanel.file.cannot.write";
    private static final String AUTO_SUCCESS = "FinishPanel.xml.success";
    private static final String AUTO_NO_SUCCESS = "FinishPanel.xml.no.success";

    private static String getTranslation(final AutomatedInstallData idata, final String text) {
        return idata.langpack.getString(text);
    }
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,PrintWriter printWriter) {
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p){
        return true;
    }

    private String read() throws Exception
    {
        byte[] byteArray = {(byte) System.in.read()};
        return new String(byteArray);
    }

    private String readln() throws Exception
    {
        String input = read();
        int available = System.in.available();
        if (available > 0)
        {
            byte[] byteArray = new byte[available];
            System.in.read(byteArray);
            input += new String(byteArray);
        }
        return input.trim();
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent) {
        if (idata.installSuccess) {
            Shell console = Shell.getInstance();
            System.out.println(getTranslation(idata, SUCCESS));
            System.out.println(getTranslation(idata, INSTALL) + " " + idata.getInstallPath());
            int value = PanelConsoleHelper.askYesNo(getTranslation(idata, AUTO_INSTALL), false);
            if (value == AbstractUIHandler.ANSWER_YES) {
            	if (idata.getVariable("jdbc.driver.install") != null){
            		if (idata.getVariable("jdbc.driver.install").equals("on")){
            			System.out.println(getTranslation(idata, AUTO_WARN));		
            		}
            	}
                File path = null;
                while (true) {
                    String strTargetPath = "";
                    String defaultFile=idata.getInstallPath() + File.separator + "auto.xml";
                    System.out.println(getTranslation(idata, AUTO_PATH) + " [" + defaultFile + "] ");
                    try
                    {
                        String strIn = console.getLocation(false);
                        if (!strIn.isEmpty())
                        {
                            strTargetPath = strIn;
                        }
                        else
                        {
                            strTargetPath = defaultFile;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    strTargetPath = vs.substitute(strTargetPath, null);
                    path = new File(IoHelper.expandHomePath(strTargetPath));
                    if (path.isDirectory()) {
                        System.out.println(getTranslation(idata, DIRECTORY));
                    } else if (path.exists()) {
                        value = PanelConsoleHelper.askYesNo(getTranslation(idata, FILE_EXISTS) + " ",false);
                        if (value == AbstractUIHandler.ANSWER_YES) break;
                    } else if (!new File(path.getAbsoluteFile().getParent()).canWrite()) {
                        System.out.println(getTranslation(idata, FILE_UNWRITE));
                    } else {
                        break;
                    }
                }
                if (path != null) {
                    try
                    {
                        FileOutputStream out = new FileOutputStream(path);
                        BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                        parent.writeXMLTree(idata.xmlData, outBuff);
                        outBuff.flush();
                        outBuff.close();

                        path.setWritable(false,false);
                        path.setReadable(false,false);
                        path.setWritable(true,true);
                        path.setReadable(true,true);

                        FinishPanel.outputVariableFile(path);
                        System.out.println(getTranslation(idata, AUTO_SUCCESS));
                    }
                    catch (Exception e)
                    {
                        System.out.println(getTranslation(idata, AUTO_NO_SUCCESS));
                        Debug.trace(e);
                    }
                }
            }
        } else {
            System.out.println(getTranslation(idata, FAIL));
        }
        return true;
    }
}
