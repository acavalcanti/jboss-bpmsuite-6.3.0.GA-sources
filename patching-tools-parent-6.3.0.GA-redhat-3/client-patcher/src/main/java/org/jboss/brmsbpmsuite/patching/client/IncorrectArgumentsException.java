package org.jboss.brmsbpmsuite.patching.client;

/**
 * Represents an exception which occurs when user specified incorrect arguments when running the
 * {@link ClientPatcherApp}
 */
public class IncorrectArgumentsException extends ClientPatcherException {

    public IncorrectArgumentsException() {
    }

    public IncorrectArgumentsException(String message) {
        super(message);
    }

    public IncorrectArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncorrectArgumentsException(Throwable cause) {
        super(cause);
    }
    
}
