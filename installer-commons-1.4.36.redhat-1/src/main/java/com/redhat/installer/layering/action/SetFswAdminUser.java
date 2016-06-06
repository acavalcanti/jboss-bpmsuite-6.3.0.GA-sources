package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * reads the application-users.properties and application-roles.properties and tries to determine if there's an SSO user
 * already in existence. We then set the defaults of the FSW admin user and indicate that the user exists
 * TODO: duplicated work within the DuplicateUserValidator. Perhaps the way the files are read can be abstracted.
 */
public class SetFswAdminUser extends PreExistingSetter
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
            // TODO: domain?
            String userPath = idata.getInstallPath() + "/standalone/configuration/application-users.properties";
            String rolePath = idata.getInstallPath() + "/standalone/configuration/application-roles.properties";
            PropertiesConfiguration applicationUsers = new PropertiesConfiguration();
            PropertiesConfiguration applicationRoles = new PropertiesConfiguration();
            try
            {
                applicationUsers.load(new File(userPath));
                applicationRoles.load(new File(rolePath));
            }
            catch (ConfigurationException e)
            {
                e.printStackTrace();
            }

            Iterator<String> users = applicationUsers.getKeys();
            while (users.hasNext())
            {
                String user = users.next();
                // PropertiesConfiguration automatically converts key=val1,val2,val3 into an arraylist containing val1 val2 val3 as distinct elements.
                List<Object> rolesList = applicationRoles.getList(user);
                // if a user exists with these roles, the user is able to use the overlord-idp SSO.
                String roles = "";
                // manually construct the true roles list, thanks PropertiesConfiguration....
                for (int i = 0; i < rolesList.size(); i++)
                {
                    if (i == rolesList.size() - 1)
                    {
                        roles += (String) rolesList.get(i);
                    }
                    else
                    {
                        roles += rolesList.get(i)+",";
                    }
                }
                if (roles.equals(PreExistingConfigurationConstants.fswUserRoles))
                {
                    idata.setVariable("fsw.user", user);
                    idata.setVariable("fsw.user.exists", "true");
                }
            }
        }
    }

    @Override
    protected void setDefaults(String xml, Document doc)
    {
        return;
    }

    @Override
    protected void resetDefaults()
    {

        String defaultFswAdmin = idata.getVariable("fsw.user.default") != null ? idata.getVariable("fsw.user.default") : "fswAdmin";
        idata.setVariable("fsw.user", defaultFswAdmin);
        idata.setVariable("fsw.user.exists", "false");
    }
}
