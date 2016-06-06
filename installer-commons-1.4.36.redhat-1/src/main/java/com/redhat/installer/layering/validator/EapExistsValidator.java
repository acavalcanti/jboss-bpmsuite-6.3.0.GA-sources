package com.redhat.installer.layering.validator;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.util.Debug;
import com.redhat.installer.layering.constant.ValidatorConstants;
import com.redhat.installer.layering.util.PlatformUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator checks bin/product.conf and modules/layers.conf to try and determine what product we're currently
 * installing onto.
 *
 * @author thauser
 */

public class EapExistsValidator implements DataValidator {
    private static final String VERSION_REGEX = "\\b\\d\\.\\d\\.\\d[\\.\\w\\w\\d]*\\b";
    private static final String EAP_PACK_ID = "eap";
    private AutomatedInstallData idata;
    private String error;
    private String message;

    @Override
    public Status validateData(AutomatedInstallData idata) {
        this.idata = idata;
        switch (eapExists()) {
            case 0:
                return Status.OK;
            case 1:
                return Status.WARNING;
            case 2:
                return Status.ERROR;
            default:
                return Status.ERROR;
        }

    }

    /**
     * Returns a status based upon the results of a look into the provided INSTALL_PATH
     * Status Meanings
     * ===============
     * 0: Found every file we needed. [OK]
     * 1: Some files are missing, but we can just warn the user, or EAP was detected (experimental) [WARNING]
     * 2: critical files are missing. [ERROR]
     * <p/>
     * Variables that will be set
     * =============================
     * ValidatorConstants.existingLayers   [existing.layers.conf]
     * ValidatorConstants.existingProduct  [existing.product.conf]
     */
    private int eapExists() {
        String path = idata.getVariable("INSTALL_PATH") + File.separator;
        File selectedInstallPath = new File(path);

        // this is necessary because we could have the user go back and forth, which would mean old values will
        // destroy the logic at the end of the installation
        idata.setVariable(ValidatorConstants.existingLayers, "");
        idata.setVariable(ValidatorConstants.existingProduct, "");

        if (installsEap()) {
            if (!selectedInstallPath.exists() || selectedInstallPath.listFiles().length == 0) {
                setVariablesForNoEapFound();
                return 0;

            } else if (selectedInstallPath.exists() && PlatformUtil.isWindows() && containsOnlyIzPackTempFiles(selectedInstallPath)) {
                setVariablesForNoEapFound();
                return 0;

            } else if (selectedInstallPath.exists() && selectedInstallPath.listFiles().length != 0 &&
                    idata.getVariable(ValidatorConstants.productName).equals(ValidatorConstants.eap)) {
                setErrorMessageId("EapExistsValidator.eap.folder.nonempty");
                setMessage(String.format(idata.langpack.getString(error), selectedInstallPath.getAbsolutePath()));
                return 2;
            }
        } else if (selectedInstallPath.exists() && selectedInstallPath.listFiles().length != 0 &&
                idata.getVariable(ValidatorConstants.productName).equals(ValidatorConstants.eap)) {
            setErrorMessageId("EapExistsValidator.eap.folder.nonempty");
            setMessage(String.format(idata.langpack.getString(error), selectedInstallPath.getAbsolutePath()));
            return 2;
        }


        // if we get to here, we have to try and confirm that EAP does exist here.
        // EAP exists if the following are all true:
        //   a) $JBOSS_HOME/bin/product.conf exists and contains "slot=eap"
        //   b) $JBOSS_HOME/modules/layers.conf does not exist.
        //   c) $JBOSS_HOME/standalone/configuration/standalone.xml exists
        //   d) $JBOSS_HOME/bin/standalone.sh exists
        //   e) $JBOSS_HOME/domain/configuration/domain.xml exists
        //   f) $JBOSS_HOME/domain/configuration/host.xml exists
        //   g) $JBOSS_HOME/bin/domain.sh exists
        //   h) $JBOSS_HOME/version.txt contains at least EAP of the version specified
        boolean productExists = false;
        boolean layersExists = false;

        try {
            productExists =
                    readFileAndSetVariable(selectedInstallPath.getAbsolutePath() + ValidatorConstants.productConfLoc, ValidatorConstants.existingProduct, idata, "slot");
            layersExists =
                    readFileAndSetVariable(selectedInstallPath.getAbsolutePath() + ValidatorConstants.layersConfLoc, ValidatorConstants.existingLayers, idata, "layers");
        } catch (IOException e) {
            Debug.log("IOException thrown while reading products and layers files.");
        }

        if (!productExists) {
            setErrorMessageId("EapExistsValidator.product.conf.missing");
            setMessage(String.format(idata.langpack.getString(error), selectedInstallPath));
            return 2;
        }

        if (!idata.getVariable(ValidatorConstants.existingProduct).equals(ValidatorConstants.eap)) {
            if (!layersExists) {
                setErrorMessageId("EapExistsValidator.layers.conf.missing");
                setMessage(String.format(idata.langpack.getString(error), selectedInstallPath));
                return 2; // if the product ISN'T eap, not having a layers.conf is a malformed installation
            }
        }

        String[] missingFiles = findMissingEapFiles(selectedInstallPath.getAbsolutePath());
        if (missingFiles.length > 0) {
            String s = "";

            for (int i = 0; i < missingFiles.length; i++) {
                if (i < missingFiles.length - 1)
                    s += missingFiles[i] + ", ";
                else
                    s += missingFiles[i] + ".";
            }
            setErrorMessageId("EapExistsValidator.missing.core.files");
            setMessage(String.format(idata.langpack.getString(getErrorMessageId()), s));
            return 2;
        }

        String existingVersion = getExistingEapVersion(selectedInstallPath.getPath());
        if (existingVersion.isEmpty()) {
            return 2;
        }
        String supportedVersion = getSupportedEapVersion(0);
        String recommendedVersion = getRecommendedEapVersion();
        String minimumSupportedVersion = getSupportedEapVersion(1);

        int supportedVersionInt = convertVersionToInt(supportedVersion);
        int recommendedVersionInt = convertVersionToInt(recommendedVersion);
        int minimumSupportedVersionInt = convertVersionToInt(minimumSupportedVersion);
        int existingVersionInt = convertVersionToInt(existingVersion);

        if (existingVersionInt < minimumSupportedVersionInt) {
            setErrorMessageId("EapExistsValidator.version.unsupported");
            setMessage(String.format(idata.langpack.getString(getErrorMessageId()), selectedInstallPath, existingVersion, minimumSupportedVersion));
            return 2;
        } else if (existingVersionInt < recommendedVersionInt && existingVersionInt >= minimumSupportedVersionInt) {
            setErrorMessageId("EapExistsValidator.version.warning");
            setMessage(String.format(idata.langpack.getString(getErrorMessageId()), selectedInstallPath, existingVersion, recommendedVersion));
            setVariablesForValidEapInstallation();
            return 1;
        } else {
            setErrorMessageId("EapExistsValidator.eap.found.warning");
            setMessage(String.format(idata.langpack.getString(getErrorMessageId()), selectedInstallPath));
            setVariablesForValidEapInstallation();
            return 0;
        }
    }

