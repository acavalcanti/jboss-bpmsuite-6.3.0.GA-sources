package com.redhat.installer.asconfiguration.jdbc.constant;

import java.util.ArrayList;
import java.util.HashMap;



/**
 * Constants class for both JBoss* panels and any derivatives
 * @author thauser
 *
 */
public final class JBossJDBCConstants
{
    // IBM
    public static final String ibmVendorName = "IBM DB2";
    public static final String ibmJdbcName = "ibmdb2";
    public static final String ibmModuleName = "com.ibm.db2";
    public static final String ibmXaDsName = "com.ibm.db2.jcc.DB2XADataSource";
    public static final String ibmDriverClassName = "com.ibm.db2.jcc.DB2Driver";
    public static final String ibmDriverClassCheck = ibmDriverClassName.replace('.', '/').concat(".class");
    public static final String ibmDirStruct = "modules/com/ibm/db2/main";
    public static final String ibmDialect = "DB2Dialect";
    public static final String ibmConnUrl = "jdbc:db2://SERVER_NAME:PORT/DATABASE_NAME";

    // Sybase
    public static final String sybaseVendorName = "Sybase jConn";
    public static final String sybaseJdbcName = "sybase";
    public static final String sybaseModuleName = "com.sybase.jconn";
    public static final String sybaseXaDsName = "com.sybase.jdbc4.jdbc.SybXADataSource";
    public static final String sybaseDriverClassName = "com.sybase.jdbc4.jdbc.SybDriver";
    public static final String sybaseDriverClassCheck = sybaseDriverClassName.replace('.', '/').concat(".class");
    public static final String sybaseDirStruct = "modules/com/sybase/jconn/main";
    public static final String sybaseDialect = "sybase";
    public static final String sybaseConnUrl = "jdbc:sybase:Tds:localhost:5000/DATABASE?JCONNECT_VERSION=6";

    // MySQL
    public static final String mysqlVendorName = "MySQL";
    public static final String mysqlJdbcName = "mysql";
    public static final String mysqlModuleName = "com.mysql";
    public static final String mysqlXaDsName = "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
    public static final String mysqlDriverClassName = "com.mysql.jdbc.Driver";
    public static final String mysqlDriverClassCheck = mysqlDriverClassName.replace('.','/').concat(".class");
    public static final String mysqlDirStruct = "modules/com/mysql/main";
    public static final String mysqlDialect = "MySQL5InnoDBDialect";
    public static final String mysqlConnUrl = "jdbc:mysql://SERVER_NAME:PORT/DATABASE_NAME";
    
    // postgresql
    public static final String postgresqlVendorName = "PostgreSQL";
    public static final String postgresqlJdbcName = "postgresql";
    public static final String postgresqlModuleName = "org.postgresql";
    public static final String postgresqlXaDsName = "org.postgresql.xa.PGXADataSource";
    public static final String postgresqlDriverClassName = "org.postgresql.Driver";
    public static final String postgresqlDriverClassCheck = postgresqlDriverClassName.replace('.', '/').concat(".class");
    public static final String postgresqlDirStruct = "modules/org/postgresql/main";
    public static final String postgresqlDialect = "PostgreSQLDialect";
    public static final String postgresqlConnUrl = "jdbc:postgresql://SERVER_NAME:PORT/DATABASE_NAME";

    // microsoft
    public static final String microsoftVendorName = "Microsoft SQL Server";
    public static final String microsoftJdbcName = "sqlserver";
    public static final String microsoftModuleName = "com.microsoft.sqlserver";
    public static final String microsoftXaDsName = "com.microsoft.sqlserver.jdbc.SQLServerXADataSource";
    public static final String microsoftDriverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String microsoftDriverClassCheck = microsoftDriverClassName.replace('.', '/').concat(".class");
    public static final String microsoftDirStruct = "modules/com/microsoft/sqlserver/main";
    public static final String microsoftDialect = "SQLServer2008Dialect";
    public static final String microsoftConnUrl = "jdbc:sqlserver://SERVER_NAME:PORT;DatabaseName=DATABASE_NAME";
    
    // oracle
    public static final String oracleVendorName = "Oracle";
    public static final String oracleJdbcName = "oracle";
    public static final String oracleModuleName = "oracle.jdbc";
    public static final String oracleXaDsName = "oracle.jdbc.xa.client.OracleXADataSource";
    public static final String oracleDriverClassName = "oracle.jdbc.OracleDriver";
    public static final String oracleDriverClassCheck = oracleDriverClassName.replace('.', '/').concat(".class");
    public static final String oracleDirStruct = "modules/oracle/jdbc/main";
    public static final String oracleDialect = "Oracle10gDialect";
    public static final String oracleConnUrl = "jdbc:oracle:thin:@ORACLE_HOST:PORT:ORACLE_SID";
    
