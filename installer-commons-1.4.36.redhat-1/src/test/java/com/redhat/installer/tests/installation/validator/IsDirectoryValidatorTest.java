package com.redhat.installer.tests.installation.validator;

import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.installation.validator.IsDirectoryValidator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Created by fcanas on 4/17/14.
 */
public class IsDirectoryValidatorTest extends ValidatorTester {

    private String tempPath;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void init() {
    }

    @Before
    public void setUp(){
        tempPath = tempFolder.getRoot().getAbsolutePath();
        v = new IsDirectoryValidator();
    }


    @Test
    public void testValidDirectory() {
        mpc.addToFields(tempFolder.getRoot().getAbsolutePath());
        assertTrueResult();
    }

    @Test
    public void testValidNotExistingDirectory() {
        mpc.addToFields(tempFolder.getRoot().getAbsolutePath() + File.separator + "this-dir-doesn't-exist");
        assertTrueResult();
    }

    @Test
    public void testTargetPointsToFile() throws Exception {
        tempFolder.newFile("fakeFile");
        mpc.addToFields(tempFolder.getRoot().getAbsoluteFile() + File.separator + "fakeFile");
        assertFalseResult();
    }

}
