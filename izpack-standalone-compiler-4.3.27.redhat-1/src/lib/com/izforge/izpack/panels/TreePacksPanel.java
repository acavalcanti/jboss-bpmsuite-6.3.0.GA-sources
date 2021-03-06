/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Vladimir Ralev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleStateSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Pack;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.ScrollPaneFactory;
import com.izforge.izpack.installer.Debugger;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.WebAccessor;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.VariableSubstitutor;

public class TreePacksPanel extends IzPanel implements PacksPanelInterface, TreeSelectionListener
{
    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 5684716698930628262L;

    // Common used Swing fields
    /**
     * The free space label.
     */
    protected JLabel freeSpaceLabel;

    /**
     * The space label.
     */
    protected JLabel spaceLabel;

    /**
     * The tip label.
     */
    protected JTextArea descriptionArea;

    /**
     * The dependencies label.
     */
    protected JTextArea dependencyArea;

    /**
     * The packs tree.
     */
    protected JTree packsTree;

    /**
     * The packs model.
     */
    protected PacksModel packsModel;

    /**
     * The tablescroll.
     */
    protected JScrollPane tableScroller;

    // Non-GUI fields
    /**
     * Map that connects names with pack objects
     */
    private Map<String, Pack> names;

    /**
     * The bytes of the current pack.
     */
    protected long bytes = 0;

    /**
     * The free bytes of the current selected disk.
     */
    protected long freeBytes = 0;

    /**
     * Are there dependencies in the packs
     */
    protected boolean dependenciesExist = false;

    /**
     * The packs locale database.
     */
    private LocaleDatabase langpack = null;

    /**
     * The name of the XML file that specifies the panel langpack
     */
    private static final String LANG_FILE_NAME = "packsLang.xml";

    private HashMap<String, Pack> idToPack;
    private HashMap<String, ArrayList<String>> treeData;
    private HashMap<Pack, Integer> packToRowNumber;

    private HashMap<String, CheckBoxNode> idToCheckBoxNode = new HashMap<String, CheckBoxNode>();
    //private boolean created = false;   // UNUSED

    private CheckTreeController checkTreeController;
    
