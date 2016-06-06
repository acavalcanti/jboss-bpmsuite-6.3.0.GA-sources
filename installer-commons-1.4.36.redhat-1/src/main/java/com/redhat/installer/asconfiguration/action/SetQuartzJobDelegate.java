package com.redhat.installer.asconfiguration.action;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.PanelAction;
import com.izforge.izpack.installer.PanelActionConfiguration;
import com.izforge.izpack.util.AbstractUIHandler;
import com.redhat.installer.asconfiguration.jdbc.constant.JBossJDBCConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by thauser on 7/28/15.
 */
public class SetQuartzJobDelegate implements PanelAction {
    private static final Map<String,String> delegateMap;

    static {
        delegateMap = new HashMap<String,String>();
        delegateMap.put(JBossJDBCConstants.ibmJdbcName, "DB2v6Delegate");
        delegateMap.put(JBossJDBCConstants.mysqlJdbcName, "StdJDBCDelegate");
        delegateMap.put(JBossJDBCConstants.h2JdbcName, "StdJDBCDelegate");
        delegateMap.put(JBossJDBCConstants.postgresqlJdbcName, "PostgreSQLDelegate");
        delegateMap.put(JBossJDBCConstants.microsoftJdbcName, "MSSQLDelegate");
        delegateMap.put(JBossJDBCConstants.sybaseJdbcName, "SybaseDelegate");
        delegateMap.put(JBossJDBCConstants.oracleJdbcName, "oracle.OracleDelegate");
    }
    @Override
    public void executeAction(AutomatedInstallData idata, AbstractUIHandler abstractUIHandler) {
        String driverName = idata.getVariable("jdbc.driver.name");
        if (driverName != null){
            idata.setVariable("quartz.jobstore.delegate", delegateMap.get(driverName));
        }
    }

    @Override
    public void initialize(PanelActionConfiguration panelActionConfiguration) {

    }
}
