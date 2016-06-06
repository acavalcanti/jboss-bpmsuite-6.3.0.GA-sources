package com.redhat.installer.tests.password.validator;

import com.izforge.izpack.rules.Condition;
import com.izforge.izpack.rules.RulesEngine;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.password.validator.SharedPasswordValidator;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class SharedPasswordValidatorTest extends DataValidatorTester {

    @BeforeClass
    public static void init() {
        Map<String,Condition> condMap = new HashMap<String,Condition>(1);
        TestUtils.insertVariableCondition(condMap, "use.same.password", "use.same.password", "true");
        TestUtils.insertVariableCondition(condMap, "teiid.install", "teiid.install", "true");
        TestUtils.insertVariableCondition(condMap, "modeshape.install", "modeshape.install", "true");
        RulesEngine rules = new RulesEngine(condMap, idata);
        idata.setRules(rules);
    }

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "adminUser", "Teiid.user.username", "Modeshape.user",
                "SharedPasswordValidator.match");

        // Variables that control conditions
        idata.setVariable("use.same.password", "true");
        idata.setVariable("addUser", "true");
        idata.setVariable("teiid.install", "true");
        idata.setVariable("modeshape.install", "true");

        // Lists of variables
        idata.setVariable("SharedPasswordValidator.conditions", ",teiid.install,modeshape.install");
        idata.setVariable("SharedPasswordValidator.usernameIDs", "adminUser,Teiid.user.username,Modeshape.user");

        // Arbitrary password and default usernames
        idata.setVariable("adminPassword", "foobar1@");
        idata.setVariable("adminUser", "admin");
        idata.setVariable("Teiid.user.username", "teiidUser");
        idata.setVariable("Modeshape.user", "modeshapeUser");

        dv = new SharedPasswordValidator();
    }

    @Test
    public void noMatch() {
        assertStatusOk();
    }

    @Test
    public void matchUnusedPassword() {
        idata.setVariable("modeshape.install", "false");
        idata.setVariable("Modeshape.user", "foobar1@");
        assertStatusOk();
    }

    @Test
    public void matchUsedPassword() {
        idata.setVariable("Modeshape.user", "foobar1@");
        assertStatusError();
    }

    @Test
    public void matchUnsharedPassword() {
        idata.setVariable("use.same.password", "false");
        idata.setVariable("Modeshape.user", "foobar1@");
        assertStatusOk();
    }
}
