package com.allan.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The full server-trusted context a promotion is evaluated against.
 *
 * <p><strong>Security:</strong>
 * <ul>
 *   <li>{@code userId} must come from the authenticated principal
 *       (SecurityContext / JWT subject), never from a request body field —
 *       otherwise a caller could evaluate or redeem coupons "as" another
 *       user, defeating per-user usage limits entirely.</li>
 *   <li>{@code items} and {@code subtotal} must be rebuilt server-side at
 *       redemption time even if a {@code CartContext} was already built
 *       once for a preview — prices, stock, or promotion state can change
 *       between "show me the discount" and "place the order" (TOCTOU).
 *       {@code RedemptionService} must never reuse a caller-supplied
 *       {@code CartContext} object as-is for the final discount amount.</li>
 *   <li>{@code evaluatedAt} defaults to "now" but is an explicit field
 *       (rather than each service calling {@code LocalDateTime.now()}
 *       independently) so a single evaluation pass is internally
 *       consistent and reproducible in tests/audits.</li>
 * </ul>
 */
public record CartContext(
        Long userId,
        List<CartItem> items,
        long subtotal,
        String couponCode,
        LocalDateTime evaluatedAt
) {
    public CartContext {
        items = items == null ? List.of() : List.copyOf(items);
        if (subtotal < 0) {
            throw new IllegalArgumentException("subtotal cannot be negative");
        }
        if (evaluatedAt == null) {
            evaluatedAt = LocalDateTime.now();
        }
    }

    /** Convenience factory for automatic (non-coupon) evaluation. */
    public static CartContext of(Long userId, List<CartItem> items, long subtotal) {
        return new CartContext(userId, items, subtotal, null, LocalDateTime.now());
    }
}