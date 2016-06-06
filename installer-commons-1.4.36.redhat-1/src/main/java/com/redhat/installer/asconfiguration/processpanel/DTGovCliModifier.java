package com.redhat.installer.asconfiguration.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.processpanel.ArgumentParser;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Incredibly non-general class to modify dtgov-sramp-repo-seed-cli-commands.txt
 * @author thauser
 *
 */

public class DTGovCliModifier {

	private static final String FILE = "file";
    private static final String PASSWORD = "password";
	
	public static void run(AbstractUIProcessHandler handler, String[]args) throws Exception{
		AutomatedInstallData idata = AutomatedInstallData.getInstance();
		ArgumentParser parser = new ArgumentParser();
		parser.parse(args);
		// we use a file arg just because the file name may change
		File dtgovCliFile = new File(parser.getStringProperty(FILE));
		String dtgovPassword = parser.getStringProperty(PASSWORD);

		String line;
		BufferedReader br = new BufferedReader(new FileReader(dtgovCliFile));
		StringBuilder sb = new StringBuilder();
		// non-portable, but fast
		while ((line = br.readLine())!= null){
			if (line.endsWith("s-ramp-server")){
				line = line + " dtgovworkflows " + dtgovPassword;
			} 
/*			line = line.replace("admin","dtgovworkflows");
			line = line.replace("overlord", idata.getVariable("workflows.plaintext.password"));*/
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}
		br.close();
		
		
		String tempFileName = parser.getStringProperty(FILE)+"TEMP";
		File tempDtgovCliFile = new File(tempFileName);
		idata.setVariable("dtgov.temp.cli.file", tempFileName);
		
		if (tempDtgovCliFile.exists()) {
            if (!tempDtgovCliFile.delete()){
                ProcessPanelHelper.printToPanel(handler, String.format(idata.langpack.getString("DTGovCliModifier.tempfile.delete.failed"), tempDtgovCliFile.getAbsolutePath()), true);
            }
        }

		FileWriter writer = new FileWriter(tempDtgovCliFile, false);
		writer.write(sb.toString());
	    ProcessPanelHelper.printToPanel(handler,String.format(idata.langpack.getString("DTGovCliModifier.success"),tempDtgovCliFile.getAbsolutePath()), false);
		writer.close();
    }
	
}
