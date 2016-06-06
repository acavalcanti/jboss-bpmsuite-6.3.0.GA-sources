/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Elmar Grom
 * Copyright 2009 Dennis Reil
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.izforge.izpack.panels;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLParser;
import com.izforge.izpack.adaptator.impl.XMLParser;
import com.izforge.izpack.gui.*;
import com.izforge.izpack.installer.*;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.rules.VariableExistenceCondition;
import com.izforge.izpack.util.*;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

public class UserInputPanel extends IzPanel implements ActionListener, ItemListener, FocusListener
{

    /**
     *
     */
    private static final long serialVersionUID = 3257850965439886129L;

    protected static final String ICON_KEY = "icon";

    /**
     * The name of the XML file that specifies the panel layout
     */
    private static final String SPEC_FILE_NAME = "userInputSpec.xml";

    private static final String LANG_FILE_NAME = "userInputLang.xml";

    /**
     * how the spec node for a specific panel is identified
     */
    private static final String NODE_ID = "panel";

    private static final String FIELD_NODE_ID = "field";

    private static final String INSTANCE_IDENTIFIER = "order";

    protected static final String PANEL_IDENTIFIER = "id";

    /** Decide to show this panel in summary or not, attribute places in panel tag */
    private static final String SUMMARY = "summary";

    /** Decide weather this field should be updated with information from  idata */
    private static final String DEPENDS_ON = "dependson";

    private static final String TYPE = "type";

    private static final String DESCRIPTION = "description";

    private static final String VARIABLE = "variable";

    private static final String AUTOPROMPT = "autoPrompt";

    private static final String REPLACEPATH = "replacePath";

    protected static final String TEXT = "txt";

    protected static final String ID = "id";

    private static final String SPEC = "spec";

    private static final String SET = "set";

    private static final String REVALIDATE = "revalidate";

    private static final String NO = "no";

    private static final String YES = "yes";

    private static final String TOPBUFFER = "topBuffer";

    private static final String RIGID = "rigid";

    private static final String TRUE = "true";

    private static final String FALSE = "false";

    private static final String ALIGNMENT = "align";

    private static final String LEFT = "left";

    private static final String CENTER = "center";

    private static final String RIGHT = "right";

    private static final String TOP = "top";

    private static final String ITALICS = "italic";

    private static final String BOLD = "bold";

    private static final String SIZE = "size";

    protected static final String VALIDATOR = "validator";

    private static final String PROCESSOR = "processor";

    protected static final String CLASS = "class";

    private static final String TITLE_FIELD = "title";

    private static final String TEXT_FIELD = "text";

    private static final String TEXT_SIZE = "size";

    private static final String TEXT_ROWS = "rows";

    private static final String STATIC_TEXT = "staticText";

    private static final String COMBO_FIELD = "combo";

    private static final String COMBO_CHOICE = "choice";

    private static final String COMBO_VALUE = "value";

    private static final String RADIO_FIELD = "radio";

    private static final String RADIO_CHOICE = "choice";

    private static final String RADIO_VALUE = "value";

    private static final String SPACE_FIELD = "space";

    private static final String DIVIDER_FIELD = "divider";

    private static final String CHECK_FIELD = "check";

    private static final String RULE_FIELD = "rule";

    private static final String RULE_LAYOUT = "layout";

    private static final String RULE_SEPARATOR = "separator";

    private static final String RULE_RESULT_FORMAT = "resultFormat";

    private static final String RULE_PLAIN_STRING = "plainString";

    private static final String RULE_DISPLAY_FORMAT = "displayFormat";

    private static final String RULE_SPECIAL_SEPARATOR = "specialSeparator";

    private static final String RULE_ENCRYPTED = "processed";

    protected static final String RULE_PARAM_NAME = "name";

    protected static final String RULE_PARAM_VALUE = "value";

    protected static final String RULE_PARAM = "param";

    private static final String PWD_FIELD = "password";

    private static final String PWD_INPUT = "pwd";

    private static final String PWD_SIZE = "size";

    private static final String SEARCH_FIELD = "search";

    private static final String FILE_FIELD = "file";

    private static final String FILE_DIR_FIELD = "filedir";

    private static final String DIR_FIELD = "dir";

    private static final String SEARCH_CHOICE = "choice";

    private static final String SEARCH_FILENAME = "filename";

    private static final String SEARCH_RESULT = "result";

    private static final String SEARCH_VALUE = "value";

    private static final String SEARCH_TYPE = "type";

    private static final String SEARCH_FILE = "file";

    private static final String SEARCH_DIRECTORY = "directory";

    private static final String SEARCH_PARENTDIR = "parentdir";

    private static final String SEARCH_CHECKFILENAME = "checkfilename";

    private static final String SELECTEDPACKS = "createForPack"; // renamed

    private static final String UNSELECTEDPACKS = "createForUnselectedPack"; // new

    private static final String DEFAULT_FOCUS = "defaultFocus"; // A field attribute.

    private static final String TOOLTIP = "tooltip"; // A field attribute.

    protected static final String ATTRIBUTE_CONDITIONID_NAME = "conditionid";

    protected static final String ATTRIBUTE_ALWAYS_DISPALAY = "alwaysDisplay";

    protected static final String VARIABLE_NODE = "variable";

    protected static final String ATTRIBUTE_VARIABLE_NAME = "name";

    protected static final String ATTRIBUTE_VARIABLE_VALUE = "value";

    private static final String ATTRIBUTE_INDENT = "indent";

    protected static final String ATTRIBUTE_TEXT_BOLD = "bold";

    private static final String BUTTON_FIELD = "button";
    // node

    private static final String NAME = "name";

    private static final String OS = "os";

    private static final String FAMILY = "family";

    private static final String MULTIPLE_FILE_FIELD = "multiFile";

    private static final String TEMPLATE            = "template";
    private static final String MULTICAST_PORT      = "multicast-port";
    private static final String MULTICAST_ADDRESS   = "multicast-address";
    private static final String PORT                = "port";

    private static final String PORT_TEMPLATE               = "port.template";
    private static final String MULTICAST_PORT_TEMPLATE     = "multicast-port.template";
    private static final String MULTICAST_ADDRESS_TEMPLATE  = "multicast-address.template";
    private static final String VALIDATOR_PORT_TEMPLATE     = "validator.template";
    private static final String OFFSET_TEMPLATE             = "offset.template";
    private static final String VALIDATOR_M_ADDRESS_TEMPLATE = "multicast-address-validator.template";
    private static final String VALIDATOR_M_PORT_TEMPLATE    = "multicast-port-validator.template";
    private static final String VALIDATOR_M_RANGE_TEMPLATE   = "multicast-port-range-validator.template";
    private static final String VALIDATOR_ADDRESS_TEMPLATE   = "verify-address-validator.template";
    private static final String VALIDATOR_RANGE_TEMPLATE   = "range-validator.template";
    private static final String VALIDATOR_COLLISION_TEMPLATE = "port.collision.check.template";
    private static final String REGEX_TEMPLATE               = "regex.template";

    private static final String MULTICAST_PORT_VALIDATOR        = "multicast-port.validator";
    private static final String MULTICAST_ADDRESS_VALIDATOR     = "multicast-address.validator";
    private static final String MULTICAST_PORT_RANGE_VALIDATOR = "multicast-port.range-validator";
    private static final String ADDRESS_VALIDATOR               = "verify-address.validator";
    private static final String RANGE_VALIDATOR               = "range-validator";
    private static final String OFFSET_VARIABLE_ELEMENT         = "maximum.offset.variable";
    private static final String COLLISION_VALIDATOR           = "port.collision.check";

    private static final String SUMMARIZE = "summarize";
    private static final String DEEP_SUB = "deepSub";

    private static String multicastAddress          = null;
    private static String multicastPort             = null;
    private static String portText                  = null;
    private static String offsetText                = null;
    private static String regexTemplate             = null;
    private static String validatorPortText         = null;
    private static String validatorRangeText         = null;
    private static String validatorVerifyAddressText    = null;
    private static String validatorMulticastAddressText = null;
    private static String validatorMulticastPortText    = null;
    private static String validatorMulticastRangeText   = null;
    private static String portOffsetVariable    = null;
    private static String offsetValidator       = null;
    private static String validatorCollisionText        = null;
    private static String PostInstallPanelId        = "postinstall";
    private boolean isVisible = true;
    private boolean isEnabled = true;
    private static Map<String, Boolean> wasVisited = new HashMap<String, Boolean>();
    // ------------------------------------------------------------------------
    // Variable Declarations
    // ------------------------------------------------------------------------
    private static int instanceCount = 0;

    protected int instanceNumber = 0;

    /**
     * If there is a possibility that some UI elements will not get added we can not allow to go
     * back to the PacksPanel, because the process of building the UI is not reversable. This
     * variable keeps track if any packs have been defined and will be used to make a decision for
     * locking the 'previous' button.
     */
    private boolean packsDefined = false;

    private InstallerFrame parentFrame;

    /**
     * The parsed result from reading the XML specification from the file
     */
    private IXMLElement spec;

    private boolean haveSpec = false;

    /**
     * Holds the references to all of the UI elements
     */
    // private Vector<Object[]> uiElements = new Vector<Object[]>();
    /**
     * Holds the references to all radio button groups
     */
    private Vector<ButtonGroup> buttonGroups = new Vector<ButtonGroup>();

    /**
     * Holds the references to all password field groups
     */
    private Vector<PasswordGroup> passwordGroups = new Vector<PasswordGroup>();

    /**
     * used for temporary storage of references to password groups that have already been read in a
     * given read cycle.
     */
    private Vector passwordGroupsRead = new Vector();

    /**
     * Used to track search fields. Contains SearchField references.
     */
    private Vector<SearchField> searchFields = new Vector<SearchField>();

    /**
     * Holds all user inputs for use in automated installation
     */
    private Vector<TextValuePair> entries = new Vector<TextValuePair>();

    /**
     * Holds all user inputs to be used in summary panel.
     */
    private Vector<TextValuePair> summaryEntries = new Vector<TextValuePair>();

    /**
     * Holds all keys from idata that must be ReversePathSubstituted
     */
    private HashSet<String> replacePathMap = new HashSet<String>();

    private LocaleDatabase langpack = null;

    // Used for dynamic controls to skip content validation unless the user
    // really clicks "Next"
    private boolean validating = true;

    private boolean eventsActivated = false;

    private Vector<UIElement> elements = new Vector<UIElement>();

    private JPanel panel;

    private JScrollPane scroller;

    private HashMap<String, String> dependencies = new HashMap<String, String>();
    private HashMap<String, String> passwords    = new HashMap<String, String>();

    /*--------------------------------------------------------------------------*/
    // This method can be used to search for layout problems. If this class is
    // compiled with this method uncommented, the layout guides will be shown
    // on the panel, making it possible to see if all components are placed
    // correctly.
    /*--------------------------------------------------------------------------*/
    // public void paint (Graphics graphics)
    // {
    // super.paint (graphics);
    // layout.showRules ((Graphics2D)graphics, Color.red);
    // }
    /*--------------------------------------------------------------------------*/
    /**
     * Constructs a <code>UserInputPanel</code>.
     *
     * @param parent reference to the application frame
     * @param installData shared information about the installation
     */
    /*--------------------------------------------------------------------------*/
    public UserInputPanel(InstallerFrame parent, InstallData installData)
    {
        super(parent, installData);
        instanceNumber = instanceCount++;
        this.parentFrame = parent;
    }

    private void createBuiltInVariableConditions(String variable) {

        if (variable == null ) return;

        VariableExistenceCondition variableCondition = new VariableExistenceCondition();
        variableCondition.setId("izpack.input." + variable);
        variableCondition.setInstalldata(idata);
        variableCondition.setVariable(variable);
        parent.getRules().addCondition(variableCondition);

    }

    //Initialize Panel
    protected void init() {

        eventsActivated = false;
        TwoColumnLayout layout;

        // get a locale database
        try {
            this.langpack = (LocaleDatabase) parent.langpack.clone();
            String resource = LANG_FILE_NAME + "_" + idata.localeISO3;
            this.langpack.add(ResourceManager.getInstance().getInputStream(resource));
        }
        catch (ResourceNotFoundException e) { Debug.trace(e);      }
        catch (Exception e)                 { e.printStackTrace(); }


        try {
            readSpec();
        }
        catch (Throwable exception) { exception.printStackTrace(); }


        // ----------------------------------------------------
        // Set the topBuffer from the attribute. topBuffer=0 is useful
        // if you don't want your panel to be moved up and down during
        // dynamic validation (showing and hiding components within the
        // same panel)
        // ----------------------------------------------------
        int topbuff = 25;
        boolean rigid = false;
        try {
            topbuff = Integer.parseInt(spec.getAttribute(TOPBUFFER));
        }
        catch (Exception ex) {}
        try {
            rigid = Boolean.parseBoolean(spec.getAttribute(RIGID));
        }
        catch (Exception ex) {}
        layout = new TwoColumnLayout(10, 5, 30, topbuff, rigid, TwoColumnLayout.LEFT);


        setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setLayout(layout);
        if (parent.hasBackground) panel.setOpaque(false);


        //Skip panel if couldn't read spec
        if (!haveSpec) return;


        // refresh variables specified in spec ... never really happens....
        updateVariables();

        //Process any templates
        IXMLElement temp = null;
        Vector<IXMLElement> fieldsTemplate = null;

        fieldsTemplate = spec.getChildrenNamed(REGEX_TEMPLATE);
        for (int i = 0; i < fieldsTemplate.size(); ++i) {
            temp = fieldsTemplate.elementAt(i);
            regexTemplate = temp.getAttribute(TEXT);
        }

        portText                         = getTranslatedTextFromID(spec, PORT_TEMPLATE);
        multicastPort                    = getTranslatedTextFromID(spec, MULTICAST_PORT_TEMPLATE);
        multicastAddress                 = getTranslatedTextFromID(spec, MULTICAST_ADDRESS_TEMPLATE);
        validatorPortText                = getTranslatedTextFromID(spec, VALIDATOR_PORT_TEMPLATE);
        validatorVerifyAddressText       = getTranslatedTextFromID(spec, VALIDATOR_ADDRESS_TEMPLATE);
        validatorMulticastAddressText    = getTranslatedTextFromID(spec, VALIDATOR_M_ADDRESS_TEMPLATE);
        validatorMulticastRangeText      = getTranslatedTextFromID(spec, VALIDATOR_M_RANGE_TEMPLATE);
        validatorMulticastPortText       = getTranslatedTextFromID(spec, VALIDATOR_M_PORT_TEMPLATE);
        validatorRangeText               = getTranslatedTextFromID(spec, VALIDATOR_RANGE_TEMPLATE);
        offsetText                       = getTranslatedTextFromID(spec, OFFSET_TEMPLATE);
        offsetValidator                  = getID(spec, OFFSET_TEMPLATE);
        portOffsetVariable               = getID(spec, OFFSET_VARIABLE_ELEMENT);
        validatorCollisionText           = getTranslatedTextFromID(spec, VALIDATOR_COLLISION_TEMPLATE);

        //Process Fields
        Vector<IXMLElement> fields = spec.getChildrenNamed(FIELD_NODE_ID);
        for (int i = 0; i < fields.size(); i++) {

            IXMLElement field         = fields.elementAt(i);
            String attribute          = field.getAttribute(TYPE);
            String associatedVariable = field.getAttribute(VARIABLE);

            // Create automatic existence condition
            createBuiltInVariableConditions(associatedVariable);

            String panelId = idata.panels.get(idata.curPanelNumber).getMetadata().getPanelid();
            String conditionId = field.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            String alwaysDisplay = field.getAttribute(ATTRIBUTE_ALWAYS_DISPALAY);


            if (panelId == null) panelId = "";

            // Check to see if we should display a field on the panel
            // Visible you get UI places exactly where they need to be without changing, leads to weird whitespace
            // Removing/Adding has no wide space, but small adjustments to UI as you add/remove
            if (conditionId != null && !this.parent.getRules().isConditionTrue(conditionId, idata.getVariables())) {
                if ((alwaysDisplay != null && alwaysDisplay.equals("true")) || this.isDisplayingHidden())
                {
                    isEnabled = false;
                }
                else
                {
                    continue;
                }
            }

            if (attribute != null) {

                if      (attribute.equals(BUTTON_FIELD))        addButtonField(field);
                else if (attribute.equals(CHECK_FIELD))         addCheckBox(field);
                else if (attribute.equals(COMBO_FIELD))         addComboBox(field);
                else if (attribute.equals(DIR_FIELD))           addDirectoryField(field);
                else if (attribute.equals(DIVIDER_FIELD))       addDivider(field);
                else if (attribute.equals(FILE_FIELD))          addFileField(field);
                else if (attribute.equals(FILE_DIR_FIELD))      addFileDirField(field);
                else if (attribute.equals(MULTIPLE_FILE_FIELD)) addMultipleFileField(field);
                else if (attribute.equals(PWD_FIELD))           addPasswordField(field);
                else if (attribute.equals(RADIO_FIELD))         addRadioButton(field);
                else if (attribute.equals(RULE_FIELD))          addRuleField(field);
                else if (attribute.equals(SEARCH_FIELD))        addSearch(field);
                else if (attribute.equals(SPACE_FIELD))         addSpace(field);
                else if (attribute.equals(STATIC_TEXT))         addText(field);
                else if (attribute.equals(TEXT_FIELD))          addTextField(field);
                else if (attribute.equals(TITLE_FIELD))         addTitle(field);

            }

            isVisible = true;
            isEnabled = true;

        } //End of Field Loop

        eventsActivated = true;

    } //End of init()


