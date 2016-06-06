package com.redhat.installer.asconfiguration.processpanel.postinstallation;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import com.redhat.installer.layering.util.PlatformUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aabulawi on 15/04/15.
 */
public class JavaOptsConfiguration {

    private static final String FILE = "file";
    private static String shouldCommentRegex;
    private static String addAfterRegex;
    private static String insertedOptsString;
    private static String commentInScript;

    public static boolean run(AbstractUIProcessHandler handler, String[] args){

        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);


        List<String> filePaths = parser.getListProperty(FILE);

        setVaribalesForPlatforms();

        for (String filePath : filePaths) {
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            File file = new File(filePath);
            if (!shouldPatchFile(file.getName()))
                continue;
            BufferedWriter writer = null;
            try {
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("javaopts.file.edited"), file.getPath()), false);
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                    while ((line = reader.readLine()) != null) {
                        if (line.matches(addAfterRegex)){
                            lines.add(line);
                            String javaOpts = getAppropriateJavaOptsVariable(file.getName());
                            if (!javaOpts.contains("-Djava.net.preferIPv4Stack"))
                                javaOpts = javaOpts + " -Djava.net.preferIPv4Stack=true";
                            if (idata.getRules().isConditionTrue("add.bits.to.configs") && file.getName().toLowerCase().startsWith("domain"))
                                javaOpts = javaOpts + " -d64";
                            lines.add(String.format(insertedOptsString, javaOpts));
                        }
                        else if (line.matches(shouldCommentRegex)){
                            line = commentInScript+line;
                            lines.add(line);
                        }
                        else {
                            lines.add(line);
                        }
                    }

                fis.close();
                reader.close();

                FileWriter fw = new FileWriter(file);
                writer = new BufferedWriter(fw);

                for (String s : lines) {
                    writer.write(s);
                    writer.newLine();
                }

                writer.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return true;

    }

    private static String getAppropriateJavaOptsVariable(String configFileName){
        if (configFileName.toLowerCase().startsWith("domain")){
            return AutomatedInstallData.getInstance().getVariable("javaopts.domain");
        }
        else if (configFileName.toLowerCase().startsWith("standalone")){
            return AutomatedInstallData.getInstance().getVariable("javaopts.standalone");
        }
        return "";
    }

    private static boolean shouldPatchFile(String configFileName){
        if (configFileName.toLowerCase().startsWith("domain")){
            return AutomatedInstallData.getInstance().getRules().isConditionTrue("javaopts.configure.domain");
        }
        else if (configFileName.toLowerCase().startsWith("standalone")){
            return AutomatedInstallData.getInstance().getRules().isConditionTrue("javaopts.configure.standalone");
        }
        return false;
    }

    private static void setVaribalesForPlatforms(){
        if (PlatformUtil.isWindows()) {
            shouldCommentRegex = "set\\s+\"JAVA_OPTS=.*\"";
            addAfterRegex = "rem # JVM memory allocation pool parameters - modify as appropriate.*";
            insertedOptsString = "set \"JAVA_OPTS=%s\"";
            commentInScript = "rem ";
        }
        else {
            shouldCommentRegex = "\\s*JAVA_OPTS=\".*\"";
            addAfterRegex = ".*\"x\\$JAVA_OPTS\"\\s*=\\s*\"x\".*";
            insertedOptsString = "    JAVA_OPTS=\"%s\"";
            commentInScript = "# ";
        }
    }
}
