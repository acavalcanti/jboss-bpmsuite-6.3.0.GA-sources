package com.redhat.installer.tests.layering.validator;

import com.redhat.installer.tests.TestUtils;
import com.redhat.installer.framework.testers.DataValidatorTester;
import com.redhat.installer.layering.constant.ValidatorConstants;
import com.redhat.installer.layering.validator.IsSupportedPlatformValidator;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
/**
 * Created by thauser on 2/7/14.
 */
public class IsSupportedPlatformValidatorTest extends DataValidatorTester
{

    @Before
    public void setUp() throws Exception {
        idata.langpack = TestUtils.createMockLangpack(tempFolder,"IsSupportedPlatformValidator.incompatibleProduct","IsSupportedPlatformValidator.incompatibleSRAMP");
        dv = new IsSupportedPlatformValidator();
    }


    @Test
    public void testFswOnEap() throws Exception{
        setProductToEap();
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusOk();
    }

    @Test
    public void testDvOnEap() throws Exception {
        setProductToEap();
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    /**
     * Installing sramp on top of soa using the
     * fsw installer should make the soa packages
     * disabled.
     */
    @Test
    public void testDisabledPackageSrampOnSoa() {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soa);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soaSramp);
        assertStatusWarning(); // warns user
        assertTrue(idata.getVariable("can.install.fsw").contains("false"));
        assertTrue(idata.getVariable("can.install.sramp").contains("true"));
    }

    /**
     * Installing fsw on top of an sramp installation
     * using fsw installer should produce a warning and
     * disable the sramp package.
     * @throws Exception
     */
    @Test
    public void testDisabledPackageSoaOnSramp() {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.sramp);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soaSramp);
        assertStatusWarning();
        assertTrue(idata.getVariable("can.install.sramp").contains("false"));
        assertTrue(idata.getVariable("can.install.fsw").contains("true"));
    }

    @Test
    public void testBrmsOnEap() throws Exception {
        setProductToEap();
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusOk();
    }

    @Test
    public void testBpmsOnEap() throws Exception {
        setProductToEap();
        setNewProduct(ValidatorConstants.bpms);
        setNewLayers(ValidatorConstants.bpms);
        assertStatusOk();
    }

    @Test
    public void testSrampOnEap() throws Exception {
        setProductToEap();
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testFswSrampOnEap() throws Exception {
        setProductToEap();
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soaSramp);
        assertStatusOk();
    }

    @Test
    public void testDvOnFsw() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soa);
        setNewProduct(ValidatorConstants.eap);
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    @Test
    public void testFswOnDv() throws Exception {
        setProductToEap();
        setExistingLayers(ValidatorConstants.dv);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusOk();
    }

    @Test
    public void testBrmsOnFsw() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soa);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusError();
    }

    @Test
    public void testFswOnBrms() throws Exception {
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.brms);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusError();
    }

    @Test
    public void testBrmsOnDv() throws Exception {
        setExistingProduct(ValidatorConstants.dv);
        setExistingLayers(ValidatorConstants.dv);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusOk();
    }

    @Test
    public void testDvOnBrms() throws Exception{
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.brms);
        setNewProduct(ValidatorConstants.dv);
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    @Test
    public void testFswOnDvBrms() throws Exception {
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.brmsEds);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusError();
    }

    @Test
    public void testDvOnFswBrms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaBrms);
        setNewProduct(ValidatorConstants.eap);
        setNewLayers(ValidatorConstants.dv);
        assertStatusError();
    }

    @Test
    public void testBrmsOnFswDv() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaEds);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusError();
    }

    @Test
    public void testBpmsOnFsw() throws Exception {
        setExistingProduct(ValidatorConstants.eap);
        setExistingLayers(ValidatorConstants.eap);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa+","+ValidatorConstants.sramp+","+ValidatorConstants.bpms);
        assertStatusOk();
    }

    @Test
    public void testFswOnBpms() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.bpms);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusError();
    }

    @Test
    public void testSrampOnFsw() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soa);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testFswOnSramp() throws Exception {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.sramp);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusOk();
    }

    @Test
    public void testDvOnFswSramp() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSramp);
        setNewProduct(ValidatorConstants.dv);
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    @Test
    public void testSrampOnFswDv() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaEds);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    // problems with this test?
    @Test
    public void testFswOnSrampDv() throws Exception {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.srampEds);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusOk();
    }

    @Test
    public void testBrmsOnFswSramp() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSramp);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusError();
    }

    @Test
    public void testFswOnBrmsSramp() throws Exception {
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.srampBrms);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusError();
    }

    @Test
    public void testSrampOnFswBrms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaBrms);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusError();
    }

    @Test
    public void testBrmsOnFswSrampDv() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSrampEds);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusError();
    }

    @Test
    public void testFswOnSrampDvBrms() throws Exception{
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.srampBrmsEds);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusError();
    }

    @Test
    public void testSrampOnFswDvBrms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaBrmsEds);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusError();
    }

    @Test
    public void testDvOnFswSrampBrms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSrampBrms);
        setNewProduct(ValidatorConstants.dv);
        setNewLayers(ValidatorConstants.dv);
        assertStatusError();
    }

    @Test
    public void testBpmsOnFswSramp() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSramp);
        setNewProduct(ValidatorConstants.bpms);
        setNewLayers(ValidatorConstants.bpms);
        assertStatusOk();
    }

    @Test
    public void testFswOnBpmsSramp() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.srampBpms);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusOk();
    }

    @Test
    public void testSrampOnFswBpms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaBpms);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testBpmsOnFswSrampDv() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSrampEds);
        setNewProduct(ValidatorConstants.bpms);
        setNewLayers(ValidatorConstants.bpms);
        assertStatusError();
    }

    @Test
    public void testFswOnSrampDvBpms() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.srampBpmsEds);
        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusError();
    }

    @Test
    public void testSrampOnFswDvBpms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaBpmsEds);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusError();
    }

    @Test
    public void testDvOnFswSrampBpms() throws Exception {
        setExistingProduct(ValidatorConstants.soa);
        setExistingLayers(ValidatorConstants.soaSrampBpms);
        setNewProduct(ValidatorConstants.dv);
        setNewLayers(ValidatorConstants.dv);
        assertStatusError();
    }

    @Test
    public void testBrmsOnSramp() throws Exception {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.sramp);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusOk();
    }

    @Test
    public void testSrampOnBrms() throws Exception {
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.brms);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testBpmsOnSramp() throws Exception {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.sramp);
        setNewProduct(ValidatorConstants.bpms);
        setNewLayers(ValidatorConstants.bpms);
        assertStatusOk();
    }

    @Test
    public void testSrampOnBpms() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.bpms);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testBpmsOnDv() throws Exception {
        setExistingProduct(ValidatorConstants.eap);
        setExistingLayers(ValidatorConstants.dv);
        setNewProduct(ValidatorConstants.bpms);
        setNewLayers(ValidatorConstants.bpms);
        assertStatusOk();
    }

    @Test
    public void testDvOnBpms() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.bpms);
        setNewProduct(ValidatorConstants.eap);
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    @Test
    public void testBrmsOnSrampDv() throws Exception {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.srampEds);
        setNewProduct(ValidatorConstants.brms);
        setNewLayers(ValidatorConstants.brms);
        assertStatusOk();
    }

    @Test
    public void testSrampOnBrmsDv() throws Exception {
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.brmsEds);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testDvOnSrampBrms() throws Exception {
        setExistingProduct(ValidatorConstants.brms);
        setExistingLayers(ValidatorConstants.srampBrms);
        setNewProduct(ValidatorConstants.dv);
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    @Test
    public void testBpmsOnSrampDv() throws Exception {
        setExistingProduct(ValidatorConstants.sramp);
        setExistingLayers(ValidatorConstants.srampEds);
        setNewProduct(ValidatorConstants.bpms);
        setNewLayers(ValidatorConstants.bpms);
        assertStatusOk();
    }

    @Test
    public void testSrampOnBpmsDv() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.bpmsEds);
        setNewProduct(ValidatorConstants.sramp);
        setNewLayers(ValidatorConstants.sramp);
        assertStatusOk();
    }

    @Test
    public void testDvOnSrampBpms() throws Exception {
        setExistingProduct(ValidatorConstants.bpms);
        setExistingLayers(ValidatorConstants.srampBpms);
        setNewProduct(ValidatorConstants.dv);
        setNewLayers(ValidatorConstants.dv);
        assertStatusOk();
    }

    @Test
    public void testFswSrampOnDv() throws Exception {
        setExistingProduct(ValidatorConstants.eap);
        setExistingLayers(ValidatorConstants.dv);

        setNewProduct(ValidatorConstants.soa);
        setNewLayers(ValidatorConstants.soa);
        assertStatusOk();
    }

    private void setProductToEap() {
        setExistingProduct(ValidatorConstants.eap);
    }

    private void setExistingProduct(String product) {
        idata.setVariable(ValidatorConstants.existingProduct, product);
    }

    private void setExistingLayers(String layers){
        idata.setVariable(ValidatorConstants.existingLayers, layers);
    }

    private void setNewProduct(String product){
        idata.setVariable(ValidatorConstants.newProduct, product);
    }

    private void setNewLayers(String layers){
        idata.setVariable(ValidatorConstants.newLayers, layers);
    }



}
