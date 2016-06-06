/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007-2009 Dennis Reil
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
package com.izforge.izpack.rules;

import com.izforge.izpack.installer.AutomatedInstallData;

/**
 * @author Dennis Reil, <izpack@reil-online.de>
 * @author Francisco Canas, <fcanas@redhat.com> (extension to n-arity ands)
 * @version $Id: OrCondition.java,v 1.1 2006/09/29 14:40:38 dennis Exp $
 */
public class OrCondition extends AnyArityCondition
{
    private static final long serialVersionUID = 8341350377205144199L;
    private static final String tag = "OR";

    public OrCondition()
    {
        super();
    }

    public OrCondition(AutomatedInstallData installdata) {
        super(installdata);
    }

    /**
     * Constructor used for simple binary conditions. Used during parsing of
     * spec files containing condition strings with +,|, etc.
     */
    public OrCondition(Condition operand1, Condition operand2, AutomatedInstallData installdata)
    {
        super(operand1, operand2, installdata);
    }

    protected String getTag() {
        return tag;
    }

    /**
     * Sequentially evaluate the operands.
     * @return True at the first true operand.
     *         False if no ops evaluate to true.
     */
    public boolean isTrue()
    {
        for (Condition operand : operands) {
            if (operand.isTrue()) {
                return true;
            }
        }
        return false;
    }

}
