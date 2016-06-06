package com.redhat.installer.installation.maven.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.GUIHelper;

import javax.swing.*;
import java.awt.*;

//import com.redhat.installer.installation.maven.panel.MavenRepoCheckPanel;

public class MavenCheckPanel extends MavenRepoCheckPanel {

    private static final long serialVersionUID = 1L;

    public MavenCheckPanel(InstallerFrame parent, InstallData idata)
    {
        this(parent, idata, new IzPanelLayout());
    }

    /**
     * @wbp.parser.constructor
     */
    public MavenCheckPanel(InstallerFrame parent, InstallData idata, LayoutManager2 layout)
    {
        super(parent, idata, layout);
        setBorder(GUIHelper.createIzPackBorder());
        setLayout(new BorderLayout());       
        
        JPanel mainPanel = GUIHelper.createMainPanel(!parent.hasBackground);
        
        JLabel title = LabelFactory.createTitleLabel(idata.langpack.getString("MavenCheckPanel.headline"), !parent.hasBackground);
        mainPanel.add(title, GUIHelper.getTitleConstraints());

        repoCheckPanel = new RepoCheckPanelFSW (this, idata, !parent.hasBackground);
        mainPanel.add(repoCheckPanel, GUIHelper.getContentPanelConstraints());
   
        add(mainPanel, BorderLayout.CENTER);
    }
    public void makeXMLData(IXMLElement panelRoot)
    {
        new MavenCheckPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    @Override
    public boolean isValidated()
    {
        if (repoCheckPanel.radioSettingsDefault.isSelected())
        {
                idata.setVariable("mavenSettings","off");
            int answer = askQuestion("",idata.langpack.getString("MavenRepoCheckPanel.warning"),
                    AbstractUIHandler.CHOICES_YES_NO, AbstractUIHandler.ANSWER_NO);
            if (answer != AbstractUIHandler.ANSWER_YES)
            {
                return false;
            }
        }
        else
        {
            idata.setVariable("mavenSettings", "on");
        }
        return super.isValidated();
    }

	@Override
    public String getSummaryBody()
    {
        String installQuickstarts = idata.getVariable("installQuickStarts");
        String installRepo = idata.getVariable("mavenSettings");
        
        if (installQuickstarts == null || installRepo == null){ return null; }
        if (installQuickstarts.equals("false")||installRepo.equals("off")) { return null; }
        
        return  idata.langpack.getString("MavenRepoCheckPanel.settings.location")
                + idata.getVariable("MAVEN_SETTINGS_FULLPATH");
    }
    
    public class RepoCheckPanelFSW extends RepoCheckPanel
    {
		public RepoCheckPanelFSW(IzPanel parent, InstallData idata, boolean isOpaque) 
		{
			super(parent, idata, isOpaque);
			JComponent[]  dynamicComponents = 
	        {  
	            	settingsPathLabel,
                    settingsPathPanel
	        };
			setDynamicComponents(dynamicComponents);
            remove(lblRepoLocation);
            remove(radioRepoDefault);
		    remove(radioRepoCustom);
		    remove(repoPathPanel);
            setInitialFocus(radioSettingsDefault);
		}
   
    }

}