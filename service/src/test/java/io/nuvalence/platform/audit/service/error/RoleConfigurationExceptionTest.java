package io.nuvalence.platform.audit.service.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RoleConfigurationExceptionTest {

    @Test
    void testConstructorAndGetMessage() {
        String errorMessage = "Test error message";
        RoleConfigurationException exception = new RoleConfigurationException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testDefaultConstructorAndGetMessage() {
        RoleConfigurationException exception = new RoleConfigurationException(null);

        // Since no message is provided, the message should be null
        assertNull(exception.getMessage());
    }
}
