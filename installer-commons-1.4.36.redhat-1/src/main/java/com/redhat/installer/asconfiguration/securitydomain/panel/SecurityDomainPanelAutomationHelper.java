package com.redhat.installer.asconfiguration.securitydomain.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.PanelAutomation;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.securitydomain.constant.SecurityDomainConstants;

import java.util.ArrayList;

public class SecurityDomainPanelAutomationHelper implements PanelAutomation, SecurityDomainConstants {

    public void makeXMLData(AutomatedInstallData idata, IXMLElement panelRoot) {
        ArrayList<IXMLElement> securityDomainData = new ArrayList<IXMLElement>();
        
        for( String id : DATUM ) {
            String dataVar = BASE + id + VARIABLE;
            IXMLElement tag = new XMLElementImpl(dataVar,panelRoot);
            String value = idata.getVariable(dataVar);
            tag.setContent(value);
            securityDomainData.add(tag);
            if (id.startsWith("add")){ //This is bad
                IXMLElement subTag  = new XMLElementImpl(BASE + id + MODULE, panelRoot);
                int numModules = Integer.parseInt(idata.getVariable(BASE + id + COUNT + VARIABLE));
                for (int i=0; i<numModules; i++) {
                    String position = DYNAMIC_HEADERS[i%3] +".";
                    IXMLElement childTag = new XMLElementImpl(BASE + id + position + i/3 + VARIABLE,panelRoot);
                    String childValue = idata.getVariable(BASE + id + position + i/3 + VARIABLE);
                    childTag.setContent(childValue);
                    subTag.addChild(childTag);
                }
                IXMLElement countTag = new XMLElementImpl(BASE + id + COUNT + VARIABLE, panelRoot);
                String count = idata.getVariable(BASE + id + COUNT + VARIABLE);
                countTag.setContent(count);
                subTag.addChild(countTag);
                securityDomainData.add(subTag);
            }
            
        }
        
        for (IXMLElement elem  : securityDomainData){
            panelRoot.addChild(elem);
        }

    }

    public void runAutomated(AutomatedInstallData idata, IXMLElement panelRoot) throws InstallerException {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());      
        IXMLElement securityRoot = panelRoot.getFirstChildNamed(SECURITY_XML_HEADER);
        if (securityRoot != null) runAutomated(idata, securityRoot);
        else {
            for (IXMLElement tag : panelRoot.getChildren()){
                if (!tag.hasChildren()){
                    String value = vs.substitute(tag.getContent());
                    idata.setVariable(tag.getName(), value);
                }
                else {
                    runAutomated(idata, tag);
                }
            }
        }
    }

}
