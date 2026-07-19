package com.allan.engine.Rules;

import com.allan.model.Cart;
import com.allan.model.PromotionRule;
import com.allan.model.User;
import com.allan.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.allan.domain.OrderStatus;

/**
 * Rule: the user must belong to a specific customer segment.
 *
 * <p>Segments are resolved at evaluation time from the user's order history.
 * No segment field is stored on the {@link User} entity — segments are derived
 * facts, not stored attributes, which means they stay accurate automatically
 * as the user's history grows.
 *
 * <p><strong>Value format:</strong> a single segment name string (case-insensitive).
 * <pre>
 *   "NEW_CUSTOMER"  →  user has zero prior delivered orders
 *   "RETURNING"     →  user has at least one prior delivered order
 *   "VIP"           →  user's total historical spend >= vipSpendThresholdUgx
 * </pre>
 *
 * <p><strong>Supported operator:</strong> {@code EQ} only — segment membership
 * is binary (you either are or are not in the segment). Other operators are
 * rejected and the rule fails safely.
 *
 * <p><strong>VIP threshold:</strong> configured via
 * {@code promotion.vip-spend-threshold-ugx} in {@code application.properties}.
 * Default is UGX 1,000,000. Adjust per business requirements without code changes:
 * <pre>
 *   promotion.vip-spend-threshold-ugx=2000000
 * </pre>
 *
 * <p><strong>Performance note:</strong> each segment check queries the orders
 * table. This is acceptable because the rule engine short-circuits on the first
 * failing rule — place cheaper rules (MinOrderValue, MinItemCount) before this
 * one on the promotion to avoid unnecessary DB hits on ineligible carts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSegmentRule implements CartRule {

    private final OrderRepository orderRepository;

    @Value("${promotion.vip-spend-threshold-ugx:1000000}")
    private long vipSpendThresholdUgx;

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        String requiredSegment = rule.getValue().trim().toUpperCase();

        boolean result = switch (requiredSegment) {

            case "NEW_CUSTOMER" -> isNewCustomer(user);

            case "RETURNING" -> !isNewCustomer(user);

            case "VIP" -> isVip(user);

            default -> {
                log.warn("UserSegmentRule: unknown segment '{}' on ruleId={}. " +
                         "Rule fails safely.", requiredSegment, rule.getId());
                yield false;
            }
        };

        if (!result) {
            log.debug("UserSegmentRule FAILED: requiredSegment={}, userId={}, ruleId={}",
                    requiredSegment, user.getId(), rule.getId());
        }

        return result;
    }

    // ── Segment resolvers ─────────────────────────────────────────────────────

    /**
     * A new customer has zero delivered orders.
     * DELIVERED is the terminal success state — pending and cancelled orders
     * do not count toward customer history.
     */
    private boolean isNewCustomer(User user) {
        long deliveredOrderCount = orderRepository
                .countByUserIdAndOrderStatus(user.getId(), OrderStatus.DELIVERED);
        return deliveredOrderCount == 0;
    }

    /**
     * A VIP customer has a total historical spend at or above the configured
     * threshold. Spend is summed from DELIVERED orders only — cancelled and
     * pending orders are excluded to prevent gaming.
     */
    private boolean isVip(User user) {
        long totalSpend = orderRepository
                .sumTotalSellingPriceByUserIdAndOrderStatus(
                        user.getId(), OrderStatus.DELIVERED);
        return totalSpend >= vipSpendThresholdUgx;
    }
}