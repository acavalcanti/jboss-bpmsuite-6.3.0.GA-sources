package com.redhat.installer.installation.processpanel;

import java.io.File;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.izforge.izpack.util.VariableSubstitutor;

/**
 * Intended usage of this class is to solve
 * https://bugzilla.redhat.com/show_bug.cgi?id=1010968 in a general way. It
 * simply deletes the file at the given path. Used for when the server or some
 * post-install job modifies the install directory, and we must revert or delete
 * some files as a result of this.
 * 
 * @author thauser
 * 
 */

public class FileDeleter {
	public static final String MSG_ENABLED = "enable-messages";

	public static void run(AbstractUIProcessHandler handler, String[] args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
		ArgumentParser parser = new ArgumentParser();
		parser.parse(args);

		boolean displayMessages = parser.hasProperty(MSG_ENABLED);

		for (String file : args) {
			if (file != null) {
				File toDelete = new File(vs.substitute(file));

                if (!toDelete.exists()) {
                    continue;
                }

				if (!toDelete.delete()) {
					if (displayMessages)
						//ProcessPanelHelper.printToPanel(handler, "Failed to delete file at: " + file, true);
                        ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("FileDeleter.failure") + file, true);
				} else {
					if (displayMessages)
                        //ProcessPanelHelper.printToPanel(handler, "Deleted file at: " + file, false);
                        ProcessPanelHelper.printToPanel(handler, idata.langpack.getString("FileDeleter.success") + " " + file, false);
				}

			}
		}

	}
}
