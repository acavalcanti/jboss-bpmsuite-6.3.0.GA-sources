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

/**
 * Additional metadata for password elements.
 * 
 * @author Dennis Reil
 * 
 */
public class PasswordUIElement extends UIElement
{

    private boolean autoPrompt = false;
    private String password = "";
    private String id = "";

    public PasswordUIElement()
    {
        super();
    }

    PasswordGroup passwordGroup;

    public PasswordGroup getPasswordGroup()
    {
        return passwordGroup;
    }

    public void setPasswordGroup(PasswordGroup passwordGroup) {
        this.passwordGroup = passwordGroup;
    }

    public void setAutoPrompt(boolean autoPrompt) {
        this.autoPrompt = autoPrompt;
    }

    public boolean getAutoPrompt() { return autoPrompt;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}