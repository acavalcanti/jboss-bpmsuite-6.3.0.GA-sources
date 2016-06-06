package com.redhat.installer.gui.dynamic;

import com.redhat.installer.asconfiguration.securitydomain.constant.SecurityDomainConstants;

import javax.swing.*;
import java.awt.*;

//TODO: Refactor out to common-installer
public class SecurityDomainModule extends JPanel  implements SecurityDomainConstants
{
    private static final long serialVersionUID = 8369099216227655047L;

    private  JComboBox code;
    private  JComboBox flag;
    private  JTextField operation;
    private  boolean enabled = true;
    
    public SecurityDomainModule(String id, String value)
    {
        super(new GridLayout(1, 1), true);
        String [][] settings = DEFAULTS_MAP.get(id);
        String[] codeOptions = settings[0];
        String[] flagOptions = settings[1];
        
        code = new JComboBox();
        DefaultComboBoxModel codeMenu = new DefaultComboBoxModel(codeOptions);
        code.setModel(codeMenu);
        
        flag = new JComboBox();
        ComboBoxModel flagMenu = new DefaultComboBoxModel(flagOptions);
        flag.setModel(flagMenu);
       
        operation = new JTextField();
        operation.setText(value);
        operation.setColumns(15);

        super.add(code);
        super.add(flag);
        super.add(operation);

    }

    public SecurityDomainModule(String id){
        this(id,  "");
    }

    public SecurityDomainModule(){ //Dummy to check what class something is

    }
    
    public String getCode() {
        return (String) code.getSelectedItem();
    }
    
    public String getFlag() {
        return (String) flag.getSelectedItem();
    }
    
    public String getOperation(){
        return operation.getText();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
        code.setEnabled(enabled);
        flag.setEnabled(enabled);
        operation.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }


}