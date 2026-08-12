package com.allan.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends ApiException {
    public InvalidOtpException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}