    // h2
    public static final String h2VendorName = "H2";
    public static final String h2JdbcName = "h2";
    public static final String h2DriverClassName = "org.h2.Driver";
    public static final String h2DriverClassCheck = JBossJDBCConstants.h2DriverClassName.replace('.','/').concat(".class");
    public static final String h2DirStruct = "modules/system/layers/base/com/h2database/h2/main";
    public static final String h2Dialect = "H2Dialect";
    // can't have a nice default for h2 connection url :( 
    
    
    // Validation
    public static final String hasEqualNotEmptyRegExpPattern = "^.+=.+$";
    public static final String valRegExpPattern  = "^((\\\"([^\\\"]+)\\\")|([^,=\\{\\}\\[\\]]+))+$";
    //public static final String valRegExpPattern  = "((\\\"([^\\\"]+)\\\")|([^,=\\{\\}\\[\\]]+))+$";
    public static final String propRegExpPattern = "^((\\\"([^\\\"]+)\\\")|([^,=\\{\\}\\[\\]]+))+$";
 
    //public static final String propRegExpPattern = "((\\\"([^\\\"]+)\\\")|([^,=\\{\\}\\[\\]]+))+";
    public static final String propertyRegExpPattern =
            "^((\\\"([^\"]+)\\\")|([^,=\\{\\}\\[\\]]+))+=((\\\"([^\"]+)\\\")|([^,=\\{\\}\\[\\]]+))+$";
    
    //Default Properties
    public static final String DATABASE      = "DatabaseName";
    public static final String SERVER        = "ServerName";
    public static final String URL           = "Url";
    public static final String PROTOCOL      = "NetworkProtocol"; //"NetworkProtocol";
    public static final String SELECT_METHOD = "SelectMethod";
    public static final String RECOVERY_USER = "RecoveryUser";
    public static final String RECOVERY_PASS = "RecoveryPass";
    public static final String PORT          = "PortNumber";
    
    
    public static final String[][] ibmDefaults        = {{JBossJDBCConstants.DATABASE, "ibmdb2db"}};
    
    public static final String[][] oracleDefaults     = {{JBossJDBCConstants.URL, "jdbc:oracle:oci8:@tc"}};
    
    public static final String[][] mysqlDefaults      = {{JBossJDBCConstants.SERVER, "localhost"}
                                            ,{JBossJDBCConstants.DATABASE, "mysqldb"}};
    
    public static final String[][] postgresqlDefaults = {{JBossJDBCConstants.SERVER, "localhost"}
                                            ,{JBossJDBCConstants.DATABASE, "postgresdb"}
                                            ,{JBossJDBCConstants.PORT, "5432"}};
    
    public static final String[][] microsoftDefaults  = {{JBossJDBCConstants.SERVER, "localhost"}
                                            ,{JBossJDBCConstants.DATABASE, "mssqldb"}
                                            ,{JBossJDBCConstants.SELECT_METHOD,"cursor"}};
    
    public static final String[][] sybaseDefaults     = {{JBossJDBCConstants.SERVER, "localhost"}
                                            ,{JBossJDBCConstants.DATABASE,"sybase"}
                                            ,{JBossJDBCConstants.PORT, "4100"}
                                            ,{JBossJDBCConstants.PROTOCOL, "Tds"}};
   
    //Use for JBossDatasourceConfigPanel -> DynamicPanels
    public static ArrayList<String> driverIndex = new ArrayList<String>(){{
    add(ibmJdbcName);
    add(sybaseJdbcName);
    add(mysqlJdbcName);
    add(postgresqlJdbcName);
    add(microsoftJdbcName);
    add(oracleJdbcName);
    }};

    public static final HashMap<String, String[][]> defaultsMap = new HashMap<String, String[][]>();
   
    static {
        defaultsMap.put(ibmJdbcName, ibmDefaults);
        defaultsMap.put(sybaseJdbcName, sybaseDefaults);
        defaultsMap.put(mysqlJdbcName, mysqlDefaults);
        defaultsMap.put(postgresqlJdbcName, postgresqlDefaults);
        defaultsMap.put(microsoftJdbcName, microsoftDefaults);
        defaultsMap.put(oracleJdbcName, oracleDefaults);
    }
    
