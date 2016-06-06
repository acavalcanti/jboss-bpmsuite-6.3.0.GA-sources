package com.redhat.installer.ports.validator;

import java.util.ArrayList;

public class HaStandaloneCollisionValidator extends PortCollisionValidator
{
    protected String getConfig() {
        return "standalone.h";
    }

    protected ArrayList<String> getExclusions() {
        ArrayList<String> exclusions = new ArrayList<String>();
        exclusions.add("standalone.h.jgroups-mping");
        exclusions.add("standalone.h.modcluster");
        return exclusions;
    }
}
