package com.allan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * A single client-supplied cart line for pre-order coupon validation.
 *
 * <p><strong>Security:</strong> only {@code productId} and {@code quantity}
 * are ever accepted from the client here. {@link CartContextBuilder} uses
 * {@code productId} solely to look up the authoritative {@link com.allan.model.Product}
 * from the database — price, seller, and category are never taken from the
 * client. See {@link CartItem} for why that matters.
 */
public record CartLineRequest(

        @NotNull(message = "Product ID is required.")
        Long productId,

        @Min(value = 1, message = "Quantity must be at least 1.")
        int quantity

) {}