    public static final HashMap<String, String> vendorPropertyMap = new HashMap<String, String>(); 
    static {
        vendorPropertyMap.put(ibmJdbcName + ".DatabaseName", "");
        
        vendorPropertyMap.put(oracleJdbcName + ".URL", "");
        
        vendorPropertyMap.put(mysqlJdbcName + ".ServerName", "");
        vendorPropertyMap.put(mysqlJdbcName + ".DatabaseName", "");
        
        vendorPropertyMap.put(sybaseJdbcName + ".ServerName", "");
        vendorPropertyMap.put(sybaseJdbcName + ".DatabaseName", "");
        vendorPropertyMap.put(sybaseJdbcName + ".PortNumber", "");
        vendorPropertyMap.put(sybaseJdbcName + ".NetworkProtocol", "");
        
        vendorPropertyMap.put(postgresqlJdbcName + ".ServerName", "");
        vendorPropertyMap.put(postgresqlJdbcName + ".DatabaseName", "");
        vendorPropertyMap.put(postgresqlJdbcName + ".PortNumber", "");
        
        vendorPropertyMap.put(microsoftJdbcName + ".ServerName", "");
        vendorPropertyMap.put(microsoftJdbcName + ".DatabaseName", "");
        vendorPropertyMap.put(microsoftJdbcName + ".SelectMethod", "");
    }
    
    public static final HashMap<String, String> sqlDialectMap = new HashMap<String, String>();
    static {
    	sqlDialectMap.put(ibmJdbcName, ibmDialect);
    	sqlDialectMap.put(mysqlJdbcName, mysqlDialect);
    	sqlDialectMap.put(postgresqlJdbcName, postgresqlDialect);
    	sqlDialectMap.put(microsoftJdbcName, microsoftDialect);
    	sqlDialectMap.put(oracleJdbcName, oracleDialect);
    	sqlDialectMap.put(h2JdbcName, h2Dialect);
    	sqlDialectMap.put(sybaseJdbcName, sybaseDialect);
    }
    
    public static final HashMap<String,String> classnameMap = new HashMap<String, String>();
    static {
    	classnameMap.put(ibmJdbcName, ibmDriverClassName);
    	classnameMap.put(mysqlJdbcName, mysqlDriverClassName);
    	classnameMap.put(postgresqlJdbcName, postgresqlDriverClassName);
    	classnameMap.put(microsoftJdbcName, microsoftDriverClassName);
    	classnameMap.put(oracleJdbcName, oracleDriverClassName);
        classnameMap.put(sybaseJdbcName, sybaseDriverClassName);
    	classnameMap.put(h2JdbcName, h2DriverClassName);
    }
    
    public static final HashMap<String,String> classnameToJDBCMap = new HashMap<String,String>();
    static {
    	classnameToJDBCMap.put(ibmDriverClassName,ibmJdbcName);
    	classnameToJDBCMap.put(mysqlDriverClassName,mysqlJdbcName);
    	classnameToJDBCMap.put(postgresqlDriverClassName,postgresqlJdbcName);
    	classnameToJDBCMap.put(microsoftDriverClassName,microsoftJdbcName);
    	classnameToJDBCMap.put(oracleDriverClassName,oracleJdbcName);
    	classnameToJDBCMap.put(h2DriverClassName,h2JdbcName);
    	classnameToJDBCMap.put(sybaseDriverClassName,sybaseJdbcName);
    }
    
    public static final HashMap<String,String> driverJarPath = new HashMap<String,String>();
    static {
    	driverJarPath.put(ibmJdbcName, ibmDirStruct);
    	driverJarPath.put(mysqlJdbcName, mysqlDirStruct);
    	driverJarPath.put(postgresqlJdbcName, postgresqlDirStruct);
    	driverJarPath.put(microsoftJdbcName, microsoftDirStruct);
    	driverJarPath.put(oracleJdbcName, oracleDirStruct);
    	driverJarPath.put(h2JdbcName, h2DirStruct);
    }
    
    public static final HashMap<String,String> connUrlMap = new HashMap<String,String>();
    static {
    	connUrlMap.put(ibmJdbcName, ibmConnUrl);
    	connUrlMap.put(mysqlJdbcName, mysqlConnUrl);
    	connUrlMap.put(postgresqlJdbcName, postgresqlConnUrl);
    	connUrlMap.put(microsoftJdbcName, microsoftConnUrl);
    	connUrlMap.put(oracleJdbcName, oracleConnUrl);
    	connUrlMap.put(sybaseJdbcName, sybaseConnUrl);
    }
    
    public static final ArrayList<String> classnameList = new ArrayList<String>();
    static {
    	classnameList.add(ibmDriverClassName);
    	classnameList.add(mysqlDriverClassName);
    	classnameList.add(postgresqlDriverClassName);
    	classnameList.add(microsoftDriverClassName);
    	classnameList.add(oracleDriverClassName);
    	classnameList.add(sybaseDriverClassName);
    	classnameList.add(h2DriverClassName);
    }
}

