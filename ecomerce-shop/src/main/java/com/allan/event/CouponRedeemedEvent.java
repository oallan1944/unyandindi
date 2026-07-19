package com.allan.event;

import java.time.LocalDateTime;

/**
 * Published by {@code RedemptionServiceImpl} after a coupon redemption is
 * persisted. Consumed by {@code CouponRedeemedEventListener} to update
 * usage stats and send a confirmation — always via
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)}, since acting on
 * a redemption that later rolls back (e.g. sending a confirmation for a
 * transaction that never committed) would be incorrect.
 *
 * <p>All monetary values are UGX whole shillings.
 */
public record CouponRedeemedEvent(
        Long redemptionId,
        Long couponId,
        Long promotionId,
        Long userId,
        Long orderId,
        long discountApplied,
        LocalDateTime redeemedAt
) {}