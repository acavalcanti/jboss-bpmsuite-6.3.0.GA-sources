package com.redhat.installer.tests.logging;

import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.logging.LogIzPackVars;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by thauser on 2/5/14.
 */
public class LogIzPackVarsTest extends ProcessPanelTester {
    private String logPath;

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder,"LogIzPackVars.message");
        logPath = idata.getInstallPath() + File.separator + TestUtils.testLogFilename;
    }

    @Test
    public void testLogFileContents() throws Exception {
        String izpackLogMsg  = "LogIzPackVars.message " + logPath;
        //MockFileBuilder.makeEmptyFile(tempFolder, "");  // not sure why TestUtils.createMockDirectory() throws an exception here.
        addToIdata("variable1", "variable2", "variable3", "variable4", "testpassword", "testpwd", "testpass");
        String val = "jboss-brms-installer-6.0.1.GA-redhat-1.jar auto.xml -variables adminPassword=admin123$,brms.password=admin123$,Pwd=mypass123";
        idata.setVariable("SYSTEM_sun_java_command",val);

        LogIzPackVars.run(handler, null);
        Set<String> result = TestUtils.getFileLinesAsSet(logPath);
        Set<String> expected = getIdataVariablesAsSet();
        expected.add(izpackLogMsg);
        assertEquals(expected, result);
    }

    @Test
    public void testObfuscator() {
        String val = "jboss-brms-installer-6.0.1.GA-redhat-1.jar auto.xml -variables adminPassword=admin123$,brms.password=admin123$,Pwd=mypass123";
        String expectedCensoredVal = "jboss-brms-installer-6.0.1.GA-redhat-1.jar auto.xml -variables adminPassword=********,brms.password=********,Pwd=********";

        assertEquals(expectedCensoredVal, LogIzPackVars.obfuscateCmdLineVariablePasswords(val));
    }

    private void addToIdata(String ... args){
        for (String s : args){
            idata.setVariable(s,s);
        }
    }

    private Set<String> getIdataVariablesAsSet(){
        Set<String> set = new HashSet<String>();
        Properties vars = idata.getVariables();
        Set<Object> keys = vars.keySet();
        for (Object key : keys){
            String strKey = (String) key;
            if (strKey.contains("password") || strKey.contains("pass") || strKey.contains("pwd")) {
               continue;
            } else if (strKey.toLowerCase().contains("system_sun_java_command")) {
                // don't print passwords when passed in as cmnd line args:
                set.add(strKey + " = " + LogIzPackVars.obfuscateCmdLineVariablePasswords((String) vars.get(strKey)));
            } else {
                set.add(strKey + " = " + vars.get(strKey));
            }
        }
        return set;
    }

    @Override
    public void testProcessPanelInstantiation() {

    }
}
