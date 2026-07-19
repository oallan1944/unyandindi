package com.allan.service.support;

import com.allan.dto.CartContext;

/**
 * Rebuilds a trusted {@link CartContext} from an already-persisted
 * {@code Order} for use at redemption time.
 *
 * <p><strong>Integration point — no implementation provided.</strong> This
 * module was not given your {@code Order}/{@code OrderItem} entities, so
 * only the contract is defined here. Implement this against your real order
 * model; it must:
 * <ul>
 *   <li>Verify the order belongs to {@code userId} — throw
 *       {@code IllegalArgumentException} (mapped to 403/404 by your
 *       exception handler) if it doesn't, so one customer can never redeem
 *       a coupon against another customer's order.</li>
 *   <li>Build every {@link com.allan.dto.CartItem} from the order's own
 *       persisted line items and a fresh {@code Product} lookup for
 *       seller/category — never from any client-submitted request body,
 *       even if the client resubmits the same data it sent at "add to
 *       cart" time.</li>
 * </ul>
 */
public interface OrderContextService {

    CartContext loadForRedemption(Long orderId, Long userId);
}