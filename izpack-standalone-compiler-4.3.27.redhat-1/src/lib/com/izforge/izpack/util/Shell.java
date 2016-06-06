package com.izforge.izpack.util;

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.ConsoleOutput;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.edit.EmacsEditMode;
import org.jboss.aesh.edit.KeyOperation;
import org.jboss.aesh.edit.KeyOperationManager;
import org.jboss.aesh.edit.Mode;
import org.jboss.aesh.edit.actions.Action;
import org.jboss.aesh.edit.actions.Operation;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shell.java replacement using AEsh
 * TODO: entire class has questionable System.exit(0) calls.
 * Created by thauser on 8/20/14.
 */
public class Shell {

    private Console console;
    private Settings settings;
    private static Shell SHELL;
    private boolean directoryField;
    private boolean disableFileCompletion;

    private Shell(){
        settings = Settings.getInstance();
        settings.resetToDefaults();
        settings.setHistoryDisabled(true);
        settings.setAliasEnabled(false);
        settings.setEnablePipelineAndRedirectionParser(false);
        settings.setEditMode(Mode.EMACS);
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")){
            String directConsole = System.getProperty("installer.direct.console");
            if (directConsole != null && directConsole.equals("true")) {
                settings.setAnsiConsole(false);
            }
        }

        setDisableFileCompletion(true);
        setDirectoryField(false);

