package com.redhat.installer.layering.processpanel;

import com.izforge.izpack.Pack;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.AbstractUIProcessHandler;
import com.redhat.installer.layering.constant.ValidatorConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Simple class to set the layers.conf and product.conf file contents to be correct, based upon the matrix here:
 * https://docspace.corp.redhat.com/docs/DOC-146624
 *
 * @author thauser
 */
public class ProductLayersConfSetter {
    // ProcessPanel window
    //private static AutomatedInstallData idata = AutomatedInstallData.getInstance();

    public static void run(AbstractUIProcessHandler handler, String[] args) throws IOException {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        // get the paths of the files in question
        String installPath = idata.getVariable("INSTALL_PATH");
        //
        String basePath = installPath;
        String layersPath = basePath + ValidatorConstants.layersConfLoc;
        String productPath = basePath + ValidatorConstants.productConfLoc;

        // read the variables for layers / product.conf
        // the current layers.conf / product.conf contents
        String existingLayers = idata.getVariable(ValidatorConstants.existingLayers);
        //System.out.println("existingLayers = "+existingLayers);
        String existingProduct = idata.getVariable(ValidatorConstants.existingProduct);
        //System.out.println("existingProduct = "+existingProduct);
        // the contents the product.conf / layers.conf should have after
        // installation is done
        //String newLayers = getNewLayersString();
        //System.out.println("newLayers = "+newLayers);
        String newProduct = getNewProductString();
        //System.out.println("newProduct = "+newProduct);
        String finalLayers = ""; // hold the value to write to layers.conf
        String finalProduct = ""; // hold the value to write to product.conf

        finalProduct = getFinalProductString(existingProduct, newProduct);
        //idata.setVariable("final.product.conf", finalProduct);
        //System.out.println("FinalProduct = " +finalProduct);
        finalLayers = getFinalLayersString(existingLayers);
        //idata.setVariable("final.layers.conf", finalLayers);
        //System.out.println("FinalLayers = " +finalLayers);

        writeConfFiles(basePath, finalProduct, finalLayers, productPath, layersPath);

    }

    /**
     * General method that uses pack-ids to decide what the product.conf needs to contain.
     *
     * @return
     */

    private static String getNewProductString() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String product = ValidatorConstants.eap;
        List<Pack> installedPacks = idata.selectedPacks;

