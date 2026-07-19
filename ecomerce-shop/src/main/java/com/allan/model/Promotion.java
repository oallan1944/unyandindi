package com.allan.model;

import com.allan.domain.PromotionScope;
import com.allan.domain.PromotionStatus;
import com.allan.domain.PromotionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Root aggregate of the promotion system.
 *
 * <p>A promotion is the top-level campaign definition. It owns:
 * <ul>
 *   <li>Eligibility {@link PromotionRule}s — what the cart must look like.</li>
 *   <li>{@link PromotionReward}s — what discount the customer receives.</li>
 *   <li>{@link Coupon}s — optional codes that gate access to this promotion.</li>
 *   <li>{@link PromotionAudit} entries — append-only change log.</li>
 * </ul>
 *
 * <p><strong>Scope and permissions (hybrid model):</strong>
 * <ul>
 *   <li>{@code vendorId == null} → platform-wide; only admin can create/modify.</li>
 *   <li>{@code vendorId != null} → vendor-specific; the service layer enforces
 *       that the authenticated seller's ID matches {@code vendorId}.</li>
 * </ul>
 *
 * <p><strong>Immutability after redemptions:</strong> once any {@link CouponRedemption}
 * exists for this promotion's coupons, {@code PromotionServiceImpl} blocks
 * destructive edits (rule changes, reward value changes). Only {@code status},
 * {@code endsAt}, and {@code priority} remain mutable. Significant changes
 * require creating a new promotion.
 *
 * <p><strong>FlashSale integration:</strong> {@link FlashSale} carries a nullable
 * {@code promotionId} FK. When set, the flash sale's discount is governed by
 * this promotion's rewards rather than its own {@code discountPercent}. The
 * scheduler activates/deactivates the promotion in sync with the sale window.
 *
 * <p><strong>Uganda Shillings (UGX):</strong> UGX has no minor units — 1 UGX = 1 UGX,
 * so no multiplication by 100 is ever needed. All monetary fields are {@code long}
 * (not {@code int}) because UGX values are large: a mid-range electronics order
 * can exceed UGX 2,000,000, and aggregate discount caps on platform-wide promotions
 * can reach UGX 50,000,000+. {@code int} max (~2.1 billion) is technically sufficient
 * for single-order values today, but {@code long} eliminates the risk entirely.
 * Never use {@code double} or {@code float} for any monetary value — floating-point
 * cannot represent arbitrary integers exactly.
 */
@Entity
@Table(
    name = "promotions",
    indexes = {
        @Index(name = "idx_promotion_vendor",       columnList = "vendor_id"),
        @Index(name = "idx_promotion_status",       columnList = "status"),
        @Index(name = "idx_promotion_scope",        columnList = "scope"),
        @Index(name = "idx_promotion_active_dates", columnList = "starts_at, ends_at"),
        @Index(name = "idx_promotion_type",         columnList = "type")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ── Ownership ─────────────────────────────────────────────────────────────

    /**
     * References {@link Seller#getId()}.
     * NULL = platform-wide promotion (admin-owned).
     * Non-null = vendor-specific; service layer checks this against JWT seller ID.
     */
    @Column(name = "seller_id")
    private Long sellerId;

    // ── Identity ──────────────────────────────────────────────────────────────

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromotionType type;

    /**
     * PLATFORM_WIDE → applies to all items in cart regardless of vendor.
     * VENDOR_SPECIFIC → applies only to items where product.seller.id == vendorId.
     * CATEGORY → applies only to items where product.category.id matches rule value.
     * PRODUCT → applies only to specific product IDs listed in rules.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PromotionScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PromotionStatus status = PromotionStatus.DRAFT;

    // ── Application logic ─────────────────────────────────────────────────────

    /**
     * Lower number = higher precedence. When multiple promotions are eligible
     * for a cart, the evaluator selects the one with the lowest priority value.
     * Admin platform promotions should use 1–10; vendor promotions 50–200.
     */
    @Column(nullable = false)
    private Integer priority = 100;

    /**
     * Can be combined with other stackable promotions on the same order.
     * The evaluator only stacks promotions when BOTH have stackable = true.
     */
    @Column(nullable = false)
    private boolean stackable = false;

    /**
     * When true, no other promotion can apply to the same order — overrides
     * stackable = true on any other promotion. Use for flash sales and
     * sitewide events where stacking would be financially dangerous.
     */
    @Column(nullable = false)
    private boolean exclusive = false;

    /**
     * Minimum cart subtotal in UGX required for this promotion to be eligible.
     * Example: {@code 50_000L} = UGX 50,000 minimum order.
     * {@code 0} = no minimum threshold.
     *
     * <p>Stored as {@code long} because UGX thresholds on platform-wide promotions
     * can legitimately reach UGX 500,000–1,000,000 without approaching int overflow,
     * but {@code long} is the correct type for all money in this application.
     */
    @Column(nullable = false)
    private long minimumOrderValue = 0L;

    /**
     * Hard cap on the computed discount amount, in UGX.
     * Example: {@code 20_000L} = maximum UGX 20,000 off regardless of cart size.
     * Relevant for {@code PERCENTAGE_OFF} promotions — without this cap, a 20%
     * discount on a UGX 2,000,000 cart grants UGX 400,000 off, which may exceed
     * vendor margins.
     * {@code 0} = no cap applied.
     */
    @Column(nullable = false)
    private long maximumDiscountAmount = 0L;

    // ── Schedule ──────────────────────────────────────────────────────────────

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    // ── Concurrency ───────────────────────────────────────────────────────────

    /**
     * Optimistic lock. Prevents concurrent admin sessions from silently
     * overwriting each other during status transitions or priority changes.
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

    /**
     * Populated from Spring Security's current principal via AuditorAware.
     * Stores the email or user ID of whoever created/last-modified this record.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // ── Relationships ─────────────────────────────────────────────────────────

    /** ALL rules must pass (AND semantics) for this promotion to be applicable. */
    @OneToMany(
        mappedBy = "promotion",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<PromotionRule> rules = new ArrayList<>();

    /** What discount(s) the customer receives when this promotion applies. */
    @OneToMany(
        mappedBy = "promotion",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<PromotionReward> rewards = new ArrayList<>();

    /**
     * Code-gated coupons under this promotion.
     * Empty list = automatic promotion (no code required; applied by evaluator).
     */
    @OneToMany(
        mappedBy = "promotion",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Coupon> coupons = new ArrayList<>();

    /** Append-only change log. Never remove entries. */
    @OneToMany(
        mappedBy = "promotion",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @OrderBy("createdAt DESC")
    private List<PromotionAudit> auditLog = new ArrayList<>();

    // ── Domain helpers ────────────────────────────────────────────────────────

    /** True if this promotion is currently within its active window. */
    public boolean isLive() {
        LocalDateTime now = LocalDateTime.now();
        return status == PromotionStatus.ACTIVE
            && !now.isBefore(startsAt)
            && !now.isAfter(endsAt);
    }

    /** True if this promotion requires a coupon code to be applied. */
    public boolean isCodeBased() {
        return !coupons.isEmpty();
    }
}
