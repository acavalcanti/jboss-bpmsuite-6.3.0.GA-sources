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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.izforge.izpack.Pack;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.InstallerFrame;
import com.izforge.izpack.installer.IzPanel;
import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * The install panel class. Launches the actual installation job.
 *
 * @author Julien Ponge
 */
public class InstallPanel extends IzPanel implements AbstractUIProgressHandler
{

	private static final long serialVersionUID = 3257282547959410992L;

	/**
	 * The tip label.
	 */
	protected JLabel tipLabel;

	/**
	 * The operation label .
	 */
	protected JLabel packOpLabel;

	/**
	 * The operation label .
	 */
	protected JLabel overallOpLabel;

	/**
	 * The icon used.
	 */
	protected String iconName = "preferences";

	/**
	 * The pack progress bar.
	 */
	protected JProgressBar packProgressBar;

	/** 
	 * List of all pack progress bars
	 */
	protected ArrayList<JProgressBar> packProgressBars;
	private int curPackNo = 0; // the current pack we're on, for accessing the list

	/**
	 * Are we using multi progress bars?
	 */
	private boolean useMultiProgress;

	/**
	 * The progress bar.
	 */
	protected JProgressBar overallProgressBar;

	/**
	 * True if the installation has been done.
	 */
	private volatile boolean validated = false;

	/**
	 * How many packs we are going to install.
	 */
	private int noOfPacks = 0;

	/**
	 * The constructor.
	 *
	 * @param parent The parent window.
	 * @param idata  The installation data.
	 */
	public InstallPanel(InstallerFrame parent, InstallData idata)
	{
		super(parent, idata, new IzPanelLayout());
		this.tipLabel = LabelFactory.create(parent.langpack.getString("InstallPanel.tip"),
				parent.icons.getImageIcon(iconName), LEADING);
		add(this.tipLabel, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
		packOpLabel = LabelFactory.create(" ", LEADING);
		add(packOpLabel, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
		useMultiProgress = idata.guiPrefs.modifier.containsKey("useMultiProgressBar") && 
				"yes".equals(idata.guiPrefs.modifier.get("useMultiProgressBar"));	
		// if the user indicates that they want multi progress bars, we do this!
		if (useMultiProgress){
            noOfPacks = idata.availablePacks.size();
 
            packProgressBars = new ArrayList<JProgressBar>();
			for (int i = 0; i < noOfPacks; i++){
                Pack p = idata.availablePacks.get(i);
                    JProgressBar toAdd = new JProgressBar();
                    toAdd.setString(parent.langpack.getString("InstallPanel.begin"));
                    toAdd.setStringPainted(true);
                    toAdd.setLayout(new BorderLayout());
                    toAdd.setValue(0);
                    toAdd.setVisible(false);
                    packProgressBars.add(toAdd);
                    add(toAdd, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
                    add(IzPanelLayout.createVerticalStrut(2));
			}
		} else {
			packProgressBar = new JProgressBar();
			packProgressBar.setStringPainted(true);
			packProgressBar.setString(parent.langpack.getString("InstallPanel.begin"));
			packProgressBar.setValue(0);
			add(packProgressBar, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
			// make sure there is some space between the progress bars
			add(IzPanelLayout.createVerticalStrut(5));
			overallOpLabel = LabelFactory.create(parent.langpack.getString("InstallPanel.progress"),
					parent.icons.getImageIcon(iconName), LEADING);
			add(this.overallOpLabel, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));

			overallProgressBar = new JProgressBar();
			overallProgressBar.setStringPainted(true);
			if (noOfPacks == 1)
			{
				overallProgressBar.setIndeterminate(true);
			}
			overallProgressBar.setString("");
			overallProgressBar.setValue(0);
			add(this.overallProgressBar, IzPanelLayout.getDefaultConstraint(FULL_LINE_CONTROL_CONSTRAINT));
		}

		getLayoutHelper().completeLayout();

	}

	/**
	 * Indicates wether the panel has been validated or not.
	 *
	 * @return The validation state.
	 */
	public boolean isValidated()
	{
		return this.validated;
	}

	/**
	 * The unpacker starts.
	 */
	public void startAction(String name, int noOfJobs)
	{
		this.noOfPacks = noOfJobs;
	     Iterator<?> iter = idata.panels.iterator();
	     IzPanel panel;
	        
	     while (iter.hasNext()) {
	         panel = (IzPanel) iter.next();
	         if ((panel.getSummaryBody()) != null){
	             panel.setView();
	         }
	     }
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				parent.blockGUI();

				// figure out how many packs there are to install
				if (!useMultiProgress){
					overallProgressBar.setMinimum(0);
					overallProgressBar.setMaximum(noOfPacks);
					overallProgressBar.setString("0 / " + Integer.toString(noOfPacks));
				}
			}
		});
	}

