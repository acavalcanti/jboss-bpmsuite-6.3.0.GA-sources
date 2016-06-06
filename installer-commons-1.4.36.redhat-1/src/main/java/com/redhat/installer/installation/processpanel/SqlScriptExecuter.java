package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.util.AbstractUIProcessHandler;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thauser on 7/28/15.
 */
public class SqlScriptExecuter {
    private static final String SCRIPT_PATHS = "script-path";
    private static final String DRIVER = "driver";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String DB_URL = "db-url";

    public boolean run(AbstractUIProcessHandler handler, String[] args) {
        ArgumentParser parser = new ArgumentParser();
        parser.parse(args);
        SqlExecuter executer = new SqlExecuter();
        List<File> scripts = getScriptList(parser);

        for (File script : scripts) {
            executer.setSrc(script);
            executer.setDriver(parser.getStringProperty(DRIVER));
            executer.setUserid(parser.getStringProperty(USERNAME));
            executer.setPassword(parser.getStringProperty(PASSWORD));
            executer.setUrl(parser.getStringProperty(DB_URL));
            try {
                executer.execute();
            } catch (BuildException be) {
                ProcessPanelHelper.printToPanel(handler, "Error executing sql script: " + script, true);
                return false;
            }
        }
        return true;
    }

    public List<File> getScriptList(ArgumentParser parser) {
        List<String> paths = parser.getListProperty(SCRIPT_PATHS);
        List<File> scripts = new ArrayList<File>();
        if (paths.size() == 1){
            File check = new File(paths.get(0));
            if (check.isDirectory()){
                for (File file : check.listFiles()){
                    scripts.add(file);
                }
            } else {
                scripts.add(check);
            }
        } else {
            for (String path : paths){
                scripts.add(new File(path));
            }
        }
        return scripts;
    }

    final class SqlExecuter extends SQLExec{
        public SqlExecuter(){
            Project project = new Project();
            project.init();
            setProject(project);
            setTaskType("sql");
            setTaskName("sql");
        }
    }
}
