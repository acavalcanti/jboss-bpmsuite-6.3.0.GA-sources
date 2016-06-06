package com.redhat.installer.installation.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;

import java.io.*;

/**
 * This class is responsible for running arbitrary system commands
 * during the post-install process. The arguments supplied to it are,
 * in order: 
 * 1 - Command to run (including full path to it if not a sys command)
 * 2 - arg1
 * 3 - arg2
 * ...
 * @author fcanas@redhat.com
 *
 */
public class CommandRunner {
	private static final String COMMAND = "command";
	private static final String MUTE = "mute";
	private static AbstractUIProcessHandler mHandler;
	private static ProcessBuilder builder;
	private static BufferedReader in;
	private static BufferedWriter out;
	
	public static boolean run(AbstractUIProcessHandler handler, final String[] args) {
		mHandler = handler;
		AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String workingDirectory = idata.getInstallPath()+File.separator;
		ArgumentParser parser = new ArgumentParser();
		parser.parse(args);
		boolean mute = args[0].equals("--mute=true");
		
		String [] arguments;
		if (mute){
			arguments = new String[args.length-1];	
		} else {
			arguments = new String[args.length];
		}
		
		for (int i = 0; i < arguments.length; i++){
			if (mute){
				arguments[i] = args[i+1];
			} else {
				arguments[i] = args[i];
			}
		}
		
		ProcessBuilder builder = new ProcessBuilder(arguments);
		builder.directory(new File(workingDirectory));
		ProcessPanelHelper.adjustJbossHome(builder);

		try {
			if (!mute){
                ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("CommandRunner.running") + builder.command(), false);
			}
			builder.redirectErrorStream(true);
			Process p = builder.start();
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			out.newLine();
			out.flush();
			Thread eater = new Thread(new Runnable() {
				public void run() {
					try {
						BufferedWriter log = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(AutomatedInstallData.getInstance().getInstallPath()+"/"+AutomatedInstallData.getInstance().getVariable("installation.logfile"),true)));
						String line;
						while((line = in.readLine()) != null) {
							//swallow everything
							ProcessPanelHelper.printToPanel(mHandler,line,false);
						}
						log.close();
					}catch (IOException e) {
                        // TODO: do something here?
					}
				}
				});
			eater.start();
			int exit = p.waitFor();
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("CommandRunner.exitcode")+ " " + exit, false);

			if (exit != 0){
				if (!mute){
                    ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("CommandRunner.failure") +" " + builder.command() + " " + idata.langpack.getString("CommandRunner.failure2") + " " + p.exitValue(), true);


				} else {
					//ProcessPanelHelper.printToPanel(mHandler, "Command failure. Failed with exit code: "+ p.exitValue(), true);

                    ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("CommandRunner.mute.failure") + " " + p.exitValue(), true);
				}
				return false;
			} else {
				if (!mute) {
                    ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("CommandRunner.success") + " " + builder.command() + " " + idata.langpack.getString("CommandRunner.success2"), false);
				} else {
                    ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("CommandRunner.mute.success"), false);
				}
			}			

		}catch (InterruptedException e) {
            ProcessPanelHelper.printExceptionToLog(e.getStackTrace());
            String errorMessage = idata.langpack.getString("CommandRunner.IOfailure");
            ProcessPanelHelper.printToPanel(mHandler, String.format(errorMessage, args[0]), true);
            return false;
		} catch (IOException e) {
			ProcessPanelHelper.printExceptionToLog(e.getStackTrace());
			String errorMessage = idata.langpack.getString("CommandRunner.IOfailure");
			ProcessPanelHelper.printToPanel(mHandler, String.format(errorMessage, args[0]), true);
			return false;
		} finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		return true;
	}
}

