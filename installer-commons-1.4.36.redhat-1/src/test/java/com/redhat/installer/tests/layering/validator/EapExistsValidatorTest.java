package com.redhat.installer.tests.layering.validator;

import com.izforge.izpack.Pack;
import com.izforge.izpack.util.OsConstraint;
import com.redhat.installer.framework.mock.MockFileBuilder;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.layering.constant.ValidatorConstants;
import com.redhat.installer.layering.validator.EapExistsValidator;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static com.redhat.installer.tests.TestUtils.*;
import static junit.framework.TestCase.assertEquals;

/**
 * Tests for the EapExistsValidator in installer-commons
 * Created by thauser on 1/29/14.
 */
public class EapExistsValidatorTest extends DataValidatorTester
{

    @Before
    public void setUp() throws Exception {
        dv = new EapExistsValidator();
        idata.langpack = TestUtils.createMockLangpack(tempFolder,
                "EapExistsValidator.server.start.missing",
                "EapExistsValidator.layers.conf.missing",
                "EapExistsValidator.standalone.conf.missing",
                "EapExistsValidator.invalid.version",
                "EapExistsValidator.product.conf.missing",
                "EapExistsValidator.domain.conf.missing",
                "EapExistsValidator.missing.core.files");
        idata.setVariable("eap.supported.version.major", "6");
        idata.setVariable("eap.supported.version.minor", "1");
        idata.setVariable("eap.supported.version.micro", "1");
        idata.setVariable("eap.supported.version.designation", "GA");
        idata.setVariable("eap.recommended.version.major", "6");
        idata.setVariable("eap.recommended.version.minor", "1");
        idata.setVariable("eap.recommended.version.micro", "1");
        idata.setVariable("eap.recommended.version.designation", "GA");
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.brms);
        Pack eapPack = new Pack("eap", "eap","eap",new ArrayList<OsConstraint>(),new ArrayList<String>(),false,false,false,"",false);
        idata.allPacks = new ArrayList<Pack>();
        idata.allPacks.add(eapPack);

    }

    /* Case: the given install path doesn't exist at all */
    @Test
    public void testNoEapExists() throws Exception {
        idata.setVariable("INSTALL_PATH", tempFolder.getRoot().getAbsolutePath() + "/new-install");
        assertStatusOk();
        assertEquals("true", idata.getVariable("eap.needs.install"));
        assertEquals(ValidatorConstants.eap, idata.getVariable(ValidatorConstants.existingProduct));
    }

    @Test
    public void testEmptyDirectory() throws Exception {
        File emptyDirectory = tempFolder.newFolder();
        idata.setVariable("INSTALL_PATH", emptyDirectory.getAbsolutePath());
        assertStatusOk();
        assertEquals("true", idata.getVariable("eap.needs.install"));
        assertEquals(ValidatorConstants.eap, idata.getVariable(ValidatorConstants.existingProduct));
    }

    /* Case: installing EAP into non-empty directory */
    @Test
    public void testEAPNonemptyDirectory() throws Exception {
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.eap);
        assertStatusError();
        assertEquals("EapExistsValidator.eap.folder.nonempty", dv.getErrorMessageId());
        assertEquals("", idata.getVariable(ValidatorConstants.existingProduct));
    }

    /* Case: directory exists and isn't empty, but product.conf doesn't exist. */
    @Test
    public void testNoProductConf() throws Exception {
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/domain/configuration/domain.xml");
        assertStatusError();
        assertEquals("EapExistsValidator.product.conf.missing", dv.getErrorMessageId());
        assertEquals("", idata.getVariable(ValidatorConstants.existingProduct));
    }

    /* Case: Valid EAP installation */
    @Test
    public void testEAPInEmptyDir() throws Exception {
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.eap);
        createProductConf(tempFolder, "eap");
        createEAPScripts(tempFolder);
        createVersionTxt(tempFolder, "JBoss Enterprise Application Platform - Version 6.1.1.GA");
        assertStatusError();
    }

    /* Case:
        missing version.txt file
     */
    @Test
    public void testNoVersionTxt() throws Exception {
        createProductConf(tempFolder, "eap");
        createEAPScripts(tempFolder);
        assertStatusError();
        assertEquals("", idata.getVariable(ValidatorConstants.existingLayers));
        assertEquals(ValidatorConstants.eap, idata.getVariable(ValidatorConstants.existingProduct));
    }

    /* Case:
        version.txt contains incompatible version
     */
    @Test
    public void testWrongVersionTxt() throws Exception {
        createProductConf(tempFolder, "eap");
        createEAPScripts(tempFolder);
        createVersionTxt(tempFolder, "JBoss Enterprise Application Platform - Version 6.1.0.GA");
        assertStatusError();
        assertEquals("", idata.getVariable(ValidatorConstants.existingLayers));
        assertEquals(ValidatorConstants.eap, idata.getVariable(ValidatorConstants.existingProduct));
        assertEquals("EapExistsValidator.version.unsupported", dv.getErrorMessageId());
    }

    /* Case:
        missing bin/standalone.sh and standalone/configuration/standalone.xml
     */
    @Test
    public void testNoStandaloneScripts() throws Exception {
        createProductConf(tempFolder, "eap");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/domain/configuration/domain.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/domain/configuration/host.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/bin/domain.sh");
        createVersionTxt(tempFolder, "6.1.1.GA");
        assertStatusError();
        assertEquals("EapExistsValidator.missing.core.files", dv.getErrorMessageId());;
    }

    /*  Case:
           missing bin/domain.sh , domain/configuration/host.xml and domain/configuration/domain.xml
     */
    @Test
    public void testNoDomainScripts() throws Exception {
        createProductConf(tempFolder, "eap");
        createVersionTxt(tempFolder, "6.1.1.GA");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/standalone/configuration/standalone.xml");
        MockFileBuilder.makeEmptyFileAtPath(tempFolder, "/bin/standalone.sh");
        assertStatusError();
        assertEquals("EapExistsValidator.missing.core.files", dv.getErrorMessageId());
    }

    /* Case:
        neither standalone.sh/.xml or domain.sh/.xml + host.xml exist.
     */
    @Test
    public void testNoScripts() throws Exception {
        createProductConf(tempFolder, "eap");
        createVersionTxt(tempFolder, "6.1.1.GA");
        assertStatusError();
        assertEquals("EapExistsValidator.missing.core.files", dv.getErrorMessageId());
    }

    /* Case:
        product.conf contains non-eap platform and layers.conf doesn't exist.
     */
    @Test
    public void testDVNoLayersConf() throws Exception {
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.dv);
        createProductConf(tempFolder, ValidatorConstants.brms);
        assertStatusError();
        assertEquals(ValidatorConstants.brms, idata.getVariable(ValidatorConstants.existingProduct));
        assertEquals("", idata.getVariable(ValidatorConstants.existingLayers));
        assertEquals("EapExistsValidator.layers.conf.missing", dv.getErrorMessageId());
    }

    /* Case:
        product.conf is malformed
     */
    @Test
    public void testEAPBadProductConf() throws Exception {
        createProductConf(tempFolder, "");
        assertStatusError();
        assertEquals("", idata.getVariable(ValidatorConstants.existingProduct));
        assertEquals("EapExistsValidator.product.conf.missing", dv.getErrorMessageId());
    }

    /* Case:
        non-eap installation layers.conf is malformed
     */
    @Test
    public void testDVBadLayersConf() throws Exception {
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.dv);
        createProductConf(tempFolder, ValidatorConstants.brms);
        createLayersConf(tempFolder, "");
        assertStatusError();
        assertEquals("", idata.getVariable(ValidatorConstants.existingLayers));
        assertEquals("EapExistsValidator.layers.conf.missing", dv.getErrorMessageId());
    }

    /* Case:
        test comment-skipping of the file reading method
     */
    @Test
    public void testCommentedLineSkip() throws Exception {
        MockFileBuilder.makeNewFileFromStringsAtPath(tempFolder, "/bin/product.conf", "#this is a comment", "#another comment", "slot=eap");
        createEAPScripts(tempFolder);
        createVersionTxt(tempFolder, "JBoss Enterprise Application Platform - Version 6.1.1.GA");
        assertStatusOk();
        assertEquals(ValidatorConstants.eap, idata.getVariable(ValidatorConstants.existingProduct));
    }

    @Test
    public void testNewerEap() throws Exception {
        createProductConf(tempFolder, "eap");
        createEAPScripts(tempFolder);
        createVersionTxt(tempFolder, "JBoss Enterprise Application Platform - Version 6.3.0.GA");
        assertStatusOk();
        assertEquals("", idata.getVariable(ValidatorConstants.existingLayers));
        assertEquals(ValidatorConstants.eap, idata.getVariable(ValidatorConstants.existingProduct));
    }

    @Test
    public void testMissingEapConfigForDV() throws Exception {
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.dv);
        createProductConf(tempFolder, "eap");
        createEAPScripts(tempFolder);
        createVersionTxt(tempFolder, "Red Hat JBoss Enterprise Application Platform - Version 6.1.1.GA");
        FileUtils.forceDelete(new File(tempFolder.getRoot().getAbsoluteFile() + "/standalone/configuration/standalone-ha.xml"));
        assertStatusError();
        assertEquals(dv.getErrorMessageId(), "EapExistsValidator.missing.core.files");
    }

    @Test
    public void testValidEapConfigForDV() throws Exception {
        idata.setVariable(ValidatorConstants.productName, ValidatorConstants.dv);
        createProductConf(tempFolder, "eap");
        createEAPScripts(tempFolder);
        createVersionTxt(tempFolder, "Red Hat JBoss Enterprise Application Platform - Version 6.1.1.GA");
        FileUtils.forceDelete(new File(tempFolder.getRoot().getAbsoluteFile() + "/standalone/configuration/standalone-full.xml"));
        FileUtils.forceDelete(new File(tempFolder.getRoot().getAbsoluteFile() + "/standalone/configuration/standalone-osgi.xml"));
        assertStatusOk();
        assertEquals(dv.getErrorMessageId(), "EapExistsValidator.eap.found.warning");
    }
}
