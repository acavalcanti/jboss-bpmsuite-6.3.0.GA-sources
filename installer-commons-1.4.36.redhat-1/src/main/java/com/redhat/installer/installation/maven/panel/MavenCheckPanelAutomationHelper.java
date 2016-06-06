package com.redhat.installer.installation.maven.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.ReversePathSubstitutor;
import com.izforge.izpack.util.VariableSubstitutor;

public class MavenCheckPanelAutomationHelper implements PanelAutomation
{

    /**
     * Asks to make the XML panel data.
     *
     * @param idata     The installation data.
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {

        IXMLElement mavenSettings = new XMLElementImpl("mavenSettings",panelRoot);

        String mavenSettingsValue = idata.getVariable("mavenSettings");
        String mavenSettingsFullPath = idata.getVariable("MAVEN_SETTINGS_FULLPATH");

        mavenSettings.setContent(mavenSettingsValue);
        IXMLElement prev1 = panelRoot.getFirstChildNamed("mavenSettings");
        if (prev1 != null){
            panelRoot.removeChild(prev1);
        }

        panelRoot.addChild(mavenSettings);

        if (mavenSettingsValue.equals("on")) {
            IXMLElement settingsFullPath = new XMLElementImpl("mavensettingsfullpath",panelRoot);
            mavenSettingsFullPath = ReversePathSubstitutor.substitute("USER_HOME", mavenSettingsFullPath);
            IXMLElement prev2 = panelRoot.getFirstChildNamed("mavensettingsfullpath");
            if (prev2 != null) {
                panelRoot.removeChild(prev2);
            }
            settingsFullPath.setContent(mavenSettingsFullPath);
            panelRoot.addChild(settingsFullPath);
        }
    }

    /**
     * Asks to run in the automated mode.
     *
     * @param idata     The installation data.
     * @param panelRoot The XML tree to read the data from.
     */
    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        IXMLElement mavenSettings = panelRoot.getFirstChildNamed("mavenSettings");
        idata.setVariable("mavenSettings", mavenSettings.getContent());
        if (idata.getVariable("mavenSettings").equals("on")) {
            IXMLElement settingsFullPath = panelRoot.getFirstChildNamed("mavensettingsfullpath");
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            String full = settingsFullPath.getContent();
            full = vs.substitute(full);
            idata.setVariable("MAVEN_SETTINGS_FULLPATH", full);
        }
    }
}
