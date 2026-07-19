package com.allan.exceptions;

/**
 * Thrown when a {@code Promotion} cannot be resolved for the caller.
 *
 * <p><strong>Security:</strong> this exception is deliberately used both
 * when the promotion truly doesn't exist AND when it exists but belongs to
 * a different seller. Service methods must never throw a distinct "access
 * denied" exception for the ownership case — doing so would let a caller
 * enumerate other sellers' promotion IDs by observing whether they get a
 * 404 or a 403. Both map to HTTP 404 at the controller layer.
 */
public class PromotionNotFoundException extends RuntimeException {

    public PromotionNotFoundException(Long promotionId) {
        super("Promotion not found: " + promotionId);
    }

    public PromotionNotFoundException(String message) {
        super(message);
    }
}