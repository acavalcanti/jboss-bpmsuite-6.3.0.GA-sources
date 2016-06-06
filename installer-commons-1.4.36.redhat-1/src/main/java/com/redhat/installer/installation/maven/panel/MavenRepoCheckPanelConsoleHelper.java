package com.redhat.installer.installation.maven.panel;

import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ConsoleInstaller;
import com.izforge.izpack.installer.PanelConsole;
import com.izforge.izpack.installer.PanelConsoleHelper;
import com.izforge.izpack.util.*;
import com.redhat.installer.installation.validator.DirectoryValidator;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Properties;

public class MavenRepoCheckPanelConsoleHelper extends PanelConsoleHelper implements PanelConsole
{
    AutomatedInstallData idata;

    //State of checkboxes
    private boolean mavenRepoSelected;
    private boolean mavenSettingsSelected;
    private String cachedMavenRepo = "";
    private String cachedSettingsLocation = "";
    VariableSubstitutor vs;

    private static final String pomPath = "/org/jboss/component/management/jboss-component-management/6.0.0-redhat-1/jboss-component-management-6.0.0-redhat-1.pom";

    public boolean runConsoleFromPropertiesFile(AutomatedInstallData installData, Properties p) {
        return true;
    }

    public boolean runGeneratePropertiesFile(AutomatedInstallData installData, PrintWriter printWriter) {
        return true;
    }
    public void makeXMLData(IXMLElement panelRoot, AutomatedInstallData idata) {
        new MavenRepoCheckPanelAutomationHelper().makeXMLData(idata, panelRoot);
    }

    private void decideToInstall()
    {
        String info = idata.langpack.getString("MavenRepoCheckPanel.info");
        String repoDefault = idata.langpack.getString("MavenRepoCheckPanel.repo.option1");
        String repoCustom = idata.langpack.getString("MavenRepoCheckPanel.repo.option2");
        String repoPrompt = idata.langpack.getString("MavenRepoCheckPanel.repo.location");
        String settingsDefault = String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.option1"),idata.getVariable("USER_HOME"));
        String settingsCustom = idata.langpack.getString("MavenRepoCheckPanel.settings.option2");
        String settingsPrompt = idata.langpack.getString("MavenRepoCheckPanel.settings.location");


        System.out.println(info);
        mavenRepoSelected = makeChoice(repoPrompt, repoDefault, repoCustom, mavenRepoSelected);
        if (mavenRepoSelected)
        {
            chooseRepoLocation();
        }
        else
        {
            idata.setVariable("MAVEN_REPO_PATH",
                    vs.substitute(idata.getVariable("MAVEN_REPO_PATH.default")));
        }


        mavenSettingsSelected = makeChoice(settingsPrompt, settingsDefault, settingsCustom, mavenSettingsSelected);
        if (mavenSettingsSelected)
        {
            chooseSettingsLocation();
        }
        else
        {
            idata.setVariable("MAVEN_SETTINGS_FULLPATH",
                    vs.substitute(idata.getVariable("MAVEN_SETTINGS_FULLPATH.default")));
        }
    }

