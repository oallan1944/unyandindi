package com.allan.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.allan.dto.RedemptionOutcome;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound response for a coupon redemption request.
 * Returned by POST /api/checkout/coupons/redeem.
 *
 * <p>{@link RedemptionOutcome} is only ever constructed on a successful
 * redemption — failures are signalled by {@code CouponRedemptionException}
 * and handled separately by
 * {@link com.allan.controller.CheckoutCouponController#handleRedemptionException},
 * which returns a {@link ValidateCouponResponse} instead. {@link #failure}
 * is kept here only so this class has a symmetrical failure path available
 * if a future caller needs to build a {@code RedemptionResponse} directly
 * without going through the exception handler.
 *
 * <p>All monetary values are UGX whole shillings ({@code long}).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RedemptionResponse {

    /** True if the coupon was successfully redeemed. */
    private boolean success;

    /**
     * Human-readable message for the checkout UI.
     * Success: "Coupon applied — UGX 10,000 off your order."
     * Failure: reason the redemption could not be completed.
     */
    private String message;

    /** ID of the immutable redemption ledger record. Null when success = false. */
    private Long redemptionId;

    /** Coupon that was redeemed. Null when success = false. */
    private Long couponId;

    /** Parent promotion ID. Null when success = false. */
    private Long promotionId;

    /** UGX discount applied (whole shillings). Null when success = false. */
    private Long discountAmount;

    /** Order total after discount, UGX whole shillings. Null when success = false. */
    private Long newOrderTotal;

    // ── Static factories ──────────────────────────────────────────────────────

    /** Builds the response from a successful {@link RedemptionOutcome}. */
    public static RedemptionResponse of(RedemptionOutcome outcome) {
        RedemptionResponse res = new RedemptionResponse();
        res.setSuccess(true);
        res.setRedemptionId(outcome.redemptionId());
        res.setCouponId(outcome.couponId());
        res.setPromotionId(outcome.promotionId());
        res.setDiscountAmount(outcome.discountApplied());
        res.setNewOrderTotal(outcome.cartTotalAtRedemption() - outcome.discountApplied());
        res.setMessage(String.format(
                "Coupon applied — UGX %,d off your order.", outcome.discountApplied()));
        return res;
    }

    public static RedemptionResponse failure(String reason) {
        RedemptionResponse res = new RedemptionResponse();
        res.setSuccess(false);
        res.setMessage(reason);
        return res;
    }
}