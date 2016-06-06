/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Jonathan Halliday
 * Copyright 2002 Elmar Grom
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

import com.izforge.izpack.Panel;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.util.*;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;

import java.util.*;

/**
 * Functions to support automated usage of the UserInputPanel
 *
 * @author Jonathan Halliday
 * @author Elmar Grom
 */
public class UserInputPanelAutomationHelper implements PanelAutomation
{

    // ------------------------------------------------------
    // automatic script section keys
    // ------------------------------------------------------
    private static final String AUTO_KEY_USER_INPUT = "userInput";

    private static final String AUTO_KEY_ENTRY = "entry";

    // ------------------------------------------------------
    // automatic script keys attributes
    // ------------------------------------------------------
    private static final String AUTO_ATTRIBUTE_KEY = "key";

    private static final String AUTO_ATTRIBUTE_VALUE = "value";

    private static final String AUTO_ATTRIBUTE_AUTOPROMPT = "autoPrompt";

    private static final String REPLACEPATH = "replacePath";

    // ------------------------------------------------------
    // String-String key-value pairs
    // ------------------------------------------------------
    private Map<String, String> entries;

    /**
     * userInputSpec
     */
//    public static Vector<IXMLElement> fields;
    private static SpecHelper specHelper;
    private static Vector<IXMLElement> panels;

    private static final String SPEC_FILE_NAME = "userInputSpec.xml";
    private static final String FIELD_NODE_ID = "field";
    private static final String VALIDATOR = "validator";
    private static final String CLASS = "class";
    protected static final String RULE_PARAM = "param";
    private static final String VALIDATION_ERROR  = "UserInputPanel.validation.error";
    private static final String PANEL_NODE_ID = "panel";
    private static final String PANEL_ORDER = "order";
    protected static final String PANEL_IDENTIFIER = "id";




    /**
     * Default constructor, used during automated installation.
     */
    public UserInputPanelAutomationHelper()
    {
        this.entries = null;
    }

    /**
     * @param entries String-String key-value pairs representing the state of the Panel
     */
    public UserInputPanelAutomationHelper(Map<String, String> entries)
    {
        this.entries = entries;
    }