    /**
     * Returns true if the user chooses customAnswer or inputs nothing and a previous value exists for the question (indicated by previous)
     *
     * @param prompt the string to ask the user about
     * @param defaultAnswer the answer which, if chosen, returns false
     * @param customAnswer the answer which, if chosen, returns true
     * @param previous the true/false value which should be returned if the user inputs nothing
     * @return the result of the above
     */
    private boolean makeChoice(String prompt, String defaultAnswer, String customAnswer, boolean previous)
    {
        Shell console = Shell.getInstance();
        System.out.println(prompt);
        while (true)
        {
            try
            {
                System.out.println(" 0 [" + (!previous ? "x" : " ") + "] " + StringTool.removeHTML(defaultAnswer));
                System.out.println(" 1 [" + (previous ? "x" : " ") + "] " + StringTool.removeHTML(customAnswer));
                String input = console.getInput();
                if (!input.trim().isEmpty())
                {
                    if (input.equals("0"))
                    {
                        return false;
                    }
                    else if (input.equals("1"))
                    {
                        return true;
                    }
                }
                else
                {
                    return previous;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void chooseRepoLocation()
    {
        String prompt = idata.langpack.getString("MavenRepoCheckPanel.repo.option2");
        String error = idata.langpack.getString("MavenRepoCheckPanel.path.error");
        String urlType = "file://";
        String path = vs.substitute(idata.getVariable("MAVEN_REPO_PATH.default"));
        String input;
        Shell console = Shell.getInstance();

        if (!cachedMavenRepo.isEmpty())
        {
            //cachedMavenRepo
            path = cachedMavenRepo;
        }

        while (true) {
            System.out.println(prompt + " [" + path + "]");
            input = console.getLocation(true);
            if (!input.trim().isEmpty()) {
                String currentPath = input;

                boolean isUrl = currentPath.toLowerCase().startsWith("http");
                if (!isUrl) {
                    File chosenPath = new File(currentPath);
                    if (!chosenPath.isDirectory() || !chosenPath.exists()) {
                        System.out.println(error);
                        continue;
                    }

                    input = urlType + input;
                    break;
                } else {
                    if (!IoHelper.remoteFileExists(input)) {
                        System.out.println(error);
                    } else {
                        break;
                    }
                }
            } else {
                input = path;
                break;
            }
        }
        cachedMavenRepo = input;
        idata.setVariable("MAVEN_REPO_PATH", input);
    }

    private void chooseSettingsLocation()
    {
        String settingsInfo = idata.langpack.getString("MavenRepoCheckPanel.settings.option2");
        String settingsError = idata.langpack.getString("MavenRepoCheckPanel.settings.error");
        String invalid = idata.langpack.getString("MavenRepoCheckPanel.settings.invalid");
        String invalidNotfile = idata.langpack
                .getString("MavenRepoCheckPanel.settings.invalid.not.file");
        String path =  vs.substitute(idata.getVariable("MAVEN_SETTINGS_FULLPATH.default"));

        String input;
        if (!cachedSettingsLocation.isEmpty())
        {
            path = cachedSettingsLocation;
        }
        Shell console = Shell.getInstance();

        while (true) {
            System.out.println(settingsInfo + " [" + path + "]");
            input = console.getLocation(false);
            if (input.trim().isEmpty()) {
                input = path;
            }

            File settingsPath = new File(input);
            if (settingsPath.getPath().toLowerCase().startsWith("http")) {
                System.out.println(invalid);
                continue;
            }

            /**
             * Check that we have an actual file selected and not just a dir.
             */
            if (settingsPath.isDirectory()) {
                System.out.println(invalidNotfile);
                continue;
            }

            int lastSlash = input.lastIndexOf(File.separator);
            String targetDir = "";
            if (lastSlash != -1) {
                targetDir = input.substring(0, lastSlash);
            }


            if (!settingsPath.exists()) {
                //Check that we can write into the directory where settings.xml should be located
                File existingParent = IoHelper.existingParent(settingsPath);

                if (existingParent == null) {
                    System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.drive.error"), settingsPath.getAbsolutePath().substring(0, 2)));
                    continue;
                } else if (!(existingParent.canWrite() && existingParent.canExecute())) {
                    System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.dir.error"), existingParent.getAbsoluteFile().getPath()));
                    continue;
                } else if (!DirectoryValidator.validate(new File(targetDir))) {
                    System.out.println(String.format(idata.langpack.getString("DirectoryValidator.invalid"), DirectoryValidator.getInvalidCharacters()));
                    continue;
                }

                int answer = PanelConsoleHelper.askYesNo(String.format(settingsError, settingsPath.getAbsolutePath()), false);
                if (answer == AbstractUIHandler.ANSWER_YES) {
                    cachedSettingsLocation = settingsPath.getAbsolutePath();
                    idata.setVariable("MAVEN_SETTINGS_FULLPATH", settingsPath.getAbsolutePath());
                    break;
                } else {
                    continue;
                }
            } else {
                if (!settingsPath.canWrite()) {
                    System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.file.error"), settingsPath.getAbsoluteFile().getPath()));
                    continue;
                }
                try {
                    MavenRepoCheckPanel.checkSchema(settingsPath);
                } catch (MalformedURLException e) {
                    // should never really occur, since we're using a constant as the URL
                    e.printStackTrace();
                } catch (SAXException e) {
                    // borked internet connection
                    if (e.getCause() != null && e.getCause().getClass().equals(UnknownHostException.class)){
                        System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.inaccessible"), settingsPath.getAbsolutePath()));
                    } else {
                        // some other parsing error, print it!
                        System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.failed"), settingsPath.getAbsolutePath(), "\n" + e.getLocalizedMessage()));
                        continue;
                    }
                }
                // also shouldn't happen, because the IOExceptions are all wrapped by the validator in SAXException, and we're already guaranteed to have both a correct URL
                // and a correct File by this point.
                catch (IOException e) {
                    System.out.println(String.format(idata.langpack.getString("MavenRepoCheckPanel.settings.schema.inaccessible"), settingsPath.getAbsolutePath()));
                }
            }
            cachedSettingsLocation = settingsPath.getAbsolutePath();
            idata.setVariable("MAVEN_SETTINGS_FULLPATH", settingsPath.getAbsolutePath());
            break;
        }
    }

    public boolean runConsole(AutomatedInstallData idata, ConsoleInstaller parent)
    {
        this.idata = idata;
        vs = new VariableSubstitutor(idata.getVariables());
        // TODO: either look into izpack updates to make it general, or
        // generalize our own Panels in the console
        // to not need specific methods. this should probably be generalized,
        // but perhaps it's not necessary
        decideToInstall();

        int i = askEndOfConsolePanel(idata);
        if (i == 1)
        {
            return true;
        }
        else if (i == 2)
        {
            return false;
        }
        else
        {
            return runConsole(idata, parent);
        }
    }

    public String getSummaryBody(AutomatedInstallData idata)
    {
        String installQuickstarts = idata.getVariable("installQuickStarts");
        String installRepo = idata.getVariable("mavenSettings");
        if (installQuickstarts == null || installRepo == null)
        {
            return null;
        }
        if (installQuickstarts.equals("false") || installRepo.equals("off"))
        {
            return null;
        }
        return idata.langpack.getString("MavenRepoCheckPanel.repo.summary")
                + " " + idata.getVariable("MAVEN_REPO_PATH") + "<br>"
                + idata.langpack.getString("MavenRepoCheckPanel.settings.summary")
                + " " + idata.getVariable("MAVEN_SETTINGS_FULLPATH");
    }
}