        try {
            console = new Console(settings);
            console.addCompletion(fileCompleter);

            Field editMode = console.getClass().getDeclaredField("editMode");
            editMode.setAccessible(true);
            editMode.set(console, new IzpackEditMode(settings.getOperationManager()));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Shell getInstance() {
        if (SHELL == null) {
            SHELL = new Shell();
        }
        return SHELL;
    }

    public String getInput(){
        return getInput(false);
    }
    public String getInput(boolean raw){
        String line = null;
        setDisableFileCompletion(true);
        try {
            ConsoleOutput input = console.read("");
            line = input.getBuffer();
            if (raw) return line;
            return line.trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(raw) return line;
        return line.trim();
    }


    /**
     * Get a folder location from the user.
     * Supports autocomplete features.
     * We trim off any trailing whitepsace.
     * The check for line.length() > 1, is an exception to leave the slash for the root directory.
     */
    public String getLocation(boolean directoryField){
        String line = null;
        setDirectoryField(directoryField);
        setDisableFileCompletion(false);

        try {
            ConsoleOutput user = console.read("");
            line = user.getBuffer();
            line = line.replace("~",System.getProperty("user.home"));
            return line.trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line.trim();
    }

    /**
     * Get a password from the user.
     * Currently the ConsoleReader does not support hiding the password being entered.
     * For some reason using a buffered reader will not show output (probably side effect of jline)
     */
    public String getPassword() {
        String line = null;
        System.out.flush();
        setDisableFileCompletion(true);

        try
        {
            ConsoleOutput user = console.read(new Prompt(""),'*');
            line = user.getBuffer();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return line;
    }

    /**
     * Get a single character from the user
     * @return A character that the user has entered.
     */
    public char getChar() throws IOException {
        String line = null;
        setDisableFileCompletion(true);
        try{
            ConsoleOutput user = console.read("");
            char a = user.getBuffer().toLowerCase().charAt(0);
            return a;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line.toLowerCase().charAt(0);
    }

    public void stopShell(){
        try {
            console.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDirectoryField(boolean isDirectory){
        this.directoryField = isDirectory;
    }

    private void setDisableFileCompletion(boolean disableFileCompletion){
        this.disableFileCompletion = disableFileCompletion;
    }

    private boolean getDirectoryField(){
        return directoryField;
    }

    private boolean getDisableFileCompletion(){
        return disableFileCompletion;
    }

    Completion fileCompleter = new Completion() {
        @Override
        public void complete(CompleteOperation co) {

            if (getDisableFileCompletion())
                return;

            String originalEntry = co.getBuffer().trim();
            String subbedEntry = originalEntry;
            if (subbedEntry.startsWith("~")) {
                subbedEntry = subbedEntry.replaceFirst("~", System.getProperty("user.home"));
            }
            String rest = getLastPathSection(subbedEntry);
            File currFile = new File(subbedEntry);
            
            if (!subbedEntry.isEmpty() && (currFile.isDirectory() || currFile.getParent() != null)) {

                File[] filesInDir = null;
                PrefixFileFilter fileFilter = new PrefixFileFilter(rest, getDirectoryField());

                // handle case of root directory, ie "/"
                if (currFile.getParentFile() == null && currFile.isDirectory()){

                    if (System.getProperty("os.name").startsWith("Windows")){
                        Pattern root = Pattern.compile("^[A-Z]:$");
                        Matcher rootMatcher = root.matcher(currFile.getPath());
                        if (rootMatcher.matches()){
                            co.addCompletionCandidate("\\");
                        } else {
                            filesInDir = currFile.listFiles(fileFilter);
                        }
                    } else {
                        filesInDir = currFile.listFiles(fileFilter);
                    }
                }
                // handle case of paths that have a parent dir but aren't a dir or file them selves, "/home/username/not_fini"
                else if (!currFile.exists() && currFile.getParentFile() != null){
                    if (System.getProperty("os.name").startsWith("Windows") && !currFile.getPath().contains(System.getProperty("file.separator"))){
                        filesInDir = null;
                    }
                    else {
                        filesInDir = currFile.getParentFile().listFiles(fileFilter);
                    }
                }
                // if it is either a file or directory and not the root dir
                else if (currFile.exists()){
                    // This means that its a file or dir and the only one that is possible at this point, so look for things in this or file now
                    if (rest.length() > 0){
                        filesInDir = currFile.getParentFile().listFiles(fileFilter);
                    }
                    // Means that it ended in slash meaning a new dir
                    else {
                        filesInDir = currFile.listFiles(fileFilter);
                    }
                }

                if (filesInDir != null){
                    for (File f : filesInDir) {
                        String candidate = f.getAbsolutePath();
                        candidate = getLastPathSection(candidate);
                        if (f.isDirectory()){
                            candidate = candidate + System.getProperty("file.separator");
                        }
                        co.addCompletionCandidate(candidate);
                    }
                }
            }

            co.setOffset(originalEntry.length() - rest.length());
            co.doAppendSeparator(false);
        }
    };

    private String getLastPathSection(String path){
        if (path.endsWith(System.getProperty("file.separator"))){
            return "";
        }
        String fileSeparator = System.getProperty("file.separator");
        // Because \\ is not a valid regex
        if (fileSeparator.equals("\\")){
            fileSeparator = fileSeparator.concat(fileSeparator);
        }
        String [] pathSegments = path.split(fileSeparator);
        String rest = "";
        if (pathSegments.length > 1) {
            rest = pathSegments[pathSegments.length - 1];
        }
        return rest;
    }

    public class IzpackEditMode extends EmacsEditMode {
        private Action mode = Action.EDIT;

        private KeyOperationManager operationManager;
        private List<KeyOperation> currentOperations = new ArrayList<KeyOperation>();
        private int operationLevel = 0;

        public IzpackEditMode(KeyOperationManager operations) {
            super(operations);
            this.operationManager = operations;
        }

        @Override
        public Operation parseInput(int[] in, String buffer) {
            {

                int input = in[0];
                if(in.length > 1) {
                    KeyOperation ko = operationManager.findOperation(in);
                    if(ko != null) {
                        //clear current operations to make sure that everything works as expected
                        currentOperations.clear();
                        currentOperations.add(ko);
                    }
                }
                else {
                    //if we're in the middle of parsing a sequence input
                    //currentOperations.add(KeyOperationFactory.findOperation(operations, input));
                    if(operationLevel > 0) {
                        Iterator<KeyOperation> operationIterator = currentOperations.iterator();
                        while(operationIterator.hasNext())
                            if(input != operationIterator.next().getKeyValues()[operationLevel])
                                operationIterator.remove();

                    }
                    // parse a first sequence input
                    else {
                        for(KeyOperation ko : operationManager.getOperations())
                            if(input == ko.getFirstValue())
                                currentOperations.add(ko);
                    }
                }

                for (KeyOperation op : currentOperations){
                    if (op.getOperation().equals(Operation.EMACS_EDIT_MODE) || op.getOperation().equals(Operation.VI_EDIT_MODE)){
                        operationLevel = 0;
                        currentOperations.clear();
                        return Operation.NO_ACTION;
                    }
                }

                //search mode need special handling
                if(mode == Action.SEARCH) {
                    if(currentOperations.size() == 1) {
                        if(currentOperations.get(0).getOperation() == Operation.NEW_LINE) {
                            mode = Action.EDIT;
                            currentOperations.clear();
                            return Operation.SEARCH_END;
                        }
                        else if(currentOperations.get(0).getOperation() == Operation.SEARCH_PREV) {
                            currentOperations.clear();
                            return Operation.SEARCH_PREV_WORD;
                        }
                        else if(currentOperations.get(0).getOperation() == Operation.SEARCH_NEXT_WORD) {
                            currentOperations.clear();
                            return Operation.SEARCH_NEXT_WORD;
                        }
                        else if(currentOperations.get(0).getOperation() == Operation.DELETE_PREV_CHAR) {
                            currentOperations.clear();
                            return Operation.SEARCH_DELETE;
                        }
                        // TODO: unhandled operation, should parse better
                        else {
                            currentOperations.clear();
                            return Operation.NO_ACTION;
                        }
                    }
                    //if we got more than one we know that it started with esc
                    else if(currentOperations.size() > 1) {
                        mode = Action.EDIT;
                        currentOperations.clear();
                        return Operation.SEARCH_EXIT;
                    }
                    // search input
                    else {
                        currentOperations.clear();
                        return Operation.SEARCH_INPUT;
                    }
                } // end search mode
                else {
                    // process if we have any hits...
                    if(currentOperations.isEmpty()) {
                        //if we've pressed meta-X, where X is not caught we just disable the output
                        if(operationLevel > 0) {
                            operationLevel = 0;
                            currentOperations.clear();
                            return Operation.NO_ACTION;
                        }
                        else
                            return Operation.EDIT;
                    }
                    else if(currentOperations.size() == 1) {
                        //need to check if this one operation have more keys
                        int level = operationLevel+1;
                        if(in.length > level)
                            level = in.length;
                        if(currentOperations.get(0).getKeyValues().length > level) {
                            operationLevel++;
                            return Operation.NO_ACTION;
                        }
                        Operation currentOperation = currentOperations.get(0).getOperation();
                        if(currentOperation == Operation.SEARCH_PREV ||
                                currentOperation == Operation.SEARCH_NEXT_WORD)
                            mode = Action.SEARCH;

                        operationLevel = 0;
                        currentOperations.clear();

                        return currentOperation;
                    }
                    else {
                        operationLevel++;
                        return Operation.NO_ACTION;
                    }
                }
            }
        }

    }
}
