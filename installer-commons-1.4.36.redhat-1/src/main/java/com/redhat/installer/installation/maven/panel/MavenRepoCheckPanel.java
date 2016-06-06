package com.redhat.installer.installation.maven.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.panels.PathSelectionPanel;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.GUIHelper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.installation.validator.DirectoryValidator;
import org.xml.sax.SAXException;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class MavenRepoCheckPanel extends IzPanel 
{
    private static final long serialVersionUID = 1L;
    private static final String MAVEN_SETTINGS_XSD = "http://maven.apache.org/xsd/settings-1.0.0.xsd";
    protected RepoCheckPanel repoCheckPanel;
    VariableSubstitutor vs;
    public MavenRepoCheckPanel(InstallerFrame parent, InstallData idata)
    {
        this(parent, idata, new IzPanelLayout());
    }

    public MavenRepoCheckPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout)
    {
        super(parent, idata, layout);
        setBorder(GUIHelper.createIzPackBorder());
        setLayout(new BorderLayout());

        vs = new VariableSubstitutor(idata.getVariables());
        JPanel mainPanel = GUIHelper.createMainPanel(!parent.hasBackground);
        
        JLabel title = LabelFactory.createTitleLabel(idata.langpack.getString("MavenRepoCheckPanel.headline"), !parent.hasBackground);
        mainPanel.add(title, GUIHelper.getTitleConstraints());
        
        repoCheckPanel = new RepoCheckPanel(this, idata, !parent.hasBackground);
        mainPanel.add(repoCheckPanel, GUIHelper.getContentPanelConstraints());

        // not sure why, but this scrollpane really makes the UI go crazy
        //JScrollPane scroller = GUIHelper.createPanelScroller(getBorder(), mainPanel, !parent.hasBackground);
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /** Generate string for the summary panel */
    public String getSummaryBody()
    {
        String installQuickstarts = idata.getVariable("installQuickStarts");
        String installRepo = idata.getVariable("mavenSettings");
        
        if (installQuickstarts == null || installRepo == null){ return null; }
        if (installQuickstarts.equals("false")||installRepo.equals("off")) { return null; }
        
        return idata.langpack.getString("MavenRepoCheckPanel.repo.summary")
                +" " + idata.getVariable("MAVEN_REPO_PATH") + "<br>"
                + idata.langpack.getString("MavenRepoCheckPanel.settings.summary")
                +" " + idata.getVariable("MAVEN_SETTINGS_FULLPATH");
    }

    public void makeXMLData(IXMLElement panelRoot)
    {
        new MavenRepoCheckPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    public boolean isValidated()
    {
        return repoCheckPanel.validated();
    }

    
    class RepoCheckPanel extends JPanel implements ChangeListener
    {
        private static final long serialVersionUID = -7481507528139017008L;
        protected JLabel repoPathLabel;
        protected PathSelectionPanel repoPathPanel;
        protected JComponent settingsPathLabel;
        protected PathSelectionPanel settingsPathPanel;
        protected JRadioButton radioRepoDefault;
        protected JTextArea lblRepoLocation;
        protected JRadioButton radioRepoCustom;
        protected JRadioButton radioSettingsDefault;
        protected JRadioButton radioSettingsCustom;
        protected String error;
        protected JComponent[]  dynamicComponents;

        /**
         * Create the panel.
         */
        public RepoCheckPanel(IzPanel parent, InstallData idata, boolean isOpaque)
        {
            int y = 0;
            String description = idata.langpack.getString("MavenRepoCheckPanel.info");

            String schemaLocation = idata.langpack.getString("MavenRepoCheckPanel.schema.info");
            String schemaLinkText = idata.langpack.getString("MavenRepoCheckPanel.schema.link");

            String repoLocationString = idata.langpack.getString("MavenRepoCheckPanel.repo.location");
            String repoDefaultString = idata.langpack.getString("MavenRepoCheckPanel.repo.option1");
            String repoCustomString = idata.langpack.getString("MavenRepoCheckPanel.repo.option2");

            String settingsLocationString = idata.langpack.getString("MavenRepoCheckPanel.settings.location");
            String settingsDefaultString = idata.langpack.getString("MavenRepoCheckPanel.settings.option1");
            String settingsCustomString = idata.langpack.getString("MavenRepoCheckPanel.settings.option2");
            
            GridBagLayout gridBagLayout = new GridBagLayout();
            gridBagLayout.columnWeights = new double[]{1.0};
            gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            
            setLayout(gridBagLayout);
            setOpaque(isOpaque);
            
            JTextArea lblDescription = LabelFactory.createMultilineLabel(description, isOpaque);       
            GridBagConstraints gbc_lblDescription = new GridBagConstraints();
            gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
            gbc_lblDescription.insets = new Insets(0, 0, 0, 0);
            gbc_lblDescription.fill = GridBagConstraints.BOTH;
            gbc_lblDescription.gridx = 0;
            gbc_lblDescription.gridy = y++;
            add(lblDescription, gbc_lblDescription);

            JLabel lblSchemaLocation = LabelFactory.create(String.format(schemaLocation, "<a href="+MAVEN_SETTINGS_XSD+">here</a>"));
            GridBagConstraints gbc_lblSchemaLocation = new GridBagConstraints();
            gbc_lblSchemaLocation.anchor = GridBagConstraints.WEST;
            gbc_lblSchemaLocation.gridx = 0;
            gbc_lblSchemaLocation.gridy = 0;

            JButton linkButton = new JButton(schemaLinkText);
            linkButton.setForeground(Color.BLUE);
            linkButton.setFocusPainted(false);
            linkButton.setMargin(new Insets(0,0,0,0));
            linkButton.setContentAreaFilled(false);
            linkButton.setBorderPainted(false);
            linkButton.setOpaque(isOpaque);

            linkButton.addActionListener(new ActionListener(){
               public void actionPerformed(ActionEvent e){
                   openLink();
               }
            });

            GridBagConstraints gbc_linkButton = new GridBagConstraints();
            gbc_linkButton.anchor = GridBagConstraints.WEST;
            gbc_linkButton.gridx = 1;
            gbc_linkButton.gridy = 0; // same level as the label

            JPanel linkPanel = new JPanel(new GridBagLayout());
            linkPanel.setOpaque(isOpaque);
            linkPanel.add(lblSchemaLocation, gbc_lblSchemaLocation);
            linkPanel.add(linkButton, gbc_linkButton);
            GridBagConstraints gbc_linkPanel = new GridBagConstraints();
            gbc_linkPanel.anchor = GridBagConstraints.WEST;
            gbc_linkPanel.fill = GridBagConstraints.NONE;
            gbc_linkPanel.insets = new Insets(0,0,5,0);
            gbc_linkPanel.gridx = 0;
            gbc_linkPanel.gridy = y++;
            add(linkPanel, gbc_linkPanel);

            lblRepoLocation = LabelFactory.createMultilineLabel(repoLocationString, isOpaque);
            GridBagConstraints gbc_lblRepoLocation = new GridBagConstraints();
            gbc_lblRepoLocation.anchor = GridBagConstraints.NORTHWEST;
            gbc_lblRepoLocation.insets = new Insets(0, 0, 5, 0);
            gbc_lblRepoLocation.fill = GridBagConstraints.BOTH;
            gbc_lblRepoLocation.gridx = 0;
            gbc_lblRepoLocation.gridy = y++;
            add(lblRepoLocation, gbc_lblRepoLocation);

            radioRepoDefault = new JRadioButton(repoDefaultString);
            radioRepoDefault.setOpaque(isOpaque);
            radioRepoDefault.setVerticalAlignment(SwingConstants.TOP);
            radioRepoDefault.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints gbc_repoDefault = new GridBagConstraints();
            gbc_repoDefault.fill = GridBagConstraints.HORIZONTAL;
            gbc_repoDefault.anchor = GridBagConstraints.WEST;
            gbc_repoDefault.gridx = 0;
            gbc_repoDefault.gridy = y++;
            add(radioRepoDefault, gbc_repoDefault);
            
            // i dislike radio buttons.
            radioRepoCustom = new JRadioButton(String.format("<html><p>%1$s</p></html>",repoCustomString));
            radioRepoCustom.setOpaque(isOpaque);
            GridBagConstraints gbc_radioRepoCustom = new GridBagConstraints();
            gbc_radioRepoCustom.fill = GridBagConstraints.HORIZONTAL;
            gbc_radioRepoCustom.anchor = GridBagConstraints.WEST;
            gbc_radioRepoCustom.gridx = 0;
            gbc_radioRepoCustom.gridy = y++;
            add(radioRepoCustom, gbc_radioRepoCustom);

            repoPathPanel = new PathSelectionPanel(parent, idata);
            repoPathPanel.setOpaque(isOpaque);
            repoPathPanel.setEnabled(false);
            repoPathPanel.getPathInputField().setForeground(Color.GRAY);
            GridBagConstraints gbc_repoPathSelectionPanel = new GridBagConstraints();
            gbc_repoPathSelectionPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_repoPathSelectionPanel.insets = new Insets(0,0,10,0);
            gbc_repoPathSelectionPanel.anchor = GridBagConstraints.NORTHWEST;
            gbc_repoPathSelectionPanel.gridx = 0;
            gbc_repoPathSelectionPanel.gridy = y++;
            add(repoPathPanel, gbc_repoPathSelectionPanel);
            repoPathPanel.setPath(idata.getVariable("MAVEN_REPO_PATH.default"));

            JTextArea lblSettingsLocation = LabelFactory.createMultilineLabel(settingsLocationString, isOpaque);
            GridBagConstraints gbc_lblSettingsLocation = new GridBagConstraints();
            gbc_lblSettingsLocation.anchor = GridBagConstraints.NORTHWEST;
            gbc_lblSettingsLocation.fill = GridBagConstraints.HORIZONTAL;
            gbc_lblSettingsLocation.insets = new Insets(0, 0, 5, 0);
            gbc_lblSettingsLocation.gridx = 0;
            gbc_lblSettingsLocation.gridy = y++;
            add(lblSettingsLocation, gbc_lblSettingsLocation);

            radioSettingsDefault = new JRadioButton(String.format(String.format("<html><p>%1$s</p></html>", settingsDefaultString),idata.getVariable("USER_HOME")));
            radioSettingsDefault.setOpaque(isOpaque);
            radioSettingsDefault.setVerticalAlignment(SwingConstants.TOP);
            radioSettingsDefault.setHorizontalAlignment(SwingConstants.LEFT);
            GridBagConstraints gbc_radioSettingsDefault = new GridBagConstraints();
            gbc_radioSettingsDefault.fill = GridBagConstraints.HORIZONTAL;
            gbc_radioSettingsDefault.anchor = GridBagConstraints.WEST;
            gbc_radioSettingsDefault.gridx = 0;
            gbc_radioSettingsDefault.gridy = y++;
            add(radioSettingsDefault, gbc_radioSettingsDefault);

            radioSettingsCustom = new JRadioButton(String.format("<html><p>%1$s</p></html>", settingsCustomString));
            radioSettingsCustom.setOpaque(isOpaque);
            GridBagConstraints gbc_radioSettingsCustom = new GridBagConstraints();
            gbc_radioSettingsCustom.fill = GridBagConstraints.HORIZONTAL;
            gbc_radioSettingsCustom.anchor = GridBagConstraints.WEST;
            gbc_radioSettingsCustom.gridx = 0;
            gbc_radioSettingsCustom.gridy = y++;
            add(radioSettingsCustom, gbc_radioSettingsCustom);


            settingsPathPanel = new PathSelectionPanel(parent, idata);
            settingsPathPanel.setOpaque(isOpaque);
            settingsPathPanel.setEnabled(false);
            settingsPathPanel.getPathInputField().setForeground(Color.GRAY);

            GridBagConstraints gbc_settingsSelectionPanel = new GridBagConstraints();
            gbc_settingsSelectionPanel.anchor = GridBagConstraints.WEST;
            gbc_settingsSelectionPanel.fill = GridBagConstraints.HORIZONTAL;
            gbc_settingsSelectionPanel.gridx = 0;
            gbc_settingsSelectionPanel.gridy = y++;
            add(settingsPathPanel, gbc_settingsSelectionPanel);

            settingsPathPanel.setPath(vs.substitute(idata.getVariable("MAVEN_SETTINGS_FULLPATH.default")));
            settingsPathPanel.setAllFiles(true);

            JComponent [] repoDynamicComponents =
            {
	    	    repoPathPanel,
                    settingsPathPanel
	        };
            setInitialFocus(radioRepoDefault);
            setDynamicComponents(repoDynamicComponents);
            setupRadioButtons();
            setAccessibilityData();

        }

        private void openLink() {
            if (Desktop.isDesktopSupported()){
                try {
                    Desktop.getDesktop().browse(new URI(MAVEN_SETTINGS_XSD));
                } catch (IOException e){
                    //TODO: error handling
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    // TODO: error handling
                    e.printStackTrace();
                }
            }
        }


        private void setAccessibilityData()
        {
            /*
             * AccessibleContext and ActionCommand setting for marathon automated testing
             */
            radioRepoDefault.setActionCommand("Use default Maven repository setup");
            AccessibleContext ac = radioRepoDefault.getAccessibleContext();
            ac.setAccessibleDescription("This JRadioButton, if selected, will use the default public Maven repository URL.");
            radioRepoCustom.setActionCommand("Install custom Maven repository");
            ac = radioRepoCustom.getAccessibleContext();
            ac.setAccessibleDescription("This JRadioButton, if selected, will allow the user to specify a custom Maven repository URL");
            settingsPathPanel.getPathInputField().setActionCommand("Contains settings.xml location");
            ac = settingsPathPanel.getPathInputField().getAccessibleContext();
            ac.setAccessibleDescription("This JTextField contains the path to the user's settings.xml.");
	        repoPathPanel.getPathInputField().setActionCommand("Contains Maven repository path or URL");
	        ac = repoPathPanel.getPathInputField().getAccessibleContext();
            ac.setAccessibleDescription("This JTextField contains the path to the Maven repository. Can be local or a URL.");
            radioSettingsDefault.setActionCommand("Use default settings.xml file path");
            ac = radioSettingsDefault.getAccessibleContext();
            ac.setAccessibleDescription("This JRadioButton, if selected, will use the default settings.xml path");
            radioSettingsCustom.setActionCommand("Use custom settings.xml file path");
            ac = radioSettingsCustom.getAccessibleContext();
            ac.setAccessibleDescription("This JRadioButton, if selected, will allow the user to specify a custom settings.xml location");
        }


        private void setupRadioButtons()
        {
            String stored = idata.getVariable("MAVEN_REPO_PATH");
            boolean prevRepoPath = stored != null;
            String settingsstored = idata.getVariable("MAVEN_SETTINGS_FULLPATH");
            boolean prevSettingsPath = settingsstored != null;
            ButtonGroup repoGroup = new ButtonGroup();
            repoGroup.add(radioRepoCustom);
            repoGroup.add(radioRepoDefault);
            radioRepoDefault.setSelected(!prevRepoPath);
            radioRepoCustom.setSelected(prevRepoPath);
            radioRepoCustom.addChangeListener(this);
            radioRepoDefault.addChangeListener(this);
            ButtonGroup settingsGroup = new ButtonGroup();
            settingsGroup.add(radioSettingsDefault);
            settingsGroup.add(radioSettingsCustom);
            radioSettingsDefault.setSelected(!prevSettingsPath);
            radioSettingsCustom.setSelected(prevSettingsPath);
            radioSettingsDefault.addChangeListener(this);
            radioSettingsCustom.addChangeListener(this);
        }

       
        public JComponent getSettingsPathLabel()
        {
            return settingsPathLabel;
        }

        
        public PathSelectionPanel getSettingsPathPanel()
        {
            return settingsPathPanel;
        }

        // TODO: generalize so that MavenCheckPanel functionality is also modified
        public void stateChanged(ChangeEvent arg0)
        {
            AbstractButton abstractButton = (AbstractButton) arg0.getSource();
            ButtonModel buttonModel = abstractButton.getModel();
            if (buttonModel.isPressed() && buttonModel.isSelected()) {
                if (abstractButton == radioRepoCustom)
                {
                    repoPathPanel.setEnabled(true);
                    repoPathPanel.getPathInputField().setForeground(UIManager.getColor("TextField.foreground"));
                }
                else if (abstractButton == radioRepoDefault)
                {
                    repoPathPanel.setEnabled(false);
                    repoPathPanel.getPathInputField().setForeground(Color.GRAY);
                }
                else if (abstractButton == radioSettingsCustom)
                {
                    settingsPathPanel.setEnabled(true);
                    settingsPathPanel.getPathInputField().setForeground(UIManager.getColor("TextField.foreground"));
                }
                else if (abstractButton == radioSettingsDefault)
                {
                    settingsPathPanel.setEnabled(false);
                    settingsPathPanel.getPathInputField().setForeground(Color.GRAY);
                }
            }
            revalidate();
            repaint();
        }
        
        public boolean validated()
        {
            if (radioSettingsDefault.isSelected())
            {
                idata.setVariable("MAVEN_SETTINGS_FULLPATH", vs.substitute(idata.getVariable("MAVEN_SETTINGS_FULLPATH.default")));
            }
            else
            {
                String settingsPath =  settingsPathPanel.getPath();
                if (settingsPath.isEmpty())
                {
                    emitError("Error", idata.langpack.getString("MavenRepoCheckPanel.settings.empty"));
                    return false;
                }
                File setFile = new File(settingsPath);

                String targetDir = setFile.getParent();

                if (setFile.getPath().toLowerCase().startsWith("http"))
                {
                    emitError("Error", idata.langpack.getString("MavenRepoCheckPanel.settings.invalid"));
                    return false;
                }

                if (setFile.isDirectory())
                {
                    // instead of failing, we append 'settings.xml' onto the end of this directory and create the file there.
                    setFile = new File(setFile.getAbsoluteFile() + File.separator + "settings.xml");
                }

                if (!setFile.exists()) // indicated settings location doesn't exist. warn about manual intervention.
                {
                    //Check that we can write into the directory where settings.xml should be located
                    // find out if creating the directory is possible
                    File existingParent = IoHelper.existingParent(setFile);

                    // we've found the furthest up parent. we check if we can write here (and thus create all subdirs / settings.xml there
                    if (existingParent == null)
                    {
                        emitError("Error", String.format(idata.langpack.getString("MavenRepoCheckPanel.drive.error"),setFile.getAbsolutePath().substring(0,2)));
                        return false;
                    }
                    else if (!(existingParent.canWrite() && existingParent.canExecute())){
                        emitError("Error", String.format(idata.langpack.getString("MavenRepoCheckPanel.dir.error"),existingParent.getAbsolutePath()));
                        return false;
                    }
                    else if (!DirectoryValidator.validate(new File(existingParent.getPath())))
                    {
                        emitError("Error", String.format(idata.langpack.getString("DirectoryValidator.invalid"), DirectoryValidator.getInvalidCharacters()));
                        return false;
                    }

                    int answer = askQuestion("", String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.error"), setFile.getAbsolutePath()),
                            AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_NO);
                    if (answer != AbstractUIHandler.ANSWER_YES) // if the answer is yes, we save the path and make sure that we create the file with the same name in the Process
                    {
                        return false;
                    }
                }
                else
                {
                    if (!setFile.canWrite())
                    {
                        emitError("Error", String.format(idata.langpack.getString("MavenRepoCheckPanel.file.error"), setFile.getAbsolutePath()));
                        return false;
                    }
                    // validate the settings.xml
                    try {
                        checkSchema(setFile);
                    } catch (MalformedURLException e) {
                        // should never really occur, since we're using a constant as the URL
                        e.printStackTrace();
                    } catch (SAXException e) {
                        // borked internet connection
                        if (e.getCause() != null && e.getCause().getClass().equals(UnknownHostException.class)){
                            emitNotification(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.inaccessible"), setFile.getAbsolutePath()), false);
                        } else {
                            // some other parsing error, print it!
                            emitError("Error", String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.failed"), setFile.getAbsolutePath(), "\n" + e.getLocalizedMessage()));
                            return false;
                        }
                    }
                    // also shouldn't happen, because the IOExceptions are all wrapped by the validator in SAXException, and we're already guaranteed to have both a correct URL
                    // and a correct File by this point.
                    catch (IOException e) {
                        emitNotification(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.inaccessible"), setFile.getAbsolutePath()), false);
                    }
                }
                idata.setVariable("MAVEN_SETTINGS_FULLPATH", setFile.getAbsolutePath());
            }

            if (Arrays.asList(dynamicComponents).contains(repoPathPanel))
            {
                if (radioRepoDefault.isSelected())
                {
                    idata.setVariable("MAVEN_REPO_PATH", idata.getVariable("MAVEN_REPO_PATH.default"));
                }
                else
                {
                    String urlType = "";
                    String currentPath = repoPathPanel.getPath();
                    boolean isUrl = currentPath.toLowerCase().startsWith("http");
                    if (isUrl)
                    {
                        if (!IoHelper.remoteFileExists(currentPath))
                        {
                            emitError("Error", idata.langpack.getString("MavenRepoCheckPanel.path.error"));
                            return false;
                        }
                    }
                    else
                    {
                        File chosenPath = new File(repoPathPanel.getPath());
                        if (!chosenPath.exists() || !chosenPath.isDirectory())
                        {
                            emitError("Error", idata.langpack.getString("MavenRepoCheckPanel.path.error"));
                            return false;
                        }
                        urlType = "file://";
                    }
                    idata.setVariable("MAVEN_REPO_PATH", urlType + repoPathPanel.getPath());
                }
            }
            return true;
        }

        public String getError()
        {
            return error;
        }


        
        public void setError(String error)
        {
            this.error = error;
        }
        
        public void setDynamicComponents( JComponent[] dynamicComponents)
        {
        	this.dynamicComponents = dynamicComponents;
        }
        
    }

    public static void checkSchema(File setFile) throws IOException, SAXException {
        URL schemaFile = new URL(MAVEN_SETTINGS_XSD);
        Source xmlFile = new StreamSource(setFile);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

}
