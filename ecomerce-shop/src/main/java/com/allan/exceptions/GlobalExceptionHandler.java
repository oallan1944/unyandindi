package com.allan.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.allan.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(ApiException ex) {
        ApiResponse res = new ApiResponse();
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(res);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
        ApiResponse res = new ApiResponse();
        res.setMessage("Invalid credentials.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // Handles @Valid failures on @RequestBody DTOs (e.g. CompleteProfileRequest).
    // Without this, MethodArgumentNotValidException fell through to the
    // catch-all Exception handler below — wrong status (500 instead of
    // 400), a generic message instead of the actual field error, and
    // routine user-input mistakes logged as server errors.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .filter(Objects::nonNull)
                .collect(Collectors.joining("; "));

        log.debug("Validation failed: {}", message);

        ApiResponse res = new ApiResponse();
        res.setMessage(message.isBlank() ? "Invalid request." : message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // Catch-all — logs the real exception server-side but never leaks
    // internals (stack traces, SQL, class names) to the client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ApiResponse res = new ApiResponse();
        res.setMessage("An unexpected error occurred. Please try again.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }
}