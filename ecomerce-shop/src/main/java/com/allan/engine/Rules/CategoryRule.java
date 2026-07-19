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
 * Rule: the cart must contain at least one product from a specific category.
 *
 * <p>Category membership is determined by {@code cartItem.getProduct().getCategory().getId()}.
 * The cart must be pre-fetched with items → products → category before this
 * rule is evaluated. If the category association is a lazy proxy that was never
 * initialized, accessing it here will throw {@code LazyInitializationException}.
 * The {@link com.allan.service.impl.PromotionEvaluatorServiceImpl} is responsible
 * for ensuring the cart graph is fully loaded before invoking the rule engine.
 *
 * <p><strong>Value format:</strong> comma-separated category ID strings (Long).
 * Whitespace around commas is trimmed. Examples:
 * <pre>
 *   "5"      →  cart must contain a product from category 5
 *   "5,12"   →  cart must contain a product from category 5 OR category 12 (IN)
 * </pre>
 *
 * <p><strong>Supported operators:</strong>
 * <ul>
 *   <li>{@code IN}     — at least one cart product belongs to one of the
 *       listed category IDs. Most common — targets a product line.</li>
 *   <li>{@code NOT_IN} — no cart product belongs to any of the listed
 *       category IDs. Use as an exclusion (e.g. "discount not valid on
 *       Electronics").</li>
 * </ul>
 *
 * <p><strong>Null safety:</strong> products without a category (category == null)
 * are silently skipped — they cannot satisfy an IN rule and are irrelevant
 * for NOT_IN. This prevents a NullPointerException if a product was saved
 * without a category during data migration.
 */
@Slf4j
@Component
public class CategoryRule implements CartRule {

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.debug("CategoryRule FAILED: cart is empty, ruleId={}", rule.getId());
            return false;
        }

        // ── Parse required category IDs ───────────────────────────────────
        Set<Long> requiredCategoryIds;
        try {
            requiredCategoryIds = Arrays.stream(rule.getValue().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            log.warn("CategoryRule: invalid value '{}' on ruleId={}. " +
                     "Expected comma-separated category IDs (long). Rule fails safely.",
                    rule.getValue(), rule.getId());
            return false;
        }

        if (requiredCategoryIds.isEmpty()) {
            log.warn("CategoryRule: empty category ID list on ruleId={}. " +
                     "Rule fails safely.", rule.getId());
            return false;
        }

        // ── Collect category IDs of products in the cart ──────────────────
        // Products with null category are filtered out — see class Javadoc.
        Set<Long> cartCategoryIds = cart.getCartItems().stream()
                .map(CartItem::getProduct)
                .filter(product -> product.getCategory() != null)
                .map(product -> product.getCategory().getId())
                .collect(Collectors.toSet());

        RuleOperator operator = rule.getOperator();

        boolean result = switch (operator) {
            case IN -> requiredCategoryIds.stream()
                    .anyMatch(cartCategoryIds::contains);

            case NOT_IN -> requiredCategoryIds.stream()
                    .noneMatch(cartCategoryIds::contains);

            default -> {
                log.warn("CategoryRule: unsupported operator '{}' on ruleId={}. " +
                         "Rule fails safely.", operator, rule.getId());
                yield false;
            }
        };

        if (!result) {
            log.debug("CategoryRule FAILED: operator={}, requiredCategoryIds={}, " +
                      "cartCategoryIds={}, ruleId={}",
                    operator, requiredCategoryIds, cartCategoryIds, rule.getId());
        }

        return result;
    }
}