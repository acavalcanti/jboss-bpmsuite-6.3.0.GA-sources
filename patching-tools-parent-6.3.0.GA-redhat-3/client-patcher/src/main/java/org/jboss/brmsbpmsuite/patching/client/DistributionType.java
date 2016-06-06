package org.jboss.brmsbpmsuite.patching.client;

import java.util.ArrayList;
import java.util.List;

public enum DistributionType {

    EAP6X_BUNDLE("eap6.x"),
    EAP6X_BC("eap6.x-bc", "eap6.x"),
    EAP6X_DASHBUILDER("eap6.x-dashbuilder", "eap6.x", TargetProduct.BPMSUITE),
    EAP6X_KIE_SERVER("eap6.x-kie-server", "eap6.x"),

    GENERIC_BUNDLE("generic"),
    GENERIC_BC("generic-bc", "generic"),
    GENERIC_DASHBUILDER("generic-dashbuilder", "generic", TargetProduct.BPMSUITE),
    GENERIC_KIE_SERVER("generic-kie-server", "generic"),

    WAS8_BUNDLE("was8"),
    WAS8_BC("was8-bc", "was8"),
    WAS8_DASHBUILDER("was8-dashbuilder", "was8", TargetProduct.BPMSUITE),
    WAS8_KIE_SERVER("was8-kie-server", "was8"),

    WLS12C_BUNDLE("wls12c"),
    WLS12C_BC("wls12c-bc", "wls12c"),
    WLS12C_DASHBUILDER("wls12c-dashbuilder", "wls12c", TargetProduct.BPMSUITE),
    WLS12C_KIE_SERVER("wls12c-kie-server", "wls12c"),

    BRMS_ENGINE("brms-engine", TargetProduct.BRMS),
    BPMSUITE_ENGINE("bpmsuite-engine", TargetProduct.BPMSUITE),
    PLANNER_ENGINE("planner-engine"),
    SUPPLEMENTARY_TOOLS("supplementary-tools");

    private final String name;
    private final String relativePath;
    private final TargetProduct target;

    private DistributionType(String nameAndRelativePath) {
        this(nameAndRelativePath, nameAndRelativePath);
    }

    private DistributionType(String name, String relativePath) {
        this(name, relativePath, TargetProduct.BRMS_AND_BPMSUITE);
    }

    private DistributionType(String name, String relativePath, TargetProduct target) {
        this.name = name;
        this.relativePath = relativePath;
        this.target = target;
    }

    private DistributionType(String nameAndRelativePath, TargetProduct target) {
        this(nameAndRelativePath, nameAndRelativePath, target);
    }

    public String getName() {
        return name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public TargetProduct getTarget() {
        return target;
    }

    public static final DistributionType fromString(String name) {
        for (DistributionType type : DistributionType.values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unrecognized distribution type '" + name + "'! ");
    }

    public static List<DistributionType> getBRMSDistributionTypes() {
        List<DistributionType> result = new ArrayList<DistributionType>();
        for (DistributionType type : DistributionType.values()) {
            if (TargetProduct.BRMS.equals(type.getTarget()) || TargetProduct.BRMS_AND_BPMSUITE.equals(type.getTarget())) {
                result.add(type);
            }
        }
        return result;
    }

    public static List<DistributionType> getBPMSuiteDistributionTypes() {
        List<DistributionType> result = new ArrayList<DistributionType>();
        for (DistributionType type : DistributionType.values()) {
            if (TargetProduct.BPMSUITE.equals(type.getTarget()) || TargetProduct.BRMS_AND_BPMSUITE.equals(
                    type.getTarget())) {
                result.add(type);
            }
        }
        return result;
    }

}
