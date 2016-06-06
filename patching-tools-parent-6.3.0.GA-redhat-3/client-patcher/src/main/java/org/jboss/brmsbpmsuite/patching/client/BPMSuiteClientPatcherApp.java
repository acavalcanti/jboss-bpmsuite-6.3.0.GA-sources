package org.jboss.brmsbpmsuite.patching.client;

public class BPMSuiteClientPatcherApp extends ClientPatcherApp {

    public static void main(String[] args) {
        commonMain(TargetProduct.BPMSUITE, DistributionType.getBPMSuiteDistributionTypes(), args);
    }
}
