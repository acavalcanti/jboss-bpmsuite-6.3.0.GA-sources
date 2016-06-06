package org.jboss.brmsbpmsuite.patching.client;

public class ClientPatcherException extends RuntimeException {

    public ClientPatcherException() {
    }

    public ClientPatcherException(String message) {
        super(message);
    }

    public ClientPatcherException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientPatcherException(Throwable cause) {
        super(cause);
    }

}
