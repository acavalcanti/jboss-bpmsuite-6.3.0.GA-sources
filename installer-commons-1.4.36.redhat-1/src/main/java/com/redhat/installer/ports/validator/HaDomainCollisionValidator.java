package com.redhat.installer.ports.validator;

import java.util.ArrayList;

public class HaDomainCollisionValidator extends PortCollisionValidator
{
    protected String getConfig() {
        return "domain.h";
    }

    protected ArrayList<String> getExclusions() {
        ArrayList<String> exclusions = new ArrayList<String>();
        exclusions.add("domain.h.jgroups-mping.port");
        exclusions.add("domain.h.modcluster.port");
        return exclusions;
    }
}
