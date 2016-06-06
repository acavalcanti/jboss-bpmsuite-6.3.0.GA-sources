package com.izforge.izpack.installer;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;


public class ProgressDialog extends AbstractProgressDialog {
    private static final long serialVersionUID = -6558347134501630050L;

    public ProgressDialog(){
        initialize();
        this.thread = new ProgressDialogThread();
    }
    
    protected void initialize(){
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));
        
        JLabel label = new JLabel("Loading...");
        main.add(label);
        JPanel progress = new JPanel();
        progress.setLayout(new BoxLayout(progress, BoxLayout.LINE_AXIS));
        
        progressBar = new JProgressBar();
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progress.add(progressBar);
        progress.add(Box.createHorizontalGlue());
        main.add(Box.createVerticalStrut(5));
        main.add(progress);
        main.add(Box.createVerticalGlue());
        this.add(main);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height / 2);
        int width = (int) (screenSize.width / 2);
        this.pack();
        Dimension dialogSize = this.getSize();
        int myheight = (int) (dialogSize.height / 2);
        int mywidth = (int) (dialogSize.width / 2);
        
        this.setLocation(width - mywidth, height - myheight);
    }
}