package com.redhat.installer.layering.validator.user;

public class TeiidAdminUserValidator extends TeiidSecurityUserValidator {
    protected String getUserVar() {
        return "Teiid.admin.user";
    }
}
