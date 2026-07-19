package com.allan.model;

import com.allan.domain.RewardType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Defines the benefit a customer receives when a {@link Promotion} is applied.
 *
 * <p>Interpretation of {@link #value} by {@link RewardType}:
 * <ul>
 *   <li>{@code PERCENTAGE_OFF}  — whole-number percentage (e.g. {@code 20} = 20% off).
 *       The promotion evaluator caps the computed discount against
 *       {@link Promotion#getMaximumDiscountAmount()} when non-zero.</li>
 *   <li>{@code FLAT_OFF}        — fixed discount in UGX with no minor-unit conversion
 *       needed (e.g. {@code 2_000L} = UGX 2,000 off). Applied to the eligible subtotal.</li>
 *   <li>{@code FREE_SHIPPING}   — {@code value} is stored as {@code 0}; the
 *       checkout service zeroes the shipping fee when this reward is applied.</li>
 *   <li>{@code FREE_ITEM}       — {@code value} is the {@link Product#getId()} of
 *       the free item. Checkout adds one unit at zero selling price.</li>
 * </ul>
 *
 * <p><strong>Tiered promotions</strong> (e.g. "spend UGX 10k → 10% off, spend UGX 20k
 * → 20% off") must be modelled as two separate {@link Promotion} entities with
 * matching {@link PromotionRule} thresholds and distinct priorities — NOT as two
 * rewards on one entity. This keeps evaluation logic simple and deterministic.
 *
 * <p><strong>Vendor participation in platform promotions:</strong>
 * {@link #applicableVendorId} allows a platform promotion to grant a reward that
 * applies only to one participating vendor's items. Example: a sitewide campaign
 * where the platform subsidises 10% off on Vendor A's electronics only.
 * {@code null} means the reward applies to all items within the promotion's scope.
 *
 * <p>Like {@link PromotionRule}, this entity is immutable once the parent promotion
 * has active redemptions.
 */
@Entity
@Table(
    name = "promotion_rewards",
    indexes = {
        @Index(name = "idx_promo_reward_promotion", columnList = "promotion_id"),
        @Index(name = "idx_promo_reward_type",      columnList = "reward_type")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PromotionReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    /**
     * The category of benefit being granted.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private RewardType rewardType;

    /**
     * Numeric reward value — interpretation depends on {@link #rewardType}:
     *
     * <ul>
     *   <li>{@code PERCENTAGE_OFF} — whole-number percentage, 1–100.
     *       Stored as {@code long} for type consistency, but the valid range
     *       is narrow. Example: {@code 20L} = 20% off.</li>
     *   <li>{@code FLAT_OFF} — fixed discount in UGX. Example: {@code 5_000L}
     *       = UGX 5,000 off. UGX has no minor units so this is the exact
     *       shilling amount deducted from the eligible subtotal.</li>
     *   <li>{@code FREE_SHIPPING} — store {@code 0L}; the checkout service
     *       zeroes the shipping fee. This field is semantically unused.</li>
     *   <li>{@code FREE_ITEM} — store the {@link Product#getId()} of the free
     *       item. Checkout adds one unit at UGX 0 selling price.</li>
     * </ul>
     *
     * <p>{@code long}, never {@code double} — UGX FLAT_OFF values like
     * UGX 10,000 must be stored exactly. Floating-point cannot guarantee this.
     */
    @Column(nullable = false)
    private long value;

    /**
     * Display label rendered in the cart summary and order confirmation email.
     * Example: "20% off your order" / "Free shipping applied" / "Free item added"
     */
    @Column(length = 255)
    private String label;

    /**
     * When non-null, this reward only applies to items sold by this vendor.
     * References {@link Seller#getId()}.
     * NULL = reward applies to all items in the promotion's scope.
     */
    @Column(name = "applicable_seller_id")
    private Long applicableSellerId;
}
