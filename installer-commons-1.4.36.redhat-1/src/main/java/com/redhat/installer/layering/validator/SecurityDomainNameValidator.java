package com.redhat.installer.layering.validator;

import com.redhat.installer.layering.validator.PreExistingVariableValidator;

/**
 * New class for SecurityDomain validation that isn't JSSE
 * Created by thauser on 2/18/14.
 */
public class SecurityDomainNameValidator extends PreExistingVariableValidator
{
    @Override
    public String getPreexistingVar() {
        return "securitydomain.preexisting.names";
    }
}
