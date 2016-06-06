package com.izforge.izpack.gui.dynamic;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.PathSelectionPanel;

/**
 * A swing component that allows the GUI user to add to a list of components dynamically. All
 * contained components must be of a single type. The contained type has to be specifically
 * supported.
 */
public class DynamicComponentsPanel extends JPanel implements ActionListener
{

    private static final long serialVersionUID = -998206641413808493L;
    private static final String addOneCommand = "AddOne";
    private static final String removeOneCommand = "RemoveOne";
    private static final String TwoJTextFieldJPanel = "com.izforge.izpack.gui.dynamic.TwoJTextFieldJPanel"; //???
    
    private final Class<?> dynamicComponentType;
    private int dynamicComponentCount = 0;
    private int minDynamicComponents;
    private final int maxDynamicComponents;
    
    private final JLabel addDynamicLabel;
    private final JButton addDynamicComponentButton;
    private final JButton removeDynamicComponentButton;
    
    private InstallData idata;
    private IzPanel izpanel;
    private String tooltip;
    private JPanel dynamicComponents;    
    private String[][] defaults = null;


    /**
     * Create a DynamicComponentsPanel
     * 
     * @param type The class reference (SomeClass.class) of the JComponent that will be added
     * dynamically
     * @param maxComponents The upper limit of the number of dynamically added components
     * @param minComponents The lower limit of the number of dynamically added components
     */
    public DynamicComponentsPanel(Class<?> type, int maxComponents, int minComponents, JComponent headerComponent)
    {
        super(true); // 1 column, n rows
        GridBagLayout layout = new GridBagLayout();
        layout.rowHeights = new int[] { 0, 0, 0 };
        layout.columnWidths = new int [] { 0 };
        layout.columnWeights = new double[] { 1.0 };
        layout.rowWeights = new double[] { 0.0, 1.0, 0.0 };
        super.setLayout(layout);
        super.setOpaque(false);

        // Test if the given class is valid
        if (type == JLabel.class || type == JButton.class)
        {
            throw new RuntimeException("JComponents of the implementing class "
                    + type.getCanonicalName() + " aren't accepted as the dynamic component type.");
        }
        else if (!JComponent.class.isAssignableFrom(type))
        {
            throw new RuntimeException("Class " + type.getCanonicalName()
                    + " isn't JComponent or a subclass of JComponent");
        }
        this.dynamicComponentType = type;

        // Test if the component limit is valid
        if (maxComponents <= 0)
        {
            throw new RuntimeException("Maxiumum components limit of " + maxComponents
                    + " is not valid (must be > 0)");
        }
        this.maxDynamicComponents = maxComponents;
        
        if (minComponents < 0){
            this.minDynamicComponents = 0;
        } else {
            this.minDynamicComponents = minComponents;
        }

        // Create the add/remove component buttons grouped in a JPanel
        JPanel addRemovePanel = new JPanel(new GridLayout(1, 2));

        JButton addButton = new JButton("+1");
        addButton.setActionCommand(addOneCommand);
        addButton.addActionListener(this);
        addRemovePanel.add(addButton);
        this.addDynamicComponentButton = addButton;

        JButton removeButton = new JButton("-1");
        removeButton.setActionCommand(removeOneCommand);
        removeButton.addActionListener(this);
        removeButton.setEnabled(false);
        addRemovePanel.add(removeButton);
        this.removeDynamicComponentButton = removeButton;
        JLabel addLabel = new JLabel();
        this.addDynamicLabel = addLabel;
        
        dynamicComponents = new JPanel(new GridLayout(0,1), true);
        
        GridBagConstraints gbc_headerComponent = new GridBagConstraints();
        gbc_headerComponent.fill = GridBagConstraints.HORIZONTAL;
        gbc_headerComponent.anchor = GridBagConstraints.WEST;
        gbc_headerComponent.gridx = 0;
        gbc_headerComponent.gridy = 0;
        
        GridBagConstraints gbc_dynamicComponents = new GridBagConstraints();
        gbc_dynamicComponents.fill = GridBagConstraints.BOTH;
        gbc_dynamicComponents.anchor = GridBagConstraints.CENTER;
        gbc_dynamicComponents.gridx = 0;
        gbc_dynamicComponents.gridy = 1;
        
        GridBagConstraints gbc_addRemovePanel = new GridBagConstraints();
        gbc_addRemovePanel.fill = GridBagConstraints.NONE;
        gbc_addRemovePanel.anchor = GridBagConstraints.EAST;
        gbc_addRemovePanel.gridx = 0;
        gbc_addRemovePanel.gridy = 2;
        
        super.add(headerComponent, gbc_headerComponent);
        super.add(dynamicComponents, gbc_dynamicComponents);
        super.add(addRemovePanel, gbc_addRemovePanel);
    }
    
