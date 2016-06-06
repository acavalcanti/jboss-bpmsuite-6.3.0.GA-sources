package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Moves a list of files into a new location
 * Default behavior moves (deletes original), copy flag does a copy.
 * <p/>
 * Created by thauser on 7/15/14.
 */
public class FileMover {

    private static final String COPY = "copy";
    private static final String MSG_ENABLED = "enable-messages";
    private static final String SOURCE = "source";
    private static final String DEST = "destination";
    private static final String DEST_IS_FILE = "dest-is-file";
    private static Operation operation;

    private enum Operation {
        COPY_FILE, COPY_FILE_TO_DIRECTORY, MOVE_FILE, MOVE_FILE_TO_DIRECTORY,
        COPY_DIR, COPY_DIR_TO_DIRECTORY, MOVE_DIR, MOVE_DIR_TO_DIRECTORY
    }

    public static void run(AbstractUIProcessHandler handler, String[] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);

        decideOperation(parser);

        boolean displayMessages = parser.hasProperty(MSG_ENABLED) && Boolean.parseBoolean(parser.getStringProperty(COPY));
        List<String> sources = parser.getListProperty(SOURCE);

        if ((operation == Operation.COPY_FILE || operation == Operation.MOVE_FILE) && sources.size() > 1){
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("FileMover.missing.args"), true);
            return;
        }

        if (parser.hasProperty(SOURCE) && parser.hasProperty(DEST)) {
            File dest = new File(parser.getStringProperty(DEST));

            // if the destination isn't a directory and it already exists, we're going to mangle something
            if (!dest.isDirectory() && dest.exists()) {
                ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("FileMover.dest.exists"), true);
                return;
            }
            for (String s : sources) {
                File source = new File(s);
                String sourcePath = source.getAbsolutePath();

                // if the source doesn't exist, we're done
                if (!source.exists()) {
                    ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("FileMover.source.notexist"), sourcePath), false);
                    continue;
                }
                try {
                    performOperation(source,dest);
                } catch (IOException e) {
                    ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("FileMover.copy.failed"), sourcePath, dest.getAbsolutePath()), true);
                    continue;
                }
                if (displayMessages) {
                    ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("FileMover.copy.success"), sourcePath, dest.getAbsolutePath()), false);
                }
            }
        } else {
            ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("FileMover.missing.args"), true);
            return;
        }
    }

    private static void decideOperation(ArgumentParser parser) {
        boolean destIsFile = parser.hasProperty(DEST_IS_FILE);
        boolean copyFile = parser.hasProperty(COPY);
        File sourceFile = new File(parser.getStringProperty(SOURCE));
        boolean sourceIsDirectory = sourceFile.isDirectory();
        if (copyFile){
            if (sourceIsDirectory){
                if (destIsFile) {
                    operation = Operation.COPY_DIR;
                } else {
                    operation = Operation.COPY_DIR_TO_DIRECTORY;
                }
            } else {
                if (destIsFile) {
                    operation = Operation.COPY_FILE;
                } else {
                    operation = Operation.COPY_FILE_TO_DIRECTORY;
                }
            }
        } else {
            if (sourceIsDirectory){
                if (destIsFile) {
                    operation = Operation.MOVE_DIR;
                } else {
                    operation = Operation.MOVE_DIR_TO_DIRECTORY;
                }
            } else {
                if (destIsFile) {
                    operation = Operation.MOVE_FILE;
                } else {
                    operation = Operation.MOVE_FILE_TO_DIRECTORY;
                }
            }
        }
    }

    private static void performOperation(File source, File dest) throws IOException {
        switch (operation) {
            case COPY_FILE:
                FileUtils.copyFile(source, dest);
                break;
            case COPY_FILE_TO_DIRECTORY:
                FileUtils.copyFileToDirectory(source, dest, true);
                break;
            case COPY_DIR:
                FileUtils.copyDirectory(source,dest);
                break;
            case COPY_DIR_TO_DIRECTORY:
                FileUtils.copyDirectoryToDirectory(source,dest);
                break;
            case MOVE_FILE:
                FileUtils.moveFile(source, dest);
                break;
            case MOVE_FILE_TO_DIRECTORY:
                FileUtils.moveFileToDirectory(source, dest, true);
                break;
            case MOVE_DIR:
                FileUtils.moveDirectory(source,dest);
                break;
            case MOVE_DIR_TO_DIRECTORY:
                FileUtils.moveDirectoryToDirectory(source,dest,true);
                break;
        }
    }

}
