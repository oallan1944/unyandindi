package com.allan.response;

import java.time.LocalDateTime;

/**
 * Outbound response for a single promotion audit log entry.
 * Returned by GET /api/admin/promotions/{id}/audit.
 *
 * <p>Audit entries are always read-only — this record has no editable flag
 * because the admin UI never shows edit/delete controls for audit records.
 *
 * <p>{@code oldValue} and {@code newValue} are raw strings (scalar enum name
 * or JSON snapshot) — the frontend renders them as plain text or parses the
 * JSON for rich diff display if needed.
 */
public record PromotionAuditResponse(

        Long id,
        Long promotionId,

        /**
         * What changed. Canonical values:
         * PROMOTION_CREATED | STATUS_CHANGED | RULE_ADDED | RULE_REMOVED |
         * REWARD_UPDATED | COUPON_GENERATED | COUPON_EXHAUSTED |
         * COUPON_DISABLED | REDEMPTION_REVERSED | PRIORITY_CHANGED |
         * SCHEDULE_UPDATED
         */
        String action,

        /**
         * State before the change. Null for creation events.
         * Scalar (enum name) or JSON object snapshot.
         */
        String oldValue,

        /**
         * State after the change. Null for deletion/archival events.
         */
        String newValue,

        /**
         * Who made the change.
         * Human: email or user ID from the JWT.
         * System: "SCHEDULER" | "REDEMPTION_SERVICE" | etc.
         */
        String actor,

        /** Additional context. May be null. */
        String notes,

        /** When the change occurred. Immutable — set once at creation. */
        LocalDateTime createdAt
){}