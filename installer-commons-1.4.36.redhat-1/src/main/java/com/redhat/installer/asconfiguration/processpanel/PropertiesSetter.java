package com.redhat.installer.asconfiguration.processpanel;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.installation.processpanel.ProcessPanelHelper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Generalized class to set properties with a given key, in a given properties file to new values. 
 *
 * @author thauser
 *
 */
public class PropertiesSetter {

	private static AbstractUIProcessHandler mHandler;
	
	public static boolean run(AbstractUIProcessHandler handler, String[]args) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
		mHandler = handler;
		File propsFile = new File(args[0]);
		if (!propsFile.exists()){
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("PropertiesSetter.notexist") + " "  + propsFile.getPath(),false);
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("PropertiesSetter.newfile") + " " +  propsFile.getPath(), false);
			try {
				propsFile.createNewFile();
			} catch (IOException e) {
				// we print this here, this points to an error in the izpack descriptors.
				e.printStackTrace();
				return false;
			}
        }

        // it is fine to do this so rigidly here, because these args are specified directly in the ProcessPanel spec, and are thus under stricter
        // control than a properties file would be.
        Properties props = new Properties();
        for (int i = 1; i < args.length; i++){
            String key = args[i].split("=")[0].trim();
            String value = args[i].split("=",2)[1].trim();
            props.put(key, value);
        }

        PropertiesConfiguration propConfig = null;
        try {
            propConfig = new PropertiesConfiguration(propsFile);
            for (Object key : props.keySet()){
                // cast to String here is safe, because key must come from ProcessPanel.spec
                propConfig.setProperty((String) key, props.get(key));
            }
            propConfig.save();
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("PropertiesSetter.success") + " " + propsFile.getPath(), false);
            return true;
        } catch (ConfigurationException e) {
            e.printStackTrace();
            ProcessPanelHelper.printToPanel(mHandler, idata.langpack.getString("PropertiesSetter.failure") + " "  + propsFile.getPath(), true);
            return false;
        }
	}
}
