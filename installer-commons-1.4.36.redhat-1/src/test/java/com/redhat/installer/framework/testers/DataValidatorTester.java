package com.redhat.installer.framework.testers;

import com.izforge.izpack.installer.DataValidator;
import com.redhat.installer.framework.constants.CommonStrings;
import org.junit.After;

import static junit.framework.TestCase.assertEquals;


/**
 * Created by thauser on 2/3/14.
 */
public abstract class DataValidatorTester extends InstallDataTester implements CommonStrings {

    public DataValidator dv;
    @After
    public void end() throws Exception{
        //FileUtils.deleteDirectory(TestPaths.INSTALL_PATH_FILE);
        dv = null;
    }

    /**
     * Expect the value Status.OK from a validateData call
     */
    public void assertStatusOk(){
        DataValidator.Status result = dv.validateData(idata);
        System.out.println("Validator message: "+dv.getFormattedMessage());
        assertEquals(DataValidator.Status.OK, result);
    }

    /**
     * Expect the value Status.ERROR from a validateData call
     */
    public void assertStatusError(){
        DataValidator.Status result = dv.validateData(idata);
        System.out.println("Validator message: "+dv.getFormattedMessage());
        assertEquals(DataValidator.Status.ERROR, result);
    }

    /**
     * Expect the value Status.SKIP from a validateData call
     */
    public void assertStatusSkip(){
        DataValidator.Status result = dv.validateData(idata);
        System.out.println("Validator message: "+dv.getFormattedMessage());
        assertEquals(DataValidator.Status.SKIP, result);
    }

    /**
     * Expect the value Status.WARNING from a validateData call
     */
    public void assertStatusWarning(){
        DataValidator.Status result = dv.validateData(idata);
        System.out.println("Validator message: "+dv.getFormattedMessage());
        assertEquals(DataValidator.Status.WARNING, result);
    }

    /**
     * Expect the value Status.FAIL from a validateData call
     */
    public void assertStatusFail(){
        DataValidator.Status result = dv.validateData(idata);
        System.out.println("Validator message: "+dv.getFormattedMessage());
        assertEquals(DataValidator.Status.FAIL, result);
    }

    /**
     * Expect the given key from the langpack on a DataValidator.getFormattedMessage() call
     *
     * @param key
     */
    public void assertLangpack(String key){
        assertEquals(idata.langpack.getString(key), dv.getFormattedMessage());
    }
}
