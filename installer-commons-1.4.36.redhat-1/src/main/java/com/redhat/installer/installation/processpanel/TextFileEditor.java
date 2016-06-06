package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.Debug;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TextFileEditor {

    private static final String FILE = "file";
    private static final String CONFIG = "config";

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();

        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);
        List<String> filePaths = parser.getListProperty(FILE);
        List<String> configs = parser.getListProperty(CONFIG);
        List<EditorConfiguration> textConfigs = new ArrayList<EditorConfiguration>();

        for (String config : configs) {
            textConfigs.add(new EditorConfiguration(config));
        }

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
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("textFileEditor.currently.editing.file"), file.getPath()), false);
                while ((line = reader.readLine()) != null) {
                    boolean removeLine = false;
                    ArrayList<String> followingLines = new ArrayList<String>();
                    for (EditorConfiguration config : textConfigs) {
                        Matcher m = config.regex.matcher(line);
                        if (m.find()) {
                            switch (config.editmode) {
                                case replace:
                                    line = m.replaceAll(config.text);
                                    break;
                                case remove:
                                    removeLine = true;
                                    break;
                                case append:
                                    line = line + config.text;
                                    break;
                                case prepend:
                                    line = config.text + line;
                                    break;
                                case after_line:
                                    followingLines.add(config.text);
                                    break;
                                case before_line:
                                    lines.add(config.text);
                                    break;
                            }
                            linesChanged++;
                        }
                    }
                    if (!removeLine)
                        lines.add(line);
                    if (!followingLines.isEmpty())
                        lines.addAll(followingLines);
                }

                FileWriter fw = new FileWriter(file);
                writer = new BufferedWriter(fw);

                for (String s : lines) {
                    writer.write(s);
                    writer.newLine();
                }

                writer.flush();
            } catch (Exception e) {
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("TextFileEditor.error"), file.getAbsolutePath()), true);
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

    private static class EditorConfiguration {

        private enum Editmode {
            replace(true),
            remove(false),
            append(true),
            prepend(true),
            after_line(true),
            before_line(true);

            private boolean needsText;

            private Editmode(boolean needsText) {
                this.needsText = needsText;
            }

            boolean isNeedsText() {
                return needsText;
            }

        }

        private Editmode editmode;
        private Pattern regex;
        private String text;

        private EditorConfiguration(String configString) {
            String[] keyPairs = configString.split(";");
            for (String pair : keyPairs) {
                String[] keypair = pair.split("#=#");
                if (keypair[0].equals("regex")) {
                    this.regex = Pattern.compile(keypair[1]);
                } else if (keypair[0].equals("editmode")) {
                    this.editmode = Editmode.valueOf(keypair[1]);
                } else if (keypair[0].equals("text")) {
                    if (keypair.length == 1)
                        this.text = "";
                    else
                        this.text = keypair[1];
                }
            }
            if (editmode == null || regex == null || (editmode.isNeedsText() && text == null)) {
                Debug.log("ProcessPanel.spec.xml has incorrect parameters");
                throw new IllegalArgumentException(String.format("Invalid configuration string for TextFileEditor job: %s", configString));
            }
        }

    }

}


