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

package com.izforge.izpack.panels;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Shell;

/**
 * Console implementation for the TreePacksPanel.
 * 
 * Based on PacksPanelConsoleHelper 
 *
 * @author Sergiy Shyrkov
 * @author Dustin Kut Moy Cheung
 */
public class TreePacksPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    Shell console = Shell.getInstance();

    private static final String INFO = "PacksPanel.info";
    private static final String EMPTY = "";
    private static final String SPACE = " ";
    private static final String NO_PACKS    = "TreePacksPanel.no.packs";
    private static final String CONTINUE    = "TreePacksPanel.continue.question";
    private static final String ALREADY_SELECTED    = "TreePacksPanel.already.selected";
    private static final String NOT_SELECTED        = "TreePacksPanel.not.selected";
    private static final String REQUIRED            = "TreePacksPanel.required";
    private static final String DONE                = "TreePacksPanel.done";
    private static final String UNSELECTABLE        = "TreePacksPanel.unselectable";
    private static final String CONFIRM             = "TreePacksPanel.confirm";
    private static final String NUMBER              = "TreePacksPanel.number";
    private static final String PROMPT              = "TreePacksPanel.prompt";
    private static final String INVALID             = "TreePacksPanel.invalid";
    private static final int SELECTED = 1;
    private static final int DESELECTED = 0;

    private static String getTranslation(AutomatedInstallData idata, String text) {
        return idata.langpack.getString(text);
    }
    
    /**
     * Simple method to print the String in the argument.
     * Used to avoid seeeing the System.out.println() code _ALL_ the time.
     *
     * @param message String to print
     * @return void
     */
    private static void out(String message) 
    {
        System.out.println(message);
    }

    private String getI18n(LocaleDatabase langpack, String key, String defaultValue)
    {
        String text = langpack.getString(key);
        return text != null && !text.equals(key) ? text : defaultValue;
    }

    @Override
    public String getSummaryBody(AutomatedInstallData idata)
    {
        StringBuffer retval = new StringBuffer(256);
        Iterator iter = idata.selectedPacks.iterator();
        boolean first = true;
        while (iter.hasNext())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            Pack pack = (Pack) iter.next();
            retval.append(getI18NPackName(pack, idata));
        }
        return retval.toString();
    }

    /**
     * This method tries to resolve the localized name of the given pack. If this is not possible,
     * the name given in the installation description file in ELEMENT <pack> will be used.
     *
     * @param pack for which the name should be resolved
     * @return localized name of the pack
     */
    private String getI18NPackName(Pack pack, AutomatedInstallData idata)
    {
        // Internationalization code
        String packName = pack.name;
        String key = pack.id;
        if (idata.langpack != null && pack.id != null && !"".equals(pack.id))
        {
            packName = idata.langpack.getString(key);
        }
        if ("".equals(packName) || key == null || key.equals(packName))
        {
            packName = pack.name;
        }
        return (packName);
    }

    /**
     * Method that is called in console mode for TreePacksPanel
     *
     * @param installData The "Database" of izpack
     *
     * @return whether the console mode is supported or not.
     */
    public boolean runConsole(AutomatedInstallData installData, ConsoleInstaller parent)
    {
        Map<String, List<String>> treeData      = new HashMap<String, List<String>>();
        Map<String, Pack> idToPack              = new HashMap<String, Pack>();
        List<String> packParents                = new LinkedList<String>();
        List<Pack> selectedPacks                = new ArrayList<Pack>();
        List<String> kids;

        // load I18N
        LocaleDatabase langpack = installData.langpack;

        try {
            InputStream inputStream = ResourceManager.getInstance().getInputStream("packsLang.xml");
            langpack.add(inputStream);
        } catch (Exception e) {
            Debug.trace(e);
        }
        // initialize selection
        out(EMPTY);
        System.out.print(getTranslation(installData, INFO));
        out(EMPTY);

        for (Pack pack : installData.availablePacks) {

            kids = null;
            idToPack.put(pack.id, pack);

            if (pack.parent != null) {

                if (treeData.containsKey(pack.parent)) {
                    kids = treeData.get(pack.parent);
                } else {
                    kids = new ArrayList<String>();
                }
                kids.add(pack.id);
            } else {
                // add to packParents packs that do not have parents
                // that is, they are top-level packs
                packParents.add(pack.id);
            }
            treeData.put(pack.parent, kids);
        }
        // Display list of packs for user to select from
        selectHelper(treeData, selectedPacks, installData, idToPack, packParents, true, "\t");
        //drawHelper(treeData, selectedPacks, installData, idToPack, packParents, true, "\t");
        /*
         * for(String packParentName : packParents) {
            drawHelper(treeData, selectedPacks, installData, idToPack, packParentName, true, "\t");
        }
         */
       
        out(getTranslation(installData, DONE));
        installData.selectedPacks = selectedPacks;

        if (selectedPacks.size() == 0) {
            out(getTranslation(installData, NO_PACKS));
            out(getTranslation(installData, CONTINUE));
        }

        // No need for break statements since we are using "return"
        switch(askEndOfConsolePanel(installData)) {
            case 1: return true;
            case 2: return false;
            default: return runConsole(installData, parent);
        }
    }

    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The XML tree to write the data in.
     */
    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
    {
        new ImgPacksPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    /**
     * It is used to "draw" the appropriate tree-like structure of the packs and ask if you want to install
     * the pack. The pack will automatically be selected if it is required; otherwise you will be prompted if
     * you want to install that pack. If a pack is not selected, then their child packs won't be installed as
     * well and you won't be prompted to install them.
     *
     * @param treeData          - Map that contains information on the parent pack and its children
     * @param selectedPacks     - the packs that are selected by the user are added there
     * @param installData       - Database of izpack
     * @param idToPack          - Map that mapds the id of the available packs to the actual Pack object
     * @param packParent        - The current "parent" pack to process
     * @param packMaster        - boolean to know if packParent is a top-level pack
     * @param indent            - String to know by how much the child packs should be indented
     *
     * @return void
     */
    /*
     SEEMS ITS NOT LONGER NEEDED
    private void drawHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks,final AutomatedInstallData installData,
                            final Map<String, Pack> idToPack, final List<String> packParents, boolean packMaster,final String indent) 
    {
        List<Pack> p = new ArrayList<Pack>();
        for(Pack pack : installData.availablePacks){
            p.add(pack);
        }
		
		// If the pack is a top-level pack and that top-level pack was not
		// selected, then return. This will avoid prompting the user to
		// install the child packs.
        // NOTE: Seems like this is never called without top-level pack
        
		if (packMaster && !selectHelper(treeData, selectedPacks, installData, idToPack, p, packMaster, indent)) {
			return;
		}
        //Below might be needed if packMaster is ever False
		//if (treeData.containsKey(packParent)) {
		//	for (String id : treeData.get(packParent)) {
		//		p = idToPack.get(id);
		//selectHelper(treeData, selectedPacks, installData, idToPack, p, false, indent);
		//	}
		//}
		
    }*/
    
    /**
     * Helper method to ask/check if the pack can/needs to be installed
     * If top-level pack, square brackets will be placed in between
     * the pack id.
     *
     * It asks the user if it wants to install the pack if:
     * 1. the pack is not required
     * 2. the pack has no condition string
     *
     * @return true     - if pack selected
     * @return false    - if pack not selected
     * 
     * 
     * @param treeData          - Map that contains information on the parent pack and its children
     * @param selectedPacks     - the packs that are selected by the user are added there
     * @param installData       - Database of izpack
     * @param idToPack          - Map that mapds the id of the available packs to the actual Pack object
     * @param packMaster        - boolean to know if packParent is a top-level pack
     * @param indent            - String to know by how much the child packs should be indented
     */
    private boolean selectHelper(final Map<String, List<String>> treeData, final List<Pack> selectedPacks,final AutomatedInstallData installData,
                            final Map<String, Pack> idToPack, final List<String> packsStrings, boolean packMaster,final String indent) 
    {             
        List<Pack> packs = new ArrayList<Pack>();
        for(Pack pack : installData.availablePacks) packs.add(pack);

        int selected[] = new int[packs.size()]; // Indicated which packs are selected or not

        // Maps view selection choice to where pack actually is on available packs and selection array
        int mapChoiceToSelection[] = new int[packs.size()];
        int choiceCount = 0;

        Map<String, Integer> idPos = new HashMap<String, Integer>();
        
        for (int i = 0; i < packs.size(); i++)
        {
            Pack pack = (Pack) packs.get(i);
            Boolean conditionSatisfied  = checkCondition(installData, pack);
            Boolean conditionExists     = !(conditionSatisfied == null);
            idPos.put(pack.id, i);
            if(conditionExists) {
                if (conditionSatisfied){
                    selected[i] = SELECTED;
                }
            } else if (pack.preselected) {
                selected[i] = SELECTED;
            }

            if (!pack.isHidden()) {
                mapChoiceToSelection[choiceCount] = i;
                choiceCount++;
            }
        }
        DynamicPackSelector dPackSelector = new DynamicPackSelector(selected, (ArrayList<Pack>) packs, idPos);
        int packnum = printPackMenu(installData, packs, indent, selected, mapChoiceToSelection);
        
        //Allow user to select/deselect packs
        while (true)
        {
            System.out.println(getTranslation(installData, PROMPT));
            int choice = -1;
            try {
                choice =  (Integer.parseInt(console.getInput())) -1;
            }
            catch(NumberFormatException e){
                out(getTranslation(installData, NUMBER));
                continue;
            }
            if (choice < packnum && choice >= 0) {
                choice = mapChoiceToSelection[choice];
                if (packs.get(choice).required || !packs.get(choice).selectable) {
                    out(getTranslation(installData, INVALID));
                }
                /* Code below should be implemented so it is more readable */
                else {
                    selected[choice] = (selected[choice] + 1) % 2;                       //Toggle between selected and unselected
                    if (treeData.containsKey(packs.get(choice).id)) {                    //Check if what the user selected is a parent
                        for (String child : treeData.get(packs.get(choice).id)){         //Select || Deselect all children
                            selected[idPos.get(child)] = selected[choice];
                        }
                    }
                    // Check if this pack is a child of a parent, and that the parent is not required
                    else if(packs.get(choice).parent != null && !packs.get(idPos.get(packs.get(choice).parent)).required ) {          
                        selected[idPos.get(packs.get(choice).parent)] = SELECTED;             // Select parent  
                        for (String child : treeData.get(packs.get(choice).parent)){          // If at least one child is unselected ensure parent is unselected
                            if(selected[idPos.get(child)] == DESELECTED) selected[idPos.get(packs.get(choice).parent)] = DESELECTED;         
                        }
                    }
                    dPackSelector.onSelectionUpdate(choice);
                    printPackMenu(installData, packs, indent, selected, mapChoiceToSelection);
                }
            } 
            else if (choice == -1) break;
            else                  out(getTranslation(installData, INVALID));
            
        }
        for (int i = 0; i < selected.length; i++){
            if (selected[i] == SELECTED) selectedPacks.add(packs.get(i));
        }
        return true;
    }
    /**
     * helper method to know if the condition assigned to the pack is satisfied
     *
     * @param installData       - the data of izpack
     * @param pack              - the pack whose condition needs to be checked 
     * @return true             - if the condition is satisfied
     *         false            - if condition not satisfied
     *         null             - if no condition assigned
     */
    private Boolean checkCondition(AutomatedInstallData installData, Pack pack) 
    {
        if (pack.hasCondition()) return installData.getRules().isConditionTrue(pack.getCondition());
        else                     return null;
    }
    /**
     * Helper method to read the input of user
     * Method returns true if user types "y", "yes" or <Enter> 
     *
     * @return int  - 0 if the answer is yes, 1 if the answer is no, -1 if the answer is invalid
     */
    /* SEEMS NO LONGER NEEDED
    private int readPrompt() 
    {
        String answer = "No";
        try {
            answer = console.getInput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes") || answer.equals(""))){
            return 0;
        } else if (answer.equalsIgnoreCase("n") || answer.equalsIgnoreCase("no")){
            return 1;
        } else {
            return -1;
        }
    }
    */

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p) 
    {
        // not implemented
        return false;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
                                                PrintWriter printWriter) 
    {
        // not implemented
        return false;
    }
    /**
     * Method will print the pack selection state onto the console
     * @param iData       - the data of izpack
     * @param packs             - list of available packs
     * @param indent
     * @param selected  array to hold selection status for available packs
     * @param choiceMap array to hold mapping of visible packs to their index in selected
     */
    public int printPackMenu(AutomatedInstallData iData, List<Pack> packs, String indent, int[] selected, int[] choiceMap)
    {
        long totalSize = 0;
        int packnum = 1;
        for (Pack p : packs) {
            if (p.isHidden())
                continue;
            else if (p.required)
                System.out.printf("%-4d [%s] %-15s [%s] (%-4s)\n", packnum, (selected[choiceMap[packnum -1]] == SELECTED ? "x" : " "),
                        getTranslation(iData, REQUIRED), iData.langpack.getString(p.id), p.toByteUnitsString(p.getSize()));
            else if (!p.selectable)
                System.out.printf( "%-4d [%s] %-15s [%s] (%-4s)\n", packnum, (selected[choiceMap[packnum-1]] == SELECTED ? "x" : " "),
                        getTranslation(iData, UNSELECTABLE), iData.langpack.getString(p.id), p.toByteUnitsString(p.getSize()));
            else
                System.out.printf( "%-4d [%s] %-15s [%s] (%-4s)\n", packnum, (selected[choiceMap[packnum-1]] == SELECTED ? "x" : " "),
                        "", iData.langpack.getString(p.id), p.toByteUnitsString(p.getSize()));

            if (selected[choiceMap[packnum-1]] == SELECTED) totalSize += p.getSize();
            packnum++;
        }
        System.out.println("Total Size Required: " + Pack.toByteUnitsString(totalSize));
        System.out.println(getTranslation(iData, CONFIRM));
        return packnum-1;
    }
}
