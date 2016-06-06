package com.redhat.installer.layering.validator;

import com.redhat.installer.layering.validator.PreExistingVariableValidator;

/**
 * Created by thauser on 2/18/14.
 */
public class LDAPConnectionNameValidator extends PreExistingVariableValidator
{
    @Override
    public String getPreexistingVar() {
        return "ldap.preexisting.conn.names";
    }
}
