/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Log;
import com.izforge.izpack.util.VariableSubstitutor;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * The finish panel class.
 *
 * @author Julien Ponge
 */
public class FinishPanel extends IzPanel implements ActionListener
{

    private static final long serialVersionUID = 3257282535107998009L;

    // Localized string IDs
    private static final String FILE_EXISTS  = "FinishPanel.file.already.exists";
    
    private static final String FINISH = "FinishPanel.";
    private static final String LINK = ".link";
    private static final String LABEL = ".label";
    private static final String SHOW = ".show";

    // Retrieval method for Localized strings
    private static String getTranslation(final InstallData idata, final String text) {
        return idata.langpack.getString(text);
    }

    /**
     * The automated installers generation button.
     */
    protected JButton autoButton;

    /**
     * The variables substitutor.
     */
    protected VariableSubstitutor vs;

    /**
     * The constructor.
     *
     * @param parent The parent.
     * @param idata  The installation data.
     */
    public FinishPanel(InstallerFrame parent, InstallData idata)
    {
        super(parent, idata, new GridBagLayout());

        vs = new VariableSubstitutor(idata.getVariables());
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the panel has been validated.
     */
    public boolean isValidated()
    {
        return true;
    }

    /** Called when the panel becomes active.
     * 
     * 1) Declare that you want to show links on your finish panel
     *     <variable name="FinishPanel.links.show" value="true"/>
     * 2) Declare link ids with comma seperated list
     *     <variable name="FinishPanel.links.list" value="admin,gov,bpel"/>
     * 3) Choose a condition for your link to be checked against
     *     <variable name="FinishPanel.gov.show" value="sramp.install" />
     * 4) Add link label and link text to eng.xml
     *     <str id="FinishPanel.gov.label" txt="Governance Console:"/>
     *     <str id="FinishPanel.gov.link" txt="http://localhost:8080/gwt-console-server"/>
     */
    public void panelActivate()
    {
    	boolean showLinks = true;
    	String linkFlag = idata.getVariable("FinishPanel.links.show");
    	if (linkFlag == null || !linkFlag.equals("true")) 
    		showLinks = false;
        parent.lockNextButton();
        parent.lockPrevButton();
        parent.setQuitButtonText(parent.langpack.getString("FinishPanel.done"));
        parent.setQuitButtonIcon("done");
        
        //Window size, width=800, height=600
        GridBagConstraints constraints = new GridBagConstraints();
        int y=0;
        int x=0;
        
        Insets noPad         = new Insets(0,0,0,0);
        Insets leftLinkPad   = new Insets(0,180,0,10);
        Insets mainLeftPad   = new Insets(0,200,0,0);
        Insets centerPad     = new Insets(0,200,0,200);
        Insets linkHeaderPad = new Insets(0,340,0,0);
        
        JLabel label;
        JTextField link; 
        constraints.fill = GridBagConstraints.HORIZONTAL;
        if (showLinks)
        {
        	constraints.gridwidth = 2;
        }
        if (idata.installSuccess)
        {
            Filler dummy = new Filler();
            constraints.insets = mainLeftPad;
            constraints.gridy = y++;
            constraints.gridx = x;
            add(dummy, constraints);
            
            label = LabelFactory.create(idata.langpack.getString("FinishPanel.success"),
                    parent.icons.getImageIcon("preferences"), LEFT);            
            constraints.gridy = y++;
            constraints.gridx = x;
            add(label, constraints);
            
            if (idata.uninstallOutJar != null)
            {
                // We prepare a message for the uninstaller feature
                String path = translatePath(idata.info.getUninstallerPath());
               
                constraints.gridy = y++;
                constraints.gridx = x;
                add(LabelFactory.create(parent.langpack
                        .getString("FinishPanel.uninst.info"), parent.icons
                        .getImageIcon("preferences"), LEFT), constraints);
                

                constraints.gridy = y++;
                constraints.gridx = x;
                add(LabelFactory.create("  " + path, parent.icons.getImageIcon("empty"),
                        LEFT), constraints);
            }

            String[] links = null;
            String availableLinks = idata.getVariable("FinishPanel.links.list"); //Get list of link ids, ids seperated with commas
            if (availableLinks != null )   links = availableLinks.split(",");         //Get individual link ids
            if (showLinks && links != null ) { 
                boolean show = false;
                for (String linkId :  links) {
                    String condId = FINISH +linkId+ SHOW;
                    boolean linkCond = idata.getRules().isConditionTrue(idata.getVariable(condId));
                    if (linkCond) show=true;
                }
                if (show){
	            label = LabelFactory.create(" ", LEFT);
	            constraints.gridy = y++;
	            constraints.gridx = x;
	            add(label, constraints);
	            
	            JLabel linkHeader = LabelFactory.create(idata.langpack.getString("FinishPanel.link.header"), LEFT);
	            constraints.insets = linkHeaderPad;
	            constraints.gridy = y++;
	            constraints.gridx = x;
	            add(linkHeader, constraints);
                }
	            
	            constraints.gridwidth = 1;
	            constraints.weightx = 0.5;
	      
	            for (String linkId :  links) {
	                //System.out.println("LinkID: " + linkId);
	                String condId = FINISH +linkId+ SHOW;
	                boolean linkCond = idata.getRules().isConditionTrue(idata.getVariable(condId));
	                //System.out.println("Link Condition :: " + linkCond);
	                if (linkCond) {
    	            	String storedValue = idata.langpack.getString(FINISH+linkId+LABEL);
    	                label = LabelFactory.create(storedValue, LEFT);
    	                constraints.insets = leftLinkPad;
    	                constraints.gridy = y;
    	                constraints.gridx = x++;
    	                add(label, constraints);
    	                
    	                link = new JTextField(idata.langpack.getString(FINISH + linkId+LINK));
    	                link.setBorder(javax.swing.BorderFactory.createEmptyBorder());
    	                link.setEditable(false);
    	                link.setOpaque(false);
    	                constraints.insets = noPad;
    	                constraints.gridy = y++;
    	                constraints.gridx = x--;
    	                add(link, constraints);
	                }
	            }
            }
            constraints.gridwidth = 2;
            constraints.weightx = 0;
            constraints.insets = centerPad;
            
            label = LabelFactory.create(" ", CENTER);
            constraints.gridy = y++;
            constraints.gridx = x;
            add(label, constraints);
            
            // We add the autoButton
            autoButton = ButtonFactory.createButton(parent.langpack.getString("FinishPanel.auto"),
                    parent.icons.getImageIcon("edit"), idata.buttonsHColor);
            autoButton.setToolTipText(parent.langpack.getString("FinishPanel.auto.tip"));
            autoButton.addActionListener(this);
            /*
             * AccessibleContext and ActionCommand settings. 
             */
            autoButton.setActionCommand("Create auto.xml");
            AccessibleContext ac = autoButton.getAccessibleContext();
            ac.setAccessibleDescription("This JButton will create the auto.xml descriptor for future installations when pressed");
            
            parent.setFocus(autoButton);
            constraints.gridy = y++;
            constraints.gridx = x;
            add(autoButton, constraints);

        
        }
        else
        {
            constraints.gridy = y++;
            constraints.gridx = x;
            add(LabelFactory.create(parent.langpack.getString("FinishPanel.fail"),
                    parent.icons.getImageIcon("stop"), CENTER), constraints);
            constraints.gridy = y++;
            constraints.gridx = x;
            String logFilePath = idata.getInstallPath() + File.separator + idata.getVariable("installation.logfile");
            File normalizedLogFilePath = new File(logFilePath);
            add(LabelFactory.create(parent.langpack.getString("See Installation Log at "
                    + normalizedLogFilePath.getAbsolutePath()),
                    CENTER), constraints);
            constraints.gridy++;
        }
        getLayoutHelper().completeLayout(); // Call, or call not?
        Log.getInstance().informUser();
    }

    /**
     * Actions-handling method.
     *
     * @param e The event.
     */
    public void actionPerformed(ActionEvent e)
    {
        // Prepares the file chooser
        JFileChooser fc = new JFileChooser();
        loadFileChooserLang();
        fc.setCurrentDirectory(new File(idata.getInstallPath()));
        fc.setMultiSelectionEnabled(false);
        fc.setDialogTitle("Save");
        javax.swing.filechooser.FileFilter xmlfilter = new FileNameExtensionFilter("XML File (.xml)", "xml");
        fc.setFileFilter(xmlfilter);
        /**
         * removing this for our build
         */
        //fc.addChoosableFileFilter(new AutomatedInstallScriptFilter());
        // fc.setCurrentDirectory(new File("."));

        // Shows it
        try
        {
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                // We handle the xml data writing
                File file = fc.getSelectedFile();

                if (fc.getFileFilter().equals(xmlfilter) && !file.getPath().endsWith(".xml")){
                    file = new File(file.getPath() + ".xml");
                }
                // If the file exists, prompt for overwrite confirmation
                int res = 0;
                if (file.exists()) {
                    res = askQuestion(parent.langpack.getString("installer.warning"), getTranslation(idata, FILE_EXISTS), AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_YES);
                }
                if (! file.exists() || (file.exists() && res == AbstractUIHandler.ANSWER_YES)) {
                    FileOutputStream out = new FileOutputStream(file);
                    BufferedOutputStream outBuff = new BufferedOutputStream(out, 5120);
                    parent.writeXMLTree(idata.xmlData, outBuff);
                    outBuff.flush();
                    outBuff.close();

                    file.setWritable(false,false);
                    file.setReadable(false,false);
                    file.setWritable(true,true);
                    file.setReadable(true,true);

                    autoButton.setEnabled(false);
                    String buttonSuccessText = parent.langpack.getString("FinishPanel.auto.generate.success");
                    if (buttonSuccessText != null && !buttonSuccessText.equals("FinishPanel.auto.generate.success")) {
                        autoButton.setText(buttonSuccessText);
                    }
                    outputVariableFile(file);
                }
            }
        }
        catch (Exception err)
        {
            Debug.trace(err);
            JOptionPane.showMessageDialog(this, err.toString(), parent.langpack
                    .getString("installer.error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    protected static void outputVariableFile(File file){
                    // if autoPromptVars isn't empty, we need to write out an empty file containing the variable names for the user's reference
                    AutomatedInstallData idata = AutomatedInstallData.getInstance();
                    if (!idata.autoPromptVars.isEmpty()){
                        String propsFilePath = file.getPath()+".variables";
                        File propsFile = null;
                        do {
                            propsFile = new File(propsFilePath);
                            if (propsFile.exists()){
                                propsFilePath+="1";
                           }
                        } while (propsFile.exists());

                        PrintWriter propsOut = null;
                        try {
                            propsOut = new PrintWriter(new FileWriter(propsFile));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        for (String variable : idata.autoPromptVars){
                            propsOut.println(variable+"=");
                        }
                        propsOut.close();
                    }

    }
    /**
     * Translates a relative path to a local system path.
     *
     * @param destination The path to translate.
     * @return The translated path.
     */
    protected String translatePath(String destination)
    {
        // Parse for variables
        destination = vs.substitute(destination, null);

        // Convert the file separator characters
        return destination.replace('/', File.separatorChar);
    }
}
