/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2007 Dennis Reil
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
package com.izforge.izpack.installer;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import com.izforge.izpack.CustomData;
import com.izforge.izpack.ExecutableFile;
import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.Panel;
import com.izforge.izpack.adaptator.IXMLElement;
import com.izforge.izpack.adaptator.IXMLWriter;
import com.izforge.izpack.adaptator.impl.XMLElementImpl;
import com.izforge.izpack.adaptator.impl.XMLWriter;
import com.izforge.izpack.installer.AutomatedInstallData.ConsoleInfo;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.panels.SkippableDataValidator;
import com.izforge.izpack.rules.RulesEngine;
import com.izforge.izpack.util.AbstractUIHandler;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.OsConstraint;
import com.izforge.izpack.util.Shell;
import com.izforge.izpack.util.StringTool;
import com.izforge.izpack.util.VariableSubstitutor;


/**
 * Runs the console installer
 * 
 * @author Mounir el hajj
 */
public class ConsoleInstaller extends InstallerBase
{

    private AutomatedInstallData installdata;

    private boolean result = false;

    private Properties properties;

    private PrintWriter printWriter;

    private ZipOutputStream outJar;
    

    private static String EAP = "eap";
    private static String DEFAULT_LANG = "eng";
    
    public ConsoleInstaller(String langcode) throws Exception
    {
        super();
        init(langcode);
    }

    public ConsoleInstaller(String langcode, Map<String,String> argVariables) throws Exception {
        init(langcode);
        loadCommandLineVariables(argVariables, installdata);
    }

