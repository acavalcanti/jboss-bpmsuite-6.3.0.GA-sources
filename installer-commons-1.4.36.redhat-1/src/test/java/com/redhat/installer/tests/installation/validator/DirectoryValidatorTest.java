package com.redhat.installer.tests.installation.validator;

import com.izforge.izpack.installer.DataValidator;
import com.redhat.installer.framework.constants.CommonStrings;
import com.redhat.installer.framework.mock.MockInstaller;
import com.redhat.installer.installation.validator.DirectoryValidator;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static junit.framework.TestCase.assertEquals;


@RunWith(Parameterized.class)
public class DirectoryValidatorTest extends MockInstaller implements CommonStrings
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String os;
    private String rootDir;
    private String originalOS = System.getProperty("os.name");
    private String separator;
    private DirectoryValidator directoryValidator;

    private static String [] invalidWindowsCharacters;
    private static String [] invalidUnixCharacters;
    private static String [] invalidInstallerCharacters;

    private ArrayList<String> testPatterns = new ArrayList<String>();
    private static Map<String, String[]> validPathsTable = new HashMap<String, String[]>();

    private final static String [] validUnixPaths = {
            "/foo bar/with spaces/", "/foo/bar/dir/is/fine"
    };
    private final static String [] validWindowsPaths = {
            "C:\\foo bar\\with spaces\\", "C:\\Program Files\\", "C:\\Program Files (x86)\\"
    };

    private String [] invalidCharacters(String os)
    {
        if (os.toLowerCase().contains(WINDOWS)) return invalidWindowsCharacters;
        else                                    return invalidUnixCharacters;
    }

    public DirectoryValidatorTest(String os, String separator, String rootDir)
    {
        this.os = os;
        this.rootDir = rootDir;
        this.separator = separator;
    }

    private DataValidator.Status getPathStatus(String path)
    {
        idata.setInstallPath(path);
        DataValidator.Status status = directoryValidator.validateData(idata);
        return status;
    }

    private boolean isPathValid(String path)
    {
        boolean result;
        mockProcessingClient.addToFields(path);
        result = directoryValidator.validate(mockProcessingClient);
        mockProcessingClient.clear();
        return result;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data()
    {
        String fileSeperator;
        String rootDirectory;
        List<Object[]> params = new ArrayList<Object[]>();

        for (String os : OSES)
        {
            if (os.equals(WINDOWS))
            {
                rootDirectory = "C:\\";
                fileSeperator = "\\";
            }
            else
            {
                rootDirectory = "/";
                fileSeperator = "/";
            }

            Object[] param = { os , fileSeperator, rootDirectory};
            params.add(param);
        }

        return params;
    }

    @BeforeClass
    public static void init() throws Exception
    {
        invalidInstallerCharacters = DirectoryValidator.invalidCharacters;
        invalidWindowsCharacters = (String[]) ArrayUtils.addAll(invalidInstallerCharacters, DirectoryValidator.invalidWindows);
        invalidUnixCharacters= (String[]) ArrayUtils.addAll(invalidInstallerCharacters, DirectoryValidator.invalidUnix);
        validPathsTable.put(UNIX, validUnixPaths);
        validPathsTable.put(WINDOWS, validWindowsPaths);
    }

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "TargetPanel.invalid"); //Dependency of DirectoryValidator
        System.setProperty("os.name", os);

        testPatterns.add(rootDir + "%sstarting" + separator);
        testPatterns.add(rootDir + "trailing%s" + separator);
        testPatterns.add(rootDir + "%s" + separator);

        directoryValidator = new DirectoryValidator();
    }

    @After
    public void tearDown()
    {
        testPatterns.clear();
        idata.getVariables().clear();
        System.setProperty("os.name", this.originalOS);
        directoryValidator = null;
        mockProcessingClient = null;
        //System.setProperty("os.name", oldOsPropName);
    }

    @AfterClass
    public static void specificDestroy() throws Exception{
        TestUtils.destroyIdataSingleton();
    }

    @Test
    public void testInvalidPatterns()
    {
        String path;
        DataValidator.Status panelResult;
        boolean userInputResult;

        for (String pattern : testPatterns)
        {
            for (String s : invalidCharacters(os))
            {
                path = String.format(pattern, s);
                panelResult = getPathStatus(path);
                userInputResult = isPathValid(path);

                assertEquals("testInvalidPatterns|"+os+"|"+path,
                             DataValidator.Status.ERROR, panelResult);
                assertEquals("testInvalidPatterns|"+os+"|"+path,
                             false, userInputResult);
        }
        }
    }

    @Test
    public void testValidPaths()
    {
        String [] specificPaths = validPathsTable.get(os);
        DataValidator.Status panelResult;
        boolean userInputResult;

        for (String path : specificPaths)
        {
            panelResult = getPathStatus(path);
            userInputResult = isPathValid(path);
            assertEquals("testValidPaths | " + os + "'" + path + "'",
                         DataValidator.Status.OK, panelResult);
            assertEquals("testValidPaths | " + os + "'" + path + "'",
                         true, userInputResult);
        }
    }

    @Test
    public void testInvalidCharsOnly(){
        for (String s : DirectoryValidator.invalidUnix){
            String path = String.format("/foo%s/bar/%s", s, s);
            assertEquals(DataValidator.Status.ERROR, this.getPathStatus(path));
    }

        System.setProperty("os.name", "windows");
        for (String s : DirectoryValidator.invalidWindows) {
            String path = String.format("C:\\foo%sbar\\%s", s, s);
            assertEquals(DataValidator.Status.ERROR, getPathStatus(path));
        }
    }
}
