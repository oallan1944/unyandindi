package com.allan.response;

import com.allan.response.PromotionRuleResponse;
import com.allan.response.PromotionRewardResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbound response for a full promotion — returned by admin and vendor
 * promotion endpoints.
 *
 * <p>Uses a Java record for immutability. Once built by
 * {@link com.allan.mapper.PromotionMapper#toResponse(com.allan.model.Promotion, boolean)},
 * this response cannot be mutated — safe to cache or pass across threads.
 *
 * <p><strong>Type fields as String:</strong> {@code type}, {@code scope},
 * {@code status} are serialized as their enum name strings (e.g. "ACTIVE",
 * "PLATFORM_WIDE") rather than enum objects. This decouples the API contract
 * from internal enum names — the frontend works with plain strings and is
 * not affected if enum constants are renamed internally.
 *
 * <p><strong>UGX monetary fields:</strong> {@code minimumOrderValue} and
 * {@code maximumDiscountAmount} are {@code long} whole shillings.
 * The frontend formats these as "UGX X,XXX" — never as decimals.
 *
 * <p><strong>Computed fields:</strong> {@code live} and {@code codeBased}
 * are derived from entity domain helpers, not stored columns.
 * {@code totalCoupons} is the size of the loaded coupons collection.
 */
public record PromotionResponse(

        Long id,

        /** NULL for platform-wide promotions. Non-null for vendor-specific. */
        Long sellerId,

        String name,
        String description,

        /** Enum name: PERCENTAGE_OFF | FLAT_AMOUNT_OFF | FREE_SHIPPING | BUY_X_GET_Y */
        String type,

        /** Enum name: PLATFORM_WIDE | VENDOR_SPECIFIC | CATEGORY | PRODUCT */
        String scope,

        /** Enum name: DRAFT | ACTIVE | PAUSED | EXPIRED | CANCELLED */
        String status,

        /** Lower = higher precedence at checkout. */
        int priority,

        boolean stackable,
        boolean exclusive,

        /** Minimum cart total in UGX whole shillings. 0 = no minimum. */
        long minimumOrderValue,

        /** Maximum discount cap in UGX whole shillings. 0 = no cap. */
        long maximumDiscountAmount,

        LocalDateTime startsAt,
        LocalDateTime endsAt,

        /** True if status is ACTIVE and current time is within the schedule window. */
        boolean live,

        /** True if this promotion requires a coupon code to be applied. */
        boolean codeBased,

        /** Total coupons generated under this promotion. */
        int totalCoupons,

        List<PromotionRuleResponse> rules,
        List<PromotionRewardResponse> rewards,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        /** Email or user ID of the admin/vendor who created this promotion. */
        String createdBy
) {}