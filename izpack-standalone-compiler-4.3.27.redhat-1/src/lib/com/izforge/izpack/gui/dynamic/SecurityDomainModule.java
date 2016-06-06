package com.izforge.izpack.gui.dynamic;

import java.awt.GridLayout;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

//TODO: Refactor out to common-installer
public class SecurityDomainModule extends JPanel
{
    private static final long serialVersionUID = 8369099216227655047L;

    private  JComboBox code;
    private  JComboBox flag;
    private  JTextField operation;
    private final String[] codeOptions = {
            "Client", "Certificate", "CertificateUsers", "CertificateRoles" , "Database", "DatabaseCertificate",
            "DatabaseUsers", "Identity", "Ldap", "LdapExtended", "RoleMapping", "RunAs", "Simple", "ConfiguredIdentity",
            "SecureIdentity", "PropertiesUsers", "SimpleUsers", "LdapUsers", "Kerbero", "SPNEGOUesrs", "AdvancedLdap",
            "AdvancedADLdap", "UsersRoles"};
    private final String[] flagOptions = { "Required", "Requisite", "Sufficient", "Optional" };
    
    public SecurityDomainModule(String value)
    {
        super(new GridLayout(1, 1), true);
        
        code = new JComboBox();
        DefaultComboBoxModel codeMenu = new DefaultComboBoxModel(codeOptions);
        code.setModel(codeMenu);
        
        flag = new JComboBox();
        ComboBoxModel flagMenu = new DefaultComboBoxModel(flagOptions);
        flag.setModel(flagMenu);
        operation = new JTextField();
        
        operation.setText(value);

        super.add(code);
        super.add(flag);
        super.add(operation);

    }

    public SecurityDomainModule(){
        this("");
    }
}