package com.redhat.installer.installation.maven.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Shell;
import com.izforge.izpack.util.StringTool;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Properties;

public class MavenCheckPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole {
    AutomatedInstallData idata;

    private static final String pomPath = "/org/jboss/component/management/jboss-component-management/6.0.0-redhat-1/jboss-component-management-6.0.0-redhat-1.pom";

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p) {
        return true;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter) {
        return true;
    }

    /* Do not use with jline
    private String read() throws Exception {
        byte[] byteArray = { (byte) System.in.read() };
        return new String(byteArray);
    }

    private String readln() throws Exception {
        String input = read();
        int available = System.in.available();
        if (available > 0) {
            byte[] byteArray = new byte[available];
            System.in.read(byteArray);
            input += new String(byteArray);
        }
        return input.trim();
    }*/

    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata) {
        new MavenCheckPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    private boolean decideToInstall() {
        String info = StringTool.removeHTML(idata.langpack.getString("MavenRepoCheckPanel.info"));
        String stored = idata.getVariable("MAVEN_REPO_PATH");
        String q1 = idata.langpack.getString("MavenRepoCheckPanel.settings.option1");
        String q2 = idata.langpack.getString("MavenRepoCheckPanel.settings.option2");
        String consolePrompt = idata.langpack.getString("MavenRepoCheckPanel.console.prompt");
        boolean prevPath = stored != null;
        Shell console = Shell.getInstance();
        System.out.println(StringTool.removeHTML(info));
        System.out.println(consolePrompt);
        while (true) {
            try {
                System.out.println(" 0 [" + (!prevPath ? "x" : " ") + "] " + StringTool.removeHTML(q1));
                System.out.println(" 1 [" + (prevPath ? "x" : " ") + "] " + StringTool.removeHTML(q2));
                String input = console.getInput();
                if (!input.trim().isEmpty()) {
                    if (input.equals("0")) {
                        idata.setVariable("mavenSettings", "off");
                        return false;
                    } else if (input.equals("1")) {
                        idata.setVariable("mavenSettings", "on");
                        return true;
                    }
                } else {
                    if (prevPath){
                        idata.setVariable("mavenSettings","on");
                    } else {
                        idata.setVariable("mavenSettings","off");
                    }
                    return prevPath;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void chooseSettingsLocation() {
        String settingsInfo = idata.langpack.getString("MavenRepoCheckPanel.settings.location");
        String settingsError = idata.langpack.getString("MavenRepoCheckPanel.settings.error");
        String invalid = idata.langpack.getString("MavenRepoCheckPanel.settings.invalid");
        String invalidNotfile = idata.langpack
                .getString("MavenRepoCheckPanel.settings.invalid.not.file");
        String path = idata.getVariable("USER_HOME") + File.separator + ".m2" + File.separator
                + "settings.xml";

        String input;
        Shell console = Shell.getInstance();

        while (true)
        {
                System.out.println(settingsInfo + "[" + path + "]");
                input = console.getLocation(false);
                if (!input.trim().isEmpty()) {
                    File settingsPath = new File(input);
                    if (settingsPath.getPath().toLowerCase().startsWith("http")){
                        System.out.println(invalid);
                        continue;
                    }
                    if (!settingsPath.exists()) {
                        int answer = PanelConsoleHelper.askYesNo(String.format(settingsError, settingsPath.getAbsolutePath()), false);
                        if (answer == AbstractUIHandler.ANSWER_YES){
                            idata.setVariable("MAVEN_SETTINGS_FULLPATH", settingsPath.toString());
                            break;
                        } else{
                            continue;   
                        }
                    }
                    /**
                     * Check that we have an actual file selected and not just a dir.
                     */
                    if (settingsPath.isDirectory())
                    {
                        System.out.println(invalidNotfile);
                        continue;
                    }

                    if (!validateSettingsLocation(settingsPath)) {
                        continue;
                    }

                    idata.setVariable("MAVEN_SETTINGS_FULLPATH", settingsPath.toString());
                    break;
                }
                else
                {
                    // Use default path
                    File defaultPath = new File(path);
                    if (!validateSettingsLocation(defaultPath)) {
                        continue;
                    }

                    idata.setVariable("MAVEN_SETTINGS_FULLPATH", path);
                    break;
                }
        }
    }

    /**
     * check the given settings.xml against the xsd
     */
    private boolean validateSettingsLocation(File path) {
        try {
            MavenRepoCheckPanel.checkSchema(path);
        } catch (MalformedURLException e) {
            // should never really occur, since we're using a constant as the URL
            e.printStackTrace();
        } catch (SAXException e) {
            // borked internet connection
            if (e.getCause() != null && e.getCause().getClass().equals(UnknownHostException.class)){
                System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.inaccessible"), path.getAbsolutePath()));
            } else {
                // some other parsing error, print it!
                System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.failed"), path.getAbsolutePath(), "\n" + e.getLocalizedMessage()));
                return false;
            }
        }
        // also shouldn't happen, because the IOExceptions are all wrapped by the validator in SAXException, and we're already guaranteed to have both a correct URL
        // and a correct File by this point.
        catch (IOException e) {
            System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.inaccessible"), path.getAbsolutePath()));
        }
        return true;
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent) {
        this.idata = idata;

        String warning = idata.langpack.getString("MavenRepoCheckPanel.warning");
        String error = idata.langpack.getString("MavenRepoCheckPanel.path.error");

        // TODO: either look into izpack updates to make it general, or
        // generalize our own Panels in the console
        // to not need specific methods. this should probably be generalized,
        // but perhaps it's not necessary

        if (decideToInstall()) {
            chooseSettingsLocation();
        }

        int i = askEndOfConsolePanel(idata);
        if (i == 1) {
            return true;
        } else if (i == 2) {
            return false;
        } else {
            return runConsole(idata, parent);
        }
    }
    
    public String getSummaryBody(AutomatedInstallData idata){
        String installQuickstarts = idata.getVariable("installQuickStarts");
        String installRepo = idata.getVariable("mavenSettings");
        if (installQuickstarts == null || installRepo == null){
            return null;
        }
        if (installQuickstarts.equals("false")||installRepo.equals("off")) {
            return null;
        }
        return idata.langpack.getString("MavenRepoCheckPanel.repo.summary")
                + idata.getVariable("MAVEN_REPO_PATH") + "<br>"
                + idata.langpack.getString("MavenRepoCheckPanel.settings.summary")
                + idata.getVariable("MAVEN_SETTINGS_FULLPATH");
    }
}
