package com.allan.dto;

/**
 * Inbound payload for issuing one {@code Coupon} (or as a template for bulk
 * generation).
 *
 * <p><strong>Security:</strong> no {@code usedCount} or {@code status}
 * field — those are service-owned and must never be set from client input.
 * {@code code} is optional: leave it {@code null} to have
 * {@code CouponService} generate a cryptographically random code (bulk
 * generation always ignores any supplied {@code code} and generates fresh
 * ones per unit).
 */
public record CouponCreateRequest(
        String code,
        Integer usageLimit,
        Integer usagePerUser
) {
}