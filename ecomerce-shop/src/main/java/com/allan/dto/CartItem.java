package com.allan.dto;

/**
 * A single cart line item, as reconstructed from authoritative server-side
 * product/order data.
 *
 * <p><strong>Security:</strong> every field here MUST be populated from the
 * database (current {@code Product} price, seller, category) at the moment
 * of evaluation — never taken verbatim from client-submitted cart JSON.
 * A client that could supply its own {@code unitPrice} or {@code sellerId}
 * could trivially forge eligibility for a promotion it doesn't qualify for,
 * or shift which vendor's items look eligible for a VENDOR_SPECIFIC
 * discount. Only {@code productId} and {@code quantity} should ever
 * originate from the client; everything else is a server-side lookup.
 */
public record CartItem(
        Long productId,
        Long sellerId,
        Long categoryId,
        int quantity,
        long unitPrice,
        long lineTotal
) {
    public CartItem {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice < 0 || lineTotal < 0) {
            throw new IllegalArgumentException("monetary fields cannot be negative");
        }
    }
}