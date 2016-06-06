package com.redhat.installer.gui.dynamic;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel that has two JTextFields on the same line. For KeyValueDynamicComponent, left hand is
 * the key and right hand is the value.
 */
public class TwoJTextFieldJPanel extends JPanel implements KeyValueDynamicComponent
{

    private static final long serialVersionUID = 8369099216227655047L;
    

    private final JTextField keyField;

    private final JTextField valueField;
    
    public TwoJTextFieldJPanel(String key, String value)
    {
        super(new GridLayout(1, 1), true);

       
        JTextField keyField = new JTextField();
        JTextField valueField = new JTextField();
        keyField.setColumns(20);
        valueField.setColumns(20);
        
        if (!key.equals("")){
            keyField.setText(key);
            keyField.setEditable(false);
            valueField.setText(value);
        }
        
        super.add(keyField);
        super.add(valueField);
        this.keyField = keyField;
        this.valueField = valueField;
            
    }
    
    public TwoJTextFieldJPanel(){
        this("", "");
    }

    public String getKey()
    {
        return keyField.getText();
    }

    public String getValue()
    {
        return valueField.getText();
    }
    
    public JTextField getKeyField(){
        return keyField;
    }
    
    public JTextField getValueField(){
        return valueField;
    }

    public void setKeyFieldColumns(int num) { keyField.setColumns(num); }

    public void setValueFieldColumns(int num) { valueField.setColumns(num); }

}
