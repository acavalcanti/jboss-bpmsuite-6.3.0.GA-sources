package org.jboss.brmsbpmsuite.patching.client;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * See http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html#JAR%20Manifest for more info about the JAR MANIFEST
 * format.
 */
public class ExpectedManifestAttribute implements ExpectedDistributionEntry {
    private static final Logger logger = LoggerFactory.getLogger(ExpectedManifestAttribute.class);

    private final String manifestPath;
    private final String attributeName;
    private final String attributeValue;

    public ExpectedManifestAttribute(String manifestPath, String attributeName, String attributeValue) {
        this.manifestPath = manifestPath;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public ExpectedManifestAttribute(String attributeName, String attributeValue) {
        this("META-INF/MANIFEST.MF", attributeName, attributeValue);
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    @Override
    public boolean isPresent(File dir) {
        File manifestFile = new File(dir, manifestPath);
        Manifest manifest;
        FileInputStream fileIs = null;
        try {
            fileIs = new FileInputStream(manifestFile);
            manifest = new Manifest(fileIs);
        } catch (IOException e) {
            logger.debug("Can not find file {} inside the distribution!", manifestPath, e);
            return false;
        } finally {
            IOUtils.closeQuietly(fileIs);
        }
        Attributes attributes = manifest.getMainAttributes();
        String actualValue = attributes.getValue(attributeName);
        return actualValue != null && attributeValue.equals(actualValue.trim());
    }

    @Override
    public String getPath() {
        return manifestPath;
    }

    @Override
    public ExpectedDistributionEntry withPath(String newRelativePath) {
        return new ExpectedManifestAttribute(newRelativePath, attributeName, attributeValue);
    }

    @Override
    public String toString() {
        return "ExpectedManifestEntry{" +
                "manifestPath='" + manifestPath + '\'' +
                ", attributeName='" + attributeName + '\'' +
                ", attributeValue='" + attributeValue + '\'' +
                '}';
    }

}
