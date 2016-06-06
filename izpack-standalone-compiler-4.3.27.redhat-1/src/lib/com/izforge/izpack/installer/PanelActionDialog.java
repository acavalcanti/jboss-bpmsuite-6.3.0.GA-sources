package com.izforge.izpack.installer;

import com.izforge.izpack.util.Debug;

import javax.swing.*;
import java.awt.*;

/**
 * Creates a visual progress bar dialog and displays it to the user while
 * the panel action runs. Useful for longer running actions.
 */
public abstract class PanelActionDialog implements PanelAction {
    protected ActionProgressDialog progressDialog;
    protected JFrame parent = null;

    public void setParent(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Call from executeAction method to start the progress dialog.
     */
    protected void startDialog(String msg) {
        try {
            progressDialog = new ActionProgressDialog(parent, msg);
            progressDialog.startProgress();
        } catch (HeadlessException ex) {
            Debug.log("Progress will not be shown. No display found.");
        }
    }

    /**
     * Call this whenever the action has finished.
     */
    protected void stopDialog() {
        if (progressDialog != null) {
            progressDialog.stopProgress();
        }
    }
}