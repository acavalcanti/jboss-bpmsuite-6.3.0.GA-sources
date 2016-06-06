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

package com.izforge.izpack.gui;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;


/**
 * This class makes it possible to use default buttons on macosx platform
 */
public class ButtonFactory
{
    private static HashMap<String,String> buttonMnemonicMap = new HashMap<String, String>();
    private static HashMap<String,String> frameShortcuts = new HashMap<String, String>();
    private static boolean useHighlightButtons = false;
    private static boolean useButtonIcons = false;

    /**
     * Enable icons for buttons This setting has no effect on OSX
     */
    public static void useButtonIcons()
    {
        useButtonIcons(true);
    }

    /**
     * Enable or disable icons for buttons This setting has no effect on OSX
     *
     * @param useit flag which determines the behavior
     */
    public static void useButtonIcons(boolean useit)
    {
        if (System.getProperty("mrj.version") == null)
        {
            useButtonIcons = useit;
        }
    }

    /**
     * Enable highlight buttons This setting has no effect on OSX
     */
    public static void useHighlightButtons()
    {
        useHighlightButtons(true);
    }

    /**
     * Enable or disable highlight buttons This setting has no effect on OSX
     *
     * @param useit flag which determines the behavior
     */
    public static void useHighlightButtons(boolean useit)
    {
        if (System.getProperty("mrj.version") == null)
        {
            useHighlightButtons = useit;
        }
        useButtonIcons(useit);
    }

    public static JButton createButton(Icon icon, Color color)
    {
        if (useHighlightButtons)
        {
            if (useButtonIcons)
            {
                return new HighlightJButton(icon, color);
            }
            else
            {
                return new HighlightJButton("", color);
            }

        }
        else
        {
            if (useButtonIcons)
            {
                return new JButton(icon);
            }
            else
            {
                return new JButton();
            }
        }
    }

    public static JButton createButton(String text, Color color)
    {
        JButton btn = null;

        if (useHighlightButtons)
        {
            btn = new HighlightJButton(text, color);
        }
        else
        {
            btn = new JButton(text);
        }

        if (text != null) {
	    String key = findMnemonic(text);
            if (key != null) {
	        btn.setMnemonic(key.charAt(0));
                buttonMnemonicMap.put(key,text);
            }
        }
        return btn;
    }

    public static JButton createButton(String text, Icon icon, Color color)
    {
        JButton btn = null;
        if (useHighlightButtons)
        {
            if (useButtonIcons)
            {
                btn = new HighlightJButton(text, icon, color);
            }
            else
            {
                btn = new HighlightJButton(text, color);
            }
        }
        else
        {
            if (useButtonIcons)
            {
                btn = new JButton(text, icon);
            }
            else
            {
                btn = new JButton(text);
            }
        }

        if (text != null) {
	    String key = findMnemonic(text);
            if (key != null) {
                btn.setMnemonic(key.charAt(0));
                buttonMnemonicMap.put(key,text);
            }
        }

        return btn;
    }

    public static JButton createButton(Action a, Color color)
    {
        if (useHighlightButtons)
        {
            return new HighlightJButton(a, color);
        }
        else
        {
            return new JButton(a);
        }
    }

    /**
     * Finds an available key mnemonic for this button,
     * and sets it. If no mnemonic is possible, no mnemonic
     * is set.
     * @param text Non-null text caption for the JButton.
     */
    public static String findMnemonic(String text) {
        String caption = text.toLowerCase();
        String key = null;

        // Iterate through the characters in this button's
        // text until we either find a suitable mnemonic,
        // or else run out of characters.
        while (caption != null && caption.length() > 0)
        {
            // The mnemonic, or key.
            key = String.valueOf(caption.charAt(0));

            // If key is already in use:
            if (buttonMnemonicMap.containsKey(key)||frameShortcuts.containsKey(key)) {
                caption = caption.substring(1);
                continue;
            } else {
                // If key doesn't exist, this mnemonic
                // is available, so set it and finish.
                return key;
            }
        }
        return null;
    }

    public static void reserveInstallerFrameShortcuts(String [] shortcuts) {
        for (String shortcut : shortcuts) {
            frameShortcuts.put(findMnemonic(shortcut),shortcut);
        }
    }

    public static void clearInstallerFrameShortcuts() {
        frameShortcuts.clear();
    }

    public static void clearButtonMnemonics() {
        buttonMnemonicMap.clear();
    }
}
