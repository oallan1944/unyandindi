package com.allan.model;

import com.allan.domain.RuleOperator;
import com.allan.domain.RuleType;
import jakarta.persistence.*;
import lombok.*;

/**
 * A single eligibility criterion attached to a {@link Promotion}.
 *
 * <p>All rules on a promotion are evaluated with <strong>AND semantics</strong>
 * — every rule must pass for the promotion to be applicable to a given cart.
 * For OR semantics, create separate promotions with the same priority.
 *
 * <p>The {@code value} field is intentionally a plain {@code String} so the
 * schema never needs to change as new {@link RuleType}s are added. Parsing
 * and type-conversion is the responsibility of the concrete rule class produced
 * by {@code RuleFactory}. Conventions by type:
 * <pre>
 *   MIN_ORDER_VALUE   →  "50000"          UGX amount as plain long string, no decimals.
 *                                         RuleFactory must use Long.parseLong(), never
 *                                         Integer.parseInt() — UGX thresholds can exceed
 *                                         Integer.MAX_VALUE on bulk/wholesale orders.
 *   MIN_ITEM_COUNT    →  "3"              integer
 *   PRODUCT_IN_CART   →  "101,204,305"    comma-separated Product IDs
 *   CATEGORY          →  "12"             Category.id  (matches Product.category.id)
 *   USER_SEGMENT      →  "NEW_CUSTOMER"   matches enum name in UserSegment
 *   FIRST_ORDER_ONLY  →  "true"           value ignored; rule checks order history
 *   VENDOR_PRODUCTS   →  "7"              Seller.id (matches Product.seller.id)
 * </pre>
 *
 * <p>This entity is immutable after the parent promotion has any confirmed
 * {@link CouponRedemption}s. The service layer enforces this — editing rules
 * retroactively would invalidate the basis on which historical discounts were
 * calculated.
 */
@Entity
@Table(
    name = "promotion_rules",
    indexes = {
        @Index(name = "idx_promo_rule_promotion", columnList = "promotion_id"),
        @Index(name = "idx_promo_rule_type",      columnList = "rule_type")
    }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PromotionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    /**
     * The dimension of the cart or customer this rule inspects.
     * Maps to a concrete implementation via {@code RuleFactory}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 40)
    private RuleType ruleType;

    /**
     * How the measured value is compared to the threshold.
     * e.g. GTE (cart total >= threshold), IN (product ID is in the set).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleOperator operator;

    /**
     * Threshold or reference value as a raw String.
     * 500 chars accommodates long comma-separated product/category ID lists.
     * Parsed by the concrete rule class — never parse here.
     */
    @Column(nullable = false, length = 500)
    private String value;

    /**
     * Human-readable label for the admin UI and audit logs.
     * Example: "Minimum order value of UGX 5,000"
     *          "Must contain product from Electronics category"
     */
    @Column(length = 255)
    private String description;
}
