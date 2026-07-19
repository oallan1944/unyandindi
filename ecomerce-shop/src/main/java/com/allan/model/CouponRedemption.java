package com.allan.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Immutable ledger record of a single successful coupon redemption.
 *
 * <p>This entity replaces the {@code @ManyToMany usedByUsers} relationship
 * that existed on the old {@code Coupon} entity. That approach could not store:
 * <ul>
 *   <li>The exact discount amount granted per redemption.</li>
 *   <li>Which order the coupon was applied to.</li>
 *   <li>The cart total at the time (needed for analytics/fraud detection).</li>
 *   <li>Reversal state for cancelled orders.</li>
 * </ul>
 *
 * <p><strong>Append-only contract:</strong> records are created on successful
 * checkout and never deleted. Order cancellation flips {@link #reversed} and
 * sets {@link #reversedAt}, then {@code RedemptionServiceImpl} decrements
 * {@link Coupon#getUsedCount()} separately. The original {@link #discount} value
 * is preserved for refund calculation accuracy.
 *
 * <p><strong>Per-customer limit check at checkout:</strong>
 * {@code CouponRedemptionRepository.countByCouponIdAndCustomerIdAndReversedFalse}
 * is the correct query — it excludes reversed redemptions so customers can reuse
 * a coupon after a genuine cancellation (if the vendor allows this).
 *
 * <p><strong>Unique constraint on orderId:</strong> one order can never receive
 * two coupon redemptions. This is enforced both by the unique DB index and by
 * a service-layer check before the Redis lock is released.
 */
@Entity
@Table(
    name = "coupon_redemptions",
    indexes = {
        @Index(name = "idx_redemption_coupon",      columnList = "coupon_id"),
        @Index(name = "idx_redemption_customer",    columnList = "customer_id"),
        @Index(name = "idx_redemption_order",       columnList = "order_id",  unique = true),
        // Composite — the most common query: "has customer X used coupon Y?"
        @Index(name = "idx_redemption_coupon_cust", columnList = "coupon_id, customer_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * The coupon that was redeemed. LAZY — most queries targeting this table
     * need only the coupon ID, not the full coupon graph.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    /**
     * References {@link User#getId()}. Stored as a plain Long (not a FK
     * relationship) to avoid pulling the full User entity into every
     * redemption lookup. Join in service layer only when needed.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * References {@link Order#getId()}. Unique — enforced by DB index above.
     * If the order is cancelled, the redemption is reversed (flag below),
     * but this field is never nulled out.
     */
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    /**
     * Exact UGX discount applied to this order at the moment of redemption.
     *
     * <p>May be less than the full reward value when:
     * <ul>
     *   <li>Cart subtotal was lower than the FLAT_OFF reward (can't discount more
     *       than the order is worth).</li>
     *   <li>Promotion's {@code maximumDiscountAmount} capped a PERCENTAGE_OFF result.
     *       Example: 20% of UGX 500,000 = UGX 100,000, but cap is UGX 20,000.</li>
     *   <li>Only a subset of cart items qualified under VENDOR_SPECIFIC scope.</li>
     * </ul>
     *
     * <p>Retained unchanged even after reversal — {@code RedemptionServiceImpl.reverse()}
     * reads this exact figure to calculate the refund amount. {@code long} because
     * UGX discount values on high-value orders can reach UGX 200,000+.
     */
    @Column(nullable = false)
    private long discount;

    /**
     * Snapshot of the cart subtotal in UGX at the moment the coupon was applied.
     * Stored for fraud analytics and reporting — never recalculated retroactively.
     *
     * <p>Effective discount rate = {@code discount} / {@code cartTotalAtRedemption}.
     * Example: UGX 10,000 discount on a UGX 150,000 cart = 6.67% effective rate.
     *
     * <p>{@code long} — a single electronics or bulk order can exceed UGX 2,000,000,
     * well within {@code long} range but outside safe {@code int} assumptions for
     * future-proofing.
     */
    @Column(name = "cart_total_at_redemption", nullable = false)
    private long cartTotalAtRedemption;

    /**
     * Flipped to {@code true} by {@code RedemptionServiceImpl.reverse()} when
     * the parent order is cancelled or refunded. Triggers a decrement of
     * {@link Coupon#getUsedCount()} in the same transaction.
     */
    @Column(nullable = false)
    private boolean reversed = false;

    /**
     * Timestamp of when the reversal occurred. NULL while not yet reversed.
     */
    @Column(name = "reversed_at")
    private LocalDateTime reversedAt;

    /**
     * Set once at creation by JPA auditing. Never updated — enforces immutability
     * of the creation timestamp at the ORM level via {@code updatable = false}.
     */
    @CreatedDate
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;
}
