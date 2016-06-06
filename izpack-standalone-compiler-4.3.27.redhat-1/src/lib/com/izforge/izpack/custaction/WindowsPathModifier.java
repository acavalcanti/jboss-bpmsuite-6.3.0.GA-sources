package com.izforge.izpack.custaction;

import com.izforge.izpack.Pack;
import com.izforge.izpack.event.*;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProgressHandler;
import com.izforge.izpack.util.StringTool;

/* This Listener is used to turn windows paths into 
 * java properties paths. Parsable is not working currently.
 */

public class WindowsPathModifier extends SimpleInstallerListener
{
    public WindowsPathModifier(){
        super(false);
    }
    
    public void beforePacks(AutomatedInstallData idata, Integer i, AbstractUIProgressHandler handler)
    throws Exception {
      
    String ipath = idata.getInstallPath();
    if(!(ipath == null || "".equals(ipath))){
        String javaipath = StringTool.normalizePath(ipath,"/");
        idata.setVariable("javaInstallPath", javaipath);
    }
        
            
    }
        
}

