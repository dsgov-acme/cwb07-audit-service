package io.nuvalence.platform.audit.service.error;

/**
 * Custom runtime exception used for errors parsing AuditEvent JSON.
 */
public class AuditEventParsingException extends RuntimeException {

    private static final long serialVersionUID = 4590164423456789L;

    public AuditEventParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
