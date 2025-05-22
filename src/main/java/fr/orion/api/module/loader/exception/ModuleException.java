package fr.orion.api.module.loader.exception;

/**
 *
 */
public class ModuleException extends RuntimeException {

    public ModuleException(String message) {
        super(message);
    }

    public ModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleException(Throwable cause) {
        super(cause);
    }
}
