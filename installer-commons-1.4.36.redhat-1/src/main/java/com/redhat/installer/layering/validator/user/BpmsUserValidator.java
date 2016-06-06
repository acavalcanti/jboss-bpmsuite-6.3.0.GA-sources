package com.redhat.installer.layering.validator.user;

/**
 * Created by mtjandra on 2/19/14.
 */
public class BpmsUserValidator extends ApplicationRealmUserValidator {

    protected String getUserVar() {
        return "bpms.user";
    }
}
