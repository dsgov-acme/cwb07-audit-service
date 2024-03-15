package io.nuvalence.platform.audit.service.error;

/**
 * Custom runtime exception used for role configuration errors.
 */
public class RoleConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1290123456789L;

    public RoleConfigurationException(String message) {
        super(message);
    }
}
