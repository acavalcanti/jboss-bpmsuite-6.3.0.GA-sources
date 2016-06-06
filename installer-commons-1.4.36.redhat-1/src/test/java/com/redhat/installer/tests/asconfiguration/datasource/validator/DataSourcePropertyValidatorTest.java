package com.redhat.installer.tests.asconfiguration.datasource.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.mock.MockDirSetter;
import com.redhat.installer.asconfiguration.datasource.validator.DataSourcePropertyValidator;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * This validator is a little weird, can't easily abstract the calls needed
 * Created by thauser on 2/3/14.
 */
public class DataSourcePropertyValidatorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private static final String VALID =  "\"valid,{}[]value\"";
    private static final String INVALID = "invalid,{}[]=value";
    DataSourcePropertyValidator dspv;
    static AutomatedInstallData idata;

    @BeforeClass
    public static void init() throws Exception{
        if (idata != null){
            TestUtils.destroyIdataSingleton();
        }
        idata = new AutomatedInstallData();
    }

    @AfterClass
    public static void destroy() throws Exception {
      //  MockDirSetter.removeMockSpecsDir();
        TestUtils.destroyIdataSingleton();
        idata = null;
    }

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "JBossDatasourceConfigPanel.xa.error.wrongFormat",
                "JBossDatasourceConfigPanel.xa.error.invalidChars",
                "JBossDatasourceConfigPanel.warning.xaProperty");
        dspv = new DataSourcePropertyValidator();

    }

    @After
    public void tearDown() throws Exception {
        idata.getVariables().clear();
        dspv = null;
    }

    @Test
    public void testValidPropertyValidValue(){
        assertTrue(dspv.validateData(VALID,VALID));
    }

    @Test
    public void testInvalidPropertyValidValue(){
        assertFalse(dspv.validateData(INVALID, VALID));
    }

    @Test
    public void testValidPropertyInvalidValue(){
        assertFalse(dspv.validateData(VALID, INVALID));
    } 
    @Test
    public void testEmptyValue(){
        assertFalse(dspv.validateData(VALID, ""));
    }

    @Test
    public void testEmptyProperty(){
        assertFalse(dspv.validateData("", VALID));
    }

    @Test
    public void testBothEmpty(){
        assertFalse(dspv.validateData("",""));
    }
}