    private void init(String langcode) throws Exception{
        installdata = new AutomatedInstallData();
        loadInstallData(this.installdata);
        Shell console = Shell.getInstance();
        if(!installdata.getVariable("product.name").equals("eap")) // If not EAP skip language selection
        {
            this.installdata.localeISO3 = DEFAULT_LANG;
        }
        else if (langcode == null) {
            // The first langpack available in list also becomes the default backup, in case
            // a string can't be found in the user selected pack.
            InstallerBase.setLangBackup((String) getAvailableLangPacks().get(0));
            System.out.println("Select language : ");
            for (String i : getAvailableLangPacks()) {
                System.out.println(getAvailableLangPacks().indexOf(i) + ": " + i);
            }
            int val = 0;
            int def = getAvailableLangPacks().indexOf(Locale.getDefault().getISO3Language().toLowerCase());
            while (true) {
                System.out.println("Please choose ["+def+"] : ");
                //String in = readln();
                String in = console.getInput();
                //Shell console = new Shell();
                //String in =  console.getInput();
                if (in.trim().length() != 0) {
                    try {
                        val = Integer.valueOf(in);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (val >= 0 && val <= getAvailableLangPacks().size()-1) {
                        this.installdata.localeISO3 = getAvailableLangPacks().get(val);
                        break;
                    }
                } else {
                    this.installdata.localeISO3 = getAvailableLangPacks().get(def);
                    break;
                }
            }
        } else if (!getAvailableLangPacks().contains(langcode)) {
            if (this.installdata.localeISO3 != null) {
                System.out.println("Language " + langcode + " not found in supported langpacks");
            }
            this.installdata.localeISO3 = getAvailableLangPacks().get(0);
        } else {
            this.installdata.localeISO3 = langcode;
        }

        // We add an xml data information
        this.installdata.xmlData.setAttribute("langpack", this.installdata.localeISO3);
        InputStream in = getClass().getResourceAsStream(
                "/langpacks/" + this.installdata.localeISO3 + ".xml");
        //Add product version and name to xml data
        IXMLElement productNameNode = new XMLElementImpl(AutomatedInstaller.PRODUCT_NAME_TAG, this.installdata.xmlData);
        productNameNode.setContent(this.installdata.getVariable("product.name").toUpperCase());
        this.installdata.xmlData.addChild(productNameNode);
        IXMLElement productVersionNode = new XMLElementImpl(AutomatedInstaller.PRODUCT_VERSION_TAG, this.installdata.xmlData);
        productVersionNode.setContent(this.installdata.info.getAppVersion());
        this.installdata.xmlData.addChild(productVersionNode);

        this.installdata.langpack = new LocaleDatabase(in);
        this.installdata.setVariable(ScriptParser.ISO3_LANG, this.installdata.localeISO3);
        ResourceManager.create(this.installdata);
        loadConditions(installdata);
        loadInstallerRequirements();
        loadDynamicVariables();
        if (!checkInstallerRequirements(installdata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            return;
        }
        // add backup custom langpack if it exists:
        addBackupLangpack(installdata);

        addCustomLangpack(installdata);
        checkJavaVersion();

        installdata.setVariable ("installerMode", "CLI");
    }


   /* Checks the Java version.
    *
    * @throws Exception Description of the Exception
    */
    private void checkJavaVersion() throws Exception
    {
        String version = System.getProperty("java.version");
        String required = this.installdata.info.getJavaVersion();
        if (version.compareTo(required) < 0)
        {
            String msgError = this.installdata.langpack.getString("java.mismatch.error");
            msgError = msgError.replaceFirst("@@", required).replaceFirst("@@", version);
            System.out.println(StringTool.formatForNewLine(msgError));
            System.exit(1);
        }
    }

    private String read() throws Exception
    {
        byte[] byteArray = {(byte) System.in.read()};
        return new String(byteArray);
    }

    private String readln() throws Exception
    {
        String input = read();
        int available = System.in.available();
        if (available > 0)
        {
            byte[] byteArray = new byte[available];
            System.in.read(byteArray);
            input += new String(byteArray);
        }
        return input.trim();
    }

    protected void iterateAndPerformAction(String strAction) throws Exception
    {
        if (!checkInstallerRequirements(this.installdata))
        {
            Debug.log("not all installerconditions are fulfilled.");
            return;
        }
        Debug.log("[ Starting console installation ] " + strAction);

        try
        {
            this.result = true;
            Iterator<Panel> panelsIterator = this.installdata.panelsOrder.iterator();
            this.installdata.curPanelNumber = -1;
            VariableSubstitutor substitutor = new VariableSubstitutor(this.installdata.getVariables());
            while (panelsIterator.hasNext())
            {
                Panel p = panelsIterator.next();
                                
                this.installdata.curPanelNumber++;
                String praefix = "com.izforge.izpack.panels.";
                if (p.className.compareTo(".") > -1)
                {
                    praefix = "";
                }
                if (!OsConstraint.oneMatchesCurrentSystem(p.osConstraints))
                {
                    continue;
                }
                IXMLElement panelRoot = new XMLElementImpl(p.className, installdata.xmlData);
                String panelId = p.getPanelid();
                if (panelId != null)
                {
                    installdata.setVariable("panelID", panelId);
                    panelRoot.setAttribute("id", panelId);
                }
                installdata.xmlData.addChild(panelRoot);
                String panelClassName = p.className;
                String consoleHelperClassName = praefix + panelClassName + "ConsoleHelper";

                Class<PanelConsole> consoleHelperClass = null;

                Debug.log("ConsoleHelper:" + consoleHelperClassName);

                try
                {
                    // here we call "preconstruction" actions
                    runPreConstructionActions(p);
                    consoleHelperClass = (Class<PanelConsole>) Class.forName(consoleHelperClassName);

                }
                catch (ClassNotFoundException e)
                {
                    Debug.log("ClassNotFoundException-skip :" + consoleHelperClassName);
                    //Work around to get actions for summary panel to run (Since SummaryPanel not displayed in console
                    if (consoleHelperClassName.endsWith("SummaryPanelConsoleHelper"))
                    {
                        String panelCondition = p.getCondition();
                        if (panelCondition == null || installdata.getRules().isConditionTrue(panelCondition))
                        {
                            runPostValidationActions(p);
                        }
                    }
                    installdata.consoles.add(null);
                    continue;
                }
                PanelConsole consoleHelperInstance = null;
                if (consoleHelperClass != null)
                {
                    try
                    {
                        Debug.log("Instantiate :" + consoleHelperClassName);
                        refreshDynamicVariables(substitutor, installdata);
                        consoleHelperInstance = consoleHelperClass.newInstance();
                    }
                    catch (Exception e)
                    {
                        Debug.log("ERROR: no default constructor for " + consoleHelperClassName + ", skipping...");
                        installdata.consoles.add(new ConsoleInfo(consoleHelperInstance, p));
                        continue;
                    }
                }
                installdata.consoles.add(new ConsoleInfo(consoleHelperInstance, p));
                if (consoleHelperInstance != null)
                {
                    try
                    {
                        Debug.log("consoleHelperInstance." + strAction + ":"
                                + consoleHelperClassName + " entered.");
                        boolean bActionResult = true;
                        boolean bIsConditionFulfilled = true;
                        String strCondition = p.getCondition();
                        if (strCondition != null)
                        {
                            bIsConditionFulfilled = installdata.getRules().isConditionTrue(
                                    strCondition);
                        }

                        if (strAction.equals("doInstall") && bIsConditionFulfilled)
                        {
                            // here we need to execute actions in the 'preactivate' stage
                            runPreActivateActions(p);
                            do
                            {
                                bActionResult = consoleHelperInstance.runConsole(this.installdata, this);
                                if (!bActionResult) { break;} // Skip validation and quit if runConsole returns false.
                            }
                            while (!validatePanel(p));
                        }
                        else if (strAction.equals("doGeneratePropertiesFile"))
                        {
                            bActionResult = consoleHelperInstance.runGeneratePropertiesFile(
                                    this.installdata, this.printWriter);
                        }
                        else if (strAction.equals("doInstallFromPropertiesFile")
                                && bIsConditionFulfilled)
                        {
                            bActionResult = consoleHelperInstance.runConsoleFromPropertiesFile(
                                    this.installdata, this.properties);
                        }
                        if (!bActionResult)
                        {
                            this.result = false;
                            return;
                        }
                        else
                        {
                            Debug.log("consoleHelperInstance." + strAction + ":"
                                    + consoleHelperClassName + " successfully done.");
                        }

                        /**
                         * Run post-validationa actions only if the panel's display condition
                         * is fulfilled.
                         */
                        if (bIsConditionFulfilled)
                        {
                            runPostValidationActions(p);
                        }
                    }
                    catch (InstallerException e)
                    {
                        /**
                         * A panel that fails to validate will throw an InstallerException after
                         * printing out an error message. We can handle that exception here by
                         * exiting gracefully. TODO: Handle other exceptions?
                         */
                        Debug.log("ERROR: console installation failed for panel " + panelClassName);
                        System.out.println("Console Installation failed for panel "
                                + panelClassName);
                        this.result = false;
                        // For Debugging
                        // System.err.println(e.toString());
                        // e.printStackTrace();
                        Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
                        Shell.getInstance().stopShell();
                        System.exit(1);
                    }
                    catch (Exception e)
                    {
                        Debug.log("Exception Caught: " + e.getMessage());
                    }

                }

            }
            Shell.getInstance().stopShell();
            if (this.result)
            {
                // we get the reference to outJar now
                // since uninstallOutJar "should" not
                // return null but instead the real
                // thing
                outJar = installdata.uninstallOutJar;
                writeUninstallData();
                System.out.println("[ Console installation done ]");
            }
            else
            {
                System.out.println("[ Console installation FAILED! ]");
            }
        }
        catch (Exception e)
        {
            this.result = false;
            System.err.println(e.toString());
            e.printStackTrace();
            System.out.println("[ Console installation FAILED! ]");
            Shell.getInstance().stopShell();
        }

    }

    /*
    Lots of duplicate code below. Unfortunately, the different GUI / Console / Automated installs all do this differently for
    whatever (bad) reason. Ideally they'd all be refactored to use the same underlying panel representation. Move to 5?
     */

    private void runPreConstructionActions(Panel p){
        List<String> preConstructionActions = p.getPreConstructionActions();
        if (preConstructionActions != null){
            for (int actionIndex = 0; actionIndex < preConstructionActions.size(); actionIndex++){
                String panelActionClass = preConstructionActions.get(actionIndex);
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(p.getPanelActionConfiguration(panelActionClass));
                action.executeAction(installdata, null);
            }
        }
    }

    private void runPreActivateActions(Panel p){
        List<String> preActivateActions = p.getPreActivationActions();
        if (preActivateActions != null){
            for (int actionIndex = 0; actionIndex < preActivateActions.size(); actionIndex++){
                String panelActionClass = preActivateActions.get(actionIndex);
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(p.getPanelActionConfiguration(panelActionClass));
                action.executeAction(installdata, null);
            }
        }
    }

    private void runPreValidateActions(Panel p){
        List<String> preValidateActions = p.getPreValidationActions();
        if (preValidateActions != null){
            for (int actionIndex = 0; actionIndex < preValidateActions.size(); actionIndex++){
                String panelActionClass = preValidateActions.get(actionIndex);
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(p.getPanelActionConfiguration(panelActionClass));
                action.executeAction(installdata, null);
            }
        }
    }

    private void runPostValidationActions(Panel p){
        List<String> postValidateActions = p.getPostValidationActions();
        if (postValidateActions != null)
        {
            for (int actionIndex = 0; actionIndex < postValidateActions.size(); actionIndex++) {
                String panelActionClass = postValidateActions.get(actionIndex);
                PanelAction action = PanelActionFactory.createPanelAction(panelActionClass);
                action.initialize(p.getPanelActionConfiguration(panelActionClass));
                // run the action
                action.executeAction(installdata, null);
            }
        }
    }
    private void writeUninstallData() {
        // Show whether a separated logfile should be also written or not.
        String logfile = installdata.getVariable("InstallerFrame.logfilePath");
        BufferedWriter extLogWriter = null;
        if (logfile != null) {
            if (logfile.toLowerCase().startsWith("default")) {
                logfile = installdata.info.getUninstallerPath() + "/install.log";
            }
            logfile = IoHelper.translatePath(logfile, new VariableSubstitutor(installdata
                    .getVariables()));
            File outFile = new File(logfile);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outFile);
            }
            catch (FileNotFoundException e) { 
                Debug.trace("Cannot create logfile!");
                Debug.error(e);
            }
            if (out != null) {
                extLogWriter = new BufferedWriter(new OutputStreamWriter(out));
            }
        }
        try {
            String condition = installdata.getVariable("UNINSTALLER_CONDITION");
            if (condition != null) {
                if (!RulesEngine.getCondition(condition).isTrue()) {
                    // condition for creating the uninstaller is not fulfilled.
                    return;
                }
            }
            // We get the data
            UninstallData udata = UninstallData.getInstance();
            List files = udata.getUninstalableFilesList();
            ZipOutputStream outJar = installdata.uninstallOutJar;

            if (outJar == null) {
                return;
            }

            // We write the files log
            outJar.putNextEntry(new ZipEntry("install.log"));
            BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(installdata.getInstallPath());
            logWriter.newLine();
            Iterator iter = files.iterator();
            // hacks for quickstarts
            String quickstarts = installdata.getVariable("installQuickStarts");
            if (quickstarts != null){
            	if (quickstarts.equals("true")){
            		logWriter.write("installQuickStarts=true");
            		logWriter.newLine();
            		logWriter.write(installdata.getVariable("install.quickstarts.path")+"/"+installdata.getVariable("quickstarts.subdir"));
            		logWriter.newLine();
            	}
            }
            if (extLogWriter != null) { // Write intern (in uninstaller.jar) and extern log file.
                while (iter.hasNext()) {
                    String txt = (String) iter.next();
                    logWriter.write(txt);
                    extLogWriter.write(txt);
                    if (iter.hasNext()) {
                        logWriter.newLine();
                        extLogWriter.newLine();
                    }
                }
                logWriter.flush();
                extLogWriter.flush();
                extLogWriter.close();
            } else {
                while (iter.hasNext()) {
                    logWriter.write((String) iter.next());
                    if (iter.hasNext()) {
                        logWriter.newLine();
                    }
                }
                logWriter.flush();
            }
            outJar.closeEntry();

            // We write the uninstaller jar file log
            outJar.putNextEntry(new ZipEntry("jarlocation.log"));
            logWriter = new BufferedWriter(new OutputStreamWriter(outJar));
            logWriter.write(udata.getUninstallerJarFilename());
            logWriter.newLine();
            logWriter.write(udata.getUninstallerPath());
            logWriter.flush();
            outJar.closeEntry();

            // Write out executables to execute on uninstall
            outJar.putNextEntry(new ZipEntry("executables"));
            ObjectOutputStream execStream = new ObjectOutputStream(outJar);
            iter = udata.getExecutablesList().iterator();
            execStream.writeInt(udata.getExecutablesList().size());
            while (iter.hasNext()) {
                ExecutableFile file = (ExecutableFile) iter.next();
                execStream.writeObject(file);
            }
            execStream.flush();
            outJar.closeEntry();

            // Write out additional uninstall data
            // Do not "kill" the installation if there is a problem
            // with custom uninstall data. Therefore log it to Debug,
            // but do not throw.
            Map<String, Object> additionalData = udata.getAdditionalData();
            if (additionalData != null && !additionalData.isEmpty()) {
                Iterator<String> keys = additionalData.keySet().iterator();
                HashSet<String> exist = new HashSet<String>();
                while (keys != null && keys.hasNext()) {
                    String key = keys.next();
                    Object contents = additionalData.get(key);
                    if ("__uninstallLibs__".equals(key)) {
                        Iterator nativeLibIter = ((List) contents).iterator();
                        while (nativeLibIter != null && nativeLibIter.hasNext()) {
                            String nativeLibName = (String) ((List) nativeLibIter.next()).get(0);
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            outJar.putNextEntry(new ZipEntry("native/" + nativeLibName));
                            InputStream in = getClass().getResourceAsStream(
                                    "/native/" + nativeLibName);
                            while ((bytesInBuffer = in.read(buffer)) != -1) {
                                outJar.write(buffer, 0, bytesInBuffer);
                                bytesCopied += bytesInBuffer;
                            }
                            outJar.closeEntry();
                        }
                    } else if ("uninstallerListeners".equals(key) || "uninstallerJars".equals(key)) { // It is a ArrayList of ArrayLists which contains the
                        // full
                        // package paths of all needed class files.
                        // First we create a new ArrayList which contains only
                        // the full paths for the uninstall listener self; thats
                        // the first entry of each sub ArrayList.
                        ArrayList<String> subContents = new ArrayList<String>();

                        // Secound put the class into uninstaller.jar
                        Iterator listenerIter = ((List) contents).iterator();
                        while (listenerIter.hasNext()) {
                            byte[] buffer = new byte[5120];
                            long bytesCopied = 0;
                            int bytesInBuffer;
                            CustomData customData = (CustomData) listenerIter.next();
                            // First element of the list contains the listener
                            // class path;
                            // remind it for later.
                            if (customData.listenerName != null) {
                                subContents.add(customData.listenerName);
                            }
                            Iterator<String> liClaIter = customData.contents.iterator();
                            while (liClaIter.hasNext()) {
                               String contentPath = liClaIter.next();
                                if (exist.contains(contentPath)) {
                                    continue;
                                }
                                exist.add(contentPath);
                                try {
                                    outJar.putNextEntry(new ZipEntry(contentPath));
                                }
                                catch (ZipException ze) { // Ignore, or ignore not ?? May be it is a
                                    // exception because
                                    // a doubled entry was tried, then we should
                                    // ignore ...
                                    Debug.trace("ZipException in writing custom data: "
                                            + ze.getMessage());
                                    continue;
                                }
                                InputStream in = getClass().getResourceAsStream("/" + contentPath);
                                if (in != null) {
                                    while ((bytesInBuffer = in.read(buffer)) != -1) {
                                        outJar.write(buffer, 0, bytesInBuffer);
                                        bytesCopied += bytesInBuffer;
                                    }
                                } else {
                                    Debug.trace("custom data not found: " + contentPath);
                                }
                                outJar.closeEntry();

                            }
                        }
                        // Third we write the list into the
                        // uninstaller.jar
                        outJar.putNextEntry(new ZipEntry(key));
                        ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                        objOut.writeObject(subContents);
                        objOut.flush();
                        outJar.closeEntry();

                    } else {
                        outJar.putNextEntry(new ZipEntry(key));
                        if (contents instanceof ByteArrayOutputStream) {
                            ((ByteArrayOutputStream) contents).writeTo(outJar);
                        } else {
                            ObjectOutputStream objOut = new ObjectOutputStream(outJar);
                            objOut.writeObject(contents);
                            objOut.flush();
                        }
                        outJar.closeEntry();
                    }
               }
            }

            // write the script files, which will
            // perform several complement and unindependend uninstall actions
            ArrayList<String> unInstallScripts = udata.getUninstallScripts();
            Iterator<String> unInstallIter = unInstallScripts.iterator();
            ObjectOutputStream rootStream;
            int idx = 0;
            while (unInstallIter.hasNext()) {
                outJar.putNextEntry(new ZipEntry(UninstallData.ROOTSCRIPT + Integer.toString(idx)));
                rootStream = new ObjectOutputStream(outJar);
                String unInstallScript = (String) unInstallIter.next();
                rootStream.writeUTF(unInstallScript);
                rootStream.flush();
                outJar.closeEntry();
                idx++;
            }

            // Cleanup
            outJar.flush();
            outJar.close();
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }

    protected void doInstall() throws Exception
    {
        try
        {
            iterateAndPerformAction("doInstall");
        }
        catch (Exception e)
        {
            throw e;
        }

        finally
        {
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    protected void doGeneratePropertiesFile(String strFile) throws Exception
    {
        try
        {
            this.printWriter = new PrintWriter(strFile);
            iterateAndPerformAction("doGeneratePropertiesFile");
            this.printWriter.flush();
        }
        catch (Exception e)
        {
            throw e;
        }

        finally
        {
            this.printWriter.close();
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }

    }

    protected void doInstallFromPropertiesFile(String strFile) throws Exception
    {
        FileInputStream in = new FileInputStream(strFile);
        try
        {
            properties = new Properties();
            properties.load(in);
            iterateAndPerformAction("doInstallFromPropertiesFile");
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            in.close();
            Housekeeper.getInstance().shutDown(this.result ? 0 : 1);
        }
    }

    /**
     * Validate a panel.
     * 
     * @param p The panel to validate
     * @return The status of the validation - false makes the installation fail
     */
    private boolean validatePanel(final Panel p) throws InstallerException
    {
        boolean returnVal = true;
        List<String> dataValidators = p.getValidators();
        // before validation begins, we need to execute all "preValidate" stage actions
        runPreValidateActions(p);
        if (dataValidators != null)
        {
            for (String dataValidator : dataValidators)
            {
                DataValidator validator = DataValidatorFactory.createDataValidator(dataValidator);
                Status validationResult = validator.validateData(installdata);
                if (validationResult != DataValidator.Status.OK)
                {
                    if (validationResult == Status.WARNING) {

                        String message;
                        if (installdata.langpack.containsKey(validator.getWarningMessageId() + ".console"))
                            message = installdata.langpack.getString(validator.getWarningMessageId()+".console");
                        else if (installdata.langpack.containsKey(validator.getErrorMessageId() + ".console"))
                            message = installdata.langpack.getString(validator.getErrorMessageId()+".console");
                        else
                            message = validator.getFormattedMessage();

                        int answer = PanelConsoleHelper.askYesNo(message, false);
                        if (answer == AbstractUIHandler.ANSWER_YES) {
                            returnVal = true;
                            continue;
                        }
                        else
                        {
                            return false;
                        }
                    } else if (validationResult == Status.SKIP) {

                        /**
                         * Custom actions for validators with SKIP options go here:
                         */
                        if (SkippableDataValidator.class.isAssignableFrom(validator.getClass())) {
                            SkippableDataValidator skipValidator = (SkippableDataValidator) validator;

                            String msg = skipValidator.getFormattedMessage() +
                                    System.getProperty("line.separator") +
                                    installdata.langpack.getString(skipValidator.getConsoleOptionsId()) +
                                    System.getProperty("line.separator");

                            int userInput = PanelConsoleHelper.askEndOfConsolePanel(installdata, msg);

                            returnVal = skipValidator.skipActions(installdata, userInput);
                            if (!returnVal) return false;
                        } else {
                            returnVal = true;
                        }

                    } else if (validationResult == Status.ERROR){
                        System.out.println("\n"
                                + installdata.langpack.getString("UserInputPanel.console.error")
                                + ": " + validator.getFormattedMessage() + "\n");
                        return false;
                    } else { // Status.FAIL case
                        /**
                         * make installation fail instantly by throwing exception otherwise we can
                         * get stuck in an infinite loop.
                         */
                        throw new InstallerException("Validating data for panel " + p.getPanelid()
                                + " was not successfull");
                    }
                    
                }
            }
        }
        return returnVal;
    }

    public void run(int type, String path) throws Exception
    {
        switch (type)
        {
            case Installer.CONSOLE_GEN_TEMPLATE:
                doGeneratePropertiesFile(path);
                break;

            case Installer.CONSOLE_FROM_TEMPLATE:
                doInstallFromPropertiesFile(path);
                break;
                
            default:
                doInstall();
        }
    }

    /**
     * Writes an XML tree.
     *
     * @param root The XML tree to write out.
     * @param out  The stream to write on.
     *
     * @throws Exception Description of the Exception
     */
    public void writeXMLTree(IXMLElement root, OutputStream out) throws Exception {
        IXMLWriter writer = new XMLWriter(out);
        for (int i = 0; i < installdata.consoles.size(); i++) {
            ConsoleInfo consoleInfo = installdata.consoles.get(i);
            if (consoleInfo != null) {
                PanelConsole panel = consoleInfo.console;
                if (panel instanceof PanelConsoleHelper) {
                    String strCondition = installdata.consoles.get(i).panel.getCondition();
                    if (strCondition == null || installdata.getRules().isConditionTrue(strCondition)) {
                        //add the +2 because we added product and version as first two nodes
                        ((PanelConsoleHelper) panel).makeXMLData(installdata.xmlData.getChildAtIndex(i+2), installdata);
                    }
                }
            }
        }
        writer.write(root);

    }
}
