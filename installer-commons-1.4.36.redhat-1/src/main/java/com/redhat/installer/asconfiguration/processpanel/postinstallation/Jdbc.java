package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.ascontroller.ServerCommandsHelper;
import com.redhat.installer.asconfiguration.ascontroller.ServerManager;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.jboss.dmr.ModelNode;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Jdbc extends PostInstallation{

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        mHandler = handler;
        serverCommands = initServerCommands(Jdbc.class);
        try {
            ServerCommandsHelper.connectContext(handler, serverCommands);
        } catch (InterruptedException ie){
            ie.printStackTrace();
            return false;
        }
        List<ModelNode> commandResults = installJdbcDriver();
        serverCommands.terminateSession();

        if (commandResults != null){
            return installResult(commandResults);
        } else {
            return false;
        }

    }

    /**
     * Copies a list of files into the appropriate module directory. Creates the directory if it doesn't exist
     * @param jarFiles list of files to copy
     * @param moduleDir the location to copy to
     * TODO: not necessary for this to be particular to JDBC. can refactor out to a new class for general usage with general
     * TODO: messages
     * @return
     */
    static boolean createJdbcFiles(List<String> jarFiles, String moduleDir) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        try {
            File moduleFile = createDirectory(idata.getInstallPath(), moduleDir);
            if (!Boolean.parseBoolean(idata.getVariable("jdbc.driver.preexisting"))) {
                copyJarToModulesDir(jarFiles, moduleFile);
            }
        } catch (Throwable e) {
            ProcessPanelHelper.printToPanel(mHandler,
                    idata.langpack.getString("postinstall.processpanel.jarcopy.error"), true);
            ProcessPanelHelper.printToPanel(mHandler,
                    e.getMessage(), true);
            return false;
        }
        return true;
    }

    /**
     * Creates a module xml in the given location.
     * @param moduleDirectory the location to create the 'module.xml'
     * @param moduleName the name of the module to include within the 'module.xml'
     * @param resources the list of files the be included in the resources section of the 'module.xml'
     * @return
     */
    public static boolean createJDBCModuleXml(String moduleDirectory, String moduleName, List<String> resources) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String jbossHome = idata.getInstallPath();
        String moduleXmlPath = jbossHome + File.separator + moduleDirectory + File.separator + "module.xml";
        List<String> resourceNames = new ArrayList<String>();
        List<String> deps = new ArrayList<String>();
        boolean preexisting = Boolean.parseBoolean(idata.getVariable("jdbc.driver.preexisting"));

        deps.add("javax.api");
        deps.add("javax.transaction.api");


        for (String resource : resources) {
            File resourceFile = new File(resource);
            if (!preexisting) {
                resourceNames.add(resourceFile.getName());
            } else {
                String relativePathToJar = getRelativePath(new File(moduleXmlPath), resourceFile);
                resourceNames.add(relativePathToJar);
            }
        }

        try {
            serverCommands.createModuleXml(jbossHome, moduleDirectory, moduleName, resourceNames, deps);
        } catch (ParserConfigurationException e) {
            ProcessPanelHelper.printToPanel(mHandler,
                    idata.langpack.getString("postinstall.processpanel.xmlcreation.error") + new File(moduleXmlPath).getAbsolutePath(), true);
            return false;
        } catch (TransformerException e) {
            ProcessPanelHelper.printToPanel(mHandler,
                    idata.langpack.getString("postinstall.processpanel.xmlcreation.error") + new File(moduleXmlPath).getAbsolutePath(), true);
            return false;
        } catch (IOException e) {
            ProcessPanelHelper.printToPanel(mHandler,
                    idata.langpack.getString("postinstall.processpanel.xmlcreation.error") + new File(moduleXmlPath).getAbsolutePath(), true);
            return false;
        }
        return true;
    }

    // Installs a JDBC driver with the gathered information onto the AS
    static List<ModelNode> installJdbcDriver() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        int counter = 1; // counter starts at one due to DynamicComponentsPanel design
        List<String> jarFiles = new ArrayList<String>();
        while (true) {
            String path = idata.getVariable("jdbc.driver.jar-" + counter + "-path");
            if (path == null) {
                break;
            }
            jarFiles.add(path);
            counter++;
        }

        idata.setVariable("jdbc.driver.location", new File(jarFiles.get(0)).getParent());

        String jdbcName = idata.getVariable("jdbc.driver.name");
        String jdbcModuleName = idata.getVariable("jdbc.driver.module.name");
        String jdbcXaDsName = idata.getVariable("jdbc.driver.xads.name");
        String jdbcDirStruct = idata.getVariable("jdbc.driver.dir.struct");

        if (!createJdbcFiles(jarFiles, jdbcDirStruct)){
            return null;
        }

        if (!createJDBCModuleXml(jdbcDirStruct, jdbcModuleName, jarFiles)){
            return null;
        }

        String descriptor = ServerManager.getConfigString();
        try {
            String jarPath;
            String driverPath = idata.getVariable("jdbc.driver.path");
            String numPreExistingJars = idata.getVariable("jdbc.driver."+descriptor+".found.count");
            int numOfJars = numPreExistingJars != null ? Integer.parseInt(numPreExistingJars) : 0;

            for (int i = numOfJars; numOfJars > 0; i--) {
                jarPath = idata.getVariable("jdbc.preexisting.driver." + descriptor + "." + i + ".jar");
                if (driverPath.equals(jarPath)) {
                    return new ArrayList<ModelNode>();
                }
            }
        } catch (Exception e) {
        //Integer parsing failed add the module
            e.printStackTrace();
            return null;
        }
        return serverCommands.installJdbcDriver(jdbcName, jdbcModuleName, jdbcXaDsName);
    }

    public static File createDirectory(String basePath, String modulePath) throws Throwable {
        File location = new File(basePath, modulePath);
        if (!location.exists()) {
            location.mkdirs();
        }
        return location;
    }

    private static void copyJarToModulesDir(List<String> filenames, File destination) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        for (String filename : filenames) {
            File sourceFile = new File(filename);
            File fileLoc = new File(destination.getPath() + File.separator + sourceFile.getName());

            if (!fileLoc.exists()) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    if (filename.startsWith("http://") || filename.startsWith("ftp://")) {
                        in = new BufferedInputStream(new URL(filename).openStream());
                    } else {
                        in = new FileInputStream(sourceFile);
                    }

                    out = new FileOutputStream(fileLoc);
                    byte[] buffer = new byte[1024];
                    int l;
                    while ((l = in.read(buffer, 0, 1024)) > 0) {
                        out.write(buffer, 0, l);
                    }
                    ProcessPanelHelper.printToPanel(mHandler,
                            idata.langpack.getString("postinstall.processpanel.jarcopy.success"), false);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static String getRelativePath(File sourceFile, File destFile) {
        int pos = 0;
        String relativePath = "";
        String[] source = sourceFile.getAbsolutePath().split(File.separator);
        String[] dest = destFile.getAbsolutePath().split(File.separator);

        //Eliminate similar paths
        while ((pos < source.length) && (pos < dest.length)
                && (source[pos].equals(dest[pos]))) {
            pos++;
        }

        //Append ../ to get into the source into the last similar directory of dest
        //additional -1 if you are using relative files
        //otherwise dont need the minus 1
        for (int a = 0; a < source.length - pos - 1; a++)
            relativePath += ".." + File.separator;

        //From the common directory start appending to destination
        for (int b = pos; b < dest.length; b++)
            relativePath += dest[b] + File.separator;


        relativePath = relativePath.substring(0, relativePath.length() - 1);

        //If ends with ".." append the destination name
        if (relativePath.endsWith("..")) relativePath += File.separator + dest[dest.length - 1];

        return relativePath;

    }
}


