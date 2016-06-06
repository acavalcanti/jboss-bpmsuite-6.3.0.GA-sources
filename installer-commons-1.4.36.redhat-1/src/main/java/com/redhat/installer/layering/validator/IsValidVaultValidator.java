package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.redhat.installer.layering.PreExistingConfigurationConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thauser on 6/23/15.
 */
public class IsValidVaultValidator implements DataValidator {
    AutomatedInstallData idata;
    String resolvedKeystorePath;
    String errorMessageId;
    String formattedMessage;


    @Override
    public Status validateData(AutomatedInstallData adata) {
        idata = adata;
        return validatePreExistingVault();
    }

    private Status validatePreExistingVault() {
        String installPath = idata.getInstallPath();
        boolean vaultFound = false;
        for (String descriptor : PreExistingConfigurationConstants.descriptors) {
            String standaloneDescriptorPath = installPath + "/standalone/configuration/" + descriptor;
            String domainDescriptorPath = installPath + "/domain/configuration/" + descriptor;
            File standaloneDesc = new File(standaloneDescriptorPath);
            File domainDesc = new File(domainDescriptorPath);
            Document standaloneDoc = null;
            Document domainDoc = null;
            try {
                if (standaloneDesc.exists() && standaloneDesc.isFile()) {
                    standaloneDoc = Jsoup.parse(standaloneDesc, "UTF-8", "");
                    if (vaultExists(standaloneDoc)) {
                        vaultFound = true;
                        if (pathSubstitutionPerformed(standaloneDoc)) {
                            break;
                        }
                    }
                } else if (domainDesc.exists() && domainDesc.isFile()) {
                    domainDoc = Jsoup.parse(domainDesc, "UTF-8", "");
                    if (vaultExists(domainDoc)) {
                        vaultFound = true;
                        if (pathSubstitutionPerformed(domainDoc)) {
                            break;
                        }
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!vaultFound) {
            return Status.OK;
        }

        File keystoreFile = new File(resolvedKeystorePath);
        if (!keystoreFile.exists() || !keystoreFile.isFile() || !keystoreFile.isAbsolute()) {
            setErrorMessageId("IsValidVaultValidator.missing.keystore");
            setFormattedMessage(idata.langpack.getString(getErrorMessageId()));
            return Status.ERROR;
        } else {
            idata.setVariable("vault.resolved.keystoreloc", resolvedKeystorePath);
            return Status.OK;
        }
    }

    /**
     * Returns true if at least one substitution was made in the value
     * @param doc
     * @return
     */
    private boolean pathSubstitutionPerformed(Document doc) {
        if (vaultExists(doc)) {
            String keystorePath = doc.select("vault > vault-option[name=KEYSTORE_URL]").first().attr("value");
            resolvedKeystorePath = keystorePath;
            while (resolvedKeystorePath.matches(".*\\$\\{.*\\}.*")) {
                String oldPath = resolvedKeystorePath;
                resolvedKeystorePath = replaceProperties(resolvedKeystorePath, doc);
                if (oldPath == resolvedKeystorePath) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean vaultExists(Document doc) {
        return !doc.select("vault").isEmpty();
    }

    private String replaceProperties(String keystorePath, Document doc) {
        String returnValue = replaceServerProperties(keystorePath);
        if (returnValue.matches(".*\\$\\{.*\\}.*")) {
            return replaceUserProperties(returnValue, doc);
        } else {
            return returnValue;
        }
    }

    private String replaceUserProperties(String inputString, Document doc) {
        String propertyName = getFirstProperty(inputString);
        String propertyValue = findPropertyInDescriptors(propertyName, doc);
        return (propertyValue != null) ? replaceProperty(inputString, propertyName, propertyValue) : inputString;
    }

    // TODO: refactor
    private String findPropertyInDescriptors(String propertyName, Document doc) {
        for (String descriptor : PreExistingConfigurationConstants.standaloneDescriptors) {
            File descriptorFile = new File(AutomatedInstallData.getInstance().getInstallPath() + "/standalone/configuration/" + descriptor);
            Elements propertyElements = getPropertyWithName(descriptorFile, propertyName, doc);
            if (!propertyElements.isEmpty()) {
                return propertyElements.last().attr("value");
            }
        }
        for (String descriptor : PreExistingConfigurationConstants.domainDescriptors) {
            File descriptorFile = new File(AutomatedInstallData.getInstance().getInstallPath() + "/domain/configuration/" + descriptor);
            Elements propertyElements = getPropertyWithName(descriptorFile, propertyName, doc);
            if (!propertyElements.isEmpty()) {
                return propertyElements.last().attr("value");
            }
        }
        return null;
    }

    private Elements getPropertyWithName(File descriptorFile, String propertyName, Document document) {
        return document.select("system-properties > property[name=" + propertyName + "]");
    }

    private String replaceServerProperties(String keystorePath) {
        Map<String, String> standaloneMap = new HashMap<String, String>();

        // assumptions
        standaloneMap.put("jboss.home.dir", idata.getInstallPath());
        standaloneMap.put("jboss.server.base.dir", standaloneMap.get("jboss.home.dir") + "/standalone");
        standaloneMap.put("jboss.server.config.dir", standaloneMap.get("jboss.server.base.dir") + "/configuration");
        standaloneMap.put("jboss.server.data.dir", standaloneMap.get("jboss.server.base.dir") + "/data");
        standaloneMap.put("jboss.server.log.dir", standaloneMap.get("jboss.server.base.dir") + "/log");

        String propertyName = getFirstProperty(keystorePath);
        if (standaloneMap.containsKey(propertyName)) {
            keystorePath = replaceProperty(keystorePath, propertyName, standaloneMap.get(propertyName));
        }
        return keystorePath;
    }

    private String replaceProperty(String inputString, String propertyName, String propertyValue) {
        return inputString.replace("${" + propertyName + "}", propertyValue);
    }

    private String getFirstProperty(String inputString) {
        return inputString.substring(inputString.indexOf('{') + 1, inputString.indexOf('}'));
    }

    @Override
    public String getErrorMessageId() {
        return errorMessageId;
    }

    @Override
    public String getWarningMessageId() {
        return errorMessageId;
    }

    @Override
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setErrorMessageId(String errorMessageId) {
        this.errorMessageId = errorMessageId;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }
}
