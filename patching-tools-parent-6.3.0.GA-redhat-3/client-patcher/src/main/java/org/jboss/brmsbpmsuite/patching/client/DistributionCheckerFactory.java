package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DistributionCheckerFactory {

    /**
     * Creates specific distribution checker based on provided distribution type and target product.
     */
    public static DistributionChecker create(DistributionType distributionType, TargetProduct product) {
        List<ExpectedDistributionEntry> expectedEntries = getExpectedDistributionEntries(distributionType, product);
        switch (distributionType) {
            // in case there are specific checkers for other distributions, they should be created here

            // EAP distro can have two different roots, the EAP_HOME and also deployments dir.
            // The checker needs to handle both variants
            case EAP6X_BUNDLE:
                return new EAP6xBundleDistributionChecker(expectedEntries);

            // WAS8 distro contains zipped WARs instead of the exploaded ones
            case WAS8_BUNDLE:
                return new WAS8BundleDistributionChecker(product);

            case WAS8_BC:
                return new WAS8SingleWarDistributionChecker("business-central.war");

            case WAS8_KIE_SERVER:
                return new WAS8SingleWarDistributionChecker("kie-server.war");

            case WAS8_DASHBUILDER:
                return new WAS8SingleWarDistributionChecker("dashbuilder.war");

            default:
                return new ExpectedContentDistributionChecker(expectedEntries);
        }
    }

    private static List<ExpectedDistributionEntry> getExpectedDistributionEntries(DistributionType distributionType,
            TargetProduct product) {
        switch (distributionType) {
            // Container bundles
            case EAP6X_BUNDLE:
                ImmutableList.Builder<ExpectedDistributionEntry> eapListBuilder = new ImmutableList.Builder<ExpectedDistributionEntry>()
                        .addAll(addPathPrefix(eap6xBCEntries(product), "standalone/deployments/business-central.war/"))
                        .addAll(addPathPrefix(eap6xKieServerEntries(), "standalone/deployments/kie-server.war/"));
                // Dashbuilder is distributed only in BPM Suite
                if (product == TargetProduct.BPMSUITE) {
                    eapListBuilder.addAll(addPathPrefix(eap6xDashbuilderEntries(), "standalone/deployments/dashbuilder.war/"));
                }
                return eapListBuilder.build();

            case GENERIC_BUNDLE:
                ImmutableList.Builder<ExpectedDistributionEntry> genericListBuilder = new ImmutableList.Builder<ExpectedDistributionEntry>()
                        .addAll(addPathPrefix(genericBCEntries(product), "business-central.war/"))
                        .addAll(addPathPrefix(genericKieServerEntries(), "kie-server.war/"));
                // Dashbuilder is distributed only in BPM Suite
                if (product == TargetProduct.BPMSUITE) {
                    genericListBuilder.addAll(addPathPrefix(genericDashbuilderEntries(), "dashbuilder.war/"));
                }
                return genericListBuilder.build();

            case WAS8_BUNDLE:
                ImmutableList.Builder<ExpectedDistributionEntry> was8ListBuilder = new ImmutableList.Builder<ExpectedDistributionEntry>()
                        .addAll(addPathPrefix(was8BCEntries(product), "business-central.war/"))
                        .addAll(addPathPrefix(was8KieServerEntries(), "kie-server.war/"));
                // Dashbuilder is distributed only in BPM Suite
                if (product == TargetProduct.BPMSUITE) {
                    was8ListBuilder.addAll(addPathPrefix(was8DashbuilderEntries(), "dashbuilder.war/"));
                }
                return was8ListBuilder.build();

            case WLS12C_BUNDLE:
                ImmutableList.Builder<ExpectedDistributionEntry> wls12cListBuilder = new ImmutableList.Builder<ExpectedDistributionEntry>()
                        .addAll(addPathPrefix(wls12cBCEntries(product), "business-central.war/"))
                        .addAll(addPathPrefix(wls12cKieServerEntries(), "kie-server.war/"));
                // Dashbuilder is distributed only in BPM Suite
                if (product == TargetProduct.BPMSUITE) {
                    wls12cListBuilder.addAll(addPathPrefix(wls12cDashbuilderEntries(), "dashbuilder.war/"));
                }
                return wls12cListBuilder.build();

            // Business Central WARs
            case EAP6X_BC:
                return eap6xBCEntries(product);

            case GENERIC_BC:
                return genericBCEntries(product);

            case WAS8_BC:
                return was8BCEntries(product);

            case WLS12C_BC:
                return wls12cBCEntries(product);

            // Dashbuilder WARs
            case EAP6X_DASHBUILDER:
                return eap6xDashbuilderEntries();

            case GENERIC_DASHBUILDER:
                return genericDashbuilderEntries();

            case WAS8_DASHBUILDER:
                return was8DashbuilderEntries();

            case WLS12C_DASHBUILDER:
                return wls12cDashbuilderEntries();

            // KIE Server WARs
            case EAP6X_KIE_SERVER:
                return eap6xKieServerEntries();

            case GENERIC_KIE_SERVER:
                return genericKieServerEntries();

            case WAS8_KIE_SERVER:
                return was8KieServerEntries();

            case WLS12C_KIE_SERVER:
                return wls12cKieServerEntries();


            case BRMS_ENGINE:
            case BPMSUITE_ENGINE:
                return expectedEntriesList(
                        // TODO this may be too restrictive, what about flat classpath (no 'lib' dir there)?
                        new ExpectedExistingDir("lib")
                );

            case PLANNER_ENGINE:
                return expectedEntriesList(
                        new ExpectedExistingDir("binaries"),
                        new ExpectedExistingDir("sources"),
                        new ExpectedExistingFile("ReadMeOptaPlanner.txt")
                );

            case SUPPLEMENTARY_TOOLS:
                return expectedEntriesList(
                        new ExpectedExistingDir("kie-config-cli-dist"),
                        new ExpectedExistingDir("helix-core"),
                        new ExpectedExistingDir("zookeeper")
                );

            default:
                // this should not happen, the code above should handle all the distribution types
                throw new IllegalArgumentException("Unknown distribution type " + distributionType);
        }
    }

    private static List<ExpectedDistributionEntry> commonBCEntries(TargetProduct product) {
        ImmutableList.Builder<ExpectedDistributionEntry> listBuilder = new ImmutableList.Builder<ExpectedDistributionEntry>();
        listBuilder.add(new ExpectedExistingFile("WEB-INF/web.xml"));
        if (product == TargetProduct.BRMS) {
            listBuilder.add(new ExpectedManifestAttribute("Implementation-Title", "KIE Drools Workbench - Distribution Wars"));
        } else if (product == TargetProduct.BPMSUITE) {
            listBuilder.add(new ExpectedManifestAttribute("Implementation-Title", "KIE Workbench - Distribution Wars"));
        } else {
            throw new IllegalStateException(
                    "Product " + product + " can not be used to determine expected Business Central entries!");
        }
        return listBuilder.build();
    }

    private static List<ExpectedDistributionEntry> eap6xBCEntries(TargetProduct product) {
        return expectedEntriesList(
                commonBCEntries(product),
                new ExpectedExistingFile("WEB-INF/jboss-web.xml")
        );
    }

    private static List<ExpectedDistributionEntry> genericBCEntries(TargetProduct product) {
        return expectedEntriesList(
                commonBCEntries(product)
        );
    }

    private static List<ExpectedDistributionEntry> was8BCEntries(TargetProduct product) {
        return expectedEntriesList(
                commonBCEntries(product)
        );
    }

    private static List<ExpectedDistributionEntry> wls12cBCEntries(TargetProduct product) {
        return expectedEntriesList(
                commonBCEntries(product),
                new ExpectedExistingFile("WEB-INF/weblogic.xml")
        );
    }


    private static List<ExpectedDistributionEntry> commonDashbuilderEntries() {
        return expectedEntriesList(
                new ExpectedExistingFile("WEB-INF/web.xml"),
                new ExpectedManifestAttribute("Implementation-Title", "jBPM Dashboard Distributions Builder")
        );
    }

    private static List<ExpectedDistributionEntry> eap6xDashbuilderEntries() {
        return expectedEntriesList(
                commonDashbuilderEntries(),
                new ExpectedExistingFile("WEB-INF/jboss-web.xml")
        );
    }

    private static List<ExpectedDistributionEntry> genericDashbuilderEntries() {
        return expectedEntriesList(
                commonDashbuilderEntries(),
                new ExpectedExistingFile("META-INF/context.xml")
        );
    }

    private static List<ExpectedDistributionEntry> was8DashbuilderEntries() {
        return expectedEntriesList(
                commonDashbuilderEntries()
        );

    }

    private static List<ExpectedDistributionEntry> wls12cDashbuilderEntries() {
        return expectedEntriesList(
                commonDashbuilderEntries(),
                new ExpectedExistingFile("WEB-INF/weblogic.xml")
        );
    }

    private static List<ExpectedDistributionEntry> commonKieServerEntries() {
        return expectedEntriesList(
                new ExpectedExistingDir("WEB-INF"),
                new ExpectedExistingFile("WEB-INF/web.xml")
        );
    }

    private static List<ExpectedDistributionEntry> eap6xKieServerEntries() {
        return expectedEntriesList(
                commonKieServerEntries(),
                new ExpectedExistingFile("META-INF/kie-server-jms.xml")
        );
    }

    private static List<ExpectedDistributionEntry> genericKieServerEntries() {
        return expectedEntriesList(
                commonKieServerEntries()
        );
    }

    private static List<ExpectedDistributionEntry> was8KieServerEntries() {
        return expectedEntriesList(
                commonKieServerEntries(),
                new ExpectedExistingFile("META-INF/kie-server-jms.xml")
        );
    }

    private static List<ExpectedDistributionEntry> wls12cKieServerEntries() {
        return expectedEntriesList(
                commonKieServerEntries(),
                new ExpectedExistingFile("META-INF/kie-server-jms.xml")
        );
    }

    private static List<ExpectedDistributionEntry> addPathPrefix(List<ExpectedDistributionEntry> expectedEntries,
            final String pathPrefix) {
        return Lists.transform(expectedEntries, new Function<ExpectedDistributionEntry, ExpectedDistributionEntry>() {
            @Override
            public ExpectedDistributionEntry apply(ExpectedDistributionEntry entry) {
                return entry.withPath(pathPrefix + entry.getPath());
            }
        });
    }


    private static <T extends ExpectedDistributionEntry> List<ExpectedDistributionEntry> expectedEntriesList(
            List<ExpectedDistributionEntry> list, T... entries) {
        List<ExpectedDistributionEntry> result = new ArrayList<ExpectedDistributionEntry>(Arrays.asList(entries));
        result.addAll(list);
        return result;
    }

    private static <T extends ExpectedDistributionEntry> List<ExpectedDistributionEntry> expectedEntriesList(T... entries) {
        return new ArrayList<ExpectedDistributionEntry>(Arrays.asList(entries));
    }

}
