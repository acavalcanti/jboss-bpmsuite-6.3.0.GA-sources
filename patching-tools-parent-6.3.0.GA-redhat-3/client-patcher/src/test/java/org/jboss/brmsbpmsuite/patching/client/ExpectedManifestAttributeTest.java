package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.Charset;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpectedManifestAttributeTest extends BaseClientPatcherTest {

    private final String SAMPLE_MANIFEST_CONTENT = "Manifest-Version: 1.0\n" +
            "Implementation-Title: KIE Workbench - Distribution Wars\n" +
            "Os-Version: 2.6.32-504.8.1.el6.x86_64\n" +
            "Built-By: mockbuild\n" +
            "Specification-Vendor: JBoss by Red Hat\n" +
            "Created-By: Apache Maven\n";

    @Before
    public void storeManifestContentInFile() throws Exception {
        // need to store the file before each test, because the tmp dir is cleaned before each test
        Files.write(SAMPLE_MANIFEST_CONTENT, new File(tmpDir, "SAMPLE-MANIFEST.MF"), Charset.forName("UTF-8"));
    }

    @Test
    public void shouldFailWhenNonExistingManifestFileSpecified() {
        ExpectedManifestAttribute attribute = new ExpectedManifestAttribute("bogus-name", "", "");
        assertFalse("Should reject entry when MANIFEST.MF does not exist!", attribute.isPresent(new File("bogus-dir")));
    }

    @Test
    public void shouldRejectUnknownManifestAttribute() {
        ExpectedManifestAttribute attribute = new ExpectedManifestAttribute("SAMPLE-MANIFEST.MF", "non-existing-attribute", "");
        assertFalse("Result should be 'false' for non-existing attribute!", attribute.isPresent(tmpDir));
    }

    @Test
    public void shouldRejectManifestAttributeWithIncorrectValue() {
        ExpectedManifestAttribute attribute = new ExpectedManifestAttribute("SAMPLE-MANIFEST.MF", "Implementation-Title",
                "bogus");
        assertFalse("Result should be 'false' for un-expected attribute value!", attribute.isPresent(tmpDir));
    }

    @Test
    public void shouldAcceptManifestAttributeWithCorrectNameAndValue() {
        ExpectedManifestAttribute attribute = new ExpectedManifestAttribute("SAMPLE-MANIFEST.MF", "Implementation-Title",
                "KIE Workbench - Distribution Wars");
        assertTrue("Result should be 'true' for attribute with expected value!", attribute.isPresent(tmpDir));
    }

    @Test
    public void shouldUseDefaultManifestLocationIfNoneSpecified() throws Exception {
        FileUtils.forceMkdir(new File(tmpDir, "META-INF"));
        Files.write(SAMPLE_MANIFEST_CONTENT, new File(tmpDir, "META-INF/MANIFEST.MF"), Charset.forName("UTF-8"));
        ExpectedManifestAttribute attribute = new ExpectedManifestAttribute("Implementation-Title",
                "KIE Workbench - Distribution Wars");
        assertTrue("Result should be 'true' for attribute with expected value!", attribute.isPresent(tmpDir));
    }

    @Test
    public void shouldSuccessfullyProcessRealManifestFile() throws Exception {
        Files.copy(getCPResourceAsFile("/MANIFEST-EAP6X-BC.MF"), new File(tmpDir, "MANIFEST-EAP6X-BC.MF"));
        ExpectedManifestAttribute attribute = new ExpectedManifestAttribute("MANIFEST-EAP6X-BC.MF", "Implementation-Title",
                "KIE Workbench - Distribution Wars");
        assertTrue("Result should be 'true' for attribute with expected value!", attribute.isPresent(tmpDir));
    }

}
