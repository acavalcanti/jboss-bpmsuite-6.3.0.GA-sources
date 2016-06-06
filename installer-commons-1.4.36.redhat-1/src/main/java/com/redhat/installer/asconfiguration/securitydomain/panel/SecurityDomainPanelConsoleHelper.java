package com.redhat.installer.asconfiguration.securitydomain.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.Shell;
import com.redhat.installer.components.ConsoleComponents;
import com.redhat.installer.asconfiguration.securitydomain.constant.SecurityDomainConstants;

import javax.swing.*;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class SecurityDomainPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole, SecurityDomainConstants {
    private AutomatedInstallData idata;
    private DataHelper helper;
    
    public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter) {
        return true;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p) {
        return true;
    }

    /** Method called when user gets to this console panel */
    public boolean runConsole(AutomatedInstallData installData, ConsoleInstaller parent) {
        idata = installData;
        this.helper = new DataHelper(idata,SecurityDomainConstants.class);
        
        String intro = idata.langpack.getString(MAIN_TEXT);
        System.out.println(intro);

        for (Map.Entry<String[], Class<?>> content : UI_ELEMENTS.entrySet()) {
            String[] info = content.getKey();
            String label = info[0];
            String[] values = Arrays.copyOfRange(info,1 , info.length );
            if(JTextField.class == content.getValue())
                if (label.equals("name"))
                    ConsoleComponents.enterValue(label, "nospaces", Pattern.compile("^\\S+$"), helper);
                else
                    ConsoleComponents.enterValue(label, "incomplete", Pattern.compile(".+"), helper);
            else
                ConsoleComponents.showComboBox(label, values, helper);
            
            if (label.startsWith("add")) { //Kind of hackish, changed to if it starts with dynamic?
                if(helper.getVariable(label).equals("Yes"))
                    showDynamicComponent(label);
                else{
                    helper.setVariable(label + COUNT, String.valueOf(0));
                }
            }
        }
        
        switch(askEndOfConsolePanel(idata)) {
            case 1: return true;
            case 2: return false;
            case 3: return runConsole(idata, parent); 
            default: throw new IllegalArgumentException("askEndOfConsolePanel Returned an unexpected value");
        }
    }
    
    /** Dynamic Panel represented in the console
     * Composed of JComboBoxes or JTextFields
     * @param id      Pass in ID that ties a label and variable together
     */
    private void showDynamicComponent(String id) {
    	Shell console = Shell.getInstance();
    	String addModule = idata.langpack.getString("securitydomain.addModule");
        int col = 0;
        int numModules = 0;
        while (true) {
            for (String suffix : DYNAMIC_HEADERS) {
                String label = id+suffix;
                //System.out.println(idata.langpack.getString(BASE+label));
                if (col<2)  ConsoleComponents.showComboBox(label, label+"."+numModules, DEFAULTS_MAP.get(id)[col], helper); //TODO: This is hacky should be resolved
                else        ConsoleComponents.enterValue(label, label+"."+numModules, "option", Pattern.compile(OPERATION_VALIDATOR), helper);
                col = (col+1) % DYNAMIC_HEADERS.length;
            }
            numModules+=1;
            //System.out.println("Would you like to add another module? [y/n] [n]");
            System.out.println(addModule);
            if(console.getInput().equals("y")) continue;
            break;
        }
        helper.setVariable(id + COUNT, String.valueOf(numModules*3));
    }
    
    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata){
        new SecurityDomainPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }
}
