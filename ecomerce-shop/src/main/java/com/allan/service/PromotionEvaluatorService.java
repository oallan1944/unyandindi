package com.allan.service;

import com.allan.dto.CartContext;
import com.allan.dto.PromotionApplicationResult;
import com.allan.model.Promotion;

import java.util.List;

/**
 * Resolves the best applicable {@link Promotion}(s) for a cart, on top of
 * {@link RuleEngineService}'s per-promotion eligibility checks.
 *
 * <p><strong>Security architecture:</strong>
 * <ul>
 *   <li><strong>Only status/window-verified promotions are candidates.</strong>
 *       {@link #findCandidatePromotions(CartContext)} must only return
 *       promotions where {@code Promotion.isLive()} is true (status ACTIVE
 *       and within {@code startsAt}/{@code endsAt}). DRAFT, PAUSED, and
 *       ARCHIVED promotions must never be considered, even if a caller
 *       somehow supplies their ID elsewhere in the flow — there is
 *       deliberately no "evaluate this specific promotionId" method on this
 *       interface, precisely to prevent a client from forcing evaluation
 *       of an otherwise-ineligible promotion.</li>
 *   <li><strong>Deterministic tie-breaking.</strong> When multiple eligible
 *       promotions share the same {@code priority}, selection MUST be
 *       deterministic (e.g. fall back to promotion {@code id} ascending) —
 *       nondeterministic selection on a revenue-affecting decision is both
 *       a correctness bug and a fairness/audit problem.</li>
 *   <li><strong>Stacking/exclusivity resolution</strong> happens here, not
 *       in {@code RuleEngineService}: an {@code exclusive} promotion, once
 *       selected as primary, always suppresses every other promotion for
 *       that cart regardless of their own {@code stackable} flag — see
 *       {@code PromotionApplicationResult} javadoc.</li>
 *   <li><strong>Coupon-gated evaluation revalidates independently.</strong>
 *       {@link #evaluateWithCoupon(CartContext, String)} must call
 *       {@code CouponService.validate(...)} itself rather than trusting a
 *       pre-validated flag passed in by the caller — this evaluator is a
 *       shared choke point and must not assume upstream validation
 *       happened correctly.</li>
 *   <li>All discount math here uses {@code long} UGX arithmetic exclusively;
 *       never {@code double}/{@code float}, per every entity's monetary
 *       field javadoc.</li>
 * </ul>
 */
public interface PromotionEvaluatorService {

    /** Automatic (non-coupon) promotions only. */
    PromotionApplicationResult evaluate(CartContext cart);

    /**
     * Automatic promotions plus the coupon-gated promotion identified by
     * {@code couponCode}, merged according to stacking/exclusivity rules.
     * Revalidates the coupon itself via {@code CouponService}; does not
     * trust {@code cart.couponCode()} alone.
     */
    PromotionApplicationResult evaluateWithCoupon(CartContext cart, String couponCode);

    /**
     * Live (status ACTIVE, within window), scope-matching promotions for
     * this cart — the full candidate set before rule evaluation or
     * stacking resolution. Exposed mainly for admin diagnostics.
     */
    List<Promotion> findCandidatePromotions(CartContext cart);
}