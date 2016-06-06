package com.izforge.izpack.installer;

import javax.swing.*;
import java.awt.*;

/**
 * A progress dialog bar that is displayed while a PanelActionDialog is run.
 * Created by fcanas on 3/31/14.
 */
public class ActionProgressDialog extends AbstractProgressDialog {
    private String msg;
    private static final int dialogWidth = 400;
    private static final int dialogHeight = 100;
    private static final int barWidth = dialogWidth;
    private static final int barHeight = 10;
    private static final int progressPadding = 5;
    private static final int hGap =  20;

    /**
     * The parent for this dialog is normally the InstallerFrame, since that's
     * where it's called from.
     * @param parent A JFrame, usually the installerFrame.
     * @param msg A formatted string to display as a user msg.
     */
    public ActionProgressDialog(JFrame parent, String msg) {
        this.parent = parent;
        this.msg = msg;
        this.initDialog();
        this.thread = new ProgressDialogThread();
    }

    /**
     * Builds the layout, progress bar, and panel for this action progress dialog.
     */
    protected void initDialog(){
        this.setSize(this.dialogWidth, this.dialogHeight);

        JPanel main = initMainPanel();
        JLabel label = initLabel();
        this.progressBar = initProgressBar();
        main.add(label);
        main.add(this.progressBar);
        setWindowLocation();
        this.add(main);
    }

    private JPanel initMainPanel() {
        JPanel main = new JPanel();
        GridLayout layout = new GridLayout(0,1);
        layout.setHgap(hGap);

        main.setLayout(layout);
        main.setSize(dialogWidth, dialogHeight);
        main.setBorder(BorderFactory.createRaisedBevelBorder());
        return main;
    }

    private JLabel initLabel() {
        JLabel label = new JLabel(msg);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBackground(Color.DARK_GRAY);
        return label;
    }

    private JProgressBar initProgressBar() {
        JProgressBar progressBar = new JProgressBar();
        progressBar.setBorder(BorderFactory.createEmptyBorder(progressPadding, progressPadding, progressPadding, progressPadding));
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(barWidth, barHeight));
        progressBar.setMaximum(barWidth);
        progressBar.setValue(0);
        return progressBar;
    }

    /**
     * Sets the location for this JWindow based on its parent, or on default
     * screen size.
     */
    private void setWindowLocation() {
        if (this.parent == null) {
            centerToScreen();
        } else {
            centerToParent();
        }
    }

    /**
     * Centers the location of this JWindow on the screen.
     */
    private void centerToScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) (screenSize.height / 2);
        int width = (int) (screenSize.width / 2);

        Dimension dialogSize = this.getSize();
        int myheight = (int) (dialogSize.height);
        int mywidth = (int) (dialogSize.width);

        this.setLocation(width - mywidth, height - myheight);
    }

    /**
     * Center this window to the installer frame parent.
     */
    private void centerToParent() {
        Point loc = this.parent.getLocation();
        int pX = (int)loc.getX();
        int pY = (int)loc.getY();

        Dimension childSize = this.getSize();
        Dimension parentSize = parent.getSize();

        setLocation(pX + getDelta(parentSize.getWidth(), childSize.getWidth()),
                pY + getDelta(parentSize.getHeight(), childSize.getHeight()));
    }

    private int getDelta(double parentDim, double childDim) {
        return (int)((parentDim - childDim) /  2.0);
    }
}
