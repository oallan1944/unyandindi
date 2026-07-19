package com.allan.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Inbound request body for simulating a promotion's discount against
 * a hypothetical cart — without persisting any state.
 * Sent via POST /api/admin/promotions/{id}/preview.
 *
 * <p>Allows admins to verify that configured rules and rewards produce
 * the expected UGX discount before publishing a DRAFT promotion.
 * Works against any promotion status including DRAFT.
 *
 * <p>No DB writes occur, no Redis lock is acquired, no {@code usedCount}
 * is touched. The service constructs a virtual cart from the supplied
 * {@link CartItemPreview} list, runs the full rule engine, computes the
 * discount, and returns a preview response.
 *
 * <p>Admin-only endpoint — {@code @PreAuthorize("hasRole('ADMIN')")}.
 */
@Getter
@Setter
@NoArgsConstructor
public class PromotionPreviewRequest {

    /**
     * Optional user ID to simulate against.
     * Used by UserSegmentRule and FirstOrderOnlyRule to inspect order history.
     * Null = simulate as a new anonymous customer with zero order history.
     */
    private Long simulatedUserId;

    @NotEmpty(message = "At least one cart item is required for preview.")
    @Valid
    private List<CartItemPreview> cartItems = new ArrayList<>();

    // ── Nested: simulated cart item ───────────────────────────────────────────

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CartItemPreview {

        @NotNull(message = "Product ID is required.")
        private Long productId;

        /** Required for CategoryRule evaluation. */
        @NotNull(message = "Category ID is required.")
        private Long categoryId;

        /** Required for VendorProductsRule evaluation. */
        @NotNull(message = "Seller ID is required.")
        private Long sellerId;

        @Min(value = 1, message = "Quantity must be at least 1.")
        private int quantity;

        /**
         * Selling price per unit in UGX whole shillings.
         * Used to compute simulated cart total for MinOrderValueRule
         * and discount calculation.
         */
        @Min(value = 0, message = "Selling price must not be negative.")
        private long sellingPriceUgx;

        /**
         * MRP per unit in UGX whole shillings.
         * Used for display in the preview response only.
         */
        @Min(value = 0, message = "MRP must not be negative.")
        private long mrpPriceUgx;
    }
}