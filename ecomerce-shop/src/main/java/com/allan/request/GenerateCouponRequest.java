package com.allan.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound request body for generating one or more coupons under
 * an existing promotion.
 * Sent via POST /api/admin/promotions/{id}/coupons or
 * POST /api/vendor/promotions/{id}/coupons.
 *
 * <p><strong>Two generation modes:</strong>
 * <ul>
 *   <li>Single vanity code — supply {@code code}, set {@code quantity = 1}.
 *       Used when the code appears in marketing material (e.g. "KAMPALA20").</li>
 *   <li>Bulk auto-generation — leave {@code code} null, set {@code quantity}
 *       2–500. The service generates cryptographically random UPPER CASE codes.</li>
 * </ul>
 * Supplying both a {@code code} and {@code quantity > 1} is rejected
 * by the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
public class GenerateCouponRequest {

    /**
     * Optional explicit code for single-code generation mode.
     * Normalized to UPPER CASE by the service on ingestion.
     * Null triggers bulk auto-generation.
     */
    @Size(min = 3, max = 50,
          message = "Coupon code must be between 3 and 50 characters.")
    @Pattern(regexp = "^[A-Za-z0-9\\-_]*$",
             message = "Coupon code may only contain letters, numbers, hyphens, and underscores.")
    private String code;

    /**
     * Number of coupons to generate. At least 1, maximum 500 per request.
     * Must be exactly 1 when {@code code} is supplied.
     */
    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    @Max(value = 500, message = "Maximum 500 coupons can be generated per request.")
    private Integer quantity;

    /**
     * Maximum total redemptions across all customers.
     * Null = unlimited (admin platform coupons only).
     * Vendors must always provide an explicit limit.
     */
    @Min(value = 1, message = "Usage limit must be at least 1 if specified.")
    private Integer usageLimit;

    /**
     * Maximum redemptions per individual customer.
     * Default 1 = one-per-customer.
     * Null = no per-customer cap (use with extreme caution).
     */
    @Min(value = 1, message = "Usage per customer must be at least 1 if specified.")
    private Integer usagePerUser = 1;
}