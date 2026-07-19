package com.allan.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound request body for recording a confirmed coupon redemption
 * after payment is confirmed.
 * Sent via POST /api/checkout/coupons/redeem (internal/checkout service only).
 *
 * <p>This is a <strong>write</strong> operation. The service will:
 * <ol>
 *   <li>Acquire the Redis distributed lock on the coupon code.</li>
 *   <li>Re-validate all eligibility conditions under the lock.</li>
 *   <li>Atomically increment {@code usedCount} on the coupon.</li>
 *   <li>Write an immutable {@link com.allan.model.CouponRedemption} record.</li>
 *   <li>Release the Redis lock.</li>
 * </ol>
 *
 * <p><strong>Accessor style:</strong> this is an ordinary Lombok
 * {@code @Getter} class, not a record — callers use {@code getOrderId()},
 * {@code getCouponCode()}, etc. (See
 * {@link com.allan.controller.CheckoutCouponController#redeem} for the
 * canonical call sites.)
 *
 * <p>Re-validation under lock is mandatory — the window between
 * {@link ValidateCouponRequest} (read-only) and this request (write)
 * can span several minutes. Another customer may have exhausted the coupon
 * in that time. The unique DB index on {@code coupon_redemptions.order_id}
 * is the final idempotency guard against double-redemption.
 *
 * <p>This endpoint is not exposed to the customer's frontend directly —
 * it is called internally by the checkout service after payment confirmation.
 */
@Getter
@Setter
@NoArgsConstructor
public class RedeemCouponRequest {

    @NotBlank(message = "Coupon code is required.")
    @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters.")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]*$",
             message = "Coupon code may only contain letters, numbers, hyphens, and underscores.")
    private String couponCode;

    /**
     * Order this redemption is attached to.
     * Unique index on {@code coupon_redemptions.order_id} prevents
     * double-redemption even if this endpoint is called twice for the same order.
     */
    @NotNull(message = "Order ID is required.")
    private Long orderId;

    /**
     * UGX discount amount actually applied to this order (whole shillings).
     * Computed by the promotion evaluator during checkout and passed here
     * for recording in the immutable redemption ledger.
     */
    @NotNull(message = "Discount amount is required.")
    @Min(value = 1, message = "Discount amount must be at least UGX 1.")
    private Long discountAmount;

    /**
     * Full cart total in UGX at the moment of redemption (whole shillings),
     * before the discount was applied.
     * Stored for analytics and fraud detection — never recalculated retroactively.
     */
    @NotNull(message = "Cart total at redemption is required.")
    @Min(value = 0, message = "Cart total must not be negative.")
    private Long cartTotalAtRedemption;
}