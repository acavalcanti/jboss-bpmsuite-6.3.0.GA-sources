package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SetSecurityDomainDefault implements PanelAction
{
    private AutomatedInstallData idata;
    private Set<String> securityDomainNames = new HashSet<String>();

    @Override
    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler)
    {
        this.idata = idata;
        boolean needInstall = Boolean.parseBoolean(idata.getVariable("eap.needs.install"));
        resetDefaults();

        if (needInstall)
        {
            return;
        }
        else
        {
            Document doc = null;
            for (String descriptor : PreExistingConfigurationConstants.standaloneDescriptors)
            {
                String path = idata.getInstallPath() + "/standalone/configuration/" + descriptor;
                File descFile = new File(path);
                if (descFile.exists())
                {
                    try
                    {
                        doc = Jsoup.parse(descFile, "UTF-8", "");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    setSecurityDomainDefaults(doc);
                }
            }
            for (String descriptor : PreExistingConfigurationConstants.domainDescriptors)
            {
                String path = idata.getInstallPath() + "/domain/configuration/" + descriptor;
                File descFile = new File(path);
                if (descFile.exists())
                {
                    try
                    {
                        doc = Jsoup.parse(descFile, "UTF-8", "");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    setSecurityDomainDefaults(doc);
                }
            }
        }
    }

    @Override
    public void initialize(PanelActionConfiguration configuration)
    {

    }

    /**
     * This simple method gets a list of pre-existing securitydomain names, and saves them to idata, delimited by spaces.
     * The contents of the variable should be used in the validation of the security domain so that no collision is possible.
     * @param doc
     */
    private void setSecurityDomainDefaults(Document doc) {
        // securitydomain.preexisting.names
        Elements securityDomains = doc.select("security-domains > security-domain");

        for (Element domain : securityDomains) {
            securityDomainNames.add(domain.attr("name"));
        }
    }

    private void resetDefaults()
    {
        //idata.setVariable("securitydomain.preexisting.names", "other,jboss-web-policy,jboss-ejb-policy,");
        return;
    }
}
