package com.allan.service.impl;

import com.allan.dto.CartContext;
import com.allan.dto.CartItem;
import com.allan.dto.RuleEvaluationOutcome;
import com.allan.model.Promotion;
import com.allan.model.PromotionRule;
import com.allan.service.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure, side-effect-free implementation of {@link RuleEngineService}. See
 * the interface javadoc for the purity/fail-closed contract this class must
 * uphold — no method here writes to the database or mutates its arguments.
 *
 * <p><strong>Two rule types are stubbed, not fully implemented</strong> —
 * {@code USER_SEGMENT} and {@code FIRST_ORDER_ONLY} — because evaluating
 * them needs data {@link CartContext} doesn't carry (a resolved user
 * segment, and order history). Both currently fail closed (return
 * {@code false}) with a logged warning rather than silently returning
 * {@code true}: a rule engine that fails open on unimplemented rule types
 * would let a promotion apply to carts it was never meant to. Wire in a
 * {@code UserSegmentResolver}/{@code OrderHistoryService} dependency and
 * replace these two branches before relying on those rule types in
 * production.
 */
@Service
public class RuleEngineServiceImpl implements RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineServiceImpl.class);

    @Override
    public boolean isEligible(Promotion promotion, CartContext cart) {
        for (PromotionRule rule : promotion.getRules()) {
            if (!evaluateRule(rule, cart)) {
                return false; // AND semantics — short-circuit on first failure
            }
        }
        return true;
    }

    @Override
    public List<RuleEvaluationOutcome> evaluate(Promotion promotion, CartContext cart) {
        List<RuleEvaluationOutcome> outcomes = new ArrayList<>();
        for (PromotionRule rule : promotion.getRules()) {
            boolean passed;
            String reason;
            try {
                passed = evaluateRule(rule, cart);
                reason = passed ? "Rule satisfied" : describeFailure(rule);
            } catch (RuntimeException ex) {
                // Fail closed: malformed rule data is a data-integrity bug,
                // never a reason to grant a discount.
                log.warn("Rule {} on promotion {} failed to evaluate cleanly; treating as not passed",
                        rule.getId(), promotion.getId(), ex);
                passed = false;
                reason = "Rule could not be evaluated";
            }
            outcomes.add(new RuleEvaluationOutcome(rule.getId(), passed, reason));
        }
        return outcomes;
    }

    @Override
    public boolean evaluateRule(PromotionRule rule, CartContext cart) {
        try {
            return switch (rule.getRuleType()) {
                case MIN_ORDER_VALUE -> compareLong(cart.subtotal(), Long.parseLong(rule.getValue().trim()), rule);
                case MIN_ITEM_COUNT -> compareLong(totalItemCount(cart), Long.parseLong(rule.getValue().trim()), rule);
                case PRODUCT_IN_CART -> {
                    Set<Long> productIds = parseIdSet(rule.getValue());
                    yield cart.items().stream().map(CartItem::productId).anyMatch(productIds::contains);
                }
                case CATEGORY -> {
                    long categoryId = Long.parseLong(rule.getValue().trim());
                    yield cart.items().stream()
                            .map(CartItem::categoryId)
                            .anyMatch(id -> id != null && id == categoryId);
                }
                case VENDOR_PRODUCTS -> {
                    long sellerId = Long.parseLong(rule.getValue().trim());
                    yield cart.items().stream()
                            .map(CartItem::sellerId)
                            .anyMatch(id -> id != null && id == sellerId);
                }
                case FIRST_ORDER_ONLY -> {
                    log.warn("FIRST_ORDER_ONLY rule {} evaluated without order-history lookup wired in; "
                            + "failing closed", rule.getId());
                    yield false; // TODO: inject an order-history lookup and check cart.userId()
                }
                case USER_SEGMENT -> {
                    log.warn("USER_SEGMENT rule {} evaluated without a segment resolver wired in; "
                            + "failing closed", rule.getId());
                    yield false; // TODO: inject a UserSegmentResolver and compare against rule.getValue()
                }
                default -> {
                    log.warn("Unrecognized rule type {} on rule {}; failing closed",
                            rule.getRuleType(), rule.getId());
                    yield false;
                }
            };
        } catch (NumberFormatException e) {
            log.warn("Rule {} has a non-numeric value '{}' for rule type {}; failing closed",
                    rule.getId(), rule.getValue(), rule.getRuleType(), e);
            return false;
        }
    }

    private int totalItemCount(CartContext cart) {
        return cart.items().stream().mapToInt(CartItem::quantity).sum();
    }

    private Set<Long> parseIdSet(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Numeric comparison per {@code RuleOperator}. Only GTE/LTE/EQ/GT/LT are
     * meaningful for the numeric rule types (MIN_ORDER_VALUE, MIN_ITEM_COUNT);
     * any other operator on a numeric rule is a configuration error and
     * fails closed rather than guessing.
     */
    private boolean compareLong(long actual, long threshold, PromotionRule rule) {
        return switch (rule.getOperator()) {
            case GTE -> actual >= threshold;
            case GT -> actual > threshold;
            case LTE -> actual <= threshold;
            case LT -> actual < threshold;
            case EQ -> actual == threshold;
            default -> {
                log.warn("Operator {} is not valid for numeric rule {} (type {}); failing closed",
                        rule.getOperator(), rule.getId(), rule.getRuleType());
                yield false;
            }
        };
    }

    private String describeFailure(PromotionRule rule) {
        // Merchant/support-facing only — see RuleEvaluationOutcome javadoc
        // on why this must stay generic rather than including raw thresholds.
        return rule.getDescription() != null
                ? "Not met: " + rule.getDescription()
                : "Rule not satisfied";
    }
}