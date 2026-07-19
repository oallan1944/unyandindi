package com.allan.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Immutable audit entry recording every state change made to a {@link Promotion}.
 *
 * <p>Written by {@code PromotionServiceImpl} on every meaningful mutation:
 * creation, status transitions, rule or reward edits, coupon generation,
 * coupon exhaustion, and redemption reversals.
 *
 * <p><strong>Append-only contract:</strong> no UPDATE or DELETE should ever
 * be issued against this table. Enforce at the DB level by granting only
 * INSERT + SELECT to the application role in production.
 *
 * <p>{@code oldValue} and {@code newValue} store either scalar strings
 * (for simple changes like status: "DRAFT" → "ACTIVE") or JSON snapshots
 * (for complex changes like a rule being replaced). Using {@code TEXT} columns
 * avoids schema migrations when the promotion model evolves — the audit log
 * remains readable regardless of what version of the domain model wrote it.
 *
 * <p><strong>Static factory methods</strong> on this class keep call sites in
 * {@code PromotionServiceImpl} concise and guarantee consistent action name
 * formatting. Always use them rather than constructing this entity manually.
 */
@Entity
@Table(
    name = "promotion_audit",
    indexes = {
        @Index(name = "idx_audit_promotion",  columnList = "promotion_id"),
        @Index(name = "idx_audit_action",     columnList = "action"),
        @Index(name = "idx_audit_actor",      columnList = "actor"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PromotionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    /**
     * Short descriptor of what changed. Free-form String (not enum) so new
     * action types never require a schema migration.
     *
     * <p>Canonical action names used by {@code PromotionServiceImpl}:
     * <pre>
     *   PROMOTION_CREATED
     *   STATUS_CHANGED          (DRAFT→ACTIVE, ACTIVE→PAUSED, etc.)
     *   RULE_ADDED
     *   RULE_REMOVED
     *   REWARD_UPDATED
     *   COUPON_GENERATED
     *   COUPON_EXHAUSTED        (usedCount reached usageLimit)
     *   COUPON_DISABLED
     *   REDEMPTION_REVERSED     (order cancelled; usedCount decremented)
     *   PRIORITY_CHANGED
     *   SCHEDULE_UPDATED        (startsAt or endsAt changed)
     * </pre>
     */
    @Column(nullable = false, length = 60)
    private String action;

    /**
     * State before the change. NULL for creation events.
     * Scalar: raw enum name or number. Complex: JSON object snapshot.
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * State after the change. NULL for deletion/archival events.
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * Who triggered the change.
     * Human action: email or user ID from the JWT principal (via AuditorAware).
     * System action: constant string — "SCHEDULER", "REDEMPTION_SERVICE", etc.
     */
    @Column(nullable = false, length = 150)
    private String actor;

    /**
     * Additional context that doesn't fit in old/new value snapshots.
     * Example: "Reversed redemption for order #4821 — customer cancellation."
     *          "Coupon SAVE20 exhausted after 500 redemptions."
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Set once at creation by JPA auditing. {@code updatable = false} enforces
     * immutability of this timestamp at the ORM level.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Static factories ──────────────────────────────────────────────────────
    // Always use these in PromotionServiceImpl — never call new PromotionAudit()
    // and set fields manually, as that risks inconsistent action name formatting.

    public static PromotionAudit of(
            Promotion promotion,
            String action,
            String oldValue,
            String newValue,
            String actor,
            String notes) {
        PromotionAudit entry = new PromotionAudit();
        entry.setPromotion(promotion);
        entry.setAction(action);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        entry.setActor(actor);
        entry.setNotes(notes);
        return entry;
    }

    /** Shorthand when there are no extra notes. */
    public static PromotionAudit of(
            Promotion promotion,
            String action,
            String oldValue,
            String newValue,
            String actor) {
        return of(promotion, action, oldValue, newValue, actor, null);
    }

    public static PromotionAudit ofCreation(Promotion promotion, String actor) {
        return of(promotion, "PROMOTION_CREATED", null, promotion.getName(), actor);
    }

    public static PromotionAudit ofStatusChange(
            Promotion promotion,
            String fromStatus,
            String toStatus,
            String actor) {
        return of(promotion, "STATUS_CHANGED", fromStatus, toStatus, actor);
    }

    public static PromotionAudit ofRedemptionReversal(
            Promotion promotion,
            Long orderId,
            String actor) {
        return of(
            promotion,
            "REDEMPTION_REVERSED",
            null,
            null,
            actor,
            "Reversed redemption for order #" + orderId
        );
    }
}
