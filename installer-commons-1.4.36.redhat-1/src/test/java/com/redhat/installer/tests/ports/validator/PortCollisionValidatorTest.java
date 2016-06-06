package com.redhat.installer.tests.ports.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.ports.validator.PortCollisionValidator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by thauser on 2/3/14.
 */
public class PortCollisionValidatorTest extends DataValidatorTester
{
    static final String CONFIG_VAR = "current.test.config";
    static final String DOMAIN_HA = "domain.h";
    static final String DOMAIN_FULL_HA = "domain.fa";
    static final String STANDALONE_FULL_HA = "standalone.fa";
    static final String STANDALONE_HA = "standalone.h";

    @BeforeClass
    public static void specificInit() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        // a little explanation: PortCollisionValidator is an abstract class that is implemented by many classes
        // in installer-commons (see *CollisionValidator). Using this anonymous class, we can test all of these at once by
        // making sure we manipulate the idata instance appropriately.
        idata.langpack = TestUtils.createMockLangpack(tempFolder, "port.collision.check.template");
        dv = new PortCollisionValidator() {
            @Override
            protected String getConfig() {
                return AutomatedInstallData.getInstance().getVariable(CONFIG_VAR);
            }

            /**
             * We can test all of the collision validators at once using this implementation
             * @return
             */
            @Override
            protected ArrayList<String> getExclusions(){

                if (idata.getVariable(CONFIG_VAR).equals(DOMAIN_FULL_HA)){
                    ArrayList<String> exclusions = new ArrayList<String>();
                    exclusions.add("domain.fa.jgroups-mping.port");
                    exclusions.add("domain.fa.messaging-group");
                    exclusions.add("domain.fa.modcluster.port");
                    return exclusions;
                } else if (idata.getVariable(CONFIG_VAR).equals(STANDALONE_FULL_HA)){
                    ArrayList<String> exclusions = new ArrayList<String>();
                    exclusions.add("standalone.fa.jgroups-mping");
                    exclusions.add("standalone.fa.messaging");
                    exclusions.add("standalone.fa.modcluster");
                    return exclusions;
                } else if (idata.getVariable(CONFIG_VAR).equals(DOMAIN_HA)){
                    ArrayList<String> exclusions = new ArrayList<String>();
                    exclusions.add("domain.h.jgroups-mping.port");
                    exclusions.add("domain.h.modcluster.port");
                    return exclusions;
                } else if (idata.getVariable(CONFIG_VAR).equals(STANDALONE_HA)){
                    ArrayList<String> exclusions = new ArrayList<String>();
                    exclusions.add("standalone.h.jgroups-mping");
                    exclusions.add("standalone.h.modcluster");
                    return exclusions;
                } else {
                    return null;
                }
            }
        };

    }

    private void fillIdataNoCollisions(String config) {
        idata.setVariable(CONFIG_VAR, config);
        idata.setVariable(config+".testport1", "${portprop:9999}");
        idata.setVariable(config+".testport2", "10000");
    }

    private void fillIdataCollisions(String config){
        idata.setVariable(CONFIG_VAR, config);
        idata.setVariable(config+".testport1", "${portprop:9999}");
        idata.setVariable(config+".testport2", "9999");
    }

    private void testExclusions(String config){
        idata.setVariable(CONFIG_VAR, config);
        if (config.equals(DOMAIN_HA) || config.equals(DOMAIN_FULL_HA)){
            idata.setVariable(config+".modcluster.port","${portprop:0}");
            idata.setVariable(config+".testport1", "0");
        } else if (config.equals(STANDALONE_HA) || config.equals(STANDALONE_FULL_HA)){
            idata.setVariable(config+".modcluster", "${portprop:0}");
            idata.setVariable(config+".testport1", "0");
        }
    }

    @Test
    public void testStandaloneNoCollision() throws Exception {
        fillIdataNoCollisions("standalone");
        assertStatusOk();
    }

    @Test
    public void testStandaloneCollision() throws Exception {
        fillIdataCollisions("standalone");
        assertStatusError();
    }

    @Test
    public void testStandaloneHaNoCollision() throws Exception {
        fillIdataNoCollisions(STANDALONE_HA);
        assertStatusOk();
    }

    @Test
    public void testStandaloneHaCollision() throws Exception {
        fillIdataCollisions(STANDALONE_HA);
        assertStatusError();
    }

    @Test
    public void testStandaloneHaExcludes() throws Exception {
        testExclusions(STANDALONE_HA);
        assertStatusOk();
    }

    @Test
    public void testStandaloneFullNoCollision() throws Exception {
        fillIdataNoCollisions("standalone.f");
        assertStatusOk();
    }

    @Test
    public void testStandaloneFullCollision() throws Exception {
        fillIdataCollisions("standalone.f");
        assertStatusError();
    }

    @Test
    public void testStandaloneFullHaNoCollision() throws Exception {
        fillIdataNoCollisions(STANDALONE_FULL_HA);
        assertStatusOk();
    }

    @Test
    public void testStandaloneFullHaCollision() throws Exception {
        fillIdataCollisions(STANDALONE_FULL_HA);
        assertStatusError();
    }

    @Test
    public void testStandaloneFullHaExcludes() throws Exception {
        testExclusions(STANDALONE_FULL_HA);
        assertStatusOk();
    }

    @Test
    public void testDomainNoCollision() throws Exception {
        fillIdataNoCollisions("domain");
        assertStatusOk();
    }

    @Test
    public void testDomainCollision() throws Exception {
        fillIdataCollisions("domain");
        assertStatusError();
    }

    @Test
    public void testDomainHaNoCollision() throws Exception {
        fillIdataNoCollisions(DOMAIN_HA);
        assertStatusOk();
    }

    @Test
    public void testDomainHaCollision() throws Exception {
        fillIdataCollisions(DOMAIN_HA);
        assertStatusError();
    }

    @Test
    public void testDomainHaExcludes() throws Exception {
        testExclusions(DOMAIN_HA);
        assertStatusOk();
    }

    @Test
    public void testDomainFullNoCollision() throws Exception {
        fillIdataNoCollisions("domain.f");
        assertStatusOk();
    }

    @Test
    public void testDomainFullCollision() throws Exception {
        fillIdataCollisions("domain.f");
        assertStatusError();
    }

    @Test
    public void testDomainFullHaNoCollision() throws Exception {
        fillIdataNoCollisions(DOMAIN_FULL_HA);
        assertStatusOk();
    }

    @Test
    public void testDomainFullHaCollision() throws Exception {
        fillIdataCollisions(DOMAIN_FULL_HA);
        assertStatusError();
    }

    @Test
    public void testDomainFullHaExclusions() throws Exception {
        testExclusions(DOMAIN_FULL_HA);
        assertStatusOk();
    }
}



