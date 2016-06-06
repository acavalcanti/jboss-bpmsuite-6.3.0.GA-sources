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

import java.util.*;
/**
 * A validator to check whether the given number is an integer >= 0
 * <p/>
 *
 * @author Jyoti Tripathi
 */
public class OffsetPosIntValidator implements Validator
{

    public boolean validate(ProcessingClient client)
    {
        int numfields = client.getNumFields();
	int offset;
        for (int i = 0; i < numfields; i++)
        {
		String value = client.getFieldContents(i);

		if ((value == null) || (value.length() == 0))
		{
		    return false;
		}

		try {
			offset = Integer.parseInt (value);
		} catch (Exception e) {
			return false;
		}

		if (offset < 0) 
			return false;
        }
 
        return true;
    }

}
