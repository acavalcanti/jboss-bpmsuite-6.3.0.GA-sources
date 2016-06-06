package com.redhat.installer.installation.processpanel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;

//import org.apache.tools.ant.DefaultLogger;

/**
 * Class for running the SQL commands
 * 
 * @author thauser
 * 
 */
public class SQLRunner {

	private static String BUILD_FILE = "build-file";
	private static String WORKING_DIR = "working-dir";
	private static String LOG_FILE = "log-file";
	private static String ERROR_FILE = "error-file";

	public static boolean run(AbstractUIProcessHandler handler, String[] args) throws FileNotFoundException {
		AutomatedInstallData idata = AutomatedInstallData.getInstance();
		ArgumentParser parser = new ArgumentParser();
		parser.parse(args);
		PrintStream logFile = null;
		if (parser.hasProperty(LOG_FILE)){
			logFile = new PrintStream(new File(parser.getStringProperty(LOG_FILE)));
		}
		
		try {
			File buildXml = new File(parser.getStringProperty(BUILD_FILE));
			String baseInstall = buildXml.getParentFile().getParent();
			Project proj = new Project();

			if (parser.hasProperty(WORKING_DIR)) {
				proj.setBasedir(parser.getStringProperty(WORKING_DIR));
			} else {
				proj.setBaseDir(buildXml.getParentFile());
			}


			proj.setUserProperty("ant.file", buildXml.getAbsolutePath());
			String driver = idata.getVariable("db.driver");
            String jdbcDriverLocation = idata.getVariable("jdbc.driver.location");
            String h2DriverLocation = baseInstall + File.separator + JBossJDBCConstants.driverJarPath.get(driver);

            if (jdbcDriverLocation == null || jdbcDriverLocation.isEmpty() )
                jdbcDriverLocation = h2DriverLocation;
			proj.setProperty("driver", JBossJDBCConstants.classnameMap.get(driver));

            proj.setProperty("driverjarloc", jdbcDriverLocation);

			Properties dataDir = new Properties();
			dataDir.setProperty("jboss.server.data.dir", idata.getVariable("jboss.server.data.dir"));
			VariableSubstitutor vs = new VariableSubstitutor(dataDir); // we make a variable substitutor with only this single var, so that we don't mistakenly put a value from idata in.
			String url = idata.getVariable("db.url");
			url = vs.substitute(url);
			proj.setProperty("url", url);
			proj.setProperty("username", idata.getVariable("db.user"));
			proj.setProperty("password", idata.getVariable("db.password"));
			proj.setProperty("dialect", idata.getVariable("db.dialect"));

			DefaultLogger consoleLogger = new DefaultLogger();

			if (parser.hasProperty(LOG_FILE)) {
				consoleLogger.setOutputPrintStream(logFile);
				consoleLogger.setErrorPrintStream(logFile);
			} else {
				consoleLogger.setOutputPrintStream(System.out);
				consoleLogger.setErrorPrintStream(System.err);
			}

			consoleLogger.setMessageOutputLevel(Project.MSG_VERBOSE);
			proj.addBuildListener(consoleLogger);
			proj.fireBuildStarted();
			proj.init();

			ProjectHelper helper = ProjectHelper.getProjectHelper();
			proj.addReference("ant.projectHelper", helper);
			helper.parse(proj, buildXml);
			proj.executeTarget(proj.getDefaultTarget());
			proj.fireBuildFinished(null);
			handler.logOutput("Database schemas created successfully. See " + parser.getStringProperty(LOG_FILE), false);
			return true;
		} catch (Exception e) {
			// TODO: this string is insufficient
			handler.logOutput("Database schema creation failed. See " + parser.getStringProperty(LOG_FILE), true);
			e.printStackTrace(logFile);
			return false;
		}
	}
}
