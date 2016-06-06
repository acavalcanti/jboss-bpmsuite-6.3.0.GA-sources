package com.redhat.installer.layering.validator;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;
import com.izforge.izpack.util.Debug;
import com.redhat.installer.layering.constant.ValidatorConstants;


/** Verify that you can layer this product on the given EAP Installation
 * Requirements as per: https://mojo.redhat.com/docs/DOC-179024
 * ==============
 * Pre-Requisite
 * =============
 * 1) Ensure the the EAPExistsValidation has passed
 *    => This will set ValidatorConstants.existingLayers && ValidatorConstants.existingProduct
 *    
 * @author thauser, fcanas
 */
public class IsSupportedPlatformValidator implements DataValidator 
{
    AutomatedInstallData idata;

    /**
     * Please do not change the name of this following variable, as we have an
     * automated script that used in testing that looks for it. Thanks!
     *
     * This value needs to be updated whenever the layering requirements in
     * the document linked above change. First, copy/paste the requirement's
     * document to a text file, then use the layers tool from
     * installer-devtools repo to regenerate the truthTableOutput string:
     * From the root of installer-devtools:
     * python -m src.layerstools.supported_platforms -f <path to layers txt file>
     **/
    //private final String truthTableOutput = "11111111111111111111111111111111";
    private String truthTableOutput = "11111100111111001100000011001000";

    TruthTable table = new TruthTable(truthTableOutput);

	String error;
    String message;

    public Status validateData(AutomatedInstallData idata) {
        this.idata = idata;
        String existingProduct = idata.getVariable(ValidatorConstants.existingProduct);
        String existingLayers = (idata.getVariable(ValidatorConstants.existingLayers) != null) ? idata.getVariable(ValidatorConstants.existingLayers) : "";

        boolean disableSramp = false;
        boolean disableSoa = false;
        String newLayers = idata.getVariable(ValidatorConstants.newLayers); 					//[new.layers.conf]
        String productReadableName = idata.getVariable(ValidatorConstants.productReadableName); //[new.product.conf]
        String invalidSRAMP = idata.langpack.getString("IsSupportedPlatformValidator.incompatibleSRAMP");
        //TODO: localize strings

        Debug.trace("Layers Validator Truth Table: ");
        Debug.trace(table.toString());

        /**
         * Check we aren't installing the same product on top of already existing product install:
         */
        for (String newLayer : newLayers.split(",")) {
            /**
             * Soa and sramp are special cases because they are 'optional' packs that the user
             * can deselect from the packs panel. So trying to install on top of a soa with soa installer
             * isn't necessarily invalid. Likewise, trying to install with soa on top of sramp is still
             * valid. In both cases, we warn user that the pre-existing product can not be 're-installed'
             * again.
             */
            if (newLayer.contains(ValidatorConstants.soa) && existingLayers.contains(ValidatorConstants.soa)) {
                disableSoa = true;
                continue;
            } else if (newLayer.contains(ValidatorConstants.sramp) && existingLayers.contains(ValidatorConstants.sramp)) {
                // If we are running the sramp-only installer:
                if (newLayers.equals("sramp")) {
                    setMessage(invalidSRAMP);
                    return Status.WARNING;
                }
                // Otherwise, we are in fsw installer but sramp is pre-existing and we can't install it again:
                disableSramp = true;
                continue;
            } else if (existingLayers.contains(newLayer)) {
                setMessage(makeReadable(newLayer) + " can not be installed on top of an " +
                        "installation already containing " + makeReadable(existingLayers));
                return Status.ERROR;
            } else if (existingProduct.contains(newLayer)) {
                setMessage(makeReadable(newLayer) + " can not be installed on top of an " +
                        makeReadable(existingProduct) + " installation.");
            }
        }

        String layersWord = mapLayersToTruthTableRow(existingProduct,existingLayers,newLayers);

        /**
         * Save this info for writing to layers and prod  files later.
         */
        idata.setVariable("layersWord",layersWord);

        /**
         * Use the previously defined TruthTable to see if the current combination of
         * existing product, layers, and new layers is a valid combination.
         */
        if (table.getOutput(layersWord)) {

            // Check special cases for soa and sramp packs:

            String prods = "";
            if (disableSoa){
                prods = "fsw";
                idata.setVariable("can.install.fsw", "false");
                idata.removePackFromSelected("soa-switchyard");
                idata.setPackSelectable("soa-switchyard", false);
                idata.setPackPreselected("soa-switchyard", false);
            } else {
                // re-enable fsw
                idata.setVariable("can.install.fsw", "true");
                idata.addPackToSelected("soa-switchyard");
                idata.setPackSelectable("soa-switchyard", true);
                idata.setPackPreselected("soa-switchyard", true);
            }

            if (disableSramp){
                if (prods.contains("fsw")) {
                    prods = "fsw and sramp";
                } else {
                    prods = "sramp";
                }
                idata.setVariable("can.install.sramp", "false");
                idata.removePackFromSelected("sramp");
                idata.setPackSelectable("sramp", false);
                idata.setPackPreselected("sramp", false);
            } else {
                // re-enable sramp
                idata.setVariable("can.install.sramp", "true");
                idata.addPackToSelected("sramp");
                idata.setPackSelectable("sramp", true);
                idata.setPackPreselected("sramp", true);
            }

            if (disableSoa || disableSramp) {
                setMessage(String.format(idata.langpack.getString("IsSupportedPlatformValidator.incompatibleProduct"), prods, prods));
                return Status.WARNING;
            }

            return Status.OK;
        }

        setMessage(makeReadable(newLayers) + " can not be installed on top of an " +
                makeReadable(existingProduct) +  " containing " +
                makeReadable(existingLayers) + ".");

        return Status.ERROR;
    }

    private String makeReadable(String layers) {
        return layers.replace("soa","fsw").toUpperCase();
    }

    /**
     * Generates a string of binary that corresponds to the truth table row for a
     * given combination of products.
     * ie. existingProd: EAP, existingLayers: SOA, newLayers: DV -> 00011
     * @param existingProduct
     * @param existingLayers
     * @param newLayers
     * @return
     */
    private String mapLayersToTruthTableRow(String existingProduct, String existingLayers, String newLayers) {

        StringBuilder sb = new StringBuilder();
        for (String prod : ValidatorConstants.layers) {
                if (existingProduct.contains(prod) ||
                        existingLayers.contains(prod) ||
                        newLayers.contains(prod)) {
                    sb.append(table.TRUE);
                } else {
                    sb.append(table.FALSE);
                }
        }
        Debug.trace("Existing prod/layer validation string: ");
        Debug.trace(sb.toString());
        return sb.toString();
    }

    private void setError(String string) {
		error = string;
	}

    private void setMessage(String string) {
        message = string;
    }

	public String getErrorMessageId() {
		// TODO Auto-generated method stub
		return error;
	}

	public String getWarningMessageId() {
		return error;
	}

	public boolean getDefaultAnswer() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public String getFormattedMessage() {
        return message;
    }

}
