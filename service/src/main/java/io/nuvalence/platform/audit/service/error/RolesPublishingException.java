package io.nuvalence.platform.audit.service.error;

/**
 * Custom runtime exception used for errors publishing roles.
 */
public class RolesPublishingException extends RuntimeException {

    private static final long serialVersionUID = 89190164423456789L;

    public RolesPublishingException(String message) {
        super(message);
    }
}
