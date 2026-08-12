package com.allan.exceptions;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends ApiException {
    public OtpExpiredException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}