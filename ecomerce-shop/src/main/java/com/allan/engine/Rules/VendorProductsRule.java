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
 * Rule: the cart must contain at least one product from a specific vendor (seller).
 *
 * <p>Vendor membership is determined by {@code cartItem.getProduct().getSeller().getId()}.
 * The cart must be pre-fetched with items → products → seller before this rule
 * is evaluated. The {@link com.allan.service.impl.PromotionEvaluatorServiceImpl}
 * is responsible for ensuring the full cart graph is loaded.
 *
 * <p><strong>Primary use case — vendor-specific promotions:</strong> a seller
 * creates a promotion that grants a discount only when the customer has bought
 * from that seller's catalogue. The {@link com.allan.model.Promotion#getVendorId()}
 * already scopes the promotion to the vendor, but this rule provides cart-level
 * enforcement — the discount only applies if the customer actually has that
 * vendor's products in the cart.
 *
 * <p><strong>Secondary use case — platform hybrid promotions:</strong> a platform
 * admin creates a promotion that applies only when the cart contains products
 * from a specific participating vendor (e.g. a co-funded campaign with a vendor).
 *
 * <p><strong>Value format:</strong> comma-separated seller ID strings (Long).
 * Whitespace around commas is trimmed. Examples:
 * <pre>
 *   "7"      →  cart must contain a product from seller 7
 *   "7,12"   →  cart must contain a product from seller 7 OR seller 12 (IN)
 *   "7"      →  cart must NOT contain any product from seller 7 (NOT_IN)
 * </pre>
 *
 * <p><strong>Supported operators:</strong>
 * <ul>
 *   <li>{@code IN}     — at least one cart product belongs to one of the
 *       listed seller IDs. Standard vendor promotion check.</li>
 *   <li>{@code NOT_IN} — no cart product belongs to any of the listed seller IDs.
 *       Exclusion rule — e.g. discount valid only when buying from other vendors.</li>
 * </ul>
 *
 * <p><strong>Null safety:</strong> products without a seller (seller == null)
 * are silently skipped. This should never occur on valid data but protects
 * against NPEs during data migration or seeding edge cases.
 */
@Slf4j
@Component
public class VendorProductsRule implements CartRule {

    @Override
    public boolean evaluate(Cart cart, User user, PromotionRule rule) {

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            log.debug("VendorProductsRule FAILED: cart is empty, ruleId={}", rule.getId());
            return false;
        }

        // ── Parse required vendor (seller) IDs ────────────────────────────
        Set<Long> requiredVendorIds;
        try {
            requiredVendorIds = Arrays.stream(rule.getValue().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        } catch (NumberFormatException e) {
            log.warn("VendorProductsRule: invalid value '{}' on ruleId={}. " +
                     "Expected comma-separated seller IDs (long). Rule fails safely.",
                    rule.getValue(), rule.getId());
            return false;
        }

        if (requiredVendorIds.isEmpty()) {
            log.warn("VendorProductsRule: empty vendor ID list on ruleId={}. " +
                     "Rule fails safely.", rule.getId());
            return false;
        }

        // ── Collect seller IDs of products currently in the cart ──────────
        // Products with null seller are filtered — see class Javadoc.
        Set<Long> cartVendorIds = cart.getCartItems().stream()
                .map(CartItem::getProduct)
                .filter(product -> product.getSeller() != null)
                .map(product -> product.getSeller().getId())
                .collect(Collectors.toSet());

        RuleOperator operator = rule.getOperator();

        boolean result = switch (operator) {
            case IN -> requiredVendorIds.stream()
                    .anyMatch(cartVendorIds::contains);

            case NOT_IN -> requiredVendorIds.stream()
                    .noneMatch(cartVendorIds::contains);

            default -> {
                log.warn("VendorProductsRule: unsupported operator '{}' on ruleId={}. " +
                         "Rule fails safely.", operator, rule.getId());
                yield false;
            }
        };

        if (!result) {
            log.debug("VendorProductsRule FAILED: operator={}, requiredVendorIds={}, " +
                      "cartVendorIds={}, ruleId={}",
                    operator, requiredVendorIds, cartVendorIds, rule.getId());
        }

        return result;
    }
}