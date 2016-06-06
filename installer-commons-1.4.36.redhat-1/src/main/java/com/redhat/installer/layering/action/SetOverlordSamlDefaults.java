package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIHandler;
import org.jsoup.nodes.Document;

import java.io.File;

/**
 * Looks for the overlord-saml.keystore. This file is only relevant for FSW / SRAMP installations, but we need to
 * know about it and ask for existing passwords instead of trying to create a new keystore.
 * Assumptions: the overlord-saml.keystore existing at the location means that we did, in fact, create it
 **/
public class SetOverlordSamlDefaults extends PreExistingSetter
{

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
            String samlPath = idata.getInstallPath() + "/standalone/configuration/overlord-saml.keystore";
            File samlFile = new File(samlPath);

            if (samlFile.exists())
            {
                idata.setVariable("saml.keystore.pre.existing", "true");
            }
        }
    }

    @Override
    protected void resetDefaults()
    {
        idata.setVariable("saml.keystore.pre.existing", "false");
    }

    @Override
    protected void setDefaults(String xml, Document doc)
    {

    }

}
