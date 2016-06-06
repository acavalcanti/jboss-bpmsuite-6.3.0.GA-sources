package com.redhat.installer.framework.testers;

import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.framework.mock.MockAbstractUIProcessHandler;
import com.redhat.installer.tests.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by thauser on 2/4/14.
 */
public abstract class ProcessPanelTester extends InstallDataTester {
    public AbstractUIProcessHandler handler = new MockAbstractUIProcessHandler();

    @AfterClass
    public static void destroy() throws Exception{
        TestUtils.destroyVariableSubstitutorIdata();
        TestUtils.destroyIdataSingleton();
    }

    @Before
    public void start() throws Exception {
        handler = new MockAbstractUIProcessHandler();
    }

    @After
    public void end() throws Exception {
        handler = null;
    }

    /**
     * All individual ProcessPanel jobs must be able to be instantiated by the ProcessPanelWorker IzPack class
     */
    @Test
    public abstract void testProcessPanelInstantiation();


    public class MockProcessPanelWorker {

        public MockProcessPanelWorker(){}
        public void loadClass(String classname, AbstractUIProcessHandler handler) throws IllegalAccessException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
            ClassLoader load = this.getClass().getClassLoader();
            Class procClass = load.loadClass(classname);
            Object o = procClass.newInstance();
            Method m = procClass.getMethod("run", new Class[]{AbstractUIProcessHandler.class, String[].class});
        }
    }
}
