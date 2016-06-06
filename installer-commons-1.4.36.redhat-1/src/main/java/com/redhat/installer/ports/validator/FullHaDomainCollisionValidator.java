package com.redhat.installer.ports.validator;

import java.util.ArrayList;

public class FullHaDomainCollisionValidator extends PortCollisionValidator
{
    protected String getConfig() {
        return "domain.fa";
    }

    protected ArrayList<String> getExclusions() {
        ArrayList<String> exclusions = new ArrayList<String>();
        exclusions.add("domain.fa.jgroups-mping.port");
        exclusions.add("domain.fa.messaging-group");
        exclusions.add("domain.fa.modcluster.port");
        return exclusions;
    }
}
