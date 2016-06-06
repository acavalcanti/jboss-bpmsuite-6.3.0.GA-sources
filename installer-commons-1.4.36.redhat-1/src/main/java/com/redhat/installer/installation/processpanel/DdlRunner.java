package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import java.io.File;

/**
 * Created by Tom on 11/9/2015.
 */
public class DdlRunner {

    private static AutomatedInstallData idata;
    private static String ddlPath;
    private static String driverClass;
    private static final String START_MESSAGE_ID = "sql.script.start";
    private static final String INIT_MESSAGE_ID = "sql.script.init";
    private static final String FAIL_MESSAGE_ID = "sql.script.fail";
    private static final String SUCCESS_MESSAGE_ID = "sql.script.success";
    private static String startMessage;
    private static String initMessage;
    private static String failMessage;
    private static String successMessage;

    public static boolean run(AbstractUIProcessHandler handler, String[] args) {
        idata = AutomatedInstallData.getInstance();
        ddlPath = args[0];

        initializeLogMessages();

        return executeScripts(handler);
    }

    private static void initializeLogMessages() {
        startMessage = idata.langpack.getString(START_MESSAGE_ID);
        startMessage = String.format(startMessage, ddlPath);
        initMessage = idata.langpack.getString(INIT_MESSAGE_ID);
        failMessage = idata.langpack.getString(FAIL_MESSAGE_ID);
        successMessage = idata.langpack.getString(SUCCESS_MESSAGE_ID);
    }

    private static boolean executeScripts(AbstractUIProcessHandler handler) {
        handler.logOutput(initMessage, false);
        SQLExec task = setUpTask(handler);
        try {
            handler.logOutput(startMessage, false);
            task.execute();
        } catch (BuildException be) {
            if (be.getMessage().contains("already exists")) {
                ProcessPanelHelper.printToPanel(handler, be.getMessage(), false);
            } else {
                ProcessPanelHelper.printToPanel(handler, failMessage, true);
                ProcessPanelHelper.printToPanel(handler, be.getMessage(), true);
                return false;
            }
        }
        handler.logOutput(successMessage, false);
        return true;
    }

    private static SQLExec setUpTask(final AbstractUIProcessHandler handler) {
        SQLExec task = new SQLExec();
        Project proj = new Project();
        proj.init();
        setVendorSpecificVariables();
        Path classPath = createClassPathRef(proj);
        SQLExec.OnError error = new SQLExec.OnError();
        error.setValue("continue");
        task.setOnerror(error);
        task.setProject(proj);
        task.setTaskType("sql");
        task.setTaskName("quartz-ddl");
        task.setSrc(new File(ddlPath));
        task.setClasspath(classPath);
        task.setUserid(idata.getVariable("quartz.db.username"));
        task.setPassword(idata.getVariable("quartz.db.password"));
        task.setDriver(driverClass);
        task.setUrl(idata.getVariable("quartz.db.url"));
        proj.addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(BuildEvent event) {

            }

            @Override
            public void buildFinished(BuildEvent event) {

            }

            @Override
            public void targetStarted(BuildEvent event) {

            }

            @Override
            public void targetFinished(BuildEvent event) {

            }

            @Override
            public void taskStarted(BuildEvent event) {
            }

            @Override
            public void taskFinished(BuildEvent event) {
            }

            @Override
            public void messageLogged(BuildEvent event) {
                if (event.getTask() != null && event.getTask().getTaskType().equals("sql"))
                    ProcessPanelHelper.printToPanel(handler, event.getMessage(), false);
            }
        });


        return task;
    }

    private static Path createClassPathRef(Project proj) {
        Path classPath = new Path(proj);
        String driverClassPath = idata.getInstallPath() + "/" + idata.getVariable("jdbc.driver.dir.struct");
        FileSet classPathRef = new FileSet();
        classPathRef.setDir(new File(driverClassPath));
        classPathRef.setIncludes("*.jar");
        classPath.addFileset(classPathRef);
        return classPath;
    }

    private static void setVendorSpecificVariables() {
        String chosenDriver = idata.getVariable("db.driver");
        if (chosenDriver.equals(JBossJDBCConstants.ibmJdbcName)) {
            ddlPath = ddlPath + "db2/quartz_tables_db2.sql";
            driverClass = JBossJDBCConstants.ibmDriverClassName;
        } else if (chosenDriver.equals(JBossJDBCConstants.microsoftJdbcName)) {
            ddlPath = ddlPath + "sqlserver/quartz_tables_sqlserver.sql";
            driverClass = JBossJDBCConstants.microsoftDriverClassName;
        } else if (chosenDriver.equals(JBossJDBCConstants.sybaseJdbcName)) {
            ddlPath = ddlPath + "sybase/quartz_tables_sybase.sql";
            driverClass = JBossJDBCConstants.sybaseDriverClassName;
        } else if (chosenDriver.equals(JBossJDBCConstants.oracleJdbcName)) {
            ddlPath = ddlPath + "oracle/quartz_tables_oracle.sql";
            driverClass = JBossJDBCConstants.oracleDriverClassName;
        } else if (chosenDriver.equals(JBossJDBCConstants.postgresqlJdbcName)) {
            ddlPath = ddlPath + "postgresql/quartz_tables_postgres.sql";
            driverClass = JBossJDBCConstants.postgresqlDriverClassName;
        } else if (chosenDriver.equals(JBossJDBCConstants.mysqlJdbcName)) {
            ddlPath = ddlPath + "mysql5/quartz_tables_mysql.sql";
            driverClass = JBossJDBCConstants.mysqlDriverClassName;
        }
    }

}
