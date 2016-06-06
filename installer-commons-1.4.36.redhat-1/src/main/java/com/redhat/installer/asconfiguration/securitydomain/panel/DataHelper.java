package com.redhat.installer.asconfiguration.securitydomain.panel;

import java.lang.reflect.Field;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;

/** SecurityDomainUtilities 
 * Provides helpful utilities to keep code DRY.
 * Also helps to avoid buggy code by having not to remember to add prefixes and suffixes. 
 * 
 * @author mtjandra
 */
public class DataHelper {
    AutomatedInstallData idata;
    Class consts;
    String BASE, LABEL, TIP, VARIABLE, ERROR;

    
    public DataHelper(AutomatedInstallData idata, Class consts) {
        this.idata = idata;
        this.consts = consts;
        try{
            BASE  = (String) consts.getField("BASE").get(null);
            LABEL = (String) consts.getField("LABEL").get(null);
            TIP   = (String) consts.getField("TIP").get(null);
            ERROR = (String) consts.getField("ERROR").get(null);
            VARIABLE = (String) consts.getField("VARIABLE").get(null);
        } catch (Exception e){ Debug.log("Ill defined constant class"); e.printStackTrace(); }
    }
     
    public String getLabel(String id) {
        return idata.langpack.getString(BASE + id + LABEL);
    }

    public String getToolTip(String id) {
        return idata.langpack.getString(BASE + id + TIP);
    }
    
    public String getVariable(String id) {
        return idata.getVariable(BASE + id + VARIABLE);
    }
    
    public void setVariable(String id, String value) {
        idata.setVariable(BASE + id + VARIABLE, value);
    }

    public String getErrorMsg(String id) {
        return idata.langpack.getString(BASE + id + ERROR);
    }
}
