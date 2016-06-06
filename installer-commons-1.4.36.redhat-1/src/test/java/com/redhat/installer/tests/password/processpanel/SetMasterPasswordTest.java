package com.redhat.installer.tests.password.processpanel;

import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngine;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.password.processpanel.SetMasterPassword;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 2/10/15.
 */
public class SetMasterPasswordTest extends ProcessPanelTester {

    SetMasterPassword smp;
    @Before
    public void setUp() throws Exception {
        smp = new SetMasterPassword();
        idata.setVariable("use.same.password", "true");
        idata.setVariable("condition1","false");
        idata.setVariable("condition2","false");
        idata.setVariable("condition3","false");
        idata.setVariable("master.password.vars", "testpass1=condition1,testpass2=condition2,testpass3=condition3");
        idata.setVariable("master.password.var", "masterpassword");
        idata.setVariable("masterpassword", "new-master-value");
        Map<String,Condition> condMap = new HashMap<String,Condition>();
        TestUtils.insertVariableCondition(condMap,"use.same.password","use.same.password","true");
        TestUtils.insertVariableCondition(condMap,"condition1","condition1","true");
        TestUtils.insertVariableCondition(condMap,"condition2","condition2","true");
        TestUtils.insertVariableCondition(condMap,"condition3","condition3","true");
        RulesEngine rulesEngine = new RulesEngine(condMap, idata);
        idata.setRules(rulesEngine);
    }

    @Test
    public void testPasswordAssignment() {
        smp.run(handler,new String[]{});
        assertEquals("new-master-value",idata.getVariable("testpass1"));
        assertEquals("new-master-value",idata.getVariable("testpass2"));
        assertEquals("new-master-value",idata.getVariable("testpass3"));
    }

    @Test
    public void testPasswordConditional(){
        idata.setVariable("condition1", "true");
        idata.setVariable("condition3", "true");
        idata.setVariable("testpass1", "unchanged");
        idata.setVariable("testpass3", "unchanged");
        smp.run(handler,new String[]{});
        assertEquals("unchanged",idata.getVariable("testpass1"));
        assertEquals("new-master-value",idata.getVariable("testpass2"));
        assertEquals("unchanged",idata.getVariable("testpass3"));
    }

    @Test
    public void testPasswordNoOverwrite(){
        idata.setVariable("use.same.password", "false");
        idata.setVariable("testpass1", "");
        idata.setVariable("testpass2", "no-override-value");
        smp.run(handler,new String[]{});
        assertEquals("",idata.getVariable("testpass1"));
        assertEquals("no-override-value",idata.getVariable("testpass2"));
        assertEquals(null,idata.getVariable("testpass3"));
    }

    @Override
    public void testProcessPanelInstantiation() {

    }
}
