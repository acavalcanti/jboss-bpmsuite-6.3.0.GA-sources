package com.redhat.installer.ports.validator;

import java.util.ArrayList;

public class FullHaStandaloneCollisionValidator extends PortCollisionValidator
{
    protected String getConfig() {
        return "standalone.fa";
    }

    protected ArrayList<String> getExclusions() {
        ArrayList<String> exclusions = new ArrayList<String>();
        exclusions.add("standalone.fa.jgroups-mping");
        exclusions.add("standalone.fa.messaging");
        exclusions.add("standalone.fa.modcluster");
        return exclusions;
    }
}