    /**
     * Serialize state to XML and insert under panelRoot.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        IXMLElement userInput;
        IXMLElement dataElement;

        // ----------------------------------------------------
        // add the item that combines all entries
        // ----------------------------------------------------
        userInput = new XMLElementImpl(AUTO_KEY_USER_INPUT,panelRoot);
        panelRoot.addChild(userInput);

        // ----------------------------------------------------
        // add all entries
        // ----------------------------------------------------
        Iterator<String> keys = this.entries.keySet().iterator();
        while (keys.hasNext())
        {
            boolean autoPrompt = false;
            String key = keys.next();
            String value = this.entries.get(key);

            if (value == null){
                /**
                 * flag value for prompted strings, kind of fragile
                 */
                autoPrompt = true;
                idata.autoPromptVars.add(key);
            } 
            dataElement = new XMLElementImpl(AUTO_KEY_ENTRY,userInput);
            dataElement.setAttribute(AUTO_ATTRIBUTE_KEY, key);
            if (autoPrompt){
                dataElement.setAttribute(AUTO_ATTRIBUTE_AUTOPROMPT, "true");
            } else {
                dataElement.setAttribute(AUTO_ATTRIBUTE_VALUE, value);
            }

            userInput.addChild(dataElement);
        }
    }

    /**
     * Deserialize state from panelRoot and set idata variables accordingly.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML root element of the panels blackbox tree.
     * @throws InstallerException if some elements are missing.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot) throws InstallerException
    {
        IXMLElement userInput;
        IXMLElement dataElement;
        String variable;
        String value;
        String autoPrompt;
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        // ----------------------------------------------------
        // get the section containing the user entries
        // ----------------------------------------------------
        userInput = panelRoot.getFirstChildNamed(AUTO_KEY_USER_INPUT);

        if (userInput == null)
        {
            throw new InstallerException("Missing userInput element on line " + panelRoot.getLineNr());
        }

        Vector<IXMLElement> userEntries = userInput.getChildrenNamed(AUTO_KEY_ENTRY);

        if (userEntries == null)
        {
            throw new InstallerException("Missing entry element(s) on line " + panelRoot.getLineNr());
        }

        // ----------------------------------------------------
        // retieve each entry and substitute the associated
        // variable
        // ----------------------------------------------------
        for (int i = 0; i < userEntries.size(); i++)
        {
            dataElement = userEntries.elementAt(i);
            variable = dataElement.getAttribute(AUTO_ATTRIBUTE_KEY);
            value = dataElement.getAttribute(AUTO_ATTRIBUTE_VALUE);
            autoPrompt = dataElement.getAttribute(AUTO_ATTRIBUTE_AUTOPROMPT);


            /**
             * Handle autoPrompt attributes, if needed.
             */
            if (!getAutoPromptVariable(idata, variable, autoPrompt)){
                /**
                 * Otherwise set Regular attributes get here.
                 */
                Debug.trace("UserInputPanel: setting variable " + variable + " to " + value);
                value = vs.substitute(value);
                idata.setVariable(variable, value);
            }

        }
    }

    /**
     * Read the userInput spec file and return an IXMLElement containing the current field.
     * @throws Exception
     */
    private static IXMLElement readFieldSpec(AutomatedInstallData idata, String variable) throws Exception {
        IXMLElement curField = null;
        String panelId = ((Panel) idata.panelsOrder.get(idata.curPanelNumber)).getPanelid();
        Integer panelOrder = idata.curPanelNumber;

        try {
            /**
             * Read the spec file only once.
             */
            if (specHelper == null) {
                specHelper = new SpecHelper();

                // read the userInput spec file from xml.
                specHelper.readSpec(specHelper.getResource(SPEC_FILE_NAME));
            }
            curField = specHelper.getFieldForVariable(variable);

        } catch (Exception e) {
            Debug.log(e.getMessage());
        } finally {
            return curField; // this may be a null
        }
    }

    /**
     * Call this from panels that use userInputPanel.xml.
     * @param idata
     * @param variable
     * @param autoPrompt
     * @return
     */

    public static boolean getAutoPromptVariable(AutomatedInstallData idata, String variable, String autoPrompt) {
        return getAutoPromptVariable(idata, variable, autoPrompt, false, null, null);
    }

    /**
     * Call this from custom panels
     * @param idata
     * @param variable
     * @param autoPrompt
     * @param prompt
     * @param rePrompt
     * @return
     */
    public static boolean getAutoPromptVariable(AutomatedInstallData idata, String variable, String autoPrompt, String prompt, String rePrompt){
        return getAutoPromptVariable(idata, variable, autoPrompt, true, prompt, rePrompt);
    }

    /**
     * Checks if a variable in the auto xml spec file has the 'autoPrompt' attribute set to 'true', and
     * handles getting that variable's value interactively if needed.
     * @param idata
     * @param variable
     * @param autoPrompt
     * @param customPanel use true if calling this method from a custom panel with no userInputPanel.xml spec
     * @return
     */
    private static boolean getAutoPromptVariable(AutomatedInstallData idata,
                                                String variable,
                                                String autoPrompt,
                                                boolean customPanel, String prompt, String rePrompt) {
        IXMLElement currentField = null;
        /**
         * Test for autoprompt usage.
         */
        if (autoPrompt == null || !autoPrompt.equalsIgnoreCase("true")) {
            return false;
        }

        /**
         * Find the xml spec for the current field.
         */
        if (!customPanel) {
            try {
                currentField = readFieldSpec(idata, variable);
            } catch (Exception e) {
                Debug.log("Unable to read userInputPanel spec xml. No validation available.");
            }
        }

        /**
         * Specified in the command line already?
         * If not, start a prompt for the user to enter it.
         * assume autoprompt variables can't be empty
         */
        if (idata.getVariable(variable) == null || idata.getVariable(variable).isEmpty()) {
            getInputs(idata, variable, currentField, prompt, rePrompt);

        } else {
            String value = idata.getVariable(variable);

            /**
             * Try the input validators, and default back to interactive user input if current value doesn't pass.
             */
            if (!(validate(idata, value, currentField))) {
                getInputs(idata, variable, currentField, prompt, rePrompt);
            }
            //Debug.trace("UserInputPanel: variable " + variable + " already set to " + value + " through command line arg.");
        }
        return true;
    }

    /**
     * Collects inputs from the console if the variable does not already have a value in iData.
     * @param idata
     * @param variable
     * @param currentField
     */
    public static void getInputs(AutomatedInstallData idata, String variable, IXMLElement currentField, String prompt, String rePrompt) {
        String value = "";
        Shell reader = Shell.getInstance();
        String first = "";
        String second = "1";
        boolean valid = false;

        if (currentField != null){
            Vector<IXMLElement> children = currentField.getFirstChildNamed("spec").getChildren();
            prompt = idata.langpack.getString(children.get(0).getAttribute("id"));
            rePrompt = idata.langpack.getString(children.get(1).getAttribute("id"));
        }

        while (!first.equals(second) || !valid) {
            System.out.println(prompt);
            first = reader.getPassword();
            System.out.println(rePrompt);
            second = reader.getPassword();

            /**
             * Make sure the two pwd inputs match
             */
            if (!first.equals(second)) {
               //System.out.println(idata.langpack.getString("password.no.match.user"));
               System.out.println("The values didn't match, please try again.");
               continue;
            }

            /**
             * Try the input validators, if any:
             */
            if (!(valid = validate(idata, first, currentField))) {
                continue;
            }

            /**
             * Passed all validation, so set this value.
             */
            value = first;
        }

        Debug.trace("UserInputPanel: setting variable " + variable + " to " + value);
        idata.setVariable(variable, value);
    }

    private static boolean validate(AutomatedInstallData idata, String input, IXMLElement currentField) {
        List<ValidatorContainer> validators = null;
        StringInputProcessingClient validation = null;
        boolean valid = false;

        /**
         * If we have a spec field, check it for validators and run validation on
         * the user input.
         */
        if (currentField != null) {
            validators = specHelper.getValidatorsFromField(idata, currentField);
            validation =  new StringInputProcessingClient(input, validators);
            if (!validation.validate()) {
                System.out.println(UserInputPanelConsoleHelper.getTranslation(idata, VALIDATION_ERROR) +
                        System.getProperty("line.separator") + validation.getValidationMessage());
                return false;
            } else {
                /**
                 * validators all pass, so we can return a valid input.
                 */
                return true;
            }
        } else {
            /*
             * If we don't have a field spec, there are no field validators and we return valid input.
             * However, we should be handling custom panel validation here if needed.
             */
            return true;
        }

    }

}
