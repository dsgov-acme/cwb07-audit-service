package io.nuvalence.platform.audit.service.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RolesPublishingExceptionTest {

    @Test
    void testConstructorAndGetMessage() {
        String errorMessage = "Test error message";
        RolesPublishingException exception = new RolesPublishingException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testDefaultConstructorAndGetMessage() {
        RolesPublishingException exception = new RolesPublishingException(null);

        // Since no message is provided, the message should be null
        assertNull(exception.getMessage());
    }
}
