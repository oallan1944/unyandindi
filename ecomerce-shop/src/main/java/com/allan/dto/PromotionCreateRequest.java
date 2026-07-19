package com.allan.dto;

import com.allan.domain.PromotionScope;
import com.allan.domain.PromotionType;

import java.time.LocalDateTime;

/**
 * Inbound payload for creating a {@code Promotion}.
 *
 * <p><strong>Security:</strong> deliberately has NO {@code sellerId} and NO
 * {@code status} field. Ownership is always taken from the authenticated
 * caller by {@code PromotionService.createSellerPromotion(sellerId, ...)} /
 * {@code createPlatformPromotion(...)}, never from the request body — a
 * request-body {@code sellerId} would let seller A create promotions "as"
 * seller B. Status always starts at {@code DRAFT}; callers must go through
 * {@code PromotionService.updateStatus(...)} to activate, which applies
 * transition validation and priority-band checks.
 */
public record PromotionCreateRequest(
        String name,
        String description,
        PromotionType type,
        PromotionScope scope,
        Integer priority,
        boolean stackable,
        boolean exclusive,
        long minimumOrderValue,
        long maximumDiscountAmount,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}