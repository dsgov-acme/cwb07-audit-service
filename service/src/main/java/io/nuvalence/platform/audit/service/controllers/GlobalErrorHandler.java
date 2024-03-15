package io.nuvalence.platform.audit.service.controllers;

import io.nuvalence.platform.audit.service.config.exception.ProvidedDataException;
import io.nuvalence.platform.audit.service.error.ApiException;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Returns error response, if exception is thrown in the code.
 */
@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {

    private static final String OPEN_API_VALIDATION_ERROR_PATTERN =
            "^([a-zA-Z]+\\.)++([a-zA-Z]++):\\s(.++)$";

    private static final String MULTIPLE_STRING_VALIDATION_PATTERN =
            "^(.*?)must match \"\\^\\((.*?)\\)\\$\"";

    /**
     * Error response object.
     */
    @AllArgsConstructor
    @Getter
    public static class ErrorResponse {
        private List<String> messages;

        public ErrorResponse(String message) {
            this.messages = Collections.singletonList(message);
        }
    }

    /**
     * Return Bad request if ApiException is thrown in the code.
     * @param e exception
     * @return ResponseEntity with http status defined in the exception
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleException(ApiException e) {
        return ResponseEntity.status(e.getHttpStatus()).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Return a bad request if a ConstraintViolationException is thrown.
     * @param e ConstraintViolationException exception.
     * @return Bad request.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleException(ConstraintViolationException e) {
        String errorMessage =
                Arrays.stream(e.getMessage().split(", "))
                        .map(
                                message -> {
                                    if (message.matches(OPEN_API_VALIDATION_ERROR_PATTERN)) {
                                        return getOpenApiValidationMessage(message);
                                    } else {
                                        return message;
                                    }
                                })
                        .collect(Collectors.joining(", "));

        return ResponseEntity.status(400).body(new ErrorResponse(errorMessage));
    }

    /**
     * Return Bad request if MethodArgumentNotValidException is thrown in the code.
     * @param e exception
     * @return ResponseEntity for HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest()
                .body(
                        e.getFieldErrorCount() == 0
                                ? new ErrorResponse(e.getMessage())
                                : new ErrorResponse(
                                        e.getFieldErrors().stream()
                                                .map(
                                                        fieldError ->
                                                                String.format(
                                                                        "'%s': %s",
                                                                        fieldError.getField(),
                                                                        fieldError
                                                                                .getDefaultMessage()))
                                                .toList()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Handles all other exceptions.
     *
     * @param e Exception to capture.
     * @return Internal server error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("An unexpected error occurred: ", e);
        return ResponseEntity.internalServerError()
                .body(
                        new ErrorResponse(
                                "Internal server error. Please contact the system administrator."));
    }

    @ExceptionHandler(ProvidedDataException.class)
    public ResponseEntity<ErrorResponse> handleProvidedDataException(ProvidedDataException e) {
        log.warn("ProvidedDataException: ", e);
        return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
    }

    /**
     * Gives a friendly format to open api validation messages.
     * @param errorMessage message to format
     * @return formatted message
     */
    private String getOpenApiValidationMessage(String errorMessage) {
        String message = errorMessage.replaceAll(OPEN_API_VALIDATION_ERROR_PATTERN, "$2: $3");

        Pattern pattern = Pattern.compile(MULTIPLE_STRING_VALIDATION_PATTERN);
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String[] values = matcher.group(2).split("\\|");
            message =
                    message.replaceFirst(
                            "^.*must match \"\\^\\(.*\\)\\$\"$",
                            matcher.group(1) + "must be either " + Arrays.toString(values));
        }

        return message;
    }
}
