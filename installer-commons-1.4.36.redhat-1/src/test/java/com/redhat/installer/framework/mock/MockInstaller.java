package com.redhat.installer.framework.mock;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

public class MockInstaller extends MockCompiler
{
    protected AbstractUIProcessHandler handler;
    protected static AutomatedInstallData idata;
    protected MockProcessingClient mockProcessingClient;

    final protected static String resourcePath = "src/test/resources/mockFiles/";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @BeforeClass
    public static void initial() throws Exception {
        idata = new AutomatedInstallData();
    }

    @Before
    public void start() throws Exception
    {
        handler = new MockAbstractUIProcessHandler();
        mockProcessingClient = new MockProcessingClient();
    }

    @After
    public void end()
    {
        idata.getVariables().clear();
    }

}
