package com.izforge.izpack.panels;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;


public class GenericInformationJPanel extends JPanel
{
    /**
     * 
     */
    private static final long serialVersionUID = -3042318505657583843L;
    private JLabel description;
    private Class<? extends JComponent> infoClass; // the component that is used to gather information from user
    private JComponent information; // holder for the information
    
    
    /** Question: Why does adding a key listener to this class produce class not found when this is being created
     * Given a type, creates a new GenericInformationJPanel. The panel consists of a JLabel and a JComponent laid out side by side 
     * horizontally, with the JComponent having a heavier weight. The JLabel is non-focusable.
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws IllegalArgumentException 
     */
    public GenericInformationJPanel(Class<? extends JComponent> className, String lblDefault, String ... info) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
        this(className, lblDefault, null, info);
    }
    
    //Allowing passing in your own personalized component
    public GenericInformationJPanel(JComponent component, String lblDefault) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
        this(JTextField.class, lblDefault, component, "");
    }

    public GenericInformationJPanel(Class<? extends JComponent> className, String lblDefault, JComponent component, String ... info) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException{
        setOpaque(false);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{190};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0};
        gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
        description = new JLabel(lblDefault);
        description.setFocusable(false);
        GridBagConstraints gbc_description = new GridBagConstraints();
        gbc_description.anchor = GridBagConstraints.WEST;
        gbc_description.gridx = 0;
        gbc_description.gridy = 0;
        add(description, gbc_description);
        
        if (component != null )
        {
            information = component;
            infoClass = information.getClass();
        } else 
        {
            infoClass = className;
            Constructor<? extends JComponent> constructor = infoClass.getConstructor();
            information = constructor.newInstance();
            setInfo(info);
        }
       
        GridBagConstraints gbc_information = new GridBagConstraints();
        gbc_information.anchor = GridBagConstraints.EAST;
        gbc_information.fill = GridBagConstraints.HORIZONTAL;
        gbc_information.gridx = 1;
        gbc_information.gridy = 0;
        add(information, gbc_information);
    }

    public String getInfo(){
        // need to explicitly support various types of component from here
       if(JTextComponent.class.isAssignableFrom(infoClass)){
            JTextField tmp = (JTextField)information;
            return tmp.getText();
       }
       if (JComboBox.class.isAssignableFrom(infoClass)){
           JComboBox tmp = (JComboBox)information;
           return (String)tmp.getSelectedItem();
       }
       if (PathSelectionPanel.class.isAssignableFrom(infoClass)){
           PathSelectionPanel tmp = (PathSelectionPanel)information;
           return tmp.getPath();
       }
       return ""; // we dun goofed
    }
    
    /** Allow to set name of Jcomponent that takes in a value from user */   
    public void setInfoName(String name){
        information.setName(name);
    }
    
    /**
     * Takes any number of string parameters. In the case of a single-text information field, this method will
     * use the first entry. in other cases, the entire array will be put into the component's model
     * @param s
     */
    public void setInfo(String ... s){
        // need to explicitly support various types of component from here
        if (JTextComponent.class.isAssignableFrom(information.getClass())){
            JTextField tmp = (JTextField)information;
            tmp.setText(s[0]);
        }
        if (JComboBox.class.isAssignableFrom(information.getClass())){
            JComboBox tmp = (JComboBox)information;
            ComboBoxModel cbm = new DefaultComboBoxModel(s);
            tmp.setModel(cbm);
        }
        if (PathSelectionPanel.class.isAssignableFrom(information.getClass())){
            PathSelectionPanel tmp = (PathSelectionPanel)information;
            tmp.setPath(s[0]);
        }
    }
    
    public void setDescription(String s){
        description.setText(s);
    }
    
    public Class<? extends Component> getComponentType(){
        return this.infoClass;
    }
    
    /**
     * Method for adding an action listener to the component within the panel. This component must be of a type that supports actionlisteners; otherwise, 
     * the method will have no effect
     * @param al
     */
    
    public void addActionListener(ActionListener al){
        if (JComboBox.class.isAssignableFrom(information.getClass())){
            JComboBox tmp = (JComboBox)information;
            tmp.addActionListener(al);
        }
        // otherwise, we do nothing
    }
    
    public void setColumns(int cols){
        if (JTextField.class.isAssignableFrom(information.getClass())){
            JTextField tmp = (JTextField)information;
            tmp.setColumns(cols);
        }
    }
    
    public JComponent getInfoComponent(){
        return information;
    }
}
