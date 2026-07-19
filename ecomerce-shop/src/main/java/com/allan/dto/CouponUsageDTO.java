package com.allan.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Internal DTO carrying coupon usage analytics from the mapper to the controller.
 * Returned on admin/vendor analytics endpoints and the coupon detail view.
 *
 * <p>Two levels of data:
 * <ul>
 *   <li>Aggregate stats — totals computed from all redemption records.</li>
 *   <li>Redemption timeline — individual {@link RedemptionSummary} records,
 *       most recent first. Paginate at the repository level for high-volume
 *       coupons — never load the full list unbounded.</li>
 * </ul>
 *
 * <p>All monetary fields are UGX whole shillings ({@code long}).
 * {@code effectiveDiscountRate} is the only {@code double} field — it is a
 * derived ratio (discount / cartTotal), not a monetary amount, so floating-point
 * is appropriate here.
 */
@Getter
@Setter
@NoArgsConstructor
public class CouponUsageDTO {

    private Long couponId;
    private String code;

    // ── Aggregate stats ───────────────────────────────────────────────────────

    /** Confirmed redemption count (excludes reversed). */
    private int usedCount;

    /** Configured maximum redemptions. Null = unlimited. */
    private Integer usageLimit;

    /** Remaining global uses. {@link Integer#MAX_VALUE} if unlimited. */
    private int remainingUses;

    /**
     * Total UGX discount granted across all non-reversed redemptions.
     * Whole shillings.
     */
    private long totalDiscountGrantedUgx;

    /** Number of redemptions reversed due to order cancellations. */
    private int reversalCount;

    /**
     * Total UGX refunded as a result of reversals.
     * Sum of {@code discount} on all reversed redemption records.
     */
    private long totalRefundedUgx;

    /** Timestamp of the most recent successful redemption. Null if never used. */
    private LocalDateTime lastRedeemedAt;

    // ── Redemption timeline ───────────────────────────────────────────────────

    /** Individual redemption summaries, most recent first. Paginated. */
    private List<RedemptionSummary> redemptions;

    // ── Nested: per-redemption summary ───────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RedemptionSummary {

        private Long redemptionId;
        private Long userId;
        private Long orderId;

        /** UGX discount on this specific redemption. Whole shillings. */
        private long discountUgx;

        /** Cart total at time of redemption. Whole shillings. */
        private long cartTotalAtRedemptionUgx;

        /**
         * Effective discount rate for this redemption.
         * = discountUgx / cartTotalAtRedemptionUgx.
         * double is correct here — this is a ratio, not a monetary amount.
         */
        private double effectiveDiscountRate;

        private boolean reversed;
        private LocalDateTime redeemedAt;
        private LocalDateTime reversedAt;
    }
}