package com.allan.engine.Rules;

import com.allan.domain.OrderStatus;
import com.allan.model.Cart;
import com.allan.model.PromotionRule;
import com.allan.model.User;
import com.allan.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Rule: this promotion can only be applied to a customer's very first order.
 *
 * <p>Designed for new-customer acquisition promotions — "10% off your first
 * order", "free shipping on your first purchase", etc.
 *
 * <p><strong>Value format:</strong> the {@code value} field on the
 * {@link com.allan.model.PromotionRule} entity is ignored for this rule type.
 * Store {@code "true"} by convention for readability in the admin UI and
 * audit logs, but the rule does not parse it.
 *
 * <p><strong>What counts as a prior order:</strong> only orders in
 * {@link OrderStatus#DELIVERED} status. Rationale:
 * <ul>
 *   <li>PENDING / PLACED — payment not confirmed; counting these would block
 *       a genuinely new customer who abandoned a checkout.</li>
 *   <li>CANCELLED — the customer never completed a purchase; should not
 *       disqualify them from the first-order promotion.</li>
 *   <li>DELIVERED — payment received and goods delivered; this is a real,
 *       completed first order. Once one exists, the promotion no longer applies.</li>
 * </ul>
 *
 * <p><strong>Race condition note:</strong> there is a theoretical window where
 * two simultaneous checkouts by the same user could both pass this rule before
 * either order reaches DELIVERED. This is mitigated at the redemption layer by
 * the per-customer usage limit ({@code usagePerCustomer = 1}) on the
 * {@link com.allan.model.Coupon} and by the unique index on
 * {@code coupon_redemptions.order_id}. The rule engine is the first gate;
 * the redemption service is the final gate.
 *
 * <p><strong>Operator:</strong> ignored — first-order status is boolean.
 * Only {@code EQ} makes semantic sense; all other operators are treated
 * identically.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FirstOrderOnlyRule implements CartRule {

    private final OrderRepository orderRepository;

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        long completedOrderCount = orderRepository
                .countByUserIdAndOrderStatus(user.getId(), OrderStatus.DELIVERED);

        boolean isFirstOrder = completedOrderCount == 0;

        if (!isFirstOrder) {
            log.debug("FirstOrderOnlyRule FAILED: userId={} has {} completed orders, " +
                      "ruleId={}", user.getId(), completedOrderCount, rule.getId());
        }

        return isFirstOrder;
    }
}