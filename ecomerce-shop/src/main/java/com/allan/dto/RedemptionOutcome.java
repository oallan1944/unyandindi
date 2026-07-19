package com.allan.dto;

/**
 * Result of a successful {@code RedemptionService.redeem(...)} call.
 *
 * <p>Returned only on success — failures are signalled via
 * {@code CouponRedemptionException} (see {@code com.allan.exception}) rather
 * than a boolean/null field, so callers can't accidentally ignore a failure
 * by skipping a null-check.
 */
public record RedemptionOutcome(
        Long redemptionId,
        Long couponId,
        Long promotionId,
        long discountApplied,
        long cartTotalAtRedemption
) {
}