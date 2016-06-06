package com.redhat.installer.tests.password.validator;

import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.password.validator.PasswordValidator;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by eunderhi on 07/08/15.
 */
public class PasswordValidatorTest extends DataValidatorTester {
    @Before
    public void setUp() throws Exception{
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "adminUser", "Teiid.user.username", "Modeshape.user",
                "SharedPasswordValidator.match");

        // Variables that control conditions
        idata.setVariable("use.same.password", "true");
        idata.setVariable("teiid.install", "true");
        idata.setVariable("modeshape.install", "true");

        // Lists of variables
        idata.setVariable("SharedPasswordValidator.conditions", ",teiid.install,modeshape.install");
        idata.setVariable("SharedPasswordValidator.usernameIDs", "adminUser,Teiid.user.username,Modeshape.user");

        // Arbitrary password and default usernames
        idata.setVariable("adminUser", "admin");

        dv = new PasswordValidator();
    }

    @Test
    public void weakPassword() {
        idata.setVariable("adminPassword", "asdfasdf{123");
        assertStatusError();
    }
    @Test
    public void strongPassword() {
        idata.setVariable("adminPassword", "qwer`132");
        assertStatusOk();
    }
    @Test
    public void autoInstallPasswords() {
        assertStatusOk();
    }
}
