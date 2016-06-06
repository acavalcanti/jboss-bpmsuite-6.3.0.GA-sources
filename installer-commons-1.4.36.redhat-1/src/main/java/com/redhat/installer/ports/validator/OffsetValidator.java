/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2003 Tino Schwarze
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

package com.redhat.installer.ports.validator;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.Validator;
import com.izforge.izpack.installer.AutomatedInstallData;

import java.util.*;
/**
 * A validator to check whether the offset is valid with the highest port number
 * <p/>
 * This validator can be used for rule input fields in the UserInputPanel to make sure that the user
 * entered a valid offset.
 *
 * @author Jyoti Tripathi
 */
public class OffsetValidator implements Validator
{
    private static final int MAXIMUM_VALID_PORT = 65535;
    public boolean validate(ProcessingClient client) {
        return isPortOffsetValidWithHighestPort(client);
    }

    private boolean isPortOffsetValidWithHighestPort(ProcessingClient client) {
        int highestUserSpecifiedPort = findHighestPortValue();
        int numfields = client.getNumFields();
        boolean isPortOffsetValid = true;
        for (int i = 0; i < numfields; i++) {
            String portOffset = client.getFieldContents(i);
            try {
                if (Integer.parseInt(portOffset) + highestUserSpecifiedPort > MAXIMUM_VALID_PORT) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return isPortOffsetValid;
    }

    private int findHighestPortValue() {
        int maxPort = 0;
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        Properties variables       = idata.getVariables();
        String[] varNames          = variables.stringPropertyNames().toArray(new String[0]);
        for (String variable : varNames) {
            if (variable.startsWith("domain") || variable.startsWith("standalone")) {
                String value = variables.getProperty(variable);
                try {
                    if (Integer.parseInt(value) > maxPort)
                        maxPort =  Integer.parseInt(value);
                } catch (Exception e){}
            }
        }
        return maxPort;
    }
}