    boolean doNotShowPackSize;
    /**
     * The constructor.
     *
     * @param parent The parent window.
     * @param idata  The installation data.
     */
    public TreePacksPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata);
        // Load langpack.
        try {
            packsModel = new PacksModel(this, idata, this.parent.getRules())
            {
                /** Required (serializable) */
                private static final long serialVersionUID = 697462278279845304L;

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return false;
                }
            };
            this.langpack = parent.langpack;
            InputStream langPackStream = null;
            String webdir = idata.info.getWebDirURL();
            if (webdir != null) {
                try {
                    java.net.URL url = new java.net.URL(webdir + "/langpacks/" + LANG_FILE_NAME + idata.localeISO3);
                    langPackStream = new WebAccessor(null).openInputStream(url);
                }
                catch (Exception e) {
                    // just ignore this. we use the fallback below
                }
            }
            
            if(langPackStream == null) {
                langPackStream = ResourceManager.getInstance().getInputStream(LANG_FILE_NAME);
            }

            this.langpack.add(langPackStream);
            langPackStream.close();
        }
        catch (Throwable exception) {
            Debug.trace(exception);
        }

        doNotShowPackSize = Boolean.parseBoolean(idata.guiPrefs.modifier.get("doNotShowPackSizeColumn"));
        
        // init the map
        computePacks(idata.availablePacks);

    }

    /**
     * The Implementation of this method should create the layout for the current class.
     */

    protected void createNormalLayout()
    {
        this.removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createLabel("PacksPanel.info", "preferences", null, null);
        add(Box.createRigidArea(new Dimension(0, 3)));
        createLabel("PacksPanel.tip", "tip", null, null);
        add(Box.createRigidArea(new Dimension(0, 5)));
        tableScroller = ScrollPaneFactory.createScroller();
        packsTree = createPacksTree(300, tableScroller, null, null);
        AccessibleContext ac = packsTree.getAccessibleContext();
        ac.setAccessibleDescription("This JTree holds checkboxes indicating that a given set of files should be installed.");
        ac.setAccessibleName("JTree holding sets of files");
        if (dependenciesExist)
        {
            dependencyArea = createTextArea("PacksPanel.dependencyList", null, null, null);
        }
        descriptionArea = createTextArea("PacksPanel.description", null, null, null);
        spaceLabel = createPanelWithLabel("PacksPanel.space", null, null);
        if (IoHelper.supported("getFreeSpace"))
        {
            add(Box.createRigidArea(new Dimension(0, 3)));
            freeSpaceLabel = createPanelWithLabel("PacksPanel.freespace", null, null);
        }
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#getLangpack()
    */
    public LocaleDatabase getLangpack()
    {
        return (langpack);
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#getBytes()
    */
    public long getBytes()
    {
        return (bytes);
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#setBytes(int)
    */
    public void setBytes(long bytes)
    {
        this.bytes = bytes;
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#showSpaceRequired()
    */
    public void showSpaceRequired()
    {
        if (spaceLabel != null) //Set the new byte size
        {
            spaceLabel.setText(Pack.toByteUnitsString(bytes));
        }
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.panels.PacksPanelInterface#showFreeSpace()
    */
    public void showFreeSpace()
    {
        if (IoHelper.supported("getFreeSpace") && freeSpaceLabel != null)
        {
            String msg = null;
            File parentDir = IoHelper.existingParent(new File(idata.getInstallPath()));
            if (parentDir == null)
            {
                freeBytes = -1;
            }
            else
            {
                freeBytes = IoHelper.getFreeSpace(parentDir.getAbsolutePath());
            }
            if (freeBytes < 0)
            {
                msg = parent.langpack.getString("PacksPanel.notAscertainable");
            }
            else
            {
                msg = Pack.toByteUnitsString(freeBytes);
            }
            freeSpaceLabel.setText(msg);
        }
    }

    public Debugger getDebugger()
    {
        return null;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the needed space is less than the free space, else false
     */
    public boolean isValidated()
    {
        //refreshPacksToInstall();
        

        
        if (IoHelper.supported("getFreeSpace") && freeBytes >= 0 && freeBytes <= bytes)
        {
            JOptionPane.showMessageDialog(this, parent.langpack
                    .getString("PacksPanel.notEnoughSpace"), parent.langpack
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
            return (false);
        }
        return (true);
    }

    /**
     * Asks to make the XML panel data.
     *
     * @param panelRoot The XML tree to write the data in.
     */
    public void makeXMLData(IXMLElement panelRoot)
    {
        new ImgPacksPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }


    /**
     * This method tries to resolve the localized name of the given pack. If this is not possible,
     * the name given in the installation description file in ELEMENT <pack> will be used.
     *
     * @param pack for which the name should be resolved
     * @return localized name of the pack
     */
    private String getI18NPackName(Pack pack)
    {
        // Internationalization code
        String packName = pack.name;
        String key = pack.id;
        if (langpack != null && pack.id != null && !"".equals(pack.id))
        {
            packName = langpack.getString(key);
        }
        if ("".equals(packName) || key == null || key.equals(packName))
        {
            packName = pack.name;
        }
        return (packName);
    }

    public String getI18NPackName(String packId)
    {
        Pack pack = idToPack.get(packId);
        if (pack == null)
        {
            return packId;
        }
        return getI18NPackName(pack);
    }

    /**
     * Layout helper method:<br>
     * Creates an label with a message given by msgId and an icon given by the iconId. If layout and
     * constraints are not null, the label will be added to layout with the given constraints. The
     * label will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param iconId      identifier for the IzPack icons
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created label
     */
    protected JLabel createLabel(String msgId, String iconId, GridBagLayout layout,
                                 GridBagConstraints constraints)
    {
        JLabel label = LabelFactory.create(parent.langpack.getString(msgId), parent.icons
                .getImageIcon(iconId), TRAILING);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(label, constraints);
        }
        add(label);
        return (label);
    }

    /**
     * Creates a panel containing a anonymous label on the left with the message for the given msgId
     * and a label on the right side with initial no text. The right label will be returned. If
     * layout and constraints are not null, the label will be added to layout with the given
     * constraints. The panel will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created (right) label
     */
    protected JLabel createPanelWithLabel(String msgId, GridBagLayout layout,
                                          GridBagConstraints constraints)
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        if (parent.hasBackground) panel.setOpaque(false);
        if (parent.hasBackground) label.setOpaque(false);
        if (label == null)
        {
            label = new JLabel("");
        }
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(LabelFactory.create(parent.langpack.getString(msgId)));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(panel, constraints);
        }
        add(panel);
        return (label);
    }

    private void refreshPacksToInstall()
    {
        idata.selectedPacks.clear();
        CheckBoxNode cbn = (CheckBoxNode) getTree().getModel().getRoot();
        Enumeration e = cbn.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            CheckBoxNode c = (CheckBoxNode) e.nextElement();
            if (c.isSelected() || c.isPartial())
            {
                idata.selectedPacks.add(c.getPack());
            }
        }
    }

    /**
     * Creates a text area with standard settings and the title given by the msgId. If scroller is
     * not null, the create text area will be added to the scroller and the scroller to this object,
     * else the text area will be added directly to this object. If layout and constraints are not
     * null, the text area or scroller will be added to layout with the given constraints. The text
     * area will be returned.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param scroller    the scroller to be used
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created text area
     */
    protected JTextArea createTextArea(String msgId, JScrollPane scroller, GridBagLayout layout,
                                       GridBagConstraints constraints)
    {
        JTextArea area = new JTextArea();
        // area.setMargin(new Insets(2, 2, 2, 2));
        area.setAlignmentX(LEFT_ALIGNMENT);
        area.setCaretPosition(0);
        area.setEditable(false);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createTitledBorder(parent.langpack.getString(msgId)));
        area.setFont(getControlTextFont());

        if (layout != null && constraints != null)
        {
            if (scroller != null)
            {
                layout.addLayoutComponent(scroller, constraints);
            }
            else
            {
                layout.addLayoutComponent(area, constraints);
            }
        }
        if (scroller != null)
        {
            scroller.setViewportView(area);
            add(scroller);
        }
        else
        {
            add(area);
        }
        // Hack to make JTree appear correctly - dcheung
        area.setPreferredSize(new Dimension(500, 10));
        return (area);

    }

    /**
     * FIXME Creates the JTree component and calls all initialization tasks
     *
     * @param width
     * @param scroller
     * @param layout
     * @param constraints
     * @return
     */
    protected JTree createPacksTree(int width, JScrollPane scroller, GridBagLayout layout,
                                    GridBagConstraints constraints)
    {
        JTree tree = new JTree((CheckBoxNode) populateTreePacks(null));
        packsTree = tree;
        tree.setCellRenderer(new CheckBoxNodeRenderer(this, !doNotShowPackSize, idata));
        tree.setEditable(false);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        checkTreeController = new CheckTreeController(this);
        tree.addMouseListener(checkTreeController);
        tree.addTreeSelectionListener(this);
        tree.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        tree.setBackground(Color.white);
        tree.setToggleClickCount(0);
        //tree.setRowHeight(0);

        //table.getSelectionModel().addTreeSelectionListener(this);
        scroller.setViewportView(tree);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        scroller.getViewport().setBackground(Color.white);
        scroller.setPreferredSize(new Dimension(width, (idata.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(scroller, constraints);
        }
        add(scroller);
        //expandNodes(tree);
        return (tree);
    }

   /* private void expandNodes(JTree tree){
        for(int i=0;i<tree.getRowCount();i++)  
        {  
            tree.expandRow(i);  
        }  
    }*/

    /**
     * Computes pack related data like the names or the dependencies state.
     *
     * @param packs
     */
    private void computePacks(List packs)
    {
        names = new HashMap<String, Pack>();
        dependenciesExist = false;
        for (Object pack1 : packs)
        {
            Pack pack = (Pack) pack1;
            names.put(pack.name, pack);
            if (pack.dependencies != null || pack.excludeGroup != null)
            {
                dependenciesExist = true;
            }
        }
    }

    /**
     * Refresh tree data from the PacksModel. This functions serves as a bridge
     * between the flat PacksModel and the tree data model.
     */
    public void fromModel()
    {
        TreeModel model = this.packsTree.getModel();
        CheckBoxNode root = (CheckBoxNode) model.getRoot();
        updateModel(root);
    }

    private int getRowIndex(Pack pack)
    {
        Object o = packToRowNumber.get(pack);
        if (o == null)
        {
            return -1;
        }
        Integer ret = (Integer) o;
        return ret;
    }

    /**
     * Helper function for fromModel() - runs the recursion
     *
     * @param rnode
     */
    private void updateModel(CheckBoxNode rnode)
    {
        int rowIndex = getRowIndex(rnode.getPack());
        if (rowIndex >= 0)
        {
            Integer state = (Integer) packsModel.getValueAt(rowIndex, 0);
            if ((state == -2) && rnode.getChildCount() > 0)
            {
                boolean dirty = false;
                Enumeration toBeDeselected = rnode.depthFirstEnumeration();
                while (toBeDeselected.hasMoreElements())
                {
                    CheckBoxNode cbn = (CheckBoxNode) toBeDeselected.nextElement();
                    boolean chDirty = cbn.isSelected() || cbn.isPartial() || cbn.isEnabled();
                    dirty = dirty || chDirty;
                    if (chDirty)
                    {
                        cbn.setPartial(false);
                        cbn.setSelected(false);
                        cbn.setEnabled(false);
                        setModelValue(cbn);
                    }
                }
                if (dirty)
                {
                    fromModel();
                }
                return;
            }
        }

        Enumeration e = rnode.children();
        while (e.hasMoreElements())
        {
            Object next = e.nextElement();
            CheckBoxNode cbnode = (CheckBoxNode) next;
            String nodeText = cbnode.getId();
            Object nodePack = idToPack.get(nodeText);
            if (!cbnode.isPartial())
            {
                int childRowIndex = getRowIndex((Pack) nodePack);
                if (childRowIndex >= 0)
                {
                    Integer state = (Integer) packsModel.getValueAt(childRowIndex, 0);
                    cbnode.setEnabled(state >= 0);
                    cbnode.setSelected(Math.abs(state.intValue()) == 1);
                }
            }
            updateModel(cbnode);
        }
    }

    /**
     * Updates a value for pack in PacksModel with data from a checkbox node
     *
     * @param cbnode This is the checkbox node which contains model values
     */
    public void setModelValue(CheckBoxNode cbnode)
    {
        String id = cbnode.getId();
        Object nodePack = idToPack.get(id);
        int value = 0;
        if (cbnode.isEnabled() && cbnode.isSelected())
        {
            value = 1; // User-selectable and selected.
        }
        if (!cbnode.isEnabled() && cbnode.isSelected())
        {
            value = -1; // Not user-selectable, but selected.
        }
        if (!cbnode.isEnabled() && !cbnode.isSelected())
        {
            value = -2; // Neither use-selectable, nor selected.
        }
        int rowIndex = getRowIndex((Pack) nodePack);
        if (rowIndex >= 0)
        {
            Integer newValue = value;
            Integer modelValue = (Integer) packsModel.getValueAt(rowIndex, 0);
            if (!newValue.equals(modelValue))
            {
                packsModel.setValueAt(newValue, rowIndex, 0);
            }
        }
    }

    /**
     * Initialize tree model sructures
     */
    private void createTreeData()
    {
        treeData = new HashMap<String, ArrayList<String>>();
        idToPack = new HashMap<String, Pack>();

        java.util.Iterator iter = idata.availablePacks.iterator();
        while (iter.hasNext())
        {
            Pack p = (Pack) iter.next();
            idToPack.put(p.id, p);
            if (p.parent != null)
            {
                ArrayList<String> kids = null;
                if (treeData.containsKey(p.parent))
                {
                    kids = treeData.get(p.parent);
                }
                else
                {
                    kids = new ArrayList<String>();
                }
                kids.add(p.id);
                treeData.put(p.parent, kids);
            }
        }
    }

    /**
     * Shows and updates the description text in the panel
     *
     * @param id
     */
    public void setDescription(String id)
    {
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
        if (descriptionArea != null)
        {
            Pack pack = idToPack.get(id);
            String desc = "";
            String key = pack.id + ".description";
            if (langpack != null && pack.id != null && !"".equals(pack.id))
            {
                desc = langpack.getString(key);
            }
            if ("".equals(desc) || key.equals(desc))
            {
                desc = pack.description;
            }
            desc = vs.substitute(desc, null);
            descriptionArea.setText(desc);
        }
    }

    /**
     * Shows and updates the dependencies text in the panel
     *
     * @param id
     */
    public void setDependencies(String id)
    {
        if (dependencyArea != null)
        {
            Pack pack = idToPack.get(id);
            List<String> dep = pack.dependencies;
            String list = "";
            if (dep != null)
            {
                list += (langpack == null) ? "Dependencies: " : langpack
                        .getString("PacksPanel.dependencies");
            }
            for (int j = 0; dep != null && j < dep.size(); j++)
            {
                String name = dep.get(j);
                list += getI18NPackName(names.get(name));
                if (j != dep.size() - 1)
                {
                    list += ", ";
                }
            }

            // add the list of the packs to be excluded
            String excludeslist = (langpack == null) ? "Excludes: " : langpack
                    .getString("PacksPanel.excludes");
            int numexcludes = 0;
            int i = getRowIndex(pack);
            if (pack.excludeGroup != null)
            {
                for (int q = 0; q < idata.availablePacks.size(); q++)
                {
                    Pack otherpack = (Pack) idata.availablePacks.get(q);
                    String exgroup = otherpack.excludeGroup;
                    if (exgroup != null)
                    {
                        if (q != i && pack.excludeGroup.equals(exgroup))
                        {

                            excludeslist += getI18NPackName(otherpack) + ", ";
                            numexcludes++;
                        }
                    }
                }
            }
            // concatenate
            if (dep != null)
            {
                excludeslist = "    " + excludeslist;
            }
            if (numexcludes > 0)
            {
                list += excludeslist;
            }
            if (list.endsWith(", "))
            {
                list = list.substring(0, list.length() - 2);
            }

            // and display the result
            dependencyArea.setText(list);
        }
    }

    /**
     * Gives a CheckBoxNode instance from the id
     *
     * @param id
     * @return
     */
    public CheckBoxNode getCbnById(String id)
    {
        return this.idToCheckBoxNode.get(id);
    }

    /**
     * Reads the available packs and creates the JTree structure based on
     * the parent definitions.
     *
     * @param parent
     * @return
     */
    private Object populateTreePacks(String parent)
    {
        if (parent == null) // the root node
        {
            java.util.Iterator iter = idata.availablePacks.iterator();
            ArrayList rootNodes = new ArrayList();
            while (iter.hasNext())
            {
                Pack p = (Pack) iter.next();
                if (p.parent == null && !p.isHidden())
                {
                    rootNodes.add(populateTreePacks(p.id));
                }
            }
            TreeNode nv = new CheckBoxNode("Root", "Root", rootNodes.toArray(), true);
            return nv;
        }
        else
        {
            ArrayList links = new ArrayList();
            Object kidsObject = treeData.get(parent);
            Pack p = idToPack.get(parent);
            String translated = getI18NPackName(parent);

            if (kidsObject != null)
            {
                ArrayList kids = (ArrayList) kidsObject;
                for (Object kid : kids)
                {
                    String kidId = (String) kid;
                    links.add(populateTreePacks(kidId));
                }

                CheckBoxNode cbn = new CheckBoxNode(parent, translated, links.toArray(), true);
                AccessibleContext ac = cbn.getAccessibleContext();
                ac.setAccessibleDescription("Indicates the pack given by "+cbn.getId()+" should be installed");
                idToCheckBoxNode.put(cbn.getId(), cbn);
                cbn.setPack(p);
                cbn.setTotalSize(p.getSize());
                return cbn;
            }
            else
            {
                CheckBoxNode cbn = new CheckBoxNode(parent, translated, true);
                AccessibleContext ac = cbn.getAccessibleContext();
                ac.setAccessibleDescription("Indicates the pack given by "+cbn.getId()+" should be installed");
                idToCheckBoxNode.put(cbn.getId(), cbn);
                cbn.setPack(p);
                cbn.setTotalSize(p.getSize());
                return cbn;
            }
        }
    }

    /**
     * Called when the panel becomes active. If a derived class implements this method also, it is
     * recomanded to call this method with the super operator first.
     */
    public void panelActivate() {
        try
        {
            super.panelActivate();
            // TODO the PacksModel could be patched such that isCellEditable
            // allows returns false. In that case the PacksModel must not be
            // adapted here.
           
            // we call this directly here, to update packs that have conditions that may have changed between viewing this 
            // panel multiple times
            packsModel.updateConditions(true);
            //initialize helper map to increa performance
            packToRowNumber = new HashMap<Pack, Integer>();
            java.util.Iterator rowpack = idata.availablePacks.iterator();
            int index = 0;
            while (rowpack.hasNext())
            {
                Pack p = (Pack) rowpack.next();
                if (!p.isHidden())
                    packToRowNumber.put(p, index++);
            }

            // Init tree structures
            createTreeData();

            // Create panel GUI (and populate the TJtree)
            createNormalLayout();

            // Reload the data from the PacksModel into the tree in order the initial
            // dependencies to be resolved and effective
            fromModel();

            // Init the pack sizes (individual and cumulative)
            CheckBoxNode root = (CheckBoxNode) packsTree.getModel().getRoot();
            checkTreeController.updateAllParents(root);
            CheckTreeController.setOriginalSize(root);

            // Total size needed is the total size of the tree rooted at root:
            bytes = CheckTreeController.initTotalSize(root, true);
            

            // Ugly repaint because of a bug in tree.treeDidChange
            packsTree.revalidate();
            packsTree.repaint();

            tableScroller.setColumnHeaderView(null);
            tableScroller.setColumnHeader(null);

            // set the JCheckBoxes to the currently selected panels. The
            // selection might have changed in another panel
            // We are already calculating the total size above. No need
            // to duplicate here.
//            java.util.Iterator iter = idata.availablePacks.iterator();
//            bytes = 0;
//            while (iter.hasNext())
//            {
//                Pack p = (Pack) iter.next();
//
//                if (idata.selectedPacks.contains(p))
//                {
//                    bytes += p.getSize();
//                }
//            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        showSpaceRequired();
        showFreeSpace();
    }

    /*
    * (non-Javadoc)
    * 
    * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
    */
    public String getSummaryBody()
    {
        StringBuffer retval = new StringBuffer(256);
        Iterator iter = idata.selectedPacks.iterator();
        boolean first = true;
        while (iter.hasNext())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            Pack pack = (Pack) iter.next();
            retval.append(getI18NPackName(pack));
        }
        return retval.toString();
    }


    public JTree getTree()
    {
        return packsTree;
    }

    public void valueChanged(TreeSelectionEvent arg0)
    {
        setDescription(((CheckBoxNode) arg0.getPath().getLastPathComponent()).getId());
    }

}

/**
 * The renderer model for individual checkbox nodes in a JTree. It renders the
 * checkbox and a label for the pack size.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckBoxNodeRenderer implements TreeCellRenderer
{
    private static final JPanel rendererPanel = new JPanel();
    private static final JLabel packDescriptionLabel = new JLabel();
    private static final JLabel packStatusLabel = new JLabel();
    private static final JLabel packSizeLabel = new JLabel();
    private static final JCheckBox checkbox = new JCheckBox();
    private static final JCheckBox normalCheckBox = new JCheckBox();
    private static final java.awt.Font normalFont = new JCheckBox().getFont();
    private static final java.awt.Font boldFont = new java.awt.Font(normalFont.getFontName(),
            java.awt.Font.BOLD,
            normalFont.getSize());
    private static final java.awt.Font plainFont = new java.awt.Font(normalFont.getFontName(),
            java.awt.Font.PLAIN,
            normalFont.getSize());
    private static final Color annotationColor = new Color(0, 0, 120);
    private static final Color changedColor = new Color(200, 0, 0);

    private static Color selectionForeground, selectionBackground,
            textForeground, textBackground;

    TreePacksPanel treePacksPanel;

    private boolean showPackSize;
    
    public CheckBoxNodeRenderer(TreePacksPanel t, boolean showPackSize, InstallData idata)
    {
        this.showPackSize = showPackSize;
        
        selectionForeground = UIManager.getColor("Tree.selectionForeground");
        selectionBackground = UIManager.getColor("Tree.selectionBackground");
        textForeground = UIManager.getColor("Tree.textForeground");
        textBackground = UIManager.getColor("Tree.textBackground");
        treePacksPanel = t;

        int treeWidth = t.getTree().getPreferredSize().width;
        int height = checkbox.getPreferredSize().height;
        int cellWidth = idata.guiPrefs.width - idata.guiPrefs.width / 4 - 30;

        //Don't touch, it fixes various layout bugs in swing/awt
        rendererPanel.setLayout(new java.awt.BorderLayout(0, 0));
        rendererPanel.setBackground(textBackground);
        rendererPanel.add(java.awt.BorderLayout.WEST, checkbox);

        if (showPackSize) {
            rendererPanel.setAlignmentX((float) 0);
            rendererPanel.setAlignmentY((float) 0);
            //JPanel sketch = new JPanel();
            //sketch.setLayout(new java.awt.GridBagLayout());
            //sketch.add(packDescriptionLabel);
            //sketch.add(packStatusLabel);
            //rendererPanel.add(java.awt.BorderLayout.CENTER, sketch);
            // rendererPanel.add(java.awt.BorderLayout.EAST, packStatusLabel);
            rendererPanel.add(java.awt.BorderLayout.EAST, packSizeLabel);
        }
        rendererPanel.setMinimumSize(new Dimension(cellWidth, height));
        rendererPanel.setPreferredSize(new Dimension(cellWidth, height));
        rendererPanel.setSize(new Dimension(cellWidth, height));

        rendererPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row,
                                                  boolean hasFocus)
    {
        treePacksPanel.fromModel();

        if (selected)
        {
            checkbox.setForeground(selectionForeground);
            checkbox.setBackground(selectionBackground);
            rendererPanel.setForeground(selectionForeground);
            rendererPanel.setBackground(selectionBackground);
            packDescriptionLabel.setBackground(selectionBackground);
            packStatusLabel.setBackground(selectionBackground);
            packSizeLabel.setBackground(selectionBackground);
        }
        else
        {
            checkbox.setForeground(textForeground);
            checkbox.setBackground(textBackground);
            rendererPanel.setForeground(textForeground);
            rendererPanel.setBackground(textBackground);
            packDescriptionLabel.setBackground(textBackground);
            packStatusLabel.setBackground(textBackground);
            packSizeLabel.setBackground(textBackground);
        }

        if ((value != null) && (value instanceof CheckBoxNode))
        {
            CheckBoxNode node = (CheckBoxNode) value;
            
            if (node.isTotalSizeChanged()) // Change to check if node selected
            {
                packSizeLabel.setForeground(changedColor); //THIS IS RED
            }
            else
            {
                if (selected)
                {
                	packDescriptionLabel.setForeground(selectionForeground);
                	packStatusLabel.setForeground(selectionForeground);
                    packSizeLabel.setForeground(selectionForeground);
                }
                else
                {
                	packDescriptionLabel.setForeground(annotationColor); //THESE ARE ALL BLUE
                	packStatusLabel.setForeground(annotationColor);
                    packSizeLabel.setForeground(annotationColor);
                }
            }

            checkbox.setText(node.getTranslatedText());
            
            packDescriptionLabel.setText("DESCRIPTION");
            packStatusLabel.setText("To Be Installed");

            if (showPackSize && (node.getTotalSize() > 0)) { //Happens here! Yeah colour changer here
                packSizeLabel.setText(Pack.toByteUnitsString(node.getTotalSize()));
            } else {
                /**
                 * If a package is of size 0, then it's a 'placeholder' package that doesn't
                 * contain children within its own tree. We don't show it's size since it's 0,
                 * but we still need to display it because it's used to select packages that
                 * are children of other packages.
                 */
                packSizeLabel.setText("");
            }
            

            if (node.isPartial())
            {
                checkbox.setSelected(false);
            }
            else
            {
                checkbox.setSelected(node.isSelected());
            }

            // Packs can now be user-selectable (or not), so enable their UI
            // elements only if this is the case.
            checkbox.setEnabled(node.isEnabled() && node.getPack().selectable);
            packSizeLabel.setEnabled(node.isEnabled() && node.getPack().selectable);

            if (node.getChildCount() > 0) //Setting Fonts?
            {
                checkbox.setFont(boldFont);
                packSizeLabel.setFont(boldFont);
            }
            else
            {
                checkbox.setFont(normalFont);
                packSizeLabel.setFont(plainFont);
            }

            if (node.isPartial())
            {
                checkbox.setIcon(new PartialIcon());
            }
            else
            {
                checkbox.setIcon(normalCheckBox.getIcon());
            }
        }
        return rendererPanel;
    }

    public Component getCheckRenderer()
    {
        return rendererPanel;
    }

}

