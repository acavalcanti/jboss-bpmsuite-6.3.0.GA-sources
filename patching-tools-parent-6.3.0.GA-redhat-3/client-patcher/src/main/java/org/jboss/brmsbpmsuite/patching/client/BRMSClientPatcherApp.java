package org.jboss.brmsbpmsuite.patching.client;

public class BRMSClientPatcherApp extends ClientPatcherApp {

    public static void main(String[] args) {
        commonMain(TargetProduct.BRMS, DistributionType.getBRMSDistributionTypes(), args);
    }

}
