package com.allan.model;

import com.allan.domain.CouponStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A redeemable code generated under a {@link Promotion}.
 *
 * <p><strong>Replaces the previous Coupon entity.</strong> The old entity had:
 * <ul>
 *   <li>{@code double discountPercentage} — floating-point is wrong for UGX money.
 *       Discount value now lives on {@link PromotionReward#getValue()} as {@code long}.</li>
 *   <li>{@code @ManyToMany usedByUsers} — could not store per-redemption discount,
 *       order reference, or reversal state. Replaced by {@link CouponRedemption}.</li>
 *   <li>{@code validityStartDate/EndDate} — validity window is now owned by the
 *       parent {@link Promotion#getStartsAt()} and {@link Promotion#getEndsAt()}.</li>
 *   <li>{@code boolean isActive} — replaced by {@link CouponStatus} enum which
 *       distinguishes ACTIVE / EXHAUSTED / EXPIRED / DISABLED states precisely.</li>
 *   <li>No usage limit enforcement and no concurrency protection.</li>
 * </ul>
 *
 * <p><strong>UGX note:</strong> no monetary value is stored on this entity directly.
 * The discount amount is on {@link PromotionReward} and the per-redemption UGX
 * discount granted is recorded on {@link CouponRedemption#getDiscount()}.
 *
 * <p><strong>Concurrency safety:</strong> {@code usedCount} is incremented only by
 * {@code RedemptionServiceImpl} after acquiring a Redis lock on the coupon code.
 * The {@code @Version} field is a second line of defence at the database level.
 *
 * <p><strong>Code format:</strong> always stored and compared in UPPER CASE.
 * Normalize on ingestion in {@code CouponServiceImpl}.
 */
@Entity
@Table(
    name = "coupons",
    indexes = {
        @Index(name = "idx_coupon_code",      columnList = "code",         unique = true),
        @Index(name = "idx_coupon_promotion", columnList = "promotion_id"),
        @Index(name = "idx_coupon_status",    columnList = "status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * The promotion this coupon belongs to.
     * A coupon is meaningless without a parent promotion — the promotion
     * holds the eligibility rules, rewards, and validity window.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    /**
     * The code entered by the customer at checkout.
     * Max 50 chars covers vanity codes (SAVE20) and UUID-derived bulk codes.
     * Always stored in UPPER CASE — normalize in CouponServiceImpl on creation.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Maximum total redemptions across all customers.
     * NULL = unlimited (admin platform coupons only — vendors must set a limit).
     */
    @Column(name = "usage_limit")
    private Integer usageLimit;

    /**
     * Running count of successful redemptions.
     * Only incremented by RedemptionServiceImpl — never write this elsewhere.
     */
    @Column(name = "used_count", nullable = false)
    private int usedCount = 0;

    /**
     * Maximum redemptions per individual customer.
     * Enforced by querying CouponRedemption at checkout.
     * Default 1 = one-per-customer, correct for almost all campaigns.
     * NULL = no per-customer cap (use with caution).
     */
    @Column(name = "usage_per_customer")
    private Integer usagePerCustomer = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status = CouponStatus.ACTIVE;

    /**
     * Optimistic lock — prevents concurrent over-redemption at the DB level
     * if the Redis lock layer fails. Never expose in a DTO or API response.
     */
    @Version
    private Long version;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Relationships ─────────────────────────────────────────────────────────

    /**
     * Immutable per-redemption records.
     * Never iterate this collection for limit checks — use
     * CouponRedemptionRepository.countByCouponIdAndCustomerIdAndReversedFalse() instead.
     */
    @OneToMany(
        mappedBy = "coupon",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @OrderBy("redeemedAt DESC")
    private List<CouponRedemption> redemptions = new ArrayList<>();

    // ── Domain helpers ────────────────────────────────────────────────────────

    /**
     * Fast availability check using this entity's own state only.
     * Does NOT check per-customer limits — that needs a repository call.
     * Always call this first; only query the DB if this returns true.
     */
    public boolean isAvailable() {
        if (status != CouponStatus.ACTIVE) return false;
        if (usageLimit == null) return true;
        return usedCount < usageLimit;
    }

    /**
     * Remaining global uses.
     * Returns Integer.MAX_VALUE for unlimited coupons (usageLimit == null).
     */
    public int remainingUses() {
        if (usageLimit == null) return Integer.MAX_VALUE;
        return Math.max(0, usageLimit - usedCount);
    }
}