/**
 * The model structure for a JTree node.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckBoxNode extends DefaultMutableTreeNode implements Accessible
{

    /**
     * Required (serializable)
     */
    private static final long serialVersionUID = 8743154051564336973L;
    String id;
    boolean selected;
    boolean partial;
    boolean enabled;
    boolean totalSizeChanged;
    String translatedText;
    Pack pack;
    long totalSize;
    long originalSize;
    String description;
    String status;
    AccessibleContext ac;

    public CheckBoxNode(String id, String translated, boolean selected) {
        this.id = id;
        this.selected = selected;
        this.translatedText = translated;
        ac = new CheckBoxNodeAccessibleContext(getId());
    }

    public CheckBoxNode(String id, String translated, Object elements[], boolean selected) {
        this.id = id;
        this.translatedText = translated;
        for (int i = 0, n = elements.length; i < n; i++)
        {
            CheckBoxNode tn = (CheckBoxNode) elements[i];
            add(tn);
        }
        ac = new CheckBoxNodeAccessibleContext(getId());
    }

    public boolean isLeaf()
    {
        return this.getChildCount() == 0;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean newValue)
    {
        selected = newValue;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String newValue)
    {
        id = newValue;
    }

    public String toString()
    {
        return getClass().getName() + "[" + id + "/" + selected + "]";
    }

    public boolean isPartial()
    {
        return partial;
    }

    public void setPartial(boolean partial)
    {
        this.partial = partial;
        if (partial)
        {
            setSelected(true);
        }
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTranslatedText()
    {
        return translatedText;
    }

    public void setTranslatedText(String translatedText)
    {
        this.translatedText = translatedText;
    }

    public Pack getPack() {
        return pack;
    }

    public void setPack(Pack pack) {
        this.pack = pack;
    }

    public long getTotalSize()
    {
        return totalSize;
    }

    public void setTotalSize(long totalSize)
    {
        this.totalSize = totalSize;
    }

    public boolean isTotalSizeChanged() {
        return totalSizeChanged;
    }
    
    public void setOriginalSize(long originalSize)
    {
        this.originalSize = originalSize;
    }

    public long getOriginalSize() {
        return this.originalSize;
    }

    public void setTotalSizeChanged(boolean totalSizeChanged)
    {
        this.totalSizeChanged = totalSizeChanged;
    }

    public AccessibleContext getAccessibleContext()
    {
        return ac;
    }
}

