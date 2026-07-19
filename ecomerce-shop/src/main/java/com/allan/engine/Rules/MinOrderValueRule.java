package com.allan.engine.Rules;

import com.allan.domain.RuleOperator;
import com.allan.model.Cart;
import com.allan.model.PromotionRule;
import com.allan.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Rule: the cart's total selling price must meet a UGX threshold.
 *
 * <p><strong>Value format:</strong> plain long string representing the minimum
 * cart total in UGX. No decimals, no currency symbol. Examples:
 * <pre>
 *   "50000"    →  UGX 50,000 minimum
 *   "500000"   →  UGX 500,000 minimum
 *   "2000000"  →  UGX 2,000,000 minimum (wholesale orders)
 * </pre>
 *
 * <p><strong>Supported operators:</strong>
 * <ul>
 *   <li>{@code GTE} — cart total >= threshold (most common)</li>
 *   <li>{@code EQ}  — cart total == threshold exactly (edge case)</li>
 *   <li>{@code LTE} — cart total <= threshold (discount for small orders)</li>
 * </ul>
 *
 * <p><strong>UGX note:</strong> parsed with {@code Long.parseLong()}, never
 * {@code Integer.parseInt()} — UGX thresholds on wholesale/bulk promotions
 * can exceed {@link Integer#MAX_VALUE} (~UGX 2.1 billion). While uncommon,
 * using {@code long} consistently across all monetary fields prevents the
 * class of overflow bugs that only appear in production on large orders.
 */
@Slf4j
@Component
public class MinOrderValueRule implements CartRule {

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        long threshold;
        try {
            threshold = Long.parseLong(rule.getValue().trim());
        } catch (NumberFormatException e) {
            log.warn("MinOrderValueRule: invalid value '{}' on ruleId={}. " +
                     "Expected a plain long (UGX amount). Rule fails safely.",
                    rule.getValue(), rule.getId());
            return false;
        }

        long cartTotal = cart.getTotalSellingPrice();
        RuleOperator operator = rule.getOperator();

        boolean result = switch (operator) {
            case GTE -> cartTotal >= threshold;
            case EQ  -> cartTotal == threshold;
            case LTE -> cartTotal <= threshold;
            default  -> {
                log.warn("MinOrderValueRule: unsupported operator '{}' on ruleId={}. " +
                         "Rule fails safely.", operator, rule.getId());
                yield false;
            }
        };

        if (!result) {
            log.debug("MinOrderValueRule FAILED: cartTotal=UGX{}, operator={}, " +
                      "threshold=UGX{}, ruleId={}",
                    cartTotal, operator, threshold, rule.getId());
        }

        return result;
    }
}