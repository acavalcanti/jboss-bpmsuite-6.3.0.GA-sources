package com.redhat.installer.password.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;

import com.izforge.izpack.installer.InstallData;
import org.jboss.as.domain.management.security.password.*;
import org.jboss.as.domain.management.security.password.simple.SimplePasswordStrengthChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eunderhi on 04/08/15.
 */
public class PasswordValidator implements DataValidator {

    private static final String ERROR_MESSAGE = "security.password.strength";
    List<PasswordRestriction> restrictions;
    SimplePasswordStrengthChecker checker;
    private String failingUser;
    AutomatedInstallData adata;

    private static final String[] USERS = {
            "adminUser",
            "Dashboard.admin.username",
            "Teiid.user.username",
            "Modeshape.user",

    };
    private static final String[] PASSWORDS = {
            "adminPassword",
            "Dashboard.admin.password",
            "Teiid.user.password",
            "Modeshape.password",

    };

    @Override
    public Status validateData(AutomatedInstallData adata) {
        makeRestrictions();
        checker = new SimplePasswordStrengthChecker();
        this.adata = adata;
        return checkAllPasswords();
    }

    private void makeRestrictions() {
        restrictions = new ArrayList<PasswordRestriction>();
        restrictions.add(new LengthRestriction(8));
        restrictions.add(new RegexRestriction(
                ".*[0-9].*",
                "Must have at least one digit.",
                "Password must have at least one digit"));
        restrictions.add(new RegexRestriction(
                ".*[0-9].*",
                "Must have at least one alphabet character",
                "Password must have at least one alphabet character."));
        restrictions.add(new RegexRestriction(
                ".*[^a-zA-Z0-9].*",
                "Must have at least one non-alphanumeric character",
                "Password must have at least one non-alphanumeric character."));
        restrictions.add(new UsernamePasswordMatch(false));
        restrictions.add(new ValueRestriction(new String[] {"root", "admin", "administrator"}, false));
    }
    private Status checkAllPasswords() {
        for(int i = 0; i < USERS.length; i++) {
            String username = adata.getVariable(USERS[i]);
            String password = adata.getVariable(PASSWORDS[i]);

            if(isSkipAdminOrAuto(i, username, password)) {
                return Status.OK;
            }
            if (checkPassword(username, password) != Status.OK) {
                failingUser = username;
                return Status.ERROR;
            }
        }
        return Status.OK;
    }
    private Status checkPassword(String username, String password) {
        PasswordStrengthCheckResult result = checker.check(username, password, restrictions);
        if(result.getStrength().getStrength() >= PasswordStrength.MEDIUM.getStrength()) {
            return Status.OK;
        }
        return Status.ERROR;
    }
    private boolean isSkipAdminOrAuto(int i, String password, String username) {
        if(username == null || password == null) return true;
        return (i > 0 && password.isEmpty());
    }
    @Override
    public String getErrorMessageId() {
        return ERROR_MESSAGE;
    }

    @Override
    public String getWarningMessageId() {
        return null;
    }

    @Override
    public boolean getDefaultAnswer() {
        return false;
    }

    @Override
    public String getFormattedMessage() {
        String id = getErrorMessageId();
        AutomatedInstallData adata = InstallData.getInstance();
        checker = new SimplePasswordStrengthChecker();

        return failingUser + adata.langpack.getString(id);
    }
}