class CheckBoxNodeAccessibleContext extends AccessibleContext
{
    AccessibleRole role;
    /**
     * Create accessibleContext with default values
     */
    public CheckBoxNodeAccessibleContext(String name){
        setAccessibleName(name);
        setAccessibleRole(AccessibleRole.CHECK_BOX);
    }
    @Override
    public Accessible getAccessibleChild(int i)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getAccessibleChildrenCount()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getAccessibleIndexInParent()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public AccessibleRole getAccessibleRole()
    {
        // TODO Auto-generated method stub
        return role;
    }
    
    public void setAccessibleRole(AccessibleRole ar){
        role = ar;
    }

    @Override
    public AccessibleStateSet getAccessibleStateSet()
    {
        return null; 
    }

    @Override
    public Locale getLocale() throws IllegalComponentStateException
    {
        // TODO Auto-generated method stub
        return null;
    }

}


/**
 * Special checkbox icon which shows partially selected nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class PartialIcon implements Icon
{
    protected int getControlSize()
    {
        return 13;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        int controlSize = getControlSize();
        g.setColor(MetalLookAndFeel.getControlShadow());
        g.fillRect(x, y, controlSize - 1, controlSize - 1);
        drawBorder(g, x, y, controlSize, controlSize);

        g.setColor(Color.green);
        drawCheck(c, g, x, y);
    }

    private void drawBorder(Graphics g, int x, int y, int w, int h)
    {
        g.translate(x, y);

        // outer frame rectangle
        g.setColor(MetalLookAndFeel.getControlDarkShadow());
        g.setColor(new Color(0.4f, 0.4f, 0.4f));
        g.drawRect(0, 0, w - 2, h - 2);

        // middle frame
        g.setColor(MetalLookAndFeel.getControlHighlight());
        g.setColor(new Color(0.6f, 0.6f, 0.6f));
        g.drawRect(1, 1, w - 2, h - 2);

        // background
        g.setColor(new Color(0.99f, 0.99f, 0.99f));
        g.fillRect(2, 2, w - 3, h - 3);

        //some extra lines for FX
        g.setColor(MetalLookAndFeel.getControl());
        g.drawLine(0, h - 1, 1, h - 2);
        g.drawLine(w - 1, 0, w - 2, 1);
        g.translate(-x, -y);
    }

    protected void drawCheck(Component c, Graphics g, int x, int y)
    {
        int controlSize = getControlSize();
        g.setColor(new Color(0.0f, 0.7f, 0.0f));

        g.fillOval(x + controlSize / 2 - 2, y + controlSize / 2 - 2, 6, 6);
    }

    public int getIconWidth()
    {
        return getControlSize();
    }

    public int getIconHeight()
    {
        return getControlSize();
    }
}

/**
 * Controller class which handles the mouse clicks on checkbox nodes. Also
 * contains utility methods to update the sizes and the states of the nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckTreeController extends MouseAdapter
{
    JTree tree;
    TreePacksPanel treePacksPanel;
    int checkWidth = new JCheckBox().getPreferredSize().width;

    public CheckTreeController(TreePacksPanel p)
    {
        this.tree = p.getTree();
        this.treePacksPanel = p;
    }

    private void selectNode(CheckBoxNode current)
    {
        current.setPartial(false);
        treePacksPanel.setModelValue(current);
        Enumeration e = current.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            CheckBoxNode child = (CheckBoxNode) e.nextElement();
            child.setSelected(current.isSelected() || child.getPack().required);
            if (!child.isSelected())
            {
                child.setPartial(false);
            }
            treePacksPanel.setModelValue(child);
        }
        treePacksPanel.fromModel();
    }

    private boolean hasExcludes(CheckBoxNode node)
    {
        Enumeration e = node.depthFirstEnumeration();
        while (e.hasMoreElements())
        {
            CheckBoxNode cbn = (CheckBoxNode) e.nextElement();
            if (cbn.getPack().excludeGroup != null)
            {
                return true;
            }
        }
        return false;
    }

    public void mouseReleased(MouseEvent me)
    {
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        if (path == null)
        {
            return;
        }
        CheckBoxNode current = (CheckBoxNode) path.getLastPathComponent();
        treePacksPanel.setDescription(current.getId());
        treePacksPanel.setDependencies(current.getId());
        if (me.getX() > tree.getPathBounds(path).x + checkWidth)
        {
            return;
        }

        // If this pack is required or NOT selectable, return now.
        if (current.getPack().required || !current.getPack().selectable)
        {
            return;
        }

        boolean currIsSelected = current.isSelected() & !current.isPartial();
        boolean currIsPartial = current.isPartial();
        boolean currHasExcludes = hasExcludes(current);
        CheckBoxNode root = (CheckBoxNode) current.getRoot();

        if (currIsPartial && currHasExcludes)
        {
            current.setSelected(false);
            selectNode(current); // deselect actually
            updateAllParents(root);
        }
        else
        {
            if (!currIsSelected)
            {
                selectAllChildNodes(current);
            }
            current.setSelected(!currIsSelected);
            selectNode(current);
            updateAllParents(root);
        }

        initTotalSize(root, true);

        // must override the bytes being computed at packsModel
        treePacksPanel.setBytes((int) root.getTotalSize());
        treePacksPanel.showSpaceRequired();
        tree.treeDidChange();
    }

    public void selectAllChildNodes(CheckBoxNode cbn)
    {
        Enumeration e = cbn.children();
        while (e.hasMoreElements())
        {
            CheckBoxNode subCbn = (CheckBoxNode) e.nextElement();
            selectAllDependencies(subCbn);
            if (subCbn.getChildCount() > 0)
            {
                selectAllChildNodes(subCbn);
            }

            subCbn.setSelected(true);
            // we need this, because the setModel ignored disabled values
            subCbn.setEnabled(true);
            treePacksPanel.setModelValue(subCbn);
            // Enable this pack only if it's not required.
            subCbn.setEnabled(!subCbn.getPack().required);
        }
    }

    public void selectAllDependencies(CheckBoxNode cbn)
    {
        Pack pack = cbn.getPack();
        List<String> deps = pack.getDependencies();
        if (deps == null)
        {
            return;
        }
        Iterator<String> e = deps.iterator();
        while (e.hasNext())
        {
            String depId = e.next();
            CheckBoxNode depCbn = treePacksPanel.getCbnById(depId);
            selectAllDependencies(depCbn);
            if (depCbn.getChildCount() > 0)
            {
                if (!depCbn.isSelected() || depCbn.isPartial())
                {
                    selectAllChildNodes(depCbn);
                }
            }
            depCbn.setSelected(true);
            // we need this, because the setModel ignored disabled values
            depCbn.setEnabled(true);
            treePacksPanel.setModelValue(depCbn);
            depCbn.setEnabled(!depCbn.getPack().required);
        }
    }

    /**
     * Updates partial/deselected/selected state of all parent nodes.
     * This is needed and is a patch to allow unrelated nodes (in terms of the tree)
     * to fire updates for each other.
     *
     * @param root
     */
    public void updateAllParents(CheckBoxNode root)
    {
        Enumeration rootEnum = root.depthFirstEnumeration();
        while (rootEnum.hasMoreElements())
        {
            CheckBoxNode child = (CheckBoxNode) rootEnum.nextElement();
            if (child.getParent() != null && !child.getParent().equals(root))
            {
                updateParents(child);
            }
        }
    }

    /**
     * Updates the parents of this particular node
     *
     * @param node
     */
    private void updateParents(CheckBoxNode node)
    {
        CheckBoxNode parent = (CheckBoxNode) node.getParent();
        if (parent != null && !parent.equals(parent.getRoot()))
        {
            Enumeration ne = parent.children();
            boolean allSelected = true;
            boolean allDeselected = true;
            while (ne.hasMoreElements())
            {
                CheckBoxNode child = (CheckBoxNode) ne.nextElement();
                if (child.isSelected())
                {
                    allDeselected = false;
                }
                else
                {
                    allSelected = false;
                }
                if (child.isPartial())
                {
                    allSelected = allDeselected = false;
                }
                if (!allSelected && !allDeselected)
                {
                    break;
                }
            }
            if (parent.getChildCount() > 0)
            {
                if (!allSelected && !allDeselected)
                {
                    setPartialParent(parent);
                }
                else
                {
                    parent.setPartial(false);
                }
                if (allSelected)
                {
                    parent.setSelected(true);
                }
                if (allDeselected)
                {
                    parent.setSelected(false);
                }
                treePacksPanel.setModelValue(parent);
                if (allSelected || allDeselected)
                {
                    updateParents(parent);
                }
            }
            //updateTotalSize(node);
        }
    }

    public static void setPartialParent(CheckBoxNode node)
    {
        node.setPartial(true);
        CheckBoxNode parent = (CheckBoxNode) node.getParent();
        if (parent != null && !parent.equals(parent.getRoot()))
        {
            setPartialParent(parent);
        }
    }

    public static long initTotalSize(CheckBoxNode node, boolean markChanged) {
        if (node.isLeaf())
        {
            return node.getPack().getSize();
        }
        Enumeration e = node.children();
        Pack nodePack = node.getPack();
        long bytes = 0;
        if (nodePack != null) {
            if (node.isSelected()) { //Calculate container size
                bytes = nodePack.getSize();
            }
        }
        while (e.hasMoreElements())
        {
            CheckBoxNode c = (CheckBoxNode) e.nextElement();
            long size = initTotalSize(c, markChanged);
            if (c.isSelected() || c.isPartial())
            {
                bytes += size;
            }
        }

        /** If the checkbox node is not the fully packed flag this */
        if (node.getOriginalSize() != bytes) {
            node.setTotalSizeChanged(true);
        } else {
            node.setTotalSizeChanged(false);
        }
        
        node.setTotalSize(bytes);
        return bytes;
    }
    
	public static long setOriginalSize(CheckBoxNode node) {
		long bytes = 0;
		if (node.isLeaf()) {
			bytes = node.getPack().getSize();
			node.setOriginalSize(bytes);
			return bytes;
		}
		
		Enumeration e = node.children();
        Pack nodePack = node.getPack();
        if (nodePack != null ) 
                bytes = nodePack.getSize();
        while (e.hasMoreElements()) {
            long size = setOriginalSize((CheckBoxNode) e.nextElement());
            bytes += size;
        }
        node.setOriginalSize(bytes);
        return bytes;
	}
}