    /** 
     * Additional constructor for adding a tooltip
     * @param type
     * @param maxComponents
     * @param minComponents
     * @param headerComponent
     * @param tooltip
     */
    public DynamicComponentsPanel(Class<?> type, int maxComponents, int minComponents, JComponent headerComponent, String tooltip){
        this(type, maxComponents, minComponents, headerComponent);
        setTooltip(tooltip);
    }
    public DynamicComponentsPanel(Class<?> type, int maxComponents, int minComponents, String tooltip) {
        this(type, maxComponents, minComponents, new JPanel(new GridLayout(1, 2)));
        setTooltip(tooltip);
    }

    //TODO: Just use getInstance()
    public void setIdata(InstallData idata){
        this.idata = idata;
    }

    public void setIzpanel(IzPanel parent) {
        this.izpanel = parent;
    }

    /**
     * Serialize contained components' data to an InstallData "variable" compatible (String/String,
     * name/value) format
     * 
     * @param namePrefix The prefix to prepend to the keys of the returned Map
     */
    public Map<String, String> serialize(String namePrefix)
    {
        LinkedHashMap<String, String> serialisedData = new LinkedHashMap<String, String>();

        Component[] components = dynamicComponents.getComponents();
        for (int i = 0; i < components.length; i++)
        {
            Component component = components[i];
            if (KeyValueDynamicComponent.class.isAssignableFrom(component.getClass())) // Check if component implmeents keyvalu thing class
            {
                KeyValueDynamicComponent keyValueComponent = (KeyValueDynamicComponent) component;
                String name = keyValueComponent.getKey();
                String value = keyValueComponent.getValue();

                serialisedData.put(namePrefix + "-" + (i + 1) + "-name", name);
                serialisedData.put(namePrefix + "-" + (i + 1) + "-value", value);
            }
            else if (PathSelectionPanel.class.isAssignableFrom(component.getClass()))
            {
                PathSelectionPanel pathSelectionComponent = (PathSelectionPanel) component;
                String path = pathSelectionComponent.getPath();
                serialisedData.put(namePrefix + "-" + (i + 1)+ "-path", path); // This stores the path names. 
            }
            else
            {
                throw new RuntimeException("Unhandled component type "
                        + component.getClass().getCanonicalName() + "at index " + i);
            }
        }
        return serialisedData;
    }
    
