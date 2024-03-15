package io.nuvalence.platform.audit.service.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class GlobalErrorHandlerTest {

    private GlobalErrorHandler globalErrorHandler;

    @BeforeEach
    public void setUp() {
        globalErrorHandler = new GlobalErrorHandler();
    }

    @Test
    @SuppressWarnings("PMD")
    void testGetOpenApiValidationMessage()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String inputMessage = "User.username: must match \"^(ABC|DEF)$\"";
        String expectedOutputMessage = "username: must be either [ABC, DEF]";

        Method method =
                GlobalErrorHandler.class.getDeclaredMethod(
                        "getOpenApiValidationMessage", String.class);
        method.setAccessible(true);

        String outputMessage = (String) method.invoke(globalErrorHandler, inputMessage);

        assertEquals(expectedOutputMessage, outputMessage);
    }

    @Test
    void testHandleExceptionConstraintViolationException() {
        String inputMessage = "User.username: must match \"^(ABC|DEF)$\"";
        ConstraintViolationException mockedException =
                Mockito.mock(ConstraintViolationException.class);

        Mockito.when(mockedException.getMessage()).thenReturn(inputMessage);

        ResponseEntity<GlobalErrorHandler.ErrorResponse> response =
                globalErrorHandler.handleException(mockedException);

        String expectedOutputMessage = "username: must be either [ABC, DEF]";

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().getMessages().size());
        assertEquals(expectedOutputMessage, response.getBody().getMessages().get(0));
    }
}
