package com.redhat.installer.asconfiguration.securitydomain.panel;

/** Imports required to create IzPack Panel */
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.GenericInformationJPanel;
import com.izforge.izpack.util.GUIHelper;
import com.redhat.installer.components.GuiComponents;
import com.redhat.installer.asconfiguration.securitydomain.constant.SecurityDomainConstants;
import com.redhat.installer.gui.dynamic.DynamicComponentsPanel;
import com.redhat.installer.gui.dynamic.SecurityDomainModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SecurityDomainPanel extends IzPanel implements SecurityDomainConstants
{
    private static final long serialVersionUID = 1L;
    private SecurityDomain securityDomainContent;
    private DataHelper helper;
    private boolean firstActivation = true;
    
    public SecurityDomainPanel(InstallerFrame parent, InstallData idata)
    {
        this (parent, idata, new IzPanelLayout());
    }

    //TODO: Generalize commons functions that can be used by any custom panel
    public SecurityDomainPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout)
    {
        super(parent, idata, layout);
        helper = new DataHelper(idata, SecurityDomainConstants.class);

        try
        {
            securityDomainContent = new SecurityDomain(idata, !parent.hasBackground, isDisplayingHidden());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        JPanel mainPanel = GuiComponents.createSkeleton(SECURITY_HEADLINE, this, idata);
        mainPanel.add(securityDomainContent, GUIHelper.getContentPanelConstraints());

        JScrollPane scroller = GUIHelper.createPanelScroller(getBorder(), mainPanel,!parent.hasBackground);
        add(scroller, BorderLayout.CENTER);
    }
    
    //Runs when use reaches the Summary Panel 
    public String getSummaryBody()
    {
        String securityDomainSelected = idata.getVariable("installSecurityDomain");
        if(securityDomainSelected == null || securityDomainSelected.equals("false"))
        {
            return null;
        }
        return showModule();
    }
    
    private String showModule() //TODO: Tie this in with the order of the components, and generalize how to print different components 
    {
        StringBuffer sb = new StringBuffer();
        Component[] components = securityDomainContent.getComponents();
        for (int i = 0; i < components.length; i++)
        {
            Component component = components[i];
            if (component instanceof GenericInformationJPanel)
            {
                GenericInformationJPanel data = (GenericInformationJPanel) component;
                sb.append(helper.getLabel(data.getName()) + " " + helper.getVariable(data.getName()) + "<br>");
                if (data.getName().contains(AUTHENTICATION_ID) || data.getName().contains(AUTHORIZATION_ID) || (data.getName().contains(MAPPING_ID) ))
                { //HACKY
                    String var = helper.getVariable((data.getName() + COUNT));
                    int numOfVariables = Integer.parseInt(helper.getVariable((data.getName() + COUNT)));
                    for (int j=0; j<numOfVariables/NUM_DYNAMIC_COL; j++) {
                        sb.append((j + 1)+". "+helper.getLabel(data.getName()+ SecurityDomainConstants.LEFT) 
                                  +helper.getVariable(data.getName()+ SecurityDomainConstants.LEFT + "." + j) + "<br>");
                        sb.append("&nbsp;"+"&nbsp;"+"&nbsp;" +"&nbsp;"+helper.getLabel(data.getName()+ SecurityDomainConstants.MIDDLE)
                                  +helper.getVariable(data.getName() + SecurityDomainConstants.MIDDLE + "." + j) + "<br>");
                        sb.append("&nbsp;"+"&nbsp;"+"&nbsp;"+"&nbsp;"+helper.getLabel(data.getName()+ SecurityDomainConstants.RIGHT)
                                  +helper.getVariable(data.getName() + SecurityDomainConstants.RIGHT + "." + j) + "<br>");
                    }
                }
            }
        }
        return sb.toString();
    }

    public void makeXMLData(IXMLElement panelRoot)
    {
        new SecurityDomainPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    public void panelActivate()
    {
        securityDomainContent.revalidate();
        securityDomainContent.repaint();
        if (isDisplayingHidden() && firstActivation)
        {   //TODO: Figure out why it can't go in the normal initialization
            for (String id : securityDomainContent.modules.keySet())
            {
                for (Component comp : securityDomainContent.modules.get(id))
                {
                    comp.setVisible(true);
                    comp.setEnabled(false);
                }
            }
            firstActivation = false;
        }
        super.panelActivate();
    }
    
    public boolean isValidated()
    {
       String msg = valid();
       if(!msg.isEmpty())
       {
           emitError("Error", msg);
           return false;
       }
       return true;
    }
    
    public String valid()
    {
        Component[] components = securityDomainContent.getComponents();
        for (int i = 0; i < components.length; i++)
        {
            int count = 0;

            Component component = components[i];

            if (component instanceof GenericInformationJPanel)
            {
                GenericInformationJPanel data = (GenericInformationJPanel) component;
                if (data.getName().equals("name")){
                    if (!Pattern.matches("^\\S+$", data.getInfo())){
                        return (idata.langpack.getString("SecurityDomainPanel.nospaces"));
                    }
                }
                if (data.getInfo().isEmpty()) return helper.getErrorMsg("incomplete");
                helper.setVariable(data.getName(), data.getInfo());
            }
            else if (component instanceof DynamicComponentsPanel)
            {
                DynamicComponentsPanel data = (DynamicComponentsPanel) component;
                if(component.isVisible() && data.isEnabled())
                {
                    if (data.serialize(data.getId()) == null) return helper.getErrorMsg("option");
                    for (Map.Entry<String, String> entry: data.serialize(data.getId()).entrySet()){
                        helper.setVariable(entry.getKey(), entry.getValue());
                        helper.setVariable(data.getId() + COUNT, String.valueOf(++count));
                    }
                }
                else
                {
                    helper.setVariable(data.getId() + COUNT, String.valueOf(0));
                }
            }
        }

        String qwe2[] = {"add.authen", "add.author", "add.mapping"};

        for (String q: qwe2 )
        {
            ArrayList<String> temp = new ArrayList<String>();
            int count = 0;
            try
            {
                count = Integer.parseInt(helper.getVariable(q+".count"));
            } catch (Exception e) {  }
            for (int i=0; i< count/SecurityDomainConstants.NUM_DYNAMIC_COL; i++)
            {
                String code = helper.getVariable(q +".left."+i);
                if(temp.contains(code))
                {
                    return helper.getErrorMsg(q + ".duplicate.code");
                }
                else
                {
                    temp.add(code);
                }
            }
        }

            return "";
    }

    class SecurityDomain extends JPanel implements ActionListener
    {
        private static final long serialVersionUID = 1L;
        private  HashMap<String, ArrayList<Component>> modules = new HashMap< String, ArrayList<Component>>();

        public SecurityDomain(InstallData idata, boolean isOpaque, boolean isDisplayingHidden) throws SecurityException, NoSuchMethodException,
                IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
        {
            initializeGUIData(modules);

            //TODO: Seems general enought to move out
            GridBagLayout gridBagLayout = new GridBagLayout();
            gridBagLayout.rowHeights    = new int[]    { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
            gridBagLayout.columnWeights = new double[] { 1.0 };
            gridBagLayout.rowWeights    = new double[] {
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0 };
            setLayout(gridBagLayout);
            setOpaque(isOpaque);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill   = GridBagConstraints.BOTH;

            //TODO: Also seems general, all panels have a main text
            JTextArea infoLabel = LabelFactory.createMultilineLabel(
                    idata.langpack.getString(MAIN_TEXT), isOpaque);
            gbc.gridy++;
            add(infoLabel, gbc);

            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            for (Map.Entry<String[], Class<?>> content : UI_ELEMENTS.entrySet())
            {
                String[] info   = content.getKey();
                String id    = info[0];
                String[] values = Arrays.copyOfRange(info,1 , info.length );
                Class component = content.getValue();
                
                GenericInformationJPanel name = new GenericInformationJPanel(component,
                        helper.getLabel(id), values);
                name.getInfoComponent().setToolTipText(helper.getToolTip(id));
                name.addActionListener(this);
                name.setName(id);
                name.setInfoName(id);
                gbc.gridy+=1;
                name.setColumns(40);
                add(name, gbc);
                if (id.startsWith("add"))
                { //TODO: Make some sort of dependency model so we don't have to rely on 'add'
                    ArrayList<Component> module = modules.get(id);
                    module.add(createModulePanel(id));
                    module.get(0).setVisible(false);
                    gbc.gridy+=1;
                    add(module.get(0), gbc);
                }
            }

        }

        /** Initialize your modules **/
        private void initializeGUIData(HashMap<String, ArrayList<Component>> modules)
        {   //TODO: Should be centralized for the SecuriyDomainPanel
            modules.put(AUTHENTICATION_ID, new ArrayList<Component>());
            modules.put(AUTHORIZATION_ID, new ArrayList<Component>());
            modules.put(MAPPING_ID, new ArrayList<Component>());
        }
        
        /** Logic to switch on/off modules */
        private void switchModule(String id, String value)
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            for (Component comp : modules.get(id))
            {
                if (isDisplayingHidden())
                {
                    comp.setVisible(true);
                    if(value.equals("Yes")) comp.setEnabled(true);
                    else                    comp.setEnabled(false);
                }
                else
                {
                    if(value.equals("Yes")) comp.setVisible(true);
                    else                    comp.setVisible(false);
                }
            }
            validate();
            repaint();
        }

        
        private JPanel createModulePanel(String id)
        {
            //1. Generate headers for the dynamic fields
            JPanel moduleHeader = new JPanel(new GridLayout(1, 1));
            for (String suffix : DYNAMIC_HEADERS)
            {
                String headerID = id+suffix;
                JLabel xaExtraPropertiesHeaderLbl = new JLabel(helper.getLabel(headerID));
                xaExtraPropertiesHeaderLbl.setOpaque(false);
                moduleHeader.add(xaExtraPropertiesHeaderLbl);
            }
            moduleHeader.setOpaque(false);
            
            //2. Create dynamic component panel
            int numberOfFields = DYNAMIC_MAP.get(id);
            DynamicComponentsPanel moduleLocal = new DynamicComponentsPanel(SecurityDomainModule.class, numberOfFields, 0,
                    moduleHeader, helper.getToolTip(id));
            moduleLocal.setAddButtonText(helper.getLabel("add.button"));
            moduleLocal.setRemoveButtonText(helper.getLabel("remove.button"));
            moduleLocal.setId(id);
            moduleLocal.initializeDefaults(new String[][] {{id, DEFAULT_OPERATION}});

            return moduleLocal;
        }

        
        public void actionPerformed(ActionEvent e)
        {
            if (e.getSource() instanceof JComboBox)
            {
                String id = ((JComboBox) e.getSource()).getName();
                String value = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if(id == null) throw new IllegalArgumentException("ID is Null");
                if(id.startsWith("add")) switchModule(id, value);
            }
        }
    }
}
