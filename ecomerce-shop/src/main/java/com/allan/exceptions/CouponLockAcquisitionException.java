package com.allan.exceptions;

/**
 * Thrown when a Redis distributed lock on a coupon code cannot be acquired
 * within the configured wait window — either because another concurrent
 * redemption already holds it, or the acquisition attempt was interrupted.
 *
 * <p>Callers (typically {@code RedemptionServiceImpl}) should let this
 * propagate as a fast, typed failure rather than blocking the request
 * thread — this is a checkout-path lock, not a work queue.
 */
public class CouponLockAcquisitionException extends RuntimeException {

    private final String couponCode;

    public CouponLockAcquisitionException(String couponCode) {
        super("Could not acquire redemption lock for coupon: " + couponCode);
        this.couponCode = couponCode;
    }

    public CouponLockAcquisitionException(String couponCode, Throwable cause) {
        super("Interrupted while acquiring redemption lock for coupon: " + couponCode, cause);
        this.couponCode = couponCode;
    }

    public String getCouponCode() {
        return couponCode;
    }
}