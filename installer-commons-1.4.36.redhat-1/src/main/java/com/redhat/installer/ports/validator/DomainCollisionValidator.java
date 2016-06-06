package com.redhat.installer.ports.validator;

public class DomainCollisionValidator extends PortCollisionValidator
{
    protected String getConfig() {
        return "domain";
    }
}
