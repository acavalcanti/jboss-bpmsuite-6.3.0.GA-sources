package com.izforge.izpack.panels;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.installer.ProcessPanelWorker;
import com.izforge.izpack.util.AbstractUIProcessHandler;


public class ProcessPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole, AbstractUIProcessHandler
{
    private int noOfJobs = 0;

    private int currentJob = 0;

    private static AutomatedInstallData installData = null;

    private static final String PROCESS     = "ProcessPanel.start.process";
    private static final String PROCESSING  = "ProcessPanel.start.processing";
    private static final String SPACE       = " ";

    public void emitNotification(String message)
    {
        // TODO Auto-generated method stub

    }

    public boolean emitWarning(String title, String message)
    {
        System.out.println(title);
        System.out.println(message);
        return false;
    }

    public void emitError(String title, String message)
    {
        System.err.println(title);
        System.err.println(message);
    }

    public void emitErrorAndBlockNext(String title, String message)
    {
        System.err.println(title);
        System.err.println(message);
    }

    public int askQuestion(String title, String question, int choices)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public int askQuestion(String title, String question, int choices, int default_choice)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void logOutput(String message, boolean stderr)
    {
        if (stderr)
        {
            System.err.println(message);
        }
        else
        {
            System.out.println(message);
        }
    }

    public void startProcessing(int no_of_processes)
    {
        System.out.println("[ " + getTranslation(PROCESSING) + " ]");
        this.noOfJobs = no_of_processes;
    }

    public void startProcess(String name)
    {
        this.currentJob++;
        System.out.println(getTranslation(PROCESS) + SPACE + name + " ("
                + Integer.toString(this.currentJob)
                + "/" + Integer.toString(this.noOfJobs) + ")");
    }

    public void finishProcess()
    {
        // TODO Auto-generated method stub

    }

    public void finishProcessing(boolean unlockPrev, boolean unlockNext)
    {
        // TODO Auto-generated method stub

    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        // TODO finish this
        return false;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        // TODO finish this
        return runConsole(installData, null);
    }

    private String getTranslation(final String text)
    {
        return installData.langpack.getString(text);
    }
    public boolean runConsole(AutomatedInstallData installData, ConsoleInstaller parent)
    {
        try
        {
            this.installData = installData;
            ProcessPanelWorker worker = new ProcessPanelWorker(installData, this);

            worker.run();
            installData.installSuccess = worker.getSuccessfulInstall();
            String installResult = installData.installSuccess ? "true" : "false";
            installData.setVariable("successful.install", installResult);

            if (!worker.getResult())
            {
                return false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new RuntimeException("The work done by the ProcessPanel failed", e);
        }

        return true;
    }


}
