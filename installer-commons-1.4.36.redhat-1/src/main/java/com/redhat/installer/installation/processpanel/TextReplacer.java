package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


public class TextReplacer {
    private static final String FILE = "file";
    private static final String REGEX = "regex";
    private static final String TEXT = "text";
    private static final String REPLACE = "replace";
    private static final String SELF = "\\|\\|SELF\\|\\|";
    private static final String REMOVE = "remove";
    private static final String JAVA_FILE_SEPARATOR = "java-file-separator";

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();

        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);

        if (!parser.hasProperty(TEXT) || (parser.hasProperty(REGEX) && parser.hasProperty(REPLACE))) {
            // required params
            Debug.log("ProcessPanel.spec.xml has incorrect parameters");
            return false;
        }

        String newString = parser.getStringProperty(TEXT);
        if (parser.hasProperty(JAVA_FILE_SEPARATOR)) {
            newString = newString.replaceAll("\\\\", "/");
        }
        List<String> filePaths = parser.getListProperty(FILE);

        outputMessage(handler, parser, idata, filePaths, newString);

        for (String filePath : filePaths) {
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            File file = new File(filePath);
            int linesChanged = 0;
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {

                FileInputStream fis = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
                if (parser.hasProperty(REGEX)) {
                    String regex = parser.getStringProperty(REGEX);
                    while ((line = reader.readLine()) != null) {
                        if (line.matches(regex)) {
                            if (parser.hasProperty(REMOVE)) {
                                String removeString = parser.getStringProperty(REMOVE);
                                lines.add(line.replaceAll(removeString, ""));
                            } else {
                                String escapedLine = line.replaceAll("\\$", "\\\\\\$");

                                newString = newString.replaceAll(SELF, escapedLine);

                                newString = newString.replaceAll("\\$", "\\\\\\$");
                                line = line.replaceAll(regex, newString);
                                lines.add(line);
                            }
                            linesChanged++;
                        } else {
                            lines.add(line);
                        }
                    }
                } else if (parser.hasProperty(REPLACE)) {
                    String replaceString = parser.getStringProperty(REPLACE);

                    while ((line = reader.readLine()) != null) {
                        if (line.contains(replaceString)) {
                            lines.add(line.replace(replaceString, newString));
                            linesChanged++;
                        } else {
                            lines.add(line);
                        }
                    }
                }

                FileWriter fw = new FileWriter(file);
                writer = new BufferedWriter(fw);

                for (String s : lines) {
                    writer.write(s);
                    writer.newLine();
                }
                writer.flush();
            } catch (Exception e) {
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("TextReplacer.error"), file.getAbsolutePath()), true);
                e.printStackTrace();
                return false;
            } finally {
                try {
                    reader.close();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * Output formatted messages to GUI or console.
     *
     * @param handler
     * @param parser
     * @param idata
     * @param filePaths
     * @param newString
     */
    private static void outputMessage(AbstractUIProcessHandler handler, ArgumentParser parser, AutomatedInstallData idata, List<String> filePaths, String newString) {
        if (parser.hasProperty(REGEX)) {
            String regex = parser.getStringProperty(REGEX);

            if (parser.hasProperty(REMOVE)) {
                String removeString = parser.getStringProperty(REMOVE);
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("TextReplacer.removal"), removeString), false);

            } else {
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("TextReplacer.replaceline"), regex, newString), false);

            }
        } else if (parser.hasProperty(REPLACE)) {
            String replaceString = parser.getStringProperty(REPLACE);
            ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("TextReplacer.replacetext"), replaceString, newString), false);
        }
        printFilePaths(handler, filePaths);
    }

    /**
     * Print the filepaths in sequence.
     *
     * @param handler
     * @param filePaths
     */
    private static void printFilePaths(AbstractUIProcessHandler handler, List<String> filePaths) {
        for (String file : filePaths) {
            ProcessPanelHelper.printToPanel(handler, file.toString(), false);
        }
    }
}

