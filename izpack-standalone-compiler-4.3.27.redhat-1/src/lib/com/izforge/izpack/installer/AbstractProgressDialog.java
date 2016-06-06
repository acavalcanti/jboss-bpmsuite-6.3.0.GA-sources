package com.izforge.izpack.installer;

import javax.swing.*;

/**
 * An abstract progress dialog that can be extended to create progress bars in
 * panels or dialogs.
 * Created by fcanas on 3/31/14.
 */
public class AbstractProgressDialog extends JWindow {
    protected JProgressBar progressBar;
    protected ProgressDialogThread thread;
    protected JFrame parent = null;

    public void setParent(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Calls the dialog, starting the progress bar.
     */
    public void startProgress() {
        this.setVisible(true);
        this.thread.init(this.progressBar);
        this.thread.start();
    }

    /**
     * Removes the dialog and stops progress bar.
     */
    public void stopProgress(){
        this.setVisible(false);
        this.thread.requestStop();
    }

    class ProgressDialogThread extends Thread {
        private boolean stopRequested;
        private JProgressBar progressBar;

        public ProgressDialogThread(){
            super("ProgressThread");
        }

        public void requestStop()
        {
            stopRequested = true;
        }

        public void init(JProgressBar progressBar)
        {
            this.progressBar = progressBar;
        }

        @Override
        public void run()
        {
            int count=0;
            boolean up = true;

            while (!stopRequested){
                if (up){
                    count++;
                    if (count >= 100){
                        up = false;
                    }
                }
                else {
                    count--;
                    if (count <= 0){
                        up = true;
                    }
                }
                this.progressBar.setValue(count);
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
