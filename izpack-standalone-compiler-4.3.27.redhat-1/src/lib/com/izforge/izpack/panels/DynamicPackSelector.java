package com.izforge.izpack.panels;

import java.util.ArrayList;
import java.util.Map;

import com.izforge.izpack.Pack;

/**
 * This class is responsible for dynamically updating pack selection during installation based on
 * the logic rules in the  xml onSelect and onDeselect attributes found in core-packs.xml.
 * @author fcanas@redhat.com
 *
 */
public class DynamicPackSelector
{
    private static final int ON_SELECT = 0;
    private static final int ON_DESELECT = 1;
    
    /**
     * Represents the status of the checkbox: selected = 1, unselected = 0
     */
    int [] checkValues;
    
    /**
     * A list of available packs.
     */
    ArrayList<Pack> packs;
    
    /**
     * A mapping of pack ID to its position on the checkValues array.
     */
    Map<String, Integer> idPos;
    
    public DynamicPackSelector(int[] checkValues, ArrayList<Pack> packs, Map<String, Integer> idPos)
    {
        this.checkValues = checkValues;
        this.packs = packs;
        this.idPos = idPos;
    }
        
    /**
     * Start of pack onSelect/onDeselect parsing and update methods. The onSelect and onDeselect
     * attributes can take strings that contain sets of pack IDs and logical operators conforming to
     * a specific format:
     * 
     * !packA - to specify that we want to unselect pack A
     * 
     * packA - to select pack A
     * 
     * !packA|packB - either deselect pack A, or select pack B. The PacksModel will check to see if
     * either pack A or pack B are already in their specified state (A unselected, B selected). If
     * neither, then it will unselect A since it's the first one listed in the string.
     * 
     * packA>packB - select pack B if pack A is already selected.
     * 
     * The xml attribute can be a string made up of any number of the above patterns separated by
     * commas. If that's the case, PacksModel will update all of them. ie.
     * onDeselect="packA,!packB,packC>!packD,packE|packF" Upon deselection of the current pack,
     * PacksModel will select A, deselect B, deselect D *if* C is selected, and select E if E or F
     * aren't already selected.
     * 
     */

    /**
     * Very similar to dependencies, except that the packs are selected directly instead of just
     * open for selection. Selects are not dependencies in the true sense; they are just a brute
     * force "selecting this pack also selects this pack and deselects this pack" by id.
     * 
     * @return int [] containing the new selections status for all of the packs.
     */
    public void onSelectionUpdate(int currentPack)
    {
        int value = checkValues[currentPack]; // Checkbox status for current pack.
        Pack pack = (Pack) packs.get(currentPack); // The current pack.
        // A list of 'pack sets' (a set being either a single pack or a conditonal of packs)
        // String[] selects = pack.selects;

        // Get the list for the corresponding event (ON_SELECT or ON_DESELECT)
        String[] packSetList = (value <= ON_SELECT) ? pack.onDeselect : pack.onSelect;

        // If we have some sets and we are currently selected:
        if (packSetList != null)
        {
            // For each of the pack sets (either a single pack, or conditionals):
            for (String set : packSetList)
            {
                // Split the set into packs if it's conditional:
                String[] packs = splitCondPacks(set);

                // If none of the packs are at their desired states
                // update the first one.
                if (noPackMeetsCondition(packs, currentPack))
                {
                    updatePackPredicate(packs[0]);
                }
            }
        }
    }

    /**
     * Takes a packstring, and updates it either by selecting it or deselecting it depending on
     * whether it's 'negated' by a '!' or not.
     * 
     * @param pack
     */
    private void updatePack(String pack)
    {
        int pos = getPosFromId(packId(pack));

        // Return if the pack isn't on the list at all.
        if (pos < 0) return;

        // If we want to deselect a pack:
        if (isDeselect(pack))
        {
            checkValues[pos] = 0;
        }
        else
        // if we want to select it
        {
            checkValues[pos] = 1;
        }
    }

    /**
     * Takes a packstring and updates it by first determining whether it's in the form of an
     * implication or not.
     * 
     * Possible combinations include !packA>packB or packA>!packB, or just plain !packA or packB.
     * 
     * Call this one first because it handles the implication types.
     */
    private void updatePackPredicate(String packstring)
    {
        if (packstring.contains(">"))
        {
            String[] imp = packstring.split(">");
            if (packSelectionCondition(imp[0]))
            {
                updatePack(imp[1]);
            }
        }
        else
        {
            updatePack(packstring);
        }
    }

    /**
     * Returns whether the pack is in the correct state or not: ie. If it should be selected, and is
     * selected, returns true. Etc. Handles packstrings in implication form: ie. If pack A selected,
     * Select pack B.
     * 
     * @param packstring
     * @return
     */
    private boolean packSelectionCondition(String packstring)
    {
        if (packstring.contains(">"))
        {
            String[] imp = packstring.split(">");
            return !packConditionIsTrue(imp[0]) || packConditionIsTrue(imp[1]);
        }
        return packConditionIsTrue(packstring);
    }

    /**
     * Takes a single atomic packstring predicate and evalutes whether it's true or not. ie. !packA
     * packB etc.
     * 
     * @param packstring
     * @return True if condition is true.
     */
    private boolean packConditionIsTrue(String packstring)
    {
        return ((!isDeselect(packstring) && packIsSelected(packId(packstring))) || (isDeselect(packstring) && !packIsSelected(packId(packstring))));
    }

    /**
     * Returns true if this is a pack that must be deselected, as opposed to selected.
     * 
     * @param packstring
     */
    private boolean isDeselect(String packstring)
    {
        return packstring.startsWith("!");
    }

    /**
     * Returns the packID from the condition string, stripping away the '!' if necessary.
     * 
     * @param packstring
     * @return
     */
    private String packId(String packstring)
    {
        return isDeselect(packstring) ? packstring.substring(1) : packstring;
    }

    /**
     * Evaluates whether given selects/deselects attribute string contains conditionals, and returns
     * the individual packs as an array of strings.
     * 
     * ie. "packA|packB|packC" returns a String array with {"packA", "packB", "packC"} "packA"
     * returns {"packA"}
     * 
     * @param condition
     * @return A String [] that holds each pack in the attribute.
     */
    private String[] splitCondPacks(String condition)
    {
        return condition.split("\\|");
    }


    /**
     * Checks whether there are no packs in the given pack list that meet their required condition.
     * 
     * @param condpacks
     * @param current
     * @return True if all packs are in the wrong state.
     */
    private boolean noPackMeetsCondition(String[] condpacks, int current)
    {
            // Check each of the packs in the condition string:
            for (String p : condpacks)
            {
            if (packSelectionCondition(p))
            {
                    return false;
                }
            }
        return true;
    }

    /**
     * Checks if the given pack is currently selected.
     * 
     * @param packname
     * @return True if selected, False if pack isn't selected or isn't on the installed pack's list.
     */
    private boolean packIsSelected(String packname)
    {
        int pos = getPosFromId(packname);

        return (pos >= 0) ? checkValues[pos] >= 1 : false;
    }
    
    /**
     * Return the position of the pack from its id, or -1 if this pack isn't on the list.
     * 
     * @param id
     * @return
     */
    private int getPosFromId(String id)
    {
        int pos;

        try
        {
            pos = idPos.get(id);
        }
        catch (java.lang.NullPointerException e)
        {
            // NPE means this pack isn't on the list, so it's either not being isntalled or hidden.
            pos = -1;
        }

        return pos;
    }

    /**
     * End of pack onSelect/onDeselect language helpers.
     */
}