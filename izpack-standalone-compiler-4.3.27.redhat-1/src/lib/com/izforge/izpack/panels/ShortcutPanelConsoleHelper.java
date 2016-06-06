package com.izforge.izpack.panels;

import java.io.PrintWriter;
import java.util.Properties;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.Shell;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.os.unix.UnixHelper;

import static com.izforge.izpack.panels.ShortcutPanel.*;


public class ShortcutPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {

    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata)
    {
        xmlShortcut(panelRoot, null);
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData,
            PrintWriter printWriter)
    {
        return false;
    }

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p)
    {
        return false;
    }

    private String read() throws Exception
    {
        byte[] byteArray = {(byte) System.in.read()};
        return new String(byteArray);
    }

    private String readln() throws Exception
    {
        Shell inputShell = Shell.getInstance();
        String input = inputShell.getInput();
        return input.trim();
    }

    public boolean runConsole(AutomatedInstallData installData, ConsoleInstaller parent)
    {
        if (OsVersion.IS_OSX) {
            return true;
        }

        panelActivate(null);

        String menuKind = installData.langpack.getString("ShortcutPanel.regular.StartMenu:Start-Menu");
        if (OsVersion.IS_UNIX && UnixHelper.kdeIsInstalled())
        {
            menuKind = installData.langpack.getString("ShortcutPanel.regular.StartMenu:K-Menu");
        }
        String q1 = StringTool.replace(installData.langpack.getString("ShortcutPanel.regular.create"), "StartMenu", menuKind);

        create = PanelConsoleHelper.askYesNo(q1, true) == AbstractUIHandler.ANSWER_YES;
        while (create) {
            System.out.print(installData.langpack.getString("ShortcutPanel.regular.list") + " [" + suggestedProgramGroup + "]:");
            try
            {
                groupName = readln();
            }
            catch (Exception e)
            {
                System.out.println(e.getStackTrace());
            }
            if (groupName == null || groupName.isEmpty()) groupName = suggestedProgramGroup;
            else {
                String firstInvalidChar = findFirstInvalidCharInProgramGroup(groupName);
                if(firstInvalidChar != null) {
                    System.out.println(String.format(installData.langpack.getString("ShortcutPanel.group.character.error"), firstInvalidChar));
                    continue;
                }
            }
            if (createImmediately) {
                createAndRegisterShortcuts();
            }
            break;
        }
        return true;
    }

    public void createAndRegisterShortcuts()
    {
        createShortcuts(null);
        addToUninstaller();
    }

}
