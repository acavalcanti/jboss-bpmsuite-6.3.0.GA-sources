package com.redhat.installer.ports.validator;

import com.redhat.installer.ports.validator.PortCollisionValidator;

public class StandaloneCollisionValidator extends PortCollisionValidator
{
    protected String getConfig() {
        return "standalone";
    }
}