	/**
	 * An error was encountered.
	 *
	 * @param error The error text.
	 */
	public void emitError(String title, String error)
	{
		this.packOpLabel.setText(error);
		idata.installSuccess = false;
		JOptionPane.showMessageDialog(this, error, parent.langpack.getString("installer.error"),
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * The unpacker stops.
	 * Set all previous panels as "viewed by summary panel"
	 */
	public void stopAction()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				parent.releaseGUI();
				parent.lockPrevButton();

				// With custom actions it is possible, that the current value
				// is not max - 1. Therefore we use always max for both
				// progress bars to signal finish state.
				// Tom: assumption is, if we got here, there weren't any failures. so we can mark everything as done ;) 

				if (useMultiProgress){
					for (JProgressBar bar : packProgressBars){
						int max = bar.getMaximum();
						if (max < 1){
							max = 1;
							bar.setMaximum(max);
						}
						bar.setValue(max);
						bar.setEnabled(false);
					}
				} else {
					overallProgressBar.setValue(overallProgressBar.getMaximum());
					int ppbMax = packProgressBar.getMaximum();
					if (ppbMax < 1)
					{
						ppbMax = 1;
						packProgressBar.setMaximum(ppbMax);
					}
					packProgressBar.setValue(ppbMax);

					packProgressBar.setString(parent.langpack.getString("InstallPanel.finished"));
					packProgressBar.setEnabled(false);
					String no_of_packs = Integer.toString(noOfPacks);
					if (noOfPacks == 1)
					{
						overallProgressBar.setIndeterminate(false);
					}
					overallProgressBar.setString(no_of_packs + " / " + no_of_packs);
					overallProgressBar.setEnabled(false);
					packOpLabel.setText(" ");
					packOpLabel.setEnabled(false);
				}
				idata.canClose = true;
				validated = true;
				if (idata.panels.indexOf(this) != (idata.panels.size() - 1))
				{
					parent.unlockNextButton();
				}
			}
		});
	}

	/**
	 * Normal progress indicator.
	 *
	 * @param val The progression value.
	 * @param msg The progression message.
	 */
	public void progress(final int val, final String msg)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// modify to use the arraylist
				if (useMultiProgress){
					if (!(curPackNo >= packProgressBars.size())) {
						JProgressBar bar = packProgressBars.get(curPackNo);
						bar.setValue(val+1);
						//bar.setString("["+(bar.getValue())+" / " + bar.getMaximum()+"]");
					}					
				} else {
					packProgressBar.setValue(val + 1);
				}
				packOpLabel.setText(msg);
			}
		});
		
	}

	/**
	 * Pack changing.
	 *
	 * @param packName The pack name.
	 * @param stepno   The number of the pack.
	 * @param max      The new maximum progress.
	 */
	public void nextStep(final String packName, final int stepno, final int max)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{	                
				if (useMultiProgress){
					//	JProgressBar prev = packProgressBars.get(curPackNo);
					//	prev.setValue(prev.getMaximum());
					curPackNo = stepno - 1;
					if (!(curPackNo >= packProgressBars.size())){
						JProgressBar bar = packProgressBars.get(curPackNo);
						if (max == 0) { //Hack to get a 0/0 progress bar to get filled up
						    bar.setValue(1);
						    bar.setMinimum(0);
						    bar.setMaximum(1);
						}
						else {
    						bar.setValue(0);
    						bar.setMinimum(0);
    						bar.setMaximum(max);
						}
						bar.add(LabelFactory.create("            "+packName, LEADING), BorderLayout.WEST);
						//bar.setString("["+0+" / "+max+"]");
					}
					
				} else {
					packProgressBar.setValue(0);
					packProgressBar.setMinimum(0);
					packProgressBar.setMaximum(max);
					packProgressBar.setString(packName);
					overallProgressBar.setValue(stepno - 1);
					overallProgressBar.setString(Integer.toString(stepno) + " / "
							+ Integer.toString(noOfPacks));
				}

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSubStepNo(final int no_of_substeps)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if (useMultiProgress){
					packProgressBars.get(curPackNo).setMaximum(no_of_substeps);
				} else{
					packProgressBar.setMaximum(no_of_substeps);
				}
			}
		});
	}

	/**
	 * Called when the panel becomes active.
	 */
	public void panelActivate()
	{
	 
	    noOfPacks = idata.selectedPacks.size();

        if (useMultiProgress){

            for (int i=0; i < noOfPacks; i++){

                packProgressBars.get(i).setVisible(true);

            }
        }
		else {
			packProgressBar.setVisible(true);
		}
		// We clip the panel
		Dimension dim = parent.getPanelsContainerSize();
		dim.width -= (dim.width / 4);
		dim.height = 150;
		setMinimumSize(dim);
		setMaximumSize(dim);
		setPreferredSize(dim);
		parent.lockNextButton();
		parent.lockPrevButton();
		parent.install(this);
		
	}
	
}
