package com.redhat.installer.layering.action;

import com.redhat.installer.layering.PreExistingConfigurationConstants;
import com.redhat.installer.layering.action.PreExistingSetter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class SetLdapDefaults extends PreExistingSetter
{
    private Set<String> ldapRealmNames = new HashSet<String>();
    private Set<String> ldapConnNames = new HashSet<String>();

    /**
     * Same as setSecurityDomainDefaults, but for ldap connections and realms.
     * TODO: Use the ldapRealmNames and ldapConnNames if they are to be used
     * @param doc
     */
    protected void setDefaults(String xml, Document doc)
    {
        Elements ldapRealms = doc.select("security-realm > authentication > ldap");
        Elements ldapConnections = doc.select("outbound-connections > ldap");

        if (ldapConnections.size() > 0)
        {
            idata.setVariable(xml + ".pre.existing.ldap", "true");
        }
        else
        {
            idata.setVariable(xml + ".pre.existing.ldap", "false");
        }

        for (Element realm : ldapRealms)
        {
            ldapRealmNames.add(realm.attr("name"));
        }

        for (Element conn : ldapConnections)
        {
            ldapConnNames.add(conn.attr("name"));
        }
    }

    protected void resetDefaults()
    {
        idata.setVariable("ldap.preexisting.realm.names", "");
        idata.setVariable("ldap.preexisting.conn.names", "");

        for (String xml : PreExistingConfigurationConstants.descriptors)
        {
            idata.setVariable(xml + ".pre.existing.ldap", "false");
        }
        idata.setVariable("pre.existing.ldap", "false");
    }
}
