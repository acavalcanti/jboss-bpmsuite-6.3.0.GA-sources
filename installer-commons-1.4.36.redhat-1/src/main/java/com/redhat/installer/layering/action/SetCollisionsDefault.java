package com.redhat.installer.layering.action;

import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

/**
 * Fills the collision Sets with the appropriate collisions within the given installer
 */
public class SetCollisionsDefault extends PreExistingSetter
{
    private Set<String> collidingExtensions = new HashSet<String>();
    private Set<String> collidingSubsystems = new HashSet<String>();
    private Set<String> collidingInfinispan = new HashSet<String>();
    private Set<String> collidingDatasources = new HashSet<String>();
    private Set<String> collidingSecurityDomains = new HashSet<String>();

    /**
     * This class makes sure that no .cli scripts that will cause installation failure if ran together are ran together, except for the
     * ones adding hornetq configuration (this needs to have user input, so it is in a validator instead)
     * @param xml
     * @param doc
     */
    @Override
    protected void setDefaults(String xml, Document doc)
    {
        // check extensions
        //NodeList extensions = doc.getElementsByTagName("extension");
        for (String module : collidingExtensions)
        {
            // for every problematic module, try to find it in the descriptor:
            // <extensions>
            // ...
            // <extension module="module"/>
            // ...
            // <extensions>
            Elements elements = doc.select(String.format("extensions > extension[module=%s]", module));
            if (!elements.isEmpty()) {
                idata.setVariable(xml + "." + module + ".extension.exists", "true");
            }
        }

        for (String xmlns : collidingSubsystems)
        {
            if (hasCollisions(doc, String.format("profile > subsystem[xmlns=%s]", xmlns)))
            {
                idata.setVariable(xml + "." + xmlns + ".subsystem.exists", "true");
            }
        }

        for (String name : collidingInfinispan)
        {
            if (hasCollisions(doc, String.format("cache-container[name=%s]", name)))
            {
                idata.setVariable(xml + "." + name + ".infinispan.exists", "true");
            }
        }

        for (String name : collidingSecurityDomains)
        {
            if (hasCollisions(doc, String.format("security-domains > security-domain[name=%s]", name)))
            {
                idata.setVariable(xml + "." + name + ".security-domain.exists", "true");
            }
        }

        for (String name : collidingDatasources)
        {
            if (hasCollisions(doc,String.format("datasources > datasource[pool-name=%s]", name)))
            {
                idata.setVariable(xml + "." + name + ".datasource.exists", "true");
            }
        }
    }

    @Override
    protected void resetDefaults()
    {
        init();

        for (String xml : PreExistingConfigurationConstants.standaloneDescriptors)
        {
            idata.setVariable(xml + ".vault.preexisting", "false");
            for (String extension : collidingExtensions)
            {
                idata.setVariable(xml + "." + extension + ".extension.exists", "false");
            }
            for (String subsystem : collidingSubsystems)
            {
                idata.setVariable(xml + "." + subsystem + ".subsystem.exists", "false");
            }
            for (String infinispan : collidingInfinispan)
            {
                idata.setVariable(xml + "." + infinispan + ".infinispan.exists", "false");
            }
            for (String datasource : collidingDatasources)
            {
                idata.setVariable(xml + "." + datasource + ".datasource.exists", "false");
            }
            for (String securitydomain : collidingSecurityDomains)
            {
                idata.setVariable(xml + "." + securitydomain + ".security-domain.exists" ,"false");
            }
        }
    }

    private boolean hasCollisions(Document doc, String query)
    {
        Elements elements = doc.select(query);
        return !elements.isEmpty();
    }

    private void addAll(String[] source, Set<String> set)
    {
        for (String s : source)
        {
            set.add(s);
        }
    }

    private void init()
    {
        String extensions = idata.getVariable("colliding.extensions");
        String infinispan = idata.getVariable("colliding.infinispanCaches");
        String datasources = idata.getVariable("colliding.datasources");
        String security = idata.getVariable("colliding.securityDomains");
        String subsystems = idata.getVariable("colliding.subsystems");

        if (extensions != null)
        {
            addAll(extensions.split(","), collidingExtensions);
        }

        if (infinispan != null)
        {
            addAll(infinispan.split(","), collidingInfinispan);
        }

        if (datasources != null)
        {
            addAll(datasources.split(","), collidingDatasources);
        }

        if (security != null)
        {
            addAll(security.split(","), collidingSecurityDomains);
        }

        if (subsystems != null)
        {
            addAll(subsystems.split(","), collidingSubsystems);
        }
    }
}
