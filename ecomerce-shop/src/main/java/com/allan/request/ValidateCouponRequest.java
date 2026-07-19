package com.allan.request;

import com.allan.dto.CartLineRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Inbound request body for validating a coupon code at checkout.
 * Sent via POST /api/checkout/coupons/validate.
 *
 * <p>This is a <strong>read-only</strong> operation — no records are written,
 * no {@code usedCount} is incremented. It checks all eligibility conditions
 * and returns the computed UGX discount so the cart preview can be shown
 * before the customer commits to payment.
 *
 * <p>{@code items} carries client-supplied {@code productId}/{@code quantity}
 * pairs only — {@link com.allan.service.support.CartContextBuilder} rebuilds
 * price/seller/category from the database for each line. See
 * {@link CartLineRequest}.
 *
 * <p>The actual redemption record is written only after payment confirmation
 * via {@link RedeemCouponRequest}.
 */
public record ValidateCouponRequest(

        @NotBlank(message = "Coupon code is required.")
        @Size(min = 3, max = 50, message = "Coupon code must be between 3 and 50 characters.")
        @Pattern(regexp = "^[A-Za-z0-9\\-_]*$",
                 message = "Coupon code may only contain letters, numbers, hyphens, and underscores.")
        String couponCode,

        @NotEmpty(message = "Cart items are required.")
        @Valid
        List<CartLineRequest> items

) {}