    //Translate ID attribut from a template with langpacks
    private String getTranslatedTextFromID (final IXMLElement spec, final String template) {
        String temp = getID(spec, template);
        return ( temp == null) ? null : langpack.getString(temp);
    }

    //Get the ID attribute from a template
    private String getID (final IXMLElement spec, final String template) {
        String temp = null;

        Vector<IXMLElement> fieldsTemplate = spec.getChildrenNamed(template);
        IXMLElement field = null;
        for (int i = 0; i < fieldsTemplate.size(); i++) {
            field = fieldsTemplate.firstElement();
            temp = field.getAttribute(ID);
        }
        return temp;
    }

    private List<ValidatorContainer> analyzeValidator(IXMLElement specElement)
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
                String message = getText(element);
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

    private void addDirectoryField(IXMLElement field)
    {
        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable = field.getAttribute(VARIABLE);

        if (variable == null  || spec == null) { return; }
        String value = idata.getVariable(variable);

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        String textSize   = spec.getAttribute(TEXT_SIZE, "1");
        List<ValidatorContainer> validatorConfig = analyzeValidator(field);

        int  size               = Integer.parseInt(textSize);
        boolean create          = Boolean.parseBoolean(spec.getAttribute("create", FALSE));
        boolean mustExist       = Boolean.parseBoolean(spec.getAttribute("mustExist", TRUE));
        boolean allowEmptyValue = Boolean.parseBoolean(spec.getAttribute("allowEmptyValue", FALSE));
        boolean replacePath     = Boolean.parseBoolean(spec.getAttribute("replacePath", FALSE));


        String  defaultValue = spec.getAttribute(SET, "");
        if(value != null ) defaultValue = value;

        if (!defaultValue.isEmpty())
        {
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            if (Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)))
            {
                defaultValue = vs.deepSubstitute(defaultValue, null);
            }
            else
            {
                defaultValue = vs.substitute(defaultValue, null);
            }
        }


        JLabel label = new JLabel(getText(spec));
        label.setEnabled(isEnabled);
        addToolTiptoElement(field, label);

        FileInputField fileInput = new DirInputField(this, idata, true, defaultValue, size, validatorConfig, mustExist, create);
        fileInput.setAllowEmptyInput(allowEmptyValue);
        fileInput.setEnabled(isEnabled);
        addToolTiptoElement(field, fileInput.getField());
        addToolTiptoElement(field, fileInput.getButton());

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.BOTH;

        UIElement dirUiElement = new UIElement();
        dirUiElement.setDeepSub(Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)));
        dirUiElement.setComponent(fileInput, parent.hasBackground);
        dirUiElement.setType(UIElementType.DIRECTORY);
        dirUiElement.setAssociatedVariable(variable);
        dirUiElement.setConstraints(constraints2);
        dirUiElement.setForPacks(forPacks);
        dirUiElement.setForOs(forOs);
        dirUiElement.setEnabled(isEnabled);
        dirUiElement.setReplacePath(replacePath);

        parseSummarizeAttribute(spec, dirUiElement);

        if (focusedOnActivate(field))  setInitialFocus(dirUiElement.getComponent());
        elements.add(labelUiElement);
        elements.add(dirUiElement);
    }

    private void addMultipleFileField(IXMLElement field)
    {
        String variable = field.getAttribute(VARIABLE);
        IXMLElement spec = field.getFirstChildNamed(SPEC);
        if ((variable == null) || (variable.length() == 0)) { return; }
        if (spec == null ) { return; }
        String value = idata.getVariable(variable);

        String labelText = getText(spec);
        int  size = Integer.parseInt(spec.getAttribute(TEXT_SIZE, "1"));

        String visRows = spec.getAttribute("visibleRows" , "10");
        String prefX = spec.getAttribute("prefX", "200");
        String prefY = spec.getAttribute("prefY", "200");
        String filter = spec.getAttribute("fileext", "");
        String filterdesc = spec.getAttribute("fileextdesc", "");
        List<ValidatorContainer> validatorConfig = analyzeValidator(field);

        int preferredX = Integer.parseInt(prefX);
        int preferredY =  Integer.parseInt(prefY);
        int visibleRows = Integer.parseInt(visRows);
        boolean allowEmptyValue = Boolean .parseBoolean(spec.getAttribute("allowEmptyValue", "false"));
        boolean createMultipleVariables =  Boolean.parseBoolean(spec.getAttribute("multipleVariables", "false"));

        filterdesc = this.langpack.getString(filterdesc);

        String defaultValue = spec.getAttribute(SET, ";");
        if(value != null && !value.isEmpty()) {
            defaultValue = value;
        }

        if (!defaultValue.isEmpty()) {
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            if (Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)))
                defaultValue = vs.deepSubstitute(defaultValue, null);
            else
                defaultValue = vs.substitute(defaultValue, null);
        }

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;

        MultipleFileInputField fileInputField = new MultipleFileInputField(parentFrame, idata,
                false, defaultValue, size, validatorConfig, filter, filterdesc, createMultipleVariables,
                visibleRows, preferredX, preferredY, labelText);
        fileInputField.setAllowEmptyInput(allowEmptyValue);
        for (String file : defaultValue.split(";")){
            if (!file.isEmpty()) fileInputField.addFile(file);
        }

        addToolTiptoElement(field, fileInputField.getFileList());
        addToolTiptoElement(field, fileInputField.getBrowseButton());

        UIElement fileUiElement = new UIElement();
        fileUiElement.setDeepSub((Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE))));
        fileUiElement.setComponent(fileInputField, parent.hasBackground);
        fileUiElement.setType(UIElementType.MULTIPLE_FILE);
        fileUiElement.setAssociatedVariable(variable);
        fileUiElement.setConstraints(constraints2);
        fileUiElement.setForPacks(forPacks);
        fileUiElement.setForOs(forOs);

        elements.add(fileUiElement);
    }

    private void addFileField(IXMLElement field) {

        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable = field.getAttribute(VARIABLE);

        if (variable == null  || spec == null) { return; }
        String value = idata.getVariable(variable);

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        String textSize   = spec.getAttribute(TEXT_SIZE, "1");
        String filter     = spec.getAttribute("fileext" , "");
        String filterdesc = spec.getAttribute("fileextdesc", "");
        filterdesc = idata.langpack.getString(filterdesc);
        List<ValidatorContainer> validatorConfig = analyzeValidator(field);


        int  size               = Integer.parseInt(textSize);
        boolean mustExist       = Boolean.parseBoolean(spec.getAttribute("mustExist", TRUE));
        boolean allowEmptyValue = Boolean.parseBoolean(spec.getAttribute("allowEmptyValue", FALSE));
        boolean replacePath     = Boolean.parseBoolean(spec.getAttribute(REPLACEPATH, FALSE));

        String  defaultValue = spec.getAttribute(SET, "");
        if(value != null ) defaultValue = value;

        if (!defaultValue.isEmpty()) {

            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            if (Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)))
                defaultValue = vs.deepSubstitute(defaultValue, null);
            else
                defaultValue = vs.substitute(defaultValue, null);

        }


        JLabel label = new JLabel(getText(spec));
        label.setEnabled(isEnabled);
        addToolTiptoElement(field, label);

        FileInputField fileInputField = new FileInputField(this, idata, false, defaultValue, size,
                validatorConfig, filter, filterdesc, mustExist);
        fileInputField.setAllowEmptyInput(allowEmptyValue);
        fileInputField.setEnabled(isEnabled);


        addToolTiptoElement(field, fileInputField.getField());
        addToolTiptoElement(field, fileInputField.getButton());

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.BOTH;

        UIElement fileUiElement = new UIElement();
        fileUiElement.setDeepSub((Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE))));
        fileUiElement.setComponent(fileInputField, parent.hasBackground);
        fileUiElement.setDependency(field.getAttribute(DEPENDS_ON, ""));
        fileUiElement.setAssociatedVariable(variable);
        fileUiElement.setConstraints(constraints2);
        fileUiElement.setType(UIElementType.FILE);
        fileUiElement.setForPacks(forPacks);
        fileUiElement.setForOs(forOs);
        fileUiElement.setEnabled(isEnabled);
        fileUiElement.setReplacePath(replacePath);
        parseSummarizeAttribute(spec, fileUiElement);

        if (focusedOnActivate(field)) setInitialFocus(fileUiElement.getComponent());

        elements.add(labelUiElement);
        elements.add(fileUiElement);

    }

    private void addFileDirField(IXMLElement field) {

        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable = field.getAttribute(VARIABLE);

        if (variable == null  || spec == null) { return; }
        String value = idata.getVariable(variable);

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = field.getChildrenNamed(OS);

        String textSize   = spec.getAttribute(TEXT_SIZE, "1");
        String filter     = spec.getAttribute("fileext" , "");
        String filterdesc = spec.getAttribute("fileextdesc", "");
        filterdesc = idata.langpack.getString(filterdesc);
        List<ValidatorContainer> validatorConfig = analyzeValidator(field);


        int  size               = Integer.parseInt(textSize);
        boolean mustExist       = Boolean.parseBoolean(spec.getAttribute("mustExist", TRUE));
        boolean allowEmptyValue = Boolean.parseBoolean(spec.getAttribute("allowEmptyValue", FALSE));
        boolean replacePath     = Boolean.parseBoolean(spec.getAttribute(REPLACEPATH, FALSE));

        String  defaultValue = spec.getAttribute(SET, "");
        if(value != null ) defaultValue = value;

        if (!defaultValue.isEmpty()) {

            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            if (Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)))
                defaultValue = vs.deepSubstitute(defaultValue, null);
            else
                defaultValue = vs.substitute(defaultValue, null);

        }


        JLabel label = new JLabel(getText(spec));
        label.setEnabled(isEnabled);
        addToolTiptoElement(field, label);

        FileInputField fileDirInputField = new FileDirInputField(this, idata, false, defaultValue, size,
                validatorConfig, mustExist);
        fileDirInputField.setAllowEmptyInput(allowEmptyValue);
        fileDirInputField.setEnabled(isEnabled);


        addToolTiptoElement(field, fileDirInputField.getField());
        addToolTiptoElement(field, fileDirInputField.getButton());

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;

        UIElement labelUiElement = new UIElement();
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.BOTH;

        UIElement fileUiElement = new UIElement();
        fileUiElement.setDeepSub((Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE))));
        fileUiElement.setComponent(fileDirInputField, parent.hasBackground);
        fileUiElement.setDependency(field.getAttribute(DEPENDS_ON, ""));
        fileUiElement.setAssociatedVariable(variable);
        fileUiElement.setConstraints(constraints2);
        fileUiElement.setType(UIElementType.FILE);
        fileUiElement.setForPacks(forPacks);
        fileUiElement.setForOs(forOs);
        fileUiElement.setEnabled(isEnabled);
        fileUiElement.setReplacePath(replacePath);
        parseSummarizeAttribute(spec, fileUiElement);

        if (focusedOnActivate(field)) setInitialFocus(fileUiElement.getComponent());

        elements.add(labelUiElement);
        elements.add(fileUiElement);

    }


    /*--------------------------------------------------------------------------*/
    /**
     * This method looks for the defaultFocus attribute in the current
     * XML field and returns "true" if defaultFocus both exists and has a value
     * of "true".
     *
     * @param AN XML field from userInputSpec file.
     */
    /*--------------------------------------------------------------------------*/
    private boolean focusedOnActivate(IXMLElement field) {

        String defaultFocus = field.getAttribute(DEFAULT_FOCUS);
        if ((defaultFocus != null) && (defaultFocus.equals(TRUE))) {
            return true;
        }
        return false;
    }

    /**
     * Checks for the presence of a tooltip attribute in this xml field. If present,
     * it sets the uielement's component's tooltip to the string that is referenced
     * by the value of the tooltip attribute.
     * @param IXMLElement The XML spec for this field.
     * @param UIElement The UIElement containing a UI component to add the tooltip to.
     */
    private void addToolTiptoElement(IXMLElement field, JComponent component)
    {
        String tooltip = field.getAttribute(TOOLTIP);
        if (tooltip != null) {
            component.setToolTipText("<html><p width=\"200\">" +idata.langpack.getString(tooltip)+"</p></html>");
        }
    }

    /**
     * Turns off summarize on a field only if the field contains the summarize attribute AND it is
     * set to 'false'. Otherwise by default, a field is shown in the summary panel.
     *
     * @param ele
     * @param field
     */
    private void parseSummarizeAttribute(IXMLElement ele, UIElement field)
    {
        if (ele == null || field == null)
        {
            return;
        }

        field.setSummarize(!(ele.hasAttribute(SUMMARIZE) && ele.getAttribute(SUMMARIZE).equals(
                "false")));
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated. Default behavior is to
     * return true.
     *
     * @return A boolean stating wether the panel has been validated or not.
     */
    /*--------------------------------------------------------------------------*/
    public boolean isValidated()
    {
        return readInput();
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is called when the panel becomes active.
     */
    /*--------------------------------------------------------------------------*/
    public void panelActivate()
    {
        super.panelActivate();
        super.removeAll();
        elements.clear();
        entries.clear();
        this.init();

        if (spec == null)
        {
            // TODO: translate
            emitError("User input specification could not be found.",
                    "The specification for the user input panel could not be found. Please contact the packager.");
            parentFrame.skipPanel();
        }

        updateVariables();
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forUnselectedPacks = spec.getChildrenNamed(UNSELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);

        if (!itemRequiredFor(forPacks) || !itemRequiredForUnselected(forUnselectedPacks)
                || !itemRequiredForOs(forOs))
        {
            parentFrame.skipPanel();
            return;
        }
        if (!haveSpec)
        {
            parentFrame.skipPanel();
            return;
        }
        buildUI();
        /*
        String panelid = this.getMetadata().getPanelid();
        if(wasVisited.get(panelid) == null){
            buildUI();
            wasVisited.put(panelid, true);
        }*/
        this.setSize(this.getMaximumSize().width, this.getMaximumSize().height);
        validate();
        if (packsDefined)
        {
            parentFrame.lockPrevButton();
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Asks the panel to set its own XML data that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    /*--------------------------------------------------------------------------*/
    public void makeXMLData(IXMLElement panelRoot)
    {
        Map<String, String> entryMap = new LinkedHashMap<String, String>();

        for (int i = 0; i < entries.size(); i++)
        {
            TextValuePair pair = entries.elementAt(i);
            // IZPACK-283: read the value from idata instead of panel data
            final String key = pair.toString();
            if (pair.getValue() == null){
                entryMap.put(key, null); // this should only happen if autoPrompt is true
            } else {
                String value = idata.getVariable(key);
                if(replacePathMap.contains(key)){
                   value = ReversePathSubstitutor.substitute("INSTALL_PATH",value);
                }
                entryMap.put(key, value);
            }
        }

        new UserInputPanelAutomationHelper(entryMap).makeXMLData(idata, panelRoot);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Builds the UI and makes it ready for display
     */
    /*--------------------------------------------------------------------------*/
    private void buildUI()
    {
        for (UIElement element : elements)
        {
            if (itemRequiredFor(element.getForPacks()) && itemRequiredForOs(element.getForOs()))
            {
                if (!element.isDisplayed())
                {
                    element.setDisplayed(true);
                    panel.add(element.getComponent(), element.getConstraints());
                }
            }
            else
            {
                if (element.isDisplayed())
                {
                    element.setDisplayed(false);
                    panel.remove(element.getComponent());
                }
            }
        }

        scroller = ScrollPaneFactory.createScroller(panel);
        Border emptyBorder = BorderFactory.createEmptyBorder();
        scroller.setViewportBorder(emptyBorder);
        scroller.getVerticalScrollBar().setBorder(emptyBorder);
        scroller.getHorizontalScrollBar().setBorder(emptyBorder);
        if (parent.hasBackground) scroller.getViewport().setOpaque(false);
        if (parent.hasBackground) scroller.setOpaque(false);

        add(scroller, BorderLayout.CENTER);
        parent.setFocus(scroller);
    }

    public void saveToInstallData(){
        this.saveToInstallData(false);
    }
    public void saveToInstallData(boolean fromNext){

        for (UIElement element : elements){
            String var = element.getAssociatedVariable();

            if (element.isDisplayed())
            {
                if (element.getType() == UIElementType.RULE){
                    RuleInputField ruleField = (RuleInputField) element.getComponent();
                    FinalAndInitialVariables moreVars = ruleField.getFinalAndInitialVariables();

                    int count = 0;
                    for(String temp: moreVars.getInitialVariables(var)) {
                        temp = temp.substring(temp.indexOf("{") + 1,
                                temp.indexOf("}"));
                        idata.setVariable(temp, ruleField.getFieldContents(count));
                        ++count;
                    }
                    idata.setVariable(var, ruleField.getText());
                }
                else if (element.getType() == UIElementType.TEXT){
                    TextInputField textField = (TextInputField) element.getComponent();
                    idata.setVariable(var, textField.getText());
                }
                else if (element.getType() == UIElementType.PASSWORD){
                    PasswordUIElement pwdField = (PasswordUIElement) element;
                    PasswordGroup group = (PasswordGroup) pwdField.getPasswordGroup();
                    String id = pwdField.getId();

                    if (fromNext) passwords.put(id, group.getNamedField(id));   //Set revalidation password on next
                    else          passwords.put(id, "");                        //Set revalidation password "" on prev
                    idata.setVariable(var, group.getPassword());
                }
                else if (element.getType() == UIElementType.CHECKBOX){
                    JCheckBox checkBox = (JCheckBox) element.getComponent();
                    String trueValue = element.getTrueValue();
                    String falseValue = element.getFalseValue();
                    if (trueValue == null) trueValue = "";
                    if (falseValue == null) falseValue="";
                    if (checkBox.isSelected()) {
                        idata.setVariable(var, trueValue);
                    }
                    else {
                        idata.setVariable(var, falseValue);
                    }
                }
                else if (element.getType() == UIElementType.RADIOBUTTON){
                    JRadioButton radioButton = (JRadioButton) element.getComponent();
                    if (radioButton.isSelected()) {
                        idata.setVariable(var, element.getTrueValue());
                    }
                }
                else if (element.getType() == UIElementType.COMBOBOX){
                    JComboBox comboBox = (JComboBox) element.getComponent();
                    String value = ((TextValuePair) comboBox.getSelectedItem()).getValue();
                    idata.setVariable(var, value);
                }
                else if (element.getType() == UIElementType.FILE){
                    FileInputField input = (FileInputField) element.getComponent();
                    String value = input.getSelectedFile().getAbsolutePath();
                    idata.setVariable(var, value);
                }
                else if (element.getType() == UIElementType.FILE_DIR){
                    FileInputField input = (FileInputField) element.getComponent();
                    String value = input.getSelectedFile().getAbsolutePath();
                    idata.setVariable(var, value);
                }
                else if (element.getType() == UIElementType.DIRECTORY){
                    FileInputField input = (FileInputField) element.getComponent();
                    String value = input.getSelectedFile().getAbsolutePath();
                    idata.setVariable(var, value);
                }
                else if (element.getType() == UIElementType.MULTIPLE_FILE) {
                    MultipleFileInputField input = (MultipleFileInputField) element.getComponent();
                    List<String> files = input.getSelectedFiles();
                    if (input.isCreateMultipleVariables()) {
                        int index = 0;
                        for (String file : files) {
                            StringBuffer indexedVariableName = new StringBuffer(var);
                            if (index > 0) {
                                indexedVariableName.append("_");
                                indexedVariableName.append(index);
                            }
                            index++;
                            idata.setVariable(indexedVariableName.toString(), file);
                        }

                    }
                    else if(files == null || files.isEmpty()) {
                        idata.setVariable(var, ";");
                    }
                    else{
                        StringBuffer buffer = new StringBuffer();
                        for (String file : files)
                        {
                            buffer.append(file);
                            buffer.append(";");
                        }
                        idata.setVariable(var, buffer.toString());
                    }
                }

            } //element.isDisplayed()
        } //UI Elements
    } //saveToInstallData
    /*--------------------------------------------------------------------------*/
    /**
     * Reads the input data from all UI elements and sets the associated variables.
     *
     * @return <code>true</code> if the operation is successdul, otherwise <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readInput()
    {
        boolean success = true;
        passwordGroupsRead.clear();
        summaryEntries.clear();


        saveToInstallData(true);

        for (UIElement element : elements)
        {
            if (element.isDisplayed() && element.isEnabled())
            {
                if (element.getType() == UIElementType.RULE)
                {
                    success = readRuleField(element);
                }
                else if (element.getType() == UIElementType.PASSWORD)
                {
                    success = readPasswordField(element);
                }
                else if (element.getType() == UIElementType.TEXT)
                {
                    success = readTextField(element);
                }
                else if (element.getType() == UIElementType.COMBOBOX)
                {
                    success = readComboBox(element);
                }
                else if (element.getType() == UIElementType.RADIOBUTTON)
                {
                    success = readRadioButton(element);
                }
                else if (element.getType() == UIElementType.CHECKBOX)
                {
                    success = readCheckBox(element);
                }
                else if (element.getType() == UIElementType.SEARCH)
                {
                    success = readSearch(element);
                }
                else if (element.getType() == UIElementType.MULTIPLE_FILE)
                {
                    success = readMultipleFileField(element);
                }
                else if (element.getType() == UIElementType.FILE)
                {
                    success = readFileField(element);
                }
                else if (element.getType() == UIElementType.DIRECTORY)
                {
                    success = readDirectoryField(element);
                }
                if (!success) { return (false); }
            }
        }
        return (true);
    }

    private boolean readDirectoryField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField input = (FileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                String value = "";
                File inputFile = input.getSelectedFile();
                if(inputFile != null && !inputFile.getName().equals("")){
                    value = input.getSelectedFile().getAbsolutePath();
                }

                if(field.getReplacePath()){
                    replacePathMap.add(field.getAssociatedVariable());
                }

                idata.setVariable(field.getAssociatedVariable(), value);
                entries.add(new TextValuePair(field.getAssociatedVariable(), value));
                if (field.isSummarized())
                    summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));

            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    private boolean readFileField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField input = (FileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                String value = "";
                File inputFile = input.getSelectedFile();
                if(inputFile != null && !inputFile.getName().equals("")){
                    value = input.getSelectedFile().getAbsolutePath();
                }

                if(field.getReplacePath()){
                    replacePathMap.add(field.getAssociatedVariable());
                }

                idata.setVariable(field.getAssociatedVariable(), value);
                entries.add(new TextValuePair(field.getAssociatedVariable(), value));
                if (field.isSummarized())
                    summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    private boolean readFileDirField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField input = (FileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                String value = "";
                File inputFile = input.getSelectedFile();
                if(inputFile != null && !inputFile.getName().equals("")){
                    value = input.getSelectedFile().getAbsolutePath();
                }

                if(field.getReplacePath()){
                    replacePathMap.add(field.getAssociatedVariable());
                }

                idata.setVariable(field.getAssociatedVariable(), value);
                entries.add(new TextValuePair(field.getAssociatedVariable(), value));
                if (field.isSummarized())
                    summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    private boolean readMultipleFileField(UIElement field)
    {
        boolean result = false;
        try
        {
            MultipleFileInputField input = (MultipleFileInputField) field.getComponent();
            result = !validating || input.validateField();
            if (result)
            {
                //String value = "";
                List<String> files = input.getSelectedFiles();
                String variable = field.getAssociatedVariable();
                if (input.isCreateMultipleVariables())
                {
                    int index = 0;
                    for (String file : files)
                    {
                        StringBuffer indexedVariableName = new StringBuffer(variable);
                        if (index > 0)
                        {
                            indexedVariableName.append("_");
                            indexedVariableName.append(index);
                        }
                        index++;
                        idata.setVariable(indexedVariableName.toString(), file);
                        entries.add(new TextValuePair(indexedVariableName.toString(), file));
                        if (field.isSummarized())
                            summaryEntries.add(new TextValuePair(field.getAssociatedVariable(),
                                    file));
                    }

                }
                else
                {
                    StringBuffer buffer = new StringBuffer();
                    for (String file : files)
                    {
                        buffer.append(file);
                        buffer.append(";");
                    }
                    idata.setVariable(variable, buffer.toString());
                    String value = buffer.toString();
                    entries.add(new TextValuePair(variable, value));
                    if (field.isSummarized())
                        summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
                }
            }
        }
        catch (Exception e)
        {
            if (Debug.stackTracing())
            {
                Debug.trace(e);
            }
        }
        return result;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the XML specification for the panel layout. The result is stored in spec.
     *
     * @throws Exception for any problems in reading the specification
     */
    /*--------------------------------------------------------------------------*/
    private void readSpec() throws Exception
    {
        InputStream input = null;
        IXMLElement data;
        Vector<IXMLElement> specElements;
        String attribute;
        String panelattribute;
        String instance = Integer.toString(instanceNumber); //TODO: Use getInstanceNumber()
        String summary;
        String condition;
        String panelid = null;
        Panel p = this.getMetadata();

        if (p != null) {
            panelid = p.getPanelid();
        }

        try {
            input = parentFrame.getResource(SPEC_FILE_NAME);
        } catch (Exception exception) {
            haveSpec = false;
            return;
        }

        if (input == null) {
            haveSpec = false;
            return;
        }

        IXMLParser parser = new XMLParser();
        data = parser.parse(input);

        /** extract the spec to this specific panel instance */
        if (data.hasChildren())
        {
            specElements = data.getChildrenNamed(NODE_ID);
            /** Find the <panel> (data) </panel> that coresponds to this panel */
            for (int i = 0; i < specElements.size(); i++)
            {
                data           = specElements.elementAt(i);
                summary        = data.getAttribute(SUMMARY);                //summary
                panelattribute = data.getAttribute(PANEL_IDENTIFIER);       //id
                attribute      = data.getAttribute(INSTANCE_IDENTIFIER);    //order

                /** If the order corresponds to the correct userInputSpec, use this piece of the spec */
                if (( attribute      != null && instance.equals(attribute) ) ||
                        ( panelattribute != null && panelid != null && panelid.equals(panelattribute) ))
                {
                    spec = data;
                    input.close(); //No longer need to read from UserInputSpec, close the stream

                    haveSpec = true;
                    return;
                }
            }
            haveSpec = false;
            return;
        }
        haveSpec = false;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds the title to the panel. There can only be one title, if mutiple titles are defined, they
     * keep overwriting what has already be defined, so that the last definition is the one that
     * prevails.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the title.
     */
    /*--------------------------------------------------------------------------*/
    private void addTitle(IXMLElement spec)
    {
        String title = getText(spec);
        boolean italic = getBoolean(spec, ITALICS, false);
        boolean bold = getBoolean(spec, BOLD, false);
        float multiplier = getFloat(spec, SIZE, 2.0f);
        int justify = getAlignment(spec);

        String icon = getIconName(spec);

        if (title != null)
        {
            JLabel label = null;
            ImageIcon imgicon = null;
            try
            {
                imgicon = parent.icons.getImageIcon(icon);
                label = LabelFactory.create(title, imgicon, JLabel.TRAILING, true);
            }
            catch (Exception e)
            {
                Debug.trace("Icon " + icon + " not found in icon list. " + e.getMessage());
                label = LabelFactory.create(title);
            }
            Font font = label.getFont();
            float size = font.getSize();
            int style = 0;

            if (bold)
            {
                style += Font.BOLD;
            }
            if (italic)
            {
                style += Font.ITALIC;
            }

            font = font.deriveFont(style, (size * multiplier));
            label.setFont(font);
            label.setAlignmentX(0);
            label.setEnabled(isEnabled);

            TwoColumnConstraints constraints = new TwoColumnConstraints();
            constraints.align = justify;
            constraints.position = TwoColumnConstraints.NORTH;
            // Non-interactive element: shouldn't get focus.
            label.setFocusable(false);

            panel.add(label, constraints);
            addToolTiptoElement(spec, label);


        }
    }

    protected String getIconName(IXMLElement element)
    {
        if (element == null) { return (null); }

        String key = element.getAttribute(ICON_KEY);
        String text = null;
        if ((key != null) && (langpack != null))
        {
            try
            {
                text = langpack.getString(key);
            }
            catch (Throwable exception)
            {
                text = null;
            }
        }

        return (text);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a rule field to the list of UI elements.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the rule field.
     */
    /*--------------------------------------------------------------------------*/
    private void addRuleField(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        String variable = spec.getAttribute(VARIABLE);
        RuleInputField field = null;
        JLabel label;
        String layout;
        String set;
        String separator;
        String format;
        String validator = null;
        String message = null;
        boolean hasParams = false;
        String paramName = null;
        String paramValue = null;
        HashMap<String, String> validateParamMap = null;
        Vector<IXMLElement> validateParams = null;
        String processor = null;
        int resultFormat = RuleInputField.DISPLAY_FORMAT;

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            label = new JLabel(getText(element));
            label.setEnabled(isEnabled);
            layout = element.getAttribute(RULE_LAYOUT);
            set = element.getAttribute(SET);

            // retrieve value of variable if not specified
            // (does not work here because of special format for set attribute)
            // if (set == null)
            // {
            // set = idata.getVariable (variable);
            // }

            separator = element.getAttribute(RULE_SEPARATOR);
            format = element.getAttribute(RULE_RESULT_FORMAT);

            if (format != null)
            {
                if (format.equals(RULE_PLAIN_STRING))
                {
                    resultFormat = RuleInputField.PLAIN_STRING;
                }
                else if (format.equals(RULE_DISPLAY_FORMAT))
                {
                    resultFormat = RuleInputField.DISPLAY_FORMAT;
                }
                else if (format.equals(RULE_SPECIAL_SEPARATOR))
                {
                    resultFormat = RuleInputField.SPECIAL_SEPARATOR;
                }
                else if (format.equals(RULE_ENCRYPTED))
                {
                    resultFormat = RuleInputField.ENCRYPTED;
                }
            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            return;
        }

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        // ----------------------------------------------------
        // get the validator and processor if they are defined
        // ----------------------------------------------------
        // element = spec.getFirstChildNamed(VALIDATOR);
        List<ValidatorContainer> validatorConfig = analyzeValidator(spec);
        element = spec.getFirstChildNamed(PROCESSOR);
        if (element != null)
        {
            processor = element.getAttribute(CLASS);
        }

        // ----------------------------------------------------
        // create an instance of RuleInputField based on the
        // extracted specifications, then add it to the list
        // of UI elements.
        // ----------------------------------------------------
        /*
        if (hasParams)
        {
            field = new RuleInputField(layout, set, separator, validator, validateParamMap,
                    processor, resultFormat, getToolkit(), idata);
        }
        else
        {
            field = new RuleInputField(layout, set, separator, validator, processor, resultFormat,
                    getToolkit(), idata);

        }
        */


       /*
        * dcheung: Before you curse me for that, let me explain to you my
        * intentions:
        *
        * RuleInputField inherits JComponent. Whenever the RuleInputField
        * constructor is called, The JComponent constructor is called first.
        *
        * The JComponent constructor calls the method setFields(). setFields()
        * require the field finalVariable to work properly.
        *
        * What setFields() is doing is create a map of the final variable and
        * the initial variables. I need that so that when you click "Back" on a·
        * custom port panel, it remembers the options you specified for that
        * "previous" panel.
        *
        * Since finalVariable is not initialized before JComponent's
        * constructor is called, I get a NullPointerException.
        *
        * The only way to initialize finalVariable BEFORE the JComponent
        * constructor is called is by setting it to static and assigning a·
        * value to it before the RuleInputField constructor is called.
        */
        RuleInputField.finalVariable = variable;
        field = new RuleInputField(layout, set, separator, validatorConfig, processor, resultFormat,
                getToolkit(), idata);
        field.setEnabled(isEnabled);
        /*
         * testing: set all rule fields with the correct description
         */
        for (JTextComponent jtc : field.getInputFields()){
            AccessibleContext ac = jtc.getAccessibleContext();
            ac.setAccessibleDescription("RuleInputField TextComponent containing part of the variable "+variable);
            addToolTiptoElement(spec, jtc);
        }
        String indentStr = spec.getAttribute(ATTRIBUTE_INDENT);
        boolean indent = (indentStr != null) ? Boolean.parseBoolean(indentStr) : false;

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;
        constraints.indent = getIndent(spec);

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);
        addToolTiptoElement(spec, label);
        parseSummarizeAttribute(spec, labelUiElement);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;
        constraints2.indent = getIndent(spec);

        UIElement ruleField = new UIElement();
        ruleField.setType(UIElementType.RULE);
        ruleField.setConstraints(constraints2);
        ruleField.setComponent(field, parent.hasBackground);
        ruleField.setForPacks(forPacks);
        ruleField.setForOs(forOs);
        ruleField.setAssociatedVariable(variable);
        ruleField.setMessage(message);
        ruleField.setEnabled(isEnabled);
        elements.add(ruleField);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
     */
    public String getSummaryBody()
    {
        Map<String, String> entryMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < summaryEntries.size(); i++)
        {
            TextValuePair pair = summaryEntries.elementAt(i);
            final String key = pair.toString();
            entryMap.put(key, idata.getVariable(key));
        }
        Iterator<String> keys = entryMap.keySet().iterator();
        StringBuilder tmp = new StringBuilder();
        while (keys.hasNext())
        {
            String key = keys.next();
            String value = entryMap.get(key);
            /**
             * if (key.contains("adminPassword") || key.contains("vault.keystorepwd") ||
             * key.contains("ssl.password") || key.contains("overlord.password") ||
             * key.contains("ldap.creds") || key.contains("postinstallServer") ||
             * key.contains("rtgov.password") || key.contains("db.password")) { continue; }
             **/
            String keyname = idata.langpack.getString(key);
            if (keyname.equals("")) {
                continue;
            }
            else if (!value.isEmpty()){
                tmp.append(keyname + ": " + value + "<br>");
            }
        }
        if (tmp.toString().trim().isEmpty()) {
            return null;
        } else {
            return tmp.toString();
        }
    }

    public String getInstanceNumber()
    {
        return Integer.toString(instanceNumber);
    }
    /*--------------------------------------------------------------------------*/
    /**
     * Reads the data from the rule input field and sets the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readRuleField(UIElement field)
    {
        RuleInputField ruleField = null;
        String variable = null;
        String message = null;

        try
        {
            ruleField = (RuleInputField) field.getComponent();
            variable = field.getAssociatedVariable();
            message = field.getMessage();
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (ruleField == null)) { return (true); }

        String validated = ruleField.validateContentsMultiValidators();
        boolean success = !validating || validated.equals("");
        if (!success)
        {
            showWarningMessageDialog(parentFrame, validated);
            return (false);
        }

        FinalAndInitialVariables boo = ruleField.getFinalAndInitialVariables();

        int count = 0;
        for(String temp: boo.getInitialVariables(variable)) {
            temp = temp.substring(temp.indexOf("{") + 1,
                    temp.indexOf("}"));
            idata.setVariable(temp, ruleField.getFieldContents(count));
            ++count;
        }
        String value = ruleField.getText();
        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        if (field.isSummarized())
            summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
        return (true);
    }


    private void addTextField(IXMLElement field) {

        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable  = field.getAttribute(VARIABLE);

        if (variable == null || spec == null) { return; }
        String value = idata.getVariable(variable);

        String  defaultValue = spec.getAttribute(SET, "");
        String textSize      = spec.getAttribute(TEXT_SIZE, "1");
        String textRows      = spec.getAttribute(TEXT_ROWS, "1");

        //String message = null;
        int size =  Integer.parseInt(textSize);
        int rows =  Integer.parseInt(textRows);



        if (value != null )  defaultValue = value;
        else if (!defaultValue.isEmpty()) {

            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            if (Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)))
                defaultValue = vs.deepSubstitute(defaultValue, null);
            else
                defaultValue = vs.substitute(defaultValue, null);

        }

        List<ValidatorContainer> validatorConfig = analyzeValidator(field);

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs    = field.getChildrenNamed(OS);
        IXMLElement descr            = field.getFirstChildNamed(DESCRIPTION);

        addDescription(descr, forPacks, forOs);

        JLabel  label = new JLabel(getText(spec));
        label.setFocusable(false);
        label.setEnabled(isEnabled);

        // construct the UI element and add it to the list
        TextInputField inputField = new TextInputField(defaultValue, size, rows, validatorConfig);
        inputField.addFocusListener(this);
        inputField.setEnabled(isEnabled);

        String indentStr = field.getAttribute(ATTRIBUTE_INDENT, FALSE);
        boolean indent   = Boolean.parseBoolean(indentStr);

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position             = TwoColumnConstraints.WEST;
        constraints.indent               = getIndent(field);

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);

        addToolTiptoElement(field, label);
        parseSummarizeAttribute(spec, labelUiElement);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;
    //  constraints2.indent   = indent;

        //AccessibleContext and ActionCommand setting for marathon automated testing
        AccessibleContext ac = inputField.getField().getAccessibleContext();
        ac.setAccessibleDescription("JTextComponent for setting variable: "+variable);

        UIElement textUiElement = new UIElement();
        textUiElement.setDeepSub(Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE)));
        textUiElement.setDependency(field.getAttribute(DEPENDS_ON, ""));
        textUiElement.setComponent(inputField, parent.hasBackground);
        textUiElement.setType(UIElementType.TEXT);
        textUiElement.setAssociatedVariable(variable);
        textUiElement.setConstraints(constraints2);
        textUiElement.setForPacks(forPacks);
        textUiElement.setForOs(forOs);
        textUiElement.setEnabled(isEnabled);
        //textUiElement.setMessage(message);

        addToolTiptoElement(field, inputField.getField());
        parseSummarizeAttribute(spec, textUiElement);
        if (focusedOnActivate(field)) setInitialFocus(inputField.getField());

        elements.add(labelUiElement);
        elements.add(textUiElement);

    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads data from the text field and sets the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readTextField(UIElement field)
    {
        TextInputField textField = null;
        String variable = null;
        String value = null;
        //String message = null;

        try
        {
            //message = field.getMessage();
            textField = (TextInputField) field.getComponent();
            variable = field.getAssociatedVariable();
            value = textField.getText();
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (value == null)) { return (true); }

        // validate the input
        Debug.trace("Validating text field");
        String validated = textField.validateContentsMultiValidators();
        boolean success = !validating || validated.equals("");
        if (!success)
        {
            showWarningMessageDialog(parentFrame, validated);
            return (false);
        }
        Debug.trace("Field validated");
        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        if (field.isSummarized())
            summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
        return (true);
    }

    //Button is soley meant for validating
    //This button is hackish since it does not really conform to the interfaces of our validators.
    private void addButtonField(IXMLElement data)
    {
        //Extracting Standard Information
        Vector<IXMLElement> forPacks = data.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = data.getChildrenNamed(OS);
        IXMLElement spec = data.getFirstChildNamed(SPEC);
        String variable = data.getAttribute(VARIABLE);

        //Extract validators and its appropriate messages
        //Validator warning messages should be a value of the id attribute that refers to a string in langpacks
        final String successMessage = idata.langpack.getString(spec.getAttribute("msg"));
        final List<ValidatorContainer> validators = analyzeValidator(spec);
        final ArrayList<String> errorMsgs = new ArrayList<String>();
        for (IXMLElement validatorTag : spec.getChildrenNamed("validator")){
            errorMsgs.add(validatorTag.getAttribute("id"));
        }

        //Dummy processor used to adhere to interface.
        final PasswordGroup dummyProcessor = new PasswordGroup(idata, new ArrayList<ValidatorContainer>(), "Dummy");

        //Generate the Test Button
        JButton button = new JButton();
        button.setText(idata.langpack.getString(spec.getAttribute("id", "Missing String")));
        button.setEnabled(isEnabled);

        final boolean useError = Boolean.parseBoolean(spec.getAttribute("useErrors", "true"));

        //Add action listener, logic to call the validate function of the validator.
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean returnValue = true;
                try
                {
                    int i = 0;
                    saveToInstallData();
                    for (ValidatorContainer v : validators){
                        Validator validator = v.getValidator();
                        returnValue = validator.validate(dummyProcessor);
                        if (!returnValue){
                            if (useError){
                                emitError(idata.langpack.getString("Error"), idata.langpack.getString(errorMsgs.get(i)));
                                return;
                            } else {
                                emitWarning(idata.langpack.getString("Warning"), idata.langpack.getString(errorMsgs.get(i)));
                                return;
                            }
                        }
                        i++;
                    }
                    for (UIElement elm: elements){
                        elm.getMessage();
                    }
                }
                catch (Exception exc)
                {
                    Debug.trace("Failed " + exc);
                }
                emitNotification(successMessage);
            }});

        //Set attributes of the button
        String indentStr = data.getAttribute(ATTRIBUTE_INDENT);
        boolean indent = (indentStr != null) ? Boolean.parseBoolean(indentStr) : false;

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;
        constraints2.indent = getIndent(data);

        UIElement buttonUiElement = new UIElement();
        buttonUiElement.setType(UIElementType.BUTTON);
        buttonUiElement.setConstraints(constraints2);
        buttonUiElement.setComponent(button, parent.hasBackground);
        buttonUiElement.setForPacks(forPacks);
        buttonUiElement.setForOs(forOs);
        buttonUiElement.setAssociatedVariable(variable);
        buttonUiElement.setDeepSub((Boolean.parseBoolean(spec.getAttribute(DEEP_SUB, FALSE))));
        buttonUiElement.setDependency(spec.getAttribute(DEPENDS_ON, ""));
        buttonUiElement.setSummarize(false);
        buttonUiElement.setEnabled(isEnabled);
        addToolTiptoElement(data, button);
        elements.add(buttonUiElement);
    }
    /*--------------------------------------------------------------------------*/
    /**
     * Adds a combo box to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     *
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;combo&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the combo box&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot;/&gt;
     *          &lt;choice text=&quot;choice 1&quot; id=&quot;&quot; value=&quot;combo box 1&quot;/&gt;
     *          &lt;choice text=&quot;choice 2&quot; id=&quot;&quot; value=&quot;combo box 2&quot; set=&quot;true&quot;/&gt;
     *          &lt;choice text=&quot;choice 3&quot; id=&quot;&quot; value=&quot;combo box 3&quot;/&gt;
     *          &lt;choice text=&quot;choice 4&quot; id=&quot;&quot; value=&quot;combo box 4&quot;/&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the combo box.
     */
    /*--------------------------------------------------------------------------*/
    private void addComboBox(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        String variable = spec.getAttribute(VARIABLE);
        TextValuePair listItem = null;
        JComboBox field = new JComboBox();

        /*
         * AccessibleContext and ActionCommand setting for marathon automated testing
         */
        field.setActionCommand("JComboBox setting variable: "+variable);
        field.setVisible(isVisible); // For post install summary fix...
        field.setEnabled(isEnabled);

        AccessibleContext ac = field.getAccessibleContext();
        ac.setAccessibleDescription("This JComboBox sets the variable: " +variable);
        JLabel label = new JLabel();
        label.setEnabled(isEnabled);
        String causesValidataion = element.getAttribute(REVALIDATE);
        if (causesValidataion != null && causesValidataion.equals("yes"))
        {
            field.addItemListener(this);
        }
        boolean userinput = false; // is there already user input?
        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            //label = new JLabel(getText(element)); //Do not get description from spec field. Use the <description> tags
            Vector<IXMLElement> choices = element.getChildrenNamed(COMBO_CHOICE);

            if (choices == null) { return; }
            // get current value of associated variable
            String currentvariablevalue = idata.getVariable(variable);
            if (currentvariablevalue != null)
            {
                // there seems to be user input
                userinput = true;
            }
            int choicenum = 0;
            for (int i = 0; i < choices.size(); i++)
            {
                String conditionid = (choices.elementAt(i)).getAttribute(ATTRIBUTE_CONDITIONID_NAME);
                if (conditionid != null)
                {
                    // check if condition is fulfilled
                    if (!this.parent.getRules().isConditionTrue(conditionid, idata.getVariables()))
                    {
                        continue;
                    }
                }
                String processorClass = (choices.elementAt(i)).getAttribute("processor");

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
                    String set = (choices.elementAt(i)).getAttribute(SET);
                    if (set == null)
                    {
                        set = "";
                    }
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        if (Boolean.parseBoolean(spec.getAttribute(DEEP_SUB, FALSE))) {
                            set = vs.deepSubstitute(set,  null);
                        } else {
                            set = vs.substitute(set, null);
                        }
                    }

                    StringTokenizer tokenizer = new StringTokenizer(choiceValues, ":");
                    int counter = 0;
                    while (tokenizer.hasMoreTokens())
                    {
                        String token = tokenizer.nextToken();
                        listItem = new TextValuePair(token, token);
                        field.addItem(listItem);
                        if (set.equals(token))
                        {
                            field.setSelectedIndex(field.getItemCount() - 1);
                        }
                        counter++;
                    }
                }
                else
                {
                    String value = (choices.elementAt(i)).getAttribute(COMBO_VALUE);
                    listItem = new TextValuePair(getText(choices.elementAt(i)), value);
                    field.addItem(listItem);
                    if (userinput)
                    {
                        // is the current value identical to the value associated with this element
                        if ((value != null) && (value.length() > 0)
                                && (currentvariablevalue.equals(value)))
                        {
                            // select it
                            field.setSelectedIndex(choicenum);
                        }
                        // else do nothing
                    }
                    else
                    {
                        // there is no user input
                        String set = (choices.elementAt(i)).getAttribute(SET);
                        if (set != null)
                        {
                            if (set != null && !"".equals(set))
                            {
                                VariableSubstitutor vs = new VariableSubstitutor(idata
                                        .getVariables());
                                if (Boolean.parseBoolean(spec.getAttribute(DEEP_SUB, FALSE))) {
                                    set = vs.deepSubstitute(set, null);
                                } else {
                                    set = vs.substitute(set, null);
                                }
                            }
                            if (set.equals(TRUE))
                            {
                                field.setSelectedIndex(choicenum);
                            }
                        }
                    }
                    choicenum++;
                }

            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            return;
        }

        // REDHART
        // We wan't description and combo box on one line. So we will not use the addDescription method.
        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        //addDescription(element, forPacks, forOs);
        String description = getText(element);
        if (description != null)
        {
            label.setText(description); //Server profile to configure:
            label.setVisible(isVisible); //For post install summary fix
            label.setEnabled(isEnabled);
        }


        //String indentStr = spec.getAttribute(ATTRIBUTE_INDENT);
        //boolean indent = (indentStr != null) ? Boolean.parseBoolean(indentStr) : false;

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.WEST;
        constraints.indent = getIndent(spec);

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(constraints);
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);
        addToolTiptoElement(spec, label);
        parseSummarizeAttribute(spec, labelUiElement);


        // uiElements
        // .add(new Object[] { null, FIELD_LABEL, null, constraints, label, forPacks, forOs});

        TwoColumnConstraints constraints2 = new TwoColumnConstraints();
        constraints2.position = TwoColumnConstraints.EAST;
        //constraints2.indent = getIndent(spec);

        UIElement comboUiElement = new UIElement();
        comboUiElement.setType(UIElementType.COMBOBOX);
        comboUiElement.setConstraints(constraints2);
        comboUiElement.setComponent(field, parent.hasBackground);
        comboUiElement.setForPacks(forPacks);
        comboUiElement.setForOs(forOs);
        comboUiElement.setAssociatedVariable(variable);
        comboUiElement.setDeepSub((Boolean.parseBoolean(spec.getAttribute(DEEP_SUB, FALSE))));
        comboUiElement.setDependency(spec.getAttribute(DEPENDS_ON, ""));
        comboUiElement.setEnabled(isEnabled);

        elements.add(comboUiElement);
        addToolTiptoElement(spec, field);
        parseSummarizeAttribute(spec, comboUiElement);

        // uiElements.add(new Object[] { null, COMBO_FIELD, variable, constraints2, field, forPacks,
        // forOs});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the combobox field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readComboBox(UIElement field)
    {
        String variable;
        String value;
        JComboBox comboBox;

        try
        {
            variable = (String) field.getAssociatedVariable();
            comboBox = (JComboBox) field.getComponent();
            value = ((TextValuePair) comboBox.getSelectedItem()).getValue();
        }
        catch (Throwable exception)
        {
            return true;
        }
        if ((variable == null) || (value == null)) { return true; }

        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        if (field.isSummarized())
            summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
        return true;
    }


    //Adds a radio button set to the list of UI elements
    private void addRadioButton(IXMLElement field) {

        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable  = field.getAttribute(VARIABLE);

        if (spec == null || variable == null) return;
        String value = idata.getVariable(variable);

        boolean first = true;

        Vector<IXMLElement> choices = spec.getChildrenNamed(RADIO_CHOICE);
        if (choices == null) { return; }

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs    = field.getChildrenNamed(OS);

        IXMLElement descr = field.getFirstChildNamed(DESCRIPTION);
        addDescription(descr, forPacks, forOs);


        ButtonGroup group = new ButtonGroup();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.indent = getIndent(field);
        constraints.stretch = true;

        for (IXMLElement choice : choices ) {

            String conditionid = choice.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            if (conditionid != null && !this.parent.getRules().isConditionTrue(conditionid, idata.getVariables()))
                continue;


            String trueValue    = choice.getAttribute(RADIO_VALUE);
            String defaultValue = choice.getAttribute(SET, FALSE);

            JRadioButton radioButton = new JRadioButton();
            radioButton.setText(getText(choice));
            radioButton.setVisible(isVisible);
            radioButton.setEnabled(isEnabled);

            String causesValidation = (choice).getAttribute(REVALIDATE);
            if (causesValidation != null && causesValidation.equals(YES)) radioButton.addActionListener(this);

            // AccessibleContext and ActionCommand setting for marathon automated testing
            AccessibleContext ac = radioButton.getAccessibleContext();
            ac.setAccessibleDescription("If this button is selected, " + variable + " will be set to " + trueValue);
            radioButton.setActionCommand("JRadioButton setting variable: " + variable + " = " + trueValue);


            // in order to properly initialize dependent controls
            // we must set this variable now
            if (defaultValue != null && !defaultValue.isEmpty()) {
                if (Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE))) defaultValue = vs.deepSubstitute(defaultValue, null);
                else  defaultValue = vs.substitute(defaultValue, null);
            }

            //Set based on variable, if not set, then set based on set attritbute
            if (value != null && value.equals(trueValue)) radioButton.setSelected(true);
            else if (value == null && defaultValue.equals(TRUE)) {
                idata.setVariable(variable, trueValue);
                radioButton.setSelected(true);
            }

            RadioButtonUIElement radioUiElement = new RadioButtonUIElement();
            radioUiElement.setType(UIElementType.RADIOBUTTON);
            radioUiElement.setComponent(radioButton, parent.hasBackground);
            radioUiElement.setDeepSub((Boolean.parseBoolean(field.getAttribute(DEEP_SUB, FALSE))));
            radioUiElement.setForOs(forOs);
            radioUiElement.setButtonGroup(group);
            radioUiElement.setForPacks(forPacks);
            radioUiElement.setTrueValue(trueValue);
            radioUiElement.setConstraints(constraints);
            radioUiElement.setAssociatedVariable(variable);
            radioUiElement.setEnabled(isEnabled);


            if (first && focusedOnActivate(field)) {
                setInitialFocus(radioUiElement.getComponent());
                first = false;
            }

            addToolTiptoElement(choice, radioButton);
            parseSummarizeAttribute(field, radioUiElement);

            group.add(radioButton);
            buttonGroups.add(group);
            elements.add(radioUiElement);

        } //Choice loop

    } //addRadioButton

    private int getIndent(IXMLElement field) {
        int value = 0;
        if (spec != null) {
            try {
                value = field.getAttribute(ATTRIBUTE_INDENT) != null ? Integer.parseInt(field.getAttribute(ATTRIBUTE_INDENT)) : 0;
            } catch (NumberFormatException e) {
                value = 0;
            }
        }
        return value;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the radio button field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readRadioButton(UIElement field)
    {
        String variable = null;
        String value = null;
        JRadioButton button = null;

        try
        {
            button = (JRadioButton) field.getComponent();

            if (!button.isSelected()) { return (true); }

            variable = field.getAssociatedVariable();
            value = field.getTrueValue();
        }
        catch (Throwable exception)
        {
            return (true);
        }

        idata.setVariable(variable, value);

        /**
         * Temporarily hacky way to skip adding values to the summary panel
         * entries.
         * TODO: Implement a new XML attribute that allows us to specify
         * if we want to add the value of any user input field to summary.
         */
        // if (!variable.contains("addUser") && !variable.contains("password")) {
        entries.add(new TextValuePair(variable, value));
        if (field.isSummarized())
            summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
        // }
        return (true);
    }


    //Adds one or more password fields to the list of UI elements.
    //Really should always add exactly two fields ....
    private void addPasswordField(IXMLElement field) {
        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable = field.getAttribute(VARIABLE);
        if (variable == null || spec == null ) return;
        String value = idata.getVariable(variable);
        IXMLElement description      = field.getFirstChildNamed(DESCRIPTION);
        IXMLElement processorElem    = field.getFirstChildNamed(PROCESSOR);
        Vector<IXMLElement> forOs    = field.getChildrenNamed(OS);
        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);

        List<ValidatorContainer> validatorsList    = analyzeValidator(field);
        if (validatorsList == null) validatorsList = new ArrayList<ValidatorContainer>();

        String processor = null;
        if (processorElem != null) processor = processorElem.getAttribute(CLASS);

        String prompt      = field.getAttribute(AUTOPROMPT, FALSE);
        boolean autoPrompt = Boolean.parseBoolean(prompt);

        addDescription(description, forPacks, forOs);
        PasswordGroup group = new PasswordGroup(idata, validatorsList, processor);

        if (spec != null) {

            VariableSubstitutor vs     = new VariableSubstitutor(idata.getVariables());
            Vector<IXMLElement> inputs = spec.getChildrenNamed(PWD_INPUT);
            if (inputs == null) return;

            int     size  = 0;
            boolean first = true;

            // process each input field
            for (IXMLElement passSpec : inputs) {

                JLabel label = new JLabel(getText(passSpec));
                label.setEnabled(isEnabled);

                String id = passSpec.getAttribute(ID);
                String set = passSpec.getAttribute(SET);
                if (set != null && !set.isEmpty())  set = vs.substitute(set, null);

                try {
                    size = Integer.parseInt(passSpec.getAttribute(PWD_SIZE, "1"));
                } catch (Throwable exception) { size = 1; }


                // construct the UI element and add it to the list
                JPasswordField passwordField = new JPasswordField(set, size);
                passwordField.setVisible(isVisible);
                passwordField.setEnabled(isEnabled);
                passwordField.setEditable(isEnabled);
                passwordField.setCaretPosition(0);

                //AccessibleContext and ActionCommand setting for marathon automated testing
                AccessibleContext ac = passwordField.getAccessibleContext();
                ac.setAccessibleDescription("This JPasswordField contains the value for variable: "+variable);
                passwordField.setActionCommand("Password field to set variable: "+variable);

                //String indentStr = field.getAttribute(ATTRIBUTE_INDENT, FALSE);
                //int indent   = getIndent(spec);

                TwoColumnConstraints constraints = new TwoColumnConstraints();
                constraints.position = TwoColumnConstraints.WEST;
                constraints.indent = getIndent(field);

                TwoColumnConstraints constraints2 = new TwoColumnConstraints();
                constraints2.position = TwoColumnConstraints.EAST;
                constraints2.indent = getIndent(spec);


                UIElement labelUiElement = new UIElement();
                labelUiElement.setComponent(label, parent.hasBackground);
                labelUiElement.setType(UIElementType.LABEL);
                labelUiElement.setConstraints(constraints);
                labelUiElement.setForPacks(forPacks);
                labelUiElement.setForOs(forOs);

                PasswordUIElement passwordUiElement = new PasswordUIElement();
                passwordUiElement.setComponent(passwordField, false);
                passwordUiElement.setType(UIElementType.PASSWORD);
                passwordUiElement.setAssociatedVariable(variable);
                passwordUiElement.setConstraints(constraints2);
                passwordUiElement.setAutoPrompt(autoPrompt);
                passwordUiElement.setPasswordGroup(group);
                passwordUiElement.setForPacks(forPacks);
                passwordUiElement.setForOs(forOs);
                passwordUiElement.setId(id);
                passwordUiElement.setEnabled(isEnabled);
                addToolTiptoElement(field, label);
                addToolTiptoElement(field, passwordField);
                parseSummarizeAttribute(field, passwordUiElement);

                elements.add(labelUiElement);
                elements.add(passwordUiElement);

                //If its the revalidation string retrieve based on what was left on the field
                //If its the "first" string, retrive based on variable and if null default to default
                if (!first && passwords.containsKey(id)) passwordField.setText(passwords.get(id));
                else if (!first ) passwordField.setText(set);
                else if ( first ) {
                    if (focusedOnActivate(field))
                    {
                        setInitialFocus(passwordUiElement.getComponent());
                    }
                    first = false;
                    if (value == null) passwordField.setText(set);
                    else passwordField.setText(value);
                }


                group.addField(passwordField);
                group.addNamedField(id, passwordField);
            }
        }

        passwordGroups.add(group);

    } //addPassword

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the password field and substitutes the associated variable.
     *
     * @param field a password group that manages one or more passord fields.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readPasswordField(UIElement field)
    {
        PasswordUIElement pwdField = (PasswordUIElement) field;

        boolean autoPrompt = pwdField.getAutoPrompt();
        PasswordGroup group = null;
        String variable = null;

        try
        {
            group = (PasswordGroup) pwdField.getPasswordGroup();
            variable = field.getAssociatedVariable();
            // Removed to support grabbing the message from multiple validators
            // message = (String) field[POS_MESSAGE];
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (passwordGroupsRead.contains(group))) { return (true); }
        passwordGroups.add(group);

        int size = group.validatorSize();
        boolean success = !validating || size < 1;

        // Use each validator to validate contents
        if (!success)
        {
            //System.out.println("Found "+(size)+" validators");
            for (int i = 0; i < size; i++)
            {
                success = group.validateContents(i);
                if (!success)
                {
                    JOptionPane.showMessageDialog(parentFrame, group.getValidatorMessage(i),
                            parentFrame.langpack.getString("UserInputPanel.error.caption"),
                            JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }

        if (success)
        {
            String value = group.getPassword();
            idata.setVariable(variable, value);
            TextValuePair pair;
            if (autoPrompt){
                pair = new TextValuePair(variable, null);
            } else {
                pair = new TextValuePair(variable, value);
            }
            entries.add(pair);
            if (field.isSummarized())
                summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
        }
        return success;
    }


    //Checkboxes default value or 'set' value should be "true", "false", or some condition variable
    //Default value will only be put into effect if the associated variable has not been set
    //Otherwise assume that your checkbox wil be set/unset based on associated variable
    private void addCheckBox(IXMLElement field) {

        IXMLElement spec = field.getFirstChildNamed(SPEC);
        String variable = field.getAttribute(VARIABLE);

        if (variable == null) { return; }
        String value = idata.getVariable(variable);

        String label             = "";
        String causesValidataion = NO;
        String trueValue         = TRUE;
        String falseValue        = FALSE;
        String defaultSetting    = FALSE;
        boolean defaultValue     = false;
        boolean set              = false;



        if (spec != null) {

            label             = getText(spec);
            trueValue         = spec.getAttribute(TRUE);
            falseValue        = spec.getAttribute(FALSE);
            causesValidataion = spec.getAttribute(REVALIDATE, NO);
            defaultSetting    = spec.getAttribute(SET, FALSE);

            if      (defaultSetting.equals(FALSE)) defaultValue = false;
            else if (defaultSetting.equals(TRUE))  defaultValue = true;
            else                                   defaultValue = idata.getRules().isConditionTrue(defaultSetting);

            if (value == null) set = defaultValue;
            else if (value.equals(trueValue))  set = true;
            else if (value.equals(falseValue)) set= false;
            else                               set = Boolean.parseBoolean(value);

            Debug.trace("CheckBox" +
                    "\n============" +
                    "\nValue of associated variable: " + value +
                    "\nDefault Value: " + defaultValue +
                    "\nCheckbox should be set to: " + set);

        }

        Vector<IXMLElement> forPacks = field.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs    = field.getChildrenNamed(OS);
        IXMLElement descr            = field.getFirstChildNamed(DESCRIPTION);
        //String indentStr             = field.getAttribute(ATTRIBUTE_INDENT, "0");
        String dependencyVar         = field.getAttribute(DEPENDS_ON, "");

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.indent               = getIndent(field);
        constraints.position             = TwoColumnConstraints.BOTH;
        constraints.stretch              = true;

        addDescription(descr, forPacks, forOs);

        //Keep track of change in dependent variable
        //If dependent variable has change ensure to reset the checkbox to its default value
        if (!dependencyVar.isEmpty()) {
            String dependecyVal =  idata.getVariable(dependencyVar);
            if(!dependencies.containsKey(dependencyVar)) {
                dependencies.put(dependencyVar, dependecyVal);
                set = defaultValue;
            }
            else if (!dependencies.get(dependencyVar).equals(dependecyVal)){
                dependencies.put(dependencyVar, idata.getVariable(dependencyVar));
                set = defaultValue;
            }
        }

        JCheckBox checkbox = new JCheckBox(label);
        checkbox.setVisible(isVisible);
        checkbox.setSelected(set);
        checkbox.setEnabled(isEnabled);

        addToolTiptoElement(field, checkbox);
        if (causesValidataion.equals(YES)) checkbox.addActionListener(this);

        //AccessibleContext and ActionCommand setting for marathon automated testing
        AccessibleContext ac = checkbox.getAccessibleContext();
        ac.setAccessibleDescription("This checkbox will set "+variable+" to "+trueValue+" if checked, or to "+falseValue+" if unchecked.");
        checkbox.setActionCommand("JCheckBox to set: " + variable + " = " + trueValue);

        UIElement checkboxUiElement = new UIElement();
        checkboxUiElement.setType(UIElementType.CHECKBOX);
        checkboxUiElement.setComponent(checkbox, parent.hasBackground);
        checkboxUiElement.setForOs(forOs);
        checkboxUiElement.setForPacks(forPacks);
        checkboxUiElement.setTrueValue(trueValue);
        checkboxUiElement.setFalseValue(falseValue);
        checkboxUiElement.setConstraints(constraints);
        checkboxUiElement.setAssociatedVariable(variable);
        checkboxUiElement.setEnabled(isEnabled);
        if (focusedOnActivate(field)) setInitialFocus(checkboxUiElement.getComponent());

        if (set) idata.setVariable(variable, trueValue);
        else     idata.setVariable(variable, falseValue);
        elements.add(checkboxUiElement);

    } //Add Checkbox

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the checkbox field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readCheckBox(UIElement field)
    {
        String variable = null;
        String trueValue = null;
        String falseValue = null;
        JCheckBox box = null;

        try
        {
            box = (JCheckBox) field.getComponent();
            variable = field.getAssociatedVariable();
            trueValue = field.getTrueValue();
            if (trueValue == null)
            {
                trueValue = "";
            }

            falseValue = field.getFalseValue();
            if (falseValue == null)
            {
                falseValue = "";
            }
        }
        catch (Throwable exception)
        {
            Debug.trace("readCheckBox(): failed: " + exception);
            return (true);
        }

        if (box.isSelected())
        {
            Debug.trace("readCheckBox(): selected, setting " + variable + " to " + trueValue);
            idata.setVariable(variable, trueValue);
            entries.add(new TextValuePair(variable, trueValue));
            if (field.isSummarized())
                summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), trueValue));
        }
        else
        {
            Debug.trace("readCheckBox(): not selected, setting " + variable + " to " + falseValue);
            idata.setVariable(variable, falseValue);
            entries.add(new TextValuePair(variable, falseValue));
            if (field.isSummarized())
                summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), falseValue));
        }

        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a search field to the list of UI elements.
     * <p/>
     * This is a complete example of a valid XML specification
     * <p/>
     *
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;search&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the search field&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot; filename=&quot;the_file_to_search&quot; result=&quot;directory&quot; /&gt; &lt;!-- values for result: directory, file --&gt;
     *          &lt;choice dir=&quot;directory1&quot; set=&quot;true&quot; /&gt; &lt;!-- default value --&gt;
     *          &lt;choice dir=&quot;dir2&quot; /&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the search field
     */
    /*--------------------------------------------------------------------------*/
    private void addSearch(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        IXMLElement element = spec.getFirstChildNamed(SPEC);
        String variable = spec.getAttribute(VARIABLE);
        String filename = null;
        String check_filename = null;
        int search_type = 0;
        int result_type = 0;
        JComboBox combobox = new JComboBox();
        JLabel label = null;

        //System.out.println ("adding search combobox, variable "+variable);

        // allow the user to enter something
        combobox.setEditable(isEnabled);

        // ----------------------------------------------------
        // extract the specification details
        // ----------------------------------------------------
        if (element != null)
        {
            label = new JLabel(getText(element));
            label.setEnabled(isEnabled);
            // search type is optional (default: file)
            search_type = SearchField.TYPE_FILE;

            String search_type_str = element.getAttribute(SEARCH_TYPE);

            if (search_type_str != null)
            {
                if (search_type_str.equals(SEARCH_FILE))
                {
                    search_type = SearchField.TYPE_FILE;
                }
                else if (search_type_str.equals(SEARCH_DIRECTORY))
                {
                    search_type = SearchField.TYPE_DIRECTORY;
                }
            }

            // result type is mandatory too
            String result_type_str = element.getAttribute(SEARCH_RESULT);

            if (result_type_str == null)
            {
                return;
            }
            else if (result_type_str.equals(SEARCH_FILE))
            {
                result_type = SearchField.RESULT_FILE;
            }
            else if (result_type_str.equals(SEARCH_DIRECTORY))
            {
                result_type = SearchField.RESULT_DIRECTORY;
            }
            else if (result_type_str.equals(SEARCH_PARENTDIR))
            {
                result_type = SearchField.RESULT_PARENTDIR;
            }
            else
            {
                return;
            }

            // might be missing - null is okay
            filename = element.getAttribute(SEARCH_FILENAME);

            check_filename = element.getAttribute(SEARCH_CHECKFILENAME);

            Vector<IXMLElement> choices = element.getChildrenNamed(SEARCH_CHOICE);

            if (choices == null) { return; }

            for (int i = 0; i < choices.size(); i++)
            {
                IXMLElement choice_el = choices.elementAt(i);

                if (!OsConstraint.oneMatchesCurrentSystem(choice_el))
                {
                    continue;
                }

                String value = choice_el.getAttribute(SEARCH_VALUE);

                combobox.addItem(value);

                String set = (choices.elementAt(i)).getAttribute(SET);
                if (set != null)
                {
                    if (set != null && !"".equals(set))
                    {
                        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                        if (Boolean.parseBoolean(spec.getAttribute(DEEP_SUB, FALSE))) {
                            set = vs.deepSubstitute(set,  null);
                        } else {
                            set = vs.substitute(set, null);
                        }
                    }
                    if (set.equals(TRUE))
                    {
                        combobox.setSelectedIndex(i);
                    }
                }
            }
        }
        // ----------------------------------------------------
        // if there is no specification element, return without
        // doing anything.
        // ----------------------------------------------------
        else
        {
            return;
        }

        // ----------------------------------------------------
        // get the description and add it to the list of UI
        // elements if it exists.
        // ----------------------------------------------------
        element = spec.getFirstChildNamed(DESCRIPTION);
        addDescription(element, forPacks, forOs);

        String indentStr = spec.getAttribute(ATTRIBUTE_INDENT);
        boolean indent =  (indentStr != null) ? Boolean.parseBoolean(indentStr) : false;

        TwoColumnConstraints westconstraint1 = new TwoColumnConstraints();
        westconstraint1.position = TwoColumnConstraints.WEST;
        westconstraint1.indent = getIndent(spec);

        UIElement labelUiElement = new UIElement();
        labelUiElement.setType(UIElementType.LABEL);
        labelUiElement.setConstraints(westconstraint1);
        labelUiElement.setComponent(label, parent.hasBackground);
        labelUiElement.setForPacks(forPacks);
        labelUiElement.setForOs(forOs);
        elements.add(labelUiElement);
        addToolTiptoElement(spec, label);

        // uiElements.add(new Object[] { null, FIELD_LABEL, null, westconstraint1, label, forPacks,
        // forOs});

        TwoColumnConstraints eastconstraint1 = new TwoColumnConstraints();
        eastconstraint1.position = TwoColumnConstraints.EAST;
        eastconstraint1.indent = getIndent(spec);

        StringBuffer tooltiptext = new StringBuffer();

        if ((filename != null) && (filename.length() > 0))
        {
            tooltiptext.append(MessageFormat.format(parentFrame.langpack
                    .getString("UserInputPanel.search.location"),
                    new Object[] { new String[] { filename}}));
        }

        boolean showAutodetect = (check_filename != null) && (check_filename.length() > 0);
        if (showAutodetect)
        {
            tooltiptext.append(MessageFormat.format(parentFrame.langpack
                    .getString("UserInputPanel.search.location.checkedfile"),
                    new Object[] { new String[] { check_filename}}));
        }

        if (tooltiptext.length() > 0)
        {
            combobox.setToolTipText(tooltiptext.toString());
        }

        UIElement searchUiElement = new UIElement();
        searchUiElement.setType(UIElementType.SEARCH);
        searchUiElement.setConstraints(eastconstraint1);
        searchUiElement.setComponent(combobox, parent.hasBackground);
        searchUiElement.setForPacks(forPacks);
        searchUiElement.setForOs(forOs);
        searchUiElement.setAssociatedVariable(variable);
        searchUiElement.setDeepSub((Boolean.parseBoolean(spec.getAttribute(DEEP_SUB, FALSE))));
        searchUiElement.setDependency(spec.getAttribute(DEPENDS_ON, ""));
        searchUiElement.setEnabled(isEnabled);
        elements.add(searchUiElement);
        addToolTiptoElement(spec, combobox);
        // uiElements.add(new Object[] { null, SEARCH_FIELD, variable, eastconstraint1, combobox,
        // forPacks, forOs});

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new com.izforge.izpack.gui.FlowLayout(
                com.izforge.izpack.gui.FlowLayout.LEADING));

        JButton autodetectButton = ButtonFactory.createButton(parentFrame.langpack
                .getString("UserInputPanel.search.autodetect"), idata.buttonsHColor);
        autodetectButton.setVisible(showAutodetect);

        autodetectButton.setToolTipText(parentFrame.langpack
                .getString("UserInputPanel.search.autodetect.tooltip"));

        buttonPanel.add(autodetectButton);

        JButton browseButton = ButtonFactory.createButton(parentFrame.langpack
                .getString("UserInputPanel.search.browse"), idata.buttonsHColor);

        buttonPanel.add(browseButton);

        TwoColumnConstraints eastonlyconstraint = new TwoColumnConstraints();
        eastonlyconstraint.position = TwoColumnConstraints.EASTONLY;
        eastonlyconstraint.indent = getIndent(spec);


        UIElement searchbuttonUiElement = new UIElement();
        searchbuttonUiElement.setType(UIElementType.SEARCHBUTTON);
        searchbuttonUiElement.setConstraints(eastonlyconstraint);
        searchbuttonUiElement.setComponent(buttonPanel, parent.hasBackground);
        searchbuttonUiElement.setForPacks(forPacks);
        searchbuttonUiElement.setForOs(forOs);
        elements.add(searchbuttonUiElement);
        addToolTiptoElement(spec, buttonPanel);
        // uiElements.add(new Object[] { null, SEARCH_BUTTON_FIELD, null, eastonlyconstraint,
        // buttonPanel, forPacks, forOs});

        searchFields.add(new SearchField(filename, check_filename, parentFrame, combobox,
                autodetectButton, browseButton, search_type, result_type));
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Reads the content of the search field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the data or if there was an
     * irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     * dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readSearch(UIElement field)
    {
        String variable = null;
        String value = null;
        JComboBox comboBox = null;

        try
        {
            variable = field.getAssociatedVariable();
            comboBox = (JComboBox) field.getComponent();
            for (int i = 0; i < this.searchFields.size(); ++i)
            {
                SearchField sf = this.searchFields.elementAt(i);
                if (sf.belongsTo(comboBox))
                {
                    value = sf.getResult();
                    break;
                }
            }
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (value == null)) { return (true); }

        idata.setVariable(variable, value);
        entries.add(new TextValuePair(variable, value));
        if (field.isSummarized())
            summaryEntries.add(new TextValuePair(field.getAssociatedVariable(), value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds text to the list of UI elements
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the text.
     */
    /*--------------------------------------------------------------------------*/
    private void addText(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        addDescription(spec, forPacks, forOs);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a dummy field to the list of UI elements to act as spacer.
     *
     * @param spec a <code>IXMLElement</code> containing other specifications. At present this
     * information is not used but might be in future versions.
     */
    /*--------------------------------------------------------------------------*/
    private void addSpace(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        JPanel panel = new JPanel();

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;
        // Non-interactive element: shouldn't get focus.
        panel.setFocusable(false);

        UIElement spaceUiElement = new UIElement();
        spaceUiElement.setType(UIElementType.SPACE);
        spaceUiElement.setConstraints(constraints);
        spaceUiElement.setComponent(panel, parent.hasBackground);
        spaceUiElement.setForPacks(forPacks);
        spaceUiElement.setForOs(forOs);
        elements.add(spaceUiElement);

        // uiElements
        // .add(new Object[] { null, SPACE_FIELD, null, constraints, panel, forPacks, forOs});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a dividing line to the list of UI elements act as separator.
     *
     * @param spec a <code>IXMLElement</code> containing additional specifications.
     */
    /*--------------------------------------------------------------------------*/
    private void addDivider(IXMLElement spec)
    {
        Vector<IXMLElement> forPacks = spec.getChildrenNamed(SELECTEDPACKS);
        Vector<IXMLElement> forOs = spec.getChildrenNamed(OS);
        JPanel panel = new JPanel();
        String alignment = spec.getAttribute(ALIGNMENT);

        if (alignment != null)
        {
            if (alignment.equals(TOP))
            {
                panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
            }
            else
            {
                panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
            }
        }
        else
        {
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        }

        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;

        UIElement dividerUiElement = new UIElement();
        dividerUiElement.setType(UIElementType.DIVIDER);
        dividerUiElement.setConstraints(constraints);
        dividerUiElement.setComponent(panel, parent.hasBackground);
        dividerUiElement.setForPacks(forPacks);
        dividerUiElement.setForOs(forOs);
        elements.add(dividerUiElement);

        // uiElements.add(new Object[] { null, DIVIDER_FIELD, null, constraints, panel, forPacks,
        // forOs});
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Adds a description to the list of UI elements.
     *
     * @param spec a <code>IXMLElement</code> containing the specification for the description.
     */
    /*--------------------------------------------------------------------------*/
    private void addDescription(IXMLElement spec, Vector<IXMLElement> forPacks,
                                Vector<IXMLElement> forOs)
    {
        String description;
        TwoColumnConstraints constraints = new TwoColumnConstraints();
        constraints.position = TwoColumnConstraints.BOTH;
        constraints.stretch = true;

        if (spec != null)
        {
            constraints.indent = getIndent(spec);
            description = getText(spec);

            // if we have a description, add it to the UI elements
            if (description != null)
            {
                javax.swing.JTextPane label = new javax.swing.JTextPane();

                // Not editable, but still selectable.
                label.setEditable(false);

                // If html tags are present enable html rendering, otherwise the JTextPane
                // looks exactly like MultiLineLabel.
                if (description.startsWith("<html>") && description.endsWith("</html>"))
                {
                    label.setContentType("text/html");
                    label.addHyperlinkListener(new HyperlinkHandler());
                }
                label.setText(description);

                // Background color and font to match the label's.
                label.setBackground(javax.swing.UIManager.getColor("label.backgroud"));
                label.setMargin(new java.awt.Insets(3, 0, 3, 0));
                // workaround to cut out layout problems
                label.getPreferredSize();
                // end of workaround.
                // Non-interactive element: shouldn't get focus.
                label.setFocusable(false);
                label.setEnabled(isEnabled);

                boolean isBold = Boolean.parseBoolean(spec.getAttribute(ATTRIBUTE_TEXT_BOLD));
                if (isBold){
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                }
                UIElement descUiElement = new UIElement();
                descUiElement.setType(UIElementType.DESCRIPTION);
                descUiElement.setConstraints(constraints);
                descUiElement.setComponent(label, parent.hasBackground);
                descUiElement.setForPacks(forPacks);
                descUiElement.setForOs(forOs);
                elements.add(descUiElement);
                addToolTiptoElement(spec, label);
                // uiElements.add(new Object[] { null, DESCRIPTION, null, constraints, label,
                // forPacks, forOs});
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Retrieves the value of a boolean attribute. If the attribute is found and the values equals
     * the value of the constant <code>TRUE</code> then true is returned. If it equals
     * <code>FALSE</code> the false is returned. In all other cases, including when the attribute is
     * not found, the default value is returned.
     *
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use if the attribute does not exist or a illegal
     * value was discovered.
     * @return <code>true</code> if the attribute is found and the value equals the the constant
     * <code>TRUE</code>. <<code> if the
     *         attribute is <code>FALSE</code>. In all other cases the default value is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean getBoolean(IXMLElement element, String attribute, boolean defaultValue)
    {
        boolean result = defaultValue;

        if ((attribute != null) && (attribute.length() > 0))
        {
            String value = element.getAttribute(attribute);

            if (value != null)
            {
                if (value.equals(TRUE))
                {
                    result = true;
                }
                else if (value.equals(FALSE))
                {
                    result = false;
                }
            }
        }

        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Retrieves the value of an integer attribute. If the attribute is not found or the value is
     * non-numeric then the default value is returned.
     *
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     * @return the value of the attribute. If the attribute is not found or the content is not a
     * legal integer, then the default value is returned.
     */
    /*--------------------------------------------------------------------------*/
    // private int getInt(IXMLElement element, String attribute, int defaultValue)
    // {
    // int result = defaultValue;
    //
    // if ((attribute != null) && (attribute.length() > 0))
    // {
    // try
    // {
    // result = Integer.parseInt(element.getAttribute(attribute));
    // }
    // catch (Throwable exception)
    // {}
    // }
    //
    // return (result);
    // }
    /*--------------------------------------------------------------------------*/
    /**
     * Retrieves the value of a floating point attribute. If the attribute is not found or the value
     * is non-numeric then the default value is returned.
     *
     * @param element the <code>IXMLElement</code> to search for the attribute.
     * @param attribute the attribute to search for
     * @param defaultValue the default value to use in case the attribute does not exist.
     *
     * @return the value of the attribute. If the attribute is not found or the content is not a
     * legal integer, then the default value is returned.
     */
    /*--------------------------------------------------------------------------*/
    private float getFloat(IXMLElement element, String attribute, float defaultValue)
    {
        float result = defaultValue;

        if ((attribute != null) && (attribute.length() > 0))
        {
            try
            {
                result = Float.parseFloat(element.getAttribute(attribute));
            }
            catch (Throwable exception)
            {}
        }

        return (result);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Extracts the text from an <code>IXMLElement</code>. The text must be defined in the resource
     * file under the key defined in the <code>id</code> attribute or as value of the attribute
     * <code>txt</code>.
     *
     * @param element the <code>IXMLElement</code> from which to extract the text.
     * @return The text defined in the <code>IXMLElement</code>. If no text can be located,
     * <code>null</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private String getText(IXMLElement element)
    {
        if (element == null) { return (null); }

        String key = element.getAttribute(ID);
        String text = element.getAttribute(TEXT);
        String templateUsed = element.getAttribute(TEMPLATE);
        boolean deepSub = Boolean.parseBoolean(element.getAttribute(DEEP_SUB, FALSE));

        if (templateUsed != null && Boolean.valueOf(templateUsed)) {
            String[] j = key.split("\\.");
            String i = j[j.length - 2];
            if (key.endsWith(MULTICAST_ADDRESS)) {
                text = multicastAddress.replaceAll(regexTemplate, i);
            } else if (key.endsWith(MULTICAST_PORT)) {
                text = multicastPort.replaceAll(regexTemplate, i);
            } else if (key.endsWith(PORT)) {
                text = portText.replaceAll(regexTemplate, i);
            } else if (key.endsWith(MULTICAST_ADDRESS_VALIDATOR)) {
                i = j[j.length - 3];
                text = validatorMulticastAddressText.replaceAll(regexTemplate, i);
            } else if (key.endsWith(MULTICAST_PORT_VALIDATOR)) {
                i = j[j.length - 3];
                text = validatorMulticastPortText.replaceAll(regexTemplate, i);
            } else if (key.endsWith(MULTICAST_PORT_RANGE_VALIDATOR)) {
                i = j[j.length -3];
                text = validatorMulticastRangeText.replaceAll(regexTemplate, i);
            } else if (key.endsWith(ADDRESS_VALIDATOR)) {
                i = j[j.length - 3]; text = validatorVerifyAddressText.replaceAll(regexTemplate, i);
            } else if (key.equals(offsetValidator)) {
                text = offsetText.replaceAll(regexTemplate,
                        AutomatedInstallData.getInstance().getVariable(portOffsetVariable));
            } else if (key.endsWith(RANGE_VALIDATOR)) {
                text = validatorRangeText.replaceAll(regexTemplate, i);
            } else if (key.endsWith(COLLISION_VALIDATOR)) {
                text = validatorCollisionText.replaceAll(regexTemplate, i);
            } else if (key.endsWith(VALIDATOR)) {
                text = validatorPortText.replaceAll(regexTemplate, i);
            } else {
                i = j[j.length -1];
                text = portText.replaceAll(regexTemplate, i);
            }
        }
        else if ((key != null) && (langpack != null)) {

            text = langpack.getString(key);
            if (text == null) text = key;

        }

        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        if (deepSub) return (vs.deepSubstitute(text, null));
        else         return (vs.substitute(text, null));

    }

    /*--------------------------------------------------------------------------*/
    /**
     * Retreives the alignment setting for the <code>IXMLElement</code>. The default value in case
     * the <code>ALIGNMENT</code> attribute is not found or the value is illegal is
     * <code>TwoColumnConstraints.LEFT</code>.
     *
     * @param element the <code>IXMLElement</code> from which to extract the alignment setting.
     * @return the alignement setting for the <code>IXMLElement</code>. The value is either
     * <code>TwoColumnConstraints.LEFT</code>, <code>TwoColumnConstraints.CENTER</code> or
     * <code>TwoColumnConstraints.RIGHT</code>.
     * @see com.izforge.izpack.gui.TwoColumnConstraints
     */
    /*--------------------------------------------------------------------------*/
    private int getAlignment(IXMLElement element)
    {
        int result = TwoColumnConstraints.LEFT;

        String value = element.getAttribute(ALIGNMENT);

        if (value != null)
        {
            if (value.equals(LEFT))
            {
                result = TwoColumnConstraints.LEFT;
            }
            else if (value.equals(CENTER))
            {
                result = TwoColumnConstraints.CENTER;
            }
            else if (value.equals(RIGHT))
            {
                result = TwoColumnConstraints.RIGHT;
            }
        }

        return (result);
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
    private boolean itemRequiredFor(Vector<IXMLElement> packs)
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

    /*--------------------------------------------------------------------------*/
    /**
     * Verifies if an item is required for any of the packs listed. An item is required for a pack
     * in the list if that pack is actually NOT selected for installation. <br>
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
    private boolean itemRequiredForUnselected(Vector<IXMLElement> packs)
    {

        String selected;
        String required;

        if (packs.size() == 0) { return (true); }

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
                if (selected.equals(required)) { return (false); }
            }
        }

        return (true);
    }

    // ----------- Inheritance stuff -----------------------------------------
    /**
     * Returns the uiElements.
     *
     * @return Returns the uiElements.
     */
    // protected Vector<Object[]> getUiElements()
    // {
    // return uiElements;
    // }
    // --------------------------------------------------------------------------
    // Inner Classes
    // --------------------------------------------------------------------------
    /*---------------------------------------------------------------------------*/

    /**
     * This class can be used to associate a text string and a (text) value.
     */
    /*---------------------------------------------------------------------------*/
    protected static class TextValuePair
    {
        private boolean autoPrompt = false;
        private String text = "";

        private String value = "";

        /*--------------------------------------------------------------------------*/
        /**
         * Constructs a new Text/Value pair, initialized with the text and a value.
         *
         * @param text the text that this object should represent
         * @param value the value that should be associated with this object
         */
        /*--------------------------------------------------------------------------*/
        public TextValuePair(String text, String value)
        {
            this.text = text;
            this.value = value;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * Sets the text
         *
         * @param text the text for this object
         */
        /*--------------------------------------------------------------------------*/
        public void setText(String text)
        {
            this.text = text;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * Sets the value of this object
         *
         * @param value the value for this object
         */
        /*--------------------------------------------------------------------------*/
        public void setValue(String value)
        {
            this.value = value;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * This method returns the text that was set for the object
         *
         * @return the object's text
         */
        /*--------------------------------------------------------------------------*/
        public String toString()
        {
            return (text);
        }

        /*--------------------------------------------------------------------------*/
        /**
         * This method returns the value that was associated with this object
         *
         * @return the object's value
         */
        /*--------------------------------------------------------------------------*/
        public String getValue()
        {
            return (value);
        }

        public boolean getAutoPrompt(){
            return autoPrompt;
        }

        public void setAutoPrompt(boolean value){
            autoPrompt = value;
        }
    }


    /*---------------------------------------------------------------------------*/
    /**
     * This class encapsulates a lot of search field functionality.
     * <p/>
     * A search field supports searching directories and files on the target system. This is a
     * helper class to manage all data belonging to a search field.
     */
    /*---------------------------------------------------------------------------*/

    private class SearchField implements ActionListener
    {

        /**
         * used in constructor - we search for a directory.
         */
        public static final int TYPE_DIRECTORY = 1;

        /**
         * used in constructor - we search for a file.
         */
        public static final int TYPE_FILE = 2;

        /**
         * used in constructor - result of search is the directory.
         */
        public static final int RESULT_DIRECTORY = 1;

        /**
         * used in constructor - result of search is the whole file name.
         */
        public static final int RESULT_FILE = 2;

        /**
         * used in constructor - result of search is the parent directory.
         */
        public static final int RESULT_PARENTDIR = 3;

        private String filename = null;

        private String checkFilename = null;

        private JButton autodetectButton = null;

        private JButton browseButton = null;

        private JComboBox pathComboBox = null;

        private int searchType = TYPE_DIRECTORY;

        private int resultType = RESULT_DIRECTORY;

        private InstallerFrame parent = null;

        /*---------------------------------------------------------------------------*/
        /**
         * Constructor - initializes the object, adds it as action listener to the "autodetect"
         * button.
         *
         * @param filename the name of the file to search for (might be null for searching
         * directories)
         * @param checkFilename the name of the file to check when searching for directories (the
         * checkFilename is appended to a found directory to figure out whether it is the right
         * directory)
         * @param combobox the <code>JComboBox</code> holding the list of choices; it should be
         * editable and contain only Strings
         * @param autobutton the autodetection button for triggering autodetection
         * @param browsebutton the browse button to look for the file
         * @param search_type what to search for - TYPE_FILE or TYPE_DIRECTORY
         * @param result_type what to return as the result - RESULT_FILE or RESULT_DIRECTORY or
         * RESULT_PARENTDIR
         */
        /*---------------------------------------------------------------------------*/
        public SearchField(String filename, String checkFilename, InstallerFrame parent,
                           JComboBox combobox, JButton autobutton, JButton browsebutton, int search_type,
                           int result_type)
        {
            this.filename = filename;
            this.checkFilename = checkFilename;
            this.parent = parent;
            this.autodetectButton = autobutton;
            this.browseButton = browsebutton;
            this.pathComboBox = combobox;
            this.searchType = search_type;
            this.resultType = result_type;

            this.autodetectButton.addActionListener(this);
            this.browseButton.addActionListener(this);

            /*
             * add DocumentListener to manage nextButton if user enters input
             */
            ((JTextField) this.pathComboBox.getEditor().getEditorComponent()).getDocument()
                    .addDocumentListener(new DocumentListener() {

                        public void changedUpdate(DocumentEvent e)
                        {
                            checkNextButtonState();
                        }

                        public void insertUpdate(DocumentEvent e)
                        {
                            checkNextButtonState();
                        }

                        public void removeUpdate(DocumentEvent e)
                        {
                            checkNextButtonState();
                        }

                        private void checkNextButtonState()
                        {
                            Document doc = ((JTextField) pathComboBox.getEditor()
                                    .getEditorComponent()).getDocument();
                            try
                            {
                                if (pathMatches(doc.getText(0, doc.getLength())))
                                {
                                    getInstallerFrame().unlockNextButton(false);
                                }
                                else
                                {
                                    getInstallerFrame().lockNextButton();
                                }
                            }
                            catch (BadLocationException e)
                            {/* ignore, it not happens */}
                        }
                    });

            autodetect();
        }

        /**
         * convenient method
         */
        private InstallerFrame getInstallerFrame()
        {
            return parent;
        }

        /**
         * Check whether the given combobox belongs to this searchfield. This is used when reading
         * the results.
         */
        public boolean belongsTo(JComboBox combobox)
        {
            return (this.pathComboBox == combobox);
        }

        /**
         * check whether the given path matches
         */
        private boolean pathMatches(String path)
        {
            if (path != null)
            { // Make sure, path is not null
                File file = null;

                if ((this.filename == null) || (this.searchType == TYPE_DIRECTORY))
                {
                    file = new File(path);
                }
                else
                {
                    file = new File(path, this.filename);
                }

                if (file.exists())
                {

                    if (((this.searchType == TYPE_DIRECTORY) && (file.isDirectory()))
                            || ((this.searchType == TYPE_FILE) && (file.isFile())))
                    {
                        // no file to check for
                        if (this.checkFilename == null) { return true; }

                        file = new File(file, this.checkFilename);

                        return file.exists();
                    }

                }

                //System.out.println (path + " did not match");
            } // end if
            return false;
        }

        /**
         * perform autodetection
         */
        public boolean autodetect()
        {
            Vector<String> items = new Vector<String>();

            /*
             * Check if the user has entered data into the ComboBox and add it to the Itemlist
             */
            String selected = (String) this.pathComboBox.getSelectedItem();
            if (selected == null)
            {
                parent.lockNextButton();
                return false;
            }
            boolean found = false;
            for (int x = 0; x < this.pathComboBox.getItemCount(); x++)
            {
                if (this.pathComboBox.getItemAt(x).equals(selected))
                {
                    found = true;
                }
            }
            if (!found)
            {
                //System.out.println("Not found in Itemlist");
                this.pathComboBox.addItem(this.pathComboBox.getSelectedItem());
            }

            // Checks whether a placeholder item is in the combobox
            // and resolve the pathes automatically:
            // /usr/lib/* searches all folders in usr/lib to find
            // /usr/lib/*/lib/tools.jar
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            for (int i = 0; i < this.pathComboBox.getItemCount(); ++i)
            {
                String path = vs.substitute((String) this.pathComboBox.getItemAt(i), null);
                //System.out.println ("autodetecting " + path);

                if (path.endsWith("*"))
                {
                    path = path.substring(0, path.length() - 1);
                    File dir = new File(path);

                    if (dir.isDirectory())
                    {
                        File[] subdirs = dir.listFiles();
                        for (File subdir : subdirs)
                        {
                            String search = subdir.getAbsolutePath();
                            if (this.pathMatches(search))
                            {
                                items.add(search);
                            }
                        }
                    }
                }
                else
                {
                    if (this.pathMatches(path))
                    {
                        items.add(path);
                    }
                }
            }
            // Make the enties in the vector unique
            items = new Vector<String>(new HashSet<String>(items));

            // Now clear the combobox and add the items out of the newly
            // generated vector
            this.pathComboBox.removeAllItems();
            for (String item : items)
            {
                String res = vs.substitute(item, "plain");
                //System.out.println ("substitution " + item + ", result " + res);
                this.pathComboBox.addItem(res);
            }

            // loop through all items
            for (int i = 0; i < this.pathComboBox.getItemCount(); ++i)
            {
                String path = (String) this.pathComboBox.getItemAt(i);

                if (this.pathMatches(path))
                {
                    this.pathComboBox.setSelectedIndex(i);
                    parent.unlockNextButton();
                    return true;
                }

            }

            // if the user entered something else, it's not listed as an item
            if (this.pathMatches((String) this.pathComboBox.getSelectedItem()))
            {
                parent.unlockNextButton();
                return true;
            }
            parent.lockNextButton();
            return false;
        }

        /*--------------------------------------------------------------------------*/
        /**
         * This is called if one of the buttons has been pressed.
         * <p/>
         * It checks, which button caused the action and acts accordingly.
         */
        /*--------------------------------------------------------------------------*/
        public void actionPerformed(ActionEvent event)
        {

            if (event.getSource() == this.autodetectButton)
            {
                if (!autodetect())
                {
                    showMessageDialog(parent, "UserInputPanel.search.autodetect.failed.message",
                            "UserInputPanel.search.autodetect.failed.caption",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
            else if (event.getSource() == this.browseButton)
            {
                JFileChooser chooser = new JFileChooser();

                if (this.resultType != TYPE_FILE)
                {
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                }

                int result = chooser.showOpenDialog(this.parent);

                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File f = chooser.getSelectedFile();

                    this.pathComboBox.setSelectedItem(f.getAbsolutePath());

                    // use any given directory directly
                    if (this.resultType != TYPE_FILE && !this.pathMatches(f.getAbsolutePath()))
                    {
                        showMessageDialog(parent, "UserInputPanel.search.wrongselection.message",
                                "UserInputPanel.search.wrongselection.caption",
                                JOptionPane.WARNING_MESSAGE);

                    }
                }

            }

            // we don't care for anything more here - getResult() does the rest
        }

        /*--------------------------------------------------------------------------*/
        /**
         * Return the result of the search according to result type.
         * <p/>
         * Sometimes, the whole path of the file is wanted, sometimes only the directory where the
         * file is in, sometimes the parent directory.
         *
         * @return null on error
         */
        /*--------------------------------------------------------------------------*/
        public String getResult()
        {
            String item = (String) this.pathComboBox.getSelectedItem();
            if (item != null)
            {
                item = item.trim();
            }
            String path = item;

            File f = new File(item);

            if (!f.isDirectory())
            {
                path = f.getParent();
            }

            // path now contains the final content of the combo box
            if (this.resultType == RESULT_DIRECTORY)
            {
                return path;
            }
            else if (this.resultType == RESULT_FILE)
            {
                if (this.filename != null)
                {
                    return path + File.separatorChar + this.filename;
                }
                else
                {
                    return item;
                }
            }
            else if (this.resultType == RESULT_PARENTDIR)
            {
                File dir = new File(path);
                return dir.getParent();
            }

            return null;
        }

    } // private class SearchFile

    //Looks like it doesn't even do anything!!!!! We never have a variable as a child of master spec....
    protected void updateVariables() {
        /**
         * Look if there are new variables defined
         */
        Vector<IXMLElement> variables = spec.getChildrenNamed(VARIABLE_NODE);
        RulesEngine rules = parent.getRules();

        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        for (int i = 0; i < variables.size(); i++)
        {
            IXMLElement variable = variables.elementAt(i);
            String vname = variable.getAttribute(ATTRIBUTE_VARIABLE_NAME);
            String vvalue = variable.getAttribute(ATTRIBUTE_VARIABLE_VALUE);

            System.out.println("Variable name: "  + vname);
            System.out.println("Variable value: " + vvalue);

            if (vvalue == null)
            {
                // try to read value element
                if (variable.hasChildren())
                {
                    IXMLElement value = variable.getFirstChildNamed("value");
                    vvalue = value.getContent();
                    System.out.println("Variable value child: " + vvalue);
                }
            }

            String conditionid = variable.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            System.out.println("Condition id: " + conditionid);

            // Check if condition for this variable is fulfilled
            if (conditionid != null && !rules.isConditionTrue(conditionid, idata.getVariables())) {
                continue;
            }

            // Are there any OS-Constraints?
            if (OsConstraint.oneMatchesCurrentSystem(variable) && vname != null)  {

                if (vvalue != null) {
                    System.out.println("Vname in os contraints: " + vname);
                    // try to substitute variables in value field
                    vvalue = vs.substitute(vvalue, null);
                    System.out.println("Variable Value for os Constraint: "  + vvalue);

                    // to cut out circular references
                    idata.setVariable(vname, "");
                    vvalue = vs.substitute(vvalue, null);
                    System.out.println("Variable Value for os Constraint After cutting curcular references: "  + vvalue);
                }

                idata.setVariable(vname, vvalue);
                entries.add(new TextValuePair(vname, vvalue)); // for save this variable to be used later by Automation Helper

            }
        } //End for loop

    }

    // Repaint all controls and validate them agains the current variables
    public void actionPerformed(ActionEvent e)
    {
        performEvent(e);
    }

    public void itemStateChanged(ItemEvent arg0)
    {
        setInitialFocus((Component) arg0.getSource());
        performEvent(arg0);
    }

    public void performEvent(AWTEvent e){
        Debug.trace("Setting validating to false");
        validating=false;
        updateDialog();
        if (e instanceof ActionEvent)
            scrollToComponent(e.getSource());
        Debug.trace("Setting validating back to true");
        validating=true;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Show localized message dialog basing on given parameters.
     *
     * @param parentFrame The parent frame.
     * @param message The message to print out in dialog box.
     * @param caption The caption of dialog box.
     * @param messageType The message type (JOptionPane.*_MESSAGE)
     */
    /*--------------------------------------------------------------------------*/
    private void showMessageDialog(InstallerFrame parentFrame, String message, String caption,
                                   int messageType)
    {
        String localizedMessage = parentFrame.langpack.getString(message);
        if ((localizedMessage == null) || (localizedMessage.trim().length() == 0))
        {
            localizedMessage = message;
        }
        String localizedCaption = parentFrame.langpack.getString(caption);
        if ((localizedCaption == null) || (localizedCaption.trim().length() == 0))
        {
            localizedCaption = caption;
        }
        JOptionPane.showMessageDialog(parentFrame, localizedMessage, localizedCaption, messageType);
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Show localized warning message dialog basing on given parameters.
     *
     * @param parentFrame parent frame.
     * @param message the message to print out in dialog box.
     */
    /*--------------------------------------------------------------------------*/
    private void showWarningMessageDialog(InstallerFrame parentFrame, String message)
    {
        showMessageDialog(parentFrame, message, "UserInputPanel.error.caption",
                JOptionPane.WARNING_MESSAGE);
    }

    private void updateDialog()
    {
        if (this.eventsActivated)
        {
            this.eventsActivated = false;
            if (isValidated())
            {
                // read input
                // and update elements
                // panelActivate();
                /*
                super.removeAll();
                elements.clear();
                entries.clear();
                summaryEntries.clear();*/
                super.removeAll();
                elements.clear();
                entries.clear();
                init();
                updateVariables();
                buildUI();
                validate();
                repaint();
            }
            this.eventsActivated = true;
        }
    }

    public void focusGained(FocusEvent e)

    {
        //updateDialog();
        //scrollToComponent(e.getSource());
    }

    public void focusLost(FocusEvent e)
    {
        updateDialog();
    }

    /**
     * Programmatically moves the scroll bar over to the object
     * in question if the  object is an instance of a JComponent.
     * @param e
     */
    private void scrollToComponent(Object e) {
        if (e instanceof JComponent) {
            JComponent element = (JComponent) e;
            Rectangle rect = element.getBounds();
            JViewport view = scroller.getViewport();
            rect.x = 0;
            view.scrollRectToVisible(rect);
        }
    }

} // public class UserInputPanel

/*---------------------------------------------------------------------------*/
class UserInputFileFilter extends FileFilter
{

    String fileext = "";

    String description = "";

    public void setFileExt(String fileext)
    {
        this.fileext = fileext;
    }

    public void setFileExtDesc(String desc)
    {
        this.description = desc;
    }

    public boolean accept(File pathname)
    {
        if (pathname.isDirectory())
        {
            return true;
        }
        else
        {
            return pathname.getAbsolutePath().endsWith(this.fileext);
        }
    }

    public String getDescription()
    {
        return this.description;
    }
}
