package com.redhat.installer.layering.validator;

import com.redhat.installer.layering.validator.PreExistingVariableValidator;

/**
 * Specific validator that checks for the ldap realm name to already exist, and warns the user if it does.
 * Created by thauser on 2/18/14.
 */
public class LDAPRealmNameValidator extends PreExistingVariableValidator
{
    @Override
    public String getPreexistingVar() {
        return "ldap.preexisting.realm.names";
    }
}
