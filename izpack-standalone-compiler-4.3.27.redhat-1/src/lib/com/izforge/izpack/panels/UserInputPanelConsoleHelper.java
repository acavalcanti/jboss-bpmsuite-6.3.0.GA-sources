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

import com.izforge.izpack.Pack;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.panels.UserInputPanel.*;
import com.izforge.izpack.util.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import static com.izforge.izpack.panels.UserInputPanel.*;
/**
 * The user input panel console helper class.
 *
 * @author Mounir El Hajj
 */
public class UserInputPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    private boolean erase = false;
    
    private boolean mustConfirm = true;
    
    private String eraseTo;

    protected int instanceNumber = 0;

    private static int instanceCount = 0;
    
    private static final int INITIAL = 0;
    
    private static final int CONFIRMATION = 1;

    private static final String SPEC_FILE_NAME = "userInputSpec.xml";

    private static final String NODE_ID = "panel";

    private static final String INSTANCE_IDENTIFIER = "order";

    protected static final String PANEL_IDENTIFIER = "id";

    private static final String FIELD_NODE_ID = "field";

    protected static final String ATTRIBUTE_CONDITIONID_NAME = "conditionid";

    private static final String VARIABLE = "variable";

    private static final String SET = "set";

    private static final String TEXT = "txt";

    private static final String SPEC = "spec";

    private static final String PWD = "pwd";

    private static final String SUMMARIZE = "summarize";

    private static final String AUTOPROMPT = "autoPrompt";

    private static final String HIDEINCONSOLE = "hideInConsole";

    private static final String REPLACEPATH = "replacePath";

    private static final String REVALIDATE = "revalidate";

    private static final String TYPE_ATTRIBUTE = "type";

    private static final String TEXT_FIELD = "text";

    private static final String COMBO_FIELD = "combo";

    private static final String STATIC_TEXT = "staticText";

    private static final String CHOICE = "choice";

    private static final String DIR = "dir";

    private static final String FILE = "file";

    private static final String FILE_DIR = "filedir";

    private static final String PASSWORD = "password";

    private static final String VALUE = "value";

    private static final String RADIO_FIELD = "radio";

    private static final String TITLE_FIELD = "title";

    private static final String CHECK_FIELD = "check";

    private static final String RULE_FIELD = "rule";

    private static final String SPACE = "space";

    private static final String DIVIDER = "divider";

    private static final String BUTTON = "button";

    static final String DISPLAY_FORMAT = "displayFormat";

    static final String PLAIN_STRING = "plainString";

    static final String SPECIAL_SEPARATOR = "specialSeparator";

    static final String LAYOUT = "layout";

    static final String RESULT_FORMAT = "resultFormat";

    private static final String DESCRIPTION = "description";

    private static final String TRUE = "true";

    private static final String NAME = "name";

    private static final String FAMILY = "family";

    private static final String OS = "os";

    private static final String SELECTEDPACKS = "createForPack";

    private static final String STRING_PORT_TEMPLATE = "port.template";
    private static final String STRING_TO_PASTE      = "regex.template";
    private static final String STRING_MULTICAST_ADDRESS_TEMPLATE = "multicast-address.template";
    private static final String STRING_MULTICAST_PORT_TEMPLATE    = "multicast-port.template";
    private static final String OFFSET_TEMPLATE             = "offset.template";
    private static final String VALIDATOR_PORT_TEMPLATE     = "validator.template";
    private static final String VALIDATOR_M_RANGE_TEMPLATE   = "multicast-port-range-validator.template";
    private static final String VALIDATOR_M_ADDRESS_TEMPLATE = "multicast-address-validator.template";
    private static final String VALIDATOR_M_PORT_TEMPLATE    = "multicast-port-validator.template";
    private static final String VALIDATOR_ADDRESS_TEMPLATE   = "verify-address-validator.template";
    private static final String VALIDATOR_RANGE_TEMPLATE = "range-validator.template";

    private static final String MULTICAST_ADDRESS_VALIDATOR     = "multicast-address.validator";
    private static final String MULTICAST_PORT_VALIDATOR        = "multicast-port.validator";
    private static final String MULTICAST_PORT_RANGE_VALIDATOR = "multicast-port.range-validator";
    private static final String ADDRESS_VALIDATOR               = "verify-address.validator";
    private static final String VALIDATOR                       = "validator";
    private static final String RANGE_VALIDATOR                 = "range-validator";

    private static final String VALIDATION_FAILED = "UserInputPanel.validation.failed";
    private static final String VALIDATION_ERROR  = "UserInputPanel.validation.error";
    private static final String CONSOLE_ERROR     = "UserInputPanel.console.error";
    private static final String INPUT_SELECTION   = "UserInputPanel.input.selection";
    private static final String INPUT_CHOICE      = "UserInputPanel.choice";

    private static final String OFFSET_VARIABLE_ELEMENT = "maximum.offset.variable";
    private static final String MULTICAST_ADDRESS   = "multicast-address";
    private static final String MULTICAST_PORT      = "multicast-port";
    private static final String ADDRESS             = "verify-address";
    private static final String PORT                = "port";

    private static Input SPACE_INTPUT_FIELD = new Input(SPACE, null, null, SPACE, "\r", 0);
    private static Input DIVIDER_INPUT_FIELD = new Input(DIVIDER, null, null, DIVIDER, "------------------------------------------", 0);

    private static String portTemplateText          = null;
    private static String regexTemplate             = null;
    private static String multicastTemplateText     = null;
    private static String multicastAddTemplateText  = null;
    private static String validatorRangeText        = null;
    private static String validatorMulticastAddressText = null;
    private static String validatorMulticastPortText    = null;
    private static String validatorMulticastRangeText   = null;
    private static String validatorVerifyAddressText    = null;
    private static String validatorPortText             = null;
    private static String offsetText                    = null;
    private static String portOffsetVariable            = null;
    private static String offsetValidator               = null;
    
    //private static HashMap prevPassword = new HashMap(); // I do not really like this solution, idata should not be set until verified
    private static String prevPassword = "";

    public List<Input> listInputs;

    /**
     * Holds all user inputs for use in automated installation
     */
    private Vector<TextValuePair> entriesForAutomatedInstall = new Vector<TextValuePair>();

    /**
     * Holds all keys from idata that must be ReversePathSubstituted
     */
    private HashSet<String> replacePathMap = new HashSet<String>();

    /**
     * holds all user inputs for use in summary panel.
     */
    private Vector<TextValuePair> entriesForSummaryPanel = new Vector<TextValuePair>();

    public UserInputPanelConsoleHelper()
    {
        instanceNumber = instanceCount++;
        listInputs = new ArrayList<Input>();

    }

    public List<ValidatorContainer> analyzeValidator(IXMLElement specElement, AutomatedInstallData idata)
    {
        List<ValidatorContainer> result = null;

        // ----------------------------------------------------
        // get the validator and processor if they are defined
        // ----------------------------------------------------

        Vector<IXMLElement> validatorsElem = specElement.getChildrenNamed(VALIDATOR);
        if (validatorsElem != null && validatorsElem.size() > 0)
        {
            int vsize = validatorsElem.size();

            result = new ArrayList<ValidatorContainer>(vsize);

            for (int i = 0; i < vsize; i++)
            {
                IXMLElement element = validatorsElem.get(i);
                String validator = element.getAttribute(CLASS);
                String message = getStrText(element, idata);
                HashMap<String, String> validateParamMap = new HashMap<String, String>();
                // ----------------------------------------------------------
                // check and see if we have any parameters for this validator.
                // If so, then add them to validateParamMap.
                // ----------------------------------------------------------
                Vector<IXMLElement> validateParams = element.getChildrenNamed(RULE_PARAM);
                if (validateParams != null && validateParams.size() > 0)
                {
                    Iterator<IXMLElement> iter = validateParams.iterator();
                    while (iter.hasNext())
                    {
                        element = iter.next();
                        String paramName = element.getAttribute(RULE_PARAM_NAME);
                        String paramValue = element.getAttribute(RULE_PARAM_VALUE);

                        validateParamMap.put(paramName, paramValue);
                    }
                }
                result.add(new ValidatorContainer(validator, message, validateParamMap));
            }
        }
        return result;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {

        collectInputs(installData);
        Iterator<Input> inputIterator = listInputs.iterator();
        while (inputIterator.hasNext())
        {
            String strVariableName = ((Input) inputIterator.next()).strVariableName;
            if (strVariableName != null)
            {
                String strVariableValue = p.getProperty(strVariableName);
                if (strVariableValue != null)
                {
                    installData.setVariable(strVariableName, strVariableValue);
                }
            }
        }
        return true;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Asks the panel to set its own XML data that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    /*--------------------------------------------------------------------------*/
    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
    {
        Map<String, String> entryMap = new LinkedHashMap<String, String>();

        for (int i = 0; i < entriesForAutomatedInstall.size(); i++)
        {
            TextValuePair pair = entriesForAutomatedInstall.elementAt(i);
            final String key = pair.toString();

            /**
             * AutoPrompt variables will not have their values displayed in the auto xml.
             */
            if (pair.getValue() == null){
                entryMap.put(key, null);
            } else {
                String value = idata.getVariable(key);
                if(replacePathMap.contains(key)){
                    value = ReversePathSubstitutor.substitute("INSTALL_PATH", value);
                }
                entryMap.put(key, value);
            }
        }

        new UserInputPanelAutomationHelper(entryMap).makeXMLData(idata, panelRoot);
    }

    @Override
    public String getSummaryBody(AutomatedInstallData idata)
    {
        Map<String, String> entryMap = new HashMap<String, String>();
        for (int i = 0; i < entriesForSummaryPanel.size(); i++)
        {
            TextValuePair pair = entriesForSummaryPanel.elementAt(i);
            final String key = pair.toString();
            entryMap.put(key, idata.getVariable(key));
        }

        Iterator<String> keys = entryMap.keySet().iterator();
        StringBuilder tmp = new StringBuilder();

        while (keys.hasNext())
        {
            String key = keys.next();
            String value = entryMap.get(key);
            String keyname = idata.langpack.getString(key);
            tmp.append(keyname + ": " + value + "<br>");
        }

        if (tmp.toString().trim().isEmpty()) {
            return null;
        } else {
            return tmp.toString();
        }
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        collectInputs(installData);
        Iterator<Input> inputIterator = listInputs.iterator();
        while (inputIterator.hasNext())
        {
            Input input = (Input) inputIterator.next();
            if (input.strVariableName != null)
            {
                printWriter.println(input.strVariableName + "=");
            }
        }
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
    {
        boolean processpanel = collectInputs(idata);
        if (!processpanel) {
            return true;
        }
        boolean status = true;
        Iterator<Input> inputsIterator = listInputs.iterator();
        try
        {
            while (inputsIterator.hasNext())
            {
                Input input = inputsIterator.next();

                if (TEXT_FIELD.equals(input.strFieldType)
                    || FILE.equals(input.strFieldType)
                    || RULE_FIELD.equals(input.strFieldType)
                    || DIR.equals(input.strFieldType)
                    || FILE_DIR.equals(input.strFieldType)    )
                {
                    status = status && processTextField(input, idata);
                }
                else if (COMBO_FIELD.equals(input.strFieldType)
                        || RADIO_FIELD.equals(input.strFieldType))
                {
                    status = status && processComboRadioField(input, idata);
                }
                else if (CHECK_FIELD.equals(input.strFieldType))
                {
                    status = status && processCheckField(input, idata);
                }
               else if(STATIC_TEXT.equals(input.strFieldType)
                           || TITLE_FIELD.equals(input.strFieldType)
                           || DIVIDER.equals(input.strFieldType)
                           || SPACE.equals(input.strFieldType) )
               {
                   status = status && processSimpleField(input, idata);
               }
               else if (PASSWORD.equals(input.strFieldType) ) {
                   status = status && processPasswordField(input, idata);
               }
               else if (BUTTON.equals(input.strFieldType)){
                    status = status && processButtonField(input, idata);
                }
            }
        }
        catch (RevalidationTriggeredException e)
        {
            return runConsole(idata, parent);
        }

        int i = askEndOfConsolePanel(idata);
        if (i == 1) {
            //Clear password for other panels
            prevPassword = "";
            return true;
        }
        else if (i == 2) {
            return false;
        }
        else {
            // if we repeat this panel, clear out the entries vectors so only ones added through the new run are included,
            // not stale data
            entriesForSummaryPanel.clear();
            entriesForAutomatedInstall.clear();
            return runConsole(idata, parent);
        }
    }

    private String paste (String getChildren, IXMLElement spec, AutomatedInstallData idata) {
            return getTranslation(idata, getID(getChildren, spec, idata));
    }

    private String getID (String getChildren, IXMLElement spec, AutomatedInstallData idata) {
        Vector<IXMLElement> var = spec.getChildrenNamed(getChildren);
        String templateID = null;
        for (int i = 0; i < var.size(); i++) {
            IXMLElement element = var.elementAt(i);
            templateID = element.getAttribute(PANEL_IDENTIFIER);
        }
        return templateID;
    }

    public boolean collectInputs(AutomatedInstallData idata)
    {
        listInputs.clear();
        IXMLElement data;
        IXMLElement spec = null;
        Vector<IXMLElement> specElements;
        String attribute;
        String dataID;
        String panelid = ((Panel) idata.panelsOrder.get(idata.curPanelNumber)).getPanelid();
        String instance = Integer.toString(instanceNumber);
        SpecHelper specHelper = new SpecHelper();

        try {
            specHelper.readSpec(specHelper.getResource(SPEC_FILE_NAME));
        } catch (Exception e1) {

            e1.printStackTrace();
            return false;
        }

        specElements = specHelper.getSpec().getChildrenNamed(NODE_ID);
        for (int i = 0; i < specElements.size(); i++)
        {
            data = specElements.elementAt(i);
            attribute = data.getAttribute(INSTANCE_IDENTIFIER);
            dataID = data.getAttribute(PANEL_IDENTIFIER);
            if (((attribute != null) && instance.equals(attribute))
                    || ((dataID != null) && (panelid != null) && (panelid.equals(dataID))))
            {

                Vector<IXMLElement> forPacks = data.getChildrenNamed(SELECTEDPACKS);
                Vector<IXMLElement> forOs = data.getChildrenNamed(OS);

                if (itemRequiredFor(forPacks, idata) && itemRequiredForOs(forOs)) {
                    spec = data;
                    break;
                }
            }
        }

        if (spec == null) {
            return false;
        }
        Vector<IXMLElement> fieldsRegexs = spec.getChildrenNamed(STRING_TO_PASTE);
        for (int i = 0; i < fieldsRegexs.size(); i++) {
            IXMLElement fieldRegex = fieldsRegexs.elementAt(i);
            regexTemplate = fieldRegex.getAttribute(TEXT);
        }

        portTemplateText            = paste(STRING_PORT_TEMPLATE, spec, idata);
        multicastAddTemplateText    = paste(STRING_MULTICAST_ADDRESS_TEMPLATE, spec, idata);
        multicastTemplateText       = paste(STRING_MULTICAST_PORT_TEMPLATE, spec, idata);
        validatorMulticastAddressText = paste(VALIDATOR_M_ADDRESS_TEMPLATE, spec, idata);
        validatorMulticastPortText    = paste(VALIDATOR_M_PORT_TEMPLATE, spec, idata);
        validatorVerifyAddressText    = paste(VALIDATOR_ADDRESS_TEMPLATE, spec, idata);
        validatorMulticastRangeText   = paste(VALIDATOR_M_RANGE_TEMPLATE, spec, idata);
        validatorPortText             = paste(VALIDATOR_PORT_TEMPLATE, spec, idata);
        validatorRangeText             = paste(VALIDATOR_RANGE_TEMPLATE, spec, idata);
        offsetText                     = paste(OFFSET_TEMPLATE, spec, idata);
        offsetValidator                = getID(OFFSET_TEMPLATE, spec, idata);
        portOffsetVariable             = getID(OFFSET_VARIABLE_ELEMENT, spec, idata);

        Vector<IXMLElement> fields = spec.getChildrenNamed(FIELD_NODE_ID);
        for (int i = 0; i < fields.size(); i++)
        {
            IXMLElement field = fields.elementAt(i);

            Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
            Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

            if (itemRequiredFor(forPacks, idata) && itemRequiredForOs(forOs)) {

                String conditionid = field.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
                if (conditionid != null)
                {
                    // check if condition is fulfilled
                    if (!idata.getRules().isConditionTrue(conditionid, idata.getVariables()))
                    {
                        continue;
                    }
                }
                Input in = getInputFromField(field, idata);
                // erase and eraseTo is used to avoid reprinting the same option
                // again. So it skips till the relevant option needs to be
                // printed
                //
                // comment this whole if condition to see what happens without
                // it when selecting options in radio / check buttons
                if (erase) {
                    if (in == null || in.strVariableName == null || in.strVariableName.equals(eraseTo) == false) {
                        continue;
                    } else {
                        erase = false;
                        continue;
                    }
                }
                if (in != null) {
                    if (in instanceof Button) {} else {
                        in.validators = analyzeValidator(field, idata);
                        if (in instanceof Password) {
                            for (Input singleInput : ((Password) in).input) {
                                singleInput.validators = in.validators;
                            }
                        }
                    }
                    listInputs.add(in);
                }
            }
         }
        return true;
    }

    boolean processSimpleField(Input input, AutomatedInstallData idata)
    {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        System.out.println(vs.substitute(input.strText, null));
        return true;
    }
    
    boolean processPasswordTextField(Input input, int stage, AutomatedInstallData idata, boolean autoPrompt) {
    	String variable = input.strVariableName;
        StringInputProcessingClient validation = null;
        String set = ""; // for passwords, there is no default value, and we don't want to show a default value.
        String value = "";
        String fieldText;
        String strIn = null;
        String prevPwd = prevPassword;
        
        if (prevPwd != null)
        {
            if (stage==INITIAL)
            {
                mustConfirm = false;
            }
            for (int j = 0; j < prevPwd.length(); j++)
            {
                set += "*";
            }
        } 
        else 
        {
            prevPwd = "";
        }
        
        if ((variable == null) || (variable.length() == 0)) 
        { 
            return true;
        }

        if (input.listChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }

        fieldText = input.listChoices.get(stage).strText;
        
        while (true) {
            if ((stage==CONFIRMATION) && (!mustConfirm))
            {
                value = prevPwd;
                mustConfirm = true;
                break;
            }
			boolean done = true;
			Shell console = Shell.getInstance();
			System.out.println(fieldText + " [" + set + "] ");
			
			try {
				strIn = new String(console.getPassword());
			} catch (Exception e) {
				e.printStackTrace();
				done = false;
			}

			if (!strIn.trim().equals("") || ((stage==CONFIRMATION) &&  mustConfirm) )
			{
			    value = strIn;
			    mustConfirm = true;
			}
			else
			{
			    value = prevPwd;
			}
			
			if (input.validators != null && !input.validators.isEmpty()) {
				validation = new StringInputProcessingClient(value, input.validators);
			}
			
			if (!validation.validate()) {
				done = false;
				// System.out.println(getTranslation(idata, VALIDATION_FAILED));
				//System.out.println("Validation failed....");
				if (stage == INITIAL) //Clear password when failing to set it initially
				{				    			    
				    prevPassword = "";
				    set = "";
				}				
				System.out.println(getTranslation(idata, VALIDATION_ERROR) + ": " + validation.getValidationMessage());
			}
            if (done) {
                break;
            }
		}
        if (stage==INITIAL)
        {
            prevPassword = value;
        }
        else if (stage == CONFIRMATION)
        {
            prevPassword = "";
        }
        idata.setVariable(variable, value);
        addToSummary(input, variable, value);
        addToAutomated(input, variable, value);
        return true;
    }

    boolean processPasswordField(Input input, AutomatedInstallData idata) {

        Password pwd = (Password) input;
        boolean autoPrompt = pwd.getAutoPrompt();

        boolean rtn = false;
        List<String> values = new LinkedList<String>();
        for (int i = 0; i < pwd.input.length; i++)
        {
            while (true)
            {
                boolean done = true;
                rtn = processPasswordTextField(pwd.input[i], i, idata, autoPrompt);
                
                if (!rtn) return rtn;
                values.add(idata.getVariable(pwd.input[i].strVariableName));
                if (i > 0 && pwd.validators != null && !pwd.validators.isEmpty())
                {
                    MultipleFieldValidator validation = new MultipleFieldValidator(values,
                            pwd.validators);
                    if (!validation.validate())
                    {
                        values.clear();
                        done = false;
                        //System.out.println(getTranslation(idata, VALIDATION_FAILED));
                        prevPassword = ""; //You want to clear your initial password if you can't confirm it.
                        System.out.println(getTranslation(idata, VALIDATION_ERROR) 
                                + ": " + validation.getValidationMessage());
                        i = 0;
                    }
                }
                if (done)
                {
                    break;
                }
            }
        }

        return rtn;

    }

    boolean processButtonField(Input input, AutomatedInstallData idata) {
        Button button = (Button) input;
        String message = button.getMessageId();
        String question = button.getQuestionId();
        List<String> errorMessages = button.getErrorMessages();

        int answer = askYesNo(getTranslation(idata,question), false);
        if (answer == ANSWER_YES) {
            int i = 0;
            for (ValidatorContainer validatorContainer : button.validators) {
                // create a dummy processor to follow izpack strictures
                StringInputProcessingClient dummyProcessor = new StringInputProcessingClient("Dummy", new ArrayList<ValidatorContainer>());
                Validator validator = validatorContainer.getValidator();
                boolean returnValue = validator.validate(dummyProcessor);
                if (!returnValue) {
                    System.out.println(getTranslation(idata, VALIDATION_ERROR) + ": " + getTranslation(idata, errorMessages.get(i)));
                } else {
                    System.out.println(getTranslation(idata, message));
                }
                i++;
            }
        }
        return true;
    }

    boolean processTextField(Input input, AutomatedInstallData idata)
    {
        String variable = input.strVariableName;
        boolean passwordText  = PASSWORD.equals(input.strFieldType);
        boolean directoryText = DIR.equals(input.strFieldType);
        boolean fileText = FILE.equals(input.strFieldType);
        boolean filedirText = FILE_DIR.equals(input.strFieldType);
        StringInputProcessingClient validation = null;
        String set;
        String fieldText;
        String value;
        String strIn;
        if ((variable == null) || (variable.length() == 0)) { return true; }

        if (input.listChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }

        set = idata.getVariable(variable);
        if (set == null)
        {
            set = input.strDefaultValue;
            if (set == null)
            {
                set = "";
            }
        }

        if (set != null && !"".equals(set))
        {
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            set = vs.deepSubstitute(set, null);
        }
        fieldText = input.listChoices.get(0).strText;

        while (true) {
            boolean done = true;
            if (fieldText == null) {
                System.out.println(" [" + set + "] ");
            } else {
                System.out.println(fieldText + " [" + set + "] ");
            }
            
            if(directoryText || fileText || filedirText) {
                Shell console = Shell.getInstance();
                strIn = console.getLocation(directoryText);
                if (!strIn.isEmpty())
                {
                    strIn = new File(strIn).getAbsolutePath();
                }
            }
            else {
                Shell console = Shell.getInstance();
                strIn = console.getInput(true);
            }
            String testValue = null;
            String systemProp = null;
            boolean isSystemProperty = false;
            value = !strIn.trim().equals("") ? strIn : set;

            if (input.validators != null && !input.validators.isEmpty())
            {
                // yeah I know I'm hardcoding the string "port" and "address"
                // sorry!
                if (value != null  && value.startsWith("${")  && value.contains(":")  && value.endsWith("}"))
                {
                	isSystemProperty = true;
                    String[] split = null;
                   
                    testValue = value.substring(2, value.length() - 1);
                    // second part is the ip address
                    split = testValue.split(":");

                    if (split.length >= 2) {
                        systemProp = split[0];
                        testValue = split[1];
                    }
                    
                    validation = new StringInputProcessingClient(testValue, input.validators);
                       	
                } else {
                    validation = new StringInputProcessingClient(value, input.validators);
                }

                if(variable.contains("port") || variable.contains("address") || variable.contains("standalone") || variable.contains("domain"))
                {
                    validation.setNumFields(2);
                }
                if (!validation.validate()) {
                    done = false;
                    System.out.println(getTranslation(idata, VALIDATION_ERROR) 
                            + ": " + validation.getValidationMessage());
                } 
               
                
                
            }

            if (done)
            {
            	if (isSystemProperty) {	            	
                	idata.setVariable(variable+"-1", systemProp); 
                	idata.setVariable(variable+"-2", testValue);      	
            	}
            	
                idata.setVariable(variable, value); // Add the username
                addToSummary(input, variable, value);
                addToAutomated(input, variable, value);
                break;
            }
            idata.setVariable(variable, value);
            addToSummary(input, variable, value);
            addToAutomated(input, variable, value);
        }
        return true;
        

    }

    boolean processComboRadioField(Input input, AutomatedInstallData idata)
    {// TODO protection if selection not valid and no set value
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        String variable = input.strVariableName;
        if ((variable == null) || (variable.length() == 0)) { return false; }
        String currentvariablevalue = idata.getVariable(variable);
                  //If we dont do this, choice with index=0 will always be displayed, no matter what is selected
                  input.iSelectedChoice = -1;
        boolean userinput = false;

        // display the description for this combo or radio field
        if (input.strText != null) {
            System.out.println(input.strText);
        }

        List<Choice> lisChoices = input.listChoices;
        if (lisChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in file field");
            return false;
        }
        // if currentvariablename is "", then skip
        if (currentvariablevalue != null && currentvariablevalue.equals("") == false)
        {
            userinput = true;
        }
        for (int i = 0; i < lisChoices.size(); i++)
        {
            Choice choice = lisChoices.get(i);
            String value = choice.strValue;
            // if the choice value is provided via a property to the process, then
            // set it as the selected choice, rather than defaulting to what the
            // spec defines.
            if (userinput)
            {
                if ((value != null) && (value.length() > 0) && (currentvariablevalue.equals(value)))
                {
                    input.iSelectedChoice = i;
                }
            }
            else
            {
                String set = choice.strSet;
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        input.iSelectedChoice = i;
                    }
                    if (currentvariablevalue == null) {
                        currentvariablevalue = input.listChoices.get(input.iSelectedChoice).strValue;
                    }
                }
            }
            if (input.iSelectedChoice == -1) input.iSelectedChoice = 0;
        }
        
        for (int i = 0; i < lisChoices.size(); i++)
        {
            Choice choice = lisChoices.get(i);
            choice.strText = vs.substitute(choice.strText);
            System.out.println(i + "  [" + (input.iSelectedChoice == i ? "x" : " ") + "] "
                    + (choice.strText != null ? choice.strText : ""));
        }
        Shell console = Shell.getInstance();
        boolean bKeepAsking = true;

        while (bKeepAsking)
        {
            System.out.println(getTranslation(idata, INPUT_SELECTION) + ": ");
            String strIn = console.getInput();
            // take default value if default value exists and no user input
            if (strIn.trim().equals("") && input.iSelectedChoice != -1)
            {
                bKeepAsking = false;
            }
            int j = -1;
            try
            {
                j = Integer.valueOf(strIn).intValue();
            }
            catch (Exception ex)
            {}
            // take user input if user input is valid
            if (j >= 0 && j < lisChoices.size())
            {
                input.iSelectedChoice = j;
                bKeepAsking = false;
            }
        }
        String newValue = input.listChoices.get(input.iSelectedChoice).strValue;
        idata.setVariable(variable, newValue);
        addToSummary(input, variable, newValue);
        addToAutomated(input, variable, newValue);

        /**
         * Note: This throws an exception when the user changes the value here,
         * so all code needed goes before this point!
         */
        if (input.revalidate && !currentvariablevalue.equals(newValue)) {
            erase = true;
            eraseTo = variable;
            throw new RevalidationTriggeredException();
        }
        return true;
    }

    boolean processCheckField(Input input, AutomatedInstallData idata)
    {
        String variable = input.strVariableName;
        if ((variable == null) || (variable.length() == 0)) { return false; }
        String currentvariablevalue = idata.getVariable(variable);
        if (currentvariablevalue == null)
        {
            currentvariablevalue = "";
        }
        List<Choice> lisChoices = input.listChoices;
        if (lisChoices.size() == 0)
        {
            Debug.trace("Error: no spec element defined in check field");
            return false;
        }
        Choice choice = null;
        for (int i = 0; i < lisChoices.size(); i++)
        {
            choice = lisChoices.get(i);
            String value = choice.strValue;

            if ((value != null) && (value.length() > 0) && (currentvariablevalue.equals(value)))
            {
                input.iSelectedChoice = i;
            }
            else
            {
                String set = input.strDefaultValue;
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }
                    if (set.equals(TRUE))
                    {
                        input.iSelectedChoice = 1;
                    }
                }
            }
        }
        System.out.println("  [" + (input.iSelectedChoice == 1 ? "x" : " ") + "] "
                + (choice.strText != null ? choice.strText : ""));
        
        Shell console = Shell.getInstance();
        boolean bKeepAsking = true;

        while (bKeepAsking)
        {
            System.out.println(getTranslation(idata, INPUT_CHOICE));
            String strIn = console.getInput();
            // take default value if default value exists and no user input
            if (strIn.trim().equals(""))
            {
                bKeepAsking = false;
            }
            int j = -1;
            try
            {
                j = Integer.valueOf(strIn).intValue();
            }
            catch (Exception ex)
            {}
            // take user input if user input is valid
            if ((j == 0) || j == 1)
            {
                input.iSelectedChoice = j;
                bKeepAsking = false;
            }
        }
        String newValue = input.listChoices.get(input.iSelectedChoice).strValue;
        idata.setVariable(variable, newValue);
        addToSummary(input, variable, newValue);
        addToAutomated(input, variable, newValue);

        /**
         * Note: This throws an exception when the user changes the value here,
         * so all code needed goes before this point!
         */
        if (input.revalidate && !currentvariablevalue.equals(newValue)) {
            erase = true;
            eraseTo = variable;
            throw new RevalidationTriggeredException();
        }
        return true;
    }

    public static String getTranslation(AutomatedInstallData idata, String id) {

        String temp = null;

        try {
           temp = idata.langpack.getString(id);
        } catch (NullPointerException e) {
            // Do nothing: ID not found
            temp = null;
        } finally {
            return temp;
        }
    }

    public static String getStrText(IXMLElement field, AutomatedInstallData idata) {
        String temp;
        String[] i;
        String j;
        final String ID = "id";
        String idAttribute = field.getAttribute(ID);

        /*
         * If spec is defined with attribute template="true"
         * and strings id are defined for port.template and multicast.template,
         * then start parsing. Also figure out when to use multicast or port template
         * based on the name of the id
         */
        if (isTemplateUsed(field, idata)) {
            i = idAttribute.split("\\.");
            j = i[i.length - 2];

            if (idAttribute.endsWith (MULTICAST_PORT)) {
                return multicastTemplateText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith (MULTICAST_ADDRESS)) {
                return multicastAddTemplateText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith (PORT)) {
                return portTemplateText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith (MULTICAST_ADDRESS_VALIDATOR)) {
                j = i[i.length - 3];
                return validatorMulticastAddressText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith (MULTICAST_PORT_VALIDATOR)) {
                j = i[i.length - 3];
                return validatorMulticastPortText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith(MULTICAST_PORT_RANGE_VALIDATOR)) {
                j = i[i.length -3];
                return validatorMulticastRangeText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith (ADDRESS_VALIDATOR)) {
                j = i[i.length - 3];
                return validatorVerifyAddressText.replaceAll(regexTemplate, j);
            } else if (idAttribute.endsWith(RANGE_VALIDATOR)) {
                return validatorRangeText.replaceAll(regexTemplate, j);
            } else if (idAttribute.equals(offsetValidator)) {
                return offsetText.replaceAll(regexTemplate, idata.getVariable(portOffsetVariable));
            } else if (idAttribute.endsWith (VALIDATOR)) {
                return validatorPortText.replaceAll(regexTemplate, j);
            } else {
                j = i[i.length - 1];
                return portTemplateText.replaceAll(regexTemplate, j);
            }
        }

        if (idAttribute == null) {
            if (field.hasAttribute(TEXT)) {
                return field.getAttribute(TEXT);
            } else {
                return "";
            }
        }

        temp = getTranslation(idata, idAttribute);
        if (temp != null) {
            return temp;
        } else if (field.hasAttribute(TEXT)) {
            return field.getAttribute(TEXT);
        } else {
            return "";
        }
    }

    private static boolean isTemplateUsed(IXMLElement field, AutomatedInstallData idata) {
        String temp;
        final String TEMPLATE = "template";

        temp = field.getAttribute(TEMPLATE);
        if (temp == null) {
            return false;
        } else {
            return Boolean.valueOf(temp);
        }
    }

    public Input getInputFromField(IXMLElement field, AutomatedInstallData idata)
    {
        String strVariableName = field.getAttribute(VARIABLE);
        String strFieldType = field.getAttribute(TYPE_ATTRIBUTE);
        String prompt = field.getAttribute(AUTOPROMPT);
        String summ = field.getAttribute(SUMMARIZE);
        String hideCons = field.getAttribute(HIDEINCONSOLE);
        boolean autoPrompt = (prompt != null) ? Boolean.parseBoolean(prompt) : false;
        boolean summarize = (summ != null) ? Boolean.parseBoolean(summ) : true;
        boolean hideInConsole = (hideCons != null) ? Boolean.parseBoolean(hideCons) : false;
        
        if (hideInConsole)
        {
            return null;
        }

        if (TITLE_FIELD.equals(strFieldType))
        {
            String strText = null;
            strText = getStrText(field, idata);
            return new Input(strVariableName, null, null, TITLE_FIELD, strText, 0);
        }

        if (STATIC_TEXT.equals(strFieldType))
        {
            String strText = null;
            strText = getStrText(field, idata);
            return new Input(strVariableName, null, null, STATIC_TEXT, strText, 0);
        }

        if (TEXT_FIELD.equals(strFieldType) || FILE.equals(strFieldType) || DIR.equals(strFieldType) || FILE_DIR.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            String reval = spec.getAttribute(REVALIDATE, "false");
            boolean revalidate = (reval != null) ? "yes".equals(reval) : false;

            String repPath = spec.getAttribute(REPLACEPATH);
            boolean replacePath = (repPath != null) ? Boolean.parseBoolean(repPath) : false;
            if (spec != null)
            {
                strText = getStrText(spec, idata);
                strSet = spec.getAttribute(SET);
            }
            if (description != null)
            {
                strFieldText = getStrText(description, idata);
            }
            choicesList.add(new Choice(strText, null, strSet));
            return new Input(strVariableName, strSet, choicesList, strFieldType, strFieldText, 0, summarize, autoPrompt, revalidate, replacePath);

        }

        if (RULE_FIELD.equals(strFieldType))
        {

            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            if (spec != null)
            {
                if (isTemplateUsed(spec, idata)) {

                }
                strText = getStrText(spec, idata);
                strSet = spec.getAttribute(SET);
            }
            if (description != null)
            {
                strFieldText = getStrText(description, idata);
            }
            if (strSet != null && spec.getAttribute(LAYOUT) != null)
            {
                StringTokenizer layoutTokenizer = new StringTokenizer(spec.getAttribute(LAYOUT));
                List<String> listSet = Arrays.asList(new String[layoutTokenizer.countTokens()]);
                StringTokenizer setTokenizer = new StringTokenizer(strSet);
                String token;
                while (setTokenizer.hasMoreTokens())
                {
                    token = setTokenizer.nextToken();
                    if (token.indexOf(":") > -1)
                    {
                        listSet.set(new Integer(token.substring(0, token.indexOf(":"))).intValue(),
                                token.substring(token.indexOf(":") + 1));
                    }
                }

                int iCounter = 0;
                StringBuffer sb = new StringBuffer();
                String strRusultFormat = spec.getAttribute(RESULT_FORMAT);
                String strSpecialSeparator = spec.getAttribute(SPECIAL_SEPARATOR);
                while (layoutTokenizer.hasMoreTokens())
                {
                    token = layoutTokenizer.nextToken();
                    if (token.matches(".*:.*:.*"))
                    {
                        sb.append(listSet.get(iCounter) != null ? listSet.get(iCounter) : "");
                        iCounter++;
                    }
                    else
                    {
                        if (SPECIAL_SEPARATOR.equals(strRusultFormat))
                        {
                            sb.append(strSpecialSeparator);
                        }
                        else if (PLAIN_STRING.equals(strRusultFormat))
                        {

                        }
                        else
                        // if (DISPLAY_FORMAT.equals(strRusultFormat))
                        {
                            sb.append(token);
                        }

                    }
                }
                strSet = sb.toString();
            }
            choicesList.add(new Choice(strText, null, strSet));
            return new Input(strVariableName, strSet, choicesList, TEXT_FIELD, strFieldText, 0);

        }

        if (COMBO_FIELD.equals(strFieldType) || RADIO_FIELD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            int selection = -1;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            String reval = spec.getAttribute(REVALIDATE, "false");
            boolean revalidate = (reval != null) ? "yes".equals(reval) : false;
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            Vector<IXMLElement> choices = null;
            if (spec != null)
            {
                choices = spec.getChildrenNamed(CHOICE);
            }
            if (description != null)
            {
                strFieldText = getStrText(description, idata);
            }
            for (int i = 0; i < choices.size(); i++)
            {
            

                IXMLElement choice = choices.elementAt(i);
                String processorClass = choice.getAttribute("processor");
                String conditionid = choice.getAttribute(ATTRIBUTE_CONDITIONID_NAME);                
                if (conditionid != null)
                {
                    // check if condition is fulfilled
                    if (!idata.getRules().isConditionTrue(conditionid, idata.getVariables()))
                    {
                        continue;
                    }
                }
                if (processorClass != null && !"".equals(processorClass))
                {
                    String choiceValues = "";
                    try
                    {
                        choiceValues = ((Processor) Class.forName(processorClass).newInstance())
                                .process(null);
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace();
                    }
                    String set = choice.getAttribute(SET);
                    if (set == null)
                    {
                        set = "";
                    }
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        set = vs.substitute(set, null);
                    }

                    StringTokenizer tokenizer = new StringTokenizer(choiceValues, ":");
                    int counter = 0;
                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();
                        String choiceSet = null;
                        if (token.equals(set)
                                ) {
                            choiceSet="true";
                            selection=counter;
                        }
                        choicesList.add(new Choice(
                                    token,
                                    token,
                                    choiceSet));
                        counter++;

                    }
                }
                else
                {
                    String value = choice.getAttribute(VALUE);

                    String set = choice.getAttribute(SET);
                     if (set != null)
                    {
                        if (set != null && !"".equals(set))
                        {
                            VariableSubstitutor vs = new VariableSubstitutor(idata
                                    .getVariables());
                            set = vs.substitute(set, null);
                        }
                        if (set.equalsIgnoreCase(TRUE))
                        {
                            selection=i;

                        }
                    }


                    choicesList.add(new Choice(
                                getStrText(choice, idata),
                                value,
                                set));

                    }
                }

            if (choicesList.size() == 1) {
                selection = 0;
            }

            return new Input(strVariableName, null, choicesList, strFieldType, strFieldText, selection,
                    revalidate, summarize, autoPrompt);
        }

        if (CHECK_FIELD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;
            int iSelectedChoice = 0;
            IXMLElement spec = field.getFirstChildNamed(SPEC);
            String reval = spec.getAttribute(REVALIDATE, "false");
            boolean revalidate = (reval != null) ? "yes".equals(reval) : false;
            IXMLElement description = field.getFirstChildNamed(DESCRIPTION);
            if (spec != null)
            {
                strText = getStrText(spec, idata);
                strSet = spec.getAttribute(SET);
                choicesList.add(new Choice(strText, spec.getAttribute("false"), null));
                choicesList.add(new Choice(strText, spec.getAttribute("true"), null));
                if (strSet != null)
                {
                    if (strSet.equalsIgnoreCase(TRUE))
                    {
                        iSelectedChoice = 1;
                    }
                }
            }
            else
            {
                System.out.println("No spec specified for input of type check");
            }

            if (description != null)
            {
                strFieldText = getStrText(description, idata);
            }
            return new Input(strVariableName, strSet, choicesList, CHECK_FIELD, strFieldText,
                    iSelectedChoice, revalidate, summarize, autoPrompt);
        }


        if (SPACE.equals(strFieldType) )
        {
            return SPACE_INTPUT_FIELD;

        }

        if (DIVIDER.equals(strFieldType))
        {
            return DIVIDER_INPUT_FIELD;
        }


        if (PASSWORD.equals(strFieldType))
        {
            List<Choice> choicesList = new ArrayList<Choice>();
            String strFieldText = null;
            String strSet = null;
            String strText = null;


            IXMLElement spec = field.getFirstChildNamed(SPEC);
            if (spec != null)
            {
                Vector<IXMLElement> pwds = spec.getChildrenNamed(PWD);
                if (pwds == null || pwds.size() == 0) {
                    System.out.println("No pwd specified in the spec for type password");
                    return null;
                }

                Input[] inputs = new Input[pwds.size()];
                for (int i = 0; i < pwds.size(); i++)
                {

                    IXMLElement pwde = pwds.elementAt(i);
                    strText = getStrText(pwde, idata);
                    strSet = pwde.getAttribute(SET);
                    choicesList.add(new Choice(strText, null, strSet));
                    inputs[i] = new Input(strVariableName, strSet, choicesList, strFieldType, strFieldText, 0, summarize, autoPrompt);

                }
                 return new Password(strFieldType, inputs, autoPrompt, summarize);

             }

            System.out.println("No spec specified for input of type password");
            return null;
        }

        if (BUTTON.equals(strFieldType)){
            IXMLElement spec = field.getFirstChildNamed(SPEC);

            if (spec != null){
                String questionText = spec.getAttribute(ID);
                List<ValidatorContainer> validatorContainers = analyzeValidator(spec, idata);
                if (validatorContainers.isEmpty()){
                    System.out.println("Button field has no validators.");
                    return null;
                }

                List<String> errorMessages = new ArrayList<String>();
                for (IXMLElement validatorTag : spec.getChildrenNamed(VALIDATOR)){
                    errorMessages.add(validatorTag.getAttribute("id"));
                }

                String successText = spec.getAttribute("msg");

                Button button = new Button(strFieldType, questionText, successText, errorMessages);
                button.validators = validatorContainers;
                return button;
            }
        }
        System.out.println(strFieldType + " field collection not implemented");
        return null;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Verifies if an item is required for any of the packs listed. An item is required for a pack
     * in the list if that pack is actually selected for installation. <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returnd. The same is
     * true if the <code>packs</code> list is empty.
     *
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes a
     * pack for which an item should be created if the pack is actually installed.
     * @return <code>true</code> if the item is required for at least one pack in the list,
     * otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     *
     * The information about the installed packs comes from InstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * --------------------------------------------------------------------------
     */
    private boolean itemRequiredFor(Vector<IXMLElement> packs, AutomatedInstallData idata)
    {

        String selected;
        String required;

        if (packs.size() == 0) { return (true); }

        // ----------------------------------------------------
        // We are getting to this point if any packs have been
        // specified. This means that there is a possibility
        // that some UI elements will not get added. This
        // means that we can not allow to go back to the
        // PacksPanel, because the process of building the
        // UI is not reversable.
        // ----------------------------------------------------
        // packsDefined = true;

        // ----------------------------------------------------
        // analyze if the any of the packs for which the item
        // is required have been selected for installation.
        // ----------------------------------------------------
        for (int i = 0; i < idata.selectedPacks.size(); i++)
        {
            selected = ((Pack) idata.selectedPacks.get(i)).name;

            for (int k = 0; k < packs.size(); k++)
            {
                required = (packs.elementAt(k)).getAttribute(NAME, "");
                if (selected.equals(required)) { return (true); }
            }
        }

        return (false);
    }

    /**
     * Adds an input's key and value to the summary panel iff its summarize property is true,
     * and it's autoPrompt property is false or not set. If both properties are true, it adds only
     * its key, but not its value.
     * @param input
     * @param key
     * @param value
     */
    private void addToSummary(Input input, String key, String value) {
        if (input.getSummarize() && !input.getAutoPrompt()) {
                entriesForSummaryPanel.add(new TextValuePair(key, value));
        }
    }

    /**
     * Adds an input's key and value to the automated install iff its autoprompt property is not true,
     * otherwise, it adds only its key, but not its value.
     * @param input
     * @param key
     * @param value
     */
    private void addToAutomated(Input input, String key, String value) {
        if (input.getAutoPrompt()){
            entriesForAutomatedInstall.add(new TextValuePair(key, null));
        } else {
            if (input.getReplacePath()){
                replacePathMap.add(key);
            }
            entriesForAutomatedInstall.add(new TextValuePair(key, value));
        }
    }

    /**
     * Verifies if an item is required for the operating system the installer executed. The
     * configuration for this feature is: <br/>
     * &lt;os family="unix"/&gt; <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of the os is empty then <code>true</code> is always returnd.
     *
     * @param os The <code>Vector</code> of <code>String</code>s. containing the os names
     * @return <code>true</code> if the item is required for the os, otherwise returns
     * <code>false</code>.
     */
    public boolean itemRequiredForOs(Vector<IXMLElement> os)
    {
        if (os.size() == 0) { return true; }

        for (int i = 0; i < os.size(); i++)
        {
            String family = (os.elementAt(i)).getAttribute(FAMILY);
            boolean match = false;

            if ("windows".equals(family))
            {
                match = OsVersion.IS_WINDOWS;
            }
            else if ("mac".equals(family))
            {
                match = OsVersion.IS_OSX;
            }
            else if ("unix".equals(family))
            {
                match = OsVersion.IS_UNIX;
            }
            if (match) { return true; }
        }
        return false;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Extracts the text from an <code>IXMLElement</code>. The text must be defined in the resource
     * file under the key defined in the <code>id</code> attribute or as value of the attribute
     * <code>txt</code>.
     *
     * @param element the <code>IXMLElement</code> from which to extract the text.
     * @param idata installer data
     * @return The text defined in the <code>IXMLElement</code>. If no text can be located,
     * <code>null</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private String getText(IXMLElement element, AutomatedInstallData idata)
    {
        if (element == null) { return (null); }

        String text = getStrText(element, idata);
        if (text == null || text.length() == 0) {
            text = element.getAttribute(ID);
        }
        if (text != null && text.length() > 0) {
            // try to parse the text, and substitute any variable it finds
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

            return (vs.substitute(text, null));
        }

        return text;
    }

    private class RevalidationTriggeredException extends RuntimeException
    {

    }

    public static class Input
    {

        public Input(String strFieldType)
        {
                this.strFieldType = strFieldType;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                     String strFieldType, String strFieldText, int iSelectedChoice, boolean summarize, boolean autoPrompt) {
            this(strVariableName, strDefaultValue, listChoices, strFieldType, strFieldText, iSelectedChoice);
            this.summarize = summarize;
            this.autoPrompt = autoPrompt;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                     String strFieldType, String strFieldText, int iSelectedChoice, boolean summarize, boolean autoPrompt, boolean revalidate, boolean replacePath) {
            this(strVariableName, strDefaultValue, listChoices, strFieldType, strFieldText, iSelectedChoice);
            this.summarize = summarize;
            this.autoPrompt = autoPrompt;
            this.replacePath = replacePath;
            this.revalidate = revalidate;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                String strFieldType, String strFieldText, int iSelectedChoice)
        {
            this.strVariableName = strVariableName;
            this.strDefaultValue = strDefaultValue;
            this.listChoices = listChoices;
            this.strFieldType = strFieldType;
            this.strText = strFieldText;
            this.iSelectedChoice = iSelectedChoice;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices, String strFieldType,
                     String strFieldText, int iSelectedChoice, boolean revalidate, boolean summarize, boolean autoPrompt) {
            this(strVariableName, strDefaultValue, listChoices, strFieldType, strFieldText, iSelectedChoice, revalidate);
            this.summarize = summarize;
            this.autoPrompt = autoPrompt;
        }

        public Input(String strVariableName, String strDefaultValue, List<Choice> listChoices,
                String strFieldType, String strFieldText, int iSelectedChoice, boolean revalidate)
        {
            this(strVariableName, strDefaultValue, listChoices, strFieldType, strFieldText, iSelectedChoice);
            this.revalidate = revalidate;
        }

        public boolean getSummarize() { return this.summarize; }
        public boolean getAutoPrompt() { return this.autoPrompt; }
        public boolean getReplacePath() {return this.replacePath; }

        String strVariableName;

        String strDefaultValue;

        List<Choice> listChoices;

        String strFieldType;

        String strText;

        int iSelectedChoice = -1;

        boolean revalidate;

        boolean summarize = true;

        boolean autoPrompt = false;

        boolean replacePath = false;

        public List<ValidatorContainer> validators;
    }

    public static class Choice
    {

        public Choice(String strText, String strValue, String strSet)
        {
            this.strText = strText;
            this.strValue = strValue;
            this.strSet = strSet;
        }

        String strText;

        String strValue;

        String strSet;
    }

    public static class Password extends Input
    {
        Input[] input;

        public Password(String strFieldType, Input[] input, boolean autoPrompt, boolean summarize) {
            super(strFieldType);
            this.input = input;
            this.autoPrompt = autoPrompt;
            this.summarize = summarize;
        }
    }

    public static class Button extends Input {

        private String successMessageId;
        private List<String> errorMessages;
        private String questionId;

        public Button(String strFieldType, String question, String successMessage, List<String> errors) {
            super(strFieldType);
            this.questionId = question;
            this.successMessageId = successMessage;
            this.errorMessages = errors;
        }

        public String getMessageId(){
            return successMessageId;
        }

        public List<String> getErrorMessages() {
            return errorMessages;
        }

        public String getQuestionId(){
            return questionId;
        }
    }
    /**
     * @Override
     */
    public String getInstanceNumber(){
        return Integer.toString(instanceNumber);
    }

}
