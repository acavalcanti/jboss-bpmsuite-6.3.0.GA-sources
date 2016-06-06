package com.redhat.installer.layering.processpanel;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.layering.constant.ValidatorConstants;

import java.io.*;
import java.util.List;

public class VersionTxtSetter {

    private static AutomatedInstallData idata = AutomatedInstallData.getInstance();

    private static final String VERSION = "/version.txt";

    private static final String LAYERS = "/modules/system/layers/";
    private static final String PRODUCT = "/org/jboss/as/product/";
    private static final String MANIFEST = "/dir/META-INF/MANIFEST.MF";

    public static void run(AbstractUIProcessHandler handler, String[] args) throws IOException {

        String installPath = idata.getVariable("INSTALL_PATH");
        String versionSrcPath = installPath + LAYERS;

        String module = "";
        List<Pack> installedPacks = idata.selectedPacks;
        for (Pack pack : installedPacks) {
            if (pack.id.startsWith(ValidatorConstants.soa)) {
                module = ValidatorConstants.soa;
                break;
            } else if (pack.id.startsWith(ValidatorConstants.sramp)) {
                module = ValidatorConstants.sramp;
                break;
            } else if (pack.id.startsWith(ValidatorConstants.dv)) {
                module = ValidatorConstants.dv;
                break;
            } else if (pack.id.startsWith(ValidatorConstants.bpms)) {
                module = ValidatorConstants.bpms;
                break;
            } else if (pack.id.startsWith(ValidatorConstants.brms)) {
                module = ValidatorConstants.brms;
                break;
            }
        }

        versionSrcPath += (module + PRODUCT);

        File f = new File(versionSrcPath);
        if (!f.exists()) {
            // No manifest to read - just return.
            return;
        }
        versionSrcPath += (module + MANIFEST);

        String versionDestPath = installPath + VERSION;

        String version = getVersion(versionSrcPath);
        writeVersion(versionDestPath, version);

    }

    public static String getVersion(String versionFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(versionFile));
        String version = "\n";
        try {
            String line = br.readLine();
            version += line.substring(line.indexOf(' ')).trim();
            version += " - Version ";
            line = br.readLine();
            version += line.substring(line.indexOf(' ')).trim();
        } finally {
            br.close();
        }
        return version;
    }

    public static void writeVersion(String versionFile, String content) {
        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(versionFile, true);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
