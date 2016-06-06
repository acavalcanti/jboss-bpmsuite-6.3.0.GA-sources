package org.jboss.brmsbpmsuite.patching.client;

public enum PatchingPhase {
    CHECK_DISTRO, BACKUP, APPLY, VERIFY, CLEAN_UP;

    public static PatchingPhase fromString(String phase) {
        if ("checkDistro".equals(phase)) {
            return CHECK_DISTRO;
        }
        if ("backup".equals(phase)) {
            return BACKUP;
        }
        if ("apply".equals(phase)) {
            return APPLY;
        }
        if ("verify".equals(phase)) {
            return VERIFY;
        }
        if ("cleanUp".equals(phase)) {
            return CLEAN_UP;
        }
        throw new IllegalArgumentException("Unrecognized phase '" + phase + "'!");
    }

}
