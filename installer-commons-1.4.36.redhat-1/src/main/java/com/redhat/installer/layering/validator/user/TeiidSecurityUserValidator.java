package com.redhat.installer.layering.validator.user;

abstract public class TeiidSecurityUserValidator extends DuplicateUserValidator {

    protected abstract String getUserVar();

    protected String getFileName() {
        return "teiid-security-users.properties";
    }

    protected String getCondVar() {
        return null;
    }

    protected Status getConflictStatus() {
        return Status.ERROR;
    }

}
