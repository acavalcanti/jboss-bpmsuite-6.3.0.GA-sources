package com.redhat.installer.tests.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.testers.ValidatorTester;
import com.redhat.installer.layering.validator.SecurityDomainNameValidator;
import org.junit.*;

/**
 * Created by thauser on 2/18/14.
 */
public class SecurityDomainNameValidatorTest extends ValidatorTester {
    private static AutomatedInstallData idata;

    @BeforeClass
    public static void specificInit() throws Exception {
        idata = new AutomatedInstallData();
        idata.setVariable("securitydomain.preexisting.names","other,jboss-ejb-policy,jboss-web-policy,");
    }
    @Before
    public void setUp() throws Exception {
        v = new SecurityDomainNameValidator();

    }

    @AfterClass
    public static void destroy() throws Exception {
        TestUtils.destroyIdataSingleton();
    }

    @After
    public void tearDown() throws Exception {
        idata.getVariables().clear();
    }

    @Test
    public void testNoClash() throws Exception {
        mpc.addToFields("newDomain");
        assertTrueResult();
    }

    @Test
    public void testClash() throws Exception {
        mpc.addToFields("other");
        assertFalseResult();

    }



}
