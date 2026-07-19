package com.allan.engine.Rules;

import com.allan.domain.RuleOperator;
import com.allan.model.Cart;
import com.allan.model.PromotionRule;
import com.allan.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Rule: the total quantity of items in the cart must meet a threshold.
 *
 * <p>Counts the sum of all {@link com.allan.model.CartItem#getQuantity()} values,
 * not the number of distinct products. A cart with 1 item × quantity 3 has a
 * total item count of 3, not 1.
 *
 * <p><strong>Value format:</strong> plain integer string. Examples:
 * <pre>
 *   "3"   →  at least 3 total items (GTE) — used for BUY_X_GET_Y promotions
 *   "10"  →  at least 10 items — bulk purchase discount
 *   "1"   →  exactly 1 item (EQ) — single-item promotion
 * </pre>
 *
 * <p><strong>Supported operators:</strong>
 * <ul>
 *   <li>{@code GTE} — total quantity >= threshold (most common)</li>
 *   <li>{@code EQ}  — total quantity == threshold exactly</li>
 *   <li>{@code LTE} — total quantity <= threshold (small basket promotion)</li>
 * </ul>
 *
 * <p><strong>BUY_X_GET_Y pattern:</strong> pair this rule ({@code GTE, "3"})
 * with a {@link com.allan.model.PromotionReward} of type {@code FREE_ITEM}
 * to model "buy 3, get 1 free" without any special-case logic in the engine.
 */
@Slf4j
@Component
public class MinItemCountRule implements CartRule {

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        int threshold;
        try {
            threshold = Integer.parseInt(rule.getValue().trim());
        } catch (NumberFormatException e) {
            log.warn("MinItemCountRule: invalid value '{}' on ruleId={}. " +
                     "Expected a plain integer. Rule fails safely.",
                    rule.getValue(), rule.getId());
            return false;
        }

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.debug("MinItemCountRule FAILED: cart is empty, ruleId={}", rule.getId());
            return false;
        }

        int totalQuantity = cart.getCartItems().stream()
                .mapToInt(item -> item.getQuantity())
                .sum();

        RuleOperator operator = rule.getOperator();

        boolean result = switch (operator) {
            case GTE -> totalQuantity >= threshold;
            case EQ  -> totalQuantity == threshold;
            case LTE -> totalQuantity <= threshold;
            default  -> {
                log.warn("MinItemCountRule: unsupported operator '{}' on ruleId={}. " +
                         "Rule fails safely.", operator, rule.getId());
                yield false;
            }
        };

        if (!result) {
            log.debug("MinItemCountRule FAILED: totalQuantity={}, operator={}, " +
                      "threshold={}, ruleId={}",
                    totalQuantity, operator, threshold, rule.getId());
        }

        return result;
    }
}