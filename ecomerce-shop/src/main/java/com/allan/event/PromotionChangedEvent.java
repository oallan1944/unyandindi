package com.allan.event;

import com.allan.domain.PromotionChangeType;

import java.time.LocalDateTime;

/**
 * Published by {@code PromotionServiceImpl} whenever a promotion's status,
 * schedule, rules, or rewards change. Consumed by
 * {@code PromotionChangedEventListener} to invalidate
 * {@code PromotionCacheService}'s active-promotions cache.
 *
 * <p>This event does <strong>not</strong> trigger a {@code PromotionAudit}
 * write — that happens synchronously inside {@code PromotionServiceImpl}
 * in the same transaction as the change (see {@code PromotionAudit}'s
 * javadoc). This event exists solely to drive the async cache-invalidation
 * side effect.
 */
public record PromotionChangedEvent(
        Long promotionId,
        PromotionChangeType changeType,
        LocalDateTime occurredAt
) {}