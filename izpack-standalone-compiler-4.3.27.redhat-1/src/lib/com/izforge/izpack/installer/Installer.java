/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
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

package com.izforge.izpack.installer;

import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;

import javax.swing.JOptionPane;

import com.izforge.izpack.Info;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.StringTool;

/**
 * The program entry point. Selects between GUI and text install modes.
 *
 * @author Jonathan Halliday
 */
public class Installer {

	public static final int INSTALLER_GUI = 0, INSTALLER_AUTO = 1, INSTALLER_CONSOLE = 2;
	public static final int CONSOLE_INSTALL = 0, CONSOLE_GEN_TEMPLATE = 1, CONSOLE_FROM_TEMPLATE = 2;

    /*
	 * The main method (program entry point).
	 * 
	 * @param args The arguments passed on the command-line.
	 */
	public static void main(String[] args) {
        Map<String,String> argVariables = new HashMap<String,String>();

		Debug.log(" - Logger initialized at '" + new Date(System.currentTimeMillis()) + "'.");

		Debug.log(" - commandline args: " + StringTool.stringArrayToSpaceSeparatedString(args));

		// OS X tweakings
		if (System.getProperty("mrj.version") != null) {
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
			System.setProperty("com.apple.mrj.application.live-resize", "true");
		}

		Info inf = null;
		try {
		    InputStream in = InstallerBase.class.getResourceAsStream("/info");
		    ObjectInputStream objIn = new ObjectInputStream(in);
		    inf = (Info) objIn.readObject();
		    objIn.close();
		    checkForPrivilegedExecution(inf, args);
		} catch (Exception e) {
		}

		try {
		    Iterator<String> args_it = Arrays.asList(args).iterator();
		    
		    int type = INSTALLER_GUI;
		    int consoleAction = CONSOLE_INSTALL;
		    String path = null, langcode = null;
		    
		    while (args_it.hasNext())
		    {
		        String arg = args_it.next().trim();
		        try {
					if ("-h".equalsIgnoreCase(arg) || "-help".equalsIgnoreCase(arg))
					{
						PrintHelp ph = new PrintHelp();
						ph.printHelp();
						System.exit(0);
					}
    		        else if ("-console".equalsIgnoreCase(arg))
    		        {
    		            type = INSTALLER_CONSOLE;
    		        }
    		        else if ("-options-template".equalsIgnoreCase(arg))
    		        {
    		            type = INSTALLER_CONSOLE;
    		            consoleAction = CONSOLE_GEN_TEMPLATE;
    		            path = args_it.next().trim();
    		        }
    		        else if ("-options".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                        consoleAction = CONSOLE_FROM_TEMPLATE;
                        path = args_it.next().trim();
                    }
    		        else if ("-language".equalsIgnoreCase(arg))
    		        {
    		            type = INSTALLER_CONSOLE;
    		            langcode = args_it.next().trim();
    		        }
    		        else if ("-version".equalsIgnoreCase(arg))
    		        {
                        System.out.println("Product: " + inf.getAppName());
    		        	System.out.println("Version: " + inf.getAppVersion());
    		        	System.exit(0);
    		        }
                    else if ("-variables".equalsIgnoreCase(arg))
                    {
                        String newVars = args_it.next().trim();
                        String [] keyValPairs = newVars.split(",");

                        for (String pair : keyValPairs) {
                            String[] keyVal = pair.split("=");
                            argVariables.put(keyVal[0], keyVal[1]);
                        }
                    } 
                    else if ("-variablefile".equalsIgnoreCase(arg))
                    {
                        String pwdFilePath = args_it.next().trim();
                        File pwdFile = new File(pwdFilePath);
                        if (pwdFile.exists()){
							 loadVariablesFromFile(pwdFile, argVariables);
                        } else {
                            System.err.println("- ERROR -");
                            System.err.println("Given properties file does not exist.");
                        }
                    }
    		        else
    		        {
    		            type = INSTALLER_AUTO;
    		            path = arg; 
    		        }
		        }
		        catch (NoSuchElementException e) {
                    PrintHelp ph = new PrintHelp();
                    ph.printHelp();
		            System.exit(1);
		        }
		    }
		    
		    // if headless, just use the console mode
		    if (type == INSTALLER_GUI && GraphicsEnvironment.isHeadless()) 
		    {
		        type = INSTALLER_CONSOLE;
		    }
		    
		    switch (type)
		    {
		        case INSTALLER_GUI:
                    // Creating an object will lauch the installer, there is no run method for the GUI installer
		            GUIInstaller gi = new GUIInstaller(argVariables);
                    gi.doInstall();
                    break;

		        case INSTALLER_AUTO:
		            AutomatedInstaller ai = new AutomatedInstaller(path, argVariables);
		            ai.doInstall();
		            break;
		            
		        case INSTALLER_CONSOLE:
		            ConsoleInstaller consoleInstaller = new ConsoleInstaller(langcode, argVariables);
		            consoleInstaller.run(consoleAction, path);
		            break;
		    }
		    
		} catch (Exception e) {
            System.out.println("Invalid file.");
            System.out.println("Please specify the correct XML file.");
            System.exit(1);
		    /*
			System.err.println("- ERROR -");
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(1);
			*/
		}
	}

	public static void loadVariablesFromFile(File variablesFile, Map<String, String> argVariables){
		Properties props = new Properties();
		try{
			props.load(new FileInputStream(variablesFile));
		} catch (IOException ioe){
			System.err.println("There was an error reading the variable file.");
			ioe.printStackTrace();
		} catch (IllegalArgumentException iae){
			System.err.println("The variable file seems to be malformed.");
			iae.printStackTrace();
		}
		if (!props.stringPropertyNames().isEmpty()){
			for (String key : props.stringPropertyNames()){
				argVariables.put(key, props.getProperty(key));
			}
		} else {
			System.err.println("The variable file was empty.");
		}
	}

	private static void checkForPrivilegedExecution(Info info, String[] args)
	{
	    if (PrivilegedRunner.isPrivilegedMode())
	    {
	        // We have been launched through a privileged execution, so stop the checkings here!
	        return;
	    }
	    else if (info.isPrivilegedExecutionRequired())
	    {
	        boolean shouldElevate = true;
	        final String conditionId = info.getPrivilegedExecutionConditionID();
	        if (conditionId != null)
	        {
	            shouldElevate = RulesEngine.getCondition(conditionId).isTrue();
	        }
	        PrivilegedRunner runner = new PrivilegedRunner(!shouldElevate);
	        String programFiles = System.getenv("ProgramFiles");
	        if (programFiles == null)
	        {
	            programFiles = "C:\\Program Files";
	        }
	        if (runner.isPlatformSupported() && runner.isElevationNeeded(programFiles))
	        {
	            try
	            {
	                if (runner.relaunchWithElevatedRights(args) == 0)
	                {
	                    System.exit(0);
	                }
	                else
	                {
	                    throw new RuntimeException("Launching an installer with elevated permissions failed.");
	                }
	            }
	            catch (Exception e)
	            {
	                e.printStackTrace();
	                JOptionPane.showMessageDialog(null, "The installer could not launch itself with administrator permissions.\n" +
	                "The installation will still continue but you may encounter problems due to insufficient permissions.");
	            }
	        }
	        else if (!runner.isPlatformSupported())
	        {
	            JOptionPane.showMessageDialog(null, "This installer should be run by an administrator.\n" +
	            "The installation will still continue but you may encounter problems due to insufficient permissions.");
	        }
	    }
	}
}
