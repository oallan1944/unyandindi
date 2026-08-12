package com.allan.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base type for exceptions that should be translated into a specific,
 * client-meaningful HTTP status by {@link GlobalExceptionHandler}, instead
 * of falling through to a generic 500.
 */
public abstract class ApiException extends Exception {

    private final HttpStatus status;

    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}