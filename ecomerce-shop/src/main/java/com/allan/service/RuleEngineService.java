package com.allan.service;

import com.allan.dto.CartContext;
import com.allan.dto.RuleEvaluationOutcome;
import com.allan.model.Promotion;
import com.allan.model.PromotionRule;

import java.util.List;

/**
 * Evaluates a {@link Promotion}'s eligibility {@link PromotionRule}s
 * (AND semantics — every rule must pass) against a {@link CartContext}.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>Purity.</strong> Every method here MUST be free of side
 *       effects — no writes to the database, no audit entries, no mutation
 *       of the {@code Promotion} or {@code CartContext} passed in. This is
 *       what makes it safe to call repeatedly for cart previews, safe to
 *       run in parallel across candidate promotions, and safe to re-run a
 *       second time inside {@code RedemptionService} immediately before
 *       committing a redemption, without any risk of double side effects.</li>
 *   <li><strong>Trusts only {@code CartContext}, never raw client input.</strong>
 *       Per {@code CartContext}'s javadoc, every price/seller/category field
 *       must already be server-verified before it reaches this service —
 *       {@code RuleEngineService} itself does no re-validation against the
 *       product catalog, so callers are responsible for building a trusted
 *       context first.</li>
 *   <li><strong>{@code PromotionRule.value} parsing.</strong> Per
 *       {@code PromotionRule}'s javadoc, numeric thresholds (e.g.
 *       {@code MIN_ORDER_VALUE}) must be parsed with {@code Long.parseLong}
 *       — never {@code Integer.parseInt} — since UGX values can exceed
 *       {@code Integer.MAX_VALUE}. Implementations delegate parsing to a
 *       {@code RuleFactory}/strategy per {@code RuleType} rather than
 *       inlining parsing logic here.</li>
 *   <li>Malformed or unrecognized {@code PromotionRule} data (bad enum,
 *       unparsable value) must fail the rule closed (treated as not
 *       passed), never open — a data-integrity problem must never
 *       accidentally grant a discount.</li>
 * </ul>
 */
public interface RuleEngineService {

    /** True only if every rule on the promotion passes. Short-circuits on first failure. */
    boolean isEligible(Promotion promotion, CartContext cart);

    /**
     * Evaluates every rule and returns a full per-rule breakdown — does
     * NOT short-circuit. Used for admin/support diagnostics; see
     * {@code RuleEvaluationOutcome} for display-safety notes.
     */
    List<RuleEvaluationOutcome> evaluate(Promotion promotion, CartContext cart);

    /** Evaluates a single rule in isolation. Fails closed on malformed rule data. */
    boolean evaluateRule(PromotionRule rule, CartContext cart);
}