        for (Pack pack : installedPacks) {
            if (pack.id.startsWith(ValidatorConstants.soa)) {
                product = ValidatorConstants.soa;
                break; // nothing can beat soa
            } else if (pack.id.startsWith(ValidatorConstants.brms)) {
                product = ValidatorConstants.brms;
            } else if (pack.id.startsWith(ValidatorConstants.bpms)) {
                product = ValidatorConstants.bpms;
            } else if (pack.id.startsWith(ValidatorConstants.sramp)) {
                product = ValidatorConstants.sramp;
            } else if (pack.id.startsWith(ValidatorConstants.dv)) {
                product = ValidatorConstants.dv;
            }
        }
        return product;
    }

    /**
     * General method that uses pack-ids to decide what the layers needs to contain.
     * Unfortunately, we need to rely on hardcoded pack-ids to decide about what the newLayers needs to contain.
     * As the amount of installers grows, so will this need to grow
     *
     * @return
     */
    private static String getNewLayersString() {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        String layers = "";
        List<Pack> installedPacks = idata.selectedPacks;

        /**
         * Decide if layers has soa. if it does, it goes first always
         */
        for (Pack pack : installedPacks) {
            // special case for RT-Gov
            if (pack.id.startsWith(ValidatorConstants.soa)) {
                layers += ValidatorConstants.soa;
                break;
            }
        }

        /**
         * Decide about sramp. it goes second
         */

        for (Pack pack : installedPacks) {
            if (pack.id.startsWith(ValidatorConstants.sramp)) {
                if (!layers.isEmpty()) { // layers has stuff in it already
                    layers += ",";
                }
                layers += ValidatorConstants.sramp;
                break; // met the requirements to add sramp to layers. short circuit
            }
        }

        /**
         * BPMS | BRMS go here. they are mutually exclusive, so
         * we can combine them here. unfortunately, the pack names are not final yet
         */

        for (Pack pack : installedPacks) {
            if (pack.id.startsWith(ValidatorConstants.brms)) {
                if (!layers.isEmpty()) {
                    layers += ",";
                }
                layers += ValidatorConstants.brms; // a little clever, only will work if pack names are equal to the constants
                break;
            } else if (pack.id.startsWith(ValidatorConstants.bpms)) {
                if (!layers.isEmpty()) {
                    layers += ",";
                }
                layers += ValidatorConstants.bpms;
                break;
            }
        }

        /**
         * EDS. the last one.
         */
        for (Pack pack : installedPacks) {
            if (pack.id.startsWith(ValidatorConstants.dv)) { // TODO: may need to change condition
                if (!layers.isEmpty()) {
                    layers += ",";
                }
                layers += ValidatorConstants.dv;
                break; // met requirements to add eds to layers. short circuit
            }
        }
        return layers;
    }

    /**
     * Writes out the layers.conf and product.conf with the new, correct values
     *
     * @param finalProduct
     * @param finalLayers
     * @param productPath
     * @param layersPath
     * @throws IOException
     */
    private static void writeConfFiles(String basePath, String finalProduct, String finalLayers, String productPath, String layersPath) {
        File productConf = new File(productPath);
        File layersConf = new File(layersPath);
        BufferedWriter prodOut = null;
        BufferedWriter layersOut = null;
        try {
            prodOut = new BufferedWriter(new FileWriter(productConf));

            prodOut.write("#product.conf written by the platform installer\n");
            prodOut.write("slot=" + finalProduct);

            layersOut = new BufferedWriter(new FileWriter(layersConf));

            layersOut.write("#layers.conf written by the platform installer\n");
            layersOut.write("layers=" + finalLayers);

            prodOut.close();
            layersOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                prodOut.close();
                layersOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Decides what the final contents of the new layers.conf needs to contain. There is no error checking here, because
     * the IsSupportedPlatformValidator already ran earlier to catch any possible error.
     *
     * @param
     * @return
     */
    private static String getFinalLayersString(String existingLayers) {
        AutomatedInstallData idata = AutomatedInstallData.getInstance();
        StringBuilder finalLayers = new StringBuilder();
        String layers = "";

        /**
         * This variable is set by the IsSupportedPlatformValidator when it runs during
         * the target panel path selection. This will contain a binary word representing
         * which combination of layers this installation will have after the installer
         * completes.
         */
        String layersWord = idata.getVariable("layersWord");

        int i = 0;

        for (char bit : layersWord.toCharArray()) {
            String layer = ValidatorConstants.layers[i];
            if (bit == '1') {
                /**
                 * Sramp and fsw/soa are special cases:
                 * They are both optional packages from the fsw installer, so we have to additionally
                 * check that the user has actually selected their installation or that they are
                 * already installed and in the layers file. Otherwise, continue.
                 */
                if ((layer.equals(ValidatorConstants.soa) ||
                        layer.equals(ValidatorConstants.sramp)) &&
                        !checkIfPackIsSelected(idata, layer) &&
                        !existingLayers.contains(layer)) {
                    i++;
                    continue;
                } else {
                    finalLayers.append(ValidatorConstants.layers[i] + ",");
                }
            } else {
                i++;
                continue;
            }
            i++;
        }

        // Remove the final comma :P
        if (finalLayers.length() > 0) {
            layers = finalLayers.substring(0, finalLayers.length() - 1);
        } else {
            layers = finalLayers.toString();
        }

        return layers;
    }

    private static boolean checkIfPackIsSelected(AutomatedInstallData idata, String layer) {

        if (idata.selectedPacks != null) {
            for (Object selectedpack : idata.selectedPacks) {
                Pack p = (Pack) selectedpack;
                if (p.id.contains(layer)) {
                    // pack is selected
                    return true;
                }
            }
        }
        // pack is not selected
        return false;
    }

    private static String getFinalProductString(String existingProduct, String newProduct) {
        if (newProduct.equals(ValidatorConstants.soa) || existingProduct.equals(ValidatorConstants.soa)) {
            return ValidatorConstants.soa;
        } else if (existingProduct.equals(ValidatorConstants.eap) || (existingProduct.equals(ValidatorConstants.sramp)) && !newProduct.equals(existingProduct)) {
            // new product takes priority in these situations
            //
            return newProduct;
        } else if ((existingProduct.equals(ValidatorConstants.brms) || existingProduct.equals(ValidatorConstants.bpms))
                && newProduct.equals(ValidatorConstants.sramp)) { //sramp loses to everything except EAP
            return existingProduct;
        } else { // the only other possibility is that we're installing a product with newProduct == ValidatorConstants.eap, thus we can just return the
            // existing product, since it's guaranteed to be correct
            return existingProduct; // eap in product.conf never beats anything, so it's safe to return the existing straight away
        }
    }
}
