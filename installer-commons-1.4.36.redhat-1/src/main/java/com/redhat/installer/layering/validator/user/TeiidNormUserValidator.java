package com.redhat.installer.layering.validator.user;

public class TeiidNormUserValidator extends TeiidSecurityUserValidator {
    protected String getUserVar() {
        return "Teiid.norm.user";
    }
}

