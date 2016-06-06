package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;

import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

abstract public class PreExistingSetter implements PanelAction
{
    protected AutomatedInstallData idata;

    public void executeAction(AutomatedInstallData idata, AbstractUIHandler handler)
    {
        this.idata = idata;
        boolean freshInstall = Boolean.parseBoolean(idata.getVariable("eap.needs.install"));
        resetDefaults();

        if(freshInstall)
        {
            resetDefaults();
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
                    setDefaults(descriptor, doc);
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
                    setDefaults(descriptor, doc);
                }
            }
        }
    }

    /**
     * Set install data variables based on an existing installation
     * @param xml Name of the descriptor file
     * @param doc JSoup Document representation of the descriptor file
     */
    protected abstract void setDefaults(String xml, Document doc);

    /**
     * Reset install data variables back to its default values
     */
    protected abstract void resetDefaults();

    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
        //Unused
    }
}
