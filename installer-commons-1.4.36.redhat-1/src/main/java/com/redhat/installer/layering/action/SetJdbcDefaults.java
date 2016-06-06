package com.redhat.installer.layering.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import com.redhat.installer.asconfiguration.jdbc.validator.JDBCConnectionUtils;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.apache.commons.io.FileUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class SetJdbcDefaults extends PreExistingSetter
{
    private HashSet<File> moduleSubdirs = new HashSet<File>();

    /**
     * This method finds all of the declared JDBC drivers per file, and matches them against the driver
     * classes that the installer can use to execute the SQL / configure components correctly with. It saves
     * drivers it finds in idata under the scheme: jdbc.preexisting.driver."xml"."number".{name,class,jar}
     * Additionally, the jdbc.driver.preexisting variable is set to true, for use in conditions and panels.
     * @param xml
     * @param doc
     */
    @Override
    protected void setDefaults(String xml, Document doc)
    {
        // get all of the defined drivers that aren't named h2, because that's there by default
        Elements drivers = doc.select("drivers > driver[name!=h2]");
        int foundCount = 0;
        for (Element driver : drivers)
        {
            String driverName = driver.attr("name");
            String driverModule = driver.attr("module");
            String driverClass = null;
            String driverJarPath = null;

            URL[] moduleJarUrls = JDBCConnectionUtils.convertToUrlArray(parseJarLocationFromModule(driverModule));
            for (URL jar : moduleJarUrls)
            {
                driverClass = findExistingDriverClass(jar);
                try
                {
                    driverJarPath = (driverClass != null) ? new File(jar.toURI()).getPath() : null;
                }
                catch (URISyntaxException e)
                {
                    // this can't occur, since the convertToUrlArray would have caught it.
                    e.printStackTrace();
                }

            }

            // we have found a usable driver jar!
            if (driverClass != null && driverJarPath != null)
            {
                foundCount++;
                String driverNameInternal = JBossJDBCConstants.classnameToJDBCMap.get(driverClass); //Ensure
                idata.setVariable("jdbc.driver.preexisting", "true");
                idata.setVariable("jdbc.preexisting.driver."+xml+"."+foundCount+".name", driverName);
                idata.setVariable("jdbc.preexisting.driver."+xml+"."+foundCount+".name.internal", driverNameInternal);
                idata.setVariable("jdbc.preexisting.driver." + xml + "." + foundCount + ".classname", driverClass);
                idata.setVariable("jdbc.preexisting.driver." + xml + "." + foundCount + ".jar", driverJarPath);
                idata.setVariable("jdbc.driver." + xml + ".found.count", String.valueOf(foundCount));

            }
        }
    }

    /**
     * This method runs through all of the jars in the parameter, and
     * attempts to load every supported driver class the installer knows about.
     * If successful, the name of the FIRST driver class successfully loaded is returned.
     * @param preExistingJDBCJars
     * @return
     */
    private String findExistingDriverClass(URL... preExistingJDBCJars)
    {
        for (String driverClassname : JBossJDBCConstants.classnameList)
        {
            Class<?> driver = JDBCConnectionUtils.findDriverClass(driverClassname, preExistingJDBCJars);
            if (driver != null)
            {
                // TODO: see if it's premature to short circuit the search here.
                return driverClassname;
            }
        }
        return null; // found nothing
    }

    /**
     * Attempts to find a JDBC driver jar referenced by the existing standalone*.xml descriptors and domain.xml
     * under the modules directory. Note that this only works if the driver has been installed as a core module (and we don't
     * really care about it if it hasn't)
     * @return
     */
    private Object[] parseJarLocationFromModule(String module)
    {
        ArrayList<File> fileList = new ArrayList<File>(1);

        for (File moduleSubdir : moduleSubdirs)
        {
            File searchDir = new File(moduleSubdir + "/" + module.replace(".", "/"), "/main/");
            if (searchDir.exists())
            {
                Iterator<File> jars = FileUtils.iterateFiles(searchDir, new String[]{"jar"}, false);
                while (jars.hasNext())
                {
                    fileList.add(jars.next());
                }
            }
        }
        ArrayList<String> resultList = new ArrayList<String>();
        for (File file : fileList)
        {
            resultList.add(file.getPath());
        }
        return resultList.toArray();
    }

    @Override
    protected void resetDefaults()
    {
        idata.setVariable("jdbc.driver.preexisting", "false");
        // different possible subdirs of where modules can be found
        moduleSubdirs.clear();
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String installPath = idata.getInstallPath();
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.modulesPath));
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.baseModulesPath));
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.soaModulesPath));
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.srampModulesPath));
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.dvModulesPath));
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.brmsModulesPath));
        moduleSubdirs.add(new File(installPath + PreExistingConfigurationConstants.bpmsModulesPath));

    }
}
