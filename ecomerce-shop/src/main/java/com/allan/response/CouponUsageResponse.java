package com.allan.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound analytics response for coupon usage statistics.
 * Returned by GET /api/admin/promotions/{id}/coupons/{couponId}/usage.
 *
 * <p>Aggregate values ({@code totalDiscountGrantedUgx}, {@code reversalCount},
 * {@code totalRefundedUgx}) are computed by {@code CouponRedemptionRepository}
 * queries in the service layer and passed into the mapper — never derived
 * from the loaded {@code redemptions} collection, which would require loading
 * potentially thousands of records.
 *
 * <p><strong>UGX:</strong> all monetary fields are {@code long} whole shillings.
 * {@code effectiveDiscountRate} in {@link RedemptionSummary} is the only
 * {@code double} field — it is a derived ratio, not a monetary value.
 */
public record CouponUsageResponse(

        Long couponId,
        String code,

        // ── Aggregate stats ───────────────────────────────────────────────────

        /** Confirmed redemption count (excludes reversed). */
        int usedCount,

        /** Configured maximum redemptions. Null = unlimited. */
        Integer usageLimit,

        /** Remaining global uses. {@link Integer#MAX_VALUE} if unlimited. */
        int remainingUses,

        /**
         * Total UGX discount granted across all non-reversed redemptions.
         * Whole shillings — fetched via SUM query, not collection iteration.
         */
        long totalDiscountGrantedUgx,

        /** Number of redemptions reversed due to order cancellations. */
        int reversalCount,

        /**
         * Total UGX discount reversed (refunded) due to order cancellations.
         * Whole shillings.
         */
        long totalRefundedUgx,

        // ── Redemption timeline ───────────────────────────────────────────────

        /** Paginated list of individual redemptions, most recent first. */
        List<RedemptionSummary> redemptions

) {

    // ── Nested record: individual redemption summary ──────────────────────────

    /**
     * Summary of a single {@link com.allan.model.CouponRedemption} record.
     *
     * <p>{@code effectiveDiscountRate} = {@code discountUgx} /
     * {@code cartTotalAtRedemptionUgx}. {@code double} is correct here —
     * this is a ratio for analytics display, not a monetary value being
     * stored or used in arithmetic.
     */
    public record RedemptionSummary(

            Long redemptionId,
            Long UserId,
            Long orderId,

            /** UGX discount applied on this order. Whole shillings. */
            long discountUgx,

            /** Full cart total before discount. Whole shillings. */
            long cartTotalAtRedemptionUgx,

            /**
             * discountUgx / cartTotalAtRedemptionUgx.
             * 0.0 when cartTotalAtRedemptionUgx is 0 (guard against division by zero).
             */
            double effectiveDiscountRate,

            boolean reversed,
            LocalDateTime redeemedAt,

            /** Null when not yet reversed. */
            LocalDateTime reversedAt
    ) {}
}