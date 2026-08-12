package com.allan.exceptions;

import org.springframework.http.HttpStatus;

public class TooManyAttemptsException extends ApiException {
    public TooManyAttemptsException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}