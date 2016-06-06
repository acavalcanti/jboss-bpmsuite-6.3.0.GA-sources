package com.redhat.installer.tests.servercommands;

import com.redhat.installer.asconfiguration.processpanel.postinstallation.Jdbc;
import com.redhat.installer.framework.testers.PostinstallTester;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by aabulawi on 31/07/15.
 */
public class JdbcTest extends PostinstallTester {

    @Test
    public void installJDBCDriver() throws Exception {
        idata.setVariable("jdbc.driver.jar-1-path", "http://www.qa.jboss.com/jdbc-drivers-products/jdbc/EAP/6.4.0/mysql55/jdbc4/mysql-connector-java-5.1.33-bin.jar");
        idata.setVariable("jdbc.driver.name", "mysql");
        idata.setVariable("jdbc.driver.module.name", "com.mysql");
        idata.setVariable("jdbc.driver.xads.name", "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
        idata.setVariable("jdbc.driver.dir.struct", "modules/com/mysql/main");
        idata.setVariable("jdbc.driver.use.preexising", "false");
        idata.setVariable("jdbc.driver.standalone.found.count", "false");
        assertTrue(Jdbc.run(mockAbstractUIProcessHandler, new String[]{}));
        assertTrue(new File(idata.getInstallPath()+"/modules/com/mysql/main/mysql-connector-java-5.1.33-bin.jar").exists());
        assertTrue(TestUtils.getXMLTagsFromConfig(idata.getInstallPath() + TestUtils.STANDALONE_CONFIG_DIR + serverMode.getConfigName(),
                "drivers > driver[name=mysql]").size() == 1);
    }
}
