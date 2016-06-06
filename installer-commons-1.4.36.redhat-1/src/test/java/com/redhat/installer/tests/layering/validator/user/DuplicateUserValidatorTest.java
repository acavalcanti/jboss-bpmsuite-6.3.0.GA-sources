package com.redhat.installer.tests.layering.validator.user;

import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.layering.validator.user.DuplicateUserValidator;
import com.redhat.installer.tests.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thauser on 2/19/14.
 */
public class DuplicateUserValidatorTest extends DataValidatorTester
{
    File tempUserFile;
    static final String CONFIG_VAR = "current.test";
    static final String MGMT_USER = "adminUser";
    static final String FSW_USER = "fsw.user";
    static final String BPMS_USER = "bpms.user";
    static final String BRMS_USER = "brms.user";
    static final String TEIID_ADMIN = "Teiid.admin.user";
    static final String TEIID_NORM = "Teiid.norm.user";
    static final String MODESHAPE = "Modeshape.user";
    static final String MGMT_FILE = "mgmt-users.properties";
    static final String APP_FILE = "application-users.properties";

    static final List<String> configList = new ArrayList<String>();
    static {
        configList.add(MGMT_USER);
        configList.add(FSW_USER);
        configList.add(BPMS_USER);
        configList.add(BRMS_USER);
        configList.add(TEIID_ADMIN);
        configList.add(TEIID_NORM);
        configList.add(MODESHAPE);
    }

    static final Map<String,String> filenameMap = new HashMap<String,String>();
    static {
        filenameMap.put(MGMT_USER, MGMT_FILE);
        filenameMap.put(FSW_USER, APP_FILE);
        filenameMap.put(BPMS_USER, APP_FILE);
        filenameMap.put(BRMS_USER, APP_FILE);
        filenameMap.put(TEIID_ADMIN, APP_FILE);
        filenameMap.put(TEIID_NORM, APP_FILE);
        filenameMap.put(MODESHAPE, APP_FILE);
    }

    @Before
    public void setUp() throws Exception {
         // similar to the PortCollisionValidatorTest, we can
         // use an anonymous class to test all of the other classes.
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "IsSupportedPlatformValidator.incompatibleSRAMP");
        dv = new DuplicateUserValidator() {
            @Override
            protected String getUserVar() {
                return idata.getVariable(CONFIG_VAR);
            }

            @Override
            protected String getCondVar() {
                String conf = idata.getVariable(CONFIG_VAR);
                if (conf.equals(TEIID_ADMIN) || conf.equals(TEIID_NORM) || conf.equals(MODESHAPE)){
                    return conf+".add";
                } else if (conf.equals(MGMT_USER)){
                    return "addUser";
                } else {
                    return null;
                }
            }

            @Override
            protected String getFileName() {
                String conf = idata.getVariable(CONFIG_VAR);
                if (conf.equals(MGMT_USER)){
                    return MGMT_FILE;
                } else {
                    return APP_FILE;
                }
            }

            @Override
            protected Status getConflictStatus() {
                String conf = idata.getVariable(CONFIG_VAR);
                if (conf.equals(MGMT_USER)){
                    return Status.SKIP;
                } else {
                    return Status.ERROR;
                }
            }
        };
    }

    // only valid for mgmt, since it's the only skippable one
    @Test
    public void testMgmtNoAddUser(){
        idata.setVariable(CONFIG_VAR, MGMT_USER);
        idata.setVariable("addUser", "false");
        assertStatusOk();
    }

    @Test
    public void testNoDuplicateUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "newuser", "testuser=testpassword");
            assertStatusOk();
        }
    }

    @Test
    public void testDuplicateUser() throws Exception{
        for (String config : configList) {
            createTestEnvironment(config, "testuser", "testuser=testpassword");
            if (config.equals(MGMT_USER)){
                assertStatusSkip();
            } else {
                assertStatusError();
            }
        }
    }

    @Test
    public void testFileParsingDuplicateUserAddingNewUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "newuser", "testuser=testpassword", "testuser=testpassword");
            assertStatusOk();
        }
    }

    @Test
    public void testFileParsingDuplicateUserAddingDuplicateUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "testuser", "testuser=testpassword", "testuser=testpassword");
            if (config.equals(MGMT_USER)){
                assertStatusSkip();
            } else {
                assertStatusError();
            }
        }
    }

    @Test
    public void testFileParsingMissingPasswordAddingNewUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "newuser", "testuser=", "anotheruser=hihi");
            assertStatusOk();
        }
    }

    @Test
    public void testFileParsingMissingPasswordAddingDuplicateUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "testuser", "testuser=", "anotheruser=hihi");
            if (config.equals(MGMT_USER)){
                assertStatusSkip();
            } else {
                assertStatusError();
            }
        }
    }

    @Test
    public void testFileParsingMissingUsernameAddingNewUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "newuser", "testuser=", "=testpassword");
            assertStatusOk();
        }
    }

    @Test
    public void testFileParsingMissingUsernameAddingDuplicateUser() throws Exception{
        for (String config : configList){
            createTestEnvironment(config, "","=testpassword");
            if (config.equals(MGMT_USER)){
                assertStatusSkip();
            } else {
                assertStatusError();
            }
        }
    }

   @Test
    public void testFileParsingMismatchedFilesAddingNewUser() throws Exception{
       for (String config : configList) {
           removeUserFiles(filenameMap.get(config));
           MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/standalone/configuration/" + filenameMap.get(config), "testuser1=testpassword1");
           MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/domain/configuration/" + filenameMap.get(config), "testuser2=testpassword2");
           idata.setVariable(CONFIG_VAR, config);
           idata.setVariable(config, "newuser");
           assertStatusOk();
       }
    }

    @Test
    public void testFileParsingMismatchedFilesAddingDuplicateUser() throws Exception{
        for (String config : configList){
            removeUserFiles(filenameMap.get(config));
            MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/standalone/configuration/" + filenameMap.get(config), "testuser1=testpassword1");
            MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/domain/configuration/" + filenameMap.get(config), "testuser2=testpassword2");
            idata.setVariable(CONFIG_VAR, config);
            idata.setVariable(config, "testuser1");
            if (config.equals(MGMT_USER)){
                assertStatusSkip();
            } else {
                assertStatusError();
            }
            idata.setVariable(config, "testuser2");
            if (config.equals(MGMT_USER)){
                assertStatusSkip();
            } else {
                assertStatusError();
            }
        }
    }

    private void removeUserFiles(String filename){
        File standalone = new File(tempFolder.getRoot(), "/standalone/configuration/"+filename);
        File domain = new File(tempFolder.getRoot(), "/domain/configuration/"+filename);
        if (standalone.exists()) { standalone.delete(); }
        if (domain.exists()) { domain.delete(); }
    }


    private void createUserFiles(String filename, String ... args) {
        MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/standalone/configuration/" + filename, args);
        MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/domain/configuration/" + filename, args);
    }

    private void createTestEnvironment(String config, String username, String ... fileContents){
        idata.setVariable(CONFIG_VAR, config);
        idata.setVariable(config, username);
        removeUserFiles(filenameMap.get(config));
        createUserFiles(filenameMap.get(config), fileContents);
    }

    public File getTempUserFile(){
        return tempUserFile;
    }


}
