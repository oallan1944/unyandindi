package com.allan.exceptions;

/**
 * Thrown when a {@code Coupon} cannot be resolved for the caller — code
 * doesn't exist, or exists but belongs to a promotion owned by a different
 * seller. Deliberately not split into separate "not found" / "forbidden"
 * exceptions, for the same enumeration-prevention reason documented on
 * {@link PromotionNotFoundException}.
 */
public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(String code) {
        super("Coupon not found: " + code);
    }

    public CouponNotFoundException(Long couponId) {
        super("Coupon not found: " + couponId);
    }
}