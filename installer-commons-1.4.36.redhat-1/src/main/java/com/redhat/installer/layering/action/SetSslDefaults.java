package com.redhat.installer.layering.action;

import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SetSslDefaults extends PreExistingSetter
{

    /**
     * Same as setSecurityDomainDefaults, but for ssl certs
     * TODO: it seems like the <ssl> element may be unique. need to double check the schema for this fact and modify the
     * check to be more efficient perhaps
     * @param xml
     * @param doc
     */
    @Override
    protected void setDefaults(String xml, Document doc)
    {
        Elements sslIdentities = doc.select("server-identities > ssl");
        if (sslIdentities.size() > 0)
        {
            idata.setVariable("pre.existing.ssl", "true");
            idata.setVariable(xml + ".pre.existing.ssl", "true"); //ex: standalone-ha.pre.existing.ssl
        }
    }

    @Override
    protected void resetDefaults()
    {
        for (String xml : PreExistingConfigurationConstants.descriptors)
        {
            idata.setVariable(xml + ".pre.existing.ssl", "false");
        }
        idata.setVariable("pre.existing.ssl", "false");
    }
}
