package com.redhat.installer.framework.testers;

import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.framework.constants.CommonStrings;
import com.redhat.installer.framework.mock.MockAbstractUIProcessHandler;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 9/2/14.
 */
public class PanelActionTester extends InstallDataTester implements CommonStrings {
    public AbstractUIProcessHandler handler;
    public PanelAction panelAction;

    @Before
    public void init(){
        handler = new MockAbstractUIProcessHandler();
    }

    @After
    public void end() {
        handler = null;
        panelAction = null;
    }

    /**
     * Verify that all given variables are true
     *
     * @param msg
     * @param trueVariables
     */
    protected static void verifyVariables(String msg, String [] trueVariables) {
        verifyVariables(msg, trueVariables, trueVariables);
    }

    /**
     * Verify that all given variables are false
     * @param msg
     * @param trueVariables
     */
    protected static void verifyFalseVariables(String msg, String [] trueVariables) {
        verifyVariables(msg, new String[0], trueVariables);
    }

    /**
     * Verify that variables in "variables" is the string 'true' if they are specified by as true by "trueVariables"
     * Otherwise verify that variables in "variables" are is the string 'false'
     *
     * @param msg
     * @param trueVariables
     * @param variables
     */
    protected static void verifyVariables(String msg, String[] trueVariables, String[] variables) {
        String expectedValue;
        for (String variable : variables) {
            if (Arrays.asList(trueVariables).contains(variable)) {
                expectedValue = TRUE;
            } else {
                expectedValue = FALSE;
            }
            assertEquals(msg + " | " + variable, expectedValue, idata.getVariable(variable));
        }
    }
}
