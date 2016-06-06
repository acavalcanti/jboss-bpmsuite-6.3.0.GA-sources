package com.redhat.installer.installation.maven.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.ReversePathSubstitutor;
import com.izforge.izpack.util.VariableSubstitutor;

public class MavenRepoCheckPanelAutomationHelper implements PanelAutomation
{

    /**
     * Asks to make the XML panel data.
     *
     * @param idata     The installation data.
     * @param panelRoot The tree to put the data in.
     */
    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        IXMLElement ipath = new XMLElementImpl("mavenrepopath",panelRoot);
        String mavenRepoPath = idata.getVariable("MAVEN_REPO_PATH");
        ipath.setContent(mavenRepoPath);
        IXMLElement settingsFullPath = new XMLElementImpl("mavensettingsfullpath",panelRoot);
        String mavenSettingsFullPath = idata.getVariable("MAVEN_SETTINGS_FULLPATH");
        mavenSettingsFullPath = ReversePathSubstitutor.substitute("USER_HOME", mavenSettingsFullPath);
        settingsFullPath.setContent(mavenSettingsFullPath);
        IXMLElement mavenSettings = new XMLElementImpl("mavenSettings",panelRoot);
        String mavenSettingsValue = idata.getVariable("mavenSettings");
        mavenSettings.setContent(mavenSettingsValue);
        IXMLElement prev = panelRoot.getFirstChildNamed("mavenrepopath");
        IXMLElement prev2 = panelRoot.getFirstChildNamed("mavensettingsfullpath");
        IXMLElement prev3 = panelRoot.getFirstChildNamed("mavenSettings");
        if (prev != null)
        {
            panelRoot.removeChild(prev);
        }
        if (prev2 != null){
            panelRoot.removeChild(prev2);
        }
        if (prev3 != null){
            panelRoot.removeChild(prev3);
        }
        if (mavenRepoPath != null && ipath != null) {
            panelRoot.addChild(ipath);
            panelRoot.addChild(settingsFullPath);
            panelRoot.addChild(mavenSettings);
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
        IXMLElement mavenRepoPath = panelRoot.getFirstChildNamed("mavenrepopath");
        IXMLElement settingsFullPath = panelRoot.getFirstChildNamed("mavensettingsfullpath");
        IXMLElement mavenSettings = panelRoot.getFirstChildNamed("mavenSettings");
        if (mavenRepoPath != null && settingsFullPath != null) {
            VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
            String path = mavenRepoPath.getContent();
            String full = settingsFullPath.getContent();
            String settings = mavenSettings.getContent();
            path = vs.substitute(path);
            full = vs.substitute(full);
            settings = vs.substitute(settings);
            // if they exist, it should be safe to just set mavenSettings to on
            idata.setVariable("MAVEN_REPO_PATH", path);
            idata.setVariable("MAVEN_SETTINGS_FULLPATH", full);
            idata.setVariable("mavenSettings", settings);
        }
    }
}
