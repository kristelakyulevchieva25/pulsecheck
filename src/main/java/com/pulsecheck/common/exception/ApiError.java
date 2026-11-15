package com.pulsecheck.common.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldValidationError> fieldErrors
) {

    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, List.of());
    }

    public static ApiError withFieldErrors(HttpStatus status, String message, String path,
                                           List<FieldValidationError> fieldErrors) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, fieldErrors);
    }

    public record FieldValidationError(String field, String message) {
    }
}