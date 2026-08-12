package com.allan.exceptions;

import org.springframework.http.HttpStatus;

public class GoogleOnlyAccountException extends ApiException {
    public GoogleOnlyAccountException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}