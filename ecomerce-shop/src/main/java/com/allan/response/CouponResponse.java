package com.allan.response;

import java.time.LocalDateTime;

/**
 * Outbound response for a single coupon — returned by coupon management
 * and checkout validation endpoints.
 *
 * <p><strong>Computed fields:</strong>
 * <ul>
 *   <li>{@code remainingUses} — derived from {@code usageLimit - usedCount}
 *       via the entity domain helper. {@link Integer#MAX_VALUE} when unlimited.</li>
 *   <li>{@code available} — derived from entity's {@code isAvailable()} helper.
 *       True only when status is ACTIVE and usedCount < usageLimit.</li>
 * </ul>
 * These are computed by the mapper, not fetched from the DB, so no extra
 * query is fired when building this response.
 *
 * <p><strong>What is omitted:</strong>
 * {@code @Version} (optimistic lock), full {@code redemptions} list
 * (use {@link CouponUsageResponse} for analytics), full promotion graph
 * (only {@code promotionId} and {@code promotionName} included).
 */
public record CouponResponse(

        Long id,

        /** ID of the parent promotion. */
        Long promotionId,

        /** Name of the parent promotion — for display without an extra request. */
        String promotionName,

        /**
         * The coupon code in UPPER CASE.
         * Always normalized on ingestion — what is stored is what is returned.
         */
        String code,

        /** Enum name: ACTIVE | EXHAUSTED | EXPIRED | DISABLED */
        String status,

        /** Maximum total redemptions across all customers. Null = unlimited. */
        Integer usageLimit,

        /** Current count of confirmed non-reversed redemptions. */
        int usedCount,

        /** Maximum redemptions per individual customer. Null = no cap. */
        Integer usagePerCustomer,

        /**
         * Remaining global uses. {@link Integer#MAX_VALUE} when usageLimit is null.
         * Derived from entity domain helper — no extra DB call.
         */
        int remainingUses,

        /**
         * True if this coupon can currently accept redemptions.
         * False if status != ACTIVE or usedCount >= usageLimit.
         * Derived from entity domain helper — no extra DB call.
         */
        boolean available,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}