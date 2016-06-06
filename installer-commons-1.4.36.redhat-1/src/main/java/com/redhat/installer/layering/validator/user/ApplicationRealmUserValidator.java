package com.redhat.installer.layering.validator.user;

/**
 * Created by thauser on 2/19/14.
 */
public abstract class ApplicationRealmUserValidator extends DuplicateUserValidator {
    @Override
    protected String getFileName() {
        return "application-users.properties";
    }

    //Variable that holdes the username
    protected String getCondVar() {
        return null;
    }

    protected Status getConflictStatus() {
        return Status.ERROR;
    }
}
