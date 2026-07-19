package com.allan.engine.Rules;

import com.allan.domain.RuleOperator;
import com.allan.model.Cart;
import com.allan.model.CartItem;
import com.allan.model.PromotionRule;
import com.allan.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule: specific product IDs must (or must not) be present in the cart.
 *
 * <p><strong>Value format:</strong> comma-separated product ID strings.
 * Whitespace around commas is trimmed. Examples:
 * <pre>
 *   "101"          →  product 101 must be in the cart
 *   "101,204,305"  →  at least one of these products must be in the cart (IN)
 *   "101,204"      →  none of these products may be in the cart (NOT_IN)
 * </pre>
 *
 * <p><strong>Supported operators:</strong>
 * <ul>
 *   <li>{@code IN}     — at least one of the listed product IDs must be present.
 *       Use to create promotions that only activate when a specific product
 *       is in the cart (e.g. "buy product X, get 10% off your order").</li>
 *   <li>{@code NOT_IN} — none of the listed product IDs may be present.
 *       Use as an exclusion rule (e.g. "discount applies except when
 *       product X is in the cart").</li>
 * </ul>
 *
 * <p><strong>Security note:</strong> product IDs are parsed to {@code Long}
 * before any comparison. Raw string comparison is never used — prevents
 * injection of crafted ID strings that might accidentally match partial values.
 */
@Slf4j
@Component
public class ProductInCartRule implements CartRule {

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.debug("ProductInCartRule FAILED: cart is empty, ruleId={}", rule.getId());
            return false;
        }

        // ── Parse the required product IDs from rule value ────────────────
        Set<Long> requiredProductIds;
        try {
            requiredProductIds = Arrays.stream(rule.getValue().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            log.warn("ProductInCartRule: invalid value '{}' on ruleId={}. " +
                     "Expected comma-separated product IDs (long). Rule fails safely.",
                    rule.getValue(), rule.getId());
            return false;
        }

        if (requiredProductIds.isEmpty()) {
            log.warn("ProductInCartRule: empty product ID list on ruleId={}. " +
                     "Rule fails safely.", rule.getId());
            return false;
        }

        // ── Collect product IDs currently in the cart ─────────────────────
        Set<Long> cartProductIds = cart.getCartItems().stream()
                .map(CartItem::getProduct)
                .map(product -> product.getId())
                .collect(Collectors.toSet());

        RuleOperator operator = rule.getOperator();

        boolean result = switch (operator) {
            // At least one required product must be present
            case IN -> requiredProductIds.stream()
                    .anyMatch(cartProductIds::contains);

            // None of the listed products may be present
            case NOT_IN -> requiredProductIds.stream()
                    .noneMatch(cartProductIds::contains);

            default -> {
                log.warn("ProductInCartRule: unsupported operator '{}' on ruleId={}. " +
                         "Rule fails safely.", operator, rule.getId());
                yield false;
            }
        };

        if (!result) {
            log.debug("ProductInCartRule FAILED: operator={}, requiredIds={}, " +
                      "cartProductIds={}, ruleId={}",
                    operator, requiredProductIds, cartProductIds, rule.getId());
        }

        return result;
    }
}
