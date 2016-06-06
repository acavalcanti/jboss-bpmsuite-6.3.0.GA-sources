package org.jboss.brmsbpmsuite.patching.client;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * Configuration options needed for running the client patcher.
 */
public class ClientPatcherConfig {

    private File distributionRoot;
    private DistributionType distributionType;
    private TargetProduct product;
    private File patchBasedir;
    private File backupBaseDir;
    private List<DistributionType> supportedDistroTypes;
    private List<PatchingPhase> phasesToExecute;

    public static ClientPatcherConfig empty() {
        return new ClientPatcherConfig();
    }

    public ClientPatcherConfig() {
        phasesToExecute = Lists.newArrayList(PatchingPhase.values());
    }

    public File getDistributionRoot() {
        return distributionRoot;
    }

    public void setDistributionRoot(File distributionRoot) {
        this.distributionRoot = distributionRoot;
    }

    public DistributionType getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(DistributionType distributionType) {
        this.distributionType = distributionType;
    }

    public TargetProduct getProduct() {
        return product;
    }

    public void setProduct(TargetProduct product) {
        this.product = product;
    }

    public File getPatchBasedir() {
        return patchBasedir;
    }

    public void setPatchBasedir(File patchBasedir) {
        this.patchBasedir = patchBasedir;
    }

    public File getBackupBaseDir() {
        return backupBaseDir;
    }

    public void setBackupBaseDir(File backupBaseDir) {
        this.backupBaseDir = backupBaseDir;
    }

    public List<DistributionType> getSupportedDistroTypes() {
        return supportedDistroTypes;
    }

    public void setSupportedDistroTypes(
            List<DistributionType> supportedDistroTypes) {
        this.supportedDistroTypes = supportedDistroTypes;
    }

    public List<PatchingPhase> getPhasesToExecute() {
        return phasesToExecute;
    }

    public void setPhasesToExecute(List<PatchingPhase> phasesToExecute) {
        this.phasesToExecute = phasesToExecute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientPatcherConfig that = (ClientPatcherConfig) o;

        if (distributionRoot != null ? !distributionRoot.equals(that.distributionRoot) : that.distributionRoot != null)
            return false;
        if (distributionType != that.distributionType) return false;
        if (product != that.product) return false;
        if (patchBasedir != null ? !patchBasedir.equals(that.patchBasedir) : that.patchBasedir != null) return false;
        if (backupBaseDir != null ? !backupBaseDir.equals(that.backupBaseDir) : that.backupBaseDir != null) return false;
        if (supportedDistroTypes != null ? !supportedDistroTypes.equals(
                that.supportedDistroTypes) : that.supportedDistroTypes != null) return false;
        return !(phasesToExecute != null ? !phasesToExecute.equals(that.phasesToExecute) : that.phasesToExecute != null);

    }

    @Override
    public int hashCode() {
        int result = distributionRoot != null ? distributionRoot.hashCode() : 0;
        result = 31 * result + (distributionType != null ? distributionType.hashCode() : 0);
        result = 31 * result + (product != null ? product.hashCode() : 0);
        result = 31 * result + (patchBasedir != null ? patchBasedir.hashCode() : 0);
        result = 31 * result + (backupBaseDir != null ? backupBaseDir.hashCode() : 0);
        result = 31 * result + (supportedDistroTypes != null ? supportedDistroTypes.hashCode() : 0);
        result = 31 * result + (phasesToExecute != null ? phasesToExecute.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ClientPatcherConfig{" +
                "distributionRoot=" + distributionRoot +
                ", distributionType=" + distributionType +
                ", product=" + product +
                ", patchBasedir=" + patchBasedir +
                ", backupBaseDir=" + backupBaseDir +
                ", supportedDistroTypes=" + supportedDistroTypes +
                ", phasesToExecute=" + phasesToExecute +
                '}';
    }

    public void validate() {
        // we can't assume the distribution root is directory, because that is not the case for some of the distribution types
        // for example for individual WAS8 wars -- those are zipped files
        checkDistributionRootExists();
        checkDistributionTypeIsSupported();
    }

    private void checkDistributionRootExists() {
        if (!distributionRoot.exists()) {
            throw new InvalidDistributionRootException(
                    "Specified distribution root '" + distributionRoot + "' does not exist!");
        }
    }

    private void checkDistributionTypeIsSupported() {
        if (!supportedDistroTypes.contains(distributionType)) {
            throw new UnsupportedDistributionTypeException("Distribution type '" + distributionType + "' is not supported! " +
                    "This usually means that the type is known, but it is not supposed to be used for selected product.");
        }
    }

}
