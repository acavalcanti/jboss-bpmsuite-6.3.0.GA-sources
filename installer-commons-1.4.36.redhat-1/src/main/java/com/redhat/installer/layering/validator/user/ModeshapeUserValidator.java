package com.redhat.installer.layering.validator.user;

public class ModeshapeUserValidator extends DuplicateUserValidator {

    protected String getFileName() {
        return "modeshape-users.properties";
    }

    protected String getUserVar() {
        return "Modeshape.user";
    }

    protected String getCondVar() {
        return null;
    }

    protected Status getConflictStatus() {
        return Status.ERROR;
    }

}
