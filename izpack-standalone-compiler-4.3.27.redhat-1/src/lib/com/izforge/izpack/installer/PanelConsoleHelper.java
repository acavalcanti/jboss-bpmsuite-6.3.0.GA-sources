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

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.izforge.izpack.util.Shell;
/**
 * Abstract class implementing basic functions needed by all panel console helpers.
 * 
 * @author Mounir El Hajj
 */
abstract public class PanelConsoleHelper 
{
    private static final String CONSOLE_ASK = "console.ask";
   
    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
    {
    }

    /**
     * This method will be called from the SummaryPanel to get the summary of this class which
     * should be placed in the SummaryPanel. The returned text should not contain a caption of this
     * item. The caption will be requested from the method getCaption. If <code>null</code>
     * returns, no summary for this panel will be generated. Default behaviour is to return
     * <code>null</code>.
     * @return the summary for this class
     */
    public String getSummaryBody(AutomatedInstallData idata)
    {
        return null;
    }
    
    public String getInstanceNumber(){
        return null;
    }

    /**
     * This method will be called from the SummaryPanel to get the caption for this class which
     * should be placed in the SummaryPanel. If <code>null</code> returns, no summary for this
     * panel will be generated. Default behaviour is to return the string given by langpack for the
     * key <code>&lt;current class name>.summaryCaption&gt;</code> if exist, else the string
     * &quot;summaryCaption.&lt;ClassName&gt;&quot;.
     * @return the caption for this class
     */
    public String getSummaryCaption(AutomatedInstallData idata)
    {
        String caption;
        String curClassname = this.getClass().getName();
        
        if (curClassname.equals("com.izforge.izpack.panels.UserInputPanelConsoleHelper")){
            caption = getI18nStringForClass("summaryCaption."+this.getInstanceNumber(),curClassname.replaceAll("ConsoleHelper",""), idata);
        } else {
            caption = getI18nStringForClass("summaryCaption", curClassname.replaceAll("ConsoleHelper",""), idata);
        }
        
        return caption;
    }
    
    /**
     * Copied directly from IzPanel, except it takes idata parameter and uses it instead of the parent frame, 
     * since it doesn't exist
     * @param curClassName
     * @param subkey
     * @param alternateClass
     * @param idata
     * @return
     */
    private String getI18nStringForClass(String curClassName, String subkey, String alternateClass, AutomatedInstallData idata)
    {

        int nameStart = curClassName.lastIndexOf('.') + 1;
        curClassName = curClassName.substring(nameStart, curClassName.length());
        StringBuffer buf = new StringBuffer();
        buf.append(curClassName).append(".").append(subkey);
        String fullkey = buf.toString();
        String retval = null;
        if (retval == null || retval.startsWith(fullkey))
        {
            retval = idata.langpack.getString(fullkey);
        }
        if (retval == null || retval.startsWith(fullkey))
        {
            if (alternateClass == null) { return (null); }
            buf.delete(0, buf.length());
            buf.append(alternateClass).append(".").append(subkey);
            retval = idata.langpack.getString(buf.toString());
        }
        if (retval != null && retval.indexOf('$') > -1)
        {
            VariableSubstitutor substitutor = new VariableSubstitutor(idata.getVariables());
            retval = substitutor.substitute(retval, null);
        }
        return (retval);
    }
    
    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method. If no key will be
     * found the key or - if alternate class is null - null returns.
     * 
     * @param subkey the subkey for the string which should be returned
     * @param alternateClass the short name of the class which should be used if no string is
     * present with the runtime class name
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey, String alternateClass, AutomatedInstallData idata)
    {
        return (getI18nStringForClass(getClass().getName().replaceAll("ConsoleHelper",""), subkey, alternateClass, idata));

    }
    
    /**
     * Calls the langpack of parent InstallerFrame for the String <tt>RuntimeClassName.subkey</tt>.
     * Do not add a point infront of subkey, it is always added in this method. If
     * <tt>RuntimeClassName.subkey</tt> is not found, the super class name will be used until it
     * is <tt>IzPanel</tt>. If no key will be found, null returns.
     * 
     * @param subkey the subkey for the string which should be returned
     * @return the founded string
     */
    public String getI18nStringForClass(String subkey)
    {
        String retval = null;
        Class<?> clazz = this.getClass();
        while (retval == null && !clazz.getName().endsWith(".IzPanel"))
        {
            retval = getI18nStringForClass(clazz.getName(), subkey, null);
            clazz = clazz.getSuperclass();
        }
        return (retval);
    }

    /**
     * All the same 'end of console' logic goodness, but with a
     * custom message for when the standard one just won't cut it!
     * @param idata
     * @param msg
     * @return
     */
    public static int askEndOfConsolePanel(AutomatedInstallData idata, String msg) {
     Shell console = Shell.getInstance();
        
        try
        {
            while (true)
            {
                System.out.println(msg);
                String strIn = console.getInput();
                System.out.println("");
                if (strIn.equals("1"))
                {
                    return 1;
                }
                else if (strIn.equals("2"))
                {
                    return 2;
                }
                else if (strIn.equals("3")) { return 3; }
            }

        }
        catch (Exception ie)
        {
            ie.printStackTrace();
        }
        return 2;
    }

    /**
     * Asks user the default message for 'end of console'.
     * @param idata
     * @return
     */
    public static int askEndOfConsolePanel(AutomatedInstallData idata)
    {
        String consoleAsk = idata.langpack.getString(CONSOLE_ASK);
        return askEndOfConsolePanel(idata, consoleAsk);
    }
    
    /**
     * Used for warning confirmation style prompts. Ie:
     * The target path already exists. Are you sure you wish to continue?
     * (yes|no):
     * @param question
     * @param defaultAnswer
     * @return
     */  
    public static int askYesNo(String question, boolean defaultanswer)
    {
        Shell console = Shell.getInstance();
        try
        {
            while (true)
            {
                System.out.print(question + " (y/n) [" + (defaultanswer ? "y" : "n") + "]:");
                String rline = console.getInput();
                if (rline.equals("y") || rline.equals("yes") )
                {
                    return AbstractUIHandler.ANSWER_YES;
                } else if (rline.equals("n") || rline.equals("no")){
                    return AbstractUIHandler.ANSWER_NO;
                } else if (rline.isEmpty()){
                    return defaultanswer ? AbstractUIHandler.ANSWER_YES : AbstractUIHandler.ANSWER_NO;
                } // if none of these are true, keep asking the question
            }
        }
        catch (Exception e)
        {
        }
        return AbstractUIHandler.ANSWER_NO;
    }

}
