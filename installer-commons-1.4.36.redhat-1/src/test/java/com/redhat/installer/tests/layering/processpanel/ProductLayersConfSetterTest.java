package com.redhat.installer.tests.layering.processpanel;

import com.izforge.izpack.Pack;
import com.redhat.installer.framework.testers.ProcessPanelTester;
import com.redhat.installer.layering.constant.ValidatorConstants;
import com.redhat.installer.layering.processpanel.ProductLayersConfSetter;
import com.redhat.installer.tests.TestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
/**
 * Created by fcanas on 2/7/14.
 */
public class ProductLayersConfSetterTest extends ProcessPanelTester {

    String [] noArgs = {};

    @Before
    public void setUp() {
        // create required directories
        new File(tempFolder.getRoot(), "/bin/product.conf").getParentFile().mkdirs();
        new File(tempFolder.getRoot(), "/modules/layers.conf").getParentFile().mkdirs();
        idata.setVariable(ValidatorConstants.existingProduct, ValidatorConstants.eap); // only eap installed
        idata.setVariable(ValidatorConstants.existingLayers, ""); // fresh no layers
        TestUtils.createLayersConf(tempFolder,"");
        TestUtils.createProductConf(tempFolder,"eap");
    }


    private String getConfLine(String path) throws Exception {
        String ret= "";
        List<String> lines = FileUtils.readLines(new File(path));
        try {
            for (String line : lines) {
                if (line.startsWith("#") || line.startsWith("!")) {
                    continue;
                } else if (line.contains("=")) {
                    String [] values = line.split("=");
                    if (values.length > 1) {
                        ret = values[1];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private String getLayers() throws Exception{
        return getConfLine(new File(tempFolder.getRoot(),"/modules/layers.conf").getAbsolutePath());
    }

    private String getProduct() throws Exception{
        return getConfLine(new File(tempFolder.getRoot(),"/bin/product.conf").getAbsolutePath());
    }

    private void testSetter(String binaryWord, String expectedLayers, String [] selectedPacks) throws Exception {
        List<Pack> mockPacks = new ArrayList<Pack>();
        for (String id : selectedPacks) {
            Pack prodPack = new Pack("mock",id,"mock",null,null,true,true,false,"",false);
            mockPacks.add(prodPack);
        }
        idata.selectedPacks = mockPacks;
        idata.setVariable("layersWord",binaryWord);
        try {
            ProductLayersConfSetter.run(handler, noArgs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(expectedLayers, getLayers());
    }

    private void testProductSetter(String existingProduct, String newProduct, String expectedProduct) throws Exception {
        idata.setVariable(ValidatorConstants.existingProduct, existingProduct);
        List<Pack> mockPacks = new ArrayList<Pack>();
        Pack prodPack = new Pack("mock",newProduct,"mock",null,null,true,true,false,"",false);
        mockPacks.add(prodPack);
        idata.selectedPacks = mockPacks;
        ProductLayersConfSetter.run(handler, noArgs);
        assertEquals(expectedProduct, getProduct());
    }

    @Test
    public void testLayersSetter() throws Exception {
        String [] packs;
        idata.setVariable(ValidatorConstants.newProduct, ValidatorConstants.soa);
        // cases below are in the same order as appears in https://mojo.redhat.com/docs/DOC-179024 upon a fresh
        // load of the page

        // EAP only
        packs = new String [] {};
        testSetter("00000","", packs);

        // EAP + SOA
        packs = new String [] {"soa"};
        testSetter("10000","soa", packs);

        // EAP + DV
        testSetter("00001", "dv", packs);

        // EAP + BRMS
        testSetter("00010", "brms", packs);

        // EAP + BPMS
        testSetter("00100", "bpms", packs);

        // EAP + SOA + DV
        testSetter("10001", "soa,dv", packs);

        // EAP + SOA + BRMS
        testSetter("10010", "soa,brms", packs);

        // EAP + BRMS + DV
        testSetter("00011","brms,dv", packs);

        // EAP + SOA + BRMS + DV
        packs = new String [] {"soa"};
        testSetter("10011", "soa,brms,dv", packs);

        // EAP + SOA + BPMS
        testSetter("10100", "soa,bpms", packs);

        // EAP + SRAMP
        packs = new String [] {"sramp"};
        testSetter("01000", "sramp", packs);

        // EAP + SOA + SRAMP
        packs = new String [] {"soa","sramp"};
        testSetter("11000","soa,sramp", packs);

        // EAP + SOA + SRAMP + DV
        testSetter("11001","soa,sramp,dv", packs);

        // EAP + SOA + SRAMP + BRMS
        testSetter("11010","soa,sramp,brms", packs);

        // EAP + SOA + SRAMP + BRMS + DV
        testSetter("11011","soa,sramp,brms,dv", packs);

        // EAP + SOA + SRAMP + BPMS
        testSetter("11100","soa,sramp,bpms", packs);

        // EAP + SOA + SRAMP + BPMS + DV
        testSetter("11101","soa,sramp,bpms,dv", packs);

        // EAP + BRMS + SRAMP
        packs = new String [] {"sramp"};
        testSetter("01010", "sramp,brms", packs);

        // EAP + BPMS + SRAMP
        testSetter("01100", "sramp,bpms", packs);

        // EAP + BPMS + DV
        testSetter("00101", "bpms,dv", packs);

        // EAP + BRMS + SRAMP + DV
        packs = new String [] {"sramp"};
        testSetter("01011","sramp,brms,dv", packs);

        // EAP + BPMS + SRAMP + DV
        testSetter("01101", "sramp,bpms,dv", packs);

        // EAP + SOA + !SRAMP
        packs = new String [] {"soa"};
        testSetter("11000","soa",packs);

        // EAP + !SOA + SRAMP
        packs = new String [] {"sramp"};
        testSetter("11000","sramp",packs);

        // EAP + SOA + pre-existing SRAMP
        idata.setVariable(ValidatorConstants.existingLayers, "sramp");
        packs = new String [] {"soa"};
        testSetter("11000","soa,sramp",packs);

        // EAP + pre-existing soa + SRAMP
        idata.setVariable(ValidatorConstants.existingLayers, "soa");
        packs = new String [] {"sramp"};
        testSetter("11000","soa,sramp",packs);

        // EAP + pre-existing soa  + pre-existing sramp
        idata.setVariable(ValidatorConstants.existingLayers, "soa,sramp");
        packs = new String [] {""};
        testSetter("11000","soa,sramp",packs);

        // EAP + pre-existing soa + pre-existing sramp + BRMS
        idata.setVariable(ValidatorConstants.existingLayers, "soa,sramp");
        packs = new String [] {""};
        testSetter("11010","soa,sramp,brms",packs);

        // EAP + pre-existing soa + sramp + BPMS
        idata.setVariable(ValidatorConstants.existingLayers, "soa");
        packs = new String [] {"sramp"};
        testSetter("11100","soa,sramp,bpms",packs);

        // EAP + SOA + sramp + pre-existing BPMS
        idata.setVariable(ValidatorConstants.existingLayers, "bpms");
        packs = new String [] {"soa,","sramp"};
        testSetter("11100","soa,sramp,bpms",packs);

        // EAP + SOA + !sramp + pre-existing BPMS
        idata.setVariable(ValidatorConstants.existingLayers, "bpms");
        packs = new String [] {"soa,"};
        testSetter("11100","soa,bpms",packs);

        // EAP + !SOA + !sramp + pre-existing BPMS
        idata.setVariable(ValidatorConstants.existingLayers, "bpms");
        packs = new String [] {};
        testSetter("11100","bpms",packs);
    }

    // thinking of perhaps refactoring these into separate tests, so one can immediately see which case has
    // failed based on method name. expected vs actual on the assertion is pretty simple enough though, so at this time i think the (minimal) effort
    // isn't worth it (this applies to the layers test as well)
    @Test
    public void testProductSetter() throws Exception {
        idata.setVariable("layersWord", "11111"); // avoid NPE in ProductLayersConfSetter
        // EAP + SOA
        testProductSetter(ValidatorConstants.eap, ValidatorConstants.soa, ValidatorConstants.soa);

        // SOA + EAP
        testProductSetter(ValidatorConstants.soa, ValidatorConstants.eap, ValidatorConstants.soa);

        // EAP + BRMS
        testProductSetter(ValidatorConstants.eap, ValidatorConstants.brms, ValidatorConstants.brms);

        // EAP + BPMS
        testProductSetter(ValidatorConstants.eap, ValidatorConstants.bpms, ValidatorConstants.bpms);

        // EAP + SRAMP
        testProductSetter(ValidatorConstants.eap, ValidatorConstants.sramp, ValidatorConstants.sramp);

        // SOA + BRMS
        testProductSetter(ValidatorConstants.soa, ValidatorConstants.brms, ValidatorConstants.soa);

        // SOA + BPMS
        testProductSetter(ValidatorConstants.soa, ValidatorConstants.bpms, ValidatorConstants.soa);

        // SOA + SRAMP
        testProductSetter(ValidatorConstants.soa, ValidatorConstants.sramp, ValidatorConstants.soa);

        // BRMS + SOA
        testProductSetter(ValidatorConstants.brms, ValidatorConstants.soa, ValidatorConstants.soa);

        // BPMS + SOA
        testProductSetter(ValidatorConstants.bpms, ValidatorConstants.soa, ValidatorConstants.soa);

        // SRAMP + SOA
        testProductSetter(ValidatorConstants.sramp, ValidatorConstants.soa, ValidatorConstants.soa);

        // SRAMP + BRMS
        testProductSetter(ValidatorConstants.sramp, ValidatorConstants.brms, ValidatorConstants.brms);

        // SRAMP + BPMS
        testProductSetter(ValidatorConstants.sramp, ValidatorConstants.bpms, ValidatorConstants.bpms);

        // BRMS + SRAMP
        testProductSetter(ValidatorConstants.brms, ValidatorConstants.sramp, ValidatorConstants.brms);

        // BPMS + SRAMP
        testProductSetter(ValidatorConstants.bpms, ValidatorConstants.sramp, ValidatorConstants.bpms);
    }

    @Override
    public void testProcessPanelInstantiation() {

    }
}
