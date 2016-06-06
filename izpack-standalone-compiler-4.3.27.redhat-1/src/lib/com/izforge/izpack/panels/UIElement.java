/*
 * IzPack - Copyright 2001-2009 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
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

import java.util.HashMap;
import java.util.Vector;

import javax.swing.JComponent;

import com.izforge.izpack.adaptator.IXMLElement;

/**
 * Metadata for elements shown in the dialog.
 * 
 * @author Dennis Reil
 */

public class UIElement
{   
    
    boolean displayed;

    UIElementType type;

    boolean enabled;

    String associatedVariable;

    JComponent component;

    Object constraints;

    Vector<IXMLElement> forPacks;

    Vector<IXMLElement> forOs;

    String trueValue;

    String falseValue;

    String message;
    
    Boolean usesDeepSub=false;

    Boolean replacePath;
    
    String[] dependsOn = {"", ""};

    String defaultValue = "";

    boolean summarize; // show in summary panel?

    public UIElement()
    {
        summarize = true;
    }

    public boolean hasVariableAssignment()
    {
        return this.associatedVariable != null;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public UIElementType getType()
    {
        return type;
    }

    public void setType(UIElementType type)
    {
        this.type = type;
    }

    public String getAssociatedVariable()
    {
        return associatedVariable;
    }

    public void setAssociatedVariable(String associatedVariable)
    {
        this.associatedVariable = associatedVariable;
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEnabled() { return this.enabled; }

    public JComponent getComponent()
    {
        return component;
    }

    public void setComponent(JComponent component, boolean transparent)
    {
        if (transparent) component.setOpaque(false);
        this.component = component;
    }

    public Object getConstraints()
    {
        return constraints;
    }

    public void setConstraints(Object constraints)
    {
        this.constraints = constraints;
    }

    public Vector<IXMLElement> getForPacks()
    {
        return forPacks;
    }

    public void setForPacks(Vector<IXMLElement> forPacks)
    {
        this.forPacks = forPacks;
    }

    public Vector<IXMLElement> getForOs()
    {
        return forOs;
    }

    public void setForOs(Vector<IXMLElement> forOs)
    {
        this.forOs = forOs;
    }

    public String getTrueValue()
    {
        return trueValue;
    }

    public void setTrueValue(String trueValue)
    {
        this.trueValue = trueValue;
    }

    public String getFalseValue()
    {
        return falseValue;
    }

    public void setFalseValue(String falseValue)
    {
        this.falseValue = falseValue;
    }

    public boolean isDisplayed()
    {
        return displayed;
    }

    public void setDisplayed(boolean displayed)
    {
        this.displayed = displayed;
    }

    public boolean isSummarized()
    {
        return this.summarize;
    }

    public void setSummarize(boolean isit)
    {
        this.summarize = isit;
    }
    
    public boolean usesDeepSub() {
        return this.usesDeepSub;
    }
    
    public void setDeepSub(boolean value) {
        this.usesDeepSub = value;
    }

    public void setReplacePath(boolean replacePath) { this.replacePath = replacePath; }

    public boolean getReplacePath() { return replacePath; }
    
    public boolean isDependent() {
        return !this.dependsOn[0].equals("");
    }
    
    public String getDependency() {
        return this.dependsOn[0];
    }

    public void setDependency(String dependency) {
        this.dependsOn[0] = dependency;
    }

    public void updateDependencyValue(String value) {
        this.dependsOn[1] = value;
    }

    public String getDependencyValue() {
        return dependsOn[1];
    }

    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public String getDefault() {
       return  this.defaultValue;
    }

}