    private void setVariablesForNoEapFound() {
        idata.setVariable("eap.needs.install", "true");
        idata.setVariable(ValidatorConstants.existingProduct, ValidatorConstants.eap);
        idata.addPackToSelected(EAP_PACK_ID);
        idata.setPackPreselected(EAP_PACK_ID, true);
    }

    private void setVariablesForValidEapInstallation() {
        idata.setVariable("eap.needs.install", "false");
        idata.removePackFromSelected(EAP_PACK_ID);
        idata.setPackPreselected(EAP_PACK_ID, false);
    }

    private boolean installsEap() {
        for (Pack p : idata.allPacks) {
            if (p.id.equals("eap")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsOnlyIzPackTempFiles(File installPath) {
        boolean allTmp = true;
        for (File file : installPath.listFiles()) {
            if (!file.getName().startsWith("izWrTe") || !file.getPath().endsWith(".tmp")) {
                allTmp = false;
            }
        }
        return allTmp;
    }

    private String[] findMissingEapFiles(String installFolder) {
        String[] requiredConfigs = (idata.getVariable("product.name").equals("dv")) ? ValidatorConstants.requiredStandalonConfigDV : ValidatorConstants.requiredStandalonConfig;
        String[] requiredScripts = (System.getProperty("os.name").toLowerCase().startsWith("windows")) ? ValidatorConstants.requiredScriptsWindows : ValidatorConstants.requiredScriptsUnix;
        String[] requiredDomainConfigs = ValidatorConstants.requiredDomainConfigNonEap;
        List<String> missingFiles = new ArrayList<String>();

        for (String config : requiredConfigs) {
            String pathToConfig = installFolder + File.separator + ValidatorConstants.standaloneConfigFolder + config + ".xml";
            if (!(new File(pathToConfig).exists()))
                missingFiles.add(ValidatorConstants.standaloneConfigFolder + config + ".xml");
        }

        for (String config : requiredDomainConfigs) {
            String pathToConfig = installFolder + File.separator + ValidatorConstants.domainConfigFolderLoc + config + ".xml";
            if (!(new File(pathToConfig).exists()))
                missingFiles.add(ValidatorConstants.domainConfigFolderLoc + config + ".xml");
        }

        for (String script : requiredScripts) {
            if (!(new File(installFolder + script).exists()))
                missingFiles.add(script);
        }
        return missingFiles.toArray(new String[missingFiles.size()]);
    }

    /**
     * Reads the given file, and fills the given variable with the result.<br/>
     *
     * @param path       path of the file
     * @param variable   variable to fill with contents of said file
     * @param idata      idata reference
     * @param linePrefix the prefix the valid line should start with (either slot= or layers=)
     * @return
     * @throws IOException
     */
    private boolean readFileAndSetVariable(String path, String variable, AutomatedInstallData idata, String linePrefix) throws IOException {
        File file = new File(path);
        String value = getValidLineFromConf(file, linePrefix);
        if (value == null) {
            return false;
        } else {
            idata.setVariable(variable, value);
        }
        return true;
    }

    /**
     * Simple method that looks for a valid line in a file of the following format:<br/>
     * Lines starting with # denote a comment and are ignored.<br/>
     * Other than commented lines, files consist of a single line containing a <word>=<value> key value pair. <br/>
     * If this is not the case, the method returns null.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private String getValidLineFromConf(File file, String linePrefix) {
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;

                while ((line = br.readLine()) != null) {
                    if (!line.startsWith("#")) {
                        break;
                    }
                }

                if (line == null || br.readLine() != null || !line.startsWith(linePrefix)) { // there are no lines, or there are > 1 lines, or the first non-comment line is not valid for this file
                    br.close();
                    return null;
                }

                String[] split = line.split("=");
                if (split.length < 2) {
                    return null; // Malformed file
                }
                return split[1];
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getExistingEapVersion(String selectedInstallPath) {
        File versionText = new File(selectedInstallPath + File.separator + "version.txt");
        String versionLine = "";
        String version = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(versionText)));
            versionLine = br.readLine();
        } catch (IOException e) {
            Debug.log("IOException thrown while reading version.txt");
        } finally {
            if (br!=null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Pattern versionPattern = Pattern.compile(VERSION_REGEX);
        Matcher matcher = versionPattern.matcher(versionLine);
        if (!matcher.find()) {
            setErrorMessageId("EapExistsValidator.invalid.version");
            setMessage(String.format(idata.langpack.getString(getErrorMessageId()), selectedInstallPath));
        } else {
            version = matcher.group();
        }
        return version;
    }

    private String getRecommendedEapVersion() {
        String majorVersion = idata.getVariable("eap.recommended.version.major");
        String minorVersion = idata.getVariable("eap.recommended.version.minor");
        String releaseVersion = idata.getVariable("eap.recommended.version.micro");
        String releaseDesignation = idata.getVariable("eap.recommended.version.designation");
        return majorVersion + "." + minorVersion + "." + releaseVersion + "." + releaseDesignation;
    }

    private String getSupportedEapVersion(int type) {
        String majorVersion = idata.getVariable("eap.supported.version.major");
        String minorVersion = idata.getVariable("eap.supported.version.minor");
        String releaseVersion = idata.getVariable("eap.supported.version.micro");
        String minimumReleaseVersion = idata.getVariable("eap.supported.version.min.micro");
        String releaseDesignation = idata.getVariable("eap.supported.version.designation");
        if (type == 0 || minimumReleaseVersion == null) {
            return majorVersion + "." + minorVersion + "." + releaseVersion + "." + releaseDesignation;
        } else {
            return majorVersion + "." + minorVersion + "." + minimumReleaseVersion + "." + releaseDesignation;
        }
    }

    private int convertVersionToInt(String versionString) {
        String[] versionNumbers = versionString.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < versionNumbers.length - 1; i++) { // cut off the DR / ER / CR designation
            sb.append(versionNumbers[i]);
        }
        return Integer.parseInt(sb.toString());
    }

    private void setErrorMessageId(String e) {
        this.error = e;
    }

    public String getErrorMessageId() {
        return error;
    }

    public String getWarningMessageId() {
        return error;
    }

    private void setMessage(String string) {
        message = string;
    }

    /**
     * This is the defaultAnswer upon validateData returning a Status.WARNING. Since the only warning is about
     * the platform already existing, I think we should return false, because returning true immediately may result
     * in a loss of user data. However, for BxMS use cases, auto-installation will never succeed with a return false.
     */
    public boolean getDefaultAnswer() {
        return true;
    }

    @Override
    public String getFormattedMessage() {
        return message;
    }

}