    /**
     * Iterates through the dynamic key/value components and checks for duplication of
     * keys.
     * Returns True if there are no duplicate keys.
     * TODO: Extend to work with PathSelection dynamic components.
     */
    public boolean noKeyDuplication() {
        Component[] components = dynamicComponents.getComponents();
        LinkedHashMap<String,String> componentMap = new LinkedHashMap<String,String>();

        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (KeyValueDynamicComponent.class.isAssignableFrom(component.getClass())) {
                KeyValueDynamicComponent kvComponent = (KeyValueDynamicComponent) component;
                if (componentMap.containsKey(kvComponent.getKey())) {
                    return false;
                } else {
                    componentMap.put(kvComponent.getKey(),kvComponent.getValue());
                }
            } else if (PathSelectionPanel.class.isAssignableFrom(component.getClass())) {
                PathSelectionPanel psPanel = (PathSelectionPanel) component;
                if (componentMap.containsKey(psPanel.getPath())) {
                    return false;
                }
                componentMap.put(psPanel.getPath(),"");
            }
        }
        return true; // No duplicates found.
    }
    
    // Error: Cannot leave a property or value field empty.
    public boolean noEmptyProperties(){
        Component[] components = dynamicComponents.getComponents();

        for (int i = 2; i < components.length; i++) {
            Component component = components[i];
            if (KeyValueDynamicComponent.class.isAssignableFrom(component.getClass())) {
                KeyValueDynamicComponent kvComponent = (KeyValueDynamicComponent) component;
                if (kvComponent.getKey().equals("") || kvComponent.getValue().equals("")) {
                    return false;
                }
            } 
        }
        return true;
    }
    
    public boolean validateDynamicComponents(DynamicValidator validator) {
        Component[] components = dynamicComponents.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (KeyValueDynamicComponent.class.isAssignableFrom(component.getClass())) {
                KeyValueDynamicComponent kvComponent = (KeyValueDynamicComponent) component;
                if (!validator.validateData(kvComponent.getKey(), kvComponent.getValue())){
                    return false;
                }
            }
        }
        return true;
    }
    
    public void actionPerformed(ActionEvent e) {
        boolean somethingChanged = false;
        String actionCommand = e.getActionCommand();

        if (actionCommand.equals(addOneCommand)) {
            oneMore();
            somethingChanged = true;
        }
        else if (actionCommand.equals(removeOneCommand)) {
            oneLess();
            somethingChanged = true;
        }

        if (somethingChanged) {
            refresh();
        }
    }

    /**
     * Make the rendered UI reflect the current configuration
     */
    private void refresh() {
        super.revalidate();
        super.repaint();
    }

    /**
     * Method to change the text of the +1 button
     * 
     * @param text
     */
    public void setAddButtonText(String text) {
        this.addDynamicComponentButton.setText(text);
        refresh();
    }

    /**
     * Method to change the text of the -1 button
     * 
     * @param text
     */
    public void setRemoveButtonText(String text) {
        this.removeDynamicComponentButton.setText(text);
        refresh();
    }
    
    public void setAddLabel() {
        this.addDynamicLabel.setText("Test");
        refresh();
    }
    /**
     * Method to easily start the panel with one component of the desired type already displayed
     */

    public void addInitial() {this.addInitial(new ArrayList<String>());}
    public void addInitial(ArrayList<String> defaultValues) { this.addInitial(defaultValues, true);}

    public void addInitial(ArrayList<String> defaultValues, boolean enabled) {
        if(defaultValues.isEmpty() || defaultValues == null) oneMore();
        else{
            for (String defaultValue : defaultValues){
                oneMore(defaultValue, enabled);
            }
        }
        refresh();
    }

    /**
     * Adds one component to the end
     */
    public void initializeDefaults(String [][] elements) {
        this.dynamicComponentCount += elements.length; // Number of default dynamic components.
        this.defaults = elements; // Keep track of original immutable default elements.
        Object object = createUnknownObject();

            for ( String[] keyValue : elements ) {
                JComponent component = null;
                if (isJTwoTextField(object)) {
                    component = new TwoJTextFieldJPanel(keyValue[0], keyValue[1]);
                }
                else if (isSecurityDomainModule(object)){
                     component = new SecurityDomainModule(keyValue[0]);
                }
                if(component != null) dynamicComponents.add(component);
            }


    }

    private void oneMore() { this.oneMore(""); }
    private void oneMore(String defaultValue) { this.oneMore(defaultValue, true);}
    private void oneMore(String defaultValue, boolean isEnabled)
    {
        Object object = createUnknownObject();

        // if (object instanceof JTextComponent) {}
        if (JComponent.class.isAssignableFrom(object.getClass()))
        {
            // for setting tooltips
            if (TwoJTextFieldJPanel.class.isAssignableFrom(object.getClass())){
                TwoJTextFieldJPanel temp = (TwoJTextFieldJPanel) object;

                if (this.tooltip!=null){
                    temp.getKeyField().setToolTipText(this.tooltip);
                    temp.getValueField().setToolTipText(this.tooltip);
                    temp.setEnabled(isEnabled);
                }
            }
            if (PathSelectionPanel.class.isAssignableFrom(object.getClass()))
            {
                if (idata != null && izpanel != null)
                {

                    // path selection panel requires special information
                    PathSelectionPanel temp = (PathSelectionPanel) object;
                    temp.setIdata(idata);
                    temp.setIzPanel(izpanel);
                    temp.createLayout();
                    temp.setEnabled(isEnabled);
                    if(defaultValue != null & !defaultValue.isEmpty()){
                        temp.setPath(defaultValue);
                    }
                    if (this.tooltip!=null){
                        temp.getPathInputField().setToolTipText(this.tooltip);
                    }
                }
                else {
                    throw new RuntimeException("Missing required izpanel and idata information for PathSelectionPanel"+ dynamicComponentType);
                }

            }
            JComponent component = (JComponent) object;
            // Insert after the current last component
            dynamicComponents.add(component);
        }
        else
        {
            throw new RuntimeException("Unhandled component type "
                    + object.getClass().getCanonicalName());
        }

        // Prevent the user from exeeding the bounds
        dynamicComponentCount++;
        if (dynamicComponentCount >= maxDynamicComponents)
        {
            addDynamicComponentButton.setEnabled(false);
        }
        else if (dynamicComponentCount > minDynamicComponents)
        {
            removeDynamicComponentButton.setEnabled(true);
        }
    }

    /**
     * Removes one component from the end
     */
    private void oneLess()
    {
        // Remove the last component
        dynamicComponents.remove(dynamicComponents.getComponentCount() - 1);

        // Prevent the user from exceeding the bounds
        dynamicComponentCount--;
        if ( !(defaults==null) && dynamicComponentCount <= defaults.length) {
            removeDynamicComponentButton.setEnabled(false); 
        }
        else if (dynamicComponentCount <= 1 ) { 
            removeDynamicComponentButton.setEnabled(false); 
        }
        else if (dynamicComponentCount < maxDynamicComponents) { 
            addDynamicComponentButton.setEnabled(true); 
        }
    }

    public void clearDynamicComponents()
    {
        for (int i = dynamicComponentCount; i > 0; i--){
            oneLess();            
        }
    }
    
    /**
     * Sets the tooltip field. The tool tip will be applied to every dynamic component within a given DynamicComponentPanel
     * @param tooltip
     */
    public void setTooltip(String tooltip){
        this.tooltip = tooltip;
    }
    
    /** Check if an object is a JTwoTextField object */
    public boolean isJTwoTextField(Object obj){
        if (JComponent.class.isAssignableFrom(obj.getClass()) &&
             TwoJTextFieldJPanel.class.isAssignableFrom(obj.getClass()))
             return true;
        return false;
    }
    
    /** Check if an object is a SecurityDomainModule object */
    public boolean isSecurityDomainModule(Object obj){
        if (JComponent.class.isAssignableFrom(obj.getClass()) &&
                SecurityDomainModule.class.isAssignableFrom(obj.getClass()))
             return true;
        return false;
    }
    
    /** Try to create the object determined by the class given in constructor */
    private Object createUnknownObject() {
        Object  object = null;
        try {
            object = dynamicComponentType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not get instance for contained component type", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get instance for contained component type", e);
        }
        return object;
    }

    public void hideButtons() {
        addDynamicComponentButton.setVisible(false);
        removeDynamicComponentButton.setVisible(false);
    }

    public void showButtons() {
        addDynamicComponentButton.setVisible(true);
        removeDynamicComponentButton.setVisible(true);
    }



}
