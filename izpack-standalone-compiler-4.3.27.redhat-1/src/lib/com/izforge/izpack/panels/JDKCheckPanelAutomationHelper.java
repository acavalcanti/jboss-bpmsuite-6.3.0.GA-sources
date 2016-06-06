package com.izforge.izpack.panels;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.VariableSubstitutor;



public class JDKCheckPanelAutomationHelper implements PanelAutomation
{
    
    private static final String JDK_EXISTS = "izpack.jdk.exists";

    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot)
    {
        IXMLElement ipath = new XMLElementImpl(JDK_EXISTS, panelRoot);
        
        ipath.setContent(idata.getVariable(JDK_EXISTS));
        
        IXMLElement prev = panelRoot.getFirstChildNamed(JDK_EXISTS);
        if (prev!=null){
            panelRoot.removeChild(prev);
        }
        panelRoot.addChild(ipath);
    }

    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot)
            throws InstallerException
    {
        IXMLElement ipath = panelRoot.getFirstChildNamed(JDK_EXISTS);
        VariableSubstitutor varSub = new VariableSubstitutor(idata.getVariables());
        String jdkExists = ipath.getContent();
        jdkExists = varSub.substitute(jdkExists, null);
        
        idata.setVariable(JDK_EXISTS, jdkExists);
    }

}
