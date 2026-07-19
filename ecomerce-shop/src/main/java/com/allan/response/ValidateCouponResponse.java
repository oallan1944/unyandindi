package com.allan.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound response for a coupon validation request.
 * Returned by POST /api/checkout/coupons/validate.
 *
 * <p>Always HTTP 200 regardless of whether the coupon is valid.
 * The {@link #valid} flag tells the client the outcome — using 4xx
 * for an invalid coupon would force the frontend to handle a normal
 * checkout flow state as an error.
 *
 * <p>{@code @JsonInclude(NON_NULL)} strips null fields from JSON:
 * <ul>
 *   <li>Success: all fields populated.</li>
 *   <li>Failure: only {@code valid=false} and {@code message} returned.
 *       Monetary and promotion fields are null and omitted — the client
 *       cannot infer internal promotion details from a failed response.</li>
 * </ul>
 *
 * <p>All monetary values are UGX whole shillings ({@code long}).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateCouponResponse {

    /** True if coupon is valid and discount has been computed. */
    private boolean valid;

    /**
     * Human-readable message for the checkout UI.
     * Success: "Coupon applied — UGX 10,000 off your order."
     * Failure: "This coupon has reached its usage limit."
     */
    private String message;

    /**
     * UGX discount to deduct from the cart total (whole shillings).
     * Null when valid = false.
     */
    private Long discountAmount;

    /**
     * Cart total after discount in UGX (whole shillings).
     * Null when valid = false.
     */
    private Long newCartTotal;

    /** Parent promotion ID. Null when valid = false. */
    private Long promotionId;

    /**
     * Display label of the reward applied.
     * Example: "20% off your order" / "Free shipping applied."
     * Null when valid = false.
     */
    private String rewardLabel;

    // ── Static factories ──────────────────────────────────────────────────────

    public static ValidateCouponResponse success(
            long discountAmount,
            long newCartTotal,
            Long promotionId,
            String rewardLabel) {

        ValidateCouponResponse res = new ValidateCouponResponse();
        res.setValid(true);
        res.setDiscountAmount(discountAmount);
        res.setNewCartTotal(newCartTotal);
        res.setPromotionId(promotionId);
        res.setRewardLabel(rewardLabel);
        res.setMessage(String.format(
                "Coupon applied — UGX %,d off your order.", discountAmount));
        return res;
    }

    public static ValidateCouponResponse failure(String reason) {
        ValidateCouponResponse res = new ValidateCouponResponse();
        res.setValid(false);
        res.setMessage(reason);
        return res